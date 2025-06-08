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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.util.List;

import static com.github.mikekirillov.utils.ClassGeneratorUtils.camelize;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class PlantUmlToSqlSchemeMojo extends AbstractMojo {

    @Parameter(property = "generate.inputFilePath", required = true)
    private String inputFilePath;

    @Parameter(property = "generate.outputDdlScriptFilePath")
    private String outputDdlScriptFilePath;

    @Parameter(property = "generate.outputDdlScriptFileName", defaultValue = "schema")
    private String outputDdlScriptFileName;

    @Parameter(property = "generate.outputDdlScriptFileExtension", defaultValue = "sql")
    private String outputDdlScriptFileExtension;

    @Parameter(property = "generate.allowSpringDataJdbcAnnotations", defaultValue = "false")
    private boolean allowSpringDataJdbcAnnotations;

    @Parameter(property = "generate.allowForeignKeyAsEmbeddedEntity", defaultValue = "false")
    private boolean allowForeignKeyAsEmbeddedEntity;

    @Parameter(property = "generate.allowForeignKeyAsEmbeddedEntityByAggregate", defaultValue = "false")
    private boolean allowForeignKeyAsEmbeddedEntityByAggregate;

    @Parameter(property = "generate.allowNoArgsConstructor", defaultValue = "false")
    private boolean allowNoArgsConstructor;

    @Parameter(property = "generate.allowIdArgConstructor", defaultValue = "false")
    private boolean allowIdArgConstructor;

    @Parameter(property = "generate.allowAllArgsConstructor", defaultValue = "false")
    private boolean allowAllArgsConstructor;

    @Parameter(property = "generate.allowGetters", defaultValue = "false")
    private boolean allowGetters;

    @Parameter(property = "generate.allowSetters", defaultValue = "false")
    private boolean allowSetters;

    @Parameter(property = "generate.allowToStringMethod", defaultValue = "false")
    private boolean allowToStringMethod;

    @Parameter(property = "generate.outputPojoFilePath")
    private String outputPojoFilePath;

    @Parameter(property = "generate.outputPojoPackageName")
    private String outputPojoPackageName;

    @Parameter(property = "generate.generateDdlScript", defaultValue = "false")
    private boolean generateDdlScript;

    @Parameter(property = "generate.generatePojo", defaultValue = "false")
    private boolean generatePojo;

    public String getInputFilePath() {
        return inputFilePath;
    }

    public String getOutputDdlScriptFilePath() {
        return outputDdlScriptFilePath;
    }

    public String getOutputDdlScriptFileName() {
        return outputDdlScriptFileName;
    }

    public String getOutputDdlScriptFileExtension() {
        return outputDdlScriptFileExtension;
    }

    public boolean isAllowSpringDataJdbcAnnotations() {
        return allowSpringDataJdbcAnnotations;
    }

    public boolean isAllowForeignKeyAsEmbeddedEntity() {
        return allowForeignKeyAsEmbeddedEntity;
    }

    public boolean isAllowForeignKeyAsEmbeddedEntityByAggregate() {
        return allowForeignKeyAsEmbeddedEntityByAggregate;
    }

    public boolean isAllowNoArgsConstructor() {
        return allowNoArgsConstructor;
    }

    public boolean isAllowIdArgConstructor() {
        return allowIdArgConstructor;
    }

    public boolean isAllowAllArgsConstructor() {
        return allowAllArgsConstructor;
    }

    public boolean isAllowGetters() {
        return allowGetters;
    }

    public boolean isAllowSetters() {
        return allowSetters;
    }

    public boolean isAllowToStringMethod() {
        return allowToStringMethod;
    }

    public String getOutputPojoFilePath() {
        return outputPojoFilePath;
    }

    public String getOutputPojoPackageName() {
        return outputPojoPackageName;
    }

    public boolean isGenerateDdlScript() {
        return generateDdlScript;
    }

    public boolean isGeneratePojo() {
        return generatePojo;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // 1. input model analysis
            PlantUmlAnalyzer analyzer = new PlantUmlAnalyzer();
            List<String> lines = analyzer.analyze(inputFilePath);
            // parsing entities as objects from string lines
            PlantUmlParser<Entity> entitiesParser = new PlantUmlEntitiesParser();
            List<Entity> entities = entitiesParser.parseLinesFrom(lines);

            if (isGenerateDdlScript()) {
                generateSql(entities);
            }
            if (isGeneratePojo()) {
                generatePojo(entities, lines);
            }
        } catch (IOException e) {
            getLog().error(FAILED_MSG, e);
            throw new MojoExecutionException(FAILED_MSG, e);
        }
    }

    private void generateSql(List<Entity> entities) {
        getLog().info("Generating DB schema");

        EntityProcessor processor = new SqlSchemaGenerator(entities);
        String sqlSchema = processor.generate();

        getLog().info("Generated schema:\n" + sqlSchema);

        // creating and writing DDL script as separate document
        FileWriter ddlScriptWriter = new FileWriter(sqlSchema, outputDdlScriptFilePath, outputDdlScriptFileName + "." + outputDdlScriptFileExtension);
        ddlScriptWriter.write();

        getLog().info(getCompleteMsg(outputDdlScriptFileName, outputDdlScriptFileExtension));
    }

    private void generatePojo(List<Entity> entities, List<String> lines) {
        getLog().info("Generating POJO's");

        PojoConfig pojoConfig = getPojoConfig();
        PlantUmlRelationsParser relationsParser = new PlantUmlRelationsParser(entities);
        List<Relation> relations = relationsParser.parseLinesFrom(lines);
        List<Relation> filteredRelsAsBridges = relationsParser.getBridgeEntities(relations);
        for (Entity entity : entities) {
            // generating POJO file content
            getLog().info("Generating POJO for " + entity.getName());

            EntityProcessor classGenerator = new ClassGenerator(pojoConfig, outputPojoPackageName, entity, entities, filteredRelsAsBridges);
            String pojoFileContent = classGenerator.generate();
            // creating and writing POJO files
            String outputFileName = camelize(entity.getName(), true);
            FileWriter pojoWriter = new FileWriter(pojoFileContent, outputPojoFilePath, outputFileName + ".java");
            pojoWriter.write();

            getLog().info(getCompleteMsg(outputFileName, "java"));
        }
    }

    private PojoConfig getPojoConfig() {
        PojoConfig pojoConfig = new PojoConfig();
        pojoConfig.setAllowSpringDataJdbcAnnotations(isAllowSpringDataJdbcAnnotations());
        pojoConfig.setAllowForeignKeyAsEmbeddedEntity(isAllowForeignKeyAsEmbeddedEntity());
        pojoConfig.setAllowForeignKeyAsEmbeddedEntityByAggregate(isAllowForeignKeyAsEmbeddedEntityByAggregate());
        pojoConfig.setAllowNoArgsConstructor(isAllowNoArgsConstructor());
        pojoConfig.setAllowIdArgConstructor(isAllowIdArgConstructor());
        pojoConfig.setAllowAllArgsConstructor(isAllowAllArgsConstructor());
        pojoConfig.setAllowGetters(isAllowGetters());
        pojoConfig.setAllowSetters(isAllowSetters());
        pojoConfig.setAllowToStringMethod(isAllowToStringMethod());
        return pojoConfig;
    }

    private String getCompleteMsg(String name, String extension) {
        return String.format("Generating %s.%s is complete", name, extension);
    }

    private static final String FAILED_MSG = "Error of analyzing input schema";
}
