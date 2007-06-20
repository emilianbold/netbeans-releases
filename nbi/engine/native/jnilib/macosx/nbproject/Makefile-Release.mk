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
FC=

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=build/Release/GNU-Generic

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/_ext/Users/lipin/tmp/nbi/engine/native/jnilib/macosx/../.common/src/CommonUtils.o \
	${OBJECTDIR}/_ext/Users/lipin/tmp/nbi/engine/native/jnilib/macosx/../.unix/src/jni_UnixNativeUtils.o

# C Compiler Flags
CFLAGS=

# CC Compiler Flags
CCFLAGS=
CXXFLAGS=

# Fortran Compiler Flags
FFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS} dist/Release/GNU-Generic/libmacosx.so

dist/Release/GNU-Generic/libmacosx.so: ${OBJECTFILES}
	${MKDIR} -p dist/Release/GNU-Generic
	${LINK.c} -shared -o dist/Release/GNU-Generic/libmacosx.so ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/_ext/Users/lipin/tmp/nbi/engine/native/jnilib/macosx/../.common/src/CommonUtils.o: ../.common/src/CommonUtils.c 
	${MKDIR} -p ${OBJECTDIR}/_ext/Users/lipin/tmp/nbi/engine/native/jnilib/macosx/../.common/src
	$(COMPILE.c) -O2 -o ${OBJECTDIR}/_ext/Users/lipin/tmp/nbi/engine/native/jnilib/macosx/../.common/src/CommonUtils.o ../.common/src/CommonUtils.c

${OBJECTDIR}/_ext/Users/lipin/tmp/nbi/engine/native/jnilib/macosx/../.unix/src/jni_UnixNativeUtils.o: ../.unix/src/jni_UnixNativeUtils.c 
	${MKDIR} -p ${OBJECTDIR}/_ext/Users/lipin/tmp/nbi/engine/native/jnilib/macosx/../.unix/src
	$(COMPILE.c) -O2 -o ${OBJECTDIR}/_ext/Users/lipin/tmp/nbi/engine/native/jnilib/macosx/../.unix/src/jni_UnixNativeUtils.o ../.unix/src/jni_UnixNativeUtils.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/Release
	${RM} dist/Release/GNU-Generic/libmacosx.so

# Subprojects
.clean-subprojects:
