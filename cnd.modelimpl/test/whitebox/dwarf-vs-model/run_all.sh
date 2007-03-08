#!/bin/bash

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
	    --res)
			shift
			RES_ROOT=$1
			;;
	    *)
		    dirs="${dirs} $1"
		    ;;
	esac
	shift
    done
}


function main() {

dirs=""

TESTCODE=${TESTCODE-${HOME}/_testcode}
INITDATA=${TESTCODE}/_initdata
RES_ROOT=${TESTCODE}/_res

params $@

if [ -z "${dirs}" ]; then
        dirs="ddd mico mysql python clucene boost"
fi

echo "Using settings from ${INITDATA}"
echo "Storing results in ${RES_ROOT}"

for PROJECT in ${dirs}; do
	echo "======================================== ${PROJECT} ========================================";
	RES="${RES_ROOT}/${PROJECT}"; 
	mkdir -p ${RES} > /dev/null; 
	time ant -f build_cli.xml run -Dargs="-c ${INITDATA}/${PROJECT}/all.gcc -t ${RES}" -Djvmargs="-Xmx1536M ${DEFS} ${DEBUG_PROFILE}" 2>&1 | tee ${RES}/_all.log; 
done
}

main $@
