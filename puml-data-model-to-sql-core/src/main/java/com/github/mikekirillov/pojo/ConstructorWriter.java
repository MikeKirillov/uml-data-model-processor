package com.github.mikekirillov.pojo;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.PojoConfig;
import com.github.mikekirillov.model.Property;

import java.util.Map;

import static com.github.mikekirillov.utils.PojoProcessorUtils.camelize;
import static com.github.mikekirillov.utils.PojoProcessorUtils.convertType;

public class ConstructorWriter {
    private final PojoConfig pojoConfig;
    private final Entity entity;
    private final Map<String, String> properties;

    public ConstructorWriter(PojoConfig pojoConfig, Entity entity, Map<String, String> properties) {
        this.pojoConfig = pojoConfig;
        this.entity = entity;
        this.properties = properties;
    }

    public void writeConstructors(StringBuilder stringBuilder) {
        String entityName = camelize(entity.getName(), true);

        if (pojoConfig.isAllowNoArgsConstructor()) {
            writeNoArgsConstructor(stringBuilder, entityName);
        }
        if (pojoConfig.isAllowIdArgConstructor()) {
            writeIdConstructor(stringBuilder, entityName);
        }
        if (pojoConfig.isAllowAllArgsConstructor()) {
            writeAllArgsConstructor(stringBuilder, entityName);
        }
    }

    private void writeNoArgsConstructor(StringBuilder stringBuilder, String entityName) {
        stringBuilder.append("\n\tpublic ")
                .append(entityName)
                .append("() {}")
                .append("\n");
    }

    private void writeIdConstructor(StringBuilder stringBuilder, String entityName) {
        entity.getProperties().stream()
                .filter(Property::isPrimaryKey)
                .findFirst()
                .ifPresent(primaryKey -> {
                    String name = primaryKey.getName();
                    String type = convertType(primaryKey.getType());
                    stringBuilder.append("\n\tpublic ")
                            .append(entityName)
                            .append("(")
                            .append(type)
                            .append(" ")
                            .append(name)
                            .append(") {\n")
                            .append("\t\tthis.")
                            .append(name)
                            .append(" = ")
                            .append(name)
                            .append(";")
                            .append("\n\t}")
                            .append("\n");
                });
    }

    private void writeAllArgsConstructor(StringBuilder stringBuilder, String entityName) {
        StringBuilder parameters = new StringBuilder();
        StringBuilder body = new StringBuilder();

        properties.forEach((fieldName, fieldType) -> {
            parameters.append(fieldType)
                    .append(" ")
                    .append(fieldName)
                    .append(", ");
            body.append("\t\tthis.")
                    .append(fieldName)
                    .append(" = ")
                    .append(fieldName)
                    .append(";\n");
        });

        parameters.setLength(parameters.length() - 2);

        stringBuilder.append("\n\tpublic ")
                .append(entityName)
                .append("(")
                .append(parameters)
                .append(") {\n")
                .append(body)
                .append("\t}\n");
    }
}
