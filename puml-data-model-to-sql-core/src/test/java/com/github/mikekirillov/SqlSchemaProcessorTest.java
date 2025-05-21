package com.github.mikekirillov;

import com.github.mikekirillov.model.Entity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static com.github.mikekirillov.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class SqlSchemaProcessorTest {
    private SqlSchemaProcessor processor;

    @Test
    public void shouldReturnSqlSchemaString() {
        List<Entity> entities = returnEntities();
        processor = new SqlSchemaProcessor(entities);
        String sqlSchema = processor.process();

        assertNotNull(sqlSchema);
    }

    @Test
    public void shouldThrowExceptionByDamagedPropertyFkEntityName() {
        List<Entity> entities = returnEntitiesDamagedFkEntityName();
        processor = new SqlSchemaProcessor(entities);

        assertThrows(NoSuchElementException.class, () -> processor.process());
    }

    @Test
    public void shouldThrowExceptionByDamagedPropertyFkEntityId() {
        List<Entity> entities = returnEntitiesDamagedFkEntityId();
        processor = new SqlSchemaProcessor(entities);

        assertThrows(NoSuchElementException.class, () -> processor.process());
    }
}