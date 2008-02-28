REM ********* DO NOT EDIT [BEGIN] ***********
@echo on
cls
set JAVA_HOME=<e.g. D:\Software\jre1.5.0_11>
set PATH=%JAVA_HOME%\bin;%PATH%
set LIB=.\lib
set INVOKER_JARS=%LIB%\ETLEngineInvoker-1.0.jar;%LIB%\etl-engine-1.0.jar
set CP=.;%INVOKER_JARS%
REM ********* DO NOT EDIT [END] ***********

set JAVA_OPTS=