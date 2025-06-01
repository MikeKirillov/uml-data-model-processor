package com.github.mikekirillov.pojo;

import com.github.mikekirillov.enums.UmlRelationType;
import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.EntityRelation;
import com.github.mikekirillov.model.PojoConfig;
import com.github.mikekirillov.model.Relation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.github.mikekirillov.utils.TestUtils.getProperty;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class ImportWriterTest {
    private ImportWriter writer;
    private PojoConfig pojoConfig;
    private Relation relation;
    private StringBuilder stringBuilder;

    @BeforeEach
    public void init() {
        pojoConfig = Mockito.mock(PojoConfig.class);
        stringBuilder = new StringBuilder();
    }

    @Test
    public void shouldReturnEmptyString() {
        Entity entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false)
        ));
        relation = Mockito.mock(Relation.class);
        writer = new ImportWriter(pojoConfig, entity, List.of(relation));
        setPojoConfig(false, false, false);
        writer.writeImports(stringBuilder);

        assertTrue(stringBuilder.toString().isEmpty());
    }

    @Test
    public void shouldAddImportUtilDate() {
        Entity entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false),
                getProperty("createdDate", "TIMESTAMP", false, false, false)
        ));
        relation = Mockito.mock(Relation.class);
        writer = new ImportWriter(pojoConfig, entity, List.of(relation));
        setPojoConfig(false, false, false);
        writer.writeImports(stringBuilder);

        assertEquals("import java.util.Date;\n", stringBuilder.toString());
    }

    @Test
    public void shouldAddImportSqlDate() {
        Entity entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false),
                getProperty("createdDate", "DATETIME", false, false, false)
        ));
        relation = Mockito.mock(Relation.class);
        writer = new ImportWriter(pojoConfig, entity, List.of(relation));
        setPojoConfig(false, false, false);
        writer.writeImports(stringBuilder);

        assertEquals("import java.sql.Date;\n", stringBuilder.toString());
    }

    @Test
    public void shouldAddImportSpringDataJdbcAnnotations() {
        Entity entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false)
        ));
        relation = Mockito.mock(Relation.class);
        writer = new ImportWriter(pojoConfig, entity, List.of(relation));
        setPojoConfig(true, false, false);
        writer.writeImports(stringBuilder);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(2, lines.length);
        assertEquals("import org.springframework.data.annotation.Id;", lines[0]);
        assertEquals("import org.springframework.data.relational.core.mapping.Table;", lines[1]);
    }

    @Test
    public void shouldAddImportMappedCollection() {
        Entity entity = new Entity("state", "st", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(128)", true, false, false),
                getProperty("gender_id", "INT", true, false, true)
        ));
        relation = Mockito.mock(Relation.class);
        writer = new ImportWriter(pojoConfig, entity, List.of(relation));
        setPojoConfig(true, true, false);
        writer.writeImports(stringBuilder);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(3, lines.length);
        assertEquals("import org.springframework.data.annotation.Id;", lines[0]);
        assertEquals("import org.springframework.data.relational.core.mapping.Table;", lines[1]);
        assertEquals("import org.springframework.data.relational.core.mapping.MappedCollection;", lines[2]);
    }

    @Test
    public void shouldAddImportColumnAndAggregateReference() {
        Entity entity = new Entity("state", "st", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(128)", true, false, false),
                getProperty("gender_id", "INT", true, false, true)
        ));
        relation = returnUnfitRelation();
        writer = new ImportWriter(pojoConfig, entity, List.of(relation));
        setPojoConfig(true, true, true);
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
        Entity entity = new Entity("state", "st", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(128)", true, false, false),
                getProperty("gender_id", "INT", true, false, true)
        ));
        relation = returnFitRelation(entity);
        writer = new ImportWriter(pojoConfig, entity, List.of(relation));
        setPojoConfig(true, true, true);
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

    private void setPojoConfig(boolean allowSpringDataJdbcAnnotations, boolean allowForeignKeyAsEmbeddedEntity, boolean allowForeignKeyAsEmbeddedEntityByAggregate) {
        given(pojoConfig.isAllowSpringDataJdbcAnnotations())
                .willReturn(allowSpringDataJdbcAnnotations);
        given(pojoConfig.isAllowForeignKeyAsEmbeddedEntity())
                .willReturn(allowForeignKeyAsEmbeddedEntity);
        given(pojoConfig.isAllowForeignKeyAsEmbeddedEntityByAggregate())
                .willReturn(allowForeignKeyAsEmbeddedEntityByAggregate);
    }

    private Relation returnUnfitRelation() {
        return new Relation(
                new EntityRelation(
                        new Entity("disc", null, List.of(
                                getProperty("id", "INT", true, true, false),
                                getProperty("name", "VARCHAR(128)", true, false, false)
                        )),
                        UmlRelationType.ONE_OR_MANY
                ),
                new EntityRelation(
                        new Entity("location", null, List.of(
                                getProperty("id", "INT", true, true, false),
                                getProperty("name", "VARCHAR(128)", true, false, false)
                        )),
                        UmlRelationType.EXACTLY_ONE
                )
        );
    }

    private Relation returnFitRelation(Entity entity) {
        return new Relation(
                new EntityRelation(
                        entity,
                        UmlRelationType.ONE_OR_MANY
                ),
                new EntityRelation(
                        new Entity("location", null, List.of(
                                getProperty("id", "INT", true, true, false),
                                getProperty("name", "VARCHAR(128)", true, false, false)
                        )),
                        UmlRelationType.EXACTLY_ONE
                )
        );
    }
}