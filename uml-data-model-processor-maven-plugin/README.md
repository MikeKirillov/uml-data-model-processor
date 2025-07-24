# uml-data-model-processor-maven-plugin

## Configuration example

```xml

<build>
    <plugins>
        ...
        <plugin>
            <groupId>io.github.mikekirillov</groupId>
            <artifactId>uml-data-model-processor-maven-plugin</artifactId>
            <version>1.0.0</version>
            <configuration>
                <inputFilePath>${project.basedir}/db_schema.puml</inputFilePath>
                <generateDdlScript>true</generateDdlScript>
                <outputDdlScriptFilePath>${project.build.directory}/generated-sources/</outputDdlScriptFilePath>
                <outputDdlScriptFileName>schema</outputDdlScriptFileName>
                <outputDdlScriptFileExtension>sql</outputDdlScriptFileExtension>
                <generatePojo>true</generatePojo>
                <allowSpringDataJdbcAnnotations>true</allowSpringDataJdbcAnnotations>
                <allowForeignKeyAsEmbeddedEntity>true</allowForeignKeyAsEmbeddedEntity>
                <allowForeignKeyAsEmbeddedEntityByAggregate>true</allowForeignKeyAsEmbeddedEntityByAggregate>
                <allowNoArgsConstructor>true</allowNoArgsConstructor>
                <allowIdArgConstructor>true</allowIdArgConstructor>
                <allowAllArgsConstructor>true</allowAllArgsConstructor>
                <allowGetters>true</allowGetters>
                <allowSetters>true</allowSetters>
                <allowToStringMethod>true</allowToStringMethod>
                <outputPojoFilePath>${project.build.directory}/generated-sources/model/</outputPojoFilePath>
                <outputPojoPackageName>io.github.mikekirillov.model.jdbc</outputPojoPackageName>
            </configuration>
            <executions>
                <execution>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Parameters

| Parameter                                    | Description                                                                     | Required | Default value |
|----------------------------------------------|---------------------------------------------------------------------------------|----------|---------------|
| `inputFilePath`                              | Path to the input PlantUML file containing the UML diagram description          | true     |               |
| `generateDdlScript`                          | Flag indicating whether to generate the DDL script                              | false    | `false`       |
| `outputDdlScriptFilePath`                    | Output directory where the generated DDL script will be saved                   | false    |               |
| `outputDdlScriptFileName`                    | Base filename for the generated DDL script (without extension)                  | false    | `schema`      |
| `outputDdlScriptFileExtension`               | Extension used for the generated DDL script file                                | false    | `sql`         |
| `generatePojo`                               | Flag determining whether to create POJO classes                                 | false    | `false`       |
| `allowSpringDataJdbcAnnotations`             | Enables support for Spring Data JDBC annotations in generated POJO classes      | false    | `false`       |
| `allowForeignKeyAsEmbeddedEntity`            | Determines whether foreign keys should be represented as embedded entities      | false    | `false`       |
| `allowForeignKeyAsEmbeddedEntityByAggregate` | If enabled, aggregates foreign key entities into bridge entities when necessary | false    | `false`       |
| `allowNoArgsConstructor`                     | Allows generation of no-argument constructors in POJO classes                   | false    | `false`       |
| `allowIdArgConstructor`                      | Enables constructor with only an ID argument                                    | false    | `false`       |
| `allowAllArgsConstructor`                    | Generates constructors with all fields passed as arguments                      | false    | `false`       |
| `allowGetters`                               | Adds getter methods to the generated POJO classes                               | false    | `false`       |
| `allowSetters`                               | Includes setter methods in the generated POJO classes                           | false    | `false`       |
| `allowToStringMethod`                        | Adds a `toString()` method implementation to each POJO class                    | false    | `false`       |
| `outputPojoFilePath`                         | Directory where generated POJO classes are stored                               | false    |               |
| `outputPojoPackageName`                      | Package name for the generated POJO classes                                     | false    |               |

## Example of running plugin from Terminal

First, build the project:

`mvn clean install`

Then run the command to generate a DDL script from a PlantUML file:

```
mvn io.github.mikekirillov:uml-data-model-processor-maven-plugin:generate
-Dgenerate.inputFilePath=/Users/michaelkirillov/Downloads/db_schema.puml
-Dgenerate.generateDdlScript=true
-Dgenerate.outputDdlScriptFilePath=/Users/michaelkirillov/Downloads
-Dgenerate.outputDdlScriptFileName=ddl-script
-Dgenerate.outputDdlScriptFileExtension=txt
```
