#!/bin/bash

export __CND_TOOLS__=gcc
export __CND_BUILD_LOG__=`pwd`/dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86/log.txt
rm -rf ${__CND_BUILD_LOG__}
export LD_PRELOAD=libBuildTrace.so
export LD_LIBRARY_PATH=`pwd`/dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86:${LD_LIBRARY_PATH}
export LD_LIBRARY_PATH_64=`pwd`/dist/SunOS-Previse_64/OracleSolarisStudio-Solaris-x86:${LD_LIBRARY_PATH_64}

sh compile.bash
bash compile.bash
ksh  compile.bash

cat ${__CND_BUILD_LOG__} | awk -f ./test.awk
