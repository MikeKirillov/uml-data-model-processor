package com.github.mikekirillov;

import com.github.mikekirillov.constants.SqlSchemaTag;
import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.Property;

import java.util.List;

public class SqlSchemaProcessor {
    private final List<Entity> entities;

    public SqlSchemaProcessor(List<Entity> entities) {
        this.entities = entities;
    }

    public String process() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Entity entity : entities) {
            processEntity(stringBuilder, entity);
        }

        return stringBuilder.toString();
    }

    private void processEntity(StringBuilder stringBuilder, Entity entity) {
        stringBuilder.append(SqlSchemaTag.CREATE_TABLE_IF_NOT_EXISTS);
        stringBuilder.append(SqlSchemaTag.SPACE);
        stringBuilder.append(entity.getName());
        stringBuilder.append(SqlSchemaTag.BRACKET_OPENED);

        List<Property> properties = entity.getProperties();
        StringBuilder secondSb = new StringBuilder();

        for (Property property : properties) {
            processProperty(stringBuilder, secondSb, property);
        }

        stringBuilder.append(secondSb);
        stringBuilder.append(SqlSchemaTag.NEW_LINE);

        int indexOfNewLine = stringBuilder.lastIndexOf(SqlSchemaTag.NEW_LINE);
        stringBuilder.replace(indexOfNewLine - 1, indexOfNewLine, "");

        stringBuilder.append(SqlSchemaTag.BRACKET_CLOSED);
        stringBuilder.append(SqlSchemaTag.SEMICOLON);
        stringBuilder.append(SqlSchemaTag.NEW_LINE);
    }

    private void processProperty(StringBuilder stringBuilder, StringBuilder secondSb, Property property) {
        stringBuilder.append(SqlSchemaTag.NEW_LINE);
        stringBuilder.append(property.getName());
        stringBuilder.append(SqlSchemaTag.SPACE);
        stringBuilder.append(property.getType());

        if (property.isMandatory()) {
            stringBuilder.append(SqlSchemaTag.SPACE);
            stringBuilder.append(SqlSchemaTag.NOT_NULL);
        } else {
            stringBuilder.append(SqlSchemaTag.SPACE);
            stringBuilder.append(SqlSchemaTag.NULL);
        }

        if (property.isPrimaryKey()) {
            stringBuilder.append(SqlSchemaTag.SPACE);
            stringBuilder.append(SqlSchemaTag.AUTO_INCREMENT);
            stringBuilder.append(SqlSchemaTag.SPACE);
            stringBuilder.append(SqlSchemaTag.PRIMARY_KEY);
        }

        stringBuilder.append(SqlSchemaTag.COMMA);

        if (property.isForeignKey()) {
            Entity referencedEntity = getReferencedEntity(property);
            Property referencedProperty = getReferencedProperty(referencedEntity, property);

            secondSb.append(SqlSchemaTag.NEW_LINE);
            secondSb.append(SqlSchemaTag.FOREIGN_KEY);
            secondSb.append(SqlSchemaTag.BRACKET_OPENED);
            secondSb.append(property.getName());
            secondSb.append(SqlSchemaTag.BRACKET_CLOSED);
            secondSb.append(SqlSchemaTag.SPACE);
            secondSb.append(SqlSchemaTag.REFERENCES);
            secondSb.append(SqlSchemaTag.SPACE);
            secondSb.append(referencedEntity.getName());
            secondSb.append(SqlSchemaTag.BRACKET_OPENED);
            secondSb.append(referencedProperty.getName());
            secondSb.append(SqlSchemaTag.BRACKET_CLOSED);
            secondSb.append(SqlSchemaTag.COMMA);
        }
    }

    private Entity getReferencedEntity(Property property) {
        return entities.stream()
                .filter(e -> property.getName().contains(e.getName()))
                .findFirst()
                .orElseThrow();
    }

    private Property getReferencedProperty(Entity entity, Property property) {
        return entity.getProperties().stream()
                .filter(p -> property.getName().contains(p.getName()))
                .findFirst()
                .orElseThrow();
    }
}
