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
