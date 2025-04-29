Этапы разработки:

1. Научиться парсить UML в некую абстракцию, из которого будет возможным:
    - генерировать скрипты под простейший формат, например schema.sql (**MVP**).
    - генерировать скрипты под другие форматы (уточню в ходе дальнейшего развития) (TODO).
    - генерировать POJO моделей (TODO).
2. Провести тестирование парсера.
3. Проанализировать и декомпозировать суть проекта на под-проекты:
    - вынести парсер в отдельную библиотеку.
    - вынести генератор скрипта в виде плагина отдельно.
    - вынести генератор POJO в виде плагина отдельно.
3. Выполнить тестирование плагинов.
4. Релиз базового функционала с минимальным набором возможностей (релиз MVP).
5. Обкатка, выявление критических багов, дефектов, задач на развитие, оформление их.
6. Решение проблем и задач первоначального MVP и подготовка релиза с исправлениями.
7. Релиз MVP 2.0.
8. Выявление дополнительных задач, плановое решение вместе с изначально определенными задачами.
9. Формирование релизного процесса, попытка выхода в [Maven Repository](https://mvnrepository.com/).


#### puml-data-model-to-sql-maven-plugin CLI call example:

* plugin and its generate goal: `com.github.mikekirillov:puml-data-model-to-sql-maven-plugin:generate`
* required field inputFilePath: `-Dgenerate.inputFilePath=/Users/michaelkirillov/...`
* required field outputFilePath: `-Dgenerate.outputFilePath=/Users/michaelkirillov/...`
* field outputFileName: `-Dgenerate.outputFileName=schema`
* field outputFileExtension: `-Dgenerate.outputFileExtension=sql`

full call:

`% mvn com.github.mikekirillov:puml-data-model-to-sql-maven-plugin:generate -Dgenerate.inputFilePath=/Users/michaelkirillov/IdeaProjects/puml-data-model-to-sql/puml-data-model-to-sql-core/src/test/resources/data-base-model.txt -Dgenerate.outputFilePath=/Users/michaelkirillov/IdeaProjects/puml-data-model-to-sql/puml-data-model-to-sql-core/src/main/resources/generated`