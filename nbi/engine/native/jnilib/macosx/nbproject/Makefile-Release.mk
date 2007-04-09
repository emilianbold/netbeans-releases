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
	build/Release/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src/UnixUtils.o \
	build/Release/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.common/src/CommonUtils.o \
	build/Release/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src/jni_UnixNativeUtils.o

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
	@${MKDIR} -p dist/Release/GNU-Generic
	${LINK.c} -shared -o dist/Release/GNU-Generic/libmacosx.so ${OBJECTFILES} ${LDLIBSOPTIONS} 

build/Release/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src/UnixUtils.o: ../.unix/src/UnixUtils.c 
	@${MKDIR} -p build/Release/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src
	$(COMPILE.c) -O2 -o build/Release/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src/UnixUtils.o ../.unix/src/UnixUtils.c

build/Release/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.common/src/CommonUtils.o: ../.common/src/CommonUtils.c 
	@${MKDIR} -p build/Release/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.common/src
	$(COMPILE.c) -O2 -o build/Release/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.common/src/CommonUtils.o ../.common/src/CommonUtils.c

build/Release/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src/jni_UnixNativeUtils.o: ../.unix/src/jni_UnixNativeUtils.c 
	@${MKDIR} -p build/Release/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src
	$(COMPILE.c) -O2 -o build/Release/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src/jni_UnixNativeUtils.o ../.unix/src/jni_UnixNativeUtils.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/Release
	${RM} dist/Release/GNU-Generic/libmacosx.so

# Subprojects
.clean-subprojects:
