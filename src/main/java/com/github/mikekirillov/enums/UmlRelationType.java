package com.github.mikekirillov.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum UmlRelationType {
    ZERO_OR_ONE("|o"),
    ZERO_OR_ONE_REVERTED("o|"),
    EXACTLY_ONE("||"),
    ZERO_OR_MANY("}o"),
    ZERO_OR_MANY_REVERTED("o{"),
    ONE_OR_MANY("}|"),
    ONE_OR_MANY_REVERTED("|{");

    private final String type;
    private static final Map<String, UmlRelationType> RELATIONS_MAP = new HashMap<>();
    private static final List<UmlRelationType> RELATIONS = new ArrayList<>();

    static {
        for (UmlRelationType value : values()) {
            RELATIONS_MAP.put(value.type, value);
            RELATIONS.add(value);
        }
    }

    UmlRelationType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static UmlRelationType valueOfType(String type) {
        return RELATIONS_MAP.get(type);
    }

    public static List<UmlRelationType> getRelations() {
        return RELATIONS;
    }

    @Override
    public String toString() {
        return "enums.Relations{" +
                "name='" + name() + '\'' +
                '}';
    }
}
