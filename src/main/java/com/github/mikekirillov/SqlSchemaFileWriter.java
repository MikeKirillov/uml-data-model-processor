package com.github.mikekirillov;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SqlSchemaFileWriter {
    private final String sqlSchema;
    private final String filePath;
    private final String fileName;

    public SqlSchemaFileWriter(String sqlSchema, String filePath, String fileName) {
        this.sqlSchema = sqlSchema;
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public void write() {
        Path path = Path.of(filePath, fileName);
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
