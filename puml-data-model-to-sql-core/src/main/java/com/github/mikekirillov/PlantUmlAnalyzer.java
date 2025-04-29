package com.github.mikekirillov;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.Relation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PlantUmlAnalyzer {
    private final PlantUmlParser<Entity> entitiesParser;

    public PlantUmlAnalyzer(PlantUmlParser<Entity> entitiesParser) {
        this.entitiesParser = entitiesParser;
    }

    public List<Entity> analyze(String filePath) throws IOException {
        Path path = Path.of(filePath);
        List<String> lines = Files.readAllLines(path).stream()
                .map(String::trim)
                .toList();

        List<Entity> entities = entitiesParser.parseLinesFrom(lines);

        // not required for MVP
        /* TODO think about split parser for entities only (just for sql script)
             and parser for entities and relations (for sql script and Java entity-classes generation)
        */
        /*PlantUmlParser<Relation> relationsParser = new PlantUmlRelationsParser(entities);
        List<Relation> relations = relationsParser.parseLinesFrom(lines);*/

        return entities;
    }
}
