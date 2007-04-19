#!/bin/bash
set -x

pack_component() 
{
    dist=$1
    base_name=$2
    component=$3
    filter=$4
    zip -r $dist/zip/$base_name-$component.zip $filter
    gtar cvzf $dist/targz/$base_name-$component.tar.gz $filter
    gtar cvjf $dist/tarbz2/$base_name-$component.tar.bz2 $filter
}

#Initialize basic scructure
DIRNAME=`dirname $0`
cd ${DIRNAME}
TRUNK_NIGHTLY_DIRNAME=`pwd`
source init.sh


#Clean destination dirs
if [ -d $DIST ]; then
    find $DIST -type f -exec rm {} \;
fi
    
if [ -d $NB_ALL ]; then
    rm -rf $NB_ALL
fi

mkdir -p $NB_ALL

cd  $NB_ALL

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

#nbbuild module is required for the list of modules
cvs -d :pserver:anoncvs@cvs.netbeans.org:/cvs checkout -D "$CVS_STAMP" nbbuild > $CVS_CHECKOUT_LOG 2>&1

#Checkout the rest of required modules for the NB IDE itself
ant -f nbbuild/build.xml checkout >> $CVS_CHECKOUT_LOG 2>&1

#Checkout modules for the components
cvs -d :pserver:anoncvs@cvs.netbeans.org:/cvs checkout -D "$CVS_STAMP" mobility uml visualweb scripting enterprise print identity  >> $CVS_CHECKOUT_LOG 2>&1


###################################################################
#
# Build all the components
#
###################################################################

#Build the NB IDE first - no validation tests!
ant -Dbuildnum=$BUILDNUM -f nbbuild/build.xml build-nozip > $IDE_BUILD_LOG 2>&1
ERROR_CODE=$?

#There is no reason to continue when the basic IDE build fails
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build basic IDE"
    exit $ERROR_CODE;
fi

#Components doesn't depend on each other, so no more error checking

#VisualWeb component
ant -Dnb_all=$NB_ALL -f visualweb/ravebuild/build.xml > $VISUALWEB_BUILD_LOG 2>&1
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build VISUALWEB"
    exit $ERROR_CODE;
fi


#Mobility component
ant -f mobility/build.xml build > $MOBILITY_BUILD_LOG 2>&1
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build MOBILITY"
    exit $ERROR_CODE;
fi

#UML component
ant -f nbbuild/build.xml -Ddo-not-rebuild-clusters=true -Dnb.clusters.list=nb.cluster.profiler,nb.cluster.harness,nb.cluster.ide,nb.cluster.java,nb.cluster.apisupport,nb.cluster.j2ee,nb.cluster.nb,nb.cluster.platform,nb.cluster.uml build-nozip > $UML_BUILD_LOG 2>&1
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build UML"
    exit $ERROR_CODE;
fi

#Ruby scripting
ant -f scripting/ruby/build.xml build > $RUBY_BUILD_LOG 2>&1
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build RUBY"
    exit $ERROR_CODE;
fi

#SOA component
ant -f nbbuild/build.xml -Ddo-not-rebuild-clusters=true -Dnb.clusters.list=nb.cluster.profiler,nb.cluster.harness,nb.cluster.ide,nb.cluster.java,nb.cluster.apisupport,nb.cluster.j2ee,nb.cluster.nb,nb.cluster.platform,nb.cluster.soa build-nozip > $SOA_BUILD_LOG 2>&1
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build SOA"
    exit $ERROR_CODE;
fi

cd $NB_ALL/nbbuild

#Remove the build helper files
rm -f netbeans/nb.cluster.*
rm -f netbeans/moduleCluster.properties
rm -f netbeans/module_tracking.xml
rm -f netbeans/build_info



###################################################################
#
# Pack all the components
#
###################################################################

cd $NB_ALL/nbbuild

#Pack all the NetBeans
pack_component $DIST $BASENAME all-in-one netbeans

cd $NB_ALL/nbbuild/netbeans

#Continue with individual component
pack_component $DIST $BASENAME uml "uml*"
rm -rf uml*

pack_component $DIST $BASENAME visualweb "visualweb*"
rm -rf visualweb*

pack_component $DIST $BASENAME ruby "ruby*"
rm -rf ruby*

pack_component $DIST $BASENAME profiler "profiler*"
rm -rf profiler*

pack_component $DIST $BASENAME platform "platform*"
rm -rf platform*

pack_component $DIST $BASENAME mobility "mobility*"
rm -rf mobility*

pack_component $DIST $BASENAME ide "ide*"
rm -rf ide*

pack_component $DIST $BASENAME harness "harness*"
rm -rf harness*

pack_component $DIST $BASENAME enterprise "enterprise*"
rm -rf enterprise*

pack_component $DIST $BASENAME soa "soa*"
rm -rf soa*

pack_component $DIST $BASENAME apisupport "apisupport*"
rm -rf apisupport*

pack_component $DIST $BASENAME java "java*"
rm -rf java*

pack_component $DIST $BASENAME nb6.0-etc "*"


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
ssh -p 222 $DIST_SERVER ln -s $DIST_SERVER_PATH/$DATESTAMP $DIST_SERVER_PATH/latest
