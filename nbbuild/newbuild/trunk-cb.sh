#!/bin/bash
set -x

#Initialize basic scructure
DIRNAME=`dirname $0`
cd ${DIRNAME}
TRUNK_CB_DIRNAME=`pwd`
export BUILD_DESC=trunk-cb
source init.sh

###################################################################
#
# Clean remains from last build
#
###################################################################

cd $BASE_DIR
rm -rf dist
ant clean

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
# Build all the components
#
###################################################################

#cd $TRUNK_CB_DIRNAME
#bash run-vw-sanity.sh
#ERROR_CODE=$?

#if [ $ERROR_CODE != 0 ]; then
#    echo "ERROR: $ERROR_CODE - VW sanity failed"
#    exit $ERROR_CODE;
#fi




###################################################################
#
# Pack all the components
#
###################################################################

cd $TRUNK_CB_DIRNAME
bash pack-all-components.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Packaging failed"
    exit $ERROR_CODE;
fi
