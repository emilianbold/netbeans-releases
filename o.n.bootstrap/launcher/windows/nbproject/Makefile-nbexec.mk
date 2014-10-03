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
GREP=grep
NM=nm
CCADMIN=CCadmin
RANLIB=ranlib
CC=gcc
CCC=g++
CXX=g++
FC=gfortran
AS=as

# Macros
CND_PLATFORM=Cygwin-Windows
CND_DLIB_EXT=dll
CND_CONF=nbexec
CND_DISTDIR=dist
CND_BUILDDIR=build

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=${CND_BUILDDIR}/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/jvmlauncher.o \
	${OBJECTDIR}/nbexec.o \
	${OBJECTDIR}/nbexecexe.o \
	${OBJECTDIR}/platformlauncher.o \
	${OBJECTDIR}/utilsfuncs.o


# C Compiler Flags
CFLAGS=

# CC Compiler Flags
CCFLAGS=-m32 -mno-cygwin
CXXFLAGS=-m32 -mno-cygwin

# Fortran Compiler Flags
FFLAGS=

# Assembler Flags
ASFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=${OBJECTDIR}/nbexec.res

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	"${MAKE}"  -f nbproject/Makefile-${CND_CONF}.mk nbexec.dll

nbexec.dll: ${OBJECTDIR}/nbexec.res

nbexec.dll: ${OBJECTFILES}
	${LINK.cc} -o nbexec.dll ${OBJECTFILES} ${LDLIBSOPTIONS} -Wl,--nxcompat -Wl,--dynamicbase -Wl,--no-seh -mno-cygwin -shared

${OBJECTDIR}/jvmlauncher.o: jvmlauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2 -s -DNBEXEC_DLL=\"nbexec.dll\" -I/cygdrive/C/Program\ Files/Java/jdk1.7.0_67/include -I/cygdrive/C/Program\ Files/Java/jdk1.7.0_67/include/win32  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/jvmlauncher.o jvmlauncher.cpp

${OBJECTDIR}/nbexec.o: nbexec.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2 -s -DNBEXEC_DLL=\"nbexec.dll\" -I/cygdrive/C/Program\ Files/Java/jdk1.7.0_67/include -I/cygdrive/C/Program\ Files/Java/jdk1.7.0_67/include/win32  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/nbexec.o nbexec.cpp

${OBJECTDIR}/nbexec.res: nbexec.rc version.h
	${MKDIR} -p ${OBJECTDIR}
	@echo Compiling Resource files...
	windres.exe -Ocoff nbexec.rc ${OBJECTDIR}/nbexec.res

: nbexec_exe.rc 
	@echo 
	

${OBJECTDIR}/nbexecexe.o: nbexecexe.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2 -s -DNBEXEC_DLL=\"nbexec.dll\" -I/cygdrive/C/Program\ Files/Java/jdk1.7.0_67/include -I/cygdrive/C/Program\ Files/Java/jdk1.7.0_67/include/win32  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/nbexecexe.o nbexecexe.cpp

${OBJECTDIR}/platformlauncher.o: platformlauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2 -s -DNBEXEC_DLL=\"nbexec.dll\" -I/cygdrive/C/Program\ Files/Java/jdk1.7.0_67/include -I/cygdrive/C/Program\ Files/Java/jdk1.7.0_67/include/win32  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/platformlauncher.o platformlauncher.cpp

${OBJECTDIR}/utilsfuncs.o: utilsfuncs.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2 -s -DNBEXEC_DLL=\"nbexec.dll\" -I/cygdrive/C/Program\ Files/Java/jdk1.7.0_67/include -I/cygdrive/C/Program\ Files/Java/jdk1.7.0_67/include/win32  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/utilsfuncs.o utilsfuncs.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r ${CND_BUILDDIR}/${CND_CONF}
	${RM} nbexec.dll
	${RM} ${OBJECTDIR}/nbexec.res
	${RM} 

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
