package com.github.mikekirillov.sql;

import com.github.mikekirillov.EntityProcessor;
import com.github.mikekirillov.model.Entity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static com.github.mikekirillov.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class SqlSchemaGeneratorTest {
    private EntityProcessor processor;

    @Test
    public void shouldReturnSqlSchemaString() {
        List<Entity> entities = returnEntities();
        processor = new SqlSchemaGenerator(entities);
        String sqlSchema = processor.generate();

        assertNotNull(sqlSchema);

        System.out.println(sqlSchema);

        String[] split = sqlSchema.split(System.lineSeparator());
        assertEquals("CREATE TABLE IF NOT EXISTS gender(", split[0]);
        assertEquals("id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,", split[1]);
        assertEquals("name VARCHAR(10) NOT NULL", split[2]);
        assertEquals(");", split[3]);
        assertEquals("CREATE TABLE IF NOT EXISTS state(", split[4]);
        assertEquals("id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,", split[5]);
        assertEquals("name VARCHAR(128) NOT NULL", split[6]);
        assertEquals(");", split[7]);
    }

    @Test
    public void shouldThrowExceptionByDamagedPropertyFkEntityName() {
        List<Entity> entities = returnEntitiesDamagedFkEntityName();
        processor = new SqlSchemaGenerator(entities);

        assertThrows(IllegalArgumentException.class, () -> processor.generate());
    }

    @Test
    public void shouldThrowExceptionByDamagedPropertyFkEntityId() {
        List<Entity> entities = returnEntitiesDamagedFkEntityId();
        processor = new SqlSchemaGenerator(entities);

        assertThrows(IllegalArgumentException.class, () -> processor.generate());
    }
}