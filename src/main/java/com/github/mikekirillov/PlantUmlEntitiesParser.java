package com.github.mikekirillov;

import com.github.mikekirillov.constants.PlantUmlSchemaTag;
import com.github.mikekirillov.enums.RelationType;
import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.Property;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class PlantUmlEntitiesParser implements PlantUmlParser<Entity> {

    @Override
    public List<Entity> parseLinesFrom(List<String> lines) {
        List<Entity> entities = new ArrayList<>();
        List<Property> properties = new ArrayList<>();
        Entity entity = null;

        for (String line : lines) {

            if (!line.toLowerCase().contains(PlantUmlSchemaTag.START) && !line.toLowerCase().contains(PlantUmlSchemaTag.END) && !StringUtils.containsOnly(line, "-")) {

                // processing entity
                if (line.toLowerCase().contains(PlantUmlSchemaTag.OBJECT_TYPE_ENTITY) || line.toLowerCase().contains(PlantUmlSchemaTag.OBJECT_TYPE_CLASS)) {
                    entity = new Entity();
                    List<String> strings = Arrays.stream(line.split(" "))
                            .map(String::toLowerCase)
                            .map(string -> string.replace("\"", "").replace("'", "").replace("{", ""))
                            .filter(string -> !string.isBlank())
                            .toList();

                    entity.setName(strings.get(1));

                    if (strings.contains(PlantUmlSchemaTag.AS_NO_SPACES)) {
                        int as = strings.indexOf(PlantUmlSchemaTag.AS_NO_SPACES);
                        entity.setAlias(strings.get(as + 1));
                    }
                }

                // processing properties
                if (!line.toLowerCase().contains(PlantUmlSchemaTag.OBJECT_TYPE_ENTITY) && !line.toLowerCase().contains(PlantUmlSchemaTag.OBJECT_TYPE_CLASS) && RelationType.getRelations().stream().noneMatch(it -> line.contains(it.getType()))) {
                    List<String> array = Arrays.stream(line.split(" "))
                            .filter(it -> !it.isBlank() && !it.contains(":"))
                            .toList();

                    PropertyBuilder propertyBuilder = new PropertyBuilder();
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
            }
        }

        return entities;
    }
}
