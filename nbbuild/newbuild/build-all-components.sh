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

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build basic IDE"
    exit $ERROR_CODE;
fi

#Build all the NBMs
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-nbms -Dmoduleconfig=all -Dbase.nbm.target.dir=${DIST}/nbms
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build NBMs"
    exit $ERROR_CODE;
fi

#ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml -Dmerge.dependent.modules=false -Dcluster.name=nb.cluster.platform sanity-build-from-source
#ERROR_CODE=$?

#if [ $ERROR_CODE != 0 ]; then
#    echo "ERROR: $ERROR_CODE - Can't build basic platform source package"
#    exit $ERROR_CODE;
#fi

cd $NB_ALL/nbbuild

#Remove the build helper files
rm -f netbeans/nb.cluster.*
rm -f netbeans/build_info
