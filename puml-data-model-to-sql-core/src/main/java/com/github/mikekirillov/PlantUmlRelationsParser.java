package com.github.mikekirillov;

import com.github.mikekirillov.enums.UmlRelationType;
import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.EntityRelation;
import com.github.mikekirillov.model.Relation;

import java.util.*;
import java.util.function.Predicate;

import static com.github.mikekirillov.utils.PojoProcessorUtils.checkRelationIsOneOrMany;

public class PlantUmlRelationsParser implements PlantUmlParser<Relation> {

    private final static Predicate<String> LINE_CONTAINS_RELATION_TYPE = line ->
            UmlRelationType.getRelations().stream().anyMatch(it -> line.contains(it.getType()));

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

    public List<Relation> getBridgeEntities(List<Relation> relations) {
        return relations.stream()
                .filter(relation -> checkEntityIsBridge(relation.getRightEntity())
                        || checkEntityIsBridge(relation.getLeftEntity()))
                .toList();
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
                Optional<Entity> leftEntity = findEntity(left);
                Optional<Entity> rightEntity = findEntity(right);

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

    private Optional<Entity> findEntity(String tag) {
        return entities.stream()
                .filter(entity -> anyContains(tag, entity.getName(), entity.getAlias()))
                .findFirst();
    }

    private boolean anyContains(String tag, String... name) {
        return Arrays.stream(name)
                .filter(Objects::nonNull)
                .anyMatch(it -> it.equals(tag));
    }

    private boolean checkEntityIsBridge(EntityRelation entityRelation) {
        String name = entityRelation.getEntity().getName();
        if (name.contains("_")) {
            String[] split = name.split("_");
            if (split.length == 2) {
                // the entity is a bridge between two others if it consists of two real entities names
                // that contains by 'entities' list
                List<String> entNames = entities.stream()
                        .map(Entity::getName)
                        .toList();
                return checkRelationIsOneOrMany(entityRelation)
                        && entNames.contains(split[0])
                        && entNames.contains(split[1]);
            }
            return false;
        }
        return false;
    }
}
