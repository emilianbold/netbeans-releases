#!/bin/sh
echo $$ > ${LOG_DIR}/timeout.pid

if [ "$WARNING_TIME" = "" ]; then
     WARNING_TIME=`expr 10 '*' 3600`
fi	

time_in_hours=`expr $WARNING_TIME / 3600`
time_in_minutes=`expr $WARNING_TIME / 60 % 60` 
time_in_seconds=`expr $WARNING_TIME % 60` 

mesg=
if [ "$time_in_hours" -gt 0 ] ;  then
   mesg="$time_in_hours hour(s) "
fi
if [ "$time_in_minutes" -gt 0 ] ;  then
   mesg="${mesg}${time_in_minutes} minute(s) "
fi
if [ "$time_in_minutes" -gt 0 ] ;  then
   mesg="${mesg}${time_in_seconds} second(s) "
fi


timer_int=60
timer=0

while [ "$timer" -lt "$WARNING_TIME" -a -f "${LOG_DIR}/test.running" ] ; do
  sleep $timer_int
  timer=`expr $timer + $timer_int`
done

if [ -r ${LOG_DIR}/test.running ]; then
  sh mail.sh "TR WARNING: Tests not finished even after ${mesg}" \
             "Host: ${HOST_NAME}
Project: ${PROJECT_NAME}
Log: ${LOGFILE}
Problem: tests still running after ${mesg}

This is only warning, execution was not killed. It may happen because of two reasons: either test execution is locked or maximum estimated execution time (variable WARNING_TIME in testconfig) is low. Look at log on right machine for reason."
fi

rm -f ${LOG_DIR}/timeout.pid
