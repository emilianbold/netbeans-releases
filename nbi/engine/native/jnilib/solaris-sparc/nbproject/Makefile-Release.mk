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
	build/Release/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-sparc/../.common/src/CommonUtils.o \
	build/Release/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-sparc/../.unix/src/UnixUtils.o \
	build/Release/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-sparc/../.unix/src/jni_UnixNativeUtils.o

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
.build-conf: ${BUILD_SUBPROJECTS} dist/Release/GNU-Solaris-Sparc/libsolaris-sparc.so

dist/Release/GNU-Solaris-Sparc/libsolaris-sparc.so: ${OBJECTFILES}
	@${MKDIR} -p dist/Release/GNU-Solaris-Sparc
	${LINK.c} -shared -static-libgcc -mimpure-text -o dist/Release/GNU-Solaris-Sparc/libsolaris-sparc.so ${OBJECTFILES} ${LDLIBSOPTIONS} 

build/Release/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-sparc/../.common/src/CommonUtils.o: ../.common/src/CommonUtils.c 
	@${MKDIR} -p build/Release/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-sparc/../.common/src
	$(COMPILE.c) -O2 -o build/Release/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-sparc/../.common/src/CommonUtils.o ../.common/src/CommonUtils.c

build/Release/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-sparc/../.unix/src/UnixUtils.o: ../.unix/src/UnixUtils.c 
	@${MKDIR} -p build/Release/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-sparc/../.unix/src
	$(COMPILE.c) -O2 -o build/Release/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-sparc/../.unix/src/UnixUtils.o ../.unix/src/UnixUtils.c

build/Release/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-sparc/../.unix/src/jni_UnixNativeUtils.o: ../.unix/src/jni_UnixNativeUtils.c 
	@${MKDIR} -p build/Release/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-sparc/../.unix/src
	$(COMPILE.c) -O2 -o build/Release/GNU-Solaris-Sparc/_ext/export/home/ksorokin/Work/nbi-trunk/engine/native/solaris-sparc/../.unix/src/jni_UnixNativeUtils.o ../.unix/src/jni_UnixNativeUtils.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/Release
	${RM} dist/Release/GNU-Solaris-Sparc/libsolaris-sparc.so

# Subprojects
.clean-subprojects:
