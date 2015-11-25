#!/bin/bash

export STUDIO_LIB=/opt/solarisstudiodev/lib/compilers
export __CND_TOOLS__=gcc
export __CND_BUILD_LOG__=`pwd`/dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86/log.txt
rm -rf ${__CND_BUILD_LOG__}
rm -rf dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86/out*
export LD_PRELOAD=libdiscover.so:libBuildTrace.so
export LD_LIBRARY_PATH_32=${STUDIO_LIB}:`pwd`/dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86:${LD_LIBRARY_PATH}
export LD_LIBRARY_PATH_64=${STUDIO_LIB}/amd64:`pwd`/dist/SunOS-Previse_64/OracleSolarisStudio-Solaris-x86:${LD_LIBRARY_PATH_64}
export SUNW_DISCOVER_OPTIONS="-w -"

csh compile.bash 2&> dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86/out1.txt
bash compile.bash 2&> dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86/out2.txt
sh compile.bash 2&> dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86/out3.txt

export LD_PRELOAD=

cat ${__CND_BUILD_LOG__} | awk -f ./test.awk
rc=$?
if [ $rc != 0 ]
then
    exit $rc
fi

res=0
cat dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86/out*.txt | egrep "__logprint\(|execl\(|execle\(|execlp\(|execv\(|execve\(|execvp\(|posix_spawn\(|posix_spawnp\("
rc=$?
if [ $rc = 0 ]
then
    res=1
fi
exit $res