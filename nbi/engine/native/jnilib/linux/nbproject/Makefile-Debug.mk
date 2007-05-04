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
	build/Debug/GNU-Linux-x86/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/linux/../.common/src/CommonUtils.o \
	build/Debug/GNU-Linux-x86/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/linux/../.unix/src/UnixUtils.o \
	build/Debug/GNU-Linux-x86/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/linux/../.unix/src/jni_UnixNativeUtils.o

# C Compiler Flags
CFLAGS=-shared -m32 -static-libgcc

# CC Compiler Flags
CCFLAGS=-shared -m32 -static-libgcc
CXXFLAGS=-shared -m32 -static-libgcc

# Fortran Compiler Flags
FFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS} dist/linux.so

dist/linux.so: ${OBJECTFILES}
	@${MKDIR} -p dist
	${LINK.c} -shared -static-libgcc -o dist/linux.so -s ${OBJECTFILES} ${LDLIBSOPTIONS} 

build/Debug/GNU-Linux-x86/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/linux/../.common/src/CommonUtils.o: ../.common/src/CommonUtils.c 
	@${MKDIR} -p build/Debug/GNU-Linux-x86/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/linux/../.common/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/linux -o build/Debug/GNU-Linux-x86/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/linux/../.common/src/CommonUtils.o ../.common/src/CommonUtils.c

build/Debug/GNU-Linux-x86/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/linux/../.unix/src/UnixUtils.o: ../.unix/src/UnixUtils.c 
	@${MKDIR} -p build/Debug/GNU-Linux-x86/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/linux/../.unix/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/linux -o build/Debug/GNU-Linux-x86/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/linux/../.unix/src/UnixUtils.o ../.unix/src/UnixUtils.c

build/Debug/GNU-Linux-x86/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/linux/../.unix/src/jni_UnixNativeUtils.o: ../.unix/src/jni_UnixNativeUtils.c 
	@${MKDIR} -p build/Debug/GNU-Linux-x86/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/linux/../.unix/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/linux -o build/Debug/GNU-Linux-x86/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/linux/../.unix/src/jni_UnixNativeUtils.o ../.unix/src/jni_UnixNativeUtils.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/Debug
	${RM} dist/linux.so

# Subprojects
.clean-subprojects:
