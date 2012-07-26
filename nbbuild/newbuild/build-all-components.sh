set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
SCRIPT_DIR=`pwd`
source init.sh

#Clean old tests results
if [ -n $WORKSPACE ]; then
    rm -rf $WORKSPACE/results
fi

cd  $NB_ALL

###################################################################
#
# Build all the components
#
###################################################################

mkdir -p nbbuild/netbeans

#Build source packages
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f nbbuild/build.xml -Dmerge.dependent.modules=false -Dcluster.config=full build-source-config
ERROR_CODE=$?

create_test_result "build.source.package" "Build Source package" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build all source package"
#    exit $ERROR_CODE;
else
    mv nbbuild/build/*-src-* $DIST/zip/$BASENAME-src.zip
fi

ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f nbbuild/build.xml -Dmerge.dependent.modules=false -Dcluster.config=platform build-source-config
ERROR_CODE=$?

create_test_result "build.source.platform" "Build Platform Source package" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build basic platform source package"
#    exit $ERROR_CODE;
else
    mv nbbuild/build/*-src-* $DIST/zip/$BASENAME-platform-src.zip
fi

#Build the NB IDE first - no validation tests!
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f nbbuild/build.xml build-nozip -Dbuild.compiler.debuglevel=source,lines,vars
ERROR_CODE=$?

create_test_result "build.IDE" "Build IDE" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build IDE"
    exit $ERROR_CODE;
fi

###############  Commit validation tests  ##########################
#cp -r $NB_ALL/nbbuild/netbeans $NB_ALL/nbbuild/netbeans-PRISTINE

TESTS_STARTED=`date`
# Different JDK for tests because JVM crashes often (see 6598709, 6607038)
JDK_TESTS=$JDK_HOME
# standard NetBeans unit and UI validation tests
ant -v -f nbbuild/build.xml -Dlocales=$LOCALES -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER commit-validation
ERROR_CODE=$?

create_test_result "test.commit-validation" "Commit Validation" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Commit validation failed"
    #TEST_CODE=1;
fi

if [ -n $WORKSPACE ]; then
    cp -r $NB_ALL/nbbuild/build/test/results $WORKSPACE
fi

echo TESTS STARTED: $TESTS_STARTED
echo TESTS FINISHED: `date`
if [ "${TEST_CODE}" = 1 ]; then
    echo "ERROR: At least one of validation tests failed"
    exit 1;
fi

#Remove file created during commit validation
rm -rf $NB_ALL/nbbuild/netbeans/nb/servicetag
rm -rf $NB_ALL/nbbuild/netbeans/enterprise/config/GlassFishEE6

ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f nbbuild/build.xml build-test-dist -Dtest.fail.on.error=false
ERROR_CODE=$?

create_test_result "build.test.dist" "Build Test Distribution" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Building of Test Distrubution failed"
    exit $ERROR_CODE;
else
    mv nbbuild/build/testdist.zip $DIST/zip/testdist-${BUILDNUMBER}.zip
fi

cd $NB_ALL

#Build JNLP
#ant -Djnlp.codebase=http://bits.netbeans.org/dev/jnlp/ -Djnlp.signjar.keystore=$KEYSTORE -Djnlp.signjar.alias=nb_ide -Djnlp.signjar.password=$STOREPASS -Djnlp.dest.dir=${DIST}/jnlp build-jnlp
#ERROR_CODE=$?

#create_test_result "build.jnlp" "Build JNLP" $ERROR_CODE
#if [ $ERROR_CODE != 0 ]; then
#    echo "ERROR: $ERROR_CODE - Cannot build JNLP"
##    exit $ERROR_CODE;
#fi

#Build all NBMs for stable UC - IDE + UC-only
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f nbbuild/build.xml build-nbms -Dcluster.config=stableuc -Dbase.nbm.target.dir=${DIST}/uc2 -Dkeystore=$KEYSTORE -Dstorepass=$STOREPASS -Dbuild.compiler.debuglevel=source,lines
ERROR_CODE=$?

create_test_result "build.NBMs" "Build all NBMs" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build all stable UC NBMs"
#    exit $ERROR_CODE;
fi

# Separate IDE nbms from stableuc nbms.
ant -f nbbuild/build.xml move-ide-nbms -Dnbms.source.location=${DIST}/uc2 -Dnbms.target.location=${DIST}/uc
ERROR_CODE=$?

create_test_result "get.ide.NBMs" "Extract IDE NBMs from all the built NBMs" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot extract IDE NBMs"
#    exit $ERROR_CODE;
fi


#Build 110n kit for HG files
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f build.xml hg-l10n-kit -Dl10n.kit=${DIST}/zip/hg-l10n-$BUILDNUMBER.zip
ERROR_CODE=$?

create_test_result "build.hg.l10n" "Build 110n kit for HG files" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build l10n kits for HG files"
#    exit $ERROR_CODE;
fi

#Build l10n kit for IDE modules
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f build.xml l10n-kit -Dnbms.location=${DIST}/uc -Dl10n.kit=${DIST}/zip/ide-l10n-$BUILDNUMBER.zip
ERROR_CODE=$?

create_test_result "build.modules.l10n" "Build l10n kit for IDE modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build l10n kits for IDE modules"
#    exit $ERROR_CODE;
fi

#Build l10n kit for stable uc modules
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f build.xml l10n-kit -Dnbms.location=${DIST}/uc2 -Dl10n.kit=${DIST}/zip/stableuc-l10n-$BUILDNUMBER.zip
ERROR_CODE=$?

create_test_result "build.modules.l10n" "Build l10n kit for stable uc modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build l10n kits for stable uc modules"
#    exit $ERROR_CODE;
fi

cd nbbuild
#Build catalog for IDE NBMs
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/uc -Dcatalog.file=${DIST}/uc/catalog.xml
ERROR_CODE=$?

create_test_result "build.ide.catalog" "Build UC catalog for IDE modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build UC catalog for IDE module"
#    exit $ERROR_CODE;
fi

#Build catalog for Stable UC NBMs
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/uc2 -Dcatalog.file=${DIST}/uc2/catalog.xml
ERROR_CODE=$?

create_test_result "build.stableuc.catalog" "Build UC catalog for stable UC modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build UC catalog for stable UC modules"
#    exit $ERROR_CODE;
fi
cd ..

#ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/javadoctools/build.xml build-javadoc
#ERROR_CODE=$?

#create_test_result "build.javadoc" "Build javadoc" $ERROR_CODE
#if [ $ERROR_CODE != 0 ]; then
#    echo "ERROR: $ERROR_CODE - Building of Javadoc Distrubution failed"
##    exit $ERROR_CODE;
#else
#    mv nbbuild/NetBeans-*-javadoc.zip $DIST/zip/$BASENAME-javadoc.zip
#    cp -r nbbuild/build/javadoc $DIST/
#fi

##ML_BUILD
#if [ $ML_BUILD == 1 ]; then
#    cd $NB_ALL
#    if [ -d $NB_ALL/l10n/.hg ] ; then
#        cd $NB_ALL/l10n
#        hg pull
#        hg update --clean --rev $L10N_BRANCH
#    else
#        rm -Rf $NB_ALL/l10n
#        hg clone --rev $L10N_BRANCH $ML_REPO l10n
#    fi
#
#    cd $NB_ALL/l10n
#    ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml -Dlocales=$LOCALES -Ddist.dir=$NB_ALL/nbbuild/netbeans-ml -Dnbms.dir=${DIST}/uc -Dnbms.dist.dir=${DIST}/ml/uc -Dkeystore=$KEYSTORE -Dstorepass=$STOREPASS build
#    ERROR_CODE=$?
#
#    create_test_result "build.ML.IDE" "Build ML IDE" $ERROR_CODE
#    if [ $ERROR_CODE != 0 ]; then
#        echo "ERROR: $ERROR_CODE - Cannot build ML IDE"
##        exit $ERROR_CODE;
#    fi
#
##    ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml -Dlocales=$LOCALES -Ddist.dir=$NB_ALL/nbbuild/netbeans-ml -Dnbms.dir=${DIST}/uc2 -Dnbms.dist.dir=${DIST}/ml/uc2 -Dkeystore=$KEYSTORE -Dstorepass=$STOREPASS build
#
##    create_test_result "build.ML.stableuc" "Build ML Stable UC modules" $ERROR_CODE
##    ERROR_CODE=$?
##    if [ $ERROR_CODE != 0 ]; then
##        echo "ERROR: $ERROR_CODE - Cannot build ML Stable UC modules"
##        exit $ERROR_CODE;
##    fi
#
#    if [ ! -z $UC_NBMS_DIR ]; then
#       for UC_CLUSTER in $UC_EXTRA_CLUSTERS; do
#          cp -r ${UC_NBMS_DIR}/${UC_CLUSTER} ${DIST}/ml/uc
#       done
#    fi
#
#    cd $NB_ALL/nbbuild
#    #Build catalog for ML FU NBMs
#    ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/ml/uc -Dcatalog.file=${DIST}/ml/uc/catalog.xml
#    ERROR_CODE=$?
#
#    create_test_result "build.ML.FU.catalog" "Build ML FU catalog" $ERROR_CODE
#    if [ $ERROR_CODE != 0 ]; then
#        echo "ERROR: $ERROR_CODE - Cannot build catalog FU for ML NBMs"
#    #    exit $ERROR_CODE;
#    fi
#
#    #Build catalog for ML stable UC NBMs
##    create_test_result "build.ML.stableuc.catalog" "Build ML Stable UC catalog" $ERROR_CODE
##    ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/uml/uc2 -Dcatalog.file=${DIST}/ml/uc2/catalog.xml
##    ERROR_CODE=$?
#
##    if [ $ERROR_CODE != 0 ]; then
##        echo "ERROR: $ERROR_CODE - Cannot build stable UC catalog for ML NBMs"
#    #    exit $ERROR_CODE;
##    fi
#
#    cp -r $NB_ALL/nbbuild/netbeans/* $NB_ALL/nbbuild/netbeans-ml/
#
#    cd $NB_ALL/nbbuild
#    #Remove the build helper files
#    rm -f netbeans-ml/nb.cluster.*
##    rm -f netbeans-ml/build_info
#    rm -rf netbeans-ml/extra
#
#    cd $NB_ALL/nbbuild/newbuild
#
#    find $BASE_DIR/dist/ml/uc/apisupport/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/apisupport/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/cnd/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/cnd/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/dlight/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/dlight/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/enterprise/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/enterprise/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/ergonomics/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/ergonomics/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/groovy/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/groovy/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/harness/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/harness/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/ide/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/ide/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/java/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/java/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/javacard/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/javacard/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/javafx/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/javafx/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/mobility/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/mobility/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/nb/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/nb/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/php/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/php/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/platform/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/platform/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/profiler/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/profiler/update_tracking/
#    rm -rf update_tracking
#
#    find $BASE_DIR/dist/ml/uc/websvccommon/ -name "*.nbm" | xargs -t -I {} bash generate_updatetracking_from_nbm.sh {}
#    mv -u update_tracking/* $BASE_DIR/main/nbbuild/netbeans-ml/websvccommon/update_tracking/
#    rm -rf update_tracking
#
#fi

cd $NB_ALL/nbbuild

if [ ! -z $UC_NBMS_DIR ]; then
   for UC_CLUSTER in $UC_EXTRA_CLUSTERS; do
      cp -r ${UC_NBMS_DIR}/${UC_CLUSTER} ${DIST}/uc
   done
fi

#Build catalog for FU NBMs
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/uc -Dcatalog.file=${DIST}/uc/catalog.xml
ERROR_CODE=$?

create_test_result "build.FU.catalog" "Build catalog FU modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build catalog FU for NBMs"
#    exit $ERROR_CODE;
fi


#Remove the build helper files
rm -f netbeans/nb.cluster.*
#rm -f netbeans/build_info
#rm -rf netbeans/extra
