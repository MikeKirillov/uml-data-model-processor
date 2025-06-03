package com.github.mikekirillov.pojo;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.PojoConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.mikekirillov.utils.TestUtils.getProperty;
import static org.junit.jupiter.api.Assertions.*;

class ConstructorWriterTest {
    private ConstructorWriter writer;
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
        pojoConfig = new PojoConfig(false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false);
        writer = new ConstructorWriter(pojoConfig, entity, properties);
        writer.writeConstructors(stringBuilder);

        assertTrue(stringBuilder.isEmpty());
    }

    @Test
    public void shouldAddNoArgsConstructor() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false)
        ));
        pojoConfig = new PojoConfig(false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                false);
        writer = new ConstructorWriter(pojoConfig, entity, properties);
        writer.writeConstructors(stringBuilder);

        assertEquals("\n\tpublic GenderEs() {}\n", stringBuilder.toString());
    }

    @Test
    public void shouldAddIdArgsConstructor() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false)
        ));
        pojoConfig = new PojoConfig(false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false);
        writer = new ConstructorWriter(pojoConfig, entity, properties);
        writer.writeConstructors(stringBuilder);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(4, lines.length);
        assertEquals("", lines[0]);
        assertEquals("\tpublic GenderEs(int id) {", lines[1]);
        assertEquals("\t\tthis.id = id;", lines[2]);
        assertEquals("\t}", lines[3]);
    }

    @Test
    public void shouldAddAllArgsConstructor() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false)
        ));
        pojoConfig = new PojoConfig(false,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false);
        writer = new ConstructorWriter(pojoConfig, entity, properties);
        writer.writeConstructors(stringBuilder);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(5, lines.length);
        assertEquals("", lines[0]);
        assertEquals("\tpublic GenderEs(String name, int id) {", lines[1]);
        assertEquals("\t\tthis.name = name;", lines[2]);
        assertEquals("\t\tthis.id = id;", lines[3]);
        assertEquals("\t}", lines[4]);
    }
}