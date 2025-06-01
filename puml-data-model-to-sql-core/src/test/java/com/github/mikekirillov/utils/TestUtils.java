package com.github.mikekirillov.utils;

import com.github.mikekirillov.enums.UmlRelationType;
import com.github.mikekirillov.model.*;

import java.util.List;

import static org.mockito.BDDMockito.given;

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

    public static Property getProperty(String name, String type, boolean isMandatory, boolean isGenerated, boolean isForeignKey) {
        return new Property.Builder()
                .name(name)
                .type(type)
                .isMandatory(isMandatory)
                .isGenerated(isGenerated)
                .isForeignKey(isForeignKey)
                .build();
    }

    public static Relation returnUnfitRelation() {
        return new Relation(
                new EntityRelation(
                        new Entity("disc", null, List.of(
                                getProperty("id", "INT", true, true, false),
                                getProperty("name", "VARCHAR(128)", true, false, false)
                        )),
                        UmlRelationType.ONE_OR_MANY
                ),
                new EntityRelation(
                        new Entity("location", null, List.of(
                                getProperty("id", "INT", true, true, false),
                                getProperty("name", "VARCHAR(128)", true, false, false)
                        )),
                        UmlRelationType.EXACTLY_ONE
                )
        );
    }

    public static Relation returnFitRelation(Entity entity) {
        return new Relation(
                new EntityRelation(
                        entity,
                        UmlRelationType.ONE_OR_MANY
                ),
                new EntityRelation(
                        new Entity("location", null, List.of(
                                getProperty("id", "INT", true, true, false),
                                getProperty("name", "VARCHAR(128)", true, false, false)
                        )),
                        UmlRelationType.EXACTLY_ONE
                )
        );
    }

    public static List<Relation> returnFitRelationForManyToMany(Entity entity) {
        Entity bridge = new Entity(entity.getName() + "_location", null, List.of(
                getProperty("id", "INT", true, true, false),
                getProperty(entity.getName() + "_id", "VARCHAR(128)", true, false, false),
                getProperty("location_id", "VARCHAR(128)", true, false, false)
        ));
        return List.of(
                new Relation(
                        new EntityRelation(entity, UmlRelationType.ONE_OR_MANY),
                        new EntityRelation(bridge, UmlRelationType.EXACTLY_ONE)
                ),
                new Relation(
                        new EntityRelation(
                                new Entity("location", null, List.of(
                                        getProperty("id", "INT", true, true, false),
                                        getProperty("name", "VARCHAR(128)", true, false, false)
                                )),
                                UmlRelationType.ONE_OR_MANY
                        ),
                        new EntityRelation(bridge, UmlRelationType.EXACTLY_ONE)
                )
        );
    }

    public static void setPojoConfig(PojoConfig pojoConfig, boolean allowSpringDataJdbcAnnotations, boolean allowForeignKeyAsEmbeddedEntity, boolean allowForeignKeyAsEmbeddedEntityByAggregate) {
        given(pojoConfig.isAllowSpringDataJdbcAnnotations())
                .willReturn(allowSpringDataJdbcAnnotations);
        given(pojoConfig.isAllowForeignKeyAsEmbeddedEntity())
                .willReturn(allowForeignKeyAsEmbeddedEntity);
        given(pojoConfig.isAllowForeignKeyAsEmbeddedEntityByAggregate())
                .willReturn(allowForeignKeyAsEmbeddedEntityByAggregate);
    }
}
