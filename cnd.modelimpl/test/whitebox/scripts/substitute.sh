#!/bin/bash

#
# Environment variables used:
#
#	DWARF_TEST_CONFIG	file for test config prefix
# 	DWARF_TEST_GCC          an absolute path to original compiler (icluding file name)
#

run() {
	echo "#======================================================================"
	echo "# Compiler substitition in `pwd`"
	echo "# Command line is $@"
	echo "# Runnung ${DWARF_TEST_GCC} -g $@" 
	echo "g++ `pwd` $@" >> ${DWARF_TEST_CONFIG_PREFIX}.gcc
	${DWARF_TEST_GCC} -g $@  
	local rc=$?
	if [ ${rc} != 0 ] ; then
		echo COMPILATION ERRORS: RC=${rc}
	else
		echo "rc=$rc"
	fi
	echo "#----------------------------------------------------------------------"
}

run $@ 

echo ""
