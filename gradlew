#!/bin/sh

##############################################################################
##
##  Gradle start up script for POSIX
##
##############################################################################

APP_HOME=$( cd "${APP_HOME:-./}" > /dev/null && pwd -P ) || exit
APP_NAME="Gradle"
APP_BASE_NAME=${0##*/}
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ ! -f "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "Downloading Gradle wrapper..."
    WRAPPER_URL="https://services.gradle.org/distributions/gradle-7.6.3-bin.zip"
    if command -v curl > /dev/null 2>&1; then
        curl -sL -o "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
            "https://raw.githubusercontent.com/gradle/gradle/v7.6.3/gradle/wrapper/gradle-wrapper.jar"
    elif command -v wget > /dev/null 2>&1; then
        wget -q -O "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
            "https://raw.githubusercontent.com/gradle/gradle/v7.6.3/gradle/wrapper/gradle-wrapper.jar"
    fi
fi

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

exec "$JAVACMD" \
    $DEFAULT_JVM_OPTS \
    $JAVA_OPTS \
    $GRADLE_OPTS \
    "-Dorg.gradle.appname=$APP_BASE_NAME" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
