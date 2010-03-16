set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
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
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml -Dmerge.dependent.modules=false -Dcluster.config=full build-source-config
ERROR_CODE=$?

create_test_result "build.source.package" "Build Source package" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build all source package"
#    exit $ERROR_CODE;
else
    mv nbbuild/build/*-src-* $DIST/zip/$BASENAME-src.zip
fi

ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml -Dmerge.dependent.modules=false -Dcluster.name=nb.cluster.platform build-source
ERROR_CODE=$?

create_test_result "build.source.platform" "Build Platform Source package" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build basic platform source package"
#    exit $ERROR_CODE;
else
    mv nbbuild/build/*-src-* $DIST/zip/$BASENAME-platform-src.zip
fi

#Build the NB IDE first - no validation tests!
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-nozip -Dbuild.compiler.debuglevel=source,lines,vars
ERROR_CODE=$?

create_test_result "build.IDE" "Build IDE" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build IDE"
    exit $ERROR_CODE;
fi

###############  Commit validation tests  ##########################
#cp -r $NB_ALL/nbbuild/netbeans $NB_ALL/nbbuild/netbeans-PRISTINE

TESTS_STARTED=`date`
# Different JDK for tests because JVM crashes often (see 6598709, 6607038)
JDK_TESTS=$JDK_HOME
# standard NetBeans unit and UI validation tests
ant -v -f nbbuild/build.xml -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER commit-validation
ERROR_CODE=$?

create_test_result "test.commit-validation" "Commit Validation" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Commit validation failed"
    #TEST_CODE=1;
fi

for TEST_SUITE in mobility.project j2ee.kit; do
    ant -f ${TEST_SUITE}/build.xml -Dtest.config=uicommit -Dbuild.test.qa-functional.results.dir=$NB_ALL/nbbuild/build/test/results -Dcontinue.after.failing.tests=true -Dtest-qa-functional-sys-prop.com.sun.aas.installRoot=/space/glassfish -Dtest-qa-functional-sys-prop.http.port=8090 -Dtest-qa-functional-sys-prop.wtk.dir=/space test
    ERROR_CODE=$?

    create_test_result "test.$TEST_SUITE" "Tests $TEST_SUITE" $ERROR_CODE
    if [ $ERROR_CODE != 0 ]; then
        echo "ERROR: $ERROR_CODE - ${TEST_SUITE}  failed"
        #TEST_CODE=1;
    fi
done
# Init application server for tests
#sh -x `dirname $0`/initAppserver.sh
# SOA (BPEL, XSLT) and XML UI validation tests
#for i in 1 2 3; do
#    ant -f xtest/instance/build.xml -Djdkhome=$JDK_TESTS -Dxtest.config=commit-validation-enterprise -Dxtest.instance.name="Enterprise tests" -Dxtest.no.cleanresults=true -Dnetbeans.dest.dir=$NB_ALL/nbbuild/test-netbeans runtests
#    ERROR_CODE=$?
#    if [ $ERROR_CODE = 0 ]; then
#        break;
#    fi
#done
#
#if [ $ERROR_CODE != 0 ]; then
#    echo "ERROR: $ERROR_CODE - SOA (BPEL, XSLT) and XML UI validation failed"
#    TEST_CODE=1;
#fi
# CND UI validation tests
#for i in 1 2 3; do
#    ant -f xtest/instance/build.xml -Djdkhome=$JDK_TESTS -Dxtest.config=commit-validation-cnd -Dxtest.instance.name="CND tests" -Dxtest.no.cleanresults=true -Dnetbeans.dest.dir=$NB_ALL/nbbuild/test-netbeans runtests
#    ERROR_CODE=$?
#    if [ $ERROR_CODE = 0 ]; then
#        break;
#    fi
#done
#
#if [ $ERROR_CODE != 0 ]; then
#    echo "ERROR: $ERROR_CODE - CND UI validation failed"
#    TEST_CODE=1;
#fi
# Profiler UI validation tests
#for i in 1 2 3; do
#    ant -f xtest/instance/build.xml -Djdkhome=$JDK_TESTS -Dxtest.config=commit-validation-profiler -Dxtest.instance.name="Profiler tests" -Dxtest.no.cleanresults=true -Dnetbeans.dest.dir=$NB_ALL/nbbuild/test-netbeans runtests
#    ERROR_CODE=$?
#    if [ $ERROR_CODE = 0 ]; then
#        break;
#    fi
#done

#if [ $ERROR_CODE != 0 ]; then
#    echo "ERROR: $ERROR_CODE - Profiler UI validation failed"
#    TEST_CODE=1;
#fi
# J2EE UI validation tests
#for i in 1 2 3; do
#    ant -f xtest/instance/build.xml -Djdkhome=$JDK_TESTS -Dxtest.config=commit-validation-j2ee -Dxtest.instance.name="J2EE tests" -Dxtest.no.cleanresults=true -D"xtest.userdata|com.sun.aas.installRoot"=$GLASSFISH_HOME -Dnetbeans.dest.dir=$NB_ALL/nbbuild/test-netbeans runtests
#    ERROR_CODE=$?
#    if [ $ERROR_CODE = 0 ]; then
#        break;
#    fi
#done

#if [ $ERROR_CODE != 0 ]; then
#    echo "ERROR: $ERROR_CODE - J2EE UI validation failed"
#    TEST_CODE=1;
#fi
# Mobility UI validation tests
#for i in 1 2 3; do
#    ant -f xtest/instance/build.xml -Djdkhome=$JDK_TESTS -Dxtest.config=commit-validation-mobility -Dxtest.instance.name="Mobility tests" -Dxtest.no.cleanresults=true -Dwtk.dir=/hudson runtests
#    ERROR_CODE=$?
#    if [ ERROR_CODE = 0 ]; then
#        break;
#    fi
#done
#ERROR_CODE=$?
#
#if [ $ERROR_CODE != 0 ]; then
#    echo "ERROR: $ERROR_CODE - Mobility UI validation failed"
#    TEST_CODE=1;
#fi
# UML UI validation tests
#for i in 1 2 3; do
#    ant -f xtest/instance/build.xml -Djdkhome=$JDK_TESTS -Dxtest.config=commit-validation-uml -Dxtest.instance.name="UML tests" -Dxtest.no.cleanresults=true -Dnetbeans.dest.dir=$NB_ALL/nbbuild/test-netbeans runtests
#    ERROR_CODE=$?
#    if [ $ERROR_CODE = 0 ]; then
#        break;
#    fi
#done
#
#if [ $ERROR_CODE != 0 ]; then
#    echo "ERROR: $ERROR_CODE - UML UI validation failed"
#    TEST_CODE=1;
#fi
# Ruby UI validation tests
#for i in 1 2 3; do
#    ant -f xtest/instance/build.xml -Djdkhome=$JDK_TESTS -Dxtest.config=commit-validation-ruby -Dxtest.instance.name="Ruby tests" -Dxtest.no.cleanresults=true -Dnetbeans.dest.dir=$NB_ALL/nbbuild/test-netbeans runtests
#    ERROR_CODE=$?
#    if [ $ERROR_CODE = 0 ]; then
#        break;
#    fi
#done
#
#if [ $ERROR_CODE != 0 ]; then
#    echo "ERROR: $ERROR_CODE - Ruby UI validation failed"
#    TEST_CODE=1;
#fi

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
rm -rf $NB_ALL/nbbuild/netbeans/nb?.*/servicetag

#Build XML modules
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml rebuild-cluster -Drebuild.cluster.name=nb.cluster.xml -Dbuild.compiler.debuglevel=source,lines
ERROR_CODE=$?

create_test_result "build.XML.modules" "Build XML modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build XML modules"
#    exit $ERROR_CODE;
fi

#Build UML modules
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml rebuild-cluster -Drebuild.cluster.name=nb.cluster.uml -Dbuild.compiler.debuglevel=source,lines
ERROR_CODE=$?

create_test_result "build.UML.modules" "Build UML modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build UML modules"
#    exit $ERROR_CODE;
fi

#Build VisualWeb modules
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml rebuild-cluster -Drebuild.cluster.name=nb.cluster.visualweb -Dbuild.compiler.debuglevel=source,lines
ERROR_CODE=$?

create_test_result "build.VisualWeb.modules" "Build VisualWeb modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build VisualWeb modules"
#    exit $ERROR_CODE;
fi

#Build the NB stableuc modules
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml rebuild-cluster -Drebuild.cluster.name=nb.cluster.stableuc -Dbuild.compiler.debuglevel=source,lines
ERROR_CODE=$?

create_test_result  "build.stableuc.modules" "Build stableuc modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build stableuc modules"
#    exit $ERROR_CODE;
fi

#Build JNLP
ant -Djnlp.codebase=http://bits.netbeans.org/trunk/jnlp/ -Djnlp.signjar.keystore=$KEYSTORE -Djnlp.signjar.alias=nb_ide -Djnlp.signjar.password=$STOREPASS -Djnlp.dest.dir=${DIST}/jnlp build-jnlp
ERROR_CODE=$?

create_test_result "build.jnlp" "Build JNLP" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build JNLP"
#    exit $ERROR_CODE;
fi

#Build all FU the NBMs
#ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-nbms -Dcluster.config=full -Dbase.nbm.target.dir=${DIST}/uc -Dkeystore=$KEYSTORE -Dstorepass=$STOREPASS -Dbuild.compiler.debuglevel=source,lines
#ERROR_CODE=$?

#create_test_result "build.NBMs" "Build NBMs" $ERROR_CODE
#if [ $ERROR_CODE != 0 ]; then
#    echo "ERROR: $ERROR_CODE - Can't build NBMs"
#    exit $ERROR_CODE;
#fi

#Build all NBMs for stable UC - IDE + UC-only
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-nbms -Dcluster.config=stableuc -Dbase.nbm.target.dir=${DIST}/uc2 -Dkeystore=$KEYSTORE -Dstorepass=$STOREPASS -Dbuild.compiler.debuglevel=source,lines
ERROR_CODE=$?

create_test_result "build.NBMs" "Build all NBMs" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build all stable UC NBMs"
#    exit $ERROR_CODE;
fi

# Separate IDE nbms from stableuc nbms.
ant -f nbbuild/build.xml move-ide-nbms -Dnbms.source.location=${DIST}/uc2 -Dnbms.target.location=${DIST}/uc
ERROR_CODE=$?

create_test_result "get.ide.NBMs" "Extract IDE NBMs from all the built NBMs" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't extract IDE NBMs"
#    exit $ERROR_CODE;
fi


#Build 110n kit for HG files
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml hg-l10n-kit -Dl10n.kit=${DIST}/zip/hg-l10n-$BUILDNUMBER.zip
ERROR_CODE=$?

create_test_result "build.hg.l10n" "Build 110n kit for HG files" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build l10n kits for HG files"
#    exit $ERROR_CODE;
fi

#Build l10n kit for IDE modules
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml l10n-kit -Dnbms.location=${DIST}/uc -Dl10n.kit=${DIST}/zip/ide-l10n-$BUILDNUMBER.zip
ERROR_CODE=$?

create_test_result "build.modules.l10n" "Build l10n kit for IDE modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build l10n kits for IDE modules"
#    exit $ERROR_CODE;
fi

#Build l10n kit for stable uc modules
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml l10n-kit -Dnbms.location=${DIST}/uc2 -Dl10n.kit=${DIST}/zip/stableuc-l10n-$BUILDNUMBER.zip
ERROR_CODE=$?

create_test_result "build.modules.l10n" "Build l10n kit for stable uc modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build l10n kits for stable uc modules"
#    exit $ERROR_CODE;
fi

cd nbbuild
Build catalog for IDE NBMs
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/uc -Dcatalog.file=${DIST}/uc/catalog.xml
ERROR_CODE=$?

create_test_result "build.ide.catalog" "Build UC catalog for IDE modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build UC catalog for IDE module"
#    exit $ERROR_CODE;
fi

Build catalog for Stable UC NBMs
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/uc2 -Dcatalog.file=${DIST}/uc2/catalog.xml
ERROR_CODE=$?

create_test_result "build.stableuc.catalog" "Build UC catalog for stable UC modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build UC catalog for stable UC modules"
#    exit $ERROR_CODE;
fi
cd ..

ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-test-dist -Dtest.fail.on.error=false -Dbuild.compiler.debuglevel=source,lines 
ERROR_CODE=$?

create_test_result "build.test.dist" "Build Test Distribution" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Building of Test Distrubution failed"
    exit $ERROR_CODE;
else
    mv nbbuild/build/testdist.zip $DIST/zip/testdist-${BUILDNUMBER}.zip
fi

ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/javadoctools/build.xml build-javadoc
ERROR_CODE=$?

create_test_result "build.javadoc" "Build javadoc" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Building of Javadoc Distrubution failed"
#    exit $ERROR_CODE;
else
    mv nbbuild/NetBeans-*-javadoc.zip $DIST/zip/$BASENAME-javadoc.zip
    cp -r nbbuild/build/javadoc $DIST/
fi

#ML_BUILD
if [ $ML_BUILD == 1 ]; then
    cd $NB_ALL
    hg clone $ML_REPO $NB_ALL/l10n
    cd $NB_ALL/l10n
    ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml -Dlocales=$LOCALES -Ddist.dir=$NB_ALL/nbbuild/netbeans-ml -Dnbms.dir=${DIST}/uc -Dnbms.dist.dir=${DIST}/ml/uc -Dkeystore=$KEYSTORE -Dstorepass=$STOREPASS build
    ERROR_CODE=$?

    create_test_result "build.ML.IDE" "Build ML IDE" $ERROR_CODE
    if [ $ERROR_CODE != 0 ]; then
        echo "ERROR: $ERROR_CODE - Can't build ML IDE"
#        exit $ERROR_CODE;
    fi

#    ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml -Dlocales=$LOCALES -Ddist.dir=$NB_ALL/nbbuild/netbeans-ml -Dnbms.dir=${DIST}/uc2 -Dnbms.dist.dir=${DIST}/ml/uc2 -Dkeystore=$KEYSTORE -Dstorepass=$STOREPASS build

#    create_test_result "build.ML.stableuc" "Build ML Stable UC modules" $ERROR_CODE
#    ERROR_CODE=$?
#    if [ $ERROR_CODE != 0 ]; then
#        echo "ERROR: $ERROR_CODE - Can't build ML Stable UC modules"
#        exit $ERROR_CODE;
#    fi

    if [ ! -z $UC_NBMS_DIR ]; then
       for UC_CLUSTER in $UC_EXTRA_CLUSTERS; do
          cp -r ${UC_NBMS_DIR}/${UC_CLUSTER} ${DIST}/ml/uc
       done
    fi

    cd $NB_ALL/nbbuild
    #Build catalog for ML FU NBMs
    ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/ml/uc -Dcatalog.file=${DIST}/ml/uc/catalog.xml
    ERROR_CODE=$?

    create_test_result "build.ML.FU.catalog" "Build ML FU catalog" $ERROR_CODE
    if [ $ERROR_CODE != 0 ]; then
        echo "ERROR: $ERROR_CODE - Can't build catalog FU for ML NBMs"
    #    exit $ERROR_CODE;
    fi

    #Build catalog for ML stable UC NBMs
#    create_test_result "build.ML.stableuc.catalog" "Build ML Stable UC catalog" $ERROR_CODE
#    ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/uml/uc2 -Dcatalog.file=${DIST}/ml/uc2/catalog.xml
#    ERROR_CODE=$?

#    if [ $ERROR_CODE != 0 ]; then
#        echo "ERROR: $ERROR_CODE - Can't build stable UC catalog for ML NBMs"
    #    exit $ERROR_CODE;
#    fi

    cp -r $NB_ALL/nbbuild/netbeans/* $NB_ALL/nbbuild/netbeans-ml/

    cd $NB_ALL/nbbuild
    #Remove the build helper files
    rm -f netbeans-ml/nb.cluster.*
#    rm -f netbeans-ml/build_info
    rm -rf netbeans-ml/extra
    rm -rf netbeans-ml/testtools
fi

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
    echo "ERROR: $ERROR_CODE - Can't build catalog FU for NBMs"
#    exit $ERROR_CODE;
fi


#Remove the build helper files
rm -f netbeans/nb.cluster.*
#rm -f netbeans/build_info
#rm -rf netbeans/extra
rm -rf netbeans/testtools
