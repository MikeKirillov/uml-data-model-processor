package com.github.mikekirillov;

import com.github.mikekirillov.model.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static com.github.mikekirillov.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class SqlSchemaProcessorTest {
    private SqlSchemaProcessor processor;

    @BeforeEach
    void init() {
        processor = new SqlSchemaProcessor();
    }

    @Test
    public void shouldReturnSqlSchemaString() {
        List<Entity> entities = returnEntities();
        String sqlSchema = processor.generateSchema(entities);

        assertNotNull(sqlSchema);
    }

    @Test
    public void shouldThrowExceptionByDamagedPropertyFkEntityName() {
        List<Entity> entities = returnEntitiesDamagedFkEntityName();

        assertThrows(NoSuchElementException.class, () -> processor.generateSchema(entities));
    }

    @Test
    public void shouldThrowExceptionByDamagedPropertyFkEntityId() {
        List<Entity> entities = returnEntitiesDamagedFkEntityId();

        assertThrows(NoSuchElementException.class, () -> processor.generateSchema(entities));
    }
}