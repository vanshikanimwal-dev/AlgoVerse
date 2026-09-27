@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12"
cd /d "%~dp0"
call mvnw.cmd javafx:run
