package com.github.mikekirillov.model;

import com.github.mikekirillov.enums.UmlRelationType;

public class EntityRelation {
    private final Entity entity;
    private final UmlRelationType relationType;

    public EntityRelation(Entity entity, UmlRelationType relationType) {
        this.entity = entity;
        this.relationType = relationType;
    }

    public Entity getEntity() {
        return entity;
    }

    public UmlRelationType getRelationType() {
        return relationType;
    }

    @Override
    public String toString() {
        return "EntityRelation{" +
                "entity=" + entity +
                ", relationType=" + relationType +
                '}';
    }
}
