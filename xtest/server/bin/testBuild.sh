#!/bin/sh
 
. `dirname $0`/set_xtesthome.sh

PROP_FILE=${XTEST_SERVER_HOME}/conf/site-properties
if [ ! -r "${PROP_FILE}" ] ; then
   echo "Site-properties file ${PROP_FILE} not found".
fi

. ${PROP_FILE}

LOG_DIR=${XTEST_SERVER_HOME}/logs
BUILDINFO_DIR=${XTEST_SERVER_HOME}/build-info
CONF_DIR=${XTEST_SERVER_HOME}/conf
export LOG_DIR BUILDINFO_DIR CONF_DIR
mkdir -p ${LOG_DIR}
mkdir -p ${BUILDINFO_DIR}

testBuild_status_file=testBuild.running

if [ -r "${LOG_DIR}/${testBuild_status_file}" ]; then
	echo Another tests are already running
  	sh ${XTEST_SERVER_HOME}/bin/mail.sh "TR ERROR: Concurrent running detected " \
                   "Host: ${HOST_NAME}
Problem: Concurrent running detected
Comandline: $0 $@

Attempt to run tests while another tests are already running."
	exit 1
fi
touch ${LOG_DIR}/${testBuild_status_file}

# check whether new last build exists
check_new_build () {
  # input:
  # BUILDFILE - build file
  # LAST_BUILDINFO - last build_info
  # BUILDINFO_PATH - path to build_info in zip

  diff_value=
  
  if [ ! -r "${BUILDFILE}" ]; then
     echo ERROR: Buildfile ${BUILDFILE} not found!
     return
  fi

  if [ ! -r "${LAST_BUILDINFO}" ]; then
     touch ${LAST_BUILDINFO}
  fi

  if [ "`echo ${BUILDFILE} | grep .zip$`" = "" ] ; then
      ls -l ${BUILDFILE} > ${LAST_BUILDINFO}.new
  else
      unzip -j -p ${BUILDFILE} ${BUILDINFO_PATH} > ${LAST_BUILDINFO}.new 2>/dev/null
      if [ "$?" -ne 0 ]; then
        # unzip failed (maybe build_info not found) => use ls
        ls -l ${BUILDFILE} > ${LAST_BUILDINFO}.new
      fi
  fi
  diff_value=`diff ${LAST_BUILDINFO}.new ${LAST_BUILDINFO}`
  mv -f ${LAST_BUILDINFO}.new ${LAST_BUILDINFO}
}

run_buildtest() {
        if [ ! -z "${xtest_stop_file}" -a -r "${XTEST_SERVER_HOME}/${xtest_stop_file}" ]; then
           return
        fi
      	LOGFILE=${LOG_DIR}/out_${test_config_name}.log
      	export LOGFILE
        export BUILDFILE
        export NBMSFILE
      	
      	oldpwd=`pwd`
      	cd ${XTEST_SERVER_HOME}/bin
        if [ ! -z "$BUILD_NUM" ] ; then
           echo "Testing build n. ${BUILD_NUM}"
           BUILDFILE=${OLD_BUILDFILE}
           NBMSFILE=${OLD_NBMSFILE}
           sh runTests.sh
        elif [ "$last" = "true" ] ; then
           echo "Testing last build"
           BUILDFILE=${LAST_BUILDFILE}
           NBMSFILE=${LAST_NBMSFILE}
           sh runTests.sh
        else 
           BUILDFILE=${LAST_BUILDFILE}
           NBMSFILE=${LAST_NBMSFILE}
           echo "Checking whether new last build is available..."
           LAST_BUILDINFO=${BUILDINFO_DIR}/last_${test_config_name}.info
           check_new_build
           if [ "$diff_value" = "" ] ; then
  		echo "... NO new build available" 
    	   else
		echo "... new build available"
		sh runTests.sh
	   fi	
	fi
	cd $oldpwd
}

exit_running() {
	rm -f ${LOG_DIR}/${testBuild_status_file}
	exit $1
}


parse_args() {
testconfigs=
last=
newonly=
BUILD_NUM=

while [ $# -gt 0 ] ; do
#    echo "Processing arg: '$1'"
    case "$1" in
        -h|-help) cat <<EOF
Usage: $0 [-n build_number | -last | -new_only ] [testconfig_1]...

Options can be

   -n num       number of tested build;
                if ommited, last new build is tested
   -last        last build is tested
   -new_only    last build is tested if and only if this build
                wasn't tested earlier. Default option.
   testconfig   testconfig file; if no one is selected, 
                content of variable TEST_CONFIGS is used.
   
EOF
exit_running 1
;;
        -n) shift; if [ $# -gt 0 ] ; then BUILD_NUM=$1; fi ;;
        -last) last=true ;;
        -new_only) newonly=true ;;
        -*) echo "Unknown argument $1. Try $0 -h"; exit_running 1 ;; 
        *) testconfigs="$testconfigs $1" ;;
    esac
shift
done

if [ "$last" = "true" -a "$newonly" = "true" ] ; then
  echo "Arguments -last and -new_only can't exist together."
  exit_running 1
fi
if [ "$last" = "true" -a ! -z "$BUILD_NUM" ] ; then
  echo "Arguments -last and -n can't exist together."
  exit_running 1
fi
if [ "$newonly" = "true" -a ! -z "$BUILD_NUM" ] ; then
  echo "Arguments -new_only and -n can't exist together."
  exit_running 1
fi

if [ -z "$testconfigs" ] ; then
  if [ -z "$TEST_CONFIG_LIST" ] ; then
       	echo "No testconfig selected and variable TEST_CONFIG_LIST is empty."
       	exit_running 1
  elif [ -f "$TEST_CONFIG_LIST" ] ; then
	testconfigs=`cat $TEST_CONFIG_LIST`
  elif [ -f "${CONF_DIR}/${TEST_CONFIG_LIST}" ] ; then
	testconfigs=`cat ${CONF_DIR}/${TEST_CONFIG_LIST}`
  else
      	echo "File $TEST_CONFIG_LIST not found"
      	exit_running 1
  fi
fi
if [ -z "$testconfigs" ] ; then
  echo "No testconfig selected."
  exit_running 1
fi
}

parse_args "$@"
for tc in $testconfigs ;
do

 if [ -r $tc ] ; then
    current_test_config=$tc
 else
    if [ -r ${CONF_DIR}/$tc ] ; then
       current_test_config=${CONF_DIR}/$tc
    else
       echo "Test config $tc not found!"
       exit_running 1
    fi
 fi
 test_config_name=`basename $current_test_config`
 ( . $current_test_config ; run_buildtest )
done

exit_running

