package io.github.mikekirillov.pojo;

import io.github.mikekirillov.model.Entity;
import io.github.mikekirillov.model.PojoConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

import static io.github.mikekirillov.utils.ClassGeneratorUtils.camelize;

public class MethodWriter {
    private final PojoConfig pojoConfig;
    private final Entity entity;
    private final Map<String, String> properties;

    public MethodWriter(PojoConfig pojoConfig, Entity entity, Map<String, String> properties) {
        this.pojoConfig = pojoConfig;
        this.entity = entity;
        this.properties = properties;
    }

    public void writeMethods(StringBuilder stringBuilder) {
        if (pojoConfig.isAllowGetters() || pojoConfig.isAllowSetters()) {
            writeGettersSetters(stringBuilder, properties);
        }
        if (pojoConfig.isAllowToStringMethod()) {
            writeToStringMethod(stringBuilder, properties);
        }
    }

    private void writeGettersSetters(StringBuilder stringBuilder, Map<String, String> properties) {
        properties.forEach((name, type) -> {
            if (pojoConfig.isAllowGetters()) {
                writeGetMethod(stringBuilder, type, name);
            }
            if (pojoConfig.isAllowSetters()) {
                writeSetMethod(stringBuilder, type, name);
            }
        });
    }

    private void writeGetMethod(StringBuilder stringBuilder, String type, String name) {
        String methodName = StringUtils.capitalize(name);
        stringBuilder.append("\n\tpublic ")
                .append(type)
                .append(" ")
                .append("get")
                .append(methodName)
                .append("() {")
                .append("\n")
                .append("\t\treturn ")
                .append(name)
                .append(";")
                .append("\n")
                .append("\t}")
                .append("\n");
    }

    private void writeSetMethod(StringBuilder stringBuilder, String type, String name) {
        String methodName = StringUtils.capitalize(name);
        stringBuilder.append("\n\tpublic void ")
                .append("set")
                .append(methodName)
                .append("(")
                .append(type)
                .append(" ")
                .append(name)
                .append(") {")
                .append("\n")
                .append("\t\tthis.")
                .append(name)
                .append(" = ")
                .append(name)
                .append(";")
                .append("\n")
                .append("\t}")
                .append("\n");
    }

    private void writeToStringMethod(StringBuilder stringBuilder, Map<String, String> properties) {
        String entityName = camelize(entity.getName(), true);
        stringBuilder.append("\n\t@Override")
                .append("\n\tpublic String toString() {")
                .append("\n\t\treturn \"")
                .append(entityName)
                .append("{\" +");
        Optional<String> firstKey = properties.keySet().stream().findFirst();
        properties.keySet().forEach(name -> {
            if (name.equals(firstKey.get())) {
                stringBuilder.append("\n\t\t\t\"")
                        .append(name)
                        .append("='\" + ")
                        .append(name)
                        .append(" + '\\'' +");
            } else {
                stringBuilder.append("\n\t\t\t\", ")
                        .append(name)
                        .append("='\" + ")
                        .append(name)
                        .append(" + '\\'' +");
            }
        });
        stringBuilder.append("\n\t\t\t'}';")
                .append("\n\t}")
                .append("\n");
    }
}
