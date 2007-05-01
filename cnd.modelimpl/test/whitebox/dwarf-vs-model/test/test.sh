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

function params() {

    while [ -n "$1" ]
    do
	case "$1" in
	    -J*)
		    DEFS="${DEFS} ${1#-J}"
		    ;;
	    --nb)
		    shift
		    echo "Using NB from $1"
		    NBDIST=$1
		    ;;
	    --jdk)
		    shift
		    JAVA=$1/bin/java
		    ;;
	    --cnd)
		    shift
		    echo "Using NB from $1"
		    CNDDIST=$1
		    ;;
	    -debug|--debug)
		    echo "debugging on port 5858"
		    DEBUG_PROFILE="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5858"
		    ;;
	    --sdebug|-sdebug)
		    echo "wait to attach debugger on port 5858"
		    DEBUG_PROFILE="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5858"
		    ;;
	    --profile|-profile)
		    echo "profile on port 5140"
		    DEBUG_PROFILE="-agentpath:/opt/netbeans/5.0/profiler1/lib/deployed/jdk15/solaris-i386/libprofilerinterface.so=/opt/netbeans/5.0/profiler1/lib,5140"
		    ;;
	    --yprofile|-yprofile)
		    echo "profile using YourKit Profiler"
		    DEBUG_PROFILE="-agentlib:yjpagent=dir=${HOME}/yjp_data"
		    ;;
	    --ycpu|-ycpu)
		    echo "profile using YourKit Profiler with CPU sampling"
		    DEBUG_PROFILE="-agentlib:yjpagent=sampling,noj2ee,dir=${HOME}/yjp_data"
		    ;;
	    --tt|--tracetree)
	    	    DEFS="${DEFS} -Dtrace.trees=true"
		    ;;
	    --tc|--tracecomp)
	    	    DEFS="${DEFS} -Dtrace.comparison=true"
		    ;;
	    --tn|--tracecounter)
	    	    DEFS="${DEFS} -Dtrace.counter=true"
		    ;;
	    --te|--traceentries)
	    	    DEFS="${DEFS} -Dtrace.entries=true"
		    ;;
	    --nc|--nocompile)
	    	    COMPILE="N"
		    ;;
	    --cfg|--config)
	    	    shift
		    CONFIG_FILE="$1"
		    ;;
	    *)
		    OPTIONS="${OPTIONS} $1"
		    ;;
	esac
	shift
    done

}

function main() {

    DEFS=""
    CONFIG_FILE="test.cfg"
    OPTIONS=""
    DEBUG_PROFILE=""
    ONLY=""
    COMPILE="Y"
    
    params $@

    pwd=`pwd`
        
    cfg="test.gcc"
    if [ -r ${cfg} ]; then rm ${cfg}; fi
    echo "Using config file ${CONFIG_FILE}"

    cat ${CONFIG_FILE} | while read line
    do
        if [ `expr match "${line}" "#"` -eq 0 ]; then
	    cmd=`echo "${line}" | awk '{print $1}'`
	    args=${line#${cmd}}

	    if [ ${COMPILE} = "Y" ]; then
		echo "Compiling: ${cmd} ${args}"
		eval "${cmd} ${args}"
		rc=$?
	    else
		echo "Skipping compile phase"
		rc=0
	    fi

	    if [ ${rc} -gt 0 ]; then
		echo "Compilation Error. Can't proceed"
		if [ -r ${cfg} ]; then rm ${cfg}; fi
		return
	    else
		echo "${cmd} ${pwd} ${args}" >> ${cfg}
	    fi
	    	    
# 	    if [ ${COMPILE} = "Y" ]; then
# 		echo "Compiling: ${cmd} ${args}"
# 		eval "${cmd} ${args}"
# 		rc=$?
# 		if [ ${rc} -gt 0 ]; then
# 		    echo "Compilation Error. Can't proceed"
# 		    if [ -r ${cfg} ]; then rm ${cfg}; fi
# 		    return
# 		else
# 		    echo "${cmd} ${pwd} ${args}" >> ${cfg}
# 		fi
# 	    else
# 		echo "Skipping compile phase"
# 		echo "${cmd} ${pwd} ${args}" >> ${cfg}
# 	    fi
	    
	fi
    done

    if [ -r ${cfg} ]; then 
    	ant -f ../build_cli.xml run  -Dargs="-c ${pwd}/${cfg} ${OPTIONS}" -Djvmargs="${DEBUG_PROFILE} ${DEFS}"
    fi
}

main $@
