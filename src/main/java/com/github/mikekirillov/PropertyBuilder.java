package com.github.mikekirillov;

import com.github.mikekirillov.model.Property;

public class PropertyBuilder {
    private String name;
    private String type;
    private boolean isMandatory;
    private boolean isGenerated;
    private boolean isForeignKey;

    public PropertyBuilder name(String name) {
        this.name = name;
        return this;
    }

    public PropertyBuilder type(String type) {
        this.type = type;
        return this;
    }

    public PropertyBuilder isMandatory(boolean isMandatory) {
        this.isMandatory = isMandatory;
        return this;
    }

    public PropertyBuilder isGenerated(boolean isGenerated) {
        this.isGenerated = isGenerated;
        return this;
    }

    public PropertyBuilder isForeignKey(boolean isForeignKey) {
        this.isForeignKey = isForeignKey;
        return this;
    }

    public Property build() {
        return new Property(name, type, isMandatory, isGenerated, isForeignKey);
    }
}
