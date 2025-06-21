package com.github.mikekirillov;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileWriter {
    private final String fileContent;
    private final String outputFilePath;
    private final String outputFile;

    public FileWriter(String fileContent, String outputFilePath, String outputFile) {
        this.fileContent = fileContent;
        this.outputFilePath = outputFilePath;
        this.outputFile = outputFile;
    }

    public void write() {
        Path path = Path.of(outputFilePath, outputFile);
        File file = new File(path.toUri());
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(fileContent, 0, fileContent.length());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
