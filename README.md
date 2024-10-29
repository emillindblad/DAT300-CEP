## Scalable Complex Event Processing
This was a project in the course DAT300 at Chalmers University of Technology. The repository is kept as reference for future projects in the course.

## How to create and run a Flink app in Intellij

You need Java 11 installed.

1. Run this
    ```bash
    mvn archetype:generate \
        -DarchetypeGroupId=org.apache.flink \
        -DarchetypeArtifactId=flink-quickstart-java \
        -DarchetypeVersion=1.20.0
    ```

    groupId, artifactId, and package name can be anything (Java moment)
2. Add FlinkCEP inside the `<depencencies>` tag in `pom.xml`:
    ```
    <depencencies>
        ...
        <dependency>
            <groupId>org.apache.flink</groupId>
            <artifactId>flink-cep</artifactId>
            <version>1.20.0</version>
        </dependency>
        ...
    </depencencies>
    ```
3. In Intellij, select open -> navigate to the pom.xml file -> open as a project
4. Open the Maven tab to the right and click on `Download sources and/or Documentation` and then `Download sources`
5. Setup the sdk and choose java 11
6. Right click the `main` function and click on `Modify Run configuration`
7. Click on the blue `Modify options` text and make sure that `Add depencencies with "provided" scope to classpath` is ticked ✓
8. Run your app
