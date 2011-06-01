#!/bin/sh

echo Copying executables...
set -x
cp dist/Solaris_x64/GNU-Solaris-x86/ptysupport ../../release/bin/nativeexecution/SunOS-x86_64/pty
cp dist/Solaris_x86/GNU-Solaris-x86/ptysupport ../../release/bin/nativeexecution/SunOS-x86/pty
cp dist/MacOS_x64/GNU-MacOSX/ptysupport ../../release/bin/nativeexecution/MacOSX-x86_64/pty
cp dist/MacOS_x86/GNU-MacOSX/ptysupport ../../release/bin/nativeexecution/MacOSX-x86/pty
cp dist/Linux_x86/GNU-Linux-x86/ptysupport ../../release/bin/nativeexecution/Linux-x86/pty
cp dist/Linux_x64/GNU-Linux-x86/ptysupport ../../release/bin/nativeexecution/Linux-x86_64/pty
cp dist/Solaris_sparc/GNU-Solaris-Sparc/ptysupport ../../release/bin/nativeexecution/SunOS-sparc/pty
cp dist/Solaris_sparc64/GNU-Solaris-Sparc/ptysupport ../../release/bin/nativeexecution/SunOS-sparc_64/pty
# Only 32-bit version for Windows...
cp dist/Windows_x86/Cygwin-Windows/ptysupport.exe ../../release/bin/nativeexecution/Windows-x86/pty
cp dist/Windows_x86/Cygwin-Windows/ptysupport.exe ../../release/bin/nativeexecution/Windows-x86_64/pty

#../../release/bin/nativeexecution/Windows-x86/pty
#../../release/bin/nativeexecution/Windows-x86_64/pty





