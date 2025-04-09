package com.github.mikekirillov;

import java.io.IOException;

public class Main {
    private static final String RESOURCES_PATH = "src/test/resources/";
    private static final String TXT_FILE_PATH = "data-base-model.txt";

    public static void main(String[] args) throws IOException {
        PlantUmlAnalyzer analyzer = new PlantUmlAnalyzer();
        analyzer.analyze(RESOURCES_PATH, TXT_FILE_PATH);
    }
}
