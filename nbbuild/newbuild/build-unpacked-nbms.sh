set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

cd  $NB_ALL

#Build napackaged NBMs for stable UC - IDE + UC-only
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-nbms -Dcluster.config=stableuc -Duse.pack200=false -Dbase.nbm.target.dir=${DIST}/uc-unpackaged -Dkeystore=$KEYSTORE -Dstorepass=$STOREPASS -Dbuild.compiler.debuglevel=source,lines
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build unpackaged all stable UC NBMs"
#    exit $ERROR_CODE;
fi

cd nbbuild
Build catalog for unpackaged NBMs
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/uc-unpackaged -Dcatalog.file=${DIST}/uc-unpackaged/catalog.xml
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build stable UC catalog for unpackaged NBMs"
    exit $ERROR_CODE;
fi
cd ..

