package com.github.mikekirillov;

import com.github.mikekirillov.constants.SqlSchemaTag;
import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.Property;

import java.util.List;
import java.util.Objects;

public class SqlSchemaProcessor {

    public String generateSchema(List<Entity> entities) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Entity entity : entities) {
            stringBuilder.append(SqlSchemaTag.CREATE_TABLE_IF_NOT_EXISTS);
            stringBuilder.append(SqlSchemaTag.SPACE);
            stringBuilder.append(entity.getName());
            stringBuilder.append(SqlSchemaTag.BRACKET_OPENED);

            List<Property> properties = entity.getProperties();
            StringBuilder secondSb = null;

            for (Property property : properties) {
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
                    Entity referencedEntity = getReferencedEntity(entities, property);
                    Property referencedProperty = getReferencedProperty(referencedEntity, property);

                    if (Objects.isNull(secondSb)) {
                        secondSb = new StringBuilder();
                    }

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

            if (Objects.nonNull(secondSb)) {
                stringBuilder.append(secondSb);
                stringBuilder.append(SqlSchemaTag.NEW_LINE);
            } else {
                stringBuilder.append(SqlSchemaTag.NEW_LINE);
            }

            int indexOfNewLine = stringBuilder.lastIndexOf(SqlSchemaTag.NEW_LINE);
            stringBuilder.replace(indexOfNewLine - 1, indexOfNewLine, "");

            stringBuilder.append(SqlSchemaTag.BRACKET_CLOSED);
            stringBuilder.append(SqlSchemaTag.SEMICOLON);
            stringBuilder.append(SqlSchemaTag.NEW_LINE);
        }

        return stringBuilder.toString();
    }

    private Entity getReferencedEntity(List<Entity> entities, Property property) {
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
