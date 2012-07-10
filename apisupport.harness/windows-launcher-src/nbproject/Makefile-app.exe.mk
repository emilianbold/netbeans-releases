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
CC=gcc.exe
CCC=g++.exe
CXX=g++.exe
FC=gfortran
AS=as.exe

# Macros
CND_PLATFORM=Cygwin-Windows
CND_DLIB_EXT=dll
CND_CONF=app.exe
CND_DISTDIR=dist
CND_BUILDDIR=build

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=${CND_BUILDDIR}/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/applauncher.o \
	${OBJECTDIR}/_ext/493252820/utilsfuncs.o \
	${OBJECTDIR}/_ext/216238457/nblauncher.o \
	${OBJECTDIR}/app.o


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
LDLIBSOPTIONS=app.res

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	"${MAKE}"  -f nbproject/Makefile-${CND_CONF}.mk ../release/launchers/app.exe

../release/launchers/app.exe: ${OBJECTFILES}
	${MKDIR} -p ../release/launchers
	${LINK.cc} -mwindows -o ../release/launchers/app.exe ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/applauncher.o: applauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -DNBEXEC_DLL=\"/lib/nbexec.dll\" -DARCHITECTURE=32 -MMD -MP -MF $@.d -o ${OBJECTDIR}/applauncher.o applauncher.cpp

${OBJECTDIR}/_ext/493252820/utilsfuncs.o: ../../o.n.bootstrap/launcher/windows/utilsfuncs.cpp 
	${MKDIR} -p ${OBJECTDIR}/_ext/493252820
	${RM} $@.d
	$(COMPILE.cc) -O2 -DNBEXEC_DLL=\"/lib/nbexec.dll\" -DARCHITECTURE=32 -MMD -MP -MF $@.d -o ${OBJECTDIR}/_ext/493252820/utilsfuncs.o ../../o.n.bootstrap/launcher/windows/utilsfuncs.cpp

${OBJECTDIR}/_ext/216238457/nblauncher.o: ../../ide/launcher/windows/nblauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}/_ext/216238457
	${RM} $@.d
	$(COMPILE.cc) -O2 -DNBEXEC_DLL=\"/lib/nbexec.dll\" -DARCHITECTURE=32 -MMD -MP -MF $@.d -o ${OBJECTDIR}/_ext/216238457/nblauncher.o ../../ide/launcher/windows/nblauncher.cpp

${OBJECTDIR}/app.o: app.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -DNBEXEC_DLL=\"/lib/nbexec.dll\" -DARCHITECTURE=32 -MMD -MP -MF $@.d -o ${OBJECTDIR}/app.o app.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r ${CND_BUILDDIR}/${CND_CONF}
	${RM} ../release/launchers/app.exe

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
