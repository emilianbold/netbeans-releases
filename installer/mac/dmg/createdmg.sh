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

if [ -z "$1" ] || [ -z "$2" ] ; then
    echo "usage: $0 srcdir dmgname"
    exit 1
fi

srcdir=$1
dmg=$2
volname="NetBeans 6.0"

rm -f /tmp/template.sparseimage
bunzip2 -d -c `dirname $0`/template.sparseimage.bz2 > /tmp/template.sparseimage
hdiutil mount /tmp/template.sparseimage
rsync -a "$srcdir/" --exclude .DS_Store /Volumes/template
diskutil rename /Volumes/template "$volname"
hdiutil unmount "/Volumes/$volname"
rm -f "$dmg"
hdiutil create -srcdevice /tmp/template.sparseimage "$dmg"
rm -f /tmp/template.sparseimage
