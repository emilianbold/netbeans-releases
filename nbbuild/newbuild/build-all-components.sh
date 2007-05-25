set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

cd  $NB_ALL

###################################################################
#
# Build all the components
#
###################################################################

#Build the NB IDE first - no validation tests!
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-nozip > $IDE_BUILD_LOG 2>&1
ERROR_CODE=$?

#There is no reason to continue when the basic IDE build fails
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build basic IDE"
    exit $ERROR_CODE;
fi

#Components doesn't depend on each other, so no more error checking

#VisualWeb component
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml -Ddo-not-rebuild-clusters=true -Dnb.clusters.list=nb.cluster.profiler,nb.cluster.harness,nb.cluster.ide,nb.cluster.java,nb.cluster.apisupport,nb.cluster.j2ee,nb.cluster.nb,nb.cluster.platform,nb.cluster.xml,nb.cluster.visualweb build-nozip > $VISUALWEB_BUILD_LOG 2>&1
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build VISUALWEB"
    exit $ERROR_CODE;
fi


#Mobility component
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f mobility/build.xml build > $MOBILITY_BUILD_LOG 2>&1
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build MOBILITY"
    exit $ERROR_CODE;
fi

#UML component
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml -Ddo-not-rebuild-clusters=true -Dnb.clusters.list=nb.cluster.profiler,nb.cluster.harness,nb.cluster.ide,nb.cluster.java,nb.cluster.apisupport,nb.cluster.j2ee,nb.cluster.nb,nb.cluster.platform,nb.cluster.xml,nb.cluster.uml build-nozip > $UML_BUILD_LOG 2>&1
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build UML"
    exit $ERROR_CODE;
fi

#Ruby scripting
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f scripting/ruby/build.xml build > $RUBY_BUILD_LOG 2>&1
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build RUBY"
    exit $ERROR_CODE;
fi

#SOA component
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml -Ddo-not-rebuild-clusters=true -Dnb.clusters.list=nb.cluster.profiler,nb.cluster.harness,nb.cluster.ide,nb.cluster.java,nb.cluster.apisupport,nb.cluster.j2ee,nb.cluster.nb,nb.cluster.platform,nb.cluster.xml,nb.cluster.soa build-nozip > $SOA_BUILD_LOG 2>&1
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build SOA"
    exit $ERROR_CODE;
fi

#Build all the NBMs
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-nbms -Dmoduleconfig=all -Dbase.nbm.target.dir=${DIST}/nbms > ${NBMS_BUILD_LOC} 2>&1
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build NBMs"
    exit $ERROR_CODE;
fi

cd $NB_ALL/nbbuild

#Remove the build helper files
rm -f netbeans/nb.cluster.*
rm -f netbeans/moduleCluster.properties
rm -f netbeans/module_tracking.xml
rm -f netbeans/build_info
