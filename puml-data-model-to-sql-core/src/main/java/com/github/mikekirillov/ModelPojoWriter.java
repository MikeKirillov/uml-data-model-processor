package com.github.mikekirillov;

import com.github.mikekirillov.model.Entity;

import java.util.List;

public interface ModelPojoWriter {
    void processEntities(List<Entity> entities);
}
