#!/bin/bash
set -x

#Initialize basic scructure
DIRNAME=`dirname $0`
cd ${DIRNAME}
TRUNK_NIGHTLY_DIRNAME=`pwd`
export BUILD_DESC=trunk-nightly
source init.sh

cd $TRUNK_NIGHTLY_DIRNAME
bash clean-all.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Clean failed"
    exit $ERROR_CODE;
fi

###################################################################
#
# Checkout all the required NB modules
#
###################################################################

cd $TRUNK_NIGHTLY_DIRNAME
bash checkout-all-components.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Checkout failed"
    exit $ERROR_CODE;
fi

###################################################################
#
# Build all the components
#
###################################################################

cd $TRUNK_NIGHTLY_DIRNAME
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

cd $TRUNK_NIGHTLY_DIRNAME
bash pack-all-components.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Packaging failed"
    exit $ERROR_CODE;
fi

###################################################################
#
# Deploy bits to the storage server
#
###################################################################

if [ ! -z $BUILD_ID ]; then
    mkdir -p $DIST_SERVER2/${BUILD_ID}
    cp -rp $DIST/*  $DIST_SERVER2/${BUILD_ID}
fi

cd $TRUNK_NIGHTLY_DIRNAME
bash build-nbi.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - NBI installers build failed"
    exit $ERROR_CODE;
fi

if [ ! -z $BUILD_ID ]; then
    mkdir -p $DIST_SERVER2/${BUILD_ID}
    cp -rp $DIST/*  $DIST_SERVER2/${BUILD_ID}
    mv $DIST_SERVER2/latest $DIST_SERVER2/latest.old
    ln -s $DIST_SERVER2/${BUILD_ID} $DIST_SERVER2/latest
    if [ $UPLOAD_ML == 0 ]; then
        rm -r $DIST/ml
    fi
fi

if [ -z $DIST_SERVER ]; then
    exit 0;
fi

cd $TRUNK_NIGHTLY_DIRNAME
bash upload-bits.sh
