package io.github.mikekirillov.uml;

import io.github.mikekirillov.model.Entity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.mikekirillov.utils.TestUtils.returnUmlLines;
import static org.junit.jupiter.api.Assertions.*;

class PlantUmlEntitiesParserTest {
    private PlantUmlParser<Entity> parser = new PlantUmlEntitiesParser();

    @Test
    public void shouldParseLines() {
        List<String> lines = returnUmlLines();
        List<Entity> entities = parser.parseLinesFrom(lines);

        assertEquals(2, entities.size());

        assertEquals("gender", entities.get(0).getName());
        assertEquals("g", entities.get(0).getAlias());
        assertEquals(2, entities.get(0).getProperties().size());

        assertEquals("id", entities.get(0).getProperties().get(0).getName());
        assertEquals("INT", entities.get(0).getProperties().get(0).getType());
        assertTrue(entities.get(0).getProperties().get(0).isPrimaryKey());
        assertTrue(entities.get(0).getProperties().get(0).isGenerated());
        assertTrue(entities.get(0).getProperties().get(0).isMandatory());
        assertFalse(entities.get(0).getProperties().get(0).isForeignKey());

        assertEquals("name", entities.get(0).getProperties().get(1).getName());
        assertEquals("VARCHAR(10)", entities.get(0).getProperties().get(1).getType());
        assertFalse(entities.get(0).getProperties().get(1).isPrimaryKey());
        assertFalse(entities.get(0).getProperties().get(1).isGenerated());
        assertTrue(entities.get(0).getProperties().get(1).isMandatory());
        assertFalse(entities.get(0).getProperties().get(1).isForeignKey());

        assertEquals("state", entities.get(1).getName());
        assertEquals("st", entities.get(1).getAlias());
        assertEquals(2, entities.get(1).getProperties().size());
    }
}