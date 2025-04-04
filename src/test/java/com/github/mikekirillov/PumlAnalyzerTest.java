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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.mikekirillov.PumlSchemaTag.AS_NO_SPACES;
import static org.junit.jupiter.api.Assertions.*;

class PumlAnalyzerTest {
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

        // System.out.println("entities".toUpperCase() + ": " + entities);
        // System.out.println("relations".toUpperCase() + ": " + relations);

        assertEquals(3, entities.size());
        assertEquals("gender", entities.get(0).getName());
        assertEquals(2, entities.get(0).getProperties().size());
        assertEquals("state", entities.get(1).getName());
        assertEquals(2, entities.get(1).getProperties().size());
        assertEquals("client", entities.get(2).getName());
        assertEquals(8, entities.get(2).getProperties().size());
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
                if (!line.toLowerCase().contains(PumlSchemaTag.OBJECT_TYPE_ENTITY) && !line.toLowerCase().contains(PumlSchemaTag.OBJECT_TYPE_CLASS)) {
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

            }

            // process relations
            // TODO...
        }
    }


}