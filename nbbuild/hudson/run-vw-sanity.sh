#!/bin/bash

## Run commit validation test for visual web cluster
## (http://wiki.netbeans.org/wiki/view/VWSanityTestInstructions).

set -x

###################################################################

# Initialization

AS_ROOT=/hudson/workdir/jobs/trunk/testappsrv
AS_HOME=${AS_ROOT}/glassfish
AS_DOMAIN=domain1
AS_PORT=8080
TEST_ROOT=`pwd`/visualweb.kit/test

###################################################################

setup_properties() {
	# Setup properties file 
	cp $TEST_ROOT/data/DefaultDeploymentTargets.properties.template $TEST_ROOT/data/tmp.properties
	MODIFIED_AS_HOME=`echo ${AS_HOME} | sed 's/\//@/g'`
	sed -e "s/J2EE_HOME/${MODIFIED_AS_HOME}/g" -e "s/@/\//g" -e "s/8080/${AS_PORT}/g" -e "s/domain1/${AS_DOMAIN}/g" $TEST_ROOT/data/tmp.properties > $TEST_ROOT/data/DefaultDeploymentTargets.properties
   
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
        #cd $TEST_ROOT
	#ant build-test-tools
        # XXX temporarily store results separately until fixed
        ant -f visualweb.kit/build.xml -Dtest.config=uicommit -Dcontinue.after.failing.tests=true test

	ERROR_CODE=$?
	if [ $ERROR_CODE != 0 ]; then
            echo "ERROR: $ERROR_CODE - Error in running visualweb sanity test"
            exit $ERROR_CODE;
	fi
}

############################# MAIN ################################

setup_properties
run_sanity

############################## END ################################
