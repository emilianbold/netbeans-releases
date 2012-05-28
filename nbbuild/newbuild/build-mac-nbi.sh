#!/bin/bash
set -x

if [ ! -z $TIP ] ; then
    echo Update to $TIP
    hg up --rev $TIP
fi

DIRNAME=`dirname $0`
cd ${DIRNAME}
SCRIPTS_DIR=`pwd`
source init.sh

if [ -z $BUILD_NBJDK7 ]; then
    BUILD_NBJDK7=0
fi

OUTPUT_DIR="$DIST/installers"
export OUTPUT_DIR

# Run new builds
sh $NB_ALL/installer/mac/newbuild/init.sh
sh $NB_ALL/installer/mac/newbuild/build.sh $MAC_PATH $BASENAME_PREFIX $BUILDNUMBER $EN_BUILD $ML_BUILD $BUILD_NBJDK7 $LOCALES > $MAC_LOG_NEW 2>&1 &
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - NBI installers build failed"
    exit $ERROR_CODE;
fi

if [ -d $DIST/ml ]; then
    mv $OUTPUT_DIR/ml/* $DIST/ml
    rm -rf $OUTPUT_DIR/ml
fi

mv $OUTPUT_DIR/* $DIST
rmdir $OUTPUT_DIR

###################################################################
#
# Sign Windows ML installers
#
###################################################################

if [ -z $DONT_SIGN_INSTALLER ]; then

    if [ -z $SIGN_CLIENT ]; then
        echo "ERROR: SIGN_CLIENT not defined - Signing failed"
        exit 1;
    fi

    if [ -z $SIGN_USR ]; then
        echo "ERROR: SIGN_USR not defined - Signing failed"
        exit 1;
    fi

    if [ -z $SIGN_PASS ]; then
        echo "ERROR: SIGN_PASS not defined - Signing failed"
        exit 1;
    fi

    find $DIST/ml/bundles -name "netbeans-*-windows.exe" | xargs -t -I [] java -Xmx2048m -jar $SIGN_CLIENT/Client.jar -file_to_sign [] -user $SIGN_USR -pass $SIGN_PASS -signed_location $DIST/ml/bundles -sign_method microsoft
    ERROR_CODE=$?

    if [ $ERROR_CODE != 0 ]; then
        echo "ERROR: $ERROR_CODE - Signing failed"
        exit $ERROR_CODE;
    fi

fi

cd $DIST
bash ${SCRIPTS_DIR}/files-info.sh bundles bundles/jdk zip zip/moduleclusters
ERROR_CODE=$?
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Counting of MD5 sums and size failed"
#    exit $ERROR_CODE;
fi

if [ $ML_BUILD == 1 ]; then
    cd $DIST/ml
    bash ${SCRIPTS_DIR}/files-info.sh bundles zip zip/moduleclusters
    ERROR_CODE=$?
    if [ $ERROR_CODE != 0 ]; then
        echo "ERROR: $ERROR_CODE - Counting of MD5 sums and size failed"
#        exit $ERROR_CODE;
    fi
fi
