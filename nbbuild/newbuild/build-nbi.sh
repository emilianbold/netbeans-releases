#!/bin/bash
set -x

init() 
{
   if [ -z '$CVS_STAMP' ]; then
       CVS_STAMP="00:00UTC today"
       export DATESTAMP=`date -u +%Y%m%d0000`
   fi

   if [ -z $BASE_DIR ]; then
       echo BASE_DIR variable not defined, using the default one: /space/NB-IDE
       echo if you want to use another base dir for the whole build feel free
       echo to define a BASE_DIR variable in your environment

       export BASE_DIR=/space/NB-IDE
   fi

   if [ -z $DIST_SERVER ]; then
       echo DIST_SERVER not defined: Upload will no work
   fi

   if [ -z $DIST_SERVER_PATH ]; then
       echo DIST_SERVER_PATH not defined using default
       DIST_SERVER_PATH=/releng/www/netbeans/6.0/nightly
   fi

   DIST=$BASE_DIR/dist
   LOGS=$DIST/logs
   BASENAME=netbeans-$BUILDNUM
   INSTALLERS_LOG=$LOGS/$BASENAME-installers.log
}
init

cd $BASE_DIR
cvs -d :pserver:anoncvs@cvs.netbeans.org:/cvs checkout -D "$CVS_STAMP" -PA -d NBI installer/infra/build

cd installer/infra/build

bash build.sh > $INSTALLERS_LOG 2>&1 
