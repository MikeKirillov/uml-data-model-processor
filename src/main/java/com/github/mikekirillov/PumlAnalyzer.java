package com.github.mikekirillov;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PumlAnalyzer {
    private static final String RESOURCES_PATH = "src/test/resources/";
    private static final String TXT_FILE_PATH = "data-base-model.txt";

    public void analyze() throws IOException {
        Path path = Path.of(RESOURCES_PATH, TXT_FILE_PATH);
        List<String> lines = Files.readAllLines(path).stream()
                .map(String::trim)
                .toList();

        // prepare entities as map
        // parse entities and properties
        // parse relations
    }
}
