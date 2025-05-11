package com.github.mikekirillov;

import com.github.mikekirillov.model.Entity;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.util.List;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class PlantUmlToSqlSchemeMojo extends AbstractMojo {

    @Parameter(property = "generate.inputFilePath", required = true)
    private String inputFilePath;

    @Parameter(property = "generate.outputFilePath", required = true)
    private String outputFilePath;

    @Parameter(property = "generate.outputFileName", defaultValue = "schema")
    private String outputFileName;

    @Parameter(property = "generate.outputFileExtension", defaultValue = "sql")
    private String outputFileExtension;

    public String getInputFilePath() {
        return inputFilePath;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public String getOutputFileExtension() {
        return outputFileExtension;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        PlantUmlParser<Entity> entitiesParser = new PlantUmlEntitiesParser();
        PlantUmlAnalyzer analyzer = new PlantUmlAnalyzer(entitiesParser);

        try {
            List<Entity> entities = analyzer.analyze(inputFilePath);
            SqlSchemaProcessor processor = new SqlSchemaProcessor(entities);
            String sqlSchema = processor.generateSchema();

            getLog().info("Generated schema:\n" + sqlSchema);

            // TODO CONFIG SKIP of sql ddl-script-gen PR pojo-gen
            SqlSchemaFileWriter writer = new SqlSchemaFileWriter(sqlSchema, outputFilePath, outputFileName + "." + outputFileExtension);
            writer.write();

            getLog().info(getCompleteMsg());
        } catch (IOException e) {
            getLog().error(FAILED_MSG, e);
            throw new MojoExecutionException(FAILED_MSG, e);
        }
    }

    private String getCompleteMsg() {
        return String.format("Generating %s.%s is complete", outputFileName, outputFileExtension);
    }

    private static final String FAILED_MSG = "Error of analyzing input schema";
}
