#!/bin/bash
set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
SCRIPTS_DIR=`pwd`
source init.sh

OUTPUT_DIR="$DIST/installers"
export OUTPUT_DIR

if [ ! -z $NATIVE_MAC_MACHINE ] && [ ! -z $MAC_PATH ]; then
   ssh $NATIVE_MAC_MACHINE rm -rf $MAC_PATH/installer $MAC_PATH/reglib/src
   ERROR_CODE=$?
   if [ $ERROR_CODE != 0 ]; then
       echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't remove old scripts"
       exit $ERROR_CODE;
   fi
   ssh $NATIVE_MAC_MACHINE mkdir -p $MAC_PATH/installer $MAC_PATH/reglib/src
   cd $NB_ALL
   gtar c installer/mac reglib/src | ssh $NATIVE_MAC_MACHINE "( cd $MAC_PATH; tar x )"

   if [ 1 -eq $ML_BUILD ] ; then
       cd $NB_ALL/l10n
       gtar c src/*/other/installer/mac/* | ssh $NATIVE_MAC_MACHINE "( cd $MAC_PATH; tar x)"
       cd $NB_ALL
   fi
   ssh $NATIVE_MAC_MACHINE rm -rf $MAC_PATH/zip/* 
   ERROR_CODE=$?
   if [ $ERROR_CODE != 0 ]; then
       echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't remove old bits"
       exit $ERROR_CODE;
   fi
   ssh $NATIVE_MAC_MACHINE mkdir -p $MAC_PATH/zip/moduleclusters
#   scp -q -v $DIST/zip/$BASENAME*.zip $NATIVE_MAC_MACHINE:$MAC_PATH/zip
   ls $DIST/zip/moduleclusters | grep -v "all-in-one" | xargs -I {} scp -q -v $DIST/zip/moduleclusters/{} $NATIVE_MAC_MACHINE:$MAC_PATH/zip/moduleclusters/
   if [ 1 -eq $ML_BUILD ] ; then
        ssh $NATIVE_MAC_MACHINE rm -rf $MAC_PATH/zip-ml/*      
        ssh $NATIVE_MAC_MACHINE mkdir -p $MAC_PATH/zip-ml/moduleclusters
	#scp -q -v $DIST/ml/zip/$BASENAME*.zip $NATIVE_MAC_MACHINE:$MAC_PATH/zip-ml
        ls $DIST/ml/zip/moduleclusters | grep -v "all-in-one" | xargs -I {} scp -q -v $DIST/ml/zip/moduleclusters/{} $NATIVE_MAC_MACHINE:$MAC_PATH/zip-ml/moduleclusters/
   fi
   ERROR_CODE=$?
   if [ $ERROR_CODE != 0 ]; then
       echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't put the zips"
       exit $ERROR_CODE;
   fi
#   ssh $NATIVE_MAC_MACHINE $MAC_PATH/run-mac-installer.sh $ML_BUILD > $MAC_LOG 2>&1 &

# Run new builds
   sh $NB_ALL/installer/mac/newbuild/init.sh | ssh $NATIVE_MAC_MACHINE "cat > $MAC_PATH/installer/mac/newbuild/build-private.sh"
   ssh $NATIVE_MAC_MACHINE chmod a+x $MAC_PATH/installer/mac/newbuild/build.sh
#   if [ 1 -eq $ML_BUILD ] ; then
#       ssh $NATIVE_MAC_MACHINE $MAC_PATH/installer/mac/newbuild/build.sh $MAC_PATH/zip-ml/moduleclusters $BASENAME_PREFIX $BUILDNUMBER $ML_BUILD > $MAC_LOG_NEW 2>&1 &  
#       mv $MAC_PATH/installer/mac/newbuild/dist/* $MAC_PATH/dist_ml 
#   fi
   ssh $NATIVE_MAC_MACHINE $MAC_PATH/installer/mac/newbuild/build.sh $MAC_PATH $BASENAME_PREFIX $BUILDNUMBER $ML_BUILD $LOCALES > $MAC_LOG_NEW 2>&1 &

fi

cd $NB_ALL/installer/infra/build

bash build.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - NBI installers build failed"
    exit $ERROR_CODE;
fi

tail -f $MAC_LOG_NEW &
TAIL_PID=$!

set +x
RUNNING_JOBS_COUNT=`jobs | wc -l | tr " " "\n" | grep -v '^$'`
#Wait for the end of native mac build
while [ $RUNNING_JOBS_COUNT -ge 2 ]; do
    #1 or more jobs
    sleep 10
    jobs > /dev/null
    RUNNING_JOBS_COUNT=`jobs | wc -l | tr " " "\n" | grep -v '^$'`
done
set -x
kill -s 9 $TAIL_PID

if [ -d $DIST/ml ]; then
    mv $OUTPUT_DIR/ml/* $DIST/ml
    rm -rf $OUTPUT_DIR/ml
fi

mv $OUTPUT_DIR/* $DIST
rmdir $OUTPUT_DIR

#Check if Mac installer was OK, 10 "BUILD SUCCESSFUL" messages should be in Mac log
if [ ! -z $NATIVE_MAC_MACHINE ] && [ ! -z $MAC_PATH ]; then
#    IS_MAC_FAILED=`cat $MAC_LOG | grep "BUILD FAILED" | wc -l | tr " " "\n" | grep -v '^$'`
#    IS_MAC_CONNECT=`cat $MAC_LOG | grep "Connection timed out" | wc -l | tr " " "\n" | grep -v '^$'`

    IS_NEW_MAC_FAILED=`cat $MAC_LOG_NEW | grep "BUILD FAILED" | wc -l | tr " " "\n" | grep -v '^$'`
    IS_NEW_MAC_CONNECT=`cat $MAC_LOG_NEW | grep "Connection timed out" | wc -l | tr " " "\n" | grep -v '^$'`

    if [ $IS_NEW_MAC_FAILED -eq 0 ] && [ $IS_NEW_MAC_CONNECT -eq 0 ]; then
        #copy the bits back
        mkdir -p $DIST/bundles
        #scp $NATIVE_MAC_MACHINE:$MAC_PATH/installer/mac/dist/* $DIST/bundles        
        #ERROR_CODE=$?
        #if [ $ERROR_CODE != 0 ]; then
        #    echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't get installers"
        #    exit $ERROR_CODE;
        #fi    
        scp $NATIVE_MAC_MACHINE:$MAC_PATH/installer/mac/newbuild/dist_en/* $DIST/bundles
        ERROR_CODE=$?
        if [ $ERROR_CODE != 0 ]; then
            echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't get installers"
            exit $ERROR_CODE;
        fi
	if [ 1 -eq $ML_BUILD ] ; then
		#scp $NATIVE_MAC_MACHINE:$MAC_PATH/dist/* $DIST/ml/bundles
		scp $NATIVE_MAC_MACHINE:$MAC_PATH/installer/mac/newbuild/dist/* $DIST/ml/bundles
                ERROR_CODE=$?
                if [ $ERROR_CODE != 0 ]; then
                    echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't get ml installers"
                    exit $ERROR_CODE;
                fi    
	fi
    else
        tail -100 $MAC_LOG_NEW
        echo "ERROR: - Native Mac Installers build failed"
        exit 1;
    fi
fi

cd $DIST
bash ${SCRIPTS_DIR}/files-info.sh bundles zip zip/moduleclusters
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
