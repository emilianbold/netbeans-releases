#!/bin/bash

# The contents of this file are subject to the terms of the Common Development
# and Distribution License (the License). You may not use this file except in
# compliance with the License.

# You can obtain a copy of the License at http://www.netbeans.org/cddl.html
# or http://www.netbeans.org/cddl.txt.

# When distributing Covered Code, include this CDDL Header Notice in each file
# and include the License file at http://www.netbeans.org/cddl.txt.
# If applicable, add the following below the CDDL Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"

# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.

set -e 

if [ -z "$1" ] || [ -z "$2" ] ; then
    echo "usage: $0 zipdir basename"
    echo ""
    echo "zipdir is the dir which contains the zip distros, e.g. nbbuild/dist"
    echo "basename is the distro filename prefix, e.g. netbeans-hudson-trunk-2464"
    echo "zipdir should contain <basename>.zip, <basename>-java.zip, <basename>-ruby.zip,..."
    echo Requires GLASSFISH_LOCATION, TOMCAT_LOCATION, OPENESB_LOCATION to be set. 
    exit 1
fi

zipdir=$1
basename=$2

progdir=`dirname $0`

dmgname=$basename

ant -f $progdir/build.xml distclean

# build GF package.  The GF dir image must already exists as $progdir/glassfish/glassfish
# ie after running java -Xmx256m -jar glassfish-installer.jar but before running ant -f setup.xml

#ant -f $progdir/glassfish/build.xml distclean build-pkg

# full download

ant -f $progdir/build.xml clean
mkdir $progdir/build
unzip -d $progdir/build $zipdir/$basename.zip
# remove uml, it has serious performance problem on Mac
rm -rf $progdir/build/netbeans/uml*
# remove mobility, there is no WTK on Mac
rm -rf $progdir/build/netbeans/mobility*
# copy over GlassFish.pkg
#mkdir -p $progdir build/pkg
#rsync -a $progdir/glassfish/build/pkg/ $progdir/build/pkg/
# build dmg
ant -f $progdir/build.xml -Ddmgname=$dmgname-macosx.dmg -Dnb.dir=$progdir/build/netbeans -Dnetbeans.appname="NetBeans 6.0 Dev" build-dmg -Dglassfish_location="$GLASSFISH_LOCATION" -Dtomcat_location="$TOMCAT_LOCATION" -Dopenesb_location="$OPENESB_LOCATION" -Dnetbeans_license_file="$progdir/licenses/NetBeans_6_Beta_1_Global_License.txt"

# javaee

ant -f $progdir/build.xml clean
mkdir $progdir/build
unzip -d $progdir/build $zipdir/$basename-javaee.zip
# copy over GlassFish.pkg
#mkdir -p $progdir build/pkg
#rsync -a $progdir/glassfish/build/pkg/ $progdir/build/pkg/
ant -f $progdir/build.xml -Ddmgname=$dmgname-javaee-macosx.dmg -Dnb.dir=$progdir/build/netbeans -Dnetbeans.appname="NetBeans 6.0 Dev" build-dmg  -Dglassfish_location="$GLASSFISH_LOCATION" -Dtomcat_location="$TOMCAT_LOCATION" -Dnetbeans_license_file="$progdir/licenses/NetBeans_6_Beta_1_WebAndJavaEE.txt"

# all others

for pkg in javase ruby cpp ; do
    dmg_postfix=$pkg
    license_file=pkg/license.txt
    if [ $pkg = cpp ]
    then
      license_file="licenses/NetBeans_6_Beta_1_CC++.txt"
    else
      if [ $pkg = javase ]
      then
        license_file="licenses/NetBeans_6_Beta_1_Java_SE.txt"
      else
        if [ $pkg = ruby ]
        then
          license_file="licenses/NetBeans_6_Beta_1_Ruby.txt"
        fi
      fi
    fi
    ant -f $progdir/build.xml clean
    mkdir $progdir/build
    unzip -d $progdir/build $zipdir/$basename-$pkg.zip
    ant -f $progdir/build.xml -Ddmgname=$dmgname-$dmg_postfix-macosx.dmg -Dnb.dir=$progdir/build/netbeans -Dnetbeans.appname="NetBeans 6.0 Dev" build-dmg -Dnetbeans_license_file="$progdir/$license_file"
done

