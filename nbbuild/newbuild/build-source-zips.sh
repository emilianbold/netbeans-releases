set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

cd  $NB_ALL

#build source zip files for particular modules
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-source-zips
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build source zips"
#    exit $ERROR_CODE;
fi
