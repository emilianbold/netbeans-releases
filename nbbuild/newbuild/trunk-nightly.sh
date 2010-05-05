#!/bin/bash
set -x

#Initialize basic scructure
DIRNAME=`dirname $0`
cd ${DIRNAME}
TRUNK_NIGHTLY_DIRNAME=`pwd`
export BUILD_DESC=trunk-nightly
source init.sh

rm -rf $DIST

if [ ! -z $WORKSPACE ]; then
    #I'm under hudson and have sources here, I need to clone them
    #Clean obsolete sources first
    rm -rf $NB_ALL
    hg clone $WORKSPACE $NB_ALL
    if [ $RUNJAVAFX == 1 ]; then
        #Clone also javafx sources - XXX needs to be parametrized
        cd $NB_ALL
        hg clone http://hg.netbeans.org/javafx
    fi
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

if [ -n $BUILD_ID ]; then
    mkdir -p $DIST_SERVER2/${BUILD_ID}
    cp -rp $DIST/*  $DIST_SERVER2/${BUILD_ID}
    if [ -n "${TESTING_SCRIPT}" ]; then
        cd $NB_ALL
        TIP_REV=`hg tip --template "{node}"`
        ssh $TESTING_SCRIPT $TIP_REV
        cd $DIRNAME
    fi
fi

if [ $UPLOAD_ML == 1 ]; then
    cp $DIST/zip/$BASENAME-platform-src.zip $DIST/ml/zip/
    cp $DIST/zip/$BASENAME-src.zip $DIST/ml/zip/
    cp $DIST/zip/$BASENAME-javadoc.zip $DIST/ml/zip/
    cp $DIST/zip/hg-l10n-$BUILDNUMBER.zip $DIST/ml/zip/
    cp $DIST/zip/ide-l10n-$BUILDNUMBER.zip $DIST/ml/zip/
    cp $DIST/zip/stable-UC-l10n-$BUILDNUMBER.zip $DIST/ml/zip/
    cp $DIST/zip/testdist-$BUILDNUMBER.zip $DIST/ml/zip/
fi

cd $TRUNK_NIGHTLY_DIRNAME
bash build-nbi.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - NBI installers build failed"
    exit $ERROR_CODE;
fi

if [ -n $BUILD_ID ]; then
    mkdir -p $DIST_SERVER2/${BUILD_ID}
    cp -rp $DIST/*  $DIST_SERVER2/${BUILD_ID}
    rm $DIST_SERVER2/latest.old
    mv $DIST_SERVER2/latest $DIST_SERVER2/latest.old
    ln -s $DIST_SERVER2/${BUILD_ID} $DIST_SERVER2/latest
    if [ $UPLOAD_ML == 0 -a ML_BUILD != 0 ]; then
        rm -r $DIST/ml
    fi
fi

if [ $UPLOAD_ML == 1 ]; then
    mv $DIST/jnlp $DIST/ml/
    mv $DIST/javadoc $DIST/ml/
fi

#XXX Remove any javafx related files before upload to public site
cd $DIST
find . -name "*javafx*" -exec rm {} \;

if [ -z $DIST_SERVER ]; then
    exit 0;
fi

cd $TRUNK_NIGHTLY_DIRNAME
bash upload-bits.sh
