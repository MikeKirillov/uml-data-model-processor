package com.github.mikekirillov.pojo;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.PojoConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static com.github.mikekirillov.utils.TestUtils.returnPojoConfigFullFalse;
import static org.junit.jupiter.api.Assertions.*;

class ClassBuilderTest {
    private ClassBuilder builder;
    private PojoConfig pojoConfig;
    private String outputFilePath;
    private Entity entity;

    @BeforeEach
    public void init() {
        outputFilePath = "com/github/mikekirillov/pojo";
    }

    @Test
    public void shouldAddClassBasicHeader() {
        entity = new Entity("gender_es", "g", new ArrayList<>());
        pojoConfig = returnPojoConfigFullFalse();
        builder = new ClassBuilder(pojoConfig, outputFilePath, entity, new ArrayList<>(), new ArrayList<>());
        String result = builder.build();
        String[] lines = result.split("\n");

        assertEquals(5, lines.length);
        assertEquals("package com.github.mikekirillov.pojo;", lines[0]);
        assertEquals("", lines[1]);
        assertEquals("", lines[2]);
        assertEquals("public class GenderEs {", lines[3]);
        assertEquals("}", lines[4]);
    }

    @Test
    public void shouldAddClassBasicHeaderAndSpringJdbc() {
        entity = new Entity("gender_es", "g", new ArrayList<>());

        pojoConfig = new PojoConfig();
        pojoConfig.setAllowSpringDataJdbcAnnotations(true);
        pojoConfig.setAllowForeignKeyAsEmbeddedEntity(false);
        pojoConfig.setAllowForeignKeyAsEmbeddedEntityByAggregate(false);
        pojoConfig.setAllowNoArgsConstructor(false);
        pojoConfig.setAllowIdArgConstructor(false);
        pojoConfig.setAllowAllArgsConstructor(false);
        pojoConfig.setAllowGetters(false);
        pojoConfig.setAllowSetters(false);
        pojoConfig.setAllowToStringMethod(false);

        builder = new ClassBuilder(pojoConfig, outputFilePath, entity, new ArrayList<>(), new ArrayList<>());
        String result = builder.build();
        String[] lines = result.split("\n");

        assertEquals(8, lines.length);
        assertEquals("package com.github.mikekirillov.pojo;", lines[0]);
        assertEquals("", lines[1]);
        assertEquals("import org.springframework.data.annotation.Id;", lines[2]);
        assertEquals("import org.springframework.data.relational.core.mapping.Table;", lines[3]);
        assertEquals("", lines[4]);
        assertEquals("@Table(\"gender_es\")", lines[5]);
        assertEquals("public class GenderEs {", lines[6]);
        assertEquals("}", lines[7]);
    }
}