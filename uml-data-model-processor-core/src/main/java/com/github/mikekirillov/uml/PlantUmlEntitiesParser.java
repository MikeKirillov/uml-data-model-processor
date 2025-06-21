package com.github.mikekirillov.uml;

import com.github.mikekirillov.constants.PlantUmlSchemaTag;
import com.github.mikekirillov.enums.UmlRelationType;
import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.Property;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Predicate;

public class PlantUmlEntitiesParser implements PlantUmlParser<Entity> {

    private final static Predicate<String> LINE_IS_START_OR_END = line ->
            line.toLowerCase().contains(PlantUmlSchemaTag.START)
                    || line.toLowerCase().contains(PlantUmlSchemaTag.END)
                    || StringUtils.containsOnly(line, "-");
    private final static Predicate<String> LINE_IS_ENTITY_OR_CLASS = line ->
            line.toLowerCase().contains(PlantUmlSchemaTag.OBJECT_TYPE_ENTITY)
                    || line.toLowerCase().contains(PlantUmlSchemaTag.OBJECT_TYPE_CLASS);
    private final static Predicate<String> LINE_HAS_ENTITY_OR_CLASS_OR_RELATION_TYPE = line ->
            line.toLowerCase().contains(PlantUmlSchemaTag.OBJECT_TYPE_ENTITY)
                    || line.toLowerCase().contains(PlantUmlSchemaTag.OBJECT_TYPE_CLASS)
                    || UmlRelationType.getRelations().stream().anyMatch(it -> line.contains(it.getType()));

    @Override
    public List<Entity> parseLinesFrom(List<String> lines) {
        List<Entity> entities = new ArrayList<>();
        List<Property> properties = new ArrayList<>();
        Entity entity = new Entity();
        for (String line : lines) {
            if (Predicate.not(LINE_IS_START_OR_END).test(line)) {
                processEntity(entity, line);
                if (LINE_HAS_ENTITY_OR_CLASS_OR_RELATION_TYPE.negate().test(line)) {
                    processProperty(line, properties);
                    if (!properties.isEmpty()) {
                        entity.setProperties(properties);
                    }
                    if (StringUtils.containsOnly(line, PlantUmlSchemaTag.CURLY_BRACKET_CLOSED)) {
                        entities.add(entity);
                        entity = new Entity();
                        properties = new ArrayList<>();
                    }
                }
            }
        }
        return entities;
    }

    private void processEntity(Entity entity, String line) {
        if (LINE_IS_ENTITY_OR_CLASS.test(line)) {
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
    }

    private void processProperty(String line, List<Property> properties) {
        List<String> strings = Arrays.stream(line.split(" "))
                .filter(it -> !it.isBlank() && !it.contains(":"))
                .toList();
        Property.Builder propertyBuilder = new Property.Builder();
        if (!strings.get(0).equals(PlantUmlSchemaTag.CURLY_BRACKET_CLOSED)) {
            if (strings.get(0).contains(PlantUmlSchemaTag.MANDATORY)) {
                propertyBuilder.isMandatory(true)
                        .name(strings.get(1))
                        .type(strings.get(2));
            } else {
                propertyBuilder.name(strings.get(0))
                        .type(strings.get(1));
            }
            if (strings.stream().anyMatch(it -> it.toLowerCase().contains(PlantUmlSchemaTag.GENERATED))) {
                propertyBuilder.isGenerated(true);
            }
            if (strings.stream().anyMatch(it -> it.toUpperCase().contains(PlantUmlSchemaTag.FOREIGN_KEY))) {
                propertyBuilder.isForeignKey(true);
            }
            properties.add(propertyBuilder.build());
        }
    }
}
