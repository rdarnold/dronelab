@echo off
setlocal
if not defined JAVAFX_LIB (
    echo Set JAVAFX_LIB to the lib directory of a JavaFX 21 SDK. 1>&2
    exit /b 1
)
set "JAVAC=javac"
if defined JAVA_HOME set "JAVAC=%JAVA_HOME%\bin\javac.exe"
if not exist "bin\" mkdir bin
"%JAVAC%" --module-path "%JAVAFX_LIB%" --add-modules javafx.controls -cp "../ext/*" @options_win @classes
