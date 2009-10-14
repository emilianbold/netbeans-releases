#!/bin/sh
if [ "$1" = "-t" ]; then
	defs="TRACE=1"
else
	defs=""
fi

gmake ${defs} clean all
gmake ${defs} 64BITS=1 clean all
