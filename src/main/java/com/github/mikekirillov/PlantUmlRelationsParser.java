package com.github.mikekirillov;

import com.github.mikekirillov.enums.RelationType;
import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.EntityRelation;
import com.github.mikekirillov.model.Relation;

import java.util.*;
import java.util.function.Predicate;

public class PlantUmlRelationsParser implements PlantUmlParser<Relation> {
    private final static Predicate<String> LINE_CONTAINS_RELATION_TYPE = line -> RelationType.getRelations().stream().anyMatch(it -> line.contains(it.getType()));

    private final List<Entity> entities;

    public PlantUmlRelationsParser(List<Entity> entities) {
        this.entities = entities;
    }

    @Override
    public List<Relation> parseLinesFrom(List<String> lines) {
        List<Relation> relations = new ArrayList<>();

        for (String line : lines) {
            processLine(line, relations);
        }

        return relations;
    }

    private void processLine(String line, List<Relation> relations) {
        if (LINE_CONTAINS_RELATION_TYPE.test(line)) {
            List<String> array = Arrays.stream(line.split(" "))
                    .filter(it -> !it.isBlank())
                    .toList();
            String left = array.get(0);
            String right = array.get(array.size() - 1);
            String relationArrow = array.get(1);

            if (!entities.isEmpty()) {
                Optional<Entity> leftEntity = findEntity(entities, left);
                Optional<Entity> rightEntity = findEntity(entities, right);

                if (leftEntity.isPresent() && rightEntity.isPresent()) {
                    RelationType leftRelationType = RelationType.valueOfType(relationArrow.substring(0, 2));
                    RelationType rightRelationType = RelationType.valueOfType(relationArrow.substring(relationArrow.length() - 2));
                    Relation relation = new Relation(
                            new EntityRelation(leftEntity.get(), leftRelationType),
                            new EntityRelation(rightEntity.get(), rightRelationType)
                    );
                    relations.add(relation);
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
