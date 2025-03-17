import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserTest {
    private static final String RESOURCES_PATH = "src/test/resources/";
    private static final String TXT_FILE_PATH = "data-base-model.txt";
    private static final String PU_FILE_PATH = "data-base-model.pu";
    private static final String PUML_FILE_PATH = "data-base-model.puml";

    private static final String TAG_START = "@startuml";
    private static final String TAG_END = "@enduml";
    private static final String TAG_TYPE_ENTITY = "entity";
    private static final String TAG_TYPE_CLASS = "class";
    private static final String TAG_AS = " as ";

    @Test
    public void shouldReadLines() throws IOException {
        Path path = Path.of(RESOURCES_PATH, TXT_FILE_PATH);

        List<String> lines = Files.readAllLines(path);
        List<String> cleanSpaces = lines.stream()
                .map(String::trim)
                .collect(Collectors.toList());

        assertEquals(TAG_START, cleanSpaces.get(0));
        assertEquals(TAG_END, cleanSpaces.get(cleanSpaces.size() - 1));

        // 1. Extracting every entity lines as map
        Map<String, List<String>> entitiesMap = new HashMap<>();
        Iterator<String> iterator = cleanSpaces.iterator();
        String lastKey = null;
        List<String> entityInnerLines = new ArrayList<>();

        while (iterator.hasNext()) {
            String line = iterator.next();

            if (line.contains(TAG_TYPE_ENTITY) || line.contains(TAG_TYPE_CLASS)) {
                int first = line.indexOf("\"") + 1;
                int second = line.lastIndexOf("\"");
                lastKey = line.substring(first, second);
            }

            if (lastKey != null) {
                if (line.equals("}")) {
                    entityInnerLines.add(line);
                    entitiesMap.put(lastKey, entityInnerLines);
                    lastKey = null;
                    entityInnerLines = new ArrayList<>();
                } else {
                    entityInnerLines.add(line);
                }
            }
        }

        entitiesMap.forEach((k, v) -> System.out.println(k + ": " + v));

        // 2. Process each entity value from map
        List<Entity> entities = new ArrayList<>();
        /*
        for (String line : cleanSpaces) {
            String entityName = null;
            String entityAlias = null;

            // get entity name between quotes ("..."). no single quotes used at entity naming
            if (line.contains(TAG_TYPE_ENTITY) || line.contains(TAG_TYPE_CLASS)) {
                int first = line.indexOf("\"") + 1;
                int second = line.lastIndexOf("\"");
                entityName = line.substring(first, second);
            }

            // check if entity has alias
            if (line.contains(TAG_AS)) {
                int first = line.lastIndexOf(TAG_AS) + TAG_AS.length();
                String alias = line.substring(first);
                *//*TODO:
         * check is missing: alias and its mentions at relations are not equals
         * (see PUML situations with creating empty entity)
         *//*
                entityAlias = alias.substring(0, alias.indexOf("{")).trim();
            }

            if (Objects.nonNull(entityName)) {
                entities.add(new Entity(entityName, entityAlias)); // TODO: OK
            }
        }

        assertEquals(3, entities.size());


        //=========////=========////=========////=========////=========//
        entities.forEach(System.out::println);
        cleanSpaces.forEach(System.out::println); // TODO DELETE
        */
    }

    static class Entity {
        String name;
        String alias;

        Entity(String name, String alias) {
            Objects.requireNonNull(name, "name");

            this.name = name;
            this.alias = alias;
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "name=" + name +
                    ", alias=" + alias +
                    '}';
        }
    }
}
