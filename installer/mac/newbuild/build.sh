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

if [ -z "$1" ] || [ -z "$2" ]|| [ -z "$3" ] || [ -z "$4" ]; then
    echo "usage: $0 zipdir prefix buildnumber ml_build [nb_locales]"
    echo ""
    echo "zipdir is the dir which contains the zip/modulclusters and zip-ml/moduleclusters"
    echo "prefix-buildnumber is the distro filename prefix, e.g. netbeans-hudson-trunk-2464"
    echo "ml_build is 1 if ml builds are requared and 0 if not"  
    echo "nb_locales is the string with the list of locales (for ml builds)"  
    exit 1
fi

work_dir=$1
prefix=$2
buildnumber=$3
ml_build=$4
if [ -n "$5" ] ; then
  nb_locales="$5"
fi
  
basename=`dirname "$0"`
. "$basename"/build-private.sh

cd "$basename"
chmod -R a+x *.sh

commonname=$work_dir/zip/moduleclusters/$prefix-$buildnumber 
ant -f $basename/build.xml build-all-dmg -Dcommon.name=$commonname -Dprefix=$prefix -Dbuildnumber=$buildnumber -Dmlbuild='false' -Dgf_builds_host=$GLASSFISH_BUILDS_HOST -Dopenesb_builds_host=$OPENESB_BUILDS_HOST -Dbinary_cache_host=$BINARY_CACHE_HOST

rm -rf "$basename"/dist_en
mv -f "$basename"/dist "$basename"/dist_en

if [ 1 -eq $ml_build ] ; then
commonname_ml=$work_dir/zip-ml/moduleclusters/$prefix-$buildnumber
ant -f $basename/build.xml build-all-dmg -Dnb.locales=$nb_locales -Dcommon.name=$commonname_ml -Dprefix=$prefix -Dbuildnumber=$buildnumber -Dmlbuild='true' -Dgf_builds_host=$GLASSFISH_BUILDS_HOST -Dopenesb_builds_host=$OPENESB_BUILDS_HOST -Dbinary_cache_host=$BINARY_CACHE_HOST
fi
