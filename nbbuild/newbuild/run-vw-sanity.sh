set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

###################################################################
#
# Initialization
#
###################################################################

BASE_DIR="/soft/visualweb/build"

SRCROOT="${BASE_DIR}/src"
CACHEROOT="${BASE_DIR}/cache"

AS_KITSERVER="/net/balui.sfbay/cache/SunAppServer/v9.0/PE/v2/b33/"
AS_BINARY="glassfish-installer-sol-sparc-v2-b33.jar"
AS_ROOT="${BASE_DIR}/SUNWappserver"

J2EE_HOME="${BASE_DIR}/SUNWappserver/glassfish"
TESTROOT="${BASE_DIR}/src/visualweb/test"

###################################################################
#
# Routines
#
###################################################################

# Download Application Server
download() {
    cp ${AS_KITSERVER}/${AS_BINARY} ${CACHEROOT}
    ERROR_CODE=$?

    if [ $ERROR_CODE != 0 ]; then
    	echo "ERROR: $ERROR_CODE - Can't download Glassfish"
    	exit $ERROR_CODE;
    fi
}

# Install Application Server
install() {
    if [ ! -d ${AS_ROOT} ]; then
	mkdir ${AS_ROOT}
    fi

    # First be good and uninstall
    if [ -x $J2EE_HOME/bin/uninstall ]; then
	$J2EE_HOME/bin/uninstall -silent
    fi
    rm -rf ${J2EE_HOME} ~/.asadmin* ~/.asadminpass ~/.asadmintruststore

    # Creating statefile
    rm -f ${AS_ROOT}/sunappserver_statefile
    echo "A" > ${AS_ROOT}/sunappserver_statefile

    if [ -d ${J2EE_HOME} ]; then
	rm -rf ${J2EE_HOME}
    fi

    chmod a+x ${CACHEROOT}/${AS_BINARY}
    cd ${AS_ROOT}
    TEMP_DISPLAY="$DISPLAY"
    DISPLAY=""
    export DISPLAY
    java -Xmx256m -jar ${CACHEROOT}/${AS_BINARY} < ${AS_ROOT}/sunappserver_statefile

    ERROR_CODE=$?

    if [ $ERROR_CODE != 0 ]; then
	echo "ERROR: $ERROR_CODE - Can't install Glassfish"
	exit $ERROR_CODE;
    fi

    DISPLAY="${TEMP_DISPLAY}"
    export DISPLAY
}

# Setup Application Server
setup() {
    if [ ! -d ${J2EE_HOME}/domains/domain1 ]; then
	cd ${J2EE_HOME}

	ant -f setup.xml
	cp $TESTROOT/data/DefaultDeploymentTargets.properties.template $TESTROOT/data/tmp.properties
	MODIFIED_J2EE_HOME=`echo ${J2EE_HOME} | sed 's/\//::/g'`
	sed -e "s/J2EE_HOME/${MODIFIED_J2EE_HOME}/g" -e "s/::/\//g" $TESTROOT/data/tmp.properties > $TESTROOT/data/DefaultDeploymentTargets.properties

	ERROR_CODE=$?

        if [ $ERROR_CODE != 0 ]; then
            echo "ERROR: $ERROR_CODE - Can't setup Glassfish"
            exit $ERROR_CODE;
        fi

	rm -f $TESTROOT/data/tmp.properties
    fi
}

# Run Sanity test on VisualWeb build
run_sanity() {
    cd $SRCROOT/visualweb/ravebuild

    ant build-test-tools -Dnetbeans.dist.dir="${J2EE_HOME}"
    ant commit-validation -Dnetbeans.dist.dir="${J2EE_HOME}"

    ERROR_CODE=$?

    if [ $ERROR_CODE != 0 ]; then
	echo "ERROR: $ERROR_CODE - Error in running sanity test"
	exit $ERROR_CODE;
    fi
}


###################################################################
#
# Main
#
###################################################################

download
install
setup
run_sanity
