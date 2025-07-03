package com.github.mikekirillov.tdd;

import com.github.mikekirillov.constants.PlantUmlSchemaTag;
import com.github.mikekirillov.constants.SqlSchemaTag;
import com.github.mikekirillov.enums.UmlRelationType;
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

import static com.github.mikekirillov.utils.TestUtils.RESOURCES_PATH_IN;
import static com.github.mikekirillov.utils.TestUtils.TXT_FILE_PATH_IN;
import static org.junit.jupiter.api.Assertions.*;

class ParserSecondTDDTest {

    @Test
    public void shouldReadLines() throws IOException {
        Path path = Path.of(RESOURCES_PATH_IN, TXT_FILE_PATH_IN);

        // TODO! think about importing basic validation by PUML tools

        List<String> lines = Files.readAllLines(path).stream()
                .map(String::trim)
                .collect(Collectors.toList());

        assertEquals(PlantUmlSchemaTag.START, lines.get(0));
        assertEquals(PlantUmlSchemaTag.END, lines.get(lines.size() - 1));

        List<Entity> entities = new ArrayList<>();
        List<Relation> relations = new ArrayList<>();

        // processing entities, properties and relations
        entitiesAndRelations(lines, entities, relations);

        assertEquals(3, entities.size());
        assertEquals("gender", entities.get(0).getName());
        assertEquals(2, entities.get(0).getProperties().size());
        assertEquals("state", entities.get(1).getName());
        assertEquals(3, entities.get(1).getProperties().size());
        assertEquals("client", entities.get(2).getName());
        assertEquals(5, entities.get(2).getProperties().size());
        assertEquals(3, relations.size());

        // generating schema.sql file
        String schemaSql = generateSchema(entities);
    }

    private void entitiesAndRelations(List<String> lines, List<Entity> entities, List<Relation> relations) {
        Entity entity = null;
        List<Property> properties = new ArrayList<>();

        for (String line : lines) {

            if (!line.toLowerCase().contains(PlantUmlSchemaTag.START) && !line.toLowerCase().contains(PlantUmlSchemaTag.END) && !StringUtils.containsOnly(line, "-")) {

                // process entity
                if (line.toLowerCase().contains(PlantUmlSchemaTag.OBJECT_TYPE_ENTITY) || line.toLowerCase().contains(PlantUmlSchemaTag.OBJECT_TYPE_CLASS)) {
                    entity = new Entity();
                    List<String> strings = Arrays.stream(line.split(" "))
                            .map(String::toLowerCase)
                            .map(string -> string.replace("\"", "").replace("'", "").replace("{", ""))
                            .filter(string -> !string.isBlank())
                            .toList();

                    entity.setName(strings.get(1));

                    if (strings.contains(PlantUmlSchemaTag.AS)) {
                        int as = strings.indexOf(PlantUmlSchemaTag.AS);
                        entity.setAlias(strings.get(as + 1));
                    }
                }

                // process properties
                if (!line.toLowerCase().contains(PlantUmlSchemaTag.OBJECT_TYPE_ENTITY) && !line.toLowerCase().contains(PlantUmlSchemaTag.OBJECT_TYPE_CLASS) && UmlRelationType.getRelations().stream().noneMatch(it -> line.contains(it.getType()))) {
                    List<String> array = Arrays.stream(line.split(" "))
                            .filter(it -> !it.isBlank() && !it.contains(":"))
                            .toList();

                    Property.Builder propertyBuilder = new Property.Builder();
                    if (!array.get(0).equals(PlantUmlSchemaTag.CURLY_BRACKET_CLOSED)) {
                        if (array.get(0).contains(PlantUmlSchemaTag.MANDATORY)) {
                            propertyBuilder.isMandatory(true);
                            propertyBuilder.name(array.get(1));
                            propertyBuilder.type(array.get(2));
                        } else {
                            propertyBuilder.name(array.get(0));
                            propertyBuilder.type(array.get(1));
                        }

                        if (array.stream().anyMatch(it -> it.toLowerCase().contains(PlantUmlSchemaTag.GENERATED))) {
                            propertyBuilder.isGenerated(true);
                        }

                        if (array.stream().anyMatch(it -> it.toUpperCase().contains(PlantUmlSchemaTag.FOREIGN_KEY))) {
                            propertyBuilder.isForeignKey(true);
                        }

                        properties.add(propertyBuilder.build());
                    }

                    if (Objects.nonNull(entity)) {
                        if (!properties.isEmpty()) {
                            entity.setProperties(properties);
                        }

                        if (StringUtils.containsOnly(line, PlantUmlSchemaTag.CURLY_BRACKET_CLOSED)) {
                            entities.add(entity);
                            entity = null;
                            properties = new ArrayList<>();
                        }
                    }
                }

                // process relations
                if (UmlRelationType.getRelations().stream().anyMatch(it -> line.contains(it.getType()))) {
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
                            UmlRelationType leftRelationType = UmlRelationType.valueOfType(relationArrow.substring(0, 2));
                            UmlRelationType rightRelationType = UmlRelationType.valueOfType(relationArrow.substring(relationArrow.length() - 2));
                            Relation relation = new Relation(
                                    new EntityRelation(leftEntity.get(), leftRelationType),
                                    new EntityRelation(rightEntity.get(), rightRelationType)
                            );
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

    private String generateSchema(List<Entity> entities) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Entity entity : entities) {
            stringBuilder.append(SqlSchemaTag.CREATE_TABLE_IF_NOT_EXISTS);
            stringBuilder.append(' ');
            stringBuilder.append(entity.getName());
            stringBuilder.append(SqlSchemaTag.BRACKET_OPENED);

            List<Property> properties = entity.getProperties();
            StringBuilder secondSb = null;

            for (Property property : properties) {
                stringBuilder.append(SqlSchemaTag.NEW_LINE);
                stringBuilder.append(property.getName());
                stringBuilder.append(' ');
                stringBuilder.append(property.getType());

                if (property.isMandatory()) {
                    stringBuilder.append(' ');
                    stringBuilder.append(SqlSchemaTag.NOT_NULL);
                } else {
                    stringBuilder.append(' ');
                    stringBuilder.append(SqlSchemaTag.NULL);
                }

                if (property.isPrimaryKey()) {
                    stringBuilder.append(' ');
                    stringBuilder.append(SqlSchemaTag.AUTO_INCREMENT);
                    stringBuilder.append(' ');
                    stringBuilder.append(SqlSchemaTag.PRIMARY_KEY);
                }

                stringBuilder.append(SqlSchemaTag.COMMA);

                if (property.isForeignKey()) {
                    Entity referencedEntity = getReferencedEntity(entities, property);
                    Property referencedProperty = getReferencedProperty(referencedEntity, property);

                    if (Objects.isNull(secondSb)) {
                        secondSb = new StringBuilder();
                    }

                    secondSb.append(SqlSchemaTag.NEW_LINE);
                    secondSb.append(SqlSchemaTag.FOREIGN_KEY);
                    secondSb.append(SqlSchemaTag.BRACKET_OPENED);
                    secondSb.append(property.getName());
                    secondSb.append(SqlSchemaTag.BRACKET_CLOSED);
                    secondSb.append(' ');
                    secondSb.append(SqlSchemaTag.REFERENCES);
                    secondSb.append(' ');
                    secondSb.append(referencedEntity.getName());
                    secondSb.append(SqlSchemaTag.BRACKET_OPENED);
                    secondSb.append(referencedProperty.getName());
                    secondSb.append(SqlSchemaTag.BRACKET_CLOSED);
                    secondSb.append(SqlSchemaTag.COMMA);
                }
            }

            if (Objects.nonNull(secondSb)) {
                stringBuilder.append(secondSb);
                stringBuilder.append(SqlSchemaTag.NEW_LINE);
            } else {
                stringBuilder.append(SqlSchemaTag.NEW_LINE);
            }

            int indexOfNewLine = stringBuilder.lastIndexOf(SqlSchemaTag.NEW_LINE);
            stringBuilder.replace(indexOfNewLine - 1, indexOfNewLine, "");

            stringBuilder.append(SqlSchemaTag.BRACKET_CLOSED);
            stringBuilder.append(SqlSchemaTag.SEMICOLON);
            stringBuilder.append(SqlSchemaTag.NEW_LINE);
        }

        return stringBuilder.toString();
    }

    private Entity getReferencedEntity(List<Entity> entities, Property property) {
        return entities.stream()
                .filter(e -> property.getName().contains(e.getName()))
                .findFirst()
                .orElseThrow();
    }

    private Property getReferencedProperty(Entity entity, Property property) {
        return entity.getProperties().stream()
                .filter(p -> property.getName().contains(p.getName()))
                .findFirst()
                .orElseThrow();
    }
}