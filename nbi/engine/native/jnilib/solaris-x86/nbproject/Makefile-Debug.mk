#
# Gererated Makefile - do not edit!
#
# Edit the Makefile in the project folder instead (../Makefile). Each target
# has a -pre and a -post target defined where you can add custumized code.
#
# This makefile implements configuration specific macros and targets.


# Environment
MKDIR=mkdir
CP=cp
CCADMIN=CCadmin
RANLIB=ranlib
CC=gcc
CCC=g++
CXX=g++
FC=g77

# Include project Makefile
include Makefile

# Object Files
OBJECTFILES= \
	build/Debug/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-x86/../.common/src/CommonUtils.o \
	build/Debug/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-x86/../.unix/src/UnixUtils.o \
	build/Debug/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-x86/../.unix/src/jni_UnixNativeUtils.o

# C Compiler Flags
CFLAGS=-m32 -fPIC -shared

# CC Compiler Flags
CCFLAGS=-m32 -fPIC -shared
CXXFLAGS=-m32 -fPIC -shared

# Fortran Compiler Flags
FFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS} dist/solaris-x86.so

dist/solaris-x86.so: ${OBJECTFILES}
	@${MKDIR} -p dist
	${LINK.c} -shared -o dist/solaris-x86.so -s ${OBJECTFILES} ${LDLIBSOPTIONS} 

build/Debug/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-x86/../.common/src/CommonUtils.o: ../.common/src/CommonUtils.c 
	@${MKDIR} -p build/Debug/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-x86/../.common/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/solaris -o build/Debug/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-x86/../.common/src/CommonUtils.o ../.common/src/CommonUtils.c

build/Debug/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-x86/../.unix/src/UnixUtils.o: ../.unix/src/UnixUtils.c 
	@${MKDIR} -p build/Debug/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-x86/../.unix/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/solaris -o build/Debug/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-x86/../.unix/src/UnixUtils.o ../.unix/src/UnixUtils.c

build/Debug/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-x86/../.unix/src/jni_UnixNativeUtils.o: ../.unix/src/jni_UnixNativeUtils.c 
	@${MKDIR} -p build/Debug/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-x86/../.unix/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/solaris -o build/Debug/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-x86/../.unix/src/jni_UnixNativeUtils.o ../.unix/src/jni_UnixNativeUtils.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/Debug
	${RM} dist/solaris-x86.so

# Subprojects
.clean-subprojects:
