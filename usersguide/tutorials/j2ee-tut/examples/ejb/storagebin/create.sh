#!/bin/bash

java -classpath $J2EE_HOME/pointbase/lib/pbtools.jar:$J2EE_HOME/pointbase/lib/pbclient.jar com.pointbase.tools.toolsCommander com.pointbase.jdbc.jdbcUniversalDriver jdbc:pointbase:server://localhost/sun-appserv-samples create.sql pbpublic pbpublic
