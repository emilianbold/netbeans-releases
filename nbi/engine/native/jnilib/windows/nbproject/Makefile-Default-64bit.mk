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
CC=gcc.exe
CCC=g++.exe
CXX=g++.exe
FC=f77.exe

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=build/Default-64bit/Cygwin-Windows

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/_ext/D_/work/nbi/engine/native/jnilib/windows/../.common/src/CommonUtils.o \
	${OBJECTDIR}/src/jni_WindowsNativeUtils.o \
	${OBJECTDIR}/src/jni_WindowsRegistry.o \
	${OBJECTDIR}/src/WindowsUtils.o

# C Compiler Flags
CFLAGS=-mno-cygwin

# CC Compiler Flags
CCFLAGS=-mno-cygwin
CXXFLAGS=-mno-cygwin

# Fortran Compiler Flags
FFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=\
	-lole32 \
	-luuid

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS} dist/windows.dll

dist/windows.dll: ${OBJECTFILES}
	${MKDIR} -p dist
	${LINK.c} -Wl,--add-stdcall-alias -shared -o dist/windows.dll -s ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/_ext/D_/work/nbi/engine/native/jnilib/windows/../.common/src/CommonUtils.o: ../.common/src/CommonUtils.c 
	${MKDIR} -p ${OBJECTDIR}/_ext/D_/work/nbi/engine/native/jnilib/windows/../.common/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/win32 -o ${OBJECTDIR}/_ext/D_/work/nbi/engine/native/jnilib/windows/../.common/src/CommonUtils.o ../.common/src/CommonUtils.c

${OBJECTDIR}/src/jni_WindowsNativeUtils.o: src/jni_WindowsNativeUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/win32 -o ${OBJECTDIR}/src/jni_WindowsNativeUtils.o src/jni_WindowsNativeUtils.c

${OBJECTDIR}/src/jni_WindowsRegistry.o: src/jni_WindowsRegistry.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/win32 -o ${OBJECTDIR}/src/jni_WindowsRegistry.o src/jni_WindowsRegistry.c

${OBJECTDIR}/src/WindowsUtils.o: src/WindowsUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/win32 -o ${OBJECTDIR}/src/WindowsUtils.o src/WindowsUtils.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/Default-64bit
	${RM} dist/windows.dll

# Subprojects
.clean-subprojects:
