@echo off

Rem ==================================
Rem Save the current PATH off so we can make 
Rem sure it a bad one doesn't effect the deploy
Rem ==================================

set SAVEPATH=%PATH%
set PATH=

for /f "usebackq tokens=* delims=\" %%a in ('%0') do (
  set basedir=%%~fsa
)

for /f "usebackq" %%b in ('%basedir%\..\..') do (
  set rave_base=%%~dpb
)

for /f "usebackq" %%b in ('%basedir%\..\..\..') do (
  set rave_root=%%~dpb
)

set EJB_DIR=%rave_base%\samples\ejb\applications
set STARTUP_DIR=%rave_base%startup\bin


IF NOT DEFINED RAVE_J2EE_HOME (
    set PE_HOME=%rave_root%\SunAppServer8
) ELSE (
    set PE_HOME=%RAVE_J2EE_HOME%
)

set RAVE_DOMAIN=creator
set USER=admin
set PASSWORD=adminadmin
set HOST=localhost

IF "%2"=="" (
    set ADMIN_PORT=24848
    set DB_PORT=29092
) ELSE (
    set ADMIN_PORT=%1
    set DB_PORT=%2
)

set CONFIGDATA=--user %USER% --password %PASSWORD% --host %HOST% --port %ADMIN_PORT%


Rem ==================================================================
Rem Create JDBC connetion pool and JDBC resource to Travel schema
Rem ==================================================================

set CONNECTION_POOL_ID=TravelDBPool
set TRAVE_DB_NAME=jdbc\:pointbase\:server\:\/\/localhost\:%DB_PORT%\/sample
set JDBC_RESOURCE_ID=jdbc/Travel
set CREATE_JDBC_CONNECTION_POOL=%PE_HOME%\bin\asadmin.bat create-jdbc-connection-pool %CONFIGDATA% --datasourceclassname com.pointbase.xa.xaDataSource --steadypoolsize 1 --maxpoolsize 8 --poolresize 1 --restype javax.sql.XADataSource --property User=travel:Password=travel:DatabaseName=%TRAVE_DB_NAME% %CONNECTION_POOL_ID%
set CREATE_JDBC_RESOURCE=%PE_HOME%\bin\asadmin.bat create-jdbc-resource %CONFIGDATA% --connectionpoolid %CONNECTION_POOL_ID%  %JDBC_RESOURCE_ID%

call %CREATE_JDBC_CONNECTION_POOL%
call %CREATE_JDBC_RESOURCE%

Rem ==================================================================
Rem Create JDBC connetion pool and JDBC resource to Jump Start Cycles (JSC) schema
Rem ==================================================================

set JSC_CONNECTION_POOL_ID=JSCDBPool
set JSC_DB_NAME=jdbc\:pointbase\:server\:\/\/localhost\:%DB_PORT%\/sample
set JSC_JDBC_RESOURCE_ID=jdbc/JSC
set CREATE_JSC_JDBC_CONNECTION_POOL=%PE_HOME%\bin\asadmin.bat create-jdbc-connection-pool %CONFIGDATA% --datasourceclassname com.pointbase.xa.xaDataSource --steadypoolsize 1 --maxpoolsize 8 --poolresize 1 --restype javax.sql.XADataSource --property User=jsc:Password=jsc:DatabaseName=%JSC_DB_NAME% %JSC_CONNECTION_POOL_ID%
set CREATE_JSC_JDBC_RESOURCE=%PE_HOME%\bin\asadmin.bat create-jdbc-resource %CONFIGDATA% --connectionpoolid %JSC_CONNECTION_POOL_ID%  %JSC_JDBC_RESOURCE_ID%

call %CREATE_JSC_JDBC_CONNECTION_POOL%
call %CREATE_JSC_JDBC_RESOURCE%

Rem =================================
Rem Deploy ejb applications
Rem ==================================

set PE_DEPLOY=%PE_HOME%\bin\asadmin.bat deploy %CONFIGDATA%

for /R %EJB_DIR% %%f in (*.ear) do call %PE_DEPLOY% %%f

Rem ==================================
Rem restore the path
Rem ==================================

set PATH=%SAVEPATH%

