package com.github.mikekirillov.utils;

import com.github.mikekirillov.enums.UmlRelationType;
import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.EntityRelation;
import com.github.mikekirillov.model.Relation;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PojoProcessorUtils {

    public static String convertType(String type) {
        return switch (type.toLowerCase()) {
            case "int", "integer" -> "int";
            case "boolean" -> "boolean";
            case "datetime", "timestamp" -> "Date";
            default -> "String";
        };
    }

    public static String snakeToCamel(String camel, boolean capitalize) {
        if (camel.contains("_")) {
            camel = Stream.of(camel.split("_"))
                    .map(StringUtils::capitalize)
                    .collect(Collectors.joining());
        }
        return capitalize ? StringUtils.capitalize(camel) : StringUtils.uncapitalize(camel);
    }

    public static boolean checkRelationIsOneOrMany(EntityRelation entityRelation) {
        return entityRelation.getRelationType().equals(UmlRelationType.ONE_OR_MANY)
                || entityRelation.getRelationType().equals(UmlRelationType.ONE_OR_MANY_REVERTED);
    }

    public static boolean checkRelationAndEntityEqByName(Relation relation, Entity entity) {
        return checkEntityRelationAndEntityEqByName(relation.getRightEntity(), entity)
                || checkEntityRelationAndEntityEqByName(relation.getLeftEntity(), entity);
    }

    private static boolean checkEntityRelationAndEntityEqByName(EntityRelation relation, Entity entity) {
        var relationName = relation.getEntity().getName();
        return relationName.contains("_") && relationName.equalsIgnoreCase(entity.getName());
    }

    public static boolean checkRelationAndMainEntityEqByName(Relation relation, Entity entity) {
        var right = relation.getRightEntity();
        var left = relation.getLeftEntity();
        return checkRelationIsOneOrMany(right) && checkRelationEqualsMainEntity(right, entity)
                || checkRelationIsOneOrMany(left) && checkRelationEqualsMainEntity(left, entity);
    }

    private static boolean checkRelationEqualsMainEntity(EntityRelation relation, Entity entity) {
        var relationName = relation.getEntity().getName();
        return !entity.getName().contains("_") && relationName.startsWith(entity.getName());
    }
}
