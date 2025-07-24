package io.github.mikekirillov.pojo;

import io.github.mikekirillov.model.Entity;
import io.github.mikekirillov.model.PojoConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.mikekirillov.utils.TestUtils.getProperty;
import static org.junit.jupiter.api.Assertions.*;

class MethodWriterTest {
    private MethodWriter writer;
    private PojoConfig pojoConfig;
    private Entity entity;
    private StringBuilder stringBuilder;
    private Map<String, String> properties;

    @BeforeEach
    public void init() {
        stringBuilder = new StringBuilder();
        properties = new HashMap<>();
        properties.put("id", "int");
        properties.put("name", "String");
    }

    @Test
    public void shouldReturnEmptyString() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false)
        ));

        pojoConfig = new PojoConfig();
        pojoConfig.setAllowSpringDataJdbcAnnotations(false);
        pojoConfig.setAllowForeignKeyAsEmbeddedEntity(false);
        pojoConfig.setAllowForeignKeyAsEmbeddedEntityByAggregate(false);
        pojoConfig.setAllowNoArgsConstructor(false);
        pojoConfig.setAllowIdArgConstructor(false);
        pojoConfig.setAllowAllArgsConstructor(false);
        pojoConfig.setAllowGetters(false);
        pojoConfig.setAllowSetters(false);
        pojoConfig.setAllowToStringMethod(false);

        writer = new MethodWriter(pojoConfig, entity, properties);
        writer.writeMethods(stringBuilder);

        assertTrue(stringBuilder.isEmpty());
    }

    @Test
    public void shouldAddGetters() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false)
        ));

        pojoConfig = new PojoConfig();
        pojoConfig.setAllowSpringDataJdbcAnnotations(false);
        pojoConfig.setAllowForeignKeyAsEmbeddedEntity(false);
        pojoConfig.setAllowForeignKeyAsEmbeddedEntityByAggregate(false);
        pojoConfig.setAllowNoArgsConstructor(false);
        pojoConfig.setAllowIdArgConstructor(false);
        pojoConfig.setAllowAllArgsConstructor(false);
        pojoConfig.setAllowGetters(true);
        pojoConfig.setAllowSetters(false);
        pojoConfig.setAllowToStringMethod(false);

        writer = new MethodWriter(pojoConfig, entity, properties);
        writer.writeMethods(stringBuilder);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(8, lines.length);
        assertEquals("", lines[0]);
        assertEquals("\tpublic String getName() {", lines[1]);
        assertEquals("\t\treturn name;", lines[2]);
        assertEquals("\t}", lines[3]);
        assertEquals("", lines[4]);
        assertEquals("\tpublic int getId() {", lines[5]);
        assertEquals("\t\treturn id;", lines[6]);
        assertEquals("\t}", lines[7]);
    }

    @Test
    public void shouldAddSetters() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false)
        ));

        pojoConfig = new PojoConfig();
        pojoConfig.setAllowSpringDataJdbcAnnotations(false);
        pojoConfig.setAllowForeignKeyAsEmbeddedEntity(false);
        pojoConfig.setAllowForeignKeyAsEmbeddedEntityByAggregate(false);
        pojoConfig.setAllowNoArgsConstructor(false);
        pojoConfig.setAllowIdArgConstructor(false);
        pojoConfig.setAllowAllArgsConstructor(false);
        pojoConfig.setAllowGetters(false);
        pojoConfig.setAllowSetters(true);
        pojoConfig.setAllowToStringMethod(false);

        writer = new MethodWriter(pojoConfig, entity, properties);
        writer.writeMethods(stringBuilder);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(8, lines.length);
        assertEquals("", lines[0]);
        assertEquals("\tpublic void setName(String name) {", lines[1]);
        assertEquals("\t\tthis.name = name;", lines[2]);
        assertEquals("\t}", lines[3]);
        assertEquals("", lines[4]);
        assertEquals("\tpublic void setId(int id) {", lines[5]);
        assertEquals("\t\tthis.id = id;", lines[6]);
        assertEquals("\t}", lines[7]);
    }

    @Test
    public void shouldAddToString() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false)
        ));

        pojoConfig = new PojoConfig();
        pojoConfig.setAllowSpringDataJdbcAnnotations(false);
        pojoConfig.setAllowForeignKeyAsEmbeddedEntity(false);
        pojoConfig.setAllowForeignKeyAsEmbeddedEntityByAggregate(false);
        pojoConfig.setAllowNoArgsConstructor(false);
        pojoConfig.setAllowIdArgConstructor(false);
        pojoConfig.setAllowAllArgsConstructor(false);
        pojoConfig.setAllowGetters(false);
        pojoConfig.setAllowSetters(false);
        pojoConfig.setAllowToStringMethod(true);

        writer = new MethodWriter(pojoConfig, entity, properties);
        writer.writeMethods(stringBuilder);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(8, lines.length);
        assertEquals("", lines[0]);
        assertEquals("\t@Override", lines[1]);
        assertEquals("\tpublic String toString() {", lines[2]);
        assertEquals("\t\treturn \"GenderEs{\" +", lines[3]);
        assertEquals("\t\t\t\"name='\" + name + '\\'' +", lines[4]);
        assertEquals("\t\t\t\", id='\" + id + '\\'' +", lines[5]);
        assertEquals("\t\t\t'}';", lines[6]);
        assertEquals("\t}", lines[7]);
    }
}