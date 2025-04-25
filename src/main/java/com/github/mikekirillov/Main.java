package com.github.mikekirillov;

import com.github.mikekirillov.model.Entity;

import java.io.IOException;
import java.util.List;

public class Main {
    private static final String RESOURCES_PATH_IN = "src/test/resources/";
    private static final String TXT_FILE_PATH_IN = "data-base-model.txt";
    private static final String RESOURCES_PATH_OUT = "src/main/resources/generated/";
    private static final String TXT_FILE_PATH_OUT = "schema.sql";

    public static void main(String[] args) throws IOException {
        PlantUmlParser<Entity> entitiesParser = new PlantUmlEntitiesParser();
        SqlSchemaProcessor processor = new SqlSchemaProcessor();
        PlantUmlAnalyzer analyzer = new PlantUmlAnalyzer(entitiesParser);

        List<Entity> entities = analyzer.analyze(RESOURCES_PATH_IN, TXT_FILE_PATH_IN);
        String sqlSchema = processor.generateSchema(entities);

        System.out.println(sqlSchema);

        SqlSchemaFileWriter writer = new SqlSchemaFileWriter(sqlSchema, RESOURCES_PATH_OUT, TXT_FILE_PATH_OUT);
        writer.write();
    }
}
