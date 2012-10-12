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

if [ -z "$SIGNING_IDENTITY" ]; then
    SIGNING_IDENTITY=0
fi

if [ ! -z $SIGNING_PASSWORD ] ; then
    security unlock-keychain -p $SIGNING_PASSWORD
fi

   if [ 1 -eq $ML_BUILD ] ; then
       cd $NB_ALL/l10n
       tar c src/*/other/installer/mac/* | ( cd $NB_ALL; tar x )
       cd $NB_ALL
   fi


# Run new builds
sh $NB_ALL/installer/mac/newbuild/init.sh
sh $NB_ALL/installer/mac/newbuild/build.sh $MAC_PATH $BASENAME_PREFIX $BUILDNUMBER $ML_BUILD $BUILD_NBJDK7 "$SIGNING_IDENTITY" $LOCALES
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - NBI installers build failed"
    exit $ERROR_CODE;
fi

if [ 1 -eq $EN_BUILD ] || [ -z $EN_BUILD ] ; then
    mkdir -p $DIST/bundles
    cp -r $WORKSPACE/installer/mac/newbuild/dist_en/* $DIST/bundles
    ERROR_CODE=$?
    if [ $ERROR_CODE != 0 ]; then
        echo "ERROR: $ERROR_CODE - Cannot copy installers"
        exit $ERROR_CODE;
    fi
fi
if [ 1 -eq $ML_BUILD ] ; then
    mkdir -p $DIST/ml/bundles
    cp -r $WORKSPACE/installer/mac/newbuild/dist/* $DIST/ml/bundles
    ERROR_CODE=$?
    if [ $ERROR_CODE != 0 ]; then
        echo "ERROR: $ERROR_CODE - Cannot copy installers"
        exit $ERROR_CODE;
    fi
fi

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

if [ 1 -eq $EN_BUILD ] || [ -z $EN_BUILD ] ; then
    cd $DIST
    #bash ${SCRIPTS_DIR}/files-info.sh bundles bundles/jdk zip zip/moduleclusters
    bash ${SCRIPTS_DIR}/files-info.sh bundles bundles/jdk
    ERROR_CODE=$?
    if [ $ERROR_CODE != 0 ]; then
        echo "ERROR: $ERROR_CODE - Counting of MD5 sums and size failed"
    #    exit $ERROR_CODE;
    fi
fi

if [ $ML_BUILD == 1 ]; then
    cd $DIST/ml
    #bash ${SCRIPTS_DIR}/files-info.sh bundles zip zip/moduleclusters
    bash ${SCRIPTS_DIR}/files-info.sh bundles
    ERROR_CODE=$?
    if [ $ERROR_CODE != 0 ]; then
        echo "ERROR: $ERROR_CODE - Counting of MD5 sums and size failed"
#        exit $ERROR_CODE;
    fi
fi

if [ ! -z $SIGNING_PASSWORD ] ; then
    security lock-keychain
fi
