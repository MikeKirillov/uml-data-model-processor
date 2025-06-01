package com.github.mikekirillov.pojo;

import com.github.mikekirillov.model.Entity;
import com.github.mikekirillov.model.PojoConfig;
import com.github.mikekirillov.model.Property;
import com.github.mikekirillov.model.Relation;

import java.util.List;

import static com.github.mikekirillov.utils.ClassGeneratorUtils.findCurrentEntityAsMainRelation;

public class ImportWriter {
    private final PojoConfig pojoConfig;
    private final Entity entity;
    private final List<Relation> relations;

    public ImportWriter(PojoConfig pojoConfig, Entity entity, List<Relation> relations) {
        this.pojoConfig = pojoConfig;
        this.entity = entity;
        this.relations = relations;
    }

    public void writeImports(StringBuilder stringBuilder) {
        if (pojoConfig.isAllowSpringDataJdbcAnnotations()) {
            stringBuilder.append("import org.springframework.data.annotation.Id;\n")
                    .append("import org.springframework.data.relational.core.mapping.Table;\n");
            if (pojoConfig.isAllowForeignKeyAsEmbeddedEntity() && hasForeignKey()) {
                if (pojoConfig.isAllowForeignKeyAsEmbeddedEntityByAggregate()) {
                    stringBuilder.append("import org.springframework.data.relational.core.mapping.Column;\n")
                            .append("import org.springframework.data.jdbc.core.mapping.AggregateReference;\n");

                    // add these imports for main entity for many-to-many cases
                    findCurrentEntityAsMainRelation(relations, entity).ifPresent(relation -> {
                        stringBuilder.append("import org.springframework.data.relational.core.mapping.MappedCollection;\n")
                                .append("import java.util.HashSet;\n")
                                .append("import java.util.Set;\n");
                    });
                } else {
                    stringBuilder.append("import org.springframework.data.relational.core.mapping.MappedCollection;\n");
                }
            }
        }

        if (hasDateTime()) {
            stringBuilder.append("import java.sql.Date;\n");
        }
        if (hasTimeStamp()) {
            stringBuilder.append("import java.util.Date;\n");
        }
        // TODO REMEMBER that scheme could contain both of date types
    }

    private boolean hasForeignKey() {
        return entity.getProperties().stream().anyMatch(Property::isForeignKey);
    }

    private boolean hasDateTime() {
        return entity.getProperties().stream().anyMatch(property -> property.getType().equals("DATETIME"));
    }

    private boolean hasTimeStamp() {
        return entity.getProperties().stream().anyMatch(property -> property.getType().equals("TIMESTAMP"));
    }
}
