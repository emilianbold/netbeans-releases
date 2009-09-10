#!/bin/bash

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

XREF=""
ERROR=""
QUITE=""
AWK=${AWK-"nawk"}

function classpath() {

    local nbdist=${NBDIST-"../nbbuild/netbeans"}
    local cnddist=${CNDDIST-"${nbdist}/cnd2"}

    CP=""

    local ide
    if [ -d "${nbdist}/ide7" ]; then
	ide="${nbdist}/ide7"
    else 
	if [ -d "${nbdist}/ide8" ]; then
	    ide="${nbdist}/ide8"
	else 
	    if [ -d "${nbdist}/ide9" ]; then
		ide="${nbdist}/ide9"
	    else 
	    	if [ -d "${nbdist}/ide10" ]; then
		    ide="${nbdist}/ide10"
		else 
		    	if [ -d "${nbdist}/ide11" ]; then
			    ide="${nbdist}/ide11"
			else 
			    	if [ -d "${nbdist}/ide12" ]; then
				    ide="${nbdist}/ide12"
				else 
				    echo "Can not find ide subdirectory in Netbeans"
				    return
				fi
			fi
		fi
	    fi
	fi
    fi

    local platform
    if [ -d "${nbdist}/platform7" ]; then
	platform="${nbdist}/platform7"
    else 
	if [ -d "${nbdist}/platform8" ]; then
	    platform="${nbdist}/platform8"
	else 
		if [ -d "${nbdist}/platform9" ]; then
		    platform="${nbdist}/platform9"
		else 
		    if [ -d "${nbdist}/platform10" ]; then
			platform="${nbdist}/platform10"
		    else
			    if [ -d "${nbdist}/platform11" ]; then
				platform="${nbdist}/platform11"
			    else
				echo "Can not find platform subdirectory in Netbeans"
				return
			    fi
		    fi
		fi
	fi
    fi
    

    CP=${CP}${path_sep}${ide}/modules/org-netbeans-modules-projectuiapi.jar
    CP=${CP}${path_sep}${ide}/modules/org-netbeans-modules-projectapi.jar
    CP=${CP}${path_sep}${ide}/modules/org-netbeans-modules-projectui.jar
    CP=${CP}${path_sep}${ide}/modules/org-netbeans-modules-project-ant.jar
    CP=${CP}${path_sep}${ide}/modules/org-netbeans-modules-project-libraries.jar
    CP=${CP}${path_sep}${ide}/modules/org-openidex-util.jar
    CP=${CP}${path_sep}${ide}/modules/org-netbeans-modules-xml-catalog.jar
    CP=${CP}${path_sep}${platform}/lib/org-openide-util.jar
    CP=${CP}${path_sep}${platform}/modules/org-openide-dialogs.jar
    CP=${CP}${path_sep}${platform}/modules/org-openide-nodes.jar
    CP=${CP}${path_sep}${platform}/core/org-openide-filesystems.jar
    CP=${CP}${path_sep}${platform}/core/core.jar
    CP=${CP}${path_sep}${platform}/modules/org-openide-loaders.jar
    CP=${CP}${path_sep}${platform}/lib/org-openide-modules.jar
    CP=${CP}${path_sep}${platform}/lib/boot.jar
    CP=${CP}${path_sep}${platform}/modules/org-netbeans-api-progress.jar
    CP=${CP}${path_sep}${platform}/modules/org-netbeans-modules-queries.jar
    CP=${CP}${path_sep}${platform}/modules/org-netbeans-modules-masterfs.jar
    CP=${CP}${path_sep}${platform}/modules/org-openide-text.jar
    CP=${CP}${path_sep}${platform}/modules/org-openide-windows.jar
    CP=${CP}${path_sep}${platform}/modules/org-netbeans-modules-editor-mimelookup.jar
    CP=${CP}${path_sep}${platform}/modules/org-openide-awt.jar

    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-api-model.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-modelimpl.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-antlr.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-api-project.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-apt.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-folding.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-makeproject.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-repository-api.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-repository.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-utils.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-model-services.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-modelutil.jar

    XREF_CP=""
    if [ -n "${XREF}" ]; then
        # update classpath needed for xref
        XREF_CP=${XREF_CP}${path_sep}${cnddist}/../../../core/build/test/unit/classes
        
        XREF_CP=${XREF_CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-completion.jar
        XREF_CP=${XREF_CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-modelutil.jar
        XREF_CP=${XREF_CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-model-services.jar

        XREF_CP=${XREF_CP}${path_sep}${platform}/core/org-openide-filesystems.jar
        XREF_CP=${XREF_CP}${path_sep}${platform}/core/core.jar
        XREF_CP=${XREF_CP}${path_sep}${platform}/lib/boot.jar
        XREF_CP=${XREF_CP}${path_sep}${platform}/modules/org-netbeans-core.jar
        XREF_CP=${XREF_CP}${path_sep}${platform}/modules/org-netbeans-modules-masterfs.jar
        XREF_CP=${XREF_CP}${path_sep}${platform}/modules/org-openide-execution.jar
        XREF_CP=${XREF_CP}${path_sep}${platform}/modules/org-openide-loaders.jar
        XREF_CP=${XREF_CP}${path_sep}${platform}/modules/org-openide-dialogs.jar
        XREF_CP=${XREF_CP}${path_sep}${platform}/modules/org-openide-awt.jar
        XREF_CP=${XREF_CP}${path_sep}${platform}/modules/org-openide-windows.jar
        XREF_CP=${XREF_CP}${path_sep}${platform}/modules/org-openide-text.jar
        XREF_CP=${XREF_CP}${path_sep}${platform}/modules/org-netbeans-modules-settings.jar
        XREF_CP=${XREF_CP}${path_sep}${ide}/modules/org-netbeans-modules-editor-mimelookup.jar
        XREF_CP=${XREF_CP}${path_sep}${ide}/modules/org-netbeans-modules-editor-settings.jar
        XREF_CP=${XREF_CP}${path_sep}${ide}/modules/org-netbeans-modules-editor-lib.jar
        XREF_CP=${XREF_CP}${path_sep}${ide}/modules/org-netbeans-modules-editor-plain-lib.jar
        XREF_CP=${XREF_CP}${path_sep}${ide}/modules/org-netbeans-modules-editor-plain.jar
        XREF_CP=${XREF_CP}${path_sep}${ide}/modules/org-netbeans-modules-editor-settings-storage.jar
        XREF_CP=${XREF_CP}${path_sep}${ide}/modules/org-netbeans-modules-editor-structure.jar
        XREF_CP=${XREF_CP}${path_sep}${ide}/modules/org-netbeans-modules-editor-util.jar
        XREF_CP=${XREF_CP}${path_sep}${ide}/modules/org-netbeans-modules-editor.jar
        XREF_CP=${XREF_CP}${path_sep}${ide}/modules/org-netbeans-modules-options-editor.jar
        XREF_CP=${XREF_CP}${path_sep}${ide}/modules/org-netbeans-modules-editor-completion.jar
    fi

    if [ -z ${QUITE} ]; then
	trace_classpath ${CP}
    fi

    if [ -n "${ERROR}" ]; then
	CP=""
    else
        if [ -n "${XREF_CP}" ]; then
	    if [ -z ${QUITE} ]; then
		trace_classpath ${XREF_CP}
	    fi
            if [ -n "${ERROR}" ]; then
                CP=""
            else
                CP=${CP}${path_sep}${XREF_CP}
            fi
        fi
    fi    
}

function trace_classpath() {
    local paths=$@
    local ERROR=""
    for F in `echo ${paths} | ${AWK} -F${path_sep} '{ for( i=1; i<=NF; i++ ) print $i }'`; do
	if [ ! -r ${F} ]; then
	    echo "File ${F} doesn't exist"
	    ERROR="y"
	fi
    done

    if [ -n "${ERROR}" ]; then
	echo "incorrect classpaths"
    else
	#print classpath
	echo "Using classpath:"
	for F in `echo ${paths} | ${AWK} -F${path_sep} '{ for( i=1; i<=NF; i++ ) print $i }'`; do
	    echo $F
	done
    fi
}

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
		    echo "debugging on port ${DBGPORT}"
		    DEBUG_PROFILE="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=${DBGPORT}"
		    ;;
	    --sdebug|-sdebug)
		    echo "wait to attach debugger on port ${DBGPORT}"
		    DEBUG_PROFILE="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=${DBGPORT}"
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
	    --ycpu|-ycpu)
		    echo "profile using YourKit Profiler with CPU sampling"
		    DEBUG_PROFILE="-agentlib:yjpagent=sampling,noj2ee,dir=${HOME}/yjp_data"
		    ;;
	    --rep)
		    echo "testing repository; threading is off "
		    DEFS="${DEFS} -Dcnd.modelimpl.use.repository=true -Dcnd.repository.use.dev=true -Dcnd.repository.1file=true -Dcnd.repository.threading=false"
		    PARAMS="${PARAMS} --cleanrepository"
		    ;;
            --xref*) 
                    echo "testing xRef Repository";
                    PARAMS="${PARAMS} $1"
                    XREF="y"
                    ;;
            --threads)
                    shift
                    echo "using $1 parser threads"
                    DEFS="${DEFS} -Dcnd.modelimpl.parser.threads=$1"
                    ;;
            --quite|--q*) 
                    QUITE="y"
                    ;;
	    *)
		    PARAMS="${PARAMS} $1"
		    ;;
	esac
	shift
    done

}

function main() {

    local nbdist=${NBDIST-"../nbbuild/netbeans"}
    local cnddist=${CNDDIST-"${nbdist}/cnd2"}

    JAVA="${JAVA-`which java`}"
    DEFS=""
    PARAMS=""
    
    DBGPORT=${DBGPORT-5858}

    DEFS="${DEFS} -Dnetbeans.dirs=${nbdist}:${cnddist}"
    DEFS="${DEFS} -Dnetbeans.home=${nbdist}/platform11"
    DEFS="${DEFS} -Dnetbeans.user=/tmp/${USER}/cnd-userdir"
    #DEFS="${DEFS} -Dcnd.modelimpl.trace=true"
    #DEFS="${DEFS} -Dparser.cache=true"
    DEFS="${DEFS} -Dparser.report.errors=true"
    DEFS="${DEFS} -Dparser.report.include.failures=true"
    #DEFS="${DEFS} -Dcnd.parser.trace=true"
    #DEFS="${DEFS} -Dlexer.tracepp=true"
    #DEFS="${DEFS} -Dppexpr.showtree=true"
    #DEFS="${DEFS} -Dlexer.tracecallback=true"
    #DEFS="${DEFS} -Dlexer.honest=true"
    #DEFS=""
    #DEFS="${DEFS} -Dcnd.modelimpl.c.define=true"
    #DEFS="${DEFS} -Dcnd.modelimpl.c.include=true"
    DEFS="${DEFS} -Dcnd.modelimpl.cpp.define=true"
    DEFS="${DEFS} -Dcnd.modelimpl.cpp.include=true"
    DEFS="${DEFS} -Dcnd.cache.skip.save=true"

    #includes
    INCL=""

    INCL="${INCL} -I/usr/local/include"
    INCL="${INCL} -I/usr/lib/gcc-lib/i586-suse-linux/3.3.4/include"
    INCL="${INCL} -I/usr/i586-suse-linux/include"
    INCL="${INCL} -I/usr/include"
    INCL="${INCL} -I/usr/X11R6/include"

    #INCL="${INCL} -I/usr/include/linux"
    #INCL="${INCL} -I/usr/include/gnu"
    #INCL="${INCL} -I/usr/include/g++"
    #INCL="${INCL} -I/usr/include/g++/backward"
    #INCL=""
    #INCL="${INCL} -I/home/vv159170/devarea/dwarf_res/Python-2.4/Include/"
    #INCL="${INCL} -I/home/vv159170/devarea/dwarf_res/Python-2.4"
    INCL="${INCL} -I.. -I."
    INCL=""

    #DEFS="${DEFS} -Dcnd.modelimpl.statistics=true"
    STAT=""
    STAT="${STAT} -s/tmp/stat/GLOBTRACEMODEL.stat"
    STAT="${STAT} -S/tmp/stat"
    STAT=""

    params $@

    if [ -n "${XREF}" ]; then
        # update main class used for testing xref
        MAIN="org.netbeans.modules.cnd.modelimpl.trace.TraceXRef"
    else
        MAIN="org.netbeans.modules.cnd.modelimpl.trace.TraceModel"
    fi    

    classpath
    if [ -z "${CP}" ]; then
	echo "Can't find some necessary jars"
	return
    fi

    if [ -z ${QUITE} ]; then
	echo "Java: " ${JAVA}
	${JAVA} -version
    fi

    #set -x
    ${JAVA} -enableassertions ${DEBUG_PROFILE} -cp ${CP} ${DEFS} ${MAIN} ${STAT} ${INCL} ${PARAMS}
}

path_sep=":"

### understand path separator
uname=`uname`
#uname_prefix=`expr substr "${uname}" 1 6`
uname_prefix=${uname:0:6}
if [ "${uname_prefix}" = "CYGWIN" ]; then
   path_sep=";"
fi

main $@
