set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

#Clean destination dirs
if [ -d $DIST ]; then
    rm -rf $DIST
fi

if [ -d $NB_ALL ]; then
    rm -rf $NB_ALL
fi
