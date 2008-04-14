#!/bin/bash
set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
SCRIPTS_DIR=`pwd`
source init.sh

if [ ! -z $NATIVE_MAC_MACHINE ] && [ ! -z $MAC_PATH ]; then
   ssh $NATIVE_MAC_MACHINE rm -rf $MAC_PATH/installer
   ERROR_CODE=$?
   if [ $ERROR_CODE != 0 ]; then
       echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't remove old scripts"
       exit $ERROR_CODE;
   fi
   ssh $NATIVE_MAC_MACHINE mkdir -p $MAC_PATH/installer
   cd $NB_ALL
   gtar c installer/mac | ssh $NATIVE_MAC_MACHINE "( cd $MAC_PATH; tar x )"

   ssh $NATIVE_MAC_MACHINE rm -rf $MAC_PATH/zip/*
   ERROR_CODE=$?
   if [ $ERROR_CODE != 0 ]; then
       echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't remove old bits"
       exit $ERROR_CODE;
   fi
   ssh $NATIVE_MAC_MACHINE mkdir -p $MAC_PATH/zip/moduleclusters
   scp -q -v $DIST/zip/$BASENAME*.zip $NATIVE_MAC_MACHINE:$MAC_PATH/zip
   ls $DIST/zip/moduleclusters | grep -v "all-in-one" | xargs -I {} scp -q -v $DIST/zip/moduleclusters/{} $NATIVE_MAC_MACHINE:$MAC_PATH/zip/moduleclusters/

   ERROR_CODE=$?
   if [ $ERROR_CODE != 0 ]; then
       echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't put the zips"
       exit $ERROR_CODE;
   fi

# Run mysql builds
   scp -q -v $NB_ALL/../build-private.sh $NATIVE_MAC_MACHINE:$MAC_PATH/installer/mac/newbuild   
   ssh $NATIVE_MAC_MACHINE chmod a+x $MAC_PATH/installer/mac/newbuild/build_mysql.sh
   ssh $NATIVE_MAC_MACHINE $MAC_PATH/installer/mac/newbuild/build_mysql.sh $MAC_PATH $BASENAME_PREFIX $BUILDNUMBER  > $MAC_LOG_NEW 2>&1 &

fi

#Check if Mac installer was OK, 10 "BUILD SUCCESSFUL" messages should be in Mac log
if [ ! -z $NATIVE_MAC_MACHINE ] && [ ! -z $MAC_PATH ]; then 

    IS_NEW_MAC_FAILED=`cat $MAC_LOG_NEW | grep "BUILD FAILED" | wc -l | tr " " "\n" | grep -v '^$'`
    IS_NEW_MAC_CONNECT=`cat $MAC_LOG_NEW | grep "Connection timed out" | wc -l | tr " " "\n" | grep -v '^$'`

    if [ $IS_NEW_MAC_FAILED -eq 0 ] && [ $IS_NEW_MAC_CONNECT -eq 0 ]; then
        #copy the bits back
       
        scp $NATIVE_MAC_MACHINE:$MAC_PATH/installer/mac/newbuild/dist_en/* $DIST/bundles
        ERROR_CODE=$?
        if [ $ERROR_CODE != 0 ]; then
            echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't get installers"
            exit $ERROR_CODE;
        fi	
    else
        tail -100 $MAC_LOG_NEW
        echo "ERROR: - Native Mac Installers build failed"
        exit 1;
    fi
fi

cd $DIST
bash ${SCRIPTS_DIR}/files-info.sh bundles zip
ERROR_CODE=$?
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Counting of MD5 sums and size failed"
#    exit $ERROR_CODE;
fi

