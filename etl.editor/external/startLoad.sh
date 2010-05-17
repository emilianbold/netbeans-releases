#!/bin/sh
clear
# ********************************
# *   ETL COMMAND LINE SETTINGS  *
# ********************************
JAVA_HOME="<set java path e.g. /usr/jdk/instances/jre1.5.0>"
DATABASE_DRIVERS="<Full Path to Driver1>":"<Full Path to Driver2>":"<Full Path to DriverX>"

# ****** DO NOT EDIT ********
PATH=$JAVA_HOME/bin:$PATH
CURRDIR=`pwd`
LIB=$CURRDIR/lib
INVOKER_JARS=$LIB/axiondb.jar:$LIB/org-netbeans-modules-etl-project-etlcli.jar:$LIB/etlengine.jar
CLASSPATH=$INVOKER_JARS:$DATABASE_DRIVERS
# xxxxxxx DO NOT EDIT xxxxxxx

# ****** Engine Invokers *********
