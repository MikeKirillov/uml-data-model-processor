package com.github.mikekirillov.uml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PlantUmlAnalyzer {

    public List<String> analyze(String filePath) throws IOException {
        Path path = Path.of(filePath);
        return Files.readAllLines(path).stream()
                .map(String::trim)
                .toList();
    }
}
