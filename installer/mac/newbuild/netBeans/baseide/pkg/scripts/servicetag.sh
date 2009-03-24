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


script_dir=`dirname "$0"`

. "$script_dir"/env.sh

jdk_home=`"$script_dir"/get_current_jdk.sh`

java_exe="$jdk_home/bin/java"

if [ -n "$1" ] ; then

    netbeansHome=`ls -d "$NETBEANS_INSTALL_DIR"/Contents/Resources/NetBeans*/platform*`
    install_dir="$2"


    "$java_exe" -Dnetbeans.home="$netbeansHome" -Dlocation="$install_dir" -Dservicetag.source="$SERVICE_TAG_SOURCE" -jar "$script_dir"/servicetag.jar "$1"

    errorCode=$?


    if [ $errorCode -ne 0 ] ; then   
	echo "SERVICE TAGS: Cannot create ST for $1, error code is $errorCode"
    else
	echo "SERVICE TAGS: ST for $1 was successfully created"
    fi

    ownership=`ls -nlda ~ | awk ' { print $3 ":" $4 } ' 2>/dev/null`
    [ -d ~/.netbeans ]  && chown "$ownership" ~/.netbeans
    [ -d ~/.netbeans-registration ]  && chown -R "$ownership" ~/.netbeans-registration
    [ -f ~/.netbeans/.superId ]  && chown "$ownership" ~/.netbeans/.superId

fi

