package com.github.mikekirillov.model;

public class PojoConfig {
    private final boolean allowSpringDataJdbcAnnotations;
    private final boolean allowForeignKeyAsEmbeddedEntity;
    private final boolean allowForeignKeyAsEmbeddedEntityByAggregate;
    private final boolean allowNoArgsConstructor;
    private final boolean allowIdArgConstructor;
    private final boolean allowAllArgsConstructor;
    private final boolean allowGetters;
    private final boolean allowSetters;
    private final boolean allowToStringMethod;

    public PojoConfig(boolean allowSpringDataJdbcAnnotations,
                      boolean allowForeignKeyAsEmbeddedEntity,
                      boolean allowForeignKeyAsEmbeddedEntityByAggregate,
                      boolean allowNoArgsConstructor,
                      boolean allowIdArgConstructor,
                      boolean allowAllArgsConstructor,
                      boolean allowGetters,
                      boolean allowSetters,
                      boolean allowToStringMethod) {
        this.allowSpringDataJdbcAnnotations = allowSpringDataJdbcAnnotations;
        this.allowForeignKeyAsEmbeddedEntity = allowForeignKeyAsEmbeddedEntity;
        this.allowForeignKeyAsEmbeddedEntityByAggregate = allowForeignKeyAsEmbeddedEntityByAggregate;
        this.allowNoArgsConstructor = allowNoArgsConstructor;
        this.allowIdArgConstructor = allowIdArgConstructor;
        this.allowAllArgsConstructor = allowAllArgsConstructor;
        this.allowGetters = allowGetters;
        this.allowSetters = allowSetters;
        this.allowToStringMethod = allowToStringMethod;
    }

    public boolean isAllowSpringDataJdbcAnnotations() {
        return allowSpringDataJdbcAnnotations;
    }

    public boolean isAllowForeignKeyAsEmbeddedEntity() {
        return allowForeignKeyAsEmbeddedEntity;
    }

    public boolean isAllowForeignKeyAsEmbeddedEntityByAggregate() {
        return allowForeignKeyAsEmbeddedEntityByAggregate;
    }

    public boolean isAllowNoArgsConstructor() {
        return allowNoArgsConstructor;
    }

    public boolean isAllowIdArgConstructor() {
        return allowIdArgConstructor;
    }

    public boolean isAllowAllArgsConstructor() {
        return allowAllArgsConstructor;
    }

    public boolean isAllowGetters() {
        return allowGetters;
    }

    public boolean isAllowSetters() {
        return allowSetters;
    }

    public boolean isAllowToStringMethod() {
        return allowToStringMethod;
    }
}
