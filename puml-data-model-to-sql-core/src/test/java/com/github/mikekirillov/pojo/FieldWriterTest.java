/*
package com.github.mikekirillov.pojo;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.PojoConfig;
import com.github.mikekirillov.model.Relation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.mikekirillov.utils.TestUtils.getProperty;
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
    public void should() {
        entity = new Entity("gender_es", "g", List.of(
                getProperty("id", "INT", true, true, false),
                getProperty("name", "VARCHAR(10)", true, false, false)
        ));
        relation = Mockito.mock(Relation.class);
        writer = new FieldWriter(pojoConfig, entity, List.of(entity), List.of(relation));
        setPojoConfig(false, false, false);
        writer.writeFields(stringBuilder, properties);

        System.out.println(stringBuilder);
    }

    private void setPojoConfig(boolean allowSpringDataJdbcAnnotations, boolean allowForeignKeyAsEmbeddedEntity, boolean allowForeignKeyAsEmbeddedEntityByAggregate) {
        given(pojoConfig.isAllowSpringDataJdbcAnnotations())
                .willReturn(allowSpringDataJdbcAnnotations);
        given(pojoConfig.isAllowForeignKeyAsEmbeddedEntity())
                .willReturn(allowForeignKeyAsEmbeddedEntity);
        given(pojoConfig.isAllowForeignKeyAsEmbeddedEntityByAggregate())
                .willReturn(allowForeignKeyAsEmbeddedEntityByAggregate);
    }
}*/
