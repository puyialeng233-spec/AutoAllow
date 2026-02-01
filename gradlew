#!/usr/bin/env sh
DIRNAME="$(dirname "$0")"
APP_BASE_NAME="$(basename "$0")"
exec java -classpath "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
