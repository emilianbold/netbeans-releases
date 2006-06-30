#!/bin/sh
#
# The contents of this file are subject to the terms of the Common Development
# and Distribution License (the License). You may not use this file except in
# compliance with the License.
#
# You can obtain a copy of the License at http://www.netbeans.org/cddl.html
# or http://www.netbeans.org/cddl.txt.
#
# When distributing Covered Code, include this CDDL Header Notice in each file
# and include the License file at http://www.netbeans.org/cddl.txt.
# If applicable, add the following below the CDDL Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
# Microsystems, Inc. All Rights Reserved.

#
# this script is a simple executor for db feeder of PES
#    - it handles running the code in an endless loop
#    - its basic behaviour is similar to other services
#          available on Unix systems
#    - you can use:
#          'xtest-dbfeeder.sh run' to tun the server
#          'xtest-dbfeeder.sh stop' to stop the server
#          'xtest-dbfeeder.sh kill' to kill the server
#


# These variables needs to be updated to correspond your values

# where is Publishing Engine Server installed
#DBFEEDER_HOME=${your_dbfeeder_home}



# where is PES' config stored
#DBFEEDER_CONFIG=${your_dbfeeder_configuration_file}



# set all the Java stuff
#JAVA_HOME=${your_jdk1.4_installation_path}


# where is JDBC driver jar 
#JDBC_DRIVER=${your_jdbc_driver_jar_archive}



#
# variables, which you can set, but don't have to
#


# sleep interval
SLEEP_INTERVAL=300



#
# internal script stuff - you should not touch anything after this line
#
dbfeeder_status_file=xtest-dbfeeder.run
dbfeeder_stop_file=xtest-dbfeeder.stop


dbfeeder_run() {

    if [ -r "$DBFEEDER_HOME/$dbfeeder_status_file" ]; then
	echo Another instance of XTest DbFeeder already running
	return
    fi

    # create the flag file with PID in DbFeeder home
    echo $$ >  $DBFEEDER_HOME/$dbfeeder_status_file

    timer=0
    check_interval=5

    while true
	do

	if [ "$timer" -eq 0 ]; then
		dbfeeder_cmd 'run'
	fi

	timer=`expr $timer + $check_interval`

	# check if DBFEEDER should be stopped
	if [ -r "$DBFEEDER_HOME/$dbfeeder_stop_file" ]; then
	    echo `date`: Stopping XTest DbFeeder
	    rm -f "$DBFEEDER_HOME/$dbfeeder_stop_file"
	    rm -f "$DBFEEDER_HOME/$dbfeeder_status_file"
	    return
	fi

 	if [ "$timer" -ge "$SLEEP_INTERVAL" ]; then
	    timer=0
	fi

	#echo Sleeping for $SLEEP_INTERVAL seconds
	sleep $check_interval
    done

}


# runs pes command
dbfeeder_cmd() {
        # create classpath
   	dbf_classpath=${JDBC_DRIVER}
   	for i in ${DBFEEDER_HOME}/lib/*.jar ; do
   		dbf_classpath=${dbf_classpath}:$i
   	done
	debug_parameters=""
#	debug_parameters="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8765"
   	cmd="$JAVA_HOME/bin/java -cp $dbf_classpath $debug_parameters -Dpes.dbfeeder.config=$DBFEEDER_CONFIG org.netbeans.xtest.pes.dbfeeder.DbFeeder"
#	echo $cmd
	eval $cmd
}


dbfeeder_stop() {
    if [ ! -r "$DBFEEDER_HOME/$dbfeeder_status_file" ]; then
	echo "XTest DbFeeder does not seem to run. Cannot stop"
	echo "You can try to $0 kill to kill the server"
	return
    fi
    echo stopping xtest DBFEEDER
    if [ -r "$DBFEEDER_HOME/$dbfeeder_stop_file" ]; then
	echo "Command $0 stop is already running"
	echo "If not, you can try to $0 kill to kill server"
	return
    fi
    
    # create the stop file and wait until run file is deleted
    touch "$DBFEEDER_HOME/$dbfeeder_stop_file"
    echo "Waiting until XTest DbFeeder finished it's work"
    while true; do
	if [ ! -r "$DBFEEDER_HOME/$dbfeeder_status_file" ]; then
	    echo "XTest DbFeeder stopped"
	    return
	fi
	# sleep for a second
	sleep 1
    done

}


dbfeeder_kill() {
    if [ -r "$DBFEEDER_HOME/$dbfeeder_status_file" ]; then
	pid_to_kill=` cat $DBFEEDER_HOME/$dbfeeder_status_file`
	echo "Killing XTest DbFeeder with PID $pid_to_kill"
	kill -9 $pid_to_kill
	rm -f "$DBFEEDER_HOME/$dbfeeder_status_file" 
	if [ -r "$DBFEEDER_HOME/$dbfeeder_stop_file" ]; then
	    rm -f "$DBFEEDER_HOME/$dbfeeder_stop_file"
	fi
	return
    fi

    if [ ! -r "$DBFEEDER_HOME/$dbfeeder_status_file" ]; then
	echo "XTest DbFeeder does not seem to run."
	echo "You have to kill xtest-pes manually"
    fi
}



if [ -z "$DBFEEDER_HOME" ]; then
    echo DBFEEDER_HOME needs to be set to XTest DbFeeder home
    exit 1
fi

if [ -z "$DBFEEDER_CONFIG" ]; then
    echo DBFEEDER_CONFIG needs to be set to DBFEEDER configuration file
    exit 1
fi

if [ -z "$JAVA_HOME" ]; then
    echo JAVA_HOME needs to be set to JDK 1.4 home
    exit 1
fi

if [ -z "$JDBC_DRIVER" ]; then
    echo JAVA_DRIVER needs to be set to your JDBC driver to be used for database connection
    exit 1
fi

cd $DBFEEDER_HOME


case "$1" in
'run')
	echo `date`: Running XTest DbFeeder
	dbfeeder_run
	;;

'start')
	echo `date`: Starting XTest DbFeeder
	dbfeeder_run &
	;;

'stop')
	echo `date`: Stopping XTest DbFeeder
	dbfeeder_stop
	;;


'kill') 
	echo `date`: Killing XTest DbFeeder
	dbfeeder_kill
	;;
	

*)
	echo "Usage: $0 { run | stop | kill }"
	exit 1
	;;


esac
exit 0
