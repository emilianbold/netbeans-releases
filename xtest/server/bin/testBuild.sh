#!/bin/sh

if [ -z "${PROP_FILE}" ] ; then
   PROP_FILE=../conf/site-properties
fi
if [ ! -r "${PROP_FILE}" ] ; then
   echo "Cannot find site-properties file ${PROP_FILE}".
fi

. ${PROP_FILE}

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

  unzip -j -p ${BUILDFILE} ${BUILDINFO_PATH} > ${LAST_BUILDINFO}.new
  diff_value=`diff ${LAST_BUILDINFO}.new ${LAST_BUILDINFO}`
  cp ${LAST_BUILDINFO}.new ${LAST_BUILDINFO}
  rm -f ${LAST_BUILDINFO}.new
}

run_buildtest() {
        if [ -r "${XTEST_SERVER_HOME}/xtest-server.stop" ]; then
           return
        fi
      	LOGFILE=${LOG_DIR}/out_${test_config_name}.log
      	export LOGFILE
        export BUILDFILE
      	
        if [ ! -z "$BUILD_NUM" ] ; then
           echo "Testing build n. ${BUILD_NUM}"
           BUILDFILE=${OLD_BUILDFILE}
           sh runTests.sh
        elif [ "$last" = "true" ] ; then
           echo "Testing last build"
           BUILDFILE=${LAST_BUILDFILE}
           sh runTests.sh
        else 
           BUILDFILE=${LAST_BUILDFILE}
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
exit 1
;;
        -n) shift; if [ $# -gt 0 ] ; then BUILD_NUM=$1; fi ;;
        -last) last=true ;;
        -new_only) newonly=true ;;
        -*) echo "Unknown argument $1. Try $0 -h"; exit 1 ;; 
        *) testconfigs="$testconfigs $1" ;;
    esac
shift
done

if [ "$last" = "true" -a "$newonly" = "true" ] ; then
  echo "Arguments -last and -new_only can't exist together."
  exit 1
fi
if [ "$last" = "true" -a ! -z "$BUILD_NUM" ] ; then
  echo "Arguments -last and -n can't exist together."
  exit 1
fi
if [ "$newonly" = "true" -a ! -z "$BUILD_NUM" ] ; then
  echo "Arguments -new_only and -n can't exist together."
  exit 1
fi
if [ -z "$testconfigs" ] ; then
  testconfigs=$TEST_CONFIGS
fi
if [ -z "$testconfigs" ] ; then
  echo "No testconfig selected."
  exit 1
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
       exit 1
    fi
 fi
 test_config_name=`basename $current_test_config`
 ( . $current_test_config ; run_buildtest )
done
