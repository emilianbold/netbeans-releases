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
CND_DLIB_EXT=dll
CND_CONF=app64.exe
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
CCFLAGS=-m64 -mno-cygwin -static-libgcc -static-libstdc++
CXXFLAGS=-m64 -mno-cygwin -static-libgcc -static-libstdc++

# Fortran Compiler Flags
FFLAGS=

# Assembler Flags
ASFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=app64.res

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	"${MAKE}"  -f nbproject/Makefile-${CND_CONF}.mk ../release/launchers/app64.exe

../release/launchers/app64.exe: ${OBJECTFILES}
	${MKDIR} -p ../release/launchers
	${LINK.cc} -mwindows -o ../release/launchers/app64.exe ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/applauncher.o: applauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -DNBEXEC_DLL=\"/lib/nbexec64.dll\" -DARCHITECTURE=64 -MMD -MP -MF $@.d -o ${OBJECTDIR}/applauncher.o applauncher.cpp

${OBJECTDIR}/_ext/493252820/utilsfuncs.o: ../../o.n.bootstrap/launcher/windows/utilsfuncs.cpp 
	${MKDIR} -p ${OBJECTDIR}/_ext/493252820
	${RM} $@.d
	$(COMPILE.cc) -O2 -DNBEXEC_DLL=\"/lib/nbexec64.dll\" -DARCHITECTURE=64 -MMD -MP -MF $@.d -o ${OBJECTDIR}/_ext/493252820/utilsfuncs.o ../../o.n.bootstrap/launcher/windows/utilsfuncs.cpp

${OBJECTDIR}/_ext/216238457/nblauncher.o: ../../ide/launcher/windows/nblauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}/_ext/216238457
	${RM} $@.d
	$(COMPILE.cc) -O2 -DNBEXEC_DLL=\"/lib/nbexec64.dll\" -DARCHITECTURE=64 -MMD -MP -MF $@.d -o ${OBJECTDIR}/_ext/216238457/nblauncher.o ../../ide/launcher/windows/nblauncher.cpp

${OBJECTDIR}/app.o: app.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -DNBEXEC_DLL=\"/lib/nbexec64.dll\" -DARCHITECTURE=64 -MMD -MP -MF $@.d -o ${OBJECTDIR}/app.o app.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r ${CND_BUILDDIR}/${CND_CONF}
	${RM} ../release/launchers/app64.exe

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
