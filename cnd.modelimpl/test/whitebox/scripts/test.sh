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

CURR_DIR=`pwd`
cd `dirname $0`
BIN_DIR=`pwd`
cd $CURR_DIR

help() {
    echo ""
    echo Usage:
    echo ""
    echo $0 { options }
    echo ""
    echo "  Options can be:"
    echo "      [--init]            <path to test configuration>"
    echo "                          only make test project"
    echo "      [--run]             <path to test configuration>"
    echo "                          run preconfigured project"
    echo "      [--report]          print report"
    echo ""
#     echo "      [-cp|--classpath]   <path where jars with test suite classes reside>"
#     echo "                          (default is ${DWARF_TEST_INSTALL})"
    echo "      [-d|--dir]          <path to test project Makefile>"
    echo "                          (default is current ${DWARF_TEST_PROJECT_DIR})"
    echo "      [-n|--netbeans]     <path to NetBeans (with CND) installation>"
    echo "                          (default is ${DWARF_TEST_NB_INSTALL})"
    echo "      [-c|--cnd]          <path to CND module installation>"
    echo "                          (default is ${DWARF_TEST_NB_INSTALL}/cnd)"
    echo "      [-p|--path]         <path to g++ substitution script>"
    echo "                          (default is ${DWARF_TEST_PATH})"
    echo "      [-t|--temp]         <path to temporary files>"
    echo "                          (default is ${DWARF_TEST_TEMP})"
    echo ""
    echo "      [-g|--sdebug]        run in debugging mode (suspend at start)"
    echo ""
    echo "      [--debug]            run in debugging mode (do NOT suspend at start)"
    echo ""
    echo "      [-m|--mail]         send test results to all recipients from this list"
    echo ""
    echo "      [-J-Dxxx]           additional defines to pass JVM"
    echo ""
    echo "      [-h|-?|-H|--help]   print help"
    echo ""
}


set_defaults() {
    DWARF_TEST_CONFIG_PATH=${DWARF_TEST_CONFIG_PATH:-"/tmp"}
    #DWARF_TEST_PATH="/export/home/nk220367/main/cnd.modelimpl/test/whitebox"}
    DWARF_TEST_INSTALL="/export/home/nk220367/main/cnd.modelimpl/test/whitebox"
    DWARF_TEST_NB_INSTALL="/opt/netbeans"
    DWARF_TEST_CND_INSTALL="/opt/netbeans/cnd"

    DWARF_TEST_PATH=${DWARF_TEST_PATH:-${BIN_DIR}}
    #DWARF_TEST_INSTALL="${DWARF_TEST_INSTALL:-${DWARF_TEST_PATH}/install}"
    #DWARF_TEST_NB_INSTALL="${DWARF_TEST_NB_INSTALL:-${DWARF_TEST_PATH}/latest-netbeans}"
    #DWARF_TEST_CND_INSTALL="${DWARF_TEST_CND_INSTALL:-${DWARF_TEST_PATH}/latest-cnd}"
    DWARF_TEST_PROJECT_DIR=${DWARF_TEST_PROJECT_DIR:-${CURR_DIR}}
    PROJECT=`basename ${DWARF_TEST_PROJECT_DIR}`
    DWARF_TEST_TEMP=${DWARF_TEST_TEMP:-"/tmp/${PROJECT}"}
#   MAIL_LIST="fd-qa-ifdef@sun.com"
    
    # Always add -Xms256m -Xmx1024m
    DWARF_ADDITIONAL_JVM_OPT="${DWARF_ADDITIONAL_JVM_OPT} -Xms256m -Xmx2048m -Dcnd.modelimpl.excl.compound=false"

    export GNU_PATH=${GNU_PATH:-"/usr/sfw/bin"}
    if [ ! -d ${GNU_PATH} ]; then
	GNU_PATH=""
    fi

}


parse_params() {

    while [ -n "$1" ]
	do
	case "$1" in

	    -g|--sdebug*)
		export DWARF_TEST_JVMOPT="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5858"
		;;

	    --debug*)
		export DWARF_TEST_JVMOPT="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5858"
		;;

	    --profile|-profile)
		    echo "profile on port 5140"
		    DEBUG_PROFILE="-agentpath:/opt/netbeans/5.0/profiler1/lib/deployed/jdk15/solaris-i386/libprofilerinterface.so=/opt/netbeans/5.0/profiler1/lib,5140"
		    ;;

	    --yprofile)
		export DWARF_TEST_JVMOPT="-agentlib:yjpagent=dir=${HOME}/yjp_data"
		;;

	    -m|--mail*)
		shift
		MAIL_LIST="$1"
		;;

# 	    -cp|--classpath*)
# 		shift
# 		DWARF_TEST_INSTALL="$1"
# 		;;
# 
	    -n|--netbeans*)
		shift
		DWARF_TEST_NB_INSTALL="$1"
		;;

	    -c|--cnd*)
		shift
		DWARF_TEST_CND_INSTALL="$1"
		;;

	    -d|--dir*)
		shift
		DWARF_TEST_PROJECT_DIR="$1"
		;;

	    -p|--path*)
		shift
		DWARF_TEST_PATH="$1"
		;;

	    -t|--temp*)
		shift
		DWARF_TEST_TEMP="$1"
		;;

	    -h|-?|-H|--help*)
		help; return
		;;

	    -J*)
		DWARF_ADDITIONAL_JVM_OPT="${DWARF_ADDITIONAL_JVM_OPT} ${1#-J}"
		;;

	    --init*)
		shift
		DWARF_TEST_CONFIG_PATH="$1"
		DWARF_TEST_MODE="init"
		;;

	    --run*)
		shift
		DWARF_TEST_CONFIG_PATH="$1"
		DWARF_TEST_MODE="run"
		;;

	    --report*)
		DWARF_TEST_MODE="report"
		;;

	    *)
		echo "Incorrect option: " $1
		DWARF_TEST_MODE="help"
		help; return
		;;
	esac
	shift
    done

}

 classpath() {
 
     #### setting DWARF_TEST_CND_INSTALL (its default depends on parameters)
     if [ -z ${DWARF_TEST_CND_INSTALL} ]; then
         DWARF_TEST_CND_INSTALL=${DWARF_TEST_NB_INSTALL}/cnd
     fi
 
     platform=`cd ${DWARF_TEST_NB_INSTALL}; find . -name org-openide-util.jar | sed 's/\.\/\(.*\)\/lib.*/\1/'`
     ide=`cd ${DWARF_TEST_NB_INSTALL}; find . -name org-netbeans-modules-projectuiapi.jar | sed 's/\.\/\(.*\)\/modules.*/\1/'`
 
     export DWARF_TEST_CLASSPATH=""
 
     #DWARF_TEST_CLASSPATH="${DWARF_TEST_INSTALL}/jawa-dwarf-dump.jar"
     #DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_INSTALL}/model-test.jar"
     #DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_INSTALL}/swing-layout-1.0.jar"	

     DWARF_TEST_CLASSPATH="${DWARF_TEST_INSTALL}/dwarf-vs-model/dist/Dwarf-vs-Model.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_INSTALL}/model-dump/dist/ModelDump.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_INSTALL}/dm-utils/dist/dm-utils.jar"
 
     #DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${platform}/lib/org-openide-util.jar"
     #DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${platform}/modules/org-openide-nodes.jar"
     #DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${platform}/core/org-openide-filesystems.jar"
     #DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${ide}/modules/org-netbeans-modules-projectuiapi.jar"
     #DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${ide}/modules/org-netbeans-modules-projectapi.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${platform}/modules/org-openide-dialogs.jar"
     #DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${platform}/modules/org-openide-loaders.jar"
     #DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${platform}/lib/org-openide-modules.jar"
 
     #DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd.jar"
     #DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd-makeproject.jar"
     #DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd-api-model.jar"
     #DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd-modelimpl.jar"
     #DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd-antlr.jar"
 
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd-dwarfdump.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd-api-model.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd-modelimpl.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd-antlr.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd-makeproject.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd-apt.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd-repository.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd-repository-api.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd-api-project.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_CND_INSTALL}/modules/org-netbeans-modules-cnd-utils.jar"
     
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${platform}/modules/org-openide-nodes.jar"            
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${platform}/modules/org-openide-loaders.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${platform}/modules/org-netbeans-api-progress.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${platform}/modules/org-netbeans-modules-queries.jar"

     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${platform}/core/core.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${platform}/core/org-openide-filesystems.jar"

     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${platform}/lib/org-openide-util.jar"           
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${platform}/lib/org-openide-modules.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${platform}/lib/boot.jar"

     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${ide}/modules/org-netbeans-modules-projectuiapi.jar"
     DWARF_TEST_CLASSPATH="${DWARF_TEST_CLASSPATH}:${DWARF_TEST_NB_INSTALL}/${ide}/modules/org-netbeans-modules-projectapi.jar"

     status=0
     for F in `echo ${DWARF_TEST_CLASSPATH} | awk -F: '{ for( i=1; i<=NF; i++ ) print $i }'`; do
         if [ ! -r ${F} ]; then
             echo "File ${F} doesn't exist"
             status=1
         fi
     done
     if [ ${status} != 0 ]; then
         return
     fi
 }

make_paths_absolute() {
    #### changing paths (excepr for PROJECT_DIR) with absolute ones

    if [ `expr match ${DWARF_TEST_INSTALL} "\/"` == 0 ]; then 
        DWARF_TEST_INSTALL="`pwd`/${DWARF_TEST_INSTALL}"
    fi
    if [ `expr match ${DWARF_TEST_NB_INSTALL} "\/"` == 0 ]; then
        DWARF_TEST_NB_INSTALL="`pwd`/${DWARF_TEST_NB_INSTALL}"
    fi
    if [ `expr match ${DWARF_TEST_CND_INSTALL} "\/"` == 0 ]; then
        DWARF_TEST_CND_INSTALL="`pwd`/${DWARF_TEST_CND_INSTALL}"
    fi
    if [ `expr match ${DWARF_TEST_PATH} "\/"` == 0 ]; then
        DWARF_TEST_PATH="`pwd`/${DWARF_TEST_PATH}"
    fi
    if [ `expr match ${DWARF_TEST_TEMP} "\/"` == 0 ]; then
        DWARF_TEST_TEMP="`pwd`/${DWARF_TEST_TEMP}"
    fi
    if [ `expr match ${DWARF_TEST_CONFIG_PATH} "\/"` == 0 ]; then
        DWARF_TEST_CONFIG_PATH="`pwd`/${DWARF_TEST_CONFIG_PATH}"
    fi
}


check_temp() {
    #### checking DWARF_TEST_TEMP ####
    rm -rf ${DWARF_TEST_TEMP}
    mkdir -p  ${DWARF_TEST_TEMP} 

    if [ ! -d ${DWARF_TEST_TEMP} ]; then
        echo "Can not create directory ${DWARF_TEST_TEMP}"
	status=1
        return 
    fi

    if [ ! -w ${DWARF_TEST_TEMP} ]; then
        echo "Directory ${DWARF_TEST_TEMP} does not have write permission"
	status=1
        return
    fi

    status=0
}

check_config_path() {
    #### checking DWARF_TEST_CONFIG_PATH ####
    if [ ! -d ${DWARF_TEST_CONFIG_PATH} ]; then
        mkdir -p  ${DWARF_TEST_CONFIG_PATH} 
    fi
    if [ ! -d ${DWARF_TEST_CONFIG_PATH} ]; then
        echo "Can't create directory ${DWARF_TEST_CONFIG_PATH}"
	status=1
        return
    fi
    if [ ! -w ${DWARF_TEST_CONFIG_PATH} ]; then
        echo "Directory ${DWARF_TEST_CONFIG_PATH} does not have write permission"
	status=1
        return
    fi
    status=0
}

check_params_and_go() {

    if [ -z ${DWARF_TEST_MODE} ]; then
	echo "One of the three modes should be specified: --init, --run or --report"
	return
    fi

    make_paths_absolute

    check_config_path
    if [ ${status} != 0 ]; then
	return
    fi

    local project_name_file="${DWARF_TEST_CONFIG_PATH}/_project"
    if [ "$DWARF_TEST_MODE" = "init" ]; then
        export PROJECT=`basename ${DWARF_TEST_PROJECT_DIR}`
        echo "${PROJECT}" > ${project_name_file}
    else
        export PROJECT=`cat ${project_name_file}`
    fi

    check_temp
    if [ ${status} != 0 ]; then
	return
    fi


    if [ "$DWARF_TEST_MODE" = "run" ]; then 

    	#### checking DWARF_TEST_PATH ####
	local file=""
    	file="${DWARF_TEST_PATH}/g++"
	if [ ! -x "${file}" ]; then
	    echo "Can't find executable ${file}"
	    return
	fi

 	#### setting and checking classpath 
 	classpath
 	if [ ${status} != 0 ]; then
 	    return
 	fi

    	export DWARF_ADDITIONAL_JVM_OPT 
    fi

    if [ "$DWARF_TEST_MODE" = "init" ]; then


	#checking the g++ presence
	if [ -z "${GNU_PATH}" ]; then
		echo "The GNU_PATH environment variable should be specified and point to the g++/gcc path"
		return
	fi
	export DWARF_TEST_GCC=${GNU_PATH}/g++
	if [ ! -x ${DWARF_TEST_GCC} ]; then 
		echo "Can't execute ${DWARF_TEST_GCC}. Set GNU_PATH environment variable correctly"
		return
	fi
	
	local WHICH_GMAKE=`which gmake`
	if [ `expr "${WHICH_GMAKE}" : "no gmake"` != 0 ]; then
		echo "no gmake found in path"
		return
	fi
	echo "gmake=${WHICH_GMAKE}"
	echo "Adding ${DWARF_TEST_PATH} to PATH"
	export PATH=${DWARF_TEST_PATH}:${PATH}
    fi

    export DWARF_ALL_ERR="${DWARF_TEST_TEMP}/${PROJECT}.err"
    export DWARF_OUT="${DWARF_TEST_TEMP}/${PROJECT}.out"
    export DWARF_LOG="${DWARF_TEST_TEMP}/${PROJECT}.log"
    export DWARF_FAIL="${DWARF_TEST_TEMP}/${PROJECT}.fail"

    rm -rf ${DWARF_ALL_ERR} > /dev/null

    pushd ${DWARF_TEST_PROJECT_DIR} > /dev/null

    export DWARF_TEST_CONFIG_PREFIX=${DWARF_TEST_CONFIG_PATH}/${PROJECT}

    if [ "$DWARF_TEST_MODE" = "init" ]; then

        local zz="/tmp/__z.cc"
        echo "" > $zz 
        ${DWARF_TEST_GCC} -v -E  $zz 2>${DWARF_TEST_CONFIG_PREFIX}.inc
        ${DWARF_TEST_GCC} -dM -E $zz >${DWARF_TEST_CONFIG_PREFIX}.mac
        rm -f $zz > /dev/null

        echo "Current directory is `pwd`"
        echo "Running gmake"
        echo ""
        rm -f ${DWARF_TEST_CONFIG_PREFIX}.gcc
        gmake clean 2>&1 | tee -a $DWARF_LOG
        gmake all   2>&1 | tee -a $DWARF_LOG
        
        #bash ${DWARF_TEST_PATH}/countfiles.sh -d `dirname ${DWARF_TEST_CONFIG_PATH}` -p ${PROJECT} > ${DWARF_TEST_CONFIG_PREFIX}.fno
        ${DWARF_TEST_PATH}/countfiles.sh -d ${DWARF_TEST_CONFIG_PATH} -p ${PROJECT} > ${DWARF_TEST_CONFIG_PREFIX}.fno
    fi
    popd > /dev/null

    if [ "$DWARF_TEST_MODE" = "run" ]; then 
	echo CONFIG=$DWARF_TEST_CONFIG_PATH
	. ${DWARF_TEST_PATH}/test-run.sh
    fi

    if [ "$DWARF_TEST_MODE" != "init" ] && [ "$DWARF_TEST_MODE" != "help" ] ; then 
	. ${DWARF_TEST_PATH}/test-report.sh
    fi
}


parse_params $@
set_defaults
check_params_and_go
