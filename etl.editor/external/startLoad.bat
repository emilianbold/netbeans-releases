@echo off
cls
REM ********************************
REM *   ETL COMMAND LINE SETTINGS  *
REM ********************************
set JAVA_HOME="<e.g. D:\Software\jre1.5.0_11>"
set DATABASE_DRIVERS="<Full Path to Driver1>";"<Full Path to Driver2>";"<Full Path to DriverX>"

REM ****** DO NOT EDIT ********
set PATH=%JAVA_HOME%\bin;%PATH%
set LIB=.\lib
set INVOKER_JARS=%LIB%\org-netbeans-modules-etl-project-etlcli.jar;%LIB%\etlengine.jar;%LIB%\axiondb.jar
set CLASSPATH=.;%INVOKER_JARS%;%DATABASE_DRIVERS%
REM xxxxxxx DO NOT EDIT xxxxxxx

REM ****** Engine Invokers *********
