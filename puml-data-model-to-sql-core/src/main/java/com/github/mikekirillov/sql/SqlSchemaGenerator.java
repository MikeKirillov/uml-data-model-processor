package com.github.mikekirillov.sql;

import com.github.mikekirillov.EntityProcessor;
import com.github.mikekirillov.constants.SqlSchemaTag;
import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.Property;

import java.util.List;

public class SqlSchemaGenerator implements EntityProcessor {
    private final List<Entity> entities;

    public SqlSchemaGenerator(List<Entity> entities) {
        this.entities = entities;
    }

    @Override
    public String generate() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Entity entity : entities) {
            createTableDefinition(stringBuilder, entity);
        }
        return stringBuilder.toString();
    }

    private void createTableDefinition(StringBuilder stringBuilder, Entity entity) {
        stringBuilder.append(SqlSchemaTag.CREATE_TABLE_IF_NOT_EXISTS)
                .append(' ')
                .append(entity.getName());

        stringBuilder.append(SqlSchemaTag.BRACKET_OPENED);

        List<Property> properties = entity.getProperties();
        for (Property property : properties) {
            createColumnDefinition(stringBuilder, property);
        }

        removeLastComma(stringBuilder);

        stringBuilder.append(SqlSchemaTag.NEW_LINE)
                .append(SqlSchemaTag.BRACKET_CLOSED)
                .append(SqlSchemaTag.SEMICOLON)
                .append(SqlSchemaTag.NEW_LINE);
    }

    private void createColumnDefinition(StringBuilder stringBuilder, Property property) {
        stringBuilder.append(SqlSchemaTag.NEW_LINE)
                .append(property.getName())
                .append(' ')
                .append(property.getType());

        // NOT NULL / NULL
        if (property.isMandatory()) {
            stringBuilder.append(' ').append(SqlSchemaTag.NOT_NULL);
        } else {
            stringBuilder.append(' ').append(SqlSchemaTag.NULL);
        }

        // PRIMARY KEY
        if (property.isPrimaryKey()) {
            stringBuilder.append(' ')
                    .append(SqlSchemaTag.AUTO_INCREMENT)
                    .append(' ')
                    .append(SqlSchemaTag.PRIMARY_KEY);
        }

        // FOREIGN KEY
        if (property.isForeignKey()) {
            stringBuilder.append(SqlSchemaTag.COMMA)
                    .append(SqlSchemaTag.NEW_LINE)
                    .append(SqlSchemaTag.FOREIGN_KEY)
                    .append(SqlSchemaTag.BRACKET_OPENED)
                    .append(property.getName())
                    .append(SqlSchemaTag.BRACKET_CLOSED)
                    .append(' ')
                    .append(SqlSchemaTag.REFERENCES)
                    .append(' ')
                    .append(findReferencedEntity(property).getName())
                    .append(SqlSchemaTag.BRACKET_OPENED)
                    .append(findReferencedProperty(property).getName())
                    .append(SqlSchemaTag.BRACKET_CLOSED);
        }

        stringBuilder.append(SqlSchemaTag.COMMA);
    }

    private Entity findReferencedEntity(Property property) {
        return entities.stream()
                .filter(e -> property.getName().contains(e.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Referenced entity not found"));
    }

    private Property findReferencedProperty(Property property) {
        Entity entity = findReferencedEntity(property);
        return entity.getProperties().stream()
                .filter(p -> property.getName().contains(p.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Referenced property not found"));
    }

    private void removeLastComma(StringBuilder stringBuilder) {
        int lastCommaIndex = stringBuilder.lastIndexOf(String.valueOf(SqlSchemaTag.COMMA));

        if (lastCommaIndex != -1) {
            stringBuilder.deleteCharAt(lastCommaIndex);
        }
    }
}
