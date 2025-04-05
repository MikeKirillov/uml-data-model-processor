package com.github.mikekirillov.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum RelationType {
    ZERO_OR_ONE("|o"),
    ZERO_OR_ONE_REVERTED("o|"),
    EXACTLY_ONE("||"),
    ZERO_OR_MANY("}o"),
    ZERO_OR_MANY_REVERTED("o{"),
    ONE_OR_MANY("}|"),
    ONE_OR_MANY_REVERTED("|{");

    private final String type;
    private static final Map<String, RelationType> RELATIONS_MAP = new HashMap<>();
    public static final List<RelationType> RELATIONS = new ArrayList<>();

    static {
        for (RelationType value : values()) {
            RELATIONS_MAP.put(value.type, value);
            RELATIONS.add(value);
        }
    }

    RelationType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static RelationType valueOfType(String type) {
        return RELATIONS_MAP.get(type);
    }

    @Override
    public String toString() {
        return "enums.Relations{" +
                "name='" + name() + '\'' +
                '}';
    }
}
