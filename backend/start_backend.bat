@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d d:\Java\MISForglxy\MISforglxyBackend
mvnw.cmd spring-boot:run
pause