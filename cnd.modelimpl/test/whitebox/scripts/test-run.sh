#!/bin/bash 


run() {
echo start dump
	local main="ddiff.DDiffMain"
        local temp=${DWARF_TEST_TEMP:-/tmp}
echo "temp=$temp"
        local all_std=${DWARF_LOG}
        local all_err=${DWARF_ALL_ERR:-${temp}/_all_err.txt}
	echo "Running test $@"
	java -cp ${DWARF_TEST_CLASSPATH} ${DWARF_TEST_JVMOPT} ${DWARF_ADDITIONAL_JVM_OPT} -Dparser.report.errors=true ${main} -t ${temp} -h ${DWARF_TEST_CONFIG_PREFIX}.gcc  2>>$all_err
	rc=$?
	if [ ${rc} != 0 ] ; then
	    echo "Error in java program: RC= ${rc}"
	fi
}

run | tee -a $DWARF_LOG
