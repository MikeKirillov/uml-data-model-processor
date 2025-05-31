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

    public void writeConstructor(StringBuilder stringBuilder) {
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
        stringBuilder.append("\n\tpublic ").append(entityName).append("() {}\n");
    }

    private void writeIdConstructor(StringBuilder stringBuilder, String entityName) {
        entity.getProperties().stream()
                .filter(Property::isPrimaryKey)
                .findFirst()
                .ifPresent(primaryKey -> {
                    String name = primaryKey.getName();
                    String type = convertType(primaryKey.getType());
                    stringBuilder.append("\n\tpublic ").append(entityName).append("(").append(type).append(" ").append(name).append(") {\n");
                    stringBuilder.append("\t\tthis.").append(name).append(" = ").append(name).append(";\n");
                    stringBuilder.append("\t}\n");
                });
    }

    private void writeAllArgsConstructor(StringBuilder stringBuilder, String entityName) {
        StringBuilder constructorParameters = new StringBuilder();
        StringBuilder declaring = new StringBuilder();
        properties.forEach((name, type) -> {
            constructorParameters.append(type).append(" ").append(name).append(", ");
            declaring.append("\t\tthis.").append(name).append(" = ").append(name).append(";\n");
        });

        String typeNameString = constructorParameters.substring(0, constructorParameters.length() - 2);
        stringBuilder.append("\n\tpublic ").append(entityName).append("(").append(typeNameString).append(") {\n");
        stringBuilder.append(declaring);
        stringBuilder.append("\t}\n");
    }
}
