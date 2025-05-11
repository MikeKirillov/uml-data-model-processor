package com.github.mikekirillov;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SqlSchemaFileWriter {
    private final String sqlSchema;
    private final String outputFilePath;
    private final String outputFile;

    public SqlSchemaFileWriter(String sqlSchema, String outputFilePath, String outputFile) {
        this.sqlSchema = sqlSchema;
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
            writer.write(sqlSchema, 0, sqlSchema.length());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
