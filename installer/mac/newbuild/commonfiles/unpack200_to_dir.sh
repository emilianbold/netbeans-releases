#!/bin/sh -x

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

unpack_dir="$1"
tmp_dir="$2"
jdk_home="$3"

if [ ! -d "$tmp_dir" ] ; then
    mkdir -p "$tmp_dir"
fi

echo Calling unpack200 in "$unpack_dir". Saving result in "$tmp_dir"
cd "$unpack_dir"
for x in `find . -name \*.jar.pack` ; do
    jar_subpath=`echo $x | sed 's/jar.pack/jar/;s/^.\///'`
    jar="$tmp_dir"/"$jar_subpath"   
    mkdir -p `dirname "$jar"`
    echo "Unpack file $x into $tmp_dir / $jar_subpath"
    /System/Library/Frameworks/JavaVM.framework/Versions/1.5/Home/bin/unpack200 "$x" "$jar"
done

exit 0
