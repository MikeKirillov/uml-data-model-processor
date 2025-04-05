package com.github.mikekirillov;

import com.github.mikekirillov.enums.RelationType;
import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.EntityRelation;
import com.github.mikekirillov.model.Property;
import com.github.mikekirillov.model.Relation;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ParserSecondTDDTest {
    private static final String RESOURCES_PATH = "src/test/resources/";
    private static final String TXT_FILE_PATH = "data-base-model.txt";
    private static final String PU_FILE_PATH = "data-base-model.pu";
    private static final String PUML_FILE_PATH = "data-base-model.puml";

    @Test
    public void shouldReadLines() throws IOException {
        Path path = Path.of(RESOURCES_PATH, TXT_FILE_PATH);

        // TODO! think about importing basic validation by PUML tools

        List<String> lines = Files.readAllLines(path).stream()
                .map(String::trim)
                .collect(Collectors.toList());

        assertEquals(PumlSchemaTag.START, lines.get(0));
        assertEquals(PumlSchemaTag.END, lines.get(lines.size() - 1));

        List<Entity> entities = new ArrayList<>();
        List<Relation> relations = new ArrayList<>();

        entitiesAndRelations(lines, entities, relations);

        // System.out.println("entities".toUpperCase() + ": " + entities); // TODO DELETE
        // System.out.println("relations".toUpperCase() + ": " + relations); // TODO DELETE

        assertEquals(3, entities.size());
        assertEquals("gender", entities.get(0).getName());
        assertEquals(2, entities.get(0).getProperties().size());
        assertEquals("state", entities.get(1).getName());
        assertEquals(2, entities.get(1).getProperties().size());
        assertEquals("client", entities.get(2).getName());
        assertEquals(8, entities.get(2).getProperties().size());
        assertEquals(2, relations.size());
    }

    private void entitiesAndRelations(List<String> lines, List<Entity> entities, List<Relation> relations) {
        Entity entity = null;
        List<Property> properties = new ArrayList<>();

        for (String line : lines) {

            if (!line.toLowerCase().contains(PumlSchemaTag.START) && !line.toLowerCase().contains(PumlSchemaTag.END) && !StringUtils.containsOnly(line, "-")) {

                // process entity
                if (line.toLowerCase().contains(PumlSchemaTag.OBJECT_TYPE_ENTITY) || line.toLowerCase().contains(PumlSchemaTag.OBJECT_TYPE_CLASS)) {
                    entity = new Entity();
                    List<String> strings = Arrays.stream(line.split(" "))
                            .map(String::toLowerCase)
                            .map(string -> string.replace("\"", "").replace("'", "").replace("{", ""))
                            .filter(string -> !string.isBlank())
                            .toList();

                    entity.setName(strings.get(1));

                    if (strings.contains(PumlSchemaTag.AS_NO_SPACES)) {
                        int as = strings.indexOf(PumlSchemaTag.AS_NO_SPACES);
                        entity.setAlias(strings.get(as + 1));
                    }
                }

                // process properties
                if (!line.toLowerCase().contains(PumlSchemaTag.OBJECT_TYPE_ENTITY) && !line.toLowerCase().contains(PumlSchemaTag.OBJECT_TYPE_CLASS) && RelationType.RELATIONS.stream().noneMatch(it -> line.contains(it.getType()))) {
                    List<String> array = Arrays.stream(line.split(" "))
                            .filter(it -> !it.isBlank() && !it.contains(":"))
                            .toList();

                    PropertyBuilder propertyBuilder = new PropertyBuilder();
                    if (!array.get(0).equals(PumlSchemaTag.CURLY_BRACKET_CLOSED)) {
                        if (array.get(0).contains(PumlSchemaTag.MANDATORY)) {
                            propertyBuilder.isMandatory(true);
                            propertyBuilder.name(array.get(1));
                            propertyBuilder.type(array.get(2));
                        } else {
                            propertyBuilder.name(array.get(0));
                            propertyBuilder.type(array.get(1));
                        }

                        if (array.stream().anyMatch(it -> it.toLowerCase().contains(PumlSchemaTag.GENERATED))) {
                            propertyBuilder.isGenerated(true);
                        }

                        if (array.stream().anyMatch(it -> it.toUpperCase().contains(PumlSchemaTag.FOREIGN_KEY))) {
                            propertyBuilder.isForeignKey(true);
                        }

                        properties.add(propertyBuilder.build());
                    }

                    if (Objects.nonNull(entity)) {
                        if (!properties.isEmpty()) {
                            entity.setProperties(properties);
                        }

                        if (StringUtils.containsOnly(line, PumlSchemaTag.CURLY_BRACKET_CLOSED)) {
                            entities.add(entity);
                            entity = null;
                            properties = new ArrayList<>();
                        }
                    }
                }

                // process relations
                if (RelationType.RELATIONS.stream().anyMatch(it -> line.contains(it.getType()))) {
                    List<String> array = Arrays.stream(line.split(" "))
                            .filter(it -> !it.isBlank())
                            .toList();

                    String left = array.get(0);
                    String right = array.get(array.size() - 1);
                    String relationArrow = array.get(1);

                    if (!entities.isEmpty()) {
                        Optional<Entity> leftEntity = findEntity(entities, left);
                        Optional<Entity> rightEntity = findEntity(entities, right);

                        assertTrue(leftEntity.isPresent());
                        assertTrue(rightEntity.isPresent());

                        if (leftEntity.isPresent() && rightEntity.isPresent()) {
                            RelationType leftRelationType = RelationType.valueOfType(relationArrow.substring(0, 2));
                            EntityRelation leftEntityRelation = new EntityRelation(leftEntity.get(), leftRelationType);

                            RelationType rightRelationType = RelationType.valueOfType(relationArrow.substring(relationArrow.length() - 2));
                            EntityRelation rightEntityRelation = new EntityRelation(rightEntity.get(), rightRelationType);

                            Relation relation = new Relation(leftEntityRelation, rightEntityRelation);

                            relations.add(relation);
                        }
                    }
                }
            }
        }
    }

    private Optional<Entity> findEntity(List<Entity> entities, String tag) {
        return entities.stream()
                .filter(entity -> entity.getName().equals(tag) || entity.getAlias().equals(tag))
                .findFirst();
    }
}