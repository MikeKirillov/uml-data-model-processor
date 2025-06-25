# Project uml-data-model-processor

## 📖 Description

The uml-data-model-processor project provides automation tools for designing and implementing databases based
on [UML](https://en.wikipedia.org/wiki/Unified_Modeling_Language) diagrams created using
the [PlantUML](https://plantuml.com/) format. Its goal is to reduce routine work for developers by automatically
generating essential artifacts such as DDL scripts and POJO class templates.

## ❓ Why Is It Useful?

When developing applications, it's common to need to construct a data model through domain
analysis ([Information Engineering Diagram](https://plantuml.com/ie-diagram)). Developers manually create database
tables and POJO classes, which takes time and resources. The tool helps speed up this process by providing high-quality
base infrastructure for further system deployment.

## ⚙️ What Does This Tool Do?

1. Automatically generates SQL queries (using SQL Data Definition Language, DDL) for creating database tables based on
   given UML diagrams from PlantUML.
2. Creates initial Java POJO classes suitable for integration with standards like JDBC or JPA.

## 🚧 How Does It Work?

The application accepts PlantUML files (.pu, .puml, .txt) and converts them into ready-to-use SQL scripts and POJO
classes. You’ll need basic knowledge of UML syntax to prepare valid diagrams. We recommend validating your diagrams
beforehand using the
online [PlantUML Web Server](https://www.plantuml.com/plantuml/uml/bP7HIuKm6CNVzrTy-9I2KZr5Y2Xb954F8htAQfzhiCtOvaEa_tjVAH5nlN3tDFWTpiuvAttWBan6CxH1XG-895g1BWBkGS8d0pe9A07Nq3nraEV4qV60eYX-O9fk6iiDakgvQc_kg_RiuZnc2_krVTMAhj4fXfhxqFztlRoADpmeTuGR9OJ6NJnCMk_2TOB-JyQxXorS0kzleoNVoRoWEnpCeLG-K2KrsbvGCvBTLbtTDO_rzoJh5B3CMJRF85b9pz4FV040).

## 🥊 Advantages of the Project:

* Time and effort savings for development teams due to automated artifact generation.
* Reduced human errors related to manual preparation of queries and classes.
* Quick prototyping capabilities allowing rapid iteration over database structures without rewriting large amounts of
  code.

## 🩼 Current Limitations:

Since the project is at the MVP stage, currently implemented features include:

* Simple SQL statements for DDL creation resulting in a `schema.sql` file, compatible with frameworks like Spring Data
  JDBC.
* Varied POJO class configurations:
    * One-to-one mapping between UML models and POJO attributes (each attribute corresponds directly to a table column).
    * Representation of foreign keys as full object references (embedded entities) rather than simple identifiers.
    * Support for Spring Data JDBC-specific annotations (`@Table`, `@Id`, `@Column`) and types such
      as [AggregateReference](https://docs.spring.io/spring-data/jdbc/docs/current/api/org/springframework/data/jdbc/core/mapping/AggregateReference.html).
* Handling of one-to-one, one-to-many, many-to-many relationships (via join tables).

Future improvements aim to expand functionality, enhance generation quality, and integrate popular frameworks (
see [Supported Technologies](#-supported-technologies)).

## 🏁 Getting Started

Before starting, ensure you have:

* JDK version 8+ installed.
* Apache Maven available.

Interaction with the generator is done via a Maven plugin. For more details on configuring the plugin, refer to
the documentation page
for [uml-data-model-processor-maven-plugin](https://github.com/MikeKirillov/uml-data-model-processor/tree/develop/uml-data-model-processor-maven-plugin).

## 🧐 Usage Examples

For detailed examples of SQL and POJO class generation, see
the [practice-ice-box](https://github.com/MikeKirillov/practice-ice-box) project.

## 🧰 Supported Technologies

Currently supported technologies include:

* Databases: MySQL, ~~PostgreSQL~~, ~~SQLite~~.
* Programming languages: Java 11+, ~~Kotlin~~, ~~Scala~~, and ~~other JVM languages~~.
* Build systems: Maven, ~~Gradle~~.
* Frameworks: Spring Data JDBC, ~~Spring Data JPA~~, ~~Hibernate~~, ~~etc~~.
* Database migration management: Spring Data JDBC (automatically executes schema.sql), ~~Liquibase~~.

> 🥷 ~~strikethrough technologies~~ indicate planned future developments

## 🏆 Contributing

🙌 Your contributions are welcome!

🗣 To discuss ideas and suggestions, please submit issues
on [GitHub](https://github.com/MikeKirillov/uml-data-model-processor/issues) repository.

⭐ Please star the project if you find it useful!

🤝 Support open-source projects!