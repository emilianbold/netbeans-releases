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

if [ -z "$jdkhome" ] ; then
    # try to find JDK

    # read Java Preferences
    if [ -x "/usr/libexec/java_home" ]; then
        jdkhome=`/usr/libexec/java_home --version 1.6+`

    # JDK1.7
    elif [ -f "/Library/Java/JavaVirtualMachines/1.7.0.jdk/Contents/Home/bin/java" ] ; then
        jdkhome="/Library/Java/JavaVirtualMachines/1.7.0.jdk/Contents/Home"

    # JDK1.6
    elif [ -f "/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/java" ] ; then
        jdkhome="/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home"
    elif [ -f "/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home/bin/java" ] ; then
        jdkhome="/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home"
    elif [ -f "/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home/bin/java" ] ; then
        jdkhome="/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home"
    fi

fi

echo $jdkhome
