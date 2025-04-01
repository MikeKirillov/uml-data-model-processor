package com.github.mikekirillov.model;

public class Relation {
    private final EntityRelation leftEntity;
    private final EntityRelation rightEntity;

    public Relation(EntityRelation leftEntity, EntityRelation rightEntity) {
        this.leftEntity = leftEntity;
        this.rightEntity = rightEntity;
    }

    @Override
    public String toString() {
        return "Relation{" +
                "leftEntity=" + leftEntity +
                ", rightEntity=" + rightEntity +
                '}';
    }
}
