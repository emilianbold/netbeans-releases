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
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
# Microsystems, Inc. All Rights Reserved.

#set -x

MWS_USERDIRS="/export/home/nbuserdirs"
TIMEOUT=900
#MEMORY_SET="128"
#MEMORY_SET="1024 512 256 192 128"
MEMORY_SET="512 256 192 128"
PROJECTS_SET="boost+mysql boost"

export LOGPATH=/tmp/mwstest/${LOGNAME-generic}/`date +%y%m%d%H%M`

if [ ! -d ${LOGPATH} ]; then
    mkdir -p ${LOGPATH}
fi

export XMLOUTPUT=${LOGPATH}/results.xml

#TEST_SRC=/set/ide/mars/spb/testcode/ddd-3.3.11/
TEST_SRC=~/SunStudioProjects/Args1/

echo logs path: ${LOGPATH}

function sfstest() 
{
    if [ ! -n "${sfstestname}" ]; then
        echo "sfstest failed. No name provided"
        return
    fi

    echo "<test name='Disk repository ${sfstestname}'>" >> ${XMLOUTPUT}
    pushd . > /dev/null
    cd ./sfs
    run-${sfstestname}.sh $TEST_SRC --noant > ${LOGPATH}/disk-${sfstestname}.log 2>&1
    if [ $? -eq 0 ]; then
        result="passed"
    else
        result="failed"
    fi
    echo "Disk repository ${sfstestname} test - ${result}"
    echo "<log name='Disk repository ${sfstestname} test log'>${LOGPATH}/disk-${sfstestname}.log</log>" >> ${XMLOUTPUT}
    echo "<result>${result}</result>" >> ${XMLOUTPUT}
    echo "</test>" >> ${XMLOUTPUT}
    popd > /dev/null
}

function repositorytest() {
    printf "%s" "Repository correctness test - "
    echo "<test name='Repository correctness'>" >> ${XMLOUTPUT}
    pushd . > /dev/null
    cd ./../../modelimpl/test/scripts/
    echo "<log name='Repository correctness test log'>${LOGPATH}/repository-correctness.log</log>" >> ${XMLOUTPUT}
    repository.sh $TEST_SRC > ${LOGPATH}/repository-correctness.log
    popd > /dev/null
    echo "</test>" >> ${XMLOUTPUT}
    echo "done."
}

function mws() {
    pushd . > /dev/null
    cd ./../..
    
    for p in ${PROJECTS_SET}
    do
        MWSPROJECT=$p
        for i in ${MEMORY_SET}
        do
            MEM=$i
            runcnd
        done
    done
    popd > /dev/null
}

function runcnd() {
    if [ "true" = "${USE_REPOSITORY}" ]; then
        REPPARAMS="-J-Dcnd.repository.hardrefs=false -J-Dcnd.repository.cache.path=/export/home/cache-row7/ -J-Dcnd.repository.delete.cache.files=false"
    else
        REPPARAMS="-J-Dcnd.repository.hardrefs=true"
    fi
    printf "%s" "MWS. Project: ${MWSPROJECT}. Memory: ${MEM}Mb. Repository: ${USE_REPOSITORY} - "
    PARAMS="-J-Dcnd.close.ide.after.parse=true"
    PARAMS="${PARAMS} -J-Dcnd.close.ide.timeout=${TIMEOUT}"
    PARAMS="${PARAMS} -J-Dcnd.close.report.xml=${XMLOUTPUT}"
    RUNLINE="run.sh  --userdir ${MWS_USERDIRS}/${MWSPROJECT} -J-Xmx${MEM}M ${REPPARAMS}"

    echo "<test name='Memory Working Set'>" >> ${XMLOUTPUT}
    echo "<param name='project' value='${MWSPROJECT}'/>" >> ${XMLOUTPUT}
    echo "<param name='repository' value='${USE_REPOSITORY}'/>" >> ${XMLOUTPUT}
    echo "<param name='memory' value='${MEM} Mb'/>" >> ${XMLOUTPUT}
    echo "<param name='run-line' value='${RUNLINE}'/>" >> ${XMLOUTPUT}
    #echo ${RUNLINE} 
    LOGNAME="${LOGPATH}/mws-rep${USE_REPOSITORY}-${MEM}.log"
    ${RUNLINE} ${PARAMS} > ${LOGNAME} 2>&1
    echo "<log name='mws log'>${LOGNAME}</log>" >> ${XMLOUTPUT}
    echo "</test>" >> ${XMLOUTPUT}
    echo "done."
}

echo "<repository-tests>" > ${XMLOUTPUT}

# Disk repository correctness test
sfstestname=correctness
sfstest

# Disk repository threading stress test
sfstestname=threading
sfstest

# Repository correctness test
repositorytest

# Memory working set with repository
USE_REPOSITORY=true
mws

# Memory working set withour repository
USE_REPOSITORY=false
mws

echo "</repository-tests>" >> ${XMLOUTPUT}

#cat ${XMLOUTPUT}