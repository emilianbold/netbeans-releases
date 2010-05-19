#!/bin/bash

#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
#
# Oracle and Java are registered trademarks of Oracle and/or its affiliates.
# Other names may be trademarks of their respective owners.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html
# or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License file at
# nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the GPL Version 2 section of the License file that
# accompanied this code. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# Contributor(s):
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.
#
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 2, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 2] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 2 code and therefore, elected the GPL
# Version 2 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.
#


run() {
echo start dump
	local main="dwarfvsmodel.DwarfVsModelMain"
        local temp=${DWARF_TEST_TEMP:-/tmp}
echo "temp=$temp"
        local all_std=${DWARF_LOG}
        local all_err=${DWARF_ALL_ERR:-${temp}/_all_err.txt}
	echo "Running test $@"
        pwd
        #java -cp ${DWARF_TEST_CLASSPATH} ${DWARF_TEST_JVMOPT} ${DWARF_ADDITIONAL_JVM_OPT} -Dparser.report.errors=true ${main} -t ${temp} -h ${DWARF_TEST_CONFIG_PREFIX}.gcc  2>>$all_err
        # -h ${DWARF_TEST_CONFIG_PREFIX}.gcc
	java -cp ${DWARF_TEST_CLASSPATH} ${DWARF_TEST_JVMOPT} ${DWARF_ADDITIONAL_JVM_OPT} -Dparser.report.errors=true -Dcnd.repository.hardrefs=true ${main} -t ${temp} -c ${DWARF_TEST_CONFIG_PREFIX}.gcc 2>>$all_err

        rc=$?
	if [ ${rc} != 0 ] ; then
	    echo "Error in java program: RC= ${rc}"
	fi
}

run | tee -a $DWARF_LOG
