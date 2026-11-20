# DroneLab

Build with JDK 21 and the JavaFX 21 SDK for your platform. Keep the Apache POI and JSON-P jars in the sibling `ext` directory, as expected by the build scripts. Use the `dronelab` directory as your working directory so the launchers can find scenarios and other resources.

On Windows, use Command Prompt:

```bat
set "JAVA_HOME=C:\path\to\jdk-21"
set "JAVAFX_LIB=C:\path\to\javafx-sdk-21\lib"
make.bat
run.bat
```

Run `procmon.bat` in a separate prompt with the same settings if you use the process monitor.

On Linux or macOS:

```sh
export JAVA_HOME=/path/to/jdk-21
export JAVAFX_LIB=/path/to/javafx-sdk-21/lib
sh make
sh run
```

If `JAVA_HOME` is unset, the scripts use the Java tools on `PATH`.
