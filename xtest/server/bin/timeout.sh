#!/bin/sh

echo $$ > ${LOG_DIR}/timeout.pid

if [ "$WARNING_TIME" = "" ]; then
     WARNING_TIME=`expr 10 '*' 3600`
fi	

time_in_hours=`expr $WARNING_TIME / 3600`
time_in_minutes=`expr $WARNING_TIME / 60 % 60` 

mesg=
if [ "$time_in_hours" -gt 0 ] ;  then
   mesg="$time_in_hours hours "
fi
if [ "$time_in_minutes" -gt 0 ] ;  then
   mesg="${mesg}${time_in_minutes} minutes "
fi

sleep $WARNING_TIME &
echo $! > ${LOG_DIR}/sleep.pid
wait $!
rm -f ${LOG_DIR}/sleep.pid

if [ -r ${LOG_DIR}/test.running ]; then
  ant -buildfile ${XTEST_SERVER_BIN}/mail.xml -Dxtest.mail.subject="TR WARNING: Tests not finished even after ${WARNING_TIME}" \
   -Dxtest.mail.message="Host: ${HOST_NAME}  Project: ${PROJECT_NAME}  Log: ${LOGFILE}  Problem: tests still running after ${mesg}"
fi

rm -f ${LOG_DIR}/timeout.pid

