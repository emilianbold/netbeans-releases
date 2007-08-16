#!/bin/bash

set -e 

if [ -z "$1" ] || [ -z "$2" ] ; then
    echo "usage: $0 zipdir basename"
    echo ""
    echo "zipdir is the dir which contains the zip distros, e.g. nbbuild/dist"
    echo "basename is the distro filename prefix, e.g. netbeans-hudson-trunk-2464"
    echo "zipdir should contain <basename>.zip, <basename>-java.zip, <basename>-ruby.zip,..."
    exit 1
fi

zipdir=$1
basename=$2

progdir=`dirname $0`

ant -f $progdir/build.xml distclean

# build GF package.  The GF dir image must already exists as $progdir/glassfish/glassfish
# ie after running java -Xmx256m -jar glassfish-installer.jar but before running ant -f setup.xml

ant -f $progdir/glassfish/build.xml distclean build-pkg

# full download

ant -f $progdir/build.xml clean
mkdir $progdir/build
unzip -d $progdir/build $zipdir/$basename.zip
# remove uml, it has serious performance problem on Mac
rm -rf $progdir/build/netbeans/uml*
# remove mobility, there is no WTK on Mac
rm -rf $progdir/build/netbeans/mobility*
# copy over GlassFish.pkg
mkdir -p $progdir build/pkg
rsync -a $progdir/glassfish/build/pkg/ $progdir/build/pkg/
# build dmg
ant -f $progdir/build.xml -Ddmgname=$basename.dmg -Dnb.dir=$progdir/build/netbeans build-dmg

ant -f $progdir/build.xml clean
mkdir $progdir/build
unzip -d $progdir/build $zipdir/$basename-java.zip
ant -f $progdir/build.xml -Ddmgname=$basename-java.dmg -Dnb.dir=$progdir/build/netbeans build-dmg 

ant -f $progdir/build.xml clean
mkdir $progdir/build
unzip -d $progdir/build $zipdir/$basename-ruby.zip
ant -f $progdir/build.xml -Ddmgname=$basename-ruby.dmg -Dnb.dir=$progdir/build/netbeans build-dmg 

ant -f $progdir/build.xml clean
mkdir $progdir/build
unzip -d $progdir/build $zipdir/$basename-javaee.zip
# copy over GlassFish.pkg
mkdir -p $progdir build/pkg
rsync -a $progdir/glassfish/build/pkg/ $progdir/build/pkg/
ant -f $progdir/build.xml -Ddmgname=$basename-javaee.dmg -Dnb.dir=$progdir/build/netbeans build-dmg 
