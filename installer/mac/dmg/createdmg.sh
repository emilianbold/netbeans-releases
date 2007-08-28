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

if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ]; then
    echo "usage: $0 srcdir dmgname tmpdir"
    exit 1
fi

dmg=$1
tmpdir=$2
shift 2
srcdirs=$*

volname="NetBeans 6.0"

rm -f $tmpdir/template.sparseimage
bunzip2 -d -c `dirname $0`/template.sparseimage.bz2 > $tmpdir/template.sparseimage
rm -rf $tmpdir/mountpoint
mkdir $tmpdir/mountpoint
hdiutil mount -mountpoint $tmpdir/mountpoint $tmpdir/template.sparseimage
rsync -a $srcdirs --exclude .DS_Store $tmpdir/mountpoint/
diskutil rename $tmpdir/mountpoint "$volname"
hdiutil unmount $tmpdir/mountpoint
rm -f "$dmg"
hdiutil create -srcdevice $tmpdir/template.sparseimage "$dmg"
rm -f $tmpdir/template.sparseimage
rmdir $tmpdir/mountpoint
