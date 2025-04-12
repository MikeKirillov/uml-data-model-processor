package com.github.mikekirillov.utils;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.Property;

import java.util.List;

public class TestUtils {
    public static final String RESOURCES_PATH = "src/test/resources/";
    public static final String TXT_FILE_PATH = "data-base-model.txt";
    public static final String PU_FILE_PATH = "data-base-model.pu";
    public static final String PUML_FILE_PATH = "data-base-model.puml";

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
                        new Property("id", "INT", true, true, false),
                        new Property("name", "VARCHAR(10)", true, false, false)
                )),
                new Entity("state", "st", List.of(
                        new Property("id", "INT", true, true, false),
                        new Property("name", "VARCHAR(128)", true, false, false)
                ))
        );
    }

    public static List<Entity> returnEntitiesWIthFk() {
        return List.of(
                new Entity("gender", "g", List.of(
                        new Property("id", "INT", true, true, false),
                        new Property("name", "VARCHAR(10)", true, false, false)
                )),
                new Entity("state", "st", List.of(
                        new Property("id", "INT", true, true, false),
                        new Property("name", "VARCHAR(128)", true, false, false),
                        new Property("gender_id", "VARCHAR(128)", true, false, true)
                ))
        );
    }

    public static List<Entity> returnEntitiesDamagedFkEntityName() {
        return List.of(
                new Entity("gender", "g", List.of(
                        new Property("id", "INT", true, true, false),
                        new Property("name", "VARCHAR(10)", true, false, false)
                )),
                new Entity("state", "st", List.of(
                        new Property("id", "INT", true, true, false),
                        new Property("name", "VARCHAR(128)", true, false, false),
                        new Property("random_id", "VARCHAR(128)", true, false, true)
                ))
        );
    }

    public static List<Entity> returnEntitiesDamagedFkEntityId() {
        return List.of(
                new Entity("gender", "g", List.of(
                        new Property("id", "INT", true, true, false),
                        new Property("name", "VARCHAR(10)", true, false, false)
                )),
                new Entity("state", "st", List.of(
                        new Property("id", "INT", true, true, false),
                        new Property("name", "VARCHAR(128)", true, false, false),
                        new Property("gender_xx", "VARCHAR(128)", true, false, true)
                ))
        );
    }
}
