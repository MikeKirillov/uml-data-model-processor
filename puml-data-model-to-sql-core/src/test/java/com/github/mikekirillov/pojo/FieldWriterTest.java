package com.github.mikekirillov.pojo;

import com.github.mikekirillov.enums.UmlRelationType;
import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.EntityRelation;
import com.github.mikekirillov.model.PojoConfig;
import com.github.mikekirillov.model.Relation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.mikekirillov.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class FieldWriterTest {
    private FieldWriter writer;
    private PojoConfig pojoConfig;
    private Entity entity;
    private Relation relation;
    private StringBuilder stringBuilder;
    private Map<String, String> properties;

    @BeforeEach
    public void init() {
        pojoConfig = Mockito.mock(PojoConfig.class);
        stringBuilder = new StringBuilder();
        properties = new HashMap<>();
    }

    @Test
    public void shouldAddSimpleFieldsOnly() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false)
        ));
        relation = Mockito.mock(Relation.class);
        writer = new FieldWriter(pojoConfig, entity, List.of(entity), List.of(relation));
        setPojoConfig(false, false, false);
        writer.writeFields(stringBuilder, properties);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(2, lines.length);
        assertEquals("\tprivate int id;", lines[0]);
        assertEquals("\tprivate String name;", lines[1]);
    }

    @Test
    public void shouldAddSimpleFieldsAndFkOnly() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(128)", true, false, false),
                getProperty("gender_id", "INT", true, false, true)
        ));
        relation = Mockito.mock(Relation.class);
        writer = new FieldWriter(pojoConfig, entity, List.of(entity), List.of(relation));
        setPojoConfig(false, false, false);
        writer.writeFields(stringBuilder, properties);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(3, lines.length);
        assertEquals("\tprivate int id;", lines[0]);
        assertEquals("\tprivate String name;", lines[1]);
        assertEquals("\tprivate int genderId;", lines[2]);
    }

    @Test
    public void shouldAddFkIfSpringDataJdbc() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(128)", true, false, false),
                getProperty("gender_id", "INT", true, false, true)
        ));
        relation = Mockito.mock(Relation.class);
        writer = new FieldWriter(pojoConfig, entity, List.of(entity), List.of(relation));
        setPojoConfig(true, false, false);
        writer.writeFields(stringBuilder, properties);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(4, lines.length);
        assertEquals("\t@Id", lines[0]);
        assertEquals("\tprivate int id;", lines[1]);
        assertEquals("\tprivate String name;", lines[2]);
        assertEquals("\tprivate int genderId;", lines[3]);
    }

    @Test
    public void shouldThrowExceptionIdReferencedEntityNotFound() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(128)", true, false, false),
                getProperty("gender_id", "INT", true, false, true)
        ));
        relation = Mockito.mock(Relation.class);
        writer = new FieldWriter(pojoConfig, entity, List.of(entity), List.of(relation));
        setPojoConfig(true, true, false);

        assertThrows(IllegalArgumentException.class,
                () -> writer.writeFields(stringBuilder, properties),
                "Referenced entity not found");
    }

    @Test
    public void shouldAddFkAsEmbeddedEntity() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(128)", true, false, false),
                getProperty("gender_id", "INT", true, false, true)
        ));
        relation = Mockito.mock(Relation.class);
        writer = new FieldWriter(pojoConfig, entity, returnEntitiesDamagedFkEntityName(), List.of(relation));
        setPojoConfig(true, true, false);
        writer.writeFields(stringBuilder, properties);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(5, lines.length);
        assertEquals("\t@Id", lines[0]);
        assertEquals("\tprivate int id;", lines[1]);
        assertEquals("\tprivate String name;", lines[2]);
        assertEquals("\t@MappedCollection(idColumn = \"id\")", lines[3]);
        assertEquals("\tprivate Gender gender;", lines[4]);
    }

    @Test
    public void shouldAddFkAsEmbeddedEntityByAggregate() {
        entity = new Entity("state", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(128)", true, false, false),
                getProperty("gender_id", "INT", true, false, true)
        ));
        relation = returnUnfitRelation();
        writer = new FieldWriter(pojoConfig, entity, returnEntitiesDamagedFkEntityName(), List.of(relation));
        setPojoConfig(true, true, true);
        writer.writeFields(stringBuilder, properties);
        String[] lines = stringBuilder.toString().split("\n");

        assertEquals(5, lines.length);
        assertEquals("\t@Id", lines[0]);
        assertEquals("\tprivate int id;", lines[1]);
        assertEquals("\tprivate String name;", lines[2]);
        assertEquals("\t@Column(\"gender_id\")", lines[3]);
        assertEquals("\tprivate AggregateReference<Gender, String> gender;", lines[4]);
    }

    @Test
    public void shouldAddFkAsEmbeddedEntityByAggregate_02() {
        entity = new Entity("state", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(128)", true, false, false),
                getProperty("gender_id", "INT", true, false, true)
        ));
        relation = returnFitRelation(entity);
        writer = new FieldWriter(pojoConfig, entity, returnEntitiesDamagedFkEntityName(), List.of(relation));
        setPojoConfig(true, true, true);
        writer.writeFields(stringBuilder, properties);
        String[] lines = stringBuilder.toString().split("\n");

        System.out.println(stringBuilder);

        assertEquals(5, lines.length);
        assertEquals("\t@Id", lines[0]);
        assertEquals("\tprivate int id;", lines[1]);
        assertEquals("\tprivate String name;", lines[2]);
        assertEquals("\t@Column(\"gender_id\")", lines[3]);
        assertEquals("\tprivate AggregateReference<Gender, String> gender;", lines[4]);
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
