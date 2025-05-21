package com.github.mikekirillov.utils;

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
}
