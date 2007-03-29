#!/bin/bash

function params() {
    while [ -n "$1" ]
    do
	case "$1" in
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


function run() {

	PARAMS=$@
	XMX="-Xmx256m"
	JVMAGRS=""
	
	params $@
	
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
		
	ant run -Dapplication.args="${PARAMS}" ${SUITE_DEFS} -Drun.jvmargs="${XMX} ${JVMAGRS} ${DEBUG_PROFILE}"
}

run $@
