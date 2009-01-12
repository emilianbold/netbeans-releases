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
OBJECTDIR=build/Release/${PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/_ext/E_/work/netbeans/hg/main_work/apisupport.harness/windows-launcher-src/../../ide/launcher/windows/nblauncher.o \
	${OBJECTDIR}/_ext/E_/work/netbeans/hg/main_work/apisupport.harness/windows-launcher-src/../../o.n.bootstrap/launcher/windows/utilsfuncs.o \
	${OBJECTDIR}/app.o \
	${OBJECTDIR}/applauncher.o

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
LDLIBSOPTIONS=app.res

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	${MAKE}  -f nbproject/Makefile-Release.mk ../release/launchers/app.exe.exe

../release/launchers/app.exe.exe: ${OBJECTFILES}
	${MKDIR} -p ../release/launchers
	${LINK.cc} -mwindows -o ../release/launchers/app.exe ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/_ext/E_/work/netbeans/hg/main_work/apisupport.harness/windows-launcher-src/../../ide/launcher/windows/nblauncher.o: ../../ide/launcher/windows/nblauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}/_ext/E_/work/netbeans/hg/main_work/apisupport.harness/windows-launcher-src/../../ide/launcher/windows
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -I/cygdrive/c/cygwin/usr/include/mingw -MMD -MP -MF $@.d -o ${OBJECTDIR}/_ext/E_/work/netbeans/hg/main_work/apisupport.harness/windows-launcher-src/../../ide/launcher/windows/nblauncher.o ../../ide/launcher/windows/nblauncher.cpp

${OBJECTDIR}/_ext/E_/work/netbeans/hg/main_work/apisupport.harness/windows-launcher-src/../../o.n.bootstrap/launcher/windows/utilsfuncs.o: ../../o.n.bootstrap/launcher/windows/utilsfuncs.cpp 
	${MKDIR} -p ${OBJECTDIR}/_ext/E_/work/netbeans/hg/main_work/apisupport.harness/windows-launcher-src/../../o.n.bootstrap/launcher/windows
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -I/cygdrive/c/cygwin/usr/include/mingw -MMD -MP -MF $@.d -o ${OBJECTDIR}/_ext/E_/work/netbeans/hg/main_work/apisupport.harness/windows-launcher-src/../../o.n.bootstrap/launcher/windows/utilsfuncs.o ../../o.n.bootstrap/launcher/windows/utilsfuncs.cpp

${OBJECTDIR}/app.o: app.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -I/cygdrive/c/cygwin/usr/include/mingw -MMD -MP -MF $@.d -o ${OBJECTDIR}/app.o app.cpp

${OBJECTDIR}/applauncher.o: applauncher.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -Wall -I/cygdrive/c/cygwin/usr/include/mingw -MMD -MP -MF $@.d -o ${OBJECTDIR}/applauncher.o applauncher.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/Release
	${RM} ../release/launchers/app.exe.exe

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
