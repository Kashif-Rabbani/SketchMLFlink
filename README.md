## SketchML - Flink 

This is a BDAPRO project

# Some important Notes

- Spark (ML) and Flink has incopatibility issues with scala implementations.
- SketchML and Flink can use Scala 2.11.12
- We changed the ml and sketchml pom files of sketchml project for compatibility.
- Altered sketchml project is imported a jar by using IntelliJ "Build Artifact" option to have all dependencies in one file.
- SketchML jar is added to the SketchmlFlink project as dependency manually.
- Flink uses Java 1.8. To execute mvn commands, make sure your OS uses Java 1.8 (This resolves maven net.alchim31 issues)
- Execute `mvn install` on the source folder for installing dependencies.
- Execute `mvn clean package` on the source folder to package jar.
- Execute `java -jar target/sketchmlFlink-1.0-SNAPSHOT.jar --inputPath PATH --outputPath PATH` to run Test.scala.
- If the main class path is changed, change \<mainClass> entry of pom.xml too.
- When the jar is going to be deployed into cluster, flink dependencies can be changed from `compile` to `provided`

