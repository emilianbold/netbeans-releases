#!/bin/bash
set -x

#Initialize basic scructure
DIRNAME=`dirname $0`
cd ${DIRNAME}
TRUNK_CB_DIRNAME=`pwd`
source init.sh

cd $TRUNK_CB_DIRNAME
bash clean-purge.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Clean failed"
    exit $ERROR_CODE;
fi

###################################################################
#
# Update all the required NB modules
#
###################################################################

cd $TRUNK_CB_DIRNAME
bash update-all-components.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Update failed"
    exit $ERROR_CODE;
fi

###################################################################
#
# Check if the build is required
#
###################################################################

cd $TRUNK_CB_DIRNAME
bash check-for-update.sh
RETURN_CODE=$?

if [ $RETURN_CODE -eq 2 ]; then
    #There were no update, sleep for 10 min and exit
    sleep 360
    exit 0;
fi


###################################################################
#
# Build all the components
#
###################################################################

cd $TRUNK_CB_DIRNAME
bash build-all-components.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Build failed"
    exit $ERROR_CODE;
fi

###################################################################
#
# Pack all the components
#
###################################################################

cd $TRUNK_CB_DIRNAME
bash pack-all-componets.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Packaging failed"
    exit $ERROR_CODE;
fi
