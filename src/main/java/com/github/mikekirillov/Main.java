package com.github.mikekirillov;

import com.github.mikekirillov.model.Entity;

import java.io.IOException;

public class Main {
    private static final String RESOURCES_PATH = "src/test/resources/";
    private static final String TXT_FILE_PATH = "data-base-model.txt";

    public static void main(String[] args) throws IOException {
        PlantUmlParser<Entity> entitiesParser = new PlantUmlEntitiesParser();
        SqlSchemaProcessor processor = new SqlSchemaProcessor();
        PlantUmlAnalyzer analyzer = new PlantUmlAnalyzer(entitiesParser, processor);

        String sqlSchema = analyzer.analyze(RESOURCES_PATH, TXT_FILE_PATH);

        System.out.println(sqlSchema);
    }
}
