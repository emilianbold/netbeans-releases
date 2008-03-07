REM ********* DO NOT EDIT [BEGIN] ***********
@echo off
cls
set JAVA_HOME=<e.g. D:\Software\jre1.5.0_11>
set PATH=%JAVA_HOME%\bin;%PATH%
set LIB=.\lib
set INVOKER_JARS=%LIB%\ETLEngineInvoker-1.0.jar;%LIB%\etlengine.jar
set CP=.;%INVOKER_JARS%
REM ********* DO NOT EDIT [END] ***********

set JAVA_OPTS=