package com.github.mikekirillov.model;

public class PojoConfig {
    private boolean allowSpringDataJdbcAnnotations;
    private boolean allowForeignKeyAsEmbeddedEntity;
    private boolean allowForeignKeyAsEmbeddedEntityByAggregate;
    private boolean allowNoArgsConstructor;
    private boolean allowIdArgConstructor;
    private boolean allowAllArgsConstructor;
    private boolean allowGetters;
    private boolean allowSetters;
    private boolean allowToStringMethod;

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

    public void setAllowSpringDataJdbcAnnotations(boolean allowSpringDataJdbcAnnotations) {
        this.allowSpringDataJdbcAnnotations = allowSpringDataJdbcAnnotations;
    }

    public void setAllowForeignKeyAsEmbeddedEntity(boolean allowForeignKeyAsEmbeddedEntity) {
        this.allowForeignKeyAsEmbeddedEntity = allowForeignKeyAsEmbeddedEntity;
    }

    public void setAllowForeignKeyAsEmbeddedEntityByAggregate(boolean allowForeignKeyAsEmbeddedEntityByAggregate) {
        this.allowForeignKeyAsEmbeddedEntityByAggregate = allowForeignKeyAsEmbeddedEntityByAggregate;
    }

    public void setAllowNoArgsConstructor(boolean allowNoArgsConstructor) {
        this.allowNoArgsConstructor = allowNoArgsConstructor;
    }

    public void setAllowIdArgConstructor(boolean allowIdArgConstructor) {
        this.allowIdArgConstructor = allowIdArgConstructor;
    }

    public void setAllowAllArgsConstructor(boolean allowAllArgsConstructor) {
        this.allowAllArgsConstructor = allowAllArgsConstructor;
    }

    public void setAllowGetters(boolean allowGetters) {
        this.allowGetters = allowGetters;
    }

    public void setAllowSetters(boolean allowSetters) {
        this.allowSetters = allowSetters;
    }

    public void setAllowToStringMethod(boolean allowToStringMethod) {
        this.allowToStringMethod = allowToStringMethod;
    }
}
