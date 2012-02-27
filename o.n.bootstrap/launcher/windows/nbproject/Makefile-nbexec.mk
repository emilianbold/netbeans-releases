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
FC=gfortran.exe
AS=as.exe

# Macros
CND_PLATFORM=Cygwin_1-Windows
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
	${OBJECTDIR}/platformlauncher.o \
	${OBJECTDIR}/nbexec.o \
	${OBJECTDIR}/utilsfuncs.o \
	${OBJECTDIR}/nbexecexe.o


# C Compiler Flags
CFLAGS=-m64

# CC Compiler Flags
CCFLAGS=-m64 -s -mno-cygwin -static-libgcc -static-libstdc++
CXXFLAGS=-m64 -s -mno-cygwin -static-libgcc -static-libstdc++

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
	${LINK.cc} -mno-cygwin -shared -o nbexec.dll ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/jvmlauncher.o: jvmlauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -s -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include/win32  -MMD -MP -MF $@.d -o ${OBJECTDIR}/jvmlauncher.o jvmlauncher.cpp

: nbexec_exe.rc 
	@echo 
	

${OBJECTDIR}/nbexec.res: nbexec.rc version.h
	${MKDIR} -p ${OBJECTDIR}
	@echo Compiling Resource files...
	x86_64-w64-mingw32-windres.exe -Ocoff nbexec.rc ${OBJECTDIR}/nbexec.res

${OBJECTDIR}/platformlauncher.o: platformlauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -s -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include/win32  -MMD -MP -MF $@.d -o ${OBJECTDIR}/platformlauncher.o platformlauncher.cpp

${OBJECTDIR}/nbexec.o: nbexec.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -s -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include/win32  -MMD -MP -MF $@.d -o ${OBJECTDIR}/nbexec.o nbexec.cpp

${OBJECTDIR}/utilsfuncs.o: utilsfuncs.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -s -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include/win32  -MMD -MP -MF $@.d -o ${OBJECTDIR}/utilsfuncs.o utilsfuncs.cpp

${OBJECTDIR}/nbexecexe.o: nbexecexe.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -s -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include -I/cygdrive/C/Program\ Files/Java/jdk1.6.0_30/include/win32  -MMD -MP -MF $@.d -o ${OBJECTDIR}/nbexecexe.o nbexecexe.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r ${CND_BUILDDIR}/${CND_CONF}
	${RM} nbexec.dll
	${RM} 
	${RM} ${OBJECTDIR}/nbexec.res

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
