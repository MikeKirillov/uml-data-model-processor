import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserTest {
    private static final String RESOURCES_PATH = "src/test/resources/";
    private static final String TXT_FILE_PATH = "data-base-model.txt";
    private static final String PU_FILE_PATH = "data-base-model.pu";
    private static final String PUML_FILE_PATH = "data-base-model.puml";

    private static final String TAG_START = "@startuml";
    private static final String TAG_END = "@enduml";
    private static final String TAG_OBJECT_TYPE_ENTITY = "entity";
    private static final String TAG_OBJECT_TYPE_CLASS = "class";
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

        List<String> relationsStrings = new ArrayList<>();
        Map<String, List<String>> entitiesStringsAsMap = getEntitiesAsMap(cleanSpaces, relationsStrings);
        List<Entity> entities = processEntities(entitiesStringsAsMap);
        List<Relation> relations = processRelations(relationsStrings, entities);

        // System.out.println(relations); // TODO DELETE
        // System.out.println(entities.get(0)); // TODO DELETE
        // entities.forEach(System.out::println); // TODO DELETE
    }

    // STEP_1. Extracting every entity lines as map
    private Map<String, List<String>> getEntitiesAsMap(List<String> cleanSpaces, List<String> relationsStrings) {
        Map<String, List<String>> entitiesMap = new HashMap<>();
        Iterator<String> iterator = cleanSpaces.iterator();
        String entityNameAsLastKey = null;
        List<String> entityInnerLines = new ArrayList<>();

        while (iterator.hasNext()) {
            String line = iterator.next();

            // get entity/class name between quotes ("..."). no single quotes used at entity naming
            // if no quotes then puml throws exception
            if (line.toLowerCase().contains(TAG_OBJECT_TYPE_ENTITY) || line.toLowerCase().contains(TAG_OBJECT_TYPE_CLASS)) {
                int first = line.indexOf("\"") + 1;
                int second = line.lastIndexOf("\"");
                entityNameAsLastKey = line.substring(first, second);
            }

            if (entityNameAsLastKey != null) {
                // check for line = "}", then it was last line of an entity
                if (line.equals(TAG_CURLY_BRACKET_CLOSED)) {
                    entityInnerLines.add(line);
                    entitiesMap.put(entityNameAsLastKey, entityInnerLines);
                    entityNameAsLastKey = null;
                    entityInnerLines = new ArrayList<>();
                } else {
                    entityInnerLines.add(line);
                }
            }

            if (line.contains(Relations.ZERO_OR_ONE.getSign()) || line.contains(Relations.EXACTLY_ONE.getSign()) ||
                    line.contains(Relations.ZERO_OR_MANY.getSign()) || line.contains(Relations.ONE_OR_MANY.getSign())) {
                relationsStrings.add(line);
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
                String lowCaseLine = line.toLowerCase();

                if (lowCaseLine.contains(TAG_AS)) {
                    int first = lowCaseLine.lastIndexOf(TAG_AS) + TAG_AS.length();
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
                    PropertyBuilder propertyBuilder = new PropertyBuilder();
                    propertyBuilder.isMandatory(line.startsWith(TAG_MANDATORY));
                    propertyBuilder.isGenerated(false);
                    propertyBuilder.isForeignKey(line.contains(TAG_FK));

                    if (line.contains(TAG_GENERATED)) {
                        propertyBuilder.isMandatory(true);
                        propertyBuilder.isMandatory(true);
                    }

                    List<String> propertyList = Arrays.asList(line.split(" "));
                    var newList = propertyList.stream()
                            .filter(it -> !it.equals(TAG_MANDATORY))
                            .filter(it -> !it.equals(":"))
                            .toList();

                    propertyBuilder.name(newList.get(0));
                    propertyBuilder.type(newList.get(1));

                    // System.out.println("newList " + newList); // TODO DELETE

                    Property property = propertyBuilder.build();

                    properties.add(property);

                    // System.out.println(propertyList); // TODO DELETE
                    // System.out.println(property); // TODO DELETE
                }
            }

            entities.add(new Entity(entityName, entityAlias, properties));
        });

        assertEquals(3, entities.size()); // OK

        return entities;
    }

    // STEP_3. Process each relation line
    private List<Relation> processRelations(List<String> relationsStrings, List<Entity> entities) {
        List<Relation> relations = new ArrayList<>();

        // just one of signs (".", "-") uses between relation arrow signs. min count is 1
        for (String string : relationsStrings) {
            List<String> split = Arrays.asList(string.split(" "));
            String left = split.get(0);
            String right = split.get(split.size() - 1);
            String arrow = split.get(1);

            Optional<Entity> leftEntity = filterEntities(entities, left);
            Optional<Entity> rightEntity = filterEntities(entities, right);

            assertTrue(leftEntity.isPresent() && rightEntity.isPresent());

            if (leftEntity.isPresent() && rightEntity.isPresent()) {
                Relations leftRelationSign = Relations.valueOfSign(arrow.substring(0, 2));
                EntityRelation leftEntityRelation = new EntityRelation(leftEntity.get(), leftRelationSign);

                Relations rightRelationSign = Relations.valueOfSign(arrow.substring(arrow.length() - 2));
                EntityRelation rightEntityRelation = new EntityRelation(rightEntity.get(), rightRelationSign);

                Relation relation = new Relation(leftEntityRelation, rightEntityRelation);

                relations.add(relation);
            }
        }

        assertEquals(2, relations.size());

        return relations;
    }

    private Optional<Entity> filterEntities(List<Entity> entities, String tag) {
        return entities.stream()
                .filter(it -> {
                    boolean namesEq = it.getName().equals(tag);

                    if (Objects.isNull(it.getAlias())) {
                        return namesEq;
                    }

                    return namesEq || it.getAlias().equals(tag);
                })
                .findFirst();
    }

    static class Entity {
        private final String name;
        private final String alias;
        private final List<Property> properties;
        /*private final List<Relation> relations;*/

        public Entity(String name, String alias, List<Property> properties/*, List<Relation> relations*/) {
            Objects.requireNonNull(name, "name");

            this.name = name;
            this.alias = alias;
            this.properties = properties;
            /*this.relations = relations;*/
        }

        public String getName() {
            return name;
        }

        public String getAlias() {
            return alias;
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "name=" + name +
                    ", alias=" + alias +
                    ", properties=" + properties +
                    /*", relations=" + relations +*/
                    '}';
        }
    }

    static class PropertyBuilder {
        private String name;
        private String type;
        private boolean isMandatory;
        private boolean isGenerated;
        private boolean isForeignKey;

        public PropertyBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PropertyBuilder type(String type) {
            this.type = type;
            return this;
        }

        public PropertyBuilder isMandatory(boolean isMandatory) {
            this.isMandatory = isMandatory;
            return this;
        }

        public PropertyBuilder isGenerated(boolean isGenerated) {
            this.isGenerated = isGenerated;
            return this;
        }

        public PropertyBuilder isForeignKey(boolean isForeignKey) {
            this.isForeignKey = isForeignKey;
            return this;
        }

        public Property build() {
            return new Property(name, type, isMandatory, isGenerated, isForeignKey);
        }
    }

    static class Property {
        // private String line;
        private final String name;
        private final String type;
        private final boolean isMandatory;
        private final boolean isGenerated;
        private final boolean isPrimaryKey;
        private final boolean isForeignKey;

        public Property(/*String line,*/ String name, String type, boolean isMandatory, boolean isGenerated, boolean isForeignKey) {
            // Objects.requireNonNull(line, "line");
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
        private final EntityRelation leftEntity;
        private final EntityRelation rightEntity;

        public Relation(EntityRelation leftEntity, EntityRelation rightEntity) {
            this.leftEntity = leftEntity;
            this.rightEntity = rightEntity;
        }

        @Override
        public String toString() {
            return "Relation{" +
                    "leftEntity=" + leftEntity +
                    ", rightEntity=" + rightEntity +
                    '}';
        }
    }

    static class EntityRelation {
        private final Entity entity;
        private final Relations relation;

        public EntityRelation(Entity entity, Relations relation) {
            this.entity = entity;
            this.relation = relation;
        }

        @Override
        public String toString() {
            return "EntityRelation{" +
                    "entity=" + entity +
                    ", relation=" + relation +
                    '}';
        }
    }

    enum Relations {
        ZERO_OR_ONE("|o"),
        ZERO_OR_ONE_REVERTED("o|"),
        EXACTLY_ONE("||"),
        ZERO_OR_MANY("}o"),
        ZERO_OR_MANY_REVERTED("o{"),
        ONE_OR_MANY("}|"),
        ONE_OR_MANY_REVERTED("|{");

        private final String sign;
        private static final Map<String, Relations> RELATIONS_MAP = new HashMap<>();

        static {
            for (Relations value : values()) {
                RELATIONS_MAP.put(value.sign, value);
            }
        }

        Relations(String sign) {
            this.sign = sign;
        }

        public String getSign() {
            return sign;
        }

        public static Relations valueOfSign(String sign) {
            return RELATIONS_MAP.get(sign);
        }

        @Override
        public String toString() {
            return "Relations{" +
                    "name='" + name() + '\'' +
                    '}';
        }
    }
}
