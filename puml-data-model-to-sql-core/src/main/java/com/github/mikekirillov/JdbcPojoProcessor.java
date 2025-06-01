package com.github.mikekirillov;

import com.github.mikekirillov.model.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.github.mikekirillov.utils.ClassGeneratorUtils.*;

// TODO DELETE
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
        this.relations = relations;
        this.pojoConfig = pojoConfig;
    }

    @Override
    public String generate() {
        StringBuilder stringBuilder = new StringBuilder();
        processEntity(stringBuilder);
        return stringBuilder.toString();
    }

    private void processEntity(StringBuilder stringBuilder) {
        String entityName = camelize(entity.getName(), true);
        writePackage(stringBuilder);
        writeImports(stringBuilder);
        writeClassDeclaration(stringBuilder, entityName);

        if (entity.getProperties().isEmpty()) {
            writeClosingFile(stringBuilder);
            return;
        }
        Map<String, String> properties = new HashMap<>();
        writeFields(stringBuilder, properties);
        if (pojoConfig.isAllowNoArgsConstructor()) {
            writeNoArgsConstructor(stringBuilder, entityName);
        }
        if (pojoConfig.isAllowIdArgConstructor()) {
            writeIdConstructor(stringBuilder, entityName);
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

    private void writeImports(StringBuilder stringBuilder) {
        List<Property> propertyList = entity.getProperties();

        if (pojoConfig.isAllowSpringDataJdbcAnnotations()) {
            stringBuilder.append("import org.springframework.data.annotation.Id;\n");
            stringBuilder.append("import org.springframework.data.relational.core.mapping.Table;\n");
            if (pojoConfig.isAllowForeignKeyAsEmbeddedEntity() && entity.getProperties().stream().anyMatch(Property::isForeignKey)) {
                if (pojoConfig.isAllowForeignKeyAsEmbeddedEntityByAggregate()) {
                    stringBuilder.append("import org.springframework.data.relational.core.mapping.Column;\n");
                    stringBuilder.append("import org.springframework.data.jdbc.core.mapping.AggregateReference;\n");

                    // add these imports for main entity for many-to-many cases
                    findMainRelationEntity().ifPresent(relation -> {
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

    private void writeClassDeclaration(StringBuilder stringBuilder, String entityName) {
        if (pojoConfig.isAllowSpringDataJdbcAnnotations()) {
            stringBuilder.append("\n@Table(\"").append(entity.getName()).append("\")");
        }
        stringBuilder.append("\npublic class ").append(entityName).append(" {\n");
    }

    private void writeFields(StringBuilder stringBuilder, Map<String, String> properties) {
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
                fieldName = camelize(foundOne.getName(), false);

                if (pojoConfig.isAllowForeignKeyAsEmbeddedEntityByAggregate()) {
                    String[] propertySplit = property.getName().split("_");

                    if (filterRelationsAsBridgeEntity().isEmpty()
                            // for many-to-many cases:
                            // to not adding main entity to bridge entities because bridge will be added to main and
                            // add into bridge the secondary entity reference field only
                            // then for training_client add client field, not training,
                            // because training class will contain set of training_client entity
                            || propertySplit.length == 2 && entity.getName().endsWith(propertySplit[0])) {
                        stringBuilder.append("\t@Column(\"").append(property.getName()).append("\")\n");
                        fieldType = "AggregateReference<" + camelize(foundOne.getName(), true) + ", String>";
                        writeField(stringBuilder, properties, fieldType, fieldName);
                    }
                } else {
                    String pkName = foundOne.getProperties().stream()
                            .filter(Property::isPrimaryKey)
                            .map(Property::getName)
                            .findFirst()
                            .orElseThrow();
                    stringBuilder.append("\t@MappedCollection(idColumn = \"").append(pkName).append("\")\n");
                    fieldType = camelize(foundOne.getName(), true);
                    writeField(stringBuilder, properties, fieldType, fieldName);
                }
            } else {
                fieldName = camelize(property.getName(), false);
                fieldType = convertType(property.getType());
                writeField(stringBuilder, properties, fieldType, fieldName);
            }
        }

        // add bonus field to main entity for many-to-many cases: when we've got training/client/training_client entities
        // training class will also contain set of training_client entity
        if (pojoConfig.isAllowSpringDataJdbcAnnotations() && pojoConfig.isAllowForeignKeyAsEmbeddedEntity() && pojoConfig.isAllowForeignKeyAsEmbeddedEntityByAggregate()) {
            findMainRelationEntity().ifPresent(relation -> {
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
                    String fieldName = camelize(entityName.get(), false) + "s";
                    String fieldType = "Set<" + camelize(entityName.get(), true) + ">";
                    writeManyToManyField(stringBuilder, properties, fieldType, fieldName);
                }
            });
        }
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

    private void writeIdConstructor(StringBuilder stringBuilder, String entityName) {
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

    private Optional<Relation> findMainRelationEntity() {
        // main is an entity that is chosen as entity that contains many-to-many mark of other entity.
        // returns relation with main and bridge entities
        return relations.stream()
                .filter(relation -> checkRelationAndMainEntityEqByName(relation, entity))
                .findFirst();
    }

    private List<Relation> filterRelationsAsBridgeEntity() {
        // filter relations with current bridge entity
        // returns empty if current entity is not bridge
        return relations.stream()
                .filter(relation -> checkRelationAndEntityEqByName(relation, entity))
                .toList();
    }
}
