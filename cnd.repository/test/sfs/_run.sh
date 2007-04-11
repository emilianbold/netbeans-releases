#!/bin/bash

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
	ide7=`ls -d  $NBDIST/ide[789]`
	if [ -d ${ide7} ]; then
	    if [ ! -z ${ide7} ]; then
		ant=${NBDIST}/ide7/ant/bin/ant
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
