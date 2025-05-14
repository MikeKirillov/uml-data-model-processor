package com.github.mikekirillov;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.Property;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.github.mikekirillov.utils.ModelPojoWriterUtils.convertType;
import static com.github.mikekirillov.utils.ModelPojoWriterUtils.snakeToCamel;
import static com.github.mikekirillov.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class JdbcModelPojoWriterTest {

    @Test
    public void shouldProcessEntityEasyPojo() throws IOException {
        boolean fkAsClass = false;
        List<Entity> entities = returnEntitiesWithFk();
        JdbcModelPojoWriter writer = new JdbcModelPojoWriter(
                POJO_GENERATOR_DIR,
                entities,
                fkAsClass,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );
        writer.write();
        assertEntities(entities, fkAsClass);
    }

    @Test
    public void shouldProcessEntityPojoWithSnakeProperty() throws IOException {
        boolean fkAsClass = true;
        List<Entity> entities = returnEntitiesWithFkSnake();
        JdbcModelPojoWriter writer = new JdbcModelPojoWriter(
                POJO_GENERATOR_DIR,
                entities,
                fkAsClass,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );
        writer.write();
        assertEntities(entities, fkAsClass);
    }

    private void assertEntities(List<Entity> entities, boolean fkAsClass) throws IOException {
        for (Entity entity : entities) {
            String capitalizedEntityName = snakeToCamel(entity.getName(), true);
            File createdFile = createJavaFile(capitalizedEntityName);
            assertTrue(createdFile.exists());
            assertTrue(createdFile.isFile());

            List<String> lines = readFile(createdFile.toPath());
            int linesSize = lines.size();
            assertEquals("package com.github.mikekirillov.tdd.model;", lines.get(0));
            assertEquals("public class " + capitalizedEntityName + " {", lines.get(1));

            List<Property> properties = entity.getProperties();
            assertProperties(properties, entities, lines, fkAsClass);
            assertEquals("}", lines.get(linesSize - 1));
            deleteJavaFile(createdFile);
        }
    }

    private void assertProperties(List<Property> properties, List<Entity> entities, List<String> lines, boolean fkAsClass) {
        for (int i = 0; i < properties.size(); i++) {
            Property property = properties.get(i);
            if (fkAsClass && property.isForeignKey()) {
                var foundOne = entities.stream()
                        .map(Entity::getName)
                        .filter(itName -> property.getName().contains(itName.toLowerCase()))
                        .findFirst()
                        .orElseThrow();
                assertEquals(
                        "private " + snakeToCamel(foundOne, true) + " " + snakeToCamel(foundOne, false) + ";",
                        lines.get(2 + i)
                );
            } else {
                assertEquals(
                        "private " + convertType(property.getType()) + " " + snakeToCamel(property.getName(), false) + ";",
                        lines.get(2 + i)
                );
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