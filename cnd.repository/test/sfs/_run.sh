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
	    --noant)
		    NOANT="true"
		    ;;
	    -J-Xmx*)
		    XMX="${1#-J}"
		    ;;
	    -J*)
		    JVMAGRS="${JVMAGRS} ${1#-J}"
		    ;;
	    --nb)
		    shift
		    echo "Using NB from $1"
		    NBDIST=$1
		    ;;
	    -debug|--debug)
		    echo "debugging on port 5858"
		    DEBUG_PROFILE="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5858"
		    ;;
	    --sdebug|-sdebug)S
		    echo "wait to attach debugger on port 5858"
		    DEBUG_PROFILE="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5858"
		    ;;
	    --yprofile|-yprofile)
		    echo "profile using YourKit Profiler"
		    DEBUG_PROFILE="-agentlib:yjpagent=dir=${HOME}/yjp_data"
		    ;;
	    --ycpu|-ycpu)
		    echo "profile using YourKit Profiler with CPU sampling"
		    DEBUG_PROFILE="-agentlib:yjpagent=sampling,noj2ee,dir=${HOME}/yjp_data"
		    ;;
	    --ycpu|-ycpu)
		    echo "profile using YourKit Profiler with CPU sampling"
		    DEBUG_PROFILE="-agentlib:yjpagent=sampling,noj2ee,dir=${HOME}/yjp_data"
		    ;;
	    *)
		    PARAMS="${PARAMS} $1"
		    ;;
	esac
	shift
    done
}

function classpath() {

    path_sep=":"

    ### understand path separator
    uname=`uname`
    #uname_prefix=`expr substr "${uname}" 1 6`
    uname_prefix=${uname:0:6}
    if [ "${uname_prefix}" = "CYGWIN" ]; then
       path_sep=";"
    fi

    CP=""

    CP=./dist/lib/org-netbeans-modules-cnd-repository.jar
    CP=./dist/sfs.jar
    local error=""

    for F in `echo ${CP} | awk -F${path_sep} '{ for( i=1; i<=NF; i++ ) print $i }'`; do
	if [ ! -r ${F} ]; then
	    echo "File ${F} doesn't exist"
	    error="y"
	fi
    done

    if [ -n "${error}" ]; then
	CP=""
    else
	#print classpath
	echo "Using classpath:"
	for F in `echo ${CP} | awk -F${path_sep} '{ for( i=1; i<=NF; i++ ) print $i }'`; do
	    echo $F
	done
    fi
}

function run() {

    PARAMS=$@
    XMX="-Xmx256m"
    JVMAGRS=""

    params $@
	
    if [ -z "${NOANT}" ]; then
	if [ -z "${NBDIST}" ]; then
		echo "Please specify NBDIST environment variable; it should point to Netbeans installation"
		return
	else
		if [ -r "${NBDIST}/bin/netbeans" ]; then
			SUITE_DEFS="-Dnbplatform.NBDEV.platform.dir=${NBDIST} -Dnbplatform.NBDEV.harness.dir=${NBDIST}/harness -Dnbplatform.NBDEV.netbeans.dest.dir=${NBDIST}"
		else
			echo "NBDIST environment variable should point to Netbeans installation"
			return
		fi
	fi
	ide=`ls -d  $NBDIST/ide[789]`
	if [ -d ${ide} ]; then
	    if [ ! -z ${ide} ]; then
		ant=${NBDIST}/ide/ant/bin/ant
		${ant} run -Dapplication.args="${PARAMS}" ${SUITE_DEFS} -Drun.jvmargs="${XMX} ${JVMAGRS} ${DEBUG_PROFILE}"
	    else
		echo "Can not find \"ide*\" subdirectory in Netbeans installation"
	    fi
	else
	    echo "Can not find \"ide*\" subdirectory in Netbeans installation"
	fi
    else
        JAVA="${JAVA-`which java`}"
        MAIN="test.sfs.TestMain"
        
        classpath
        if [ -z "${CP}" ]; then
            echo "Can't find some necessary jars"
            return
        fi

        ${JAVA} -cp ${CP} ${DEFS} ${MAIN} ${PARAMS}
    fi
}

run $@
echo result=$?
