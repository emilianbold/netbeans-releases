#!/bin/sh 
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
# nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
# particular file as subject to the "Classpath" exception as provided
# by Sun in the GPL Version 2 section of the License file that
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

PARAMS=""
SUSPEND="n"
CONSOLE="-J-Dnetbeans.logger.console=true"
DBGPORT=${DBGPORT-5858}
TMP_PREFIX=${TMP_PREFIX-/var/tmp}
USERDIR="--userdir ${TMP_PREFIX}/${USER}/cnd-userdir"
PARSERRORS="-J-Dparser.report.errors=true"
DEBUG="-J-Xdebug -J-Djava.compiler=NONE -J-Xrunjdwp:transport=dt_socket,server=y"
PRG=$0
NB_COPY="${TMP_PREFIX}/${USER}/nb-copy"

while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    if expr "$link" : '^/' 2> /dev/null >/dev/null; then
        PRG="$link"
    else
        PRG="`dirname "$PRG"`/$link"
    fi
done

while [ -n "$1" ]
do
    case "$1" in
        --gdb)
                echo "Enable GDB-window and do not delete gdb-cmds logs"
                PARAMS="${PARAMS} -J-Dgdb.console.window=true -J-Dgdb.console.savelog=true"
                ;;
	--cache)
		shift
                echo "Redirecting cache to $1"
                PARAMS="${PARAMS} -J-Dcnd.repository.cache.path="$1""
                ;;
        --nopersist)
                echo "Setting persistence OFF"
                PARAMS="${PARAMS} -J-Dcnd.modelimpl.persistent=false"
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
	--nocon|--noconsole)
		CONSOLE=""
		;;
	--nocopy|--nc)
		NB_COPY=""
		;;
        --cp|--copy)
		shift
                NB_COPY=$1
		if [ -r ${NB_COPY} ]; then
		    echo "The content of ${NB_COPY} will be deleted"
		    echo "Press ENTER to proceed or ^C to abort"
		    read t
		fi
                ;;
	--nodebug|-nodebug)
		echo "Debug mode OFF"
		DEBUG=""
		;;
	--sdebug|-sdebug)
		echo "wait to attach debugger on port ${DBGPORT}"
		SUSPEND="y"
		;;
	--debug|-debug)
		echo "debigging mode (nowait)"
		SUSPEND="n"
		;;
        --ycpu|-ycpu)
                echo "profile using YourKit Profiler with CPU sampling, save snapshots in ${HOME}/yjp_data/IDE"
                PROFILE="-J-agentlib:yjpagent=sampling,monitors,noj2ee,disablej2ee,dir=${HOME}/yjp_data/IDE"
                ;;
        --yprofile|-yprofile)
                echo "profile using YourKit Profiler, save snapshots in ${HOME}/yjp_data/IDE"
                PROFILE="-J-agentlib:yjpagent=dir=${HOME}/yjp_data/IDE,noj2ee,disablestacktelemetry,disablej2ee,disableexceptiontelemetry"
                ;;
        --userdir)
		shift
                echo "setting userdir to $1"
		USERDIR="--userdir $1"
                ;;
        --nouserdir)
                echo "setting userdir to standard one"
		USERDIR=""
                ;;
	--noerr)
		echo "suppressing parser errors"
		PARSERRORS=""
		;;
	--hardrefs|--hard)
                echo "using in-memory (hard refs) repository"
		PARAMS="${PARAMS} -J-Dcnd.repository.hardrefs=true"
		;;
	--threads)
                shift
                echo "using $1 parser threads"
                PARAMS="${PARAMS} -J-Dcnd.modelimpl.parser.threads=$1"
		;;
	*)
		PARAMS="${PARAMS} $1"
		;;
    esac
    shift
done

if [ -z "${NBDIST}" ]; then
	CND=`dirname "$PRG"`
	cd ${CND}
	CND=`pwd`
	cd $OLDPWD
	NBDIST="${CND}/../nbbuild/netbeans"
fi

if [ -z "${NBDIST}" ]; then
	echo "Please specify NBDIST environment variable; it should point to Netbeans installation"
        exit 1;
else
	if [ ! -r "${NBDIST}/bin/netbeans" ]; then
		echo "NBDIST environment variable should point to Netbeans installation"
                exit 1;
	fi
fi

if [ -n "${DEBUG}" ]; then
    DEBUG="${DEBUG},suspend=${SUSPEND},address=${DBGPORT}"
fi

DEFS="-J-Dnetbeans.system_http_proxy=webcache:8080"
DEFS="${DEFS} ${CONSOLE}"
DEFS="${DEFS} ${PARSERRORS}"
DEFS="${DEFS} -J-Dcnd.modelimpl.timing=true"
DEFS="${DEFS} -J-Dcnd.modelimpl.timing.per.file.flat=true"
DEFS="${DEFS} -J-Dparser.report.include.failures=true"
DEFS="${DEFS} -J-Dsun.java2d.pmoffscreen=false"
DEFS="${DEFS} -J-Dtest.xref.action=true"
DEFS="${DEFS} -J-Dcnd.classview.sys-includes=true"
DEFS="${DEFS} -J-Dcnd.callgraph.showgraph=true"
DEFS="${DEFS} -J-Dcnd.trace.includes=true"
DEFS="${DEFS} -J-Dcnd.standalone.trace=true"
DEFS="${DEFS} -J-Dcnd.refactoring.extra=true"
##DEFS="${DEFS} -J-Dcnd.parser.queue.trace=true"
##DEFS="${DEFS} -J-Dcnd.modelimpl.parser.threads=2"
##DEFS="${DEFS} -J-Dcnd.modelimpl.no.reparse.include=true"


if [ -z "${NB_COPY}" ]; then
    echo "Using original NB location: ${NBDIST}"
else
    echo "Copying NB from ${NBDIST} to ${NB_COPY}..."
    mkdir -p ${NB_COPY} > /dev/null
    rm -rf ${NB_COPY}/* > /dev/null
    cp -r ${NBDIST}/* ${NB_COPY}
    echo "Launching NB from ${NB_COPY}"
    NBDIST="${NB_COPY}"
fi

${NBDIST}/bin/netbeans -J-ea -J-server ${USERDIR} ${DEBUG} ${PROFILE} ${DEFS} ${PARAMS}
