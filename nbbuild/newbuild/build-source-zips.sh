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
    exit $ERROR_CODE;
fi

###################################################################
#
# Deploy sources to the storage server
#
###################################################################

if [ -n $BUILD_ID ]; then
    mkdir -p $DIST_SERVER2/source-zips/${BUILD_ID}
    cp -rp $NB_ALL/nbbuild/build/source-zips/*  $DIST_SERVER2/source-zips/${BUILD_ID}
    rm $DIST_SERVER2/source-zips/latest.old
    mv $DIST_SERVER2/source-zips/latest $DIST_SERVER2/source-zips/latest.old
    ln -s $DIST_SERVER2/source-zips/${BUILD_ID} $DIST_SERVER2/source-zips/latest
fi
