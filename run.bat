@echo off
setlocal
if not defined JAVAFX_LIB (
    echo Set JAVAFX_LIB to the lib directory of a JavaFX 21 SDK. 1>&2
    exit /b 1
)
set "JAVA=java"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\java.exe"
rem Allow Direct3D enough texture space for the 6500-pixel maps and 3D view.
"%JAVA%" --module-path "%JAVAFX_LIB%" --add-modules javafx.controls -Dprism.maxTextureSize=8192 -Dprism.maxvram=1G -Dfile.encoding=UTF8 -Xmx128G -cp "bin;../ext/*;." dronelab.DroneLab %*
