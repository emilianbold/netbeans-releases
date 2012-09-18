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

set -x -e 

echo Given parameters: $1 $2 $3 $4 $5 $6

if [ -z "$1" ] || [ -z "$2" ]|| [ -z "$3" ] || [ -z "$4" ] || [ -z "$5" ] || [ -z "$6" ]; then
    echo "usage: $0 zipdir prefix buildnumber build_jdk7 signing_identity [nb_locales]"
    echo ""
    echo "zipdir is the dir which contains the zip/modulclusters
    echo "prefix is the distro filename prefix, e.g. netbeans-hudson-trunk in netbeans-hudson-trunk-2464"
    echo "buildnumber is the distro buildnumber, e.g. 2464 in netbeans-hudson-trunk-2464"
    echo "build_jdk7 is 1 if bundle jdk7 are required and 0 if not"
    echo "signing_identity is a digital identity for signing the OS X installer or 0 if not signing"
    echo "nb_locales is the string with the list of locales
    exit 1
fi

work_dir=$1
prefix=$2
buildnumber=$3
build_jdk7=$4
signing_identity=$5
if [ -n "$6" ] ; then
  nb_locales=",$6"
fi

basename=`dirname "$0"`

if [ -f "$basename"/build-private.sh ]; then
  . "$basename"/build-private.sh
fi

cd "$basename"
chmod -R a+x *.sh

commonname=$work_dir/zip/moduleclusters/$prefix-$buildnumber 
if [ -z $build_jdk7 ] || [ 0 -eq $build_jdk7 ] ; then
    target="build-all-dmg"
    build_jdk7=0
else
    target="build-jdk-bundle-dmg"
fi

if [ -z $en_build ] ; then
    en_build=1
fi

if [ 0 -eq "${signing_identity}" ] ; then
    signing_identity=0
fi

rm -rf "$basename"/dist_en
ant -f $basename/build.xml $target -Dlocales=$nb_locales -Dcommon.name=$commonname -Dprefix=$prefix -Dbuildnumber=$buildnumber  -Dsigning_identity="${signing_identity}" -Dbuild.jdk7=$build_jdk7 -Dgf_builds_host=$GLASSFISH_BUILDS_HOST -Djdk_builds_host=$JDK_BUILDS_HOST -Dopenesb_builds_host=$OPENESB_BUILDS_HOST -Dbinary_cache_host=$BINARY_CACHE_HOST
mv -f "$basename"/dist "$basename"/dist_en
