#!/bin/bash
set -x

#Initialize basic scructure
DIRNAME=`dirname $0`
cd ${DIRNAME}
TRUNK_NIGHTLY_DIRNAME=`pwd`
source init.sh

cd $TRUNK_NIGHTLY_DIRNAME
bash clean-all.sh

#Clean the leftovers from the last build.
#For more info about "cvspurge" take a look 
#at http://www.red-bean.com/cvsutils/
#for i in `ls | grep -v "CVS"`; do
#    cvspurge $i;
#done


###################################################################
#
# Checkout all the required NB modules
#
###################################################################

cd $TRUNK_NIGHTLY_DIRNAME
bash checkout-all-componets.sh

###################################################################
#
# Build all the components
#
###################################################################

cd $TRUNK_NIGHTLY_DIRNAME
bash build-all-componets.sh

###################################################################
#
# Pack all the components
#
###################################################################

cd $TRUNK_NIGHTLY_DIRNAME
bash pack-all-componets.sh

###################################################################
#
# Deploy bits to the storage server
#
###################################################################

if [ -z $DIST_SERVER ]; then
    exit 0;
fi

ssh -p 222 $DIST_SERVER mkdir -p $DIST_SERVER_PATH/$DATESTAMP
scp -P 222 -q -r -v $DIST/* $DIST_SERVER:$DIST_SERVER_PATH/$DATESTAMP > $SCP_LOG 2>&1 &

cd $TRUNK_NIGHTLY_DIRNAME
bash build-nbi.sh

scp -P 222 -q -r -v $DIST/installers $DIST_SERVER:$DIST_SERVER_PATH/$DATESTAMP > $SCP_LOG 2>&1
ssh -p 222 $DIST_SERVER rm $DIST_SERVER_PATH/latest.old
ssh -p 222 $DIST_SERVER mv $DIST_SERVER_PATH/latest $DIST_SERVER_PATH/latest.old
ssh -p 222 $DIST_SERVER ln -s $DIST_SERVER_PATH/$DATESTAMP $DIST_SERVER_PATH/latest
