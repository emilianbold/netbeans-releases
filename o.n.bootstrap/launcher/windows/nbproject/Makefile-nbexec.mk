#
# Generated Makefile - do not edit!
#
# Edit the Makefile in the project folder instead (../Makefile). Each target
# has a -pre and a -post target defined where you can add customized code.
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
FC=
AS=

# Macros
PLATFORM=Cygwin-Windows

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=build/nbexec/${PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/jvmlauncher.o \
	${OBJECTDIR}/platformlauncher.o \
	${OBJECTDIR}/utilsfuncs.o \
	${OBJECTDIR}/nbexec.o \
	${OBJECTDIR}/nbexecexe.o

# C Compiler Flags
CFLAGS=-m32

# CC Compiler Flags
CCFLAGS=-m32 -s -mno-cygwin
CXXFLAGS=-m32 -s -mno-cygwin

# Fortran Compiler Flags
FFLAGS=

# Assembler Flags
ASFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	${MAKE}  -f nbproject/Makefile-nbexec.mk nbexec.dll

nbexec.dll: ${OBJECTFILES}
	${LINK.cc} -mno-cygwin -shared -o nbexec.dll -fPIC ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/jvmlauncher.o: jvmlauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -s -I/cygdrive/c/cygwin/usr/include/mingw -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_10/include -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_10/include/win32 -fPIC  -MMD -MP -MF $@.d -o ${OBJECTDIR}/jvmlauncher.o jvmlauncher.cpp

${OBJECTDIR}/platformlauncher.o: platformlauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -s -I/cygdrive/c/cygwin/usr/include/mingw -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_10/include -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_10/include/win32 -fPIC  -MMD -MP -MF $@.d -o ${OBJECTDIR}/platformlauncher.o platformlauncher.cpp

${OBJECTDIR}/utilsfuncs.o: utilsfuncs.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -s -I/cygdrive/c/cygwin/usr/include/mingw -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_10/include -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_10/include/win32 -fPIC  -MMD -MP -MF $@.d -o ${OBJECTDIR}/utilsfuncs.o utilsfuncs.cpp

${OBJECTDIR}/nbexec.o: nbexec.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -s -I/cygdrive/c/cygwin/usr/include/mingw -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_10/include -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_10/include/win32 -fPIC  -MMD -MP -MF $@.d -o ${OBJECTDIR}/nbexec.o nbexec.cpp

${OBJECTDIR}/nbexecexe.o: nbexecexe.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -s -I/cygdrive/c/cygwin/usr/include/mingw -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_10/include -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_10/include/win32 -fPIC  -MMD -MP -MF $@.d -o ${OBJECTDIR}/nbexecexe.o nbexecexe.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/nbexec
	${RM} nbexec.dll

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
