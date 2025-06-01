package com.github.mikekirillov.pojo;

import com.github.mikekirillov.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.mikekirillov.utils.PojoProcessorUtils.camelize;

public class ClassBuilder {
    private final PojoConfig pojoConfig;
    private final String outputFilePath;
    private final Entity entity;
    private final List<Entity> allEntities;
    private final List<Relation> relations;

    public ClassBuilder(PojoConfig pojoConfig, String outputFilePath, Entity entity, List<Entity> allEntities, List<Relation> relations) {
        this.pojoConfig = pojoConfig;
        this.outputFilePath = outputFilePath;
        this.entity = entity;
        this.allEntities = allEntities;
        this.relations = relations;
    }

    public String build() {
        Map<String, String> properties = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        writePackage(stringBuilder);
        writeImports(stringBuilder);
        writeClassDeclaration(stringBuilder);

        if (entity.getProperties().isEmpty()) {
            closeClass(stringBuilder);
            return stringBuilder.toString();
        }

        writeFields(stringBuilder, properties);
        writeConstructors(stringBuilder, properties);
        writeMethods(stringBuilder, properties);
        closeClass(stringBuilder);

        return stringBuilder.toString();
    }

    private void writePackage(StringBuilder stringBuilder) {
        String filePath = outputFilePath.replace("/", ".");
        stringBuilder.append("package ").append(filePath).append(";\n\n");
    }

    private void writeImports(StringBuilder stringBuilder) {
        ImportWriter writer = new ImportWriter(pojoConfig, entity, relations);
        writer.writeImports(stringBuilder);
    }

    private void writeClassDeclaration(StringBuilder stringBuilder) {
        String entityName = camelize(entity.getName(), true);

        if (pojoConfig.isAllowSpringDataJdbcAnnotations()) {
            stringBuilder.append("\n@Table(\"").append(entity.getName()).append("\")");
        }
        stringBuilder.append("\npublic class ").append(entityName).append(" {\n");
    }

    private void writeFields(StringBuilder stringBuilder, Map<String, String> properties) {
        FieldWriter writer = new FieldWriter(pojoConfig, entity, allEntities, relations);
        writer.writeFields(stringBuilder, properties);
    }

    private void writeConstructors(StringBuilder stringBuilder, Map<String, String> properties) {
        ConstructorWriter writer = new ConstructorWriter(pojoConfig, entity, properties);
        writer.writeConstructors(stringBuilder);
    }

    private void writeMethods(StringBuilder stringBuilder, Map<String, String> properties) {
        MethodWriter writer = new MethodWriter(pojoConfig, entity, properties);
        writer.writeMethods(stringBuilder);
    }

    private void closeClass(StringBuilder stringBuilder) {
        stringBuilder.append("}\n");
    }
}
