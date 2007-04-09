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
	build/Debug/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src/UnixUtils.o \
	build/Debug/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.common/src/CommonUtils.o \
	build/Debug/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src/jni_UnixNativeUtils.o

# C Compiler Flags
CFLAGS=-arch i386 -arch ppc -isysroot /Developer/SDKs/MacOSX10.4u.sdk -dynamiclib

# CC Compiler Flags
CCFLAGS=-arch i386 -arch ppc -isysroot /Developer/SDKs/MacOSX10.4u.sdk -dynamiclib
CXXFLAGS=-arch i386 -arch ppc -isysroot /Developer/SDKs/MacOSX10.4u.sdk -dynamiclib

# Fortran Compiler Flags
FFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS} dist/macosx.dylib

dist/macosx.dylib: ${OBJECTFILES}
	@${MKDIR} -p dist
	${LINK.c} -Wl,-syslibroot /Developer/SDKs/MacOSX10.4u.sdk -arch i386 -arch ppc -shared -o dist/macosx.dylib -s ${OBJECTFILES} ${LDLIBSOPTIONS} 

build/Debug/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src/UnixUtils.o: ../.unix/src/UnixUtils.c 
	@${MKDIR} -p build/Debug/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src
	$(COMPILE.c) -s -I/usr/java/include -o build/Debug/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src/UnixUtils.o ../.unix/src/UnixUtils.c

build/Debug/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.common/src/CommonUtils.o: ../.common/src/CommonUtils.c 
	@${MKDIR} -p build/Debug/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.common/src
	$(COMPILE.c) -s -I/usr/java/include -o build/Debug/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.common/src/CommonUtils.o ../.common/src/CommonUtils.c

build/Debug/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src/jni_UnixNativeUtils.o: ../.unix/src/jni_UnixNativeUtils.c 
	@${MKDIR} -p build/Debug/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src
	$(COMPILE.c) -s -I/usr/java/include -o build/Debug/GNU-Generic/_ext/Users/tester/ks152834/Work/nbi-trunk/engine/native/macosx/../.unix/src/jni_UnixNativeUtils.o ../.unix/src/jni_UnixNativeUtils.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/Debug
	${RM} dist/macosx.dylib

# Subprojects
.clean-subprojects:
