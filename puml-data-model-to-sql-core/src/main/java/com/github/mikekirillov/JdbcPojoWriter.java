package com.github.mikekirillov;

import com.github.mikekirillov.enums.UmlRelationType;
import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.EntityRelation;
import com.github.mikekirillov.model.Property;
import com.github.mikekirillov.model.Relation;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.github.mikekirillov.utils.PojoProcessorUtils.convertType;
import static com.github.mikekirillov.utils.PojoProcessorUtils.camelize;

public class JdbcPojoWriter {
    private final String outputModelPath;
    private final List<Entity> entities;
    private final List<Relation> relations;
    private final boolean requiresSpringDataJdbcAnnotations;
    private final boolean allowForeignKeyAsEmbeddedEntity;
    private final boolean allowForeignKeyAsEmbeddedEntityByAggregate;
    private final boolean requiresNoArgsConstructor;
    private final boolean requiresIdArgConstructor;
    private final boolean requiresAllArgsConstructor;
    private final boolean requiresGetters;
    private final boolean requiresSetters;
    private final boolean requiresToStringMethod;

    public JdbcPojoWriter(String outputModelPath,
                          List<Entity> entities,
                          List<Relation> relations,
                          boolean requiresSpringDataJdbcAnnotations,
                          boolean allowForeignKeyAsEmbeddedEntity,
                          boolean allowForeignKeyAsEmbeddedEntityByAggregate,
                          boolean requiresNoArgsConstructor,
                          boolean requiresIdArgConstructor,
                          boolean requiresAllArgsConstructor,
                          boolean requiresGetters,
                          boolean requiresSetters,
                          boolean requiresToStringMethod) {
        this.outputModelPath = outputModelPath;
        this.entities = entities;
        this.relations = getBridgeEntities(relations);
        this.allowForeignKeyAsEmbeddedEntity = allowForeignKeyAsEmbeddedEntity;
        this.allowForeignKeyAsEmbeddedEntityByAggregate = allowForeignKeyAsEmbeddedEntityByAggregate;
        this.requiresSpringDataJdbcAnnotations = requiresSpringDataJdbcAnnotations;
        this.requiresNoArgsConstructor = requiresNoArgsConstructor;
        this.requiresIdArgConstructor = requiresIdArgConstructor;
        this.requiresAllArgsConstructor = requiresAllArgsConstructor;
        this.requiresGetters = requiresGetters;
        this.requiresSetters = requiresSetters;
        this.requiresToStringMethod = requiresToStringMethod;
    }

    public void write() {
        for (Entity entity : entities) {
            processEntity(entity);
        }
    }

    private void processEntity(Entity entity) {
        String entityName = camelize(entity.getName(), true);
        Path path = Path.of(outputModelPath, entityName + ".java");
        File file = new File(path.toUri());

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writePackage(writer);
            writeImports(writer, entity);
            writeClassDeclaration(writer, entity, entityName);
            if (!entity.getProperties().isEmpty()) {
                Map<String, String> properties = new HashMap<>();

                writeFields(writer, entity, properties);

                if (requiresNoArgsConstructor) {
                    writeNoArgsConstructor(writer, entityName);
                }
                if (requiresIdArgConstructor) {
                    writeIdConstructor(writer, entity, entityName);
                }
                if (requiresAllArgsConstructor) {
                    writeAllArgsConstructor(writer, properties, entityName);
                }
                if (requiresGetters || requiresSetters) {
                    writeGettersSetters(writer, properties);
                }
                if (requiresToStringMethod) {
                    writeToStringMethod(writer, properties, entityName);
                }
            } else {
                if (requiresNoArgsConstructor) {
                    writeNoArgsConstructor(writer, entityName);
                }
            }
            writeClosingFile(writer);
        } catch (IOException | UnsupportedOperationException e) {
            file.delete();
            throw new RuntimeException(e);
        }
    }

    private void writePackage(Writer writer) throws IOException {
        writer.write("package com.github.mikekirillov.tdd.model;\n\n");
    }

    private void writeImports(Writer writer, Entity entity) throws IOException {
        List<Property> propertyList = entity.getProperties();

        if (requiresSpringDataJdbcAnnotations) {
            writer.write("import org.springframework.data.annotation.Id;\n");
            writer.write("import org.springframework.data.relational.core.mapping.Table;\n");
            if (allowForeignKeyAsEmbeddedEntity && entity.getProperties().stream().anyMatch(Property::isForeignKey)) {
                if (allowForeignKeyAsEmbeddedEntityByAggregate) {
                    writer.write("import org.springframework.data.relational.core.mapping.Column;\n");
                    writer.write("import org.springframework.data.jdbc.core.mapping.AggregateReference;\n");

                    // add for main entity for many-to-many cases
                    findMainRelationEntity(entity).ifPresent(relation -> {
                        try {
                            writer.write("import org.springframework.data.relational.core.mapping.MappedCollection;\n");
                            writer.write("import java.util.HashSet;\n");
                            writer.write("import java.util.Set;\n");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    writer.write("import org.springframework.data.relational.core.mapping.MappedCollection;\n");
                }
            }
        }

        if (!propertyList.isEmpty()) {
            if (propertyList.stream().anyMatch(property -> property.getType().equals("DATETIME"))) {
                writer.write("import java.sql.Date;\n");
            }

            if (propertyList.stream().anyMatch(property -> property.getType().equals("TIMESTAMP"))) {
                writer.write("import java.util.Date;\n");
            }

            // TODO REMEMBER that scheme could contain both of date types
        }
    }

    private void writeClassDeclaration(Writer writer, Entity entity, String entityName) throws IOException {
        writer.write("\n@Table(\"" + entity.getName() + "\")");
        writer.write("\npublic class " + entityName + " {\n");
    }

    private void writeFields(Writer writer, Entity entity, Map<String, String> properties) throws IOException {
        for (Property property : entity.getProperties()) {
            String fieldName, fieldType;

            if (requiresSpringDataJdbcAnnotations && property.isPrimaryKey()) {
                writer.write("\t@Id\n");
            }

            if (requiresSpringDataJdbcAnnotations && allowForeignKeyAsEmbeddedEntity && property.isForeignKey()) {
                String propertyName = property.getName().toLowerCase();
                Entity foundOne = entities.stream()
                        .filter(it -> propertyName.contains(it.getName().toLowerCase()))
                        .findFirst()
                        .orElseThrow();
                fieldName = camelize(foundOne.getName(), false);

                if (allowForeignKeyAsEmbeddedEntityByAggregate) {
                    String[] propertySplit = property.getName().split("_");

                    // add not secondary entity reference field only: for training_client add client field, not training,
                    // because training class will contain set of training_client entity
                    if (filterRelationsAsBridgeEntity(entity).isEmpty()
                            || propertySplit.length == 2 && entity.getName().endsWith(propertySplit[0])) {
                        writer.write("\t@Column(\"" + property.getName() + "\")\n");
                        fieldType = "AggregateReference<" + camelize(foundOne.getName(), true) + ", String>";
                        writeField(writer, properties, fieldType, fieldName);
                    }
                } else {
                    String pkName = foundOne.getProperties().stream()
                            .filter(Property::isPrimaryKey)
                            .map(Property::getName)
                            .findFirst()
                            .orElseThrow();
                    writer.write("\t@MappedCollection(idColumn = \"" + pkName + "\")\n");
                    fieldType = camelize(foundOne.getName(), true);
                    writeField(writer, properties, fieldType, fieldName);
                }
            } else {
                fieldName = camelize(property.getName(), false);
                fieldType = convertType(property.getType());
                writeField(writer, properties, fieldType, fieldName);
            }
        }

        // add bonus field to main entity for many-to-many cases: when we've got training/client/training_client entities
        // training class will also contain set of training_client entity
        if (requiresSpringDataJdbcAnnotations && allowForeignKeyAsEmbeddedEntity && allowForeignKeyAsEmbeddedEntityByAggregate) {
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
                    try {
                        writer.write("\t@MappedCollection(idColumn = \"" + propertyName.get() + "\")\n");
                        String fieldName = camelize(entityName.get(), false) + "s";
                        String fieldType = "Set<" + camelize(entityName.get(), true) + ">";
                        writeManyToManyField(writer, properties, fieldType, fieldName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    private void writeField(Writer writer, Map<String, String> properties, String type, String name) throws IOException {
        writer.write("\tprivate " + type + " " + name + ";\n");
        properties.put(name, type);
    }

    private void writeManyToManyField(Writer writer, Map<String, String> properties, String type, String name) throws IOException {
        writer.write("\tprivate " + type + " " + name + " = new HashSet<>();\n");
        properties.put(name, type);
    }

    private void writeNoArgsConstructor(Writer writer, String entityName) throws IOException {
        writer.write("\n\tpublic " + entityName + "() {}\n");
    }

    private void writeIdConstructor(Writer writer, Entity entity, String entityName) throws IOException {
        entity.getProperties().stream()
                .filter(Property::isPrimaryKey)
                .findFirst()
                .ifPresent(primaryKey -> {
                    String name = primaryKey.getName();
                    String type = convertType(primaryKey.getType());

                    try {
                        writer.write("\n\tpublic " + entityName + "(" + type + " " + name + ") {\n");
                        writer.write("\t\tthis." + name + " = " + name + ";\n");
                        writer.write("\t}\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void writeAllArgsConstructor(Writer writer, Map<String, String> properties, String entityName) throws IOException {
        StringBuilder constructorParameters = new StringBuilder();
        StringBuilder declaring = new StringBuilder();

        properties.forEach((name, type) -> {
            constructorParameters.append(type).append(" ").append(name).append(", ");
            declaring.append("\t\tthis.").append(name).append(" = ").append(name).append(";\n");
        });

        String typeNameString = constructorParameters.substring(0, constructorParameters.length() - 2);

        writer.write("\n\tpublic " + entityName + "(" + typeNameString + ") {\n");
        writer.write(declaring.toString());
        writer.write("\t}\n");
    }

    private void writeGettersSetters(Writer writer, Map<String, String> properties) throws IOException {
        properties.forEach((name, type) -> {
            String getterName = "get" + StringUtils.capitalize(name);
            String setterName = "set" + StringUtils.capitalize(name);

            try {
                if (requiresGetters) {
                    writer.write("\n\tpublic " + type + " " + getterName + "() {\n");
                    writer.write("\t\treturn " + name + ";\n");
                    writer.write("\t}\n");
                }
                if (requiresSetters) {
                    writer.write("\n\tpublic void " + setterName + "(" + type + " " + name + ") {\n");
                    writer.write("\t\tthis." + name + " = " + name + ";\n");
                    writer.write("\t}\n");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void writeToStringMethod(Writer writer, Map<String, String> properties, String entityName) throws IOException {
        writer.write("\n\t@Override");
        writer.write("\n\tpublic String toString() {");
        writer.write("\n\t\treturn \"" + entityName + "{\" +");

        Optional<String> firstKey = properties.keySet().stream().findFirst();

        properties.forEach((key, values) -> {
            try {
                if (firstKey.isPresent() && key.equals(firstKey.get())) {
                    writer.write("\n\t\t\t\"" + key + "='\" + " + key + " + '\\'' +");
                } else {
                    writer.write("\n\t\t\t\", " + key + "='\" + " + key + " + '\\'' +");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        writer.write("\n\t\t\t'}';");
        writer.write("\n\t}\n");
    }

    private void writeClosingFile(Writer writer) throws IOException {
        writer.write("}\n");
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

    private boolean checkRelationIsOneOrMany(EntityRelation entityRelation) {
        return entityRelation.getRelationType().equals(UmlRelationType.ONE_OR_MANY)
                || entityRelation.getRelationType().equals(UmlRelationType.ONE_OR_MANY_REVERTED);
    }

    private boolean checkRelationIsExactlyOne(EntityRelation entityRelation) {
        return entityRelation.getRelationType().equals(UmlRelationType.EXACTLY_ONE);
    }


    private boolean checkBridgeContainsOtherEntityName(EntityRelation bridgeEntity, EntityRelation otherEntity) {
        return bridgeEntity.getEntity().getName().toLowerCase().contains(otherEntity.getEntity().getName().toLowerCase());
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

    private boolean checkRelationEqualsEntity(EntityRelation relation, Entity entity) {
        var relationName = relation.getEntity().getName();
        return relationName.contains("_") && relationName.equalsIgnoreCase(entity.getName());
    }

    private boolean checkRelationEqualsMainEntity(EntityRelation relation, Entity entity) {
        var relationName = relation.getEntity().getName();
        return !entity.getName().contains("_") && relationName.startsWith(entity.getName());
    }
}
