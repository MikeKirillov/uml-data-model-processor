package com.github.mikekirillov.model;

import java.util.Objects;

public class Property {
    // private String line;
    private final String name;
    private final String type;
    private final boolean isMandatory;
    private final boolean isGenerated;
    private final boolean isPrimaryKey;
    private final boolean isForeignKey;

    public Property(/*String line,*/ String name, String type, boolean isMandatory, boolean isGenerated, boolean isForeignKey) {
        // Objects.requireNonNull(line, "line");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");

        // this.line = line;
        this.name = name;
        this.type = type;
        this.isMandatory = isMandatory;
        this.isGenerated = isGenerated;
        this.isPrimaryKey = isMandatory && isGenerated;
        this.isForeignKey = isForeignKey;
    }

    @Override
    public String toString() {
        return "Property{" +
                // "line=" + line +
                "name=" + name +
                // ", name=" + name +
                ", type=" + type +
                ", isMandatory=" + isMandatory +
                ", isGenerated=" + isGenerated +
                ", isPrimaryKey=" + isPrimaryKey +
                ", isForeignKey=" + isForeignKey +
                '}';
    }
}
