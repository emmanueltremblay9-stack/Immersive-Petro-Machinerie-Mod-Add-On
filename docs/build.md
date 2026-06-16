# Build

Immersive Petro-Machinery targets:

- Minecraft `1.21.1`
- NeoForge `21.1.164`
- Java `21`

## Java 21 Policy

The Gradle build is configured to compile with Java 21:

- Java toolchain language version: `21`
- Source compatibility: `21`
- Target compatibility: `21`
- Java compiler release: `21`

Gradle auto-detects installed JDKs and does not auto-download toolchains.

## Windows PowerShell

Normal build command:

```powershell
.\gradlew.bat build
```

If system `java` is not Java 21, the Gradle toolchain can still compile with an
installed Java 21 JDK as long as Gradle can start.

If `JAVA_HOME` is set incorrectly, Gradle fails before reading the project
toolchain. `JAVA_HOME` must be either unset or set to a JDK root.

Correct:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\graalvm-jdk-21.0.4+8.1"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat build
```

Incorrect:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot\bin"
```

The incorrect form points at a `bin` directory, not a JDK root, and it is Java
17 instead of Java 21.

## Current Local Observation

On this machine, plain `java -version` currently reports Java 17 from PATH. The
project still builds when `JAVA_HOME` is unset because Gradle starts on Java 17
and the Java 21 toolchain is auto-detected for compilation.

If Gradle cannot find a Java 21 installation automatically on another machine,
install a Java 21 JDK and set `JAVA_HOME` to that JDK root before building.
