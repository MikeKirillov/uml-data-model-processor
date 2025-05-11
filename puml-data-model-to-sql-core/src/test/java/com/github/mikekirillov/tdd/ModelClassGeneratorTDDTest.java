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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.mikekirillov.utils.TestUtils.POJO_GENERATOR_DIR;
import static com.github.mikekirillov.utils.TestUtils.returnEntitiesWIthFk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelClassGeneratorTDDTest {
    private List<Entity> entities;
    private File parentDir;

    @BeforeEach
    void init() {
        entities = returnEntitiesWIthFk();
        Path path = Path.of(POJO_GENERATOR_DIR);
        parentDir = new File(path.toUri());
    }

    @AfterEach
    void out() {
        for (String fileName : Objects.requireNonNull(parentDir.list())) {
            Path path = Path.of(POJO_GENERATOR_DIR + fileName);
            File fileToDelete = new File(path.toUri());

            // fileToDelete.delete();
            // fileToDelete.getParentFile().delete();
        }
    }

    @Test
    public void shouldGeneratePOJOs() {
        processEntities(entities);

        assertTrue(parentDir.exists());
        assertEquals(entities.size(), Objects.requireNonNull(parentDir.list()).length);
        assertTrue(
                Objects.requireNonNull(parentDir.list())[0].toLowerCase()
                        .contains(entities.get(0).getName().toLowerCase())
        );
        assertTrue(
                Objects.requireNonNull(parentDir.list())[1].toLowerCase()
                        .contains(entities.get(1).getName().toLowerCase())
        );
    }

    private void processEntities(List<Entity> entities) {
        for (Entity entity : entities) {
            generatePojo(entity);
        }
    }

    private void generatePojo(Entity entity) {
        String entityName = StringUtils.capitalize(entity.getName());
        Path path = Path.of(POJO_GENERATOR_DIR, entityName + ".java");
        File file = new File(path.toUri());

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            // generating package
            writer.write("package com.github.mikekirillov.tdd.model;\n\n");

            // generating imports
            List<Property> propertyList = entity.getProperties();
            if (propertyList.stream().anyMatch(property -> property.getType().equals("DATETIME"))) {
                writer.write("import java.sql.Date;\n");
            }
            if (propertyList.stream().anyMatch(property -> property.getType().equals("TIMESTAMP"))) {
                writer.write("import java.util.Date;\n");
            }

            // for Spring Data JDBC config param check
            writer.write("import org.springframework.data.annotation.Id;\n");

            // generating class declaring
            writer.write("\npublic class " + entityName + " {\n");

            // generating fields
            Map<String, String> properties = new HashMap<>();
            for (Property property : entity.getProperties()) {
                String propertyType = getPropertyType(property.getType());
                String propertyName = camelToSnake(property.getName());

                if (property.isPrimaryKey()) {
                    writer.write("\t@Id\n");
                }

                writer.write("\tprivate " + propertyType + " " + propertyName + ";\n");
                properties.put(propertyName, propertyType);
            }

            // generating empty constructor
            writer.write("\n\tpublic " + entityName + "() {}\n");

            // generating id constructor
            entity.getProperties().stream()
                    .filter(Property::isPrimaryKey)
                    .findFirst()
                    .ifPresent(primaryKey -> {
                        String name = primaryKey.getName();
                        String type = getPropertyType(primaryKey.getType());

                        try {
                            writer.write("\n\tpublic " + entityName + "(" + type + " " + name + ") {\n");
                            writer.write("\t\tthis." + name + " = " + name + ";\n");
                            writer.write("\t}\n");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            if (!properties.isEmpty()) {
                // generating all fields constructor
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

                // generating getters and setters
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

            writer.write("}\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldGeneratePOJOsWithInnerFkClass() {
        processEntitiesWithInnerFkClass(entities);

        assertTrue(parentDir.exists());
        assertEquals(entities.size(), Objects.requireNonNull(parentDir.list()).length);
        assertTrue(
                Objects.requireNonNull(parentDir.list())[0].toLowerCase()
                        .contains(entities.get(0).getName().toLowerCase())
        );
        assertTrue(
                Objects.requireNonNull(parentDir.list())[1].toLowerCase()
                        .contains(entities.get(1).getName().toLowerCase())
        );
    }

    private void processEntitiesWithInnerFkClass(List<Entity> entities) {
        List<Entity> isFks = entities.stream()
                .filter(entity -> entity.getProperties().stream().noneMatch(Property::isForeignKey))
                .toList();

        for (Entity entity : isFks) {
            generatePojo(entity);
        }

        List<Entity> hasFks = entities.stream()
                .filter(entity -> entity.getProperties().stream().anyMatch(Property::isForeignKey))
                .toList();

        for (Entity entity : hasFks) {
            generatePojoWithInnerFkClass(entity, isFks);
        }
    }

    private void generatePojoWithInnerFkClass(Entity entity, List<Entity> isFks) {
        String entityName = StringUtils.capitalize(entity.getName());
        Path path = Path.of(POJO_GENERATOR_DIR, entityName + ".java");
        File file = new File(path.toUri());

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            // generating package
            writer.write("package com.github.mikekirillov.tdd.model;\n\n");

            // generating imports
            List<Property> propertyList = entity.getProperties();
            if (propertyList.stream().anyMatch(property -> property.getType().equals("DATETIME"))) {
                writer.write("import java.sql.Date;\n");
            }
            if (propertyList.stream().anyMatch(property -> property.getType().equals("TIMESTAMP"))) {
                writer.write("import java.util.Date;\n");
            }

            // for Spring Data JDBC config param check
            writer.write("import org.springframework.data.annotation.Id;\n");

            // generating class declaring
            writer.write("\npublic class " + entityName + " {\n");

            // generating fields
            Map<String, String> properties = new HashMap<>();
            for (Property property : entity.getProperties()) {
                String propertyType = getPropertyType(property.getType());
                String propertyName = camelToSnake(property.getName());

                if (property.isPrimaryKey()) {
                    writer.write("\t@Id\n");
                }

                writer.write("\tprivate " + propertyType + " " + propertyName + ";\n");
                properties.put(propertyName, propertyType);
            }

            // generating empty constructor
            writer.write("\n\tpublic " + entityName + "() {}\n");

            // generating id constructor
            entity.getProperties().stream()
                    .filter(Property::isPrimaryKey)
                    .findFirst()
                    .ifPresent(primaryKey -> {
                        String name = primaryKey.getName();
                        String type = getPropertyType(primaryKey.getType());

                        try {
                            writer.write("\n\tpublic " + entityName + "(" + type + " " + name + ") {\n");
                            writer.write("\t\tthis." + name + " = " + name + ";\n");
                            writer.write("\t}\n");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            if (!properties.isEmpty()) {
                // generating all fields constructor
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

                // generating getters and setters
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

            writer.write("}\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getPropertyType(String type) {
        return switch (type.toLowerCase()) {
            case "int", "integer" -> "int";
            case "boolean" -> "boolean";
            case "datetime", "timestamp" -> "Date";
            default -> "String";
        };
    }

    private String camelToSnake(String camel) {
        if (camel.contains("_")) {
            String snake = Stream.of(camel.split("_"))
                    .map(StringUtils::capitalize)
                    .collect(Collectors.joining());

            return StringUtils.uncapitalize(snake);
        }

        return camel;
    }
}
