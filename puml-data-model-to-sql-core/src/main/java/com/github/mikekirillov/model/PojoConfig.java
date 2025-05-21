package com.github.mikekirillov.model;

public class PojoConfig {
    private final boolean requiresSpringDataJdbcAnnotations;
    private final boolean allowForeignKeyAsEmbeddedEntity;
    private final boolean allowForeignKeyAsEmbeddedEntityByAggregate;
    private final boolean requiresNoArgsConstructor;
    private final boolean requiresIdArgConstructor;
    private final boolean requiresAllArgsConstructor;
    private final boolean requiresGetters;
    private final boolean requiresSetters;
    private final boolean requiresToStringMethod;

    public PojoConfig(boolean requiresSpringDataJdbcAnnotations,
                      boolean allowForeignKeyAsEmbeddedEntity,
                      boolean allowForeignKeyAsEmbeddedEntityByAggregate,
                      boolean requiresNoArgsConstructor,
                      boolean requiresIdArgConstructor,
                      boolean requiresAllArgsConstructor,
                      boolean requiresGetters,
                      boolean requiresSetters,
                      boolean requiresToStringMethod) {
        this.requiresSpringDataJdbcAnnotations = requiresSpringDataJdbcAnnotations;
        this.allowForeignKeyAsEmbeddedEntity = allowForeignKeyAsEmbeddedEntity;
        this.allowForeignKeyAsEmbeddedEntityByAggregate = allowForeignKeyAsEmbeddedEntityByAggregate;
        this.requiresNoArgsConstructor = requiresNoArgsConstructor;
        this.requiresIdArgConstructor = requiresIdArgConstructor;
        this.requiresAllArgsConstructor = requiresAllArgsConstructor;
        this.requiresGetters = requiresGetters;
        this.requiresSetters = requiresSetters;
        this.requiresToStringMethod = requiresToStringMethod;
    }

    public boolean isRequiresSpringDataJdbcAnnotations() {
        return requiresSpringDataJdbcAnnotations;
    }

    public boolean isAllowForeignKeyAsEmbeddedEntity() {
        return allowForeignKeyAsEmbeddedEntity;
    }

    public boolean isAllowForeignKeyAsEmbeddedEntityByAggregate() {
        return allowForeignKeyAsEmbeddedEntityByAggregate;
    }

    public boolean isRequiresNoArgsConstructor() {
        return requiresNoArgsConstructor;
    }

    public boolean isRequiresIdArgConstructor() {
        return requiresIdArgConstructor;
    }

    public boolean isRequiresAllArgsConstructor() {
        return requiresAllArgsConstructor;
    }

    public boolean isRequiresGetters() {
        return requiresGetters;
    }

    public boolean isRequiresSetters() {
        return requiresSetters;
    }

    public boolean isRequiresToStringMethod() {
        return requiresToStringMethod;
    }
}
