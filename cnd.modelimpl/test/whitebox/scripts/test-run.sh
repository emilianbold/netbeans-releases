#!/bin/bash 

#
# The contents of this file are subject to the terms of the Common Development
# and Distribution License (the License). You may not use this file except in
# compliance with the License.
#
# You can obtain a copy of the License at http://www.netbeans.org/cddl.html
# or http://www.netbeans.org/cddl.txt.
#
# When distributing Covered Code, include this CDDL Header Notice in each file
# and include the License file at http://www.netbeans.org/cddl.txt.
# If applicable, add the following below the CDDL Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.
#


run() {
echo start dump
	local main="ddiff.DDiffMain"
        local temp=${DWARF_TEST_TEMP:-/tmp}
echo "temp=$temp"
        local all_std=${DWARF_LOG}
        local all_err=${DWARF_ALL_ERR:-${temp}/_all_err.txt}
	echo "Running test $@"
	java -cp ${DWARF_TEST_CLASSPATH} ${DWARF_TEST_JVMOPT} ${DWARF_ADDITIONAL_JVM_OPT} -Dparser.report.errors=true ${main} -t ${temp} -h ${DWARF_TEST_CONFIG_PREFIX}.gcc  2>>$all_err
	rc=$?
	if [ ${rc} != 0 ] ; then
	    echo "Error in java program: RC= ${rc}"
	fi
}

run | tee -a $DWARF_LOG
