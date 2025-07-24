package io.github.mikekirillov.pojo;

import io.github.mikekirillov.EntityProcessor;
import io.github.mikekirillov.model.Entity;
import io.github.mikekirillov.model.PojoConfig;
import io.github.mikekirillov.model.Relation;

import java.util.List;

public class ClassGenerator implements EntityProcessor {
    private final PojoConfig pojoConfig;
    private final String outputPackageName;
    private final Entity entity;
    private final List<Entity> allEntities;
    private final List<Relation> relations;

    public ClassGenerator(PojoConfig pojoConfig, String outputPackageName, Entity entity, List<Entity> allEntities, List<Relation> relations) {
        this.pojoConfig = pojoConfig;
        this.outputPackageName = outputPackageName;
        this.entity = entity;
        this.allEntities = allEntities;
        this.relations = relations;
    }

    @Override
    public String generate() {
        ClassBuilder builder = new ClassBuilder(pojoConfig, outputPackageName, entity, allEntities, relations);
        return builder.build();
    }
}
