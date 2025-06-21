package com.github.mikekirillov;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.github.mikekirillov.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class FileWriterTest {
    private FileWriter writer;
    private File file;
    private String fileContent;

    @BeforeEach
    void init() {
        fileContent = returnSqlSchema();
        writer = new FileWriter(fileContent, RESOURCES_PATH_OUT, TXT_FILE_PATH_OUT);
        Path path = Path.of(RESOURCES_PATH_OUT, TXT_FILE_PATH_OUT);
        file = new File(path.toUri());
    }

    @AfterEach
    void out() {
        file.delete(); // clean generated file
        file.getParentFile().delete(); // clean generated directory
    }

    @Test
    public void shouldCreateFile() throws IOException {
        writer.write();

        assertTrue(file.getParentFile().exists());
        assertTrue(file.exists());
        assertTrue(file.isFile());
        assertEquals(TXT_FILE_PATH_OUT, file.getName());

        List<String> lines = Files.readAllLines(file.toPath()).stream().toList();
        String[] splitSchema = fileContent.split("\n");

        assertTrue(() -> lines.get(0).contains(splitSchema[0])
                && lines.get(1).contains(splitSchema[1])
                && lines.get(2).contains(splitSchema[2])
                && lines.get(3).contains(splitSchema[3]));
    }
}