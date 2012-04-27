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
CC=x86_64-w64-mingw32-gcc.exe
CCC=x86_64-w64-mingw32-g++.exe
CXX=x86_64-w64-mingw32-g++.exe
FC=gfortran
AS=as.exe

# Macros
CND_PLATFORM=Cygwin64-Windows
CND_CONF=nbexec64
CND_DISTDIR=dist
CND_BUILDDIR=build

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=${CND_BUILDDIR}/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/jvmlauncher.o \
	${OBJECTDIR}/platformlauncher.o \
	${OBJECTDIR}/nbexec.o \
	${OBJECTDIR}/utilsfuncs.o \
	${OBJECTDIR}/nbexecexe.o


# C Compiler Flags
CFLAGS=

# CC Compiler Flags
CCFLAGS=-m64 -mno-cygwin -static-libgcc -static-libstdc++
CXXFLAGS=-m64 -mno-cygwin -static-libgcc -static-libstdc++

# Fortran Compiler Flags
FFLAGS=

# Assembler Flags
ASFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=${OBJECTDIR}/nbexec64.res

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	"${MAKE}"  -f nbproject/Makefile-${CND_CONF}.mk nbexec64.dll

nbexec64.dll: ${OBJECTDIR}/nbexec64.res

nbexec64.dll: ${OBJECTFILES}
	${LINK.cc} -mno-cygwin -shared -o nbexec64.dll ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/jvmlauncher.o: jvmlauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -s -DNBEXEC_DLL=\"nbexec64.dll\" -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include/win32  -MMD -MP -MF $@.d -o ${OBJECTDIR}/jvmlauncher.o jvmlauncher.cpp

: nbexec_exe.rc 
	@echo 
	

${OBJECTDIR}/nbexec64.res: nbexec.rc version.h
	${MKDIR} -p ${OBJECTDIR}
	@echo Compiling Resource files...
	x86_64-w64-mingw32-windres.exe -Ocoff nbexec.rc ${OBJECTDIR}/nbexec64.res

${OBJECTDIR}/platformlauncher.o: platformlauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -s -DNBEXEC_DLL=\"nbexec64.dll\" -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include/win32  -MMD -MP -MF $@.d -o ${OBJECTDIR}/platformlauncher.o platformlauncher.cpp

${OBJECTDIR}/nbexec.o: nbexec.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -s -DNBEXEC_DLL=\"nbexec64.dll\" -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include/win32  -MMD -MP -MF $@.d -o ${OBJECTDIR}/nbexec.o nbexec.cpp

${OBJECTDIR}/utilsfuncs.o: utilsfuncs.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -s -DNBEXEC_DLL=\"nbexec64.dll\" -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include/win32  -MMD -MP -MF $@.d -o ${OBJECTDIR}/utilsfuncs.o utilsfuncs.cpp

${OBJECTDIR}/nbexecexe.o: nbexecexe.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -s -DNBEXEC_DLL=\"nbexec64.dll\" -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include/win32  -MMD -MP -MF $@.d -o ${OBJECTDIR}/nbexecexe.o nbexecexe.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r ${CND_BUILDDIR}/${CND_CONF}
	${RM} nbexec64.dll
	${RM} 
	${RM} ${OBJECTDIR}/nbexec64.res

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
