#!/bin/bash

pack_component() 
{
    dist=$1
    base_name=$2
    component=$3
    filter=$4
    zip -r $dist/zip/$base_name-$component.zip $filter
    gtar cvzf $dist/targz/$base_name-$component.tar.gz $filter
    gtar cvjf $dist/targz/$base_name-$component.tar.bz2 $filter
}

ANT_OPTS="-Xmx512m"

if [ -z $NB_ALL ]; then
    NB_ALL=/space/NB-IDE/nb-all
fi

mkdir -p $NB_ALL

cd  $NB_ALL

for i in `ls | grep -v "CVS"`; do
    cvspurge $i;
done

cvs -d :pserver:anoncvs@cvs.netbeans.org:/cvs checkout -D '00:00UTC today' nbbuild
ant -f nbbuild/build.xml checkout
cvs -d :pserver:anoncvs@cvs.netbeans.org:/cvs checkout -D '00:00UTC today' mobility uml visualweb scripting enterprise print

DATESTAMP=`date -u +%Y%m%d%M`
BUILDNUM=trunk-nightly-all-$DATESTAMP

ant -Dbuildnum=$BUILDNUM -f nbbuild/build.xml build-nozip
ERROR_CODE=$?
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build basic IDE"
    exit $ERROR_CODE;
fi
ant -Dnb_all=$NB_ALL -f visualweb/ravebuild/build.xml
ant -f mobility/build.xml build
ant -f uml/build.xml build
ant -f scripting/ruby/build.xml build
ant -f nbbuild/entpack/build.xml build

cd nbbuild
mkdir -p dist/zip
mkdir -p dist/targz
mkdir -p dist/tarbz2

echo "To be written" > dist/INSTALL.txt

rm -f netbeans/nb.cluster.*
rm -f netbeans/moduleCluster.properties
rm -f netbeans/module_tracking.xml
rm -f netbeans/build_info

DIST=$NB_ALL/nbbuild/dist
BASENAME=netbeans-$BUILDNUM

pack_component $DIST $BASENAME all-in-one netbeans

cd netbeans

pack_component $DIST $BASENAME uml "uml*"
rm -rf uml*

pack_component $DIST $BASENAME visualweb "visulalweb*"
rm -rf visualweb*

pack_component $DIST $BASENAME ruby "ruby*"
rm -rf ruby*

pack_component $DIST $BASENAME profiler "profiler*"
rm -rf profiler*

pack_component $DIST $BASENAME platform "platform*"
rm -rf platform*

pack_component $DIST $BASENAME mobility "mobility*"
rm -rf mobility*

pack_component $DIST $BASENAME ide "ide*"
rm -rf ide*

pack_component $DIST $BASENAME harness "harness*"
rm -rf harness*

pack_component $DIST $BASENAME enterprise "enterprise*"
rm -rf enterprise*

pack_component $DIST $BASENAME soa "soa*"
rm -rf soa*

pack_component $DIST $BASENAME nb6.0-etc "*"
