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
3. In Intellij, select open -> navigate to the pom.xml file -> open as a project.
4. Open the Maven tab to the right and click on `Generate Sources and Update Folders For All Projects`
5. Right click the `main` function and click on `Modify Run configuration`
6. Click on the blue `Modify options` text and make sure that `Add depencencies with "provided" scope to classpath` is ticked ✓.
7. Go to Project settings and choose Java 11 as the SDK.
8. Run your shitty app.
