package com.github.mikekirillov;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.Relation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PlantUmlAnalyzer {

    public void analyze(String pack, String file) throws IOException {
        Path path = Path.of(pack, file);
        List<String> lines = Files.readAllLines(path).stream()
                .map(String::trim)
                .toList();

        PlantUmlEntitiesParser entitiesParser = new PlantUmlEntitiesParser();
        List<Entity> entities = entitiesParser.parseLinesFrom(lines);

        PlantUmlRelationsParser relationsParser = new PlantUmlRelationsParser(entities);
        List<Relation> relations = relationsParser.parseLinesFrom(lines);

        SqlSchemaProcessor schemaProcessor = new SqlSchemaProcessor();
        String sqlSchema = schemaProcessor.generateSchema(entities);

        System.out.println(sqlSchema);
    }
}
