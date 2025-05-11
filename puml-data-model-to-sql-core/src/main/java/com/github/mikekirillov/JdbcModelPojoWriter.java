package com.github.mikekirillov;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.Property;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JdbcModelPojoWriter implements ModelPojoWriter {
    private final String outputModelPath;
    private boolean requiredSpringDataJdbcAnnotations;
    private boolean requiredGetters;
    private boolean requiredSetters;
    private boolean requiredNoArgsConstructor;
    private boolean requiredIdArgConstructor;
    private boolean requiredAllArgsConstructor;

    public JdbcModelPojoWriter(String outputModelPath) {
        this.outputModelPath = outputModelPath;
    }

    @Override
    public void processEntities(List<Entity> entities) {
        for (Entity entity : entities) {
            write(entity);
        }
    }

    @Override
    public void write(Entity entity) {
        String entityName = StringUtils.capitalize(entity.getName());
        Path path = Path.of(outputModelPath, entityName + ".java");
        File file = new File(path.toUri());

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writePackage(writer);
            writeImports(writer, entity);
            writeClassDeclaration(writer, entityName);

            if (!entity.getProperties().isEmpty()) {
                Map<String, String> properties = new HashMap<>();

                // TODO make configuration checks for unit generation sent by plugin

                writeFields(writer, entity, properties);
                writeEmptyConstructor(writer, entityName);
                writeIdConstructor(writer, entity, entityName);
                writeAllArgsConstructor(writer, properties, entityName);
                writeGettersSetters(writer, properties);
            } else {
                writeEmptyConstructor(writer, entityName);
            }

            writeClosingFile(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writePackage(Writer writer) throws IOException {
        writer.write("package com.github.mikekirillov.tdd.model;\n\n");
    }

    private void writeImports(Writer writer, Entity entity) throws IOException {
        List<Property> propertyList = entity.getProperties();

        // TODO for Spring Data JDBC config param check
        writer.write("import org.springframework.data.annotation.Id;\n");

        if (!propertyList.isEmpty()) {
            if (propertyList.stream().anyMatch(property -> property.getType().equals("DATETIME"))) {
                writer.write("import java.sql.Date;\n");
            }

            if (propertyList.stream().anyMatch(property -> property.getType().equals("TIMESTAMP"))) {
                writer.write("import java.util.Date;\n");
            }

            // TODO REMEMBER that scheme could contain both of date types

            // TODO FK property could be used as is (from uml scheme) or as link to other generated POJO
        }
    }

    private void writeClassDeclaration(Writer writer, String entityName) throws IOException {
        writer.write("\npublic class " + entityName + " {\n");
    }

    private void writeFields(Writer writer, Entity entity, Map<String, String> properties) throws IOException {
        for (Property property : entity.getProperties()) {
            String name = camelToSnake(property.getName());
            String type = convertType(property.getType());

            if (property.isPrimaryKey()) {
                writer.write("\t@Id\n");
            }

            writer.write("\tprivate " + type + " " + name + ";\n");
            properties.put(name, type);

            // TODO FK property could be used as is (from uml scheme) or as link to other generated POJO
        }
    }

    private void writeEmptyConstructor(Writer writer, String entityName) throws IOException {
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

        // TODO FK property could be used as is (from uml scheme) or as link to other generated POJO
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

        // TODO FK property could be used as is (from uml scheme) or as link to other generated POJO
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
