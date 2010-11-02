#!/bin/bash

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

function classpath() {

    CP=""

    CP=${CP}${path_sep}${platform_lib_path}boot.jar
    CP=${CP}${path_sep}${platform_lib_path}org-openide-util.jar
    CP=${CP}${path_sep}${platform_lib_path}org-openide-util-lookup.jar
    CP=${CP}${path_sep}${platform_lib_path}org-openide-modules.jar

    CP=${CP}${path_sep}${platform_core_path}core.jar
    CP=${CP}${path_sep}${platform_core_path}org-openide-filesystems.jar

    CP=${CP}${path_sep}${platform_path}org-openide-loaders.jar
    CP=${CP}${path_sep}${platform_path}org-openide-nodes.jar
    CP=${CP}${path_sep}${platform_path}org-openide-dialogs.jar
    CP=${CP}${path_sep}${platform_path}org-openide-awt.jar
    CP=${CP}${path_sep}${platform_path}org-openide-text.jar
    CP=${CP}${path_sep}${platform_path}org-openide-windows.jar
    CP=${CP}${path_sep}${platform_path}org-netbeans-modules-masterfs.jar
    CP=${CP}${path_sep}${platform_path}org-netbeans-modules-queries.jar

    CP=${CP}${path_sep}${ide_path}org-netbeans-modules-dlight-nativeexecution.jar
    CP=${CP}${path_sep}${ide_path}/ext/jsch-0.1.43.jar

    CP=${CP}${path_sep}${ide_path}org-openidex-util.jar
    CP=${CP}${path_sep}${ide_path}org-netbeans-api-java-classpath.jar
    CP=${CP}${path_sep}${ide_path}org-netbeans-modules-project-ant.jar
    CP=${CP}${path_sep}${ide_path}org-netbeans-modules-projectapi.jar
    CP=${CP}${path_sep}${ide_path}org-netbeans-modules-projectuiapi.jar
    CP=${CP}${path_sep}${ide_path}org-netbeans-modules-project-libraries.jar
    CP=${CP}${path_sep}${ide_path}org-netbeans-modules-xml-catalog.jar

    CP=${CP}${path_sep}${cnd_path}org-netbeans-modules-cnd-discovery.jar
    CP=${CP}${path_sep}${cnd_path}org-netbeans-modules-cnd-apt.jar
    CP=${CP}${path_sep}${cnd_path}org-netbeans-modules-cnd-utils.jar
    CP=${CP}${path_sep}${cnd_path}org-netbeans-modules-cnd-makeproject.jar
    CP=${CP}${path_sep}${cnd_path}org-netbeans-modules-cnd.jar
    CP=${CP}${path_sep}${cnd_path}org-netbeans-modules-cnd-api-project.jar
    CP=${CP}${path_sep}${cnd_path}org-netbeans-modules-cnd-toolchain.jar
    CP=${CP}${path_sep}${cnd_path}org-netbeans-modules-cnd-remote.jar
    CP=${CP}${path_sep}${cnd_path}org-netbeans-modules-cnd-api-remote.jar

}

function trace_classpath() {
    local paths=$@
    local ERROR=""
    for F in `echo ${paths} | awk -F${path_sep} '{ for( i=1; i<=NF; i++ ) print $i }'`; do
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
	for F in `echo ${paths} | awk -F${path_sep} '{ for( i=1; i<=NF; i++ ) print $i }'`; do
	    echo $F
	done
    fi
}

function params() {

    while [ -n "$1" ]
    do
	case "$1" in
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
	    *)
		    PARAMS="${PARAMS} $1"
		    ;;
	esac
	shift
    done

}

function main() {

    JAVA="${JAVA-`which java`}"
    DEFS=""
    PARAMS=""
    
    DBGPORT=${DBGPORT-5858}

    params $@

    MAIN="org.netbeans.modules.cnd.discovery.project.StandAlone"
    nb_path=`pwd`/../../../../../../../../../../../nbbuild/netbeans/
    cnd_path=${nb_path}cnd/modules/
    platform_lib_path=${nb_path}platform/lib/
    platform_core_path=${nb_path}platform/core/
    platform_path=${nb_path}platform/modules/
    ide_path=${nb_path}ide/modules/

#/export/home/as204739/cnd-main/cnd.discovery/test/unit/src/org/netbeans/modules/cnd/discovery/project/cases

    classpath
    if [ -z "${CP}" ]; then
	echo "Can't find some necessary jars"
	return
    fi

    if [ -z ${QUITE} ]; then
	echo "Java: " ${JAVA}
	${JAVA} -version
    fi

    set -x
#    ${JAVA} -Xmx512M -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8888 -agentlib:yjpagent=dir=/home/as204739/yjp_data/IDE -Dorg.netbeans.modules.cnd.makeproject.api.runprofiles=true -cp ${CP} ${MAIN} ${PARAMS}
    ${JAVA} -Xmx512M -Dnetbeans.dirs=${nb_path}ide -Dorg.netbeans.modules.cnd.makeproject.api.runprofiles=true -cp ${CP} ${MAIN} ${PARAMS}

}

path_sep=":"

### understand path separator
uname=`uname`
#uname_prefix=`expr substr "${uname}" 1 6`
uname_prefix=${uname:0:6}
if [ "${uname_prefix}" = "CYGWIN" ]; then
   path_sep=";"
fi

#main $@
main -project=open-solaris  -root=/net/elif/export1/sside/pomona/java_cp/wsb138 -netbeans-project=/var/tmp/pomona


