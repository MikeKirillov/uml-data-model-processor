package com.github.mikekirillov;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.Property;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.github.mikekirillov.utils.ClassGeneratorUtils.convertType;
import static com.github.mikekirillov.utils.ClassGeneratorUtils.camelize;
import static com.github.mikekirillov.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

// TODO DELETE
class JdbcPojoWriterTest {

    @Test
    public void shouldProcessEntityEasyPojo() throws IOException {
        boolean requiresSpringDataJdbcAnnotations = false;
        boolean allowForeignKeyAsEmbeddedEntity = false;
        List<Entity> entities = returnEntitiesWithFk();
        JdbcPojoWriter writer = new JdbcPojoWriter(
                POJO_GENERATOR_DIR,
                entities,
                new ArrayList<>(),
                requiresSpringDataJdbcAnnotations,
                allowForeignKeyAsEmbeddedEntity,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );
        writer.write();
        assertEntities(entities, requiresSpringDataJdbcAnnotations, allowForeignKeyAsEmbeddedEntity);
    }

    @Test
    public void shouldProcessEntityPojoWithSnakeProperty() throws IOException {
        boolean requiresSpringDataJdbcAnnotations = true;
        boolean allowForeignKeyAsEmbeddedEntity = true;
        List<Entity> entities = returnEntitiesWithFkSnake();
        JdbcPojoWriter writer = new JdbcPojoWriter(
                POJO_GENERATOR_DIR,
                entities,
                new ArrayList<>(),
                requiresSpringDataJdbcAnnotations,
                allowForeignKeyAsEmbeddedEntity,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );
        writer.write();
        assertEntities(entities, requiresSpringDataJdbcAnnotations, allowForeignKeyAsEmbeddedEntity);
    }

    private void assertEntities(List<Entity> entities,
                                boolean requiresSpringDataJdbcAnnotations,
                                boolean allowForeignKeyAsEmbeddedEntity) throws IOException {
        for (Entity entity : entities) {
            String capitalizedEntityName = camelize(entity.getName(), true);
            File createdFile = createJavaFile(capitalizedEntityName);
            assertTrue(createdFile.exists());
            assertTrue(createdFile.isFile());

            List<String> lines = readFile(createdFile.toPath());
            int linesSize = lines.size();
            assertEquals("package com.github.mikekirillov.tdd.model;", lines.get(0));

            if (allowForeignKeyAsEmbeddedEntity) {
                if (entity.getProperties().stream().anyMatch(Property::isForeignKey)) {
                    assertEquals("public class " + capitalizedEntityName + " {", lines.get(5));
                } else {
                    assertEquals("public class " + capitalizedEntityName + " {", lines.get(4));
                }
            } else {
                assertEquals("public class " + capitalizedEntityName + " {", lines.get(2));
            }

            List<Property> properties = entity.getProperties();
            assertProperties(properties, entity, entities, lines, requiresSpringDataJdbcAnnotations, allowForeignKeyAsEmbeddedEntity);
            assertEquals("}", lines.get(linesSize - 1));
            deleteJavaFile(createdFile);
        }
    }

    private void assertProperties(List<Property> properties,
                                  Entity entity,
                                  List<Entity> entities,
                                  List<String> lines,
                                  boolean requiresSpringDataJdbcAnnotations,
                                  boolean allowForeignKeyAsEmbeddedEntity) {
        for (int i = 0; i < properties.size(); i++) {
            Property property = properties.get(i);
            if (requiresSpringDataJdbcAnnotations && property.isForeignKey()) {
                var foundOne = entities.stream()
                        .map(Entity::getName)
                        .filter(itName -> property.getName().contains(itName.toLowerCase()))
                        .findFirst()
                        .orElseThrow();
                if (allowForeignKeyAsEmbeddedEntity) {
                    assertEquals(
                            "private " + camelize(foundOne, true) + " " + camelize(foundOne, false) + ";",
                            lines.get(8 + i)
                    );
                } else {
                    assertEquals(
                            "private " + camelize(foundOne, true) + " " + camelize(foundOne, false) + ";",
                            lines.get(3 + i)
                    );
                }
            } else {
                if (allowForeignKeyAsEmbeddedEntity) {
                    if (entity.getProperties().stream().anyMatch(Property::isForeignKey)) {
                        assertEquals(
                                "private " + convertType(property.getType()) + " " + camelize(property.getName(), false) + ";",
                                lines.get(7 + i)
                        );
                    } else {
                        assertEquals(
                                "private " + convertType(property.getType()) + " " + camelize(property.getName(), false) + ";",
                                lines.get(6 + i)
                        );
                    }
                } else {
                    assertEquals(
                            "private " + convertType(property.getType()) + " " + camelize(property.getName(), false) + ";",
                            lines.get(3 + i)
                    );
                }
            }
        }
    }

    private File createJavaFile(String name) {
        return new File(POJO_GENERATOR_DIR, name + ".java");
    }

    private void deleteJavaFile(File file) {
        file.delete();
        file.getParentFile().delete();
    }

    private List<String> readFile(Path path) throws IOException {
        return Files.readAllLines(path).stream()
                .filter(line -> !line.isBlank())
                .map(String::trim)
                .toList();
    }
}