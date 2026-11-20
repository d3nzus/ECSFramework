#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

mkdir -p out
find src examples test -name '*.java' -print > out/sources.txt

JAVAC=javac
JAVA=java
if [ -n "${JAVA_HOME:-}" ]; then
  JAVAC="$JAVA_HOME/bin/javac"
  JAVA="$JAVA_HOME/bin/java"
fi

"$JAVAC" -encoding UTF-8 -d out @out/sources.txt
"$JAVA" -ea -cp out dev.d3nzus.ecs.tests.TestRunner
