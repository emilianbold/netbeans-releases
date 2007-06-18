#!/bin/bash

set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

###################################################################

# Initialization

AS_ROOT="${BASE_DIR}/SUNWappserver"

if [ "x${J2EE_HOME}x" = "xx" ]; then
    J2EE_HOME="${AS_ROOT}/glassfish"
fi

TEST_ROOT="${NB_ALL}/visualweb/test"

###################################################################

download () {
	# Download App Server
	if [ ! -d ${AS_ROOT} ]; then
		mkdir -p $AS_ROOT
	fi

	if [ -f ${AS_ROOT}/${AS_BINARY} ]; then
		rm -f ${AS_ROOT}/${AS_BINARY}
	fi

	if [ -f ${AS_KITSERVER}/${AS_BINARY} ]; then
    	cp ${AS_KITSERVER}/${AS_BINARY} ${AS_ROOT}
    	ERROR_CODE=$?
	else
    	echo "ERROR: Please set AS_KITSERVER and AS_BINARY - ${AS_KITSERVER}/${AS_BINARY}"
    	exit 2;
	fi

	if [ $ERROR_CODE != 0 ]; then
    	echo "ERROR: $ERROR_CODE - Can't download Glassfish"
    	exit $ERROR_CODE;
	fi
}

###################################################################

uninstall() {
	# First be good and uninstall
	if [ -x $J2EE_HOME/bin/uninstall ]; then
    	$J2EE_HOME/bin/uninstall -silent
	fi

	# This is a temp hack as the "uninstall" command is broken
	if [ -d ${J2EE_HOME} -a ! -z ${J2EE_HOME} ]; then
		rm -rf ${J2EE_HOME}
	fi
}

###################################################################

create_statefile() {
	# Creating statefile
	rm -f ${AS_ROOT}/sunappserver_statefile
	echo "A" > ${AS_ROOT}/sunappserver_statefile
}

###################################################################

install() {
	# Install Application Server
	if [ ! -d ${AS_ROOT} ]; then
		mkdir ${AS_ROOT}
	fi

	chmod a+x ${AS_ROOT}/${AS_BINARY}
	cd ${AS_ROOT}

	TEMP_DISPLAY="${DISPLAY}"
	unset DISPLAY

	java -Xmx256m -jar ${AS_ROOT}/${AS_BINARY} < ${AS_ROOT}/sunappserver_statefile

	ERROR_CODE=$?
	if [ $ERROR_CODE != 0 ]; then
    	echo "ERROR: $ERROR_CODE - Can't install Glassfish"
    	exit $ERROR_CODE;
	fi

	DISPLAY="${TEMP_DISPLAY}"
	export DISPLAY
}

###################################################################

setup_appserver() {
	# Setup Application Server
	cd ${J2EE_HOME}
	ant -f setup.xml -Dinstance.port=28080 -Ddomain.name=visualweb
}

###################################################################

setup_properties() {
	# Setup properties file 
	cp $TEST_ROOT/data/DefaultDeploymentTargets.properties.template $TEST_ROOT/data/tmp.properties
	MODIFIED_J2EE_HOME=`echo ${J2EE_HOME} | sed 's/\//::/g'`
	sed -e "s/J2EE_HOME/${MODIFIED_J2EE_HOME}/g" -e "s/::/\//g" -e "s/8080/28080/g" -e "s/domain1/visualweb/g" $TEST_ROOT/data/tmp.properties > $TEST_ROOT/data/DefaultDeploymentTargets.properties
   
	ERROR_CODE=$?
	if [ $ERROR_CODE != 0 ]; then
		echo "ERROR: $ERROR_CODE - Can't setup Glassfish"
		exit $ERROR_CODE;
	fi
    
	rm -f $TEST_ROOT/data/tmp.properties
}

###################################################################

run_sanity() {
	# Run Sanity test on VisualWeb build
	cd ${NB_ALL}/visualweb/ravebuild
	ant build-test-tools -Dnetbeans.dist.dir="${J2EE_HOME}"
	ant commit-validation -Dnetbeans.dist.dir="${J2EE_HOME}"

	ERROR_CODE=$?
	if [ $ERROR_CODE != 0 ]; then
    	echo "ERROR: $ERROR_CODE - Error in running sanity test"
    	exit $ERROR_CODE;
	fi
}

############################# MAIN ################################

download

uninstall

create_statefile

install

setup_appserver

setup_properties

run_sanity

############################## END ################################
