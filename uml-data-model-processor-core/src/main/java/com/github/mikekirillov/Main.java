package com.github.mikekirillov;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.PojoConfig;
import com.github.mikekirillov.model.Relation;
import com.github.mikekirillov.pojo.ClassGenerator;
import com.github.mikekirillov.sql.SqlSchemaGenerator;
import com.github.mikekirillov.uml.PlantUmlAnalyzer;
import com.github.mikekirillov.uml.PlantUmlEntitiesParser;
import com.github.mikekirillov.uml.PlantUmlParser;
import com.github.mikekirillov.uml.PlantUmlRelationsParser;

import java.io.IOException;
import java.util.List;

import static com.github.mikekirillov.utils.ClassGeneratorUtils.camelize;

public class Main {
    private static final String RESOURCES_PATH_IN = "uml-data-model-processor-core/src/test/resources/";
    private static final String TXT_FILE_PATH_IN = "data-base-model.txt";
    // private static final String TXT_FILE_PATH_IN = "data-base-model.puml";
    private static final String RESOURCES_PATH_OUT = "uml-data-model-processor-core/src/main/resources/generated/";
    private static final String TXT_FILE_PATH_OUT = "schema.sql";
    private static final String POJO_GENERATOR_OUT_DIR = "uml-data-model-processor-core/src/main/resources/generated/model";
    private static final String POJO_GENERATOR_OUT_PACKAGE_NAME = "com.github.mikekirillov.icebox.pojo.model";

    public static void main(String[] args) throws IOException {
        // 1. input model analysis
        PlantUmlAnalyzer analyzer = new PlantUmlAnalyzer();
        List<String> lines = analyzer.analyze(RESOURCES_PATH_IN + TXT_FILE_PATH_IN);
        // parsing entities as objects from string lines
        PlantUmlParser<Entity> entitiesParser = new PlantUmlEntitiesParser();
        List<Entity> entities = entitiesParser.parseLinesFrom(lines);

        // 2. generating SQL Data Definition Language (DDL) model
        EntityProcessor processor = new SqlSchemaGenerator(entities);
        String sqlSchema = processor.generate();
        // creating and writing DDL script as separate document
        FileWriter ddlScriptWriter = new FileWriter(sqlSchema, RESOURCES_PATH_OUT, TXT_FILE_PATH_OUT);
        ddlScriptWriter.write();

        // 3. generating POJO - data model Java classes
        PojoConfig pojoConfig = getPojoConfig();
        // parsing entities relations as objects from string lines
        PlantUmlRelationsParser relationsParser = new PlantUmlRelationsParser(entities);
        List<Relation> relations = relationsParser.parseLinesFrom(lines);
        List<Relation> filteredRelsAsBridges = relationsParser.getBridgeEntities(relations);
        for (Entity entity : entities) {
            // generating POJO file content
            EntityProcessor classGenerator = new ClassGenerator(pojoConfig, POJO_GENERATOR_OUT_PACKAGE_NAME, entity, entities, filteredRelsAsBridges);
            String pojoFileContent = classGenerator.generate();
            // creating and writing POJO files
            FileWriter pojoWriter = new FileWriter(pojoFileContent, POJO_GENERATOR_OUT_DIR, camelize(entity.getName(), true) + ".java");
            pojoWriter.write();
        }
    }

    private static PojoConfig getPojoConfig() {
        PojoConfig pojoConfig = new PojoConfig();

        pojoConfig.setAllowSpringDataJdbcAnnotations(false);
        pojoConfig.setAllowForeignKeyAsEmbeddedEntity(false);
        pojoConfig.setAllowForeignKeyAsEmbeddedEntityByAggregate(false);
        pojoConfig.setAllowNoArgsConstructor(true);
        pojoConfig.setAllowIdArgConstructor(false);
        pojoConfig.setAllowAllArgsConstructor(true);
        pojoConfig.setAllowGetters(false);
        pojoConfig.setAllowSetters(false);
        pojoConfig.setAllowToStringMethod(false);

        return pojoConfig;
    }
}
