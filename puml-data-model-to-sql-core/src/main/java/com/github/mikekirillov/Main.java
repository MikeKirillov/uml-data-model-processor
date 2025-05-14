package com.github.mikekirillov;

import com.github.mikekirillov.model.Entity;

import java.io.IOException;
import java.util.List;

public class Main {
    private static final String RESOURCES_PATH_IN = "puml-data-model-to-sql-core/src/test/resources/";
    private static final String TXT_FILE_PATH_IN = "data-base-model.txt";
    private static final String RESOURCES_PATH_OUT = "puml-data-model-to-sql-core/src/main/resources/generated/";
    private static final String TXT_FILE_PATH_OUT = "schema.sql";
    private static final String POJO_GENERATOR_OUT_DIR = "puml-data-model-to-sql-core/src/main/resources/generated/model";

    public static void main(String[] args) throws IOException {
        // 1. input model analysis
        PlantUmlParser<Entity> entitiesParser = new PlantUmlEntitiesParser();
        PlantUmlAnalyzer analyzer = new PlantUmlAnalyzer(entitiesParser);
        List<Entity> entities = analyzer.analyze(RESOURCES_PATH_IN + TXT_FILE_PATH_IN);

        // 2. generating SQL Data Definition Language (DDL) model
        SqlSchemaProcessor processor = new SqlSchemaProcessor(entities);
        String sqlSchema = processor.generateSchema();

        System.out.println(sqlSchema);

        // 3. creating and writing DDL script as separate document
        SqlSchemaFileWriter writer = new SqlSchemaFileWriter(sqlSchema, RESOURCES_PATH_OUT, TXT_FILE_PATH_OUT);
        writer.write();

        // 4. generating POJO - data model Java classes
        ModelPojoWriter modelPojoWriter = new JdbcModelPojoWriter(
                POJO_GENERATOR_OUT_DIR,
                entities,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                true
        );
        modelPojoWriter.write();
    }
}
