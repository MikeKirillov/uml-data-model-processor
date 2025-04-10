package com.github.mikekirillov;

import com.github.mikekirillov.constants.PlantUmlSchemaTag;
import com.github.mikekirillov.enums.UmlRelationType;
import com.github.mikekirillov.model.Relation;
import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.EntityRelation;
import com.github.mikekirillov.model.Property;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserFirstTDDTest {
    private static final String RESOURCES_PATH = "src/test/resources/";
    private static final String TXT_FILE_PATH = "data-base-model.txt";
    private static final String PU_FILE_PATH = "data-base-model.pu";
    private static final String PUML_FILE_PATH = "data-base-model.puml";

    @Test
    public void shouldReadLines() throws IOException {
        Path path = Path.of(RESOURCES_PATH, TXT_FILE_PATH);

        // TODO! think about importing basic validation by PUML tools

        List<String> lines = Files.readAllLines(path);
        List<String> cleanSpaces = lines.stream()
                .map(String::trim)
                .collect(Collectors.toList());

        assertEquals(PlantUmlSchemaTag.START, cleanSpaces.get(0));
        assertEquals(PlantUmlSchemaTag.END, cleanSpaces.get(cleanSpaces.size() - 1));

        List<String> relationsStrings = new ArrayList<>();
        Map<String, List<String>> entitiesStringsAsMap = getEntitiesAsMap(cleanSpaces, relationsStrings);
        List<Entity> entities = processEntities(entitiesStringsAsMap);
        List<Relation> relations = processRelations(relationsStrings, entities);
    }

    // STEP_1. Extracting every entity lines as map
    private Map<String, List<String>> getEntitiesAsMap(List<String> cleanSpaces, List<String> relationsStrings) {
        Map<String, List<String>> entitiesMap = new HashMap<>();
        Iterator<String> iterator = cleanSpaces.iterator();
        String entityNameAsLastKey = null;
        List<String> entityInnerLines = new ArrayList<>();

        while (iterator.hasNext()) {
            String line = iterator.next();

            // get entity/class name
            if (line.toLowerCase().contains(PlantUmlSchemaTag.OBJECT_TYPE_ENTITY) || line.toLowerCase().contains(PlantUmlSchemaTag.OBJECT_TYPE_CLASS)) {
                // between quotes ("...") when alias is given. if no quotes then PUML throws exception
                if (line.toLowerCase().contains(PlantUmlSchemaTag.AS_BETWEEN_SPACES)) {
                    int first = line.indexOf("\"") + 1;
                    int second = line.lastIndexOf("\"");
                    entityNameAsLastKey = line.substring(first, second);
                } else {
                    // second word after entity/class declaring
                    List<String> splitLine = Arrays.stream(line.split(" "))
                            .filter(it -> !it.isBlank())
                            .toList();
                    entityNameAsLastKey = splitLine.get(1);
                }
            }

            if (entityNameAsLastKey != null) {
                // check for line = "}", then it was last line of an entity
                if (line.equals(PlantUmlSchemaTag.CURLY_BRACKET_CLOSED)) {
                    entityInnerLines.add(line);
                    entitiesMap.put(entityNameAsLastKey, entityInnerLines);
                    entityNameAsLastKey = null;
                    entityInnerLines = new ArrayList<>();
                } else {
                    entityInnerLines.add(line);
                }
            }

            if (line.contains(UmlRelationType.ZERO_OR_ONE.getType()) || line.contains(UmlRelationType.EXACTLY_ONE.getType()) ||
                    line.contains(UmlRelationType.ZERO_OR_MANY.getType()) || line.contains(UmlRelationType.ONE_OR_MANY.getType())) {
                relationsStrings.add(line);
            }
        }

        assertEquals(3, entitiesMap.size());

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

                if (lowCaseLine.contains(PlantUmlSchemaTag.AS_BETWEEN_SPACES)) {
                    int first = lowCaseLine.lastIndexOf(PlantUmlSchemaTag.AS_BETWEEN_SPACES) + PlantUmlSchemaTag.AS_BETWEEN_SPACES.length();
                    String alias = line.substring(first);
                /*TODO! check is missing:
                   alias and its mentions at relations are not equals (see PUML situations with creating empty entity) */
                    if (alias.contains(PlantUmlSchemaTag.CURLY_BRACKET_OPENED)) {
                        entityAlias = alias.substring(0, alias.indexOf(PlantUmlSchemaTag.CURLY_BRACKET_OPENED)).trim();
                    } else {
                        entityAlias = alias;
                    }
                }

                // check for other lines except entity name and curly brackets
                // and parse properties
                if (!line.contains(PlantUmlSchemaTag.AS_BETWEEN_SPACES) && !line.contains(PlantUmlSchemaTag.CURLY_BRACKET_OPENED) && !line.contains(PlantUmlSchemaTag.CURLY_BRACKET_CLOSED) && !StringUtils.containsOnly(line, "-")) {
                    PropertyBuilder propertyBuilder = new PropertyBuilder();
                    propertyBuilder.isMandatory(line.startsWith(PlantUmlSchemaTag.MANDATORY));
                    propertyBuilder.isForeignKey(line.contains(PlantUmlSchemaTag.FOREIGN_KEY));

                    if (line.contains(PlantUmlSchemaTag.GENERATED)) {
                        propertyBuilder.isMandatory(true);
                    }

                    List<String> propertyList = Arrays.asList(line.split(" "));
                    var newList = propertyList.stream()
                            .filter(it -> !it.equals(PlantUmlSchemaTag.MANDATORY))
                            .filter(it -> !it.equals(":"))
                            .toList();

                    propertyBuilder.name(newList.get(0));
                    propertyBuilder.type(newList.get(1));

                    Property property = propertyBuilder.build();
                    properties.add(property);
                }
            }

            entities.add(new Entity(entityName, entityAlias, properties));
        });

        assertEquals(3, entities.size());

        return entities;
    }

    // STEP_3. Process each relation line
    private List<Relation> processRelations(List<String> relationsStrings, List<Entity> entities) {
        List<Relation> relations = new ArrayList<>();

        // just one of signs (".", "-") uses between relation arrow signs. min count is 1
        for (String string : relationsStrings) {
            List<String> split = Arrays.stream(string.split(" "))
                    .filter(it -> !it.isBlank())
                    .toList();
            String left = split.get(0);
            String right = split.get(split.size() - 1);
            String arrow = split.get(1);

            Optional<Entity> leftEntity = filterEntities(entities, left);
            Optional<Entity> rightEntity = filterEntities(entities, right);

            assertTrue(leftEntity.isPresent() && rightEntity.isPresent());

            if (leftEntity.isPresent() && rightEntity.isPresent()) {
                UmlRelationType leftRelationType = UmlRelationType.valueOfType(arrow.substring(0, 2));
                EntityRelation leftEntityRelation = new EntityRelation(leftEntity.get(), leftRelationType);

                UmlRelationType rightRelationType = UmlRelationType.valueOfType(arrow.substring(arrow.length() - 2));
                EntityRelation rightEntityRelation = new EntityRelation(rightEntity.get(), rightRelationType);

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
}
