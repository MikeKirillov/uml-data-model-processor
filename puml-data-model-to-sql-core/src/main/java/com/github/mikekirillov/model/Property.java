package com.github.mikekirillov.model;

import java.util.Objects;

public class Property {
    private final String name;
    private final String type;
    private final boolean isMandatory;
    private final boolean isGenerated;
    private final boolean isPrimaryKey;
    private final boolean isForeignKey;

    private Property(Builder builder) {
        Objects.requireNonNull(builder.name, "name");
        Objects.requireNonNull(builder.type, "type");

        this.name = builder.name;
        this.type = builder.type;
        this.isMandatory = builder.isMandatory;
        this.isGenerated = builder.isGenerated;
        this.isPrimaryKey = builder.isMandatory && builder.isGenerated;
        this.isForeignKey = builder.isForeignKey;
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

    public static class Builder {
        private String name;
        private String type;
        private boolean isMandatory;
        private boolean isGenerated;
        private boolean isForeignKey;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder isMandatory(boolean isMandatory) {
            this.isMandatory = isMandatory;
            return this;
        }

        public Builder isGenerated(boolean isGenerated) {
            this.isGenerated = isGenerated;
            return this;
        }

        public Builder isForeignKey(boolean isForeignKey) {
            this.isForeignKey = isForeignKey;
            return this;
        }

        public Property build() {
            return new Property(this);
        }
    }
}
