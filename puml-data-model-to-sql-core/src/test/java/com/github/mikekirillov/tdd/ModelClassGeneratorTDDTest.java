package com.github.mikekirillov.tdd;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.Property;
import org.apache.commons.lang3.StringUtils;
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

import static com.github.mikekirillov.utils.TestUtils.returnEntitiesWIthFk;

public class ModelClassGeneratorTDDTest {

    @Test
    public void shouldGeneratePOJOs() {
        List<Entity> entities = returnEntitiesWIthFk();

        System.out.println(entities);
        System.out.println(entities.size());

        processEntities(entities);
    }

    private void processEntities(List<Entity> entities) {
        for (Entity entity : entities) {
            generatePojo(entity);
        }
    }

    private void generatePojo(Entity entity) {
        String entityName = StringUtils.capitalize(entity.getName());
        Path path = Path.of("src/test/java/com/github/mikekirillov/tdd/model/", entityName + ".java");
        File file = new File(path.toUri());

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("package com.github.mikekirillov.tdd.model;\n\n");
            writer.write("import org.springframework.data.annotation.Id;\n\n");
            writer.write("public class " + entityName + " {\n");

            Map<String, String> properties = new HashMap<>();

            // generating fields
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

            // generating all fields constructor
            if (!properties.isEmpty()) {
                StringBuilder typeNames = new StringBuilder();
                StringBuilder declaring = new StringBuilder();

                properties.forEach((name, type) -> {
                    typeNames.append(type).append(" ").append(name).append(", ");
                    declaring.append("\t\tthis.").append(name).append(" = ").append(name).append(";\n");
                });

                String typeNameString = typeNames.substring(0, typeNames.length() - 2);

                writer.write("\n\tpublic " + entityName + "(" + typeNameString + ") {\n");
                writer.write(declaring.toString());
                writer.write("\t}\n");
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
