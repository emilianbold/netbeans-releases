#!/bin/sh

PROP_FILE=../conf/site-properties

if [ ! -r "${PROP_FILE}" ] ; then
   echo "Cannot find site-properties file ${PROP_FILE}".
fi

. ${PROP_FILE}

if [ -z "$XTEST_SERVER_HOME" -o ! -d "$XTEST_SERVER_HOME" ]; then
    echo XTEST_SERVER_HOME needs to be set. Edit ../conf/site-properties file.
    exit 1
fi



SLEEP_INTERVAL=900

xtest_status_file=xtest-server.run
xtest_stop_file=xtest-server.stop
xtest_output_file=xtest-server.out


xtest_server_start() {
    if [ -r "${XTEST_SERVER_HOME}/$xtest_status_file" ]; then
	echo Another instance of Xtest Server is already running
	return
    fi
    echo 'Starting Xtest Testing Server'
    xtest_server_run > ${XTEST_SERVER_HOME}/${xtest_output_file} &
    echo $! > ${XTEST_SERVER_HOME}/$xtest_status_file
}
    
xtest_server_run() {    
    # create the flag file with PID
    timer=0
    check_interval=10
    while true
    do
	if [ "$timer" -eq 0 ]; then
	    echo "Running tests. DON'T INTERRUPT."
	    cd ${XTEST_SERVER_HOME}/bin
	    sh testBuild.sh
    	    echo SLEEPING for $SLEEP_INTERVAL seconds. Now you can interrupt me.
	fi

	timer=`expr $timer + $check_interval`

	# check if xtest server should be stopped
	if [ -r "${XTEST_SERVER_HOME}/$xtest_stop_file" ] ; then
	    echo Stopping XTest PES
	    rm -f "${XTEST_SERVER_HOME}/$xtest_stop_file"
	    rm -f "${XTEST_SERVER_HOME}/$xtest_status_file"
	    return
	fi

 	if [ "$timer" -ge "$SLEEP_INTERVAL" ]; then
	    timer=0
	fi
	
	sleep $check_interval
    done
}

xtest_server_stop() {
    if [ ! -r "${XTEST_SERVER_HOME}/$xtest_status_file" ] ; then
	echo "Xtest Server does not seem to run. Cannot stop"
	echo "You can try to $0 kill to kill the server"
	return
    fi
    if [ -r "${XTEST_SERVER_HOME}/$xtest_stop_file" ] ; then
	echo "Command $0 stop is already running"
	echo "If not, you can try to $0 kill to kill server"
	return
    fi

    echo Stopping Xtest Testing Server. It can take long time.
    # create the stop file and wait until run file is deleted
    touch "${XTEST_SERVER_HOME}/$xtest_stop_file"
    echo "Check server status by '$0 status'"
    xtest_server_wait &
}
    
xtest_server_wait() {    
    while true; do
	if [ ! -r "${XTEST_SERVER_HOME}/$xtest_status_file" ] ; then
	    echo "Xtest Server stopped"
	    return
	fi
	# sleep for a 5 seconds
	sleep 5
    done
}

xtest_server_kill() {
    if [ -r "${XTEST_SERVER_HOME}/$xtest_status_file" ] ; then
	pid_to_kill=` cat ${XTEST_SERVER_HOME}/$xtest_status_file`
	echo "Killing Xtest Testing Server with PID $pid_to_kill"
	rm -f ${LOG_DIR}/test.running
	rm -f "${XTEST_SERVER_HOME}/$xtest_status_file" 
	if [ -r "${XTEST_SERVER_HOME}/$xtest_stop_file" ] ; then
	    rm -f "${XTEST_SERVER_HOME}/$xtest_stop_file"
	fi
        sh ${XTEST_SERVER_HOME}/bin/killtree.sh $pid_to_kill
        echo "Xtest Testing Server and all its subprocesses was killed."
    else
	echo "XTest PES does not seem to run."
	echo "You have to kill xtest-pes manually"
    fi
}

xtest_server_status() {
    if [ -r "${XTEST_SERVER_HOME}/$xtest_status_file" ] ; then
	if [ -r "${XTEST_SERVER_HOME}/$xtest_stop_file" ] ; then
            echo "Xtest Testing Server is stopping."
        else
            echo "Xtest Testing Server is running."
        fi
    else
	echo "Xtest Testing Server is not running."
    fi


}

case "$1" in
'start')
	xtest_server_start
	;;

'stop')
	xtest_server_stop
	;;

'kill') 
	xtest_server_kill
	;;

'status') 
	xtest_server_status
	;;

*)
	echo "Usage: $0 { start | stop | kill | status }"
	exit 1
	;;

esac
exit 0
