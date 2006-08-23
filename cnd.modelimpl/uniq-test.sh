#!/bin/sh 

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
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
# Microsystems, Inc. All Rights Reserved.

DIR=${REGRESSION_SRC}
FILES=`(cd ${DIR}; ls *.ppp *.cc *.cpp *.c)`
#FILES=`(cd ${DIR}; ls *.cc)`
TEMP="/tmp/uniq"

rm -rf ${TEMP} > /dev/null
mkdir -p ${TEMP} > /dev/null

failures="${TEMP}/__failures"
rm -rf ${failures} > /dev/null

for F in ${FILES}; do 
 	file_std="${TEMP}/${F}.dat"
 	file_err="${TEMP}/${F}.err"
	./tracemodel.sh ${DIR}/${F} -fu >  ${file_std} 2>${file_err}
	err=`cat ${file_err}`
	if [ -z "${err}" ]; then
		rm ${file_err}
	fi
	cnt=`grep "Unique name check failed" ${file_std} | wc -l`
	text="${F} ${cnt}"
	echo ${text}
	if [ ${cnt} -gt 0 ]; then
		echo ${text} >> "${failures}"
	fi
done

echo "FAILURES:"
cat ${failures}
