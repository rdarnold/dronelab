#!/bin/sh
set -e
: "${JAVAFX_LIB:?Set JAVAFX_LIB to the lib directory of a JavaFX 21 SDK.}"
mkdir -p bin
"${JAVA_HOME:+${JAVA_HOME}/bin/}javac" --module-path "$JAVAFX_LIB" --add-modules javafx.controls -cp "../ext/*" @options_nix @classes
