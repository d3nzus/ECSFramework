@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out
dir /s /b src\*.java examples\*.java test\*.java > out\sources.txt

if defined JAVA_HOME (
  "%JAVA_HOME%\bin\javac" -encoding UTF-8 -d out @out\sources.txt || exit /b 1
  "%JAVA_HOME%\bin\java" -ea -cp out dev.d3nzus.ecs.tests.TestRunner
) else (
  javac -encoding UTF-8 -d out @out\sources.txt || exit /b 1
  java -ea -cp out dev.d3nzus.ecs.tests.TestRunner
)
