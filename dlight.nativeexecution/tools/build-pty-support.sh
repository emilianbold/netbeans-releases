#!/bin/sh

cd `dirname $0`
MYDIR=`pwd`

WDIR=/tmp/${USER}/ps
rm -rf ${WDIR}
mkdir -p ${WDIR}
LHOST=`hostname`.`cat /etc/resolv.conf | grep domain| sed 's/domain \(.*\)/\1/'`
LUSER=$USER

build() {
   HOST=$1
   shift
   CONFIGS=$@

   printf "Building ${CONFIGS} on host ${HOST} ... "
   do_build $HOST $CONFIGS
   if [ $? -eq 0 ]; then
      echo OK
   else
      echo FAIL
      exit 1
   fi
}

do_build() {
   HOST=$1
   shift
   CONFIGS=$@

   cd ${MYDIR}
   gtar cf ${WDIR}/ps.tar ./PtySupport || tar cf ${WDIR}/ps.tar ./PtySupport >> build.log 2>&1
   cat << EOF | ssh ${HOST} sh -s 
   rm -rf ${WDIR}
   mkdir -p ${WDIR}
EOF

   scp ${WDIR}/ps.tar ${HOST}:${WDIR} >> build.log 2>&1

   cat << EOF | ssh ${HOST} sh -s 
   cd ${WDIR}
   gtar xf ./ps.tar || tar xf ./ps.tar || return 1 
   cd PtySupport
   PATH=${PATH}:/usr/ccs/bin
   export PATH

   for i in ${CONFIGS}; do
      make CONF=\$i >> build.log 2>&1 || return 1
   done
   cd dist
   scp -r * ${LUSER}@${LHOST}:${WDIR} >> build.log 2>&1
EOF
}

copy_to_release() {
   echo Copying executables...
   set -x
   cp ${WDIR}/Solaris_x64/GNU-Solaris-x86/ptysupport ../release/bin/nativeexecution/SunOS-x86_64/pty
   cp ${WDIR}/Solaris_x86/GNU-Solaris-x86/ptysupport ../release/bin/nativeexecution/SunOS-x86/pty
   cp ${WDIR}/MacOS_x64/GNU-MacOSX/ptysupport ../release/bin/nativeexecution/MacOSX-x86_64/pty
   cp ${WDIR}/MacOS_x86/GNU-MacOSX/ptysupport ../release/bin/nativeexecution/MacOSX-x86/pty
   cp ${WDIR}/Linux_x86/GNU-Linux-x86/ptysupport ../release/bin/nativeexecution/Linux-x86/pty
   cp ${WDIR}/Linux_x64/GNU-Linux-x86/ptysupport ../release/bin/nativeexecution/Linux-x86_64/pty
   cp ${WDIR}/Solaris_sparc/GNU-Solaris-Sparc/ptysupport ../release/bin/nativeexecution/SunOS-sparc/pty
   cp ${WDIR}/Solaris_sparc64/GNU-Solaris-Sparc/ptysupport ../release/bin/nativeexecution/SunOS-sparc_64/pty
   # Only 32-bit version for Windows...
   cp ${WDIR}/Windows_x86/Cygwin-Windows/ptysupport.exe ../release/bin/nativeexecution/Windows-x86/pty
   cp ${WDIR}/Windows_x86/Cygwin-Windows/ptysupport.exe ../release/bin/nativeexecution/Windows-x86_64/pty
}


