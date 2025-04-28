package com.github.mikekirillov.model;

import java.util.Objects;

public class Property {
    private final String name;
    private final String type;
    private final boolean isMandatory;
    private final boolean isGenerated;
    private final boolean isPrimaryKey;
    private final boolean isForeignKey;

    public Property(String name, String type, boolean isMandatory, boolean isGenerated, boolean isForeignKey) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");

        this.name = name;
        this.type = type;
        this.isMandatory = isMandatory;
        this.isGenerated = isGenerated;
        this.isPrimaryKey = isMandatory && isGenerated;
        this.isForeignKey = isForeignKey;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public boolean isForeignKey() {
        return isForeignKey;
    }

    @Override
    public String toString() {
        return "Property{" +
                "name=" + name +
                ", type=" + type +
                ", isMandatory=" + isMandatory +
                ", isGenerated=" + isGenerated +
                ", isPrimaryKey=" + isPrimaryKey +
                ", isForeignKey=" + isForeignKey +
                '}';
    }
}
