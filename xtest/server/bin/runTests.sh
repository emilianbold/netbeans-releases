#!/bin/sh

# Required environment variables:
#  JAVA_HOME
#  ANT_HOME
#  BUILDFILE
#  HOST_NAME
#  DRIVER_CONFIG
#  PROJECT_NAME

# Optional environment variables:
#  DRIVER_ARGS
#  LOGFILE
#  SHIP_RESULTS

#==========

if [ -z "${XTEST_SERVER_BIN}" ] ; then
  XTEST_SERVER_BIN=. 
fi
if [ -z "${LOG_DIR}" ] ; then
  LOG_DIR =../log
fi
export XTEST_SERVER_BIN LOG_DIR

# Check java
if [ -z "${JAVA_HOME}" -o ! -d "${JAVA_HOME}/bin" ]; then
  echo "Java not found. Set JAVA_HOME."
  return 1
fi

# Check ant
if [ -z "${ANT_HOME}" -o ! -d "${ANT_HOME}/bin" ]; then
  echo "Ant not found. Set ANT_HOME."
  return 1
fi

# Check tested build
if [ ! -r "${BUILDFILE}" ]; then
  echo "Cannot find build ${BUILDFILE}. Set variable BUILDFILE "
  return 1
fi

# Check hostname
if [ -z "${HOST_NAME}" -o "${HOST_NAME}" = "unknown" ]; then
  echo "Set variable HOST_NAME with corect value."
  return 1
fi
export HOST_NAME

# Check driver config
if [ ! -r "${DRIVER_CONFIG}" ]; then
  echo "Cannot find driver config ${DRIVER_CONFIG}. Set variable ${DRIVER_CONFIG}."
  return 1
fi

# Check project name
if [ -z "${PROJECT_NAME}" ]; then
  echo "Variable PROJECT_NAME not set."
  return 1
fi
export PROJECT_NAME

# Check log file
if [ ! -z "${LOGFILE}" ]; then
  LOG_ARG="-logfile ${LOGFILE}"
  export LOGFILE
else  
  echo "Variable LOGFILE not set. Using standart output."
  LOG_ARG=
fi

# Check results shipping
if [ ! -z "${SHIP_RESULTS}" ]; then
  SHIP_ARG="-Dxtest.ship.results.to=${SHIP_RESULTS}"
else  
  echo "Variable SHIP_RESULTS not set. Results will not be shipped to server."
  SHIP_ARG=
fi

CMD_TO_RUN="cd ${XTEST_SERVER_BIN} ; ant ${LOG_ARG} -Dide.install.path=${BUILDFILE} \
   -Dxtest.tested.project='${PROJECT_NAME}' -Dxtest.driver.config=${DRIVER_CONFIG} \
   -Dxtest.machine=${HOST_NAME} ${SHIP_ARG} ${DRIVER_ARGS}"

JDK_HOME=$JAVA_HOME
JAVA_PATH=$JAVA_HOME

OLD_PATH=$PATH

PATH=${JAVA_HOME}/bin:${ANT_HOME}/bin:$PATH
export JDK_HOME JAVA_HOME JAVA_PATH PATH

# flag that tests are running
touch ${LOG_DIR}/test.running

sh ${XTEST_SERVER_BIN}/timeout.sh > ${LOG_DIR}/timeout.out &

echo Testing build ${BUILDFILE} of project ${PROJECT_NAME}
echo Time: `date`  Log: ${LOGFILE}

eval $CMD_TO_RUN

if [ ! $? -eq 0 ]; then
  ant -buildfile ${XTEST_SERVER_BIN}/mail.xml -Dxtest.mail.subject="TR ERROR: Test buildscript failed" \
    -Dxtest.mail.message="Host: ${HOST_NAME}  Project: ${PROJECT_NAME}  \
    Log: ${LOGFILE}  Problem: buildscript failed. Look at log for more details."
fi

echo Test finished at `date`

rm -f ${LOG_DIR}/test.running

if [ -r ${LOG_DIR}/timeout.pid ]; then
  kill `cat ${LOG_DIR}/timeout.pid`
fi
if [ -r ${LOG_DIR}/sleep.pid ]; then
  kill `cat ${LOG_DIR}/sleep.pid`
fi

PATH=$OLD_PATH

