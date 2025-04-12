package com.github.mikekirillov;

import com.github.mikekirillov.model.Entity;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.github.mikekirillov.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlantUmlAnalyzerTest {
    private PlantUmlAnalyzer analyzer;

    @Test
    public void shouldVerifyAllMethodsCalls() throws IOException {
        PlantUmlParser<Entity> entitiesParser = Mockito.mock(PlantUmlEntitiesParser.class);
        SqlSchemaProcessor processor = Mockito.mock(SqlSchemaProcessor.class);
        analyzer = new PlantUmlAnalyzer(entitiesParser, processor);

        List<String> lines = returnUmlLines();
        List<Entity> entities = returnEntities();

        MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class);

        when(Files.readAllLines(any())).thenReturn(lines);
        given(entitiesParser.parseLinesFrom(any())).willReturn(entities);

        analyzer.analyze(RESOURCES_PATH, TXT_FILE_PATH);

        verify(entitiesParser).parseLinesFrom(lines);
        verify(processor).generateSchema(entities);

        filesMockedStatic.close();
    }

    @Test
    public void shouldReturnSqlSchema() throws IOException {
        analyzer = new PlantUmlAnalyzer(
                new PlantUmlEntitiesParser(),
                new SqlSchemaProcessor()
        );

        String sqlSchema = analyzer.analyze(RESOURCES_PATH, TXT_FILE_PATH);

        assertNotNull(sqlSchema);
    }
}