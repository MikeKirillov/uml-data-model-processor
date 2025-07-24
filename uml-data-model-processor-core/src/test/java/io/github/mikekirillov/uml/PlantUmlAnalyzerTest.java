package io.github.mikekirillov.uml;

import io.github.mikekirillov.model.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static io.github.mikekirillov.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

class PlantUmlAnalyzerTest {
    private PlantUmlParser<Entity> entitiesParser;
    private PlantUmlAnalyzer analyzer;

    @BeforeEach
    void setAnalyzer() {
        entitiesParser = Mockito.mock(PlantUmlEntitiesParser.class);
        analyzer = new PlantUmlAnalyzer();
    }

    @Test
    public void shouldVerifyAllMethodsCalls() throws IOException {
        List<String> lines = returnUmlLines();
        List<Entity> entities = returnEntities();
        MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class);

        when(Files.readAllLines(any())).thenReturn(lines);
        given(entitiesParser.parseLinesFrom(any())).willReturn(entities);

        List<Entity> result = entitiesParser.parseLinesFrom(lines);

        assertEquals(2, result.size());

        filesMockedStatic.close();
    }
}