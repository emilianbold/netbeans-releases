#!/bin/sh

# Required environment variables:
#  JAVA_HOME
#  ANT_HOME
#  BUILDFILE
#  HOST_NAME
#  DRIVER_CONFIG

# Optional environment variables:
#  PROJECT_NAME
#  TESTED_TYPE
#  DRIVER_ARGS
#  LOGFILE
#  SHIP_RESULTS
#  NBMSFILE
#  RE_CONF_NAME
#  USER_CONF_NAME

#==========

if [ -z "${LOG_DIR}" ] ; then
  LOG_DIR =../logs
fi
export LOG_DIR

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
if [ ! -z "${PROJECT_NAME}" ]; then
  PROJECT_NAME_ARG="-Dxtest.tested.project='${PROJECT_NAME}'"
fi

# Check tested type
if [ ! -z "${TESTED_TYPE}" ]; then
  TESTED_TYPE_ARG="-Dxtest.tested.type='${TESTED_TYPE}'"
fi

# Check log file
if [ ! -z "${LOGFILE}" ]; then
  LOG_ARG="-logfile \"${LOGFILE}\""
  export LOGFILE
  if [ -f "${LOGFILE}" ] ; then
     mv ${LOGFILE} ${LOGFILE}.old
  fi
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

# Check NBMSFILE
if [ ! -z "${NBMSFILE}" ] ; then
  if [ "`echo ${NBMSFILE} | grep .zip$`" = "" ] ; then
     NBMS_ARG="-Dide.nbm.dir=${NBMSFILE}"
  else
     NBMS_ARG="-Dide.nbm.zipfile=${NBMSFILE}"
  fi
fi

# Check XTEST_HOME
if [ ! -z "${XTEST_HOME}" ] ; then
  XTEST_HOME_ARG="-Dxtest.home=${XTEST_HOME}"
fi

RE_CONF_ARG=

# Check RE_CONF_NAME
if [ ! -z "$RE_CONF_NAME" ] ; then
  RE_CONF_ARG=-Dxtest.driver.re.conf=`dirname ${BUILDFILE}`/${RE_CONF_NAME}
fi

# Check USER_CONF_NAME
if [ ! -z "$USER_CONF_NAME" ] ; then
  RE_CONF_ARG="${RE_CONF_ARG} -Dxtest.driver.re.user.conf=`dirname ${BUILDFILE}`/${USER_CONF_NAME}"
fi

#=============

CMD_TO_RUN="ant ${LOG_ARG} ${XTEST_HOME_ARG} -Dide.install.path=${BUILDFILE} ${RE_CONF_ARG} ${NBMS_ARG} \
   ${PROJECT_NAME_ARG} ${TESTED_TYPE_ARG} -Dxtest.driver.config=${DRIVER_CONFIG} \
   -Dxtest.machine=${HOST_NAME} ${SHIP_ARG} ${DRIVER_ARGS}"

JDK_HOME=$JAVA_HOME
JAVA_PATH=$JAVA_HOME

OLD_PATH=$PATH

case "`uname`" in
     CYGWIN*) PATH=`cygpath -u "${JAVA_HOME}"`/bin:`cygpath -u "${ANT_HOME}"`/bin:$PATH ;;
     *)       PATH=${JAVA_HOME}/bin:${ANT_HOME}/bin:$PATH ;;
esac

export JDK_HOME JAVA_HOME JAVA_PATH PATH

# flag that tests are running
touch ${LOG_DIR}/test.running

sh timeout.sh &

echo Testing build ${BUILDFILE} of project ${PROJECT_NAME}
echo Time: `date`  Log: ${LOGFILE}

eval $CMD_TO_RUN

if [ ! $? -eq 0 ]; then
  sh mail.sh "TR ERROR: Test execution failed" \
             "Host: ${HOST_NAME}
Project: ${PROJECT_NAME}
Log: ${LOGFILE}
Problem: Test execution failed

Main buildscript failed and test execution is not complete. Look at log on right machine for reason of this failure."
fi

echo Test finished at `date`

rm -f ${LOG_DIR}/test.running

if [ -r ${LOG_DIR}/timeout.pid ]; then
  wait `cat ${LOG_DIR}/timeout.pid`
fi

PATH=$OLD_PATH

