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

#Build source packages
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml -Dmerge.dependent.modules=false -Dcluster.config=full build-source-config
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build all source package"
#    exit $ERROR_CODE;
else
    mv nbbuild/build/*-src-* $DIST/zip/$BASENAME-src.zip
fi

ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml -Dmerge.dependent.modules=false -Dcluster.name=nb.cluster.platform build-source
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build basic platform source package"
#    exit $ERROR_CODE;
else
    mv nbbuild/build/*-src-* $DIST/zip/$BASENAME-platform-src.zip
fi

#Build the NB IDE first - no validation tests!
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-nozip -Dcluster.config=stableuc -Dbuild.compiler.debuglevel=source,lines
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build basic IDE"
    exit $ERROR_CODE;
fi

#Build all FU the NBMs
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-nbms -Dmoduleconfig=all -Dbase.nbm.target.dir=${DIST}/uc -Dkeystore=$KEYSTORE -Dstorepass=$STOREPASS -Dbuild.compiler.debuglevel=source,lines
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build NBMs"
    exit $ERROR_CODE;
fi

cd nbbuild
#Build catalog for FU NBMs
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/uc -Dcatalog.file=${DIST}/uc/catalog.xml -Dcatalog.base.url="."
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build catalog FU for NBMs"
#    exit $ERROR_CODE;
fi

#Build all NBMs for stable UC
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-nbms -Dmoduleconfig=stableuc -Dbase.nbm.target.dir=${DIST}/uc2 -Dkeystore=$KEYSTORE -Dstorepass=$STOREPASS -Dbuild.compiler.debuglevel=source,lines
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build stable UC NBMs"
    exit $ERROR_CODE;
fi

#Build catalog for stable UC NBMs
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/uc2 -Dcatalog.file=${DIST}/uc2/catalog.xml -Dcatalog.base.url="."
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build stable UC catalog for NBMs"
#    exit $ERROR_CODE;
fi

cd ..

ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml all-xtest build-test-dist -Dtest.fail.on.error=false -Dbuild.compiler.debuglevel=source,lines 
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Building of Test Distrubution failed"
#    exit $ERROR_CODE;
else
    mv nbbuild/build/testdist.zip $DIST/zip/testdist-${BUILDNUMBER}.zip
fi

ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-javadoc
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Building of Javadoc Distrubution failed"
#    exit $ERROR_CODE;
else
    mv nbbuild/NetBeans-*-javadoc.zip $DIST/zip/$BASENAME-javadoc.zip
    cp -r nbbuild/build/javadoc $DIST/
fi

cd $NB_ALL/nbbuild

#Remove the build helper files
rm -f netbeans/nb.cluster.*
rm -f netbeans/build_info
rm -rf netbeans/extra
rm -rf netbeans/testtools
