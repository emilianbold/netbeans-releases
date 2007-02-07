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
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.

#
# this script is a simple executor for Publishing Engine Server
#    - it handles running the code in an endless loop
#    - its basic behaviour is similar to other services
#          available on Unix systems
#    - you can use:
#          'xtest-pes.sh run' to tun the server
#          'xtest-pes.sh stop' to stop the server
#          'xtest-pes.sh kill' to kill the server
#          'xtest-pes.sh version' to get the version
#

#
# variables needed to be set before running PES
#


# where is Publishing Engine Server installed
# PES_HOME=${your_pes_home}


# where is PES' configuration file stored. For more information about the file please
# look at the http://xtest.netbeans.org/XTest_publishing_server.html
# PES_CONFIG=${your_pes_configuration_file}


# set the JAVA_HOME to your JDK 1.4 or greater installation path
# JAVA_HOME=${your_jdk_installation_path}



#
# variables, which you can set, but don't have to
#

# sleep interval between two consequent checks of incoming results (in seconds)
SLEEP_INTERVAL=300



#
# internal script stuff - you should not touch anything after this line
#
pes_status_file=xtest-pes.run
pes_stop_file=xtest-pes.stop


pes_server_run() {

    if [ -r "$PES_HOME/$pes_status_file" ]; then
	echo Another instance of XTest PES already running
	return
    fi

    # create the flag file with PID in PES home
    echo $$ >  $PES_HOME/$pes_status_file

    timer=0
    check_interval=5

    while true
	do

	if [ "$timer" -eq 0 ]; then
		pes_server_cmd 'run'
	fi

	timer=`expr $timer + $check_interval`

	# check if PES should be stopped
	if [ -r "$PES_HOME/$pes_stop_file" ]; then
	    echo `date`: Stopping XTest PES
	    rm -f "$PES_HOME/$pes_stop_file"
	    rm -f "$PES_HOME/$pes_status_file"
	    return
	fi

 	if [ "$timer" -ge "$SLEEP_INTERVAL" ]; then
	    timer=0
	fi

	#echo Sleeping for $SLEEP_INTERVAL seconds
	sleep $check_interval
    done

}

pes_server_reconfigure() {

    if [ -r "$PES_HOME/$pes_status_file" ]; then
	echo Another instance of XTest PES already running
	return
    fi

    # create the flag file with PID in PES home
    echo $$ >  $PES_HOME/$pes_status_file
	
    # run pes
    pes_server_cmd 'reconfigure'
	
    # delete the status file	
    if [ -r "$PES_HOME/$pes_status_file" ]; then
        rm -f "$PES_HOME/$pes_status_file"
    fi	
}

# runs pes command
pes_server_cmd() {
	pes_cmd=${1:-run}	
	# create PES' classpath
   	pes_classpath=""
   	for i in ${PES_HOME}/lib/*.jar ; do
                if [ -z "${pes_classpath}" ]; then
           		pes_classpath=$i
                else 
                        pes_classpath=${pes_classpath}';'$i
                fi
   	done
	debug_parameters=""
#	debug_parameters="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8765"
   	cmd="$JAVA_HOME/bin/java -cp '$pes_classpath' $debug_parameters -Dxtest.home=$PES_HOME -Dpes.config=$PES_CONFIG -Dpes.command=$pes_cmd org.netbeans.xtest.pes.PEServer"
#	echo running : $cmd
#	echo `date`: Running PES
	eval $cmd
#	echo `date`: PES Done
}


pes_server_stop() {
    if [ ! -r "$PES_HOME/$pes_status_file" ]; then
	echo "XTest PES does not seem to run. Cannot stop"
	echo "You can try to $0 kill to kill the server"
	return
    fi
    echo stopping xtest PES
    if [ -r "$PES_HOME/$pes_stop_file" ]; then
	echo "Command $0 stop is already running"
	echo "If not, you can try to $0 kill to kill server"
	return
    fi
    
    # create the stop file and wait until run file is deleted
    touch "$PES_HOME/$pes_stop_file"
    echo "Waiting until XTest PES finished it's work"
    while true; do
	if [ ! -r "$PES_HOME/$pes_status_file" ]; then
	    echo "XTest PES stopped"
	    return
	fi
	# sleep for a second
	sleep 1
    done

}


pes_server_kill() {
    if [ -r "$PES_HOME/$pes_status_file" ]; then
	pid_to_kill=` cat $PES_HOME/$pes_status_file`
	echo "Killing xtest-pes with PID $pid_to_kill"
	kill -9 $pid_to_kill
	rm -f "$PES_HOME/$pes_status_file" 
	if [ -r "$PES_HOME/$pes_stop_file" ]; then
	    rm -f "$PES_HOME/$pes_stop_file"
	fi
	return
    fi

    if [ ! -r "$PES_HOME/$pes_status_file" ]; then
	echo "XTest PES does not seem to run."
	echo "You have to kill xtest-pes manually"
    fi
}



if [ -z "$PES_HOME" ]; then
    echo PES_HOME needs to be set to XTest PES home
    exit 1
fi

if [ -z "$PES_CONFIG" ]; then
    echo PES_CONFIG needs to be set to PES configuration file
    exit 1
fi

if [ -z "$JAVA_HOME" ]; then
    echo JAVA_HOME needs to be set to JDK 1.4 or later home
    exit 1
fi

cd $PES_HOME


case "$1" in
'run')
	echo `date`: Running XTest Publishing Engine Server
	pes_server_run
	;;

'start')
	echo `date`: Starting XTest Publishing Engine Server
	pes_server_run &
	;;

'stop')
	echo `date`: Stopping XTest Publishing Engine Server
	pes_server_stop
	;;


'kill') 
	echo `date`: Killing XTest Publishing Engine Server
	pes_server_kill
	;;
	
'reconfigure')
	echo `date`: Reconfiguring XTest Publishing Engine Server
	pes_server_reconfigure
	;;

'version')	
	pes_server_cmd version
	;;

*)
	echo "Usage: $0 { run | stop | kill | reconfigure | version}"
	exit 1
	;;


esac
exit 0
