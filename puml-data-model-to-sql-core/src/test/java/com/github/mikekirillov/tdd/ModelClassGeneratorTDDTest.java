package com.github.mikekirillov.tdd;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.Property;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.mikekirillov.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelClassGeneratorTDDTest {
    private List<Entity> entities;
    private File parentDir;

    @BeforeEach
    void init() {
        entities = returnEntitiesWithFkSnake();
        Path path = Path.of(POJO_GENERATOR_DIR);
        parentDir = new File(path.toUri());
    }

    @AfterEach
    void out() {
        for (String fileName : Objects.requireNonNull(parentDir.list())) {
            Path path = Path.of(POJO_GENERATOR_DIR + fileName);
            File fileToDelete = new File(path.toUri());

            fileToDelete.delete();
            fileToDelete.getParentFile().delete();
        }
    }

    @Test
    public void shouldGeneratePOJOs() {
        processEntities(entities);

        assertTrue(parentDir.exists());
        assertEquals(entities.size(), Objects.requireNonNull(parentDir.list()).length);
    }

    @Test
    public void shouldGeneratePOJOsWithInnerFkClass() {
        processEntitiesWithInnerFkClass(entities);

        assertTrue(parentDir.exists());
        assertEquals(entities.size(), Objects.requireNonNull(parentDir.list()).length);
    }

    private void processEntities(List<Entity> entities) {
        for (Entity entity : entities) {
            generatePojo(entity, false, null);
        }
    }

    private void processEntitiesWithInnerFkClass(List<Entity> entities) {
        List<Entity> fks = entities.stream()
                .filter(entity -> entity.getProperties().stream().noneMatch(Property::isForeignKey))
                .toList();

        for (Entity entity : fks) {
            generatePojo(entity, false, null);
        }

        List<Entity> hasFks = entities.stream()
                .filter(entity -> entity.getProperties().stream().anyMatch(Property::isForeignKey))
                .toList();

        for (Entity entity : hasFks) {
            generatePojo(entity, true, fks);
        }
    }

    private void generatePojo(Entity entity, boolean fkAsClass, List<Entity> fks) {
        String entityName = snakeToCamel(entity.getName(), true);
        Path path = createDirAndFile(entityName);

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writePackage(writer);
            writeImports(writer, entity);
            writeClassDeclaration(writer, entityName);

            if (!entity.getProperties().isEmpty()) {
                Map<String, String> properties = new HashMap<>();

                writeFields(writer, entity, properties, fkAsClass, fks);
                writeNoArgsConstructor(writer, entityName);
                writeIdConstructor(writer, entity, entityName);
                writeAllArgsConstructor(writer, properties, entityName);
                writeGettersSetters(writer, properties);
                writeToStringMethod(writer, properties, entityName);
            } else {
                writeNoArgsConstructor(writer, entityName);
            }

            writeClosingFile(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path createDirAndFile(String entityName) {
        Path path = Path.of(POJO_GENERATOR_DIR, entityName + ".java");
        File file = new File(path.toUri());

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return path;
    }

    private void writePackage(Writer writer) throws IOException {
        writer.write("package com.github.mikekirillov.tdd.model;\n\n");
    }

    private void writeImports(Writer writer, Entity entity) throws IOException {
        List<Property> propertyList = entity.getProperties();

        // for Spring Data JDBC config param check
        writer.write("import org.springframework.data.annotation.Id;\n");

        if (!propertyList.isEmpty()) {
            if (propertyList.stream().anyMatch(property -> property.getType().equals("DATETIME"))) {
                writer.write("import java.sql.Date;\n");
            }

            if (propertyList.stream().anyMatch(property -> property.getType().equals("TIMESTAMP"))) {
                writer.write("import java.util.Date;\n");
            }
        }
    }

    private void writeClassDeclaration(Writer writer, String entityName) throws IOException {
        writer.write("\npublic class " + entityName + " {\n");
    }

    private void writeFields(Writer writer, Entity entity, Map<String, String> properties, boolean fkAsClass, List<Entity> fks) throws IOException {
        for (Property property : entity.getProperties()) {
            String name, type;

            // for Spring Data JDBC config param check
            if (property.isPrimaryKey()) {
                writer.write("\t@Id\n");
            }

            if (property.isForeignKey() && fkAsClass) {
                String propertyName = property.getName().toLowerCase();
                String foundOne = fks.stream()
                        .map(Entity::getName)
                        .filter(itName -> propertyName.contains(itName.toLowerCase()))
                        .findFirst()
                        .orElseThrow();
                name = snakeToCamel(foundOne, false);
                type = snakeToCamel(foundOne, true);

            } else {
                name = snakeToCamel(property.getName(), false);
                type = convertType(property.getType());
            }

            writer.write("\tprivate " + type + " " + name + ";\n");
            properties.put(name, type);
        }
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
                writer.write("\n\tpublic " + type + " " + getterName + "() {\n");
                writer.write("\t\treturn " + name + ";\n");
                writer.write("\t}\n");
                writer.write("\n\tpublic void " + setterName + "(" + type + " " + name + ") {\n");
                writer.write("\t\tthis." + name + " = " + name + ";\n");
                writer.write("\t}\n");
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

        writer.write("\n\t\t'}';");
        writer.write("\n\t}\n");
    }

    private void writeClosingFile(Writer writer) throws IOException {
        writer.write("}\n");
    }

    private String convertType(String type) {
        return switch (type.toLowerCase()) {
            case "int", "integer" -> "int";
            case "boolean" -> "boolean";
            case "datetime", "timestamp" -> "Date";
            default -> "String";
        };
    }

    private String snakeToCamel(String camel, boolean capitalize) {
        if (camel.contains("_")) {
            camel = Stream.of(camel.split("_"))
                    .map(StringUtils::capitalize)
                    .collect(Collectors.joining());
        }

        return capitalize ? StringUtils.capitalize(camel) : StringUtils.uncapitalize(camel);
    }
}
