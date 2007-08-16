#!/bin/bash
set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

cd $BASE_DIR
cvs -d :pserver:anoncvs@cvs.netbeans.org:/cvs checkout -D "$CVS_STAMP" -PA -d NBI installer/infra/build

cd NBI

if [ ! -z $NATIVE_MAC_MACHINE ]; then
   ssh $NATIVE_MAC_MACHINE rm -f $MAC_PATH/zip/*
   scp $DIST/zip/$BASENAME*.zip $NATIVE_MAC_MACHINE:$MAC_PATH/zip
   ssh $NATIVE_MAC_MACHINE $MAC_PATH/run-mac-installer.sh > $MAC_LOG 2>&1 &
fi

bash build.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - NBI installers build failed"
    exit $ERROR_CODE;
fi

set +x
RUNNING_JOBS_COUNT=`jobs | wc -l | tr " " "\n" | grep -v '^$'`
#Wait for the end of native mac build
while [ $RUNNING_JOBS_COUNT -ge 1 ]; do
    #1 or more jobs
    sleep 10
    jobs > /dev/null
    RUNNING_JOBS_COUNT=`jobs | wc -l | tr " " "\n" | grep -v '^$'`
done
set -x

mv $DIST/installers/* $DIST
rmdir $DIST/installers

#Check if Mac installer was OK, 10 "BUILD SUCCESSFUL" messages should be in Mac log
if [ ! -z $NATIVE_MAC_MACHINE ]; then
    IS_MAC_OK=`cat $MAC_LOG | grep "BUILD SUCCESSFUL" | wc -l | tr " " "\n" | grep -v '^$'`
    if [ $IS_MAC_OK -ge 10 ]; then
        #copy the bits back
        mkdir -p $DIST/bundles
        scp $NATIVE_MAC_MACHINE:$MAC_PATH/installer/mac/dist/* $DIST/bundles
    else
        tail -100 $MAC_LOG
        echo "ERROR: - Native Mac NBI installers build failed"
#        exit 1;
    fi
fi
