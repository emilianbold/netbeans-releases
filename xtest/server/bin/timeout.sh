#!/bin/sh

echo $$ > ${LOG_DIR}/timeout.pid

if [ "$WARNING_TIME" = "" ]; then
     WARNING_TIME=10h
fi	
 
sleep $WARNING_TIME &
echo $! > ${LOG_DIR}/sleep.pid
wait $!
rm -f ${LOG_DIR}/sleep.pid

if [ -r ${LOG_DIR}/test.running ]; then
  ant -buildfile ${XTEST_SERVER_BIN}/mail.xml -Dxtest.mail.subject="TR WARNING: Tests not finished even after ${WARNING_TIME}" \
   -Dxtest.mail.message="Host: ${HOST_NAME}  Project: ${PROJECT_NAME}  Log: ${LOGFILE}  Problem: tests still running after ${WARNING_TIME}"
fi

rm -f ${LOG_DIR}/timeout.pid

