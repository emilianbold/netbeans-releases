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
    gtar cvjf $dist/tarbz/$base_name-$component.tar.bz2 $filter
}

init()
{
   export ANT_OPTS="-Xmx512m"
   export JAVA_HOME=$JDK_HOME

   DATESTAMP=`date -u +%Y%m%d%M`
   BUILDNUM=trunk-nightly-all-$DATESTAMP
   
   if [ -z $BASE_DIR ]; then
       echo BASE_DIR variable not defined, using the default one: /space/NB-IDE
       echo if you want to use another base dir for the whole build feel free
       echo to define a BASE_DIR variable in your environment

       BASE_DIR=/space/NB-IDE
   fi

   NB_ALL=$BASE_DIR/nb-all
   
   mkdir -p $NB_ALL

   DIST=$BASE_DIR/dist
   LOGS=$DIST/logs
   BASENAME=netbeans-$BUILDNUM

   mkdir -p $DIST/zip
   mkdir -p $DIST/targz
   mkdir -p $DIST/tarbz
   mkdir -p $LOGS

   echo "To be written" > $DIST/INSTALL.txt
   
   #LOGS
   CVS_CHECKOUT_LOG=$LOGS/$BASENAME-cvs-checkout.log
   IDE_BUILD_LOG=$LOGS/$BASENAME-build-ide.log
   MOBILITY_BUILD_LOG=$LOGS/$BASENAME-build-mobility.log
   VISUALWEB_BUILD_LOG=$LOGS/$BASENAME-build-visualweb.log
   UML_BUILD_LOG=$LOGS/$BASENAME-build-uml.log
   SOA_BUILD_LOG=$LOGS/$BASENAME-build-soa.log
   RUBY_BUILD_LOG=$LOGS/$BASENAME-build-ruby.log
}

#Initialize basic scructure
init

#Clean destination dirs
if [ -d $DIST ]; then
    find $DIST -type f -exec rm {} \;
fi
    
cd  $NB_ALL

#Clean the leftovers from the last build.
#For more info about "cvspurge" take a look 
#at http://www.red-bean.com/cvsutils/
for i in `ls | grep -v "CVS"`; do
    cvspurge $i;
done


###################################################################
#
# Checkout all the required NB modules
#
###################################################################

#nbbuild module is required for the list of modules
cvs -d :pserver:anoncvs@cvs.netbeans.org:/cvs checkout -D '00:00UTC today' nbbuild > $CVS_CHECKOUT_LOG 2>&1

#Checkout the rest of required modules for the NB IDE itself
ant -f nbbuild/build.xml checkout >> $CVS_CHECKOUT_LOG 2>&1

#Checkout modules for the components
cvs -d :pserver:anoncvs@cvs.netbeans.org:/cvs checkout -D '00:00UTC today' mobility uml visualweb scripting enterprise print >> $CVS_CHECKOUT_LOG 2>&1


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

#Mobility component
ant -f mobility/build.xml build > $MOBILITY_BUILD_LOG 2>&1

#UML component
ant -f uml/build.xml build > $ULM_BUILD_LOG 2>&1

#Ruby scripting
ant -f scripting/ruby/build.xml build > $RUBY_BUILD_LOG 2>&1

#SOA component
ant -f nbbuild/entpack/build.xml build > $SOA_BUILD_LOG 2>&1


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

#Pack all the NetBeans
pack_component $DIST $BASENAME all-in-one netbeans

cd netbeans

#Continue with individual component
pack_component $DIST $BASENAME uml "uml*"
rm -rf uml*

pack_component $DIST $BASENAME visualweb "visulalweb*"
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

pack_component $DIST $BASENAME nb6.0-etc "*"
