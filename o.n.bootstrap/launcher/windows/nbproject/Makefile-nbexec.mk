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
CC=gcc
CCC=g++
CXX=g++
FC=
AS=as

# Macros
CND_PLATFORM=Cygwin-Windows
CND_CONF=nbexec
CND_DISTDIR=dist

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=build/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/platformlauncher.o \
	${OBJECTDIR}/nbexec.o \
	${OBJECTDIR}/nbexecexe.o \
	${OBJECTDIR}/jvmlauncher.o \
	${OBJECTDIR}/utilsfuncs.o

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
LDLIBSOPTIONS=${OBJECTDIR}/nbexec.res

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	${MAKE}  -f nbproject/Makefile-nbexec.mk nbexec.dll

nbexec.dll: ${OBJECTDIR}/nbexec.res

nbexec.dll: ${OBJECTFILES}
	${LINK.cc} -mno-cygwin -shared -o nbexec.dll -fPIC ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/platformlauncher.o: nbproject/Makefile-${CND_CONF}.mk platformlauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -s -I/cygdrive/c/cygwin/usr/include/mingw -I/cygdrive/D/Program\ Files/Java/jdk1.6.0_22/include/win32 -I/cygdrive/D/Program\ Files/Java/jdk1.6.0_22/include -fPIC  -MMD -MP -MF $@.d -o ${OBJECTDIR}/platformlauncher.o platformlauncher.cpp

${OBJECTDIR}/nbexec.o: nbproject/Makefile-${CND_CONF}.mk nbexec.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -s -I/cygdrive/c/cygwin/usr/include/mingw -I/cygdrive/D/Program\ Files/Java/jdk1.6.0_22/include/win32 -I/cygdrive/D/Program\ Files/Java/jdk1.6.0_22/include -fPIC  -MMD -MP -MF $@.d -o ${OBJECTDIR}/nbexec.o nbexec.cpp

${OBJECTDIR}/nbexec.res: nbproject/Makefile-${CND_CONF}.mk nbexec.rc version.h
	${MKDIR} -p ${OBJECTDIR}
	@echo Compiling Resource files...
	windres.exe -Ocoff nbexec.rc ${OBJECTDIR}/nbexec.res

${OBJECTDIR}/nbexecexe.o: nbproject/Makefile-${CND_CONF}.mk nbexecexe.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -s -I/cygdrive/c/cygwin/usr/include/mingw -I/cygdrive/D/Program\ Files/Java/jdk1.6.0_22/include/win32 -I/cygdrive/D/Program\ Files/Java/jdk1.6.0_22/include -fPIC  -MMD -MP -MF $@.d -o ${OBJECTDIR}/nbexecexe.o nbexecexe.cpp

: nbproject/Makefile-${CND_CONF}.mk nbexec_exe.rc 
	@echo 
	

${OBJECTDIR}/jvmlauncher.o: nbproject/Makefile-${CND_CONF}.mk jvmlauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -s -I/cygdrive/c/cygwin/usr/include/mingw -I/cygdrive/D/Program\ Files/Java/jdk1.6.0_22/include/win32 -I/cygdrive/D/Program\ Files/Java/jdk1.6.0_22/include -fPIC  -MMD -MP -MF $@.d -o ${OBJECTDIR}/jvmlauncher.o jvmlauncher.cpp

${OBJECTDIR}/utilsfuncs.o: nbproject/Makefile-${CND_CONF}.mk utilsfuncs.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -s -I/cygdrive/c/cygwin/usr/include/mingw -I/cygdrive/D/Program\ Files/Java/jdk1.6.0_22/include/win32 -I/cygdrive/D/Program\ Files/Java/jdk1.6.0_22/include -fPIC  -MMD -MP -MF $@.d -o ${OBJECTDIR}/utilsfuncs.o utilsfuncs.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/nbexec
	${RM} nbexec.dll
	${RM} ${OBJECTDIR}/nbexec.res
	${RM} 

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
