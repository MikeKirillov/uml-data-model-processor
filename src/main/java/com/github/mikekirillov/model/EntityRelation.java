package com.github.mikekirillov.model;

import com.github.mikekirillov.enums.RelationType;

public class EntityRelation {
    private final Entity entity;
    private final RelationType relationType;

    public EntityRelation(Entity entity, RelationType relationType) {
        this.entity = entity;
        this.relationType = relationType;
    }

    @Override
    public String toString() {
        return "EntityRelation{" +
                "entity=" + entity +
                ", relationType=" + relationType +
                '}';
    }
}
