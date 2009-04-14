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

default_jdk="/System/Library/Frameworks/JavaVM.framework/Versions/1.5/Home"

java_bin=`which java 2>&1`

if [ $? -ne 0 ] || [ -n "`echo \"$java_bin\" | grep \"no java in\"`" ] ; then
    # no java in path... strange
    java_bin=/usr/bin/java
fi

if [ -f "$java_bin" ] ; then
    java_version=`"$java_bin" -fullversion 2>&1`
    if [ $? -eq 0 ] && [ -n "`echo \"$java_version\" | grep 1.6.0`" ] ; then 
        # don`t use Developer Preview versions
        if [ -z "`echo \"$java_version\" | grep \"1.6.0_b\|1.6.0-b\|1.6.0_01\|1.6.0_04\|-dp\"`" ] ; then
            if [ -f "/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home/bin/java" ] ; then
		default_jdk="/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home"
            elif [ -f "/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home/bin/java" ] ; then
                default_jdk="/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home"
            fi
        fi	
    fi
fi

echo "$default_jdk"
