package com.github.mikekirillov.pojo;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.PojoConfig;
import com.github.mikekirillov.model.Relation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.github.mikekirillov.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class ImportWriterTest {
    private ImportWriter writer;
    private PojoConfig pojoConfig;
    private Relation relation;
    private StringBuilder stringBuilder;
    private Entity entity;

    @BeforeEach
    public void init() {
        stringBuilder = new StringBuilder();
    }

    @Test
    public void shouldReturnEmptyString() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false)
        ));
        relation = Mockito.mock(Relation.class);
        pojoConfig = new PojoConfig(false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false);
        writer = new ImportWriter(pojoConfig, entity, List.of(relation));
        writer.writeImports(stringBuilder);

        assertTrue(stringBuilder.isEmpty());
    }

    @Test
    public void shouldAddImportUtilDate() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false),
                getProperty("createdDate", "TIMESTAMP", false, false, false)
        ));
        relation = Mockito.mock(Relation.class);
        pojoConfig = new PojoConfig(false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false);
        writer = new ImportWriter(pojoConfig, entity, List.of(relation));
        writer.writeImports(stringBuilder);

        assertEquals("import java.util.Date;\n", stringBuilder.toString());
    }

    @Test
    public void shouldAddImportSqlDate() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false),
                getProperty("createdDate", "DATETIME", false, false, false)
        ));
        relation = Mockito.mock(Relation.class);
        pojoConfig = new PojoConfig(false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false);
        writer = new ImportWriter(pojoConfig, entity, List.of(relation));
        writer.writeImports(stringBuilder);

        assertEquals("import java.sql.Date;\n", stringBuilder.toString());
    }

    @Test
    public void shouldAddImportSpringDataJdbcAnnotations() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false)
        ));
        relation = Mockito.mock(Relation.class);
        pojoConfig = new PojoConfig(true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false);
        writer = new ImportWriter(pojoConfig, entity, List.of(relation));
        writer.writeImports(stringBuilder);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(2, lines.length);
        assertEquals("import org.springframework.data.annotation.Id;", lines[0]);
        assertEquals("import org.springframework.data.relational.core.mapping.Table;", lines[1]);
    }

    @Test
    public void shouldAddImportMappedCollection() {
        entity = new Entity("state", "st", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(128)", true, false, false),
                getProperty("gender_id", "INT", true, false, true)
        ));
        relation = Mockito.mock(Relation.class);
        pojoConfig = new PojoConfig(true,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false);
        writer = new ImportWriter(pojoConfig, entity, List.of(relation));
        writer.writeImports(stringBuilder);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(3, lines.length);
        assertEquals("import org.springframework.data.annotation.Id;", lines[0]);
        assertEquals("import org.springframework.data.relational.core.mapping.Table;", lines[1]);
        assertEquals("import org.springframework.data.relational.core.mapping.MappedCollection;", lines[2]);
    }

    @Test
    public void shouldAddImportColumnAndAggregateReference() {
        entity = new Entity("state", "st", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(128)", true, false, false),
                getProperty("gender_id", "INT", true, false, true)
        ));
        relation = returnUnfitRelation();
        pojoConfig = new PojoConfig(true,
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                false);
        writer = new ImportWriter(pojoConfig, entity, List.of(relation));
        writer.writeImports(stringBuilder);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(4, lines.length);
        assertEquals("import org.springframework.data.annotation.Id;", lines[0]);
        assertEquals("import org.springframework.data.relational.core.mapping.Table;", lines[1]);
        assertEquals("import org.springframework.data.relational.core.mapping.Column;", lines[2]);
        assertEquals("import org.springframework.data.jdbc.core.mapping.AggregateReference;", lines[3]);
    }

    @Test
    public void shouldAddImportMappedCollectionAndSet() {
        entity = new Entity("state", "st", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(128)", true, false, false),
                getProperty("gender_id", "INT", true, false, true)
        ));
        relation = returnFitRelation(entity);
        pojoConfig = new PojoConfig(true,
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                false);
        writer = new ImportWriter(pojoConfig, entity, List.of(relation));
        writer.writeImports(stringBuilder);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(7, lines.length);
        assertEquals("import org.springframework.data.annotation.Id;", lines[0]);
        assertEquals("import org.springframework.data.relational.core.mapping.Table;", lines[1]);
        assertEquals("import org.springframework.data.relational.core.mapping.Column;", lines[2]);
        assertEquals("import org.springframework.data.jdbc.core.mapping.AggregateReference;", lines[3]);
        assertEquals("import org.springframework.data.relational.core.mapping.MappedCollection;", lines[4]);
        assertEquals("import java.util.HashSet;", lines[5]);
        assertEquals("import java.util.Set;", lines[6]);
    }
}
