package com.github.mikekirillov;

import com.github.mikekirillov.enums.UmlRelationType;
import com.github.mikekirillov.model.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.github.mikekirillov.utils.PojoProcessorUtils.convertType;
import static com.github.mikekirillov.utils.PojoProcessorUtils.snakeToCamel;

public class JdbcPojoProcessor implements EntityProcessor {
    private final PojoConfig pojoConfig;
    private final String outputFilePath;
    private final Entity entity;
    private final List<Entity> entities;
    private final List<Relation> relations;

    public JdbcPojoProcessor(PojoConfig pojoConfig,
                             String outputFilePath,
                             Entity entity,
                             List<Entity> entities,
                             List<Relation> relations) {
        this.entity = entity;
        this.outputFilePath = outputFilePath;
        this.entities = entities;
        this.relations = getBridgeEntities(relations);
        this.pojoConfig = pojoConfig;
    }

    @Override
    public String process() {
        StringBuilder stringBuilder = new StringBuilder();
        processEntity(stringBuilder);
        return stringBuilder.toString();
    }

    private void processEntity(StringBuilder stringBuilder) {
        String entityName = snakeToCamel(entity.getName(), true);
        writePackage(stringBuilder);
        writeImports(stringBuilder, entity);
        writeClassDeclaration(stringBuilder, entity, entityName);

        if (entity.getProperties().isEmpty()) {
            writeClosingFile(stringBuilder);
            return;
        }
        Map<String, String> properties = new HashMap<>();
        writeFields(stringBuilder, entity, properties);
        if (pojoConfig.isAllowNoArgsConstructor()) {
            writeNoArgsConstructor(stringBuilder, entityName);
        }
        if (pojoConfig.isAllowIdArgConstructor()) {
            writeIdConstructor(stringBuilder, entity, entityName);
        }
        if (pojoConfig.isAllowAllArgsConstructor()) {
            writeAllArgsConstructor(stringBuilder, properties, entityName);
        }
        if (pojoConfig.isAllowGetters() || pojoConfig.isAllowSetters()) {
            writeGettersSetters(stringBuilder, properties);
        }
        if (pojoConfig.isAllowToStringMethod()) {
            writeToStringMethod(stringBuilder, properties, entityName);
        }
        writeClosingFile(stringBuilder);
    }

    private void writePackage(StringBuilder stringBuilder) {
        String filePath = outputFilePath.replace("/", ".");
        stringBuilder.append("package ").append(filePath).append(";\n\n");
    }

    private void writeImports(StringBuilder stringBuilder, Entity entity) {
        List<Property> propertyList = entity.getProperties();

        if (pojoConfig.isAllowSpringDataJdbcAnnotations()) {
            stringBuilder.append("import org.springframework.data.annotation.Id;\n");
            stringBuilder.append("import org.springframework.data.relational.core.mapping.Table;\n");
            if (pojoConfig.isAllowForeignKeyAsEmbeddedEntity() && entity.getProperties().stream().anyMatch(Property::isForeignKey)) {
                if (pojoConfig.isAllowForeignKeyAsEmbeddedEntityByAggregate()) {
                    stringBuilder.append("import org.springframework.data.relational.core.mapping.Column;\n");
                    stringBuilder.append("import org.springframework.data.jdbc.core.mapping.AggregateReference;\n");

                    // add for main entity for many-to-many cases
                    findMainRelationEntity(entity).ifPresent(relation -> {
                        stringBuilder.append("import org.springframework.data.relational.core.mapping.MappedCollection;\n");
                        stringBuilder.append("import java.util.HashSet;\n");
                        stringBuilder.append("import java.util.Set;\n");
                    });
                } else {
                    stringBuilder.append("import org.springframework.data.relational.core.mapping.MappedCollection;\n");
                }
            }
        }

        if (!propertyList.isEmpty()) {
            if (propertyList.stream().anyMatch(property -> property.getType().equals("DATETIME"))) {
                stringBuilder.append("import java.sql.Date;\n");
            }
            if (propertyList.stream().anyMatch(property -> property.getType().equals("TIMESTAMP"))) {
                stringBuilder.append("import java.util.Date;\n");
            }
            // TODO REMEMBER that scheme could contain both of date types
        }
    }

    private Optional<Relation> findMainRelationEntity(Entity entity) {
        return relations.stream()
                .filter(relation -> {
                    var right = relation.getRightEntity();
                    var left = relation.getLeftEntity();
                    return checkRelationIsOneOrMany(right) && checkRelationEqualsMainEntity(right, entity)
                            || checkRelationIsOneOrMany(left) && checkRelationEqualsMainEntity(left, entity);
                })
                .findFirst();
    }

    private boolean checkRelationIsOneOrMany(EntityRelation entityRelation) {
        return entityRelation.getRelationType().equals(UmlRelationType.ONE_OR_MANY)
                || entityRelation.getRelationType().equals(UmlRelationType.ONE_OR_MANY_REVERTED);
    }

    private boolean checkRelationEqualsMainEntity(EntityRelation relation, Entity entity) {
        var relationName = relation.getEntity().getName();
        return !entity.getName().contains("_") && relationName.startsWith(entity.getName());
    }

    private void writeClassDeclaration(StringBuilder stringBuilder, Entity entity, String entityName) {
        stringBuilder.append("\n@Table(\"").append(entity.getName()).append("\")");
        stringBuilder.append("\npublic class ").append(entityName).append(" {\n");
    }

    private void writeFields(StringBuilder stringBuilder, Entity entity, Map<String, String> properties) {
        for (Property property : entity.getProperties()) {
            String fieldName, fieldType;

            if (pojoConfig.isAllowSpringDataJdbcAnnotations() && property.isPrimaryKey()) {
                stringBuilder.append("\t@Id\n");
            }

            if (pojoConfig.isAllowSpringDataJdbcAnnotations() && pojoConfig.isAllowForeignKeyAsEmbeddedEntity() && property.isForeignKey()) {
                String propertyName = property.getName().toLowerCase();
                Entity foundOne = this.entities.stream()
                        .filter(it -> propertyName.contains(it.getName().toLowerCase()))
                        .findFirst()
                        .orElseThrow();
                fieldName = snakeToCamel(foundOne.getName(), false);

                if (pojoConfig.isAllowForeignKeyAsEmbeddedEntityByAggregate()) {
                    String[] propertySplit = property.getName().split("_");

                    // add not secondary entity reference field only: for training_client add client field, not training,
                    // because training class will contain set of training_client entity
                    if (filterRelationsAsBridgeEntity(entity).isEmpty()
                            || propertySplit.length == 2 && entity.getName().endsWith(propertySplit[0])) {
                        stringBuilder.append("\t@Column(\"").append(property.getName()).append("\")\n");
                        fieldType = "AggregateReference<" + snakeToCamel(foundOne.getName(), true) + ", String>";
                        writeField(stringBuilder, properties, fieldType, fieldName);
                    }
                } else {
                    String pkName = foundOne.getProperties().stream()
                            .filter(Property::isPrimaryKey)
                            .map(Property::getName)
                            .findFirst()
                            .orElseThrow();
                    stringBuilder.append("\t@MappedCollection(idColumn = \"").append(pkName).append("\")\n");
                    fieldType = snakeToCamel(foundOne.getName(), true);
                    writeField(stringBuilder, properties, fieldType, fieldName);
                }
            } else {
                fieldName = snakeToCamel(property.getName(), false);
                fieldType = convertType(property.getType());
                writeField(stringBuilder, properties, fieldType, fieldName);
            }
        }

        // add bonus field to main entity for many-to-many cases: when we've got training/client/training_client entities
        // training class will also contain set of training_client entity
        if (pojoConfig.isAllowSpringDataJdbcAnnotations() && pojoConfig.isAllowForeignKeyAsEmbeddedEntity() && pojoConfig.isAllowForeignKeyAsEmbeddedEntityByAggregate()) {
            findMainRelationEntity(entity).ifPresent(relation -> {
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
                    stringBuilder.append("\t@MappedCollection(idColumn = \"").append(propertyName.get()).append("\")\n");
                    String fieldName = snakeToCamel(entityName.get(), false) + "s";
                    String fieldType = "Set<" + snakeToCamel(entityName.get(), true) + ">";
                    writeManyToManyField(stringBuilder, properties, fieldType, fieldName);
                }
            });
        }
    }

    private List<Relation> filterRelationsAsBridgeEntity(Entity entity) {
        return relations.stream()
                .filter(relation -> {
                    var right = relation.getRightEntity();
                    var left = relation.getLeftEntity();
                    return checkRelationIsOneOrMany(right) && checkRelationEqualsEntity(right, entity)
                            || checkRelationIsOneOrMany(left) && checkRelationEqualsEntity(left, entity);
                })
                .toList();
    }

    private boolean checkRelationEqualsEntity(EntityRelation relation, Entity entity) {
        var relationName = relation.getEntity().getName();
        return relationName.contains("_") && relationName.equalsIgnoreCase(entity.getName());
    }

    private void writeField(StringBuilder stringBuilder, Map<String, String> properties, String type, String name) {
        stringBuilder.append("\tprivate ").append(type).append(" ").append(name).append(";\n");
        properties.put(name, type);
    }

    private void writeManyToManyField(StringBuilder stringBuilder, Map<String, String> properties, String type, String name) {
        stringBuilder.append("\tprivate ").append(type).append(" ").append(name).append(" = new HashSet<>();\n");
        properties.put(name, type);
    }

    private void writeNoArgsConstructor(StringBuilder stringBuilder, String entityName) {
        stringBuilder.append("\n\tpublic ").append(entityName).append("() {}\n");
    }

    private void writeIdConstructor(StringBuilder stringBuilder, Entity entity, String entityName) {
        entity.getProperties().stream()
                .filter(Property::isPrimaryKey)
                .findFirst()
                .ifPresent(primaryKey -> {
                    String name = primaryKey.getName();
                    String type = convertType(primaryKey.getType());
                    stringBuilder.append("\n\tpublic ").append(entityName).append("(").append(type).append(" ").append(name).append(") {\n");
                    stringBuilder.append("\t\tthis.").append(name).append(" = ").append(name).append(";\n");
                    stringBuilder.append("\t}\n");
                });
    }

    private void writeAllArgsConstructor(StringBuilder stringBuilder, Map<String, String> properties, String entityName) {
        StringBuilder constructorParameters = new StringBuilder();
        StringBuilder declaring = new StringBuilder();
        properties.forEach((name, type) -> {
            constructorParameters.append(type).append(" ").append(name).append(", ");
            declaring.append("\t\tthis.").append(name).append(" = ").append(name).append(";\n");
        });
        String typeNameString = constructorParameters.substring(0, constructorParameters.length() - 2);
        stringBuilder.append("\n\tpublic ").append(entityName).append("(").append(typeNameString).append(") {\n");
        stringBuilder.append(declaring);
        stringBuilder.append("\t}\n");
    }

    private void writeGettersSetters(StringBuilder stringBuilder, Map<String, String> properties) {
        properties.forEach((name, type) -> {
            String getterName = "get" + StringUtils.capitalize(name);
            String setterName = "set" + StringUtils.capitalize(name);
            if (pojoConfig.isAllowGetters()) {
                stringBuilder.append("\n\tpublic ").append(type).append(" ").append(getterName).append("() {\n");
                stringBuilder.append("\t\treturn ").append(name).append(";\n");
                stringBuilder.append("\t}\n");
            }
            if (pojoConfig.isAllowSetters()) {
                stringBuilder.append("\n\tpublic void ").append(setterName).append("(").append(type).append(" ").append(name).append(") {\n");
                stringBuilder.append("\t\tthis.").append(name).append(" = ").append(name).append(";\n");
                stringBuilder.append("\t}\n");
            }
        });
    }

    private void writeToStringMethod(StringBuilder stringBuilder, Map<String, String> properties, String entityName) {
        stringBuilder.append("\n\t@Override");
        stringBuilder.append("\n\tpublic String toString() {");
        stringBuilder.append("\n\t\treturn \"").append(entityName).append("{\" +");

        Optional<String> firstKey = properties.keySet().stream().findFirst();
        properties.forEach((key, values) -> {
            if (firstKey.isPresent() && key.equals(firstKey.get())) {
                stringBuilder.append("\n\t\t\t\"").append(key).append("='\" + ").append(key).append(" + '\\'' +");
            } else {
                stringBuilder.append("\n\t\t\t\", ").append(key).append("='\" + ").append(key).append(" + '\\'' +");
            }
        });
        stringBuilder.append("\n\t\t\t'}';");
        stringBuilder.append("\n\t}\n");
    }

    private void writeClosingFile(StringBuilder stringBuilder) {
        stringBuilder.append("}\n");
    }

    private List<Relation> getBridgeEntities(List<Relation> relations) {
        return relations.stream()
                .filter(relation -> checkEntityIsBridge(relation.getRightEntity())
                        && checkBridgeContainsOtherEntityName(relation.getRightEntity(), relation.getLeftEntity())
                        || checkEntityIsBridge(relation.getLeftEntity())
                        && checkBridgeContainsOtherEntityName(relation.getLeftEntity(), relation.getRightEntity()))
                .toList();
    }

    private boolean checkEntityIsBridge(EntityRelation entityRelation) {
        String name = entityRelation.getEntity().getName();
        if (name.contains("_")) {
            String[] split = name.split("_");
            if (split.length == 2) {
                List<String> entNames = entities.stream()
                        .map(Entity::getName)
                        .toList();
                return checkRelationIsOneOrMany(entityRelation)
                        && entNames.contains(split[0])
                        && entNames.contains(split[1]);
            }
            return false;
        }
        return false;
    }

    private boolean checkBridgeContainsOtherEntityName(EntityRelation bridgeEntity, EntityRelation otherEntity) {
        return bridgeEntity.getEntity().getName().toLowerCase().contains(otherEntity.getEntity().getName().toLowerCase());
    }
}
