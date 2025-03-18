import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    private static final String TAG_CURLY_BRACKET_OPENED = "{";
    private static final String TAG_CURLY_BRACKET_CLOSED = "}";
    private static final String TAG_GENERATED = "generated";
    private static final String TAG_FK = "FK";
    private static final String TAG_MANDATORY = "*";

    @Test
    public void shouldReadLines() throws IOException {
        Path path = Path.of(RESOURCES_PATH, TXT_FILE_PATH);

        // TODO! think about importing basic validation by PUML tools

        List<String> lines = Files.readAllLines(path);
        List<String> cleanSpaces = lines.stream()
                .map(String::trim)
                .collect(Collectors.toList());

        assertEquals(TAG_START, cleanSpaces.get(0));
        assertEquals(TAG_END, cleanSpaces.get(cleanSpaces.size() - 1));

        // cleanSpaces.forEach(System.out::println); // TODO DELETE

        var entitiesAsMap = getEntitiesAsMap(cleanSpaces);
        var entities = processEntities(entitiesAsMap);
        // TODO! don't forget about relation arrows


        // System.out.println(entities.get(0)); // TODO DELETE
    }

    // STEP_1. Extracting every entity lines as map
    private Map<String, List<String>> getEntitiesAsMap(List<String> cleanSpaces) {
        Map<String, List<String>> entitiesMap = new HashMap<>();
        Iterator<String> iterator = cleanSpaces.iterator();
        String entityNameAsLastKey = null;
        List<String> entityInnerLines = new ArrayList<>();

        while (iterator.hasNext()) {
            String line = iterator.next();

            // get entity name between quotes ("..."). no single quotes used at entity naming
            if (line.contains(TAG_TYPE_ENTITY) || line.contains(TAG_TYPE_CLASS)) {
                int first = line.indexOf("\"") + 1;
                int second = line.lastIndexOf("\"");
                entityNameAsLastKey = line.substring(first, second);
            }

            if (entityNameAsLastKey != null) {
                if (line.equals(TAG_CURLY_BRACKET_CLOSED)) {
                    entityInnerLines.add(line);
                    entitiesMap.put(entityNameAsLastKey, entityInnerLines);
                    entityNameAsLastKey = null;
                    entityInnerLines = new ArrayList<>();
                } else {
                    entityInnerLines.add(line);
                }
            }
        }

        assertEquals(3, entitiesMap.size()); // OK
        // entitiesMap.forEach((k, v) -> System.out.println(k + ": " + v)); // TODO DELETE

        return entitiesMap;
    }

    // STEP_2. Process each entity value from map
    private List<Entity> processEntities(Map<String, List<String>> entitiesMap) {
        List<Entity> entities = new ArrayList<>();

        entitiesMap.forEach((entityName, entityLines) -> { // TODO inline double loops at single
            String entityAlias = null;
            List<Property> properties = new ArrayList<>();

            for (String line : entityLines) {
                // check if entity has alias
                if (line.contains(TAG_AS)) {
                    int first = line.lastIndexOf(TAG_AS) + TAG_AS.length();
                    String alias = line.substring(first);
                /*TODO! check is missing:
                   alias and its mentions at relations are not equals (see PUML situations with creating empty entity) */
                    if (alias.contains(TAG_CURLY_BRACKET_OPENED)) {
                        entityAlias = alias.substring(0, alias.indexOf(TAG_CURLY_BRACKET_OPENED)).trim();
                    } else {
                        entityAlias = alias;
                    }
                }

                // check for other lines except entity name and curly brackets
                // and parse properties
                if (!line.contains(TAG_AS) && !line.contains(TAG_CURLY_BRACKET_OPENED) && !line.contains(TAG_CURLY_BRACKET_CLOSED) && !StringUtils.containsOnly(line, "-")) {
                    boolean isMandatory = line.startsWith(TAG_MANDATORY);
                    boolean isGenerated = false;

                    if (line.contains(TAG_GENERATED)) {
                        isMandatory = true;
                        isGenerated = true;
                    }

                    boolean isForeignKey = line.contains(TAG_FK);

                    List<String> propertyList = Arrays.asList(line.split(" "));
                    var newList = propertyList.stream()
                            // .map(it -> it.replaceAll("[^\\sa-zA-Z0-9]", ""))
                            .map(it -> it.replaceAll("[^a-zA-Z ]", ""))
                            .filter(it -> !it.isEmpty())
                            .toList();

                    System.out.println("newList " + newList);

                    Property property = new Property(line, "null", "null", isMandatory, isGenerated, isForeignKey);

                    properties.add(property);

                    System.out.println(propertyList); // TODO DELETE
                    System.out.println(property); // TODO DELETE
                }
            }

            entities.add(new Entity(entityName, entityAlias, properties));
        });

        assertEquals(3, entities.size()); // OK

        return entities;
    }

    static class Entity {
        String name;
        String alias;
        List<Property> properties;
        List<Relation> relations;

        Entity(String name, String alias, List<Property> properties) {
            Objects.requireNonNull(name, "name");

            this.name = name;
            this.alias = alias;
            this.properties = properties;
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "name=" + name +
                    ", alias=" + alias +
                    ", properties=" + properties +
                    ", relations=" + relations +
                    '}';
        }
    }

    static class Property {
        // String line;
        String name;
        String type;
        boolean isMandatory;
        boolean isGenerated;
        boolean isPrimaryKey;
        boolean isForeignKey;

        Property(String line, String name, String type, boolean isMandatory, boolean isGenerated, boolean isForeignKey) {
            Objects.requireNonNull(line, "line");
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(type, "type");

            // this.line = line;
            this.name = name;
            this.type = type;
            this.isMandatory = isMandatory;
            this.isGenerated = isGenerated;
            this.isPrimaryKey = isMandatory && isGenerated;
            this.isForeignKey = isForeignKey;
        }

        // TODO! try fluent api style

        @Override
        public String toString() {
            return "Property{" +
                    // "line=" + line +
                    "name=" + name +
                    // ", name=" + name +
                    ", type=" + type +
                    ", isMandatory=" + isMandatory +
                    ", isGenerated=" + isGenerated +
                    ", isPrimaryKey=" + isPrimaryKey +
                    ", isForeignKey=" + isForeignKey +
                    '}';
        }
    }

    static class Relation {
        // TODO!
    }
}
