#!/bin/bash

if [ -z "${1}" ]; then
	dirs="ddd mico mysql python clucene boost"
else
	dirs=$@
fi

TESTCODE=${TESTCODE-${HOME}/_testcode}
RES_ROOT=${TESTCODE}/_res

for PROJECT in ${dirs}; do
	echo "======================================== ${PROJECT} ========================================";
	RES="${RES_ROOT}/${PROJECT}"; 
	mkdir -p ${RES} > /dev/null; 
	time ant -f build_cli.xml run -Dargs="-c ${TESTCODE}/_initdata/${PROJECT}/all.gcc -t ${RES}" -Djvmargs="-Xmx1536M" 2>&1 | tee ${RES}/_all.log; 
done

