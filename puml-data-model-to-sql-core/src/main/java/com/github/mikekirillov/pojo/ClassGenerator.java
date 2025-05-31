package com.github.mikekirillov.pojo;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.PojoConfig;
import com.github.mikekirillov.model.Relation;

import java.util.List;

public class ClassGenerator {
    private final PojoConfig pojoConfig;
    private final String outputFilePath;
    private final Entity entity;
    private final List<Entity> allEntities;
    private final List<Relation> relations;

    public ClassGenerator(PojoConfig pojoConfig, String outputFilePath, Entity entity, List<Entity> allEntities, List<Relation> relations) {
        this.pojoConfig = pojoConfig;
        this.outputFilePath = outputFilePath;
        this.entity = entity;
        this.allEntities = allEntities;
        this.relations = relations;
    }

    public String generate() {
        ClassBuilder builder = new ClassBuilder(pojoConfig, outputFilePath, entity, allEntities, relations);
        return builder.build();
    }
}
