package com.github.mikekirillov;

import com.github.mikekirillov.enums.UmlRelationType;
import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.Relation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.mikekirillov.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class PlantUmlRelationsParserTest {
    private PlantUmlParser<Relation> parser;

    @Test
    public void shouldReturnEmptyRelationsList() {
        List<Entity> entities = returnEntities();
        parser = new PlantUmlRelationsParser(entities);

        List<String> lines = returnUmlLines();
        List<Relation> relations = parser.parseLinesFrom(lines);

        assertTrue(relations.isEmpty());
    }

    @Test
    public void shouldReturnRelations() {
        List<Entity> entities = returnEntitiesWithFk();
        parser = new PlantUmlRelationsParser(entities);

        List<String> lines = returnUmlLinesWithFk();
        List<Relation> relations = parser.parseLinesFrom(lines);

        assertEquals(1, relations.size());

        assertEquals("state", relations.get(0).getLeftEntity().getEntity().getName());
        assertEquals(UmlRelationType.ONE_OR_MANY, relations.get(0).getLeftEntity().getRelationType());

        assertEquals("gender", relations.get(0).getRightEntity().getEntity().getName());
        assertEquals(UmlRelationType.EXACTLY_ONE, relations.get(0).getRightEntity().getRelationType());
    }

    @Test
    public void shouldFilterRelationsWithBridgesEntities() {
        // TODO TEST
    }
}