#!/bin/bash
set -x
cd ${WORKSPACE}
rm -rf installer nbi nbextracted zipdist
hg up

cd $LAST_BITS

BUILD_NUMBER=`ls | grep netbeans | cut -f 3 -d "-" | uniq`

cd ${WORKSPACE}
#ssh $NATIVE_MAC_MACHINE rm -rf $MAC_PATH/installer
#ssh $NATIVE_MAC_MACHINE mkdir -p $MAC_PATH/installer
#gtar c installer/mac | ssh $NATIVE_MAC_MACHINE "( cd $MAC_PATH; tar x )"
#ssh $NATIVE_MAC_MACHINE rm -rf $MAC_PATH/zip/*

EMMA_DIR=${WORKSPACE}/../emma
EMMA_SH="$EMMA_DIR/emma.sh"
EMMA_TXT="$EMMA_DIR/emma.txt"
EMMA_JAR="$EMMA_DIR/emma.jar"

EXTRACTED_DIR=$BASE_DIR/nbextracted

mkdir -p ${EXTRACTED_DIR}
NB_EXTRACTED=${EXTRACTED_DIR}/netbeans

unzip -d ${EXTRACTED_DIR} ${LAST_BITS}/${BUILD_DESC}-${BUILD_NUMBER}-all-in-one.zip
mkdir ${NB_EXTRACTED}/emma-lib
chmod a+w ${NB_EXTRACTED}/emma-lib
cp ${EMMA_JAR} ${NB_EXTRACTED}/emma-lib/
cp ${EMMA_TXT} ${NB_EXTRACTED}/emma-lib/netbeans_coverage.em

cd ${BASE_DIR}
bash ${EMMA_SH} ${NB_EXTRACTED} ${NB_EXTRACTED}/emma-lib/netbeans_coverage.em ${EMMA_JAR}

sed -i -e "s/^netbeans_default_options=\"/netbeans_default_options=\"--cp:p \"\\\\\"\"$\{NETBEANS_HOME\}\/emma-lib\/emma.jar\"\\\\\"\" -J-Demma.coverage.file=\"\\\\\"\"$\{NETBEANS_HOME\}\/emma-lib\/netbeans_coverage.ec\"\\\\\"\" -J-Dnetbeans.security.nocheck=true /" ${NB_EXTRACTED}/etc/netbeans.conf

BASENAME=${BUILD_DESC}-${BUILD_NUMBER}
export DIST=${WORKSPACE}/dist/zip
mkdir -p ${DIST}
cd ${NB_EXTRACTED}
expat='extra|testtools'
for c in platform ide java apisupport harness enterprise profiler visualweb ruby mobility soa xml cnd identity gsf php groovy webcommon websvccommon; do
    find * | egrep "^$c[0-9]*/" | zip -q $DIST/$BASENAME-$c.zip -@ || exit
    expat="$expat|$c[0-9]*"
done
find * | egrep -v "^($expat)(/|$)" | zip -q $DIST/$BASENAME-nb6.5-etc.zip -@ || exit
cd ${NB_EXTRACTED_DIR}
zip -q $DIST/$BASENAME-all-in-one.zip netbeans
cd ${WORKSPACE}

rm -rf ${EXTRACTED_DIR}

#ssh $NATIVE_MAC_MACHINE mkdir -p $MAC_PATH/zip/moduleclusters
#scp -q -v ${DIST}/*.zip $NATIVE_MAC_MACHINE:$MAC_PATH/zip/moduleclusters/
#scp -q -v ${BASE_DIR}/../build-private.sh $NATIVE_MAC_MACHINE:$MAC_PATH/installer/mac/newbuild
#ssh $NATIVE_MAC_MACHINE sh $MAC_PATH/installer/mac/newbuild/build.sh $MAC_PATH/zip/moduleclusters ${BUILD_DESC} $BUILD_NUMBER 0

#cd ${BASE_DIR}/installer/infra/build
#bash build.sh

#scp $NATIVE_MAC_MACHINE:$MAC_PATH/installer/mac/newbuild/dist/* ${WORKSPACE}/dist/installers/bundles
