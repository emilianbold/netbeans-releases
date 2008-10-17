#!/bin/bash
set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}

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
   #scp -q -v $LAST_BITS_ZIP/$BASENAME*.zip $NATIVE_MAC_MACHINE:$MAC_PATH/zip
   ls $LAST_BITS_ZIP/moduleclusters | grep -v "all-in-one" | grep -v "mobility" | grep -v "enterprise" | grep -v "visualweb" | grep -v "ruby" | grep -v "uml" | grep -v "soa" | xargs -I {} scp -q -v $LAST_BITS_ZIP/moduleclusters/{} $NATIVE_MAC_MACHINE:$MAC_PATH/zip/moduleclusters/

   ERROR_CODE=$?
   if [ $ERROR_CODE != 0 ]; then
       echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't put the zips"
       exit $ERROR_CODE;
   fi

   if [ 1 -eq $ML_BUILD ] ; then
        ssh $NATIVE_MAC_MACHINE rm -rf $MAC_PATH/zip-ml/*      
        ssh $NATIVE_MAC_MACHINE mkdir -p $MAC_PATH/zip-ml/moduleclusters
	#scp -q -v $DIST/ml/zip/$BASENAME*.zip $NATIVE_MAC_MACHINE:$MAC_PATH/zip-ml
        ls $DIST/ml/zip/moduleclusters | grep -v "all-in-one" | grep -v "mobility" | grep -v "enterprise" | grep -v "visualweb" | grep -v "ruby" | grep -v "uml" | grep -v "soa" | xargs -I {} scp -q -v $DIST/ml/zip/moduleclusters/{} $NATIVE_MAC_MACHINE:$MAC_PATH/zip-ml/moduleclusters/
        ERROR_CODE=$?
        if [ $ERROR_CODE != 0 ]; then
             echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't put the zips"
             exit $ERROR_CODE;
        fi
   fi

   scp -q -v $NB_ALL/../build-private.sh $NATIVE_MAC_MACHINE:$MAC_PATH/installer/mac/newbuild   
   ERROR_CODE=$?
   if [ $ERROR_CODE != 0 ]; then
       echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't get installers"
       exit $ERROR_CODE;
   fi

   ssh $NATIVE_MAC_MACHINE sh $MAC_PATH/installer/mac/newbuild/build_javafx.sh $MAC_PATH $BASENAME_PREFIX $BUILDNUMBER $ML_BUILD $LOCALES
   ERROR_CODE=$?
   if [ $ERROR_CODE != 0 ]; then
       echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't get installers"
       exit $ERROR_CODE;
   fi

   mkdir -p $DIST/bundles
   scp -q -v $NATIVE_MAC_MACHINE:$MAC_PATH/installer/mac/newbuild/dist_en/* $DIST/bundles
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
fi
