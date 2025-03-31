package model;

import java.util.List;
import java.util.Objects;

public class Entity {
    private final String name;
    private final String alias;
    private final List<Property> properties;
    /*private final List<Relation> relations;*/

    public Entity(String name, String alias, List<Property> properties/*, List<Relation> relations*/) {
        Objects.requireNonNull(name, "name");

        this.name = name;
        this.alias = alias;
        this.properties = properties;
        /*this.relations = relations;*/
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "name=" + name +
                ", alias=" + alias +
                ", properties=" + properties +
                /*", relations=" + relations +*/
                '}';
    }
}
