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
OBJECTDIR=build/netbeans.exe/${PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/netbeans.o \
	${OBJECTDIR}/_ext/E_/work/netbeans/hg/main_work/ide/launcher/windows/../../../o.n.bootstrap/launcher/windows/utilsfuncs.o \
	${OBJECTDIR}/nblauncher.o

# C Compiler Flags
CFLAGS=

# CC Compiler Flags
CCFLAGS=-m32 -s -mno-cygwin
CXXFLAGS=-m32 -s -mno-cygwin

# Fortran Compiler Flags
FFLAGS=

# Assembler Flags
ASFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=netbeans.res

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	${MAKE}  -f nbproject/Makefile-netbeans.exe.mk netbeans.exe.exe

netbeans.exe.exe: ${OBJECTFILES}
	${LINK.cc} -mwindows -o netbeans.exe ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/netbeans.o: netbeans.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -I/cygdrive/c/cygwin/usr/include/mingw -MMD -MP -MF $@.d -o ${OBJECTDIR}/netbeans.o netbeans.cpp

${OBJECTDIR}/_ext/E_/work/netbeans/hg/main_work/ide/launcher/windows/../../../o.n.bootstrap/launcher/windows/utilsfuncs.o: ../../../o.n.bootstrap/launcher/windows/utilsfuncs.cpp 
	${MKDIR} -p ${OBJECTDIR}/_ext/E_/work/netbeans/hg/main_work/ide/launcher/windows/../../../o.n.bootstrap/launcher/windows
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -I/cygdrive/c/cygwin/usr/include/mingw -MMD -MP -MF $@.d -o ${OBJECTDIR}/_ext/E_/work/netbeans/hg/main_work/ide/launcher/windows/../../../o.n.bootstrap/launcher/windows/utilsfuncs.o ../../../o.n.bootstrap/launcher/windows/utilsfuncs.cpp

${OBJECTDIR}/nblauncher.o: nblauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -I/cygdrive/c/cygwin/usr/include/mingw -MMD -MP -MF $@.d -o ${OBJECTDIR}/nblauncher.o nblauncher.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/netbeans.exe
	${RM} netbeans.exe.exe

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
