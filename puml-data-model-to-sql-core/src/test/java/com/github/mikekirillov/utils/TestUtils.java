package com.github.mikekirillov.utils;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.Property;

import java.util.List;

public class TestUtils {
    public static final String RESOURCES_PATH_IN = "src/test/resources/";
    public static final String TXT_FILE_PATH_IN = "data-base-model.txt";
    public static final String PU_FILE_PATH_IN = "data-base-model.pu";
    public static final String PUML_FILE_PATH_IN = "data-base-model.puml";
    public static final String RESOURCES_PATH_OUT = "src/test/resources/generated/";
    public static final String TXT_FILE_PATH_OUT = "schema.sql";
    public static final String POJO_GENERATOR_DIR = "src/test/resources/model/";

    public static List<String> returnUmlLines() {
        return List.of(
                "@startuml",
                "entity \"gender\" AS g {",
                "* id : INT <<generated>>",
                "--",
                "* name    : VARCHAR(10)",
                "}",
                "",
                "class \"state\" as st{",
                "* id : INT <<generated>>",
                "--",
                "* name : VARCHAR(128)",
                "}",
                "@enduml"
        );
    }

    public static List<String> returnUmlLinesWithFk() {
        return List.of(
                "@startuml",
                "entity \"gender\" AS g {",
                "* id : INT <<generated>>",
                "--",
                "* name    : VARCHAR(10)",
                "}",
                "",
                "class \"state\" as st{",
                "* id : INT <<generated>>",
                "--",
                "* name : VARCHAR(128)",
                "gender_id : INT",
                "}",
                "st   }|..|| g",
                "@enduml"
        );
    }

    public static List<Entity> returnEntities() {
        return List.of(
                new Entity("gender", "g", List.of(
                        getProperty("id", "INT", true, true, false),
                        getProperty("name", "VARCHAR(10)", true, false, false)
                )),
                new Entity("state", "st", List.of(
                        getProperty("id", "INT", true, true, false),
                        getProperty("name", "VARCHAR(128)", true, false, false)
                ))
        );
    }

    public static List<Entity> returnEntitiesWithFk() {
        return List.of(
                new Entity("gender", "g", List.of(
                        getProperty("id", "INT", true, true, false),
                        getProperty("name", "VARCHAR(10)", true, false, false)
                )),
                new Entity("state", "st", List.of(
                        getProperty("id", "INT", true, true, false),
                        getProperty("name", "VARCHAR(128)", true, false, false),
                        getProperty("gender_id", "INT", true, false, true)
                ))
        );
    }

    public static List<Entity> returnEntitiesDamagedFkEntityName() {
        return List.of(
                new Entity("gender", "g", List.of(
                        getProperty("id", "INT", true, true, false),
                        getProperty("name", "VARCHAR(10)", true, false, false)
                )),
                new Entity("state", "st", List.of(
                        getProperty("id", "INT", true, true, false),
                        getProperty("name", "VARCHAR(128)", true, false, false),
                        getProperty("random_id", "VARCHAR(128)", true, false, true)
                ))
        );
    }

    public static List<Entity> returnEntitiesDamagedFkEntityId() {
        return List.of(
                new Entity("gender", "g", List.of(
                        getProperty("id", "INT", true, true, false),
                        getProperty("name", "VARCHAR(10)", true, false, false)
                )),
                new Entity("state", "st", List.of(
                        getProperty("id", "INT", true, true, false),
                        getProperty("name", "VARCHAR(128)", true, false, false),
                        getProperty("gender_xx", "VARCHAR(128)", true, false, true)
                ))
        );
    }

    public static List<Entity> returnEntitiesWithFkSnake() {
        return List.of(
                new Entity("gender_es", "g", List.of(
                        getProperty("id", "INT", true, true, false),
                        getProperty("name", "VARCHAR(10)", true, false, false)
                )),
                new Entity("state", "st", List.of(
                        getProperty("id", "INT", true, true, false),
                        getProperty("name", "VARCHAR(128)", true, false, false),
                        getProperty("gender_es_id", "INT", true, false, true)
                )),
                new Entity("coach", "st", List.of(
                        getProperty("id", "INT", true, true, false),
                        getProperty("name", "VARCHAR(128)", true, false, false),
                        getProperty("state_id", "INT", true, false, true)
                ))
        );
    }

    public static String returnSqlSchema() {
        return "CREATE TABLE IF NOT EXISTS gender(\n" +
                "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                "name VARCHAR(10) NOT NULL\n" +
                ");";
    }

    private static Property getProperty(String name, String type, boolean isMandatory, boolean isGenerated, boolean isForeignKey) {
        return new Property.Builder()
                .name(name)
                .type(type)
                .isMandatory(isMandatory)
                .isGenerated(isGenerated)
                .isForeignKey(isForeignKey)
                .build();
    }
}
