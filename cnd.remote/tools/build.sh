#!/bin/sh

#
# Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
#

if [ -z "${MAKE}" ]; then
    if [ `uname -s` = SunOS ]; then
	MAKE=gmake
    else
        MAKE=make
    fi
fi

if [ "$1" = "-t" ]; then
	defs="TRACE=1"
else
	defs=""
fi

${MAKE} ${defs} clean all
rc32=$?
${MAKE} ${defs} 64BITS=1 clean all
rc64=$?
if [ ${rc32} -eq 0 ]; then bash -c "echo -e '\E[;32m' 32-bit build: OK"; else bash -c "echo -e '\E[;31m' 32-bit build: FAILURE"; fi
if [ ${rc64} -eq 0 ]; then bash -c "echo -e '\E[;32m' 64-bit build: OK"; else bash -c "echo -e '\E[;31m' 64-bit build: FAILURE"; fi

