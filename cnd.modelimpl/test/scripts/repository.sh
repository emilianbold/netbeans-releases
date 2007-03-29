#!/bin/bash

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

TEST_PROJECT="$1"
PROJECT_TEMP_FILE=/tmp/reptest.${RANDOM}
echo ${PROJECT_TEMP_FILE}
echo "Running without repository..."
./_parse_project.sh ${TEST_PROJECT} -fq  -J-Dcnd.repository.use.hardcache=true  > ${PROJECT_TEMP_FILE}.original 
#2>&1
echo "Running with repository..."
./_parse_project.sh ${TEST_PROJECT} -fq --cleanrepository -J-Dcnd.repository.workaround.nulldata=true> ${PROJECT_TEMP_FILE}.repository 
#2>&1

diff ${PROJECT_TEMP_FILE}.original ${PROJECT_TEMP_FILE}.repository > ${PROJECT_TEMP_FILE}.diff
echo To see detailed results do
echo cat ${PROJECT_TEMP_FILE}.diff

echo "Lines in diff file:"
cat  ${PROJECT_TEMP_FILE}.diff | wc -l




