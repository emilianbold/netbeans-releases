#!/bin/bash
if [ "$1" = "-t" ]; then
	defs="TRACE=1"
else
	defs=""
fi

gmake ${defs} clean all
rc32=$?
gmake ${defs} 64BITS=1 clean all
rc64=$?
if [ ${rc32} -eq 0 ]; then bash -c "echo -e '\E[;32m' 32-bit build: OK"; else bash -c "echo -e '\E[;31m' 32-bit build: FAILURE"; fi
if [ ${rc64} -eq 0 ]; then bash -c "echo -e '\E[;32m' 64-bit build: OK"; else bash -c "echo -e '\E[;31m' 64-bit build: FAILURE"; fi

