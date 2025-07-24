package io.github.mikekirillov.pojo;

import io.github.mikekirillov.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.mikekirillov.utils.ClassGeneratorUtils.*;

public class FieldWriter {
    private final PojoConfig pojoConfig;
    private final Entity entity;
    private final List<Entity> allEntities;
    private final List<Relation> relations;

    public FieldWriter(PojoConfig pojoConfig, Entity entity, List<Entity> allEntities, List<Relation> relations) {
        this.pojoConfig = pojoConfig;
        this.entity = entity;
        this.allEntities = allEntities;
        this.relations = relations;
    }

    public void writeFields(StringBuilder stringBuilder, Map<String, String> properties) {
        writeSimpleFields(stringBuilder, properties);
        if (pojoConfig.isAllowSpringDataJdbcAnnotations() && pojoConfig.isAllowForeignKeyAsEmbeddedEntity()) {
            writeForeignKeyFields(stringBuilder, properties);
            if (pojoConfig.isAllowForeignKeyAsEmbeddedEntityByAggregate()) {
                writeManyToManyFields(stringBuilder, properties);
            }
        }
    }

    private void writeSimpleFields(StringBuilder stringBuilder, Map<String, String> properties) {
        for (Property property : entity.getProperties()) {
            if (!property.isForeignKey() || !pojoConfig.isAllowForeignKeyAsEmbeddedEntity()) {
                annotateIdForPkField(stringBuilder, property);
                String fieldName = camelize(property.getName(), false);
                String fieldType = convertType(property.getType());
                writeField(stringBuilder, properties, fieldType, fieldName);
            }
        }
    }

    private void writeForeignKeyFields(StringBuilder stringBuilder, Map<String, String> properties) {
        for (Property property : entity.getProperties()) {
            if (property.isForeignKey()) {
                handleForeignKeyField(stringBuilder, properties, property);
            }
        }
    }

    private void writeManyToManyFields(StringBuilder stringBuilder, Map<String, String> properties) {
        Optional<Relation> mainRelation = findCurrentEntityAsMainRelation(relations, entity);
        if (mainRelation.isPresent()) {
            Relation relation = mainRelation.get();
            writeManyToManyField(stringBuilder, properties, relation);
        }
    }

    private void annotateIdForPkField(StringBuilder stringBuilder, Property property) {
        if (pojoConfig.isAllowSpringDataJdbcAnnotations() && property.isPrimaryKey()) {
            stringBuilder.append("\t@Id").append("\n");
        }
    }

    // Специализированная обработка внешних ключей
    private void handleForeignKeyField(StringBuilder stringBuilder, Map<String, String> properties, Property foreignKeyProperty) {
        String propertyName = foreignKeyProperty.getName().toLowerCase();
        Entity entityByFkPropName = allEntities.stream()
                .filter(it -> propertyName.contains(it.getName().toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Referenced entity not found"));
        // Использовать конкретные сценарии для разных способов настройки внешнего ключа
        if (pojoConfig.isAllowForeignKeyAsEmbeddedEntityByAggregate()) {
            // Вариант с AggregateReference
            handleAggregateReferenceField(stringBuilder, properties, foreignKeyProperty, entityByFkPropName);
        } else {
            // Другой способ
            handleDirectReferenceField(stringBuilder, properties, entityByFkPropName);
        }
    }

    private void handleAggregateReferenceField(StringBuilder stringBuilder, Map<String, String> properties, Property foreignKeyProperty, Entity entityByFkPropName) {
        // for many-to-many cases:
        // to not adding main entity to bridge entities because bridge will be added to main and
        // add into bridge the secondary entity reference field only
        // then for training_client add client field, not training,
        // because training class will contain set of training_client entity
        if (filterRelationsAsBridgeForCurrentEntity().isEmpty() || propertyIsBridgeAndEntityIsMain(foreignKeyProperty)) {
            stringBuilder.append("\t@Column(\"")
                    .append(foreignKeyProperty.getName())
                    .append("\")")
                    .append("\n");
            String fieldType = "AggregateReference<" + camelize(entityByFkPropName.getName(), true) + ", String>";
            String fieldName = camelize(entityByFkPropName.getName(), false);
            writeField(stringBuilder, properties, fieldType, fieldName);
        }
    }

    private boolean propertyIsBridgeAndEntityIsMain(Property foreignKeyProperty) {
        String[] propertySplit = foreignKeyProperty.getName().split("_");
        return propertySplit.length == 2 && entity.getName().endsWith(propertySplit[0]);
    }

    private void handleDirectReferenceField(StringBuilder stringBuilder, Map<String, String> properties, Entity entityByFkPropName) {
        String pkName = entityByFkPropName.getProperties().stream()
                .filter(Property::isPrimaryKey)
                .map(Property::getName)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("PK property not found"));
        stringBuilder.append("\t@MappedCollection(idColumn = \"")
                .append(pkName)
                .append("\")")
                .append("\n");
        String fieldType = camelize(entityByFkPropName.getName(), true);
        String fieldName = camelize(entityByFkPropName.getName(), false);
        writeField(stringBuilder, properties, fieldType, fieldName);
    }

    // Общая логика записи простого поля
    private void writeField(StringBuilder stringBuilder, Map<String, String> properties, String type, String name) {
        stringBuilder.append("\tprivate ")
                .append(type)
                .append(" ")
                .append(name)
                .append(";")
                .append("\n");
        properties.put(name, type);
    }

    private void writeManyToManyField(StringBuilder stringBuilder, Map<String, String> properties, Relation relation) {
        List<EntityRelation> entityRelations = new ArrayList<>();
        entityRelations.add(relation.getLeftEntity());
        entityRelations.add(relation.getRightEntity());
        Optional<String> propertyName = entityRelations.stream()
                .flatMap(it -> it.getEntity().getProperties().stream())
                .map(Property::getName)
                .filter(property -> property.startsWith(entity.getName()))
                .findFirst();
        Optional<String> entityName = entityRelations.stream()
                .map(entityRelation -> entityRelation.getEntity().getName())
                .filter(it -> it.startsWith(entity.getName()))
                .findFirst();
        if (propertyName.isPresent() && entityName.isPresent()) {
            stringBuilder.append("\t@MappedCollection(idColumn = \"")
                    .append(propertyName.get())
                    .append("\")")
                    .append("\n");
            String fieldName = camelize(entityName.get(), false) + "s";
            String fieldType = "Set<" + camelize(entityName.get(), true) + ">";
            stringBuilder.append("\tprivate ")
                    .append(fieldType)
                    .append(" ")
                    .append(fieldName)
                    .append(" = new HashSet<>();")
                    .append("\n");
            properties.put(fieldName, fieldType);
        }
    }

    private List<Relation> filterRelationsAsBridgeForCurrentEntity() {
        // filter relations with current bridge entity
        // returns empty if current entity is not bridge
        return relations.stream()
                .filter(relation -> checkRelationAndEntityEqByName(relation, entity))
                .toList();
    }
}
