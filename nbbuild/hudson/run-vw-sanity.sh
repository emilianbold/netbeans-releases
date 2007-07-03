#!/bin/bash

## Install GlassFish apllication server and run commit validation test for visual web cluster
## (http://wiki.netbeans.org/wiki/view/VWSanityTestInstructions).
## Results are added to xtest/instance/results.

set -x

###################################################################

# Initialization

AS_ROOT=/hudson/workdir/jobs/trunk/workspace/nbbuild/build/vw-sanity
AS_BINARY=/hudson/glassfish-installer-v2-b52.jar
J2EE_HOME=${AS_ROOT}/glassfish
TEST_ROOT=`pwd`/visualweb/test

mkdir -p $AS_ROOT

###################################################################

uninstall() {
	# Stop domain
	$J2EE_HOME/bin/asadmin stop-domain visualweb

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

install() {
	# Install Application Server
	cd ${AS_ROOT}

	TEMP_DISPLAY="${DISPLAY}"
	unset DISPLAY
        
	# Creating statefile
	rm -f ${AS_ROOT}/sunappserver_statefile
	echo "A" > ${AS_ROOT}/sunappserver_statefile
	java -Xmx256m -jar ${AS_BINARY} < ${AS_ROOT}/sunappserver_statefile

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
	ant -f ${J2EE_HOME}/setup.xml -Dinstance.port=28080 -Ddomain.name=visualweb
}

###################################################################

setup_properties() {
	# Setup properties file 
	cp $TEST_ROOT/data/DefaultDeploymentTargets.properties.template $TEST_ROOT/data/tmp.properties
	MODIFIED_J2EE_HOME=`echo ${J2EE_HOME} | sed 's/\//@/g'`
	sed -e "s/J2EE_HOME/${MODIFIED_J2EE_HOME}/g" -e "s/@/\//g" -e "s/8080/28080/g" -e "s/domain1/visualweb/g" $TEST_ROOT/data/tmp.properties > $TEST_ROOT/data/DefaultDeploymentTargets.properties
   
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
        cd $TEST_ROOT/../ravebuild
	ant build-test-tools
        ## XXX temporarily for debugging reasons store results separately
        # ant commit-validation -Dxtest.results=$TEST_ROOT/../../xtest/instance/results
	ant commit-validation -Dxtest.results=$TEST_ROOT/../../xtest/instance/results/vw

	ERROR_CODE=$?
	if [ $ERROR_CODE != 0 ]; then
            echo "ERROR: $ERROR_CODE - Error in running visualweb sanity test"
            exit $ERROR_CODE;
	fi
}

###################################################################

cleanup() {
	# Stop domain
	$J2EE_HOME/bin/asadmin stop-domain visualweb
}
############################# MAIN ################################

uninstall
install
setup_appserver
setup_properties
run_sanity
cleanup

############################## END ################################
