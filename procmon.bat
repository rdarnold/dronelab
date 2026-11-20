@echo off
setlocal
if not defined JAVAFX_LIB (
    echo Set JAVAFX_LIB to the lib directory of a JavaFX 21 SDK. 1>&2
    exit /b 1
)
set "JAVA=java"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\java.exe"
"%JAVA%" --module-path "%JAVAFX_LIB%" --add-modules javafx.controls -Dfile.encoding=UTF8 -Xmx100M -cp "bin;../ext/*;." dronelab.procmon.DLProcMon %*
