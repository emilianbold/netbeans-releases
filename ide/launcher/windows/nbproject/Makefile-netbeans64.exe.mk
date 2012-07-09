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
CND_CONF=netbeans64.exe
CND_DISTDIR=dist
CND_BUILDDIR=build

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=${CND_BUILDDIR}/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/netbeans.o \
	${OBJECTDIR}/nblauncher.o \
	${OBJECTDIR}/_ext/1413142467/utilsfuncs.o


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
LDLIBSOPTIONS=netbeans64.res

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	"${MAKE}"  -f nbproject/Makefile-${CND_CONF}.mk netbeans64.exe

netbeans64.exe: ${OBJECTFILES}
	${LINK.cc} -mwindows -o netbeans64.exe ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/netbeans.o: netbeans.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -s -DNBEXEC_DLL=\"/lib/nbexec64.dll\" -DARCHITECTURE=64 -MMD -MP -MF $@.d -o ${OBJECTDIR}/netbeans.o netbeans.cpp

${OBJECTDIR}/nblauncher.o: nblauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -s -DNBEXEC_DLL=\"/lib/nbexec64.dll\" -DARCHITECTURE=64 -MMD -MP -MF $@.d -o ${OBJECTDIR}/nblauncher.o nblauncher.cpp

${OBJECTDIR}/_ext/1413142467/utilsfuncs.o: ../../../o.n.bootstrap/launcher/windows/utilsfuncs.cpp 
	${MKDIR} -p ${OBJECTDIR}/_ext/1413142467
	${RM} $@.d
	$(COMPILE.cc) -O2 -s -DNBEXEC_DLL=\"/lib/nbexec64.dll\" -DARCHITECTURE=64 -MMD -MP -MF $@.d -o ${OBJECTDIR}/_ext/1413142467/utilsfuncs.o ../../../o.n.bootstrap/launcher/windows/utilsfuncs.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r ${CND_BUILDDIR}/${CND_CONF}
	${RM} netbeans64.exe

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
