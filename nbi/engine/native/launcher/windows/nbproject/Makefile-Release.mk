#
# Gererated Makefile - do not edit!
#
# Edit the Makefile in the project folder instead (../Makefile). Each target
# has a -pre and a -post target defined where you can add custumized code.
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

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=build/Release/Cygwin-Windows

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/src/FileUtils.o \
	${OBJECTDIR}/src/ProcessUtils.o \
	${OBJECTDIR}/src/Main.o \
	${OBJECTDIR}/src/Launcher.o \
	${OBJECTDIR}/src/StringUtils.o \
	${OBJECTDIR}/src/RegistryUtils.o \
	${OBJECTDIR}/src/ExtractUtils.o \
	${OBJECTDIR}/src/SystemUtils.o \
	${OBJECTDIR}/src/JavaUtils.o

# C Compiler Flags
CFLAGS=

# CC Compiler Flags
CCFLAGS=
CXXFLAGS=

# Fortran Compiler Flags
FFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS} dist/Release/Cygwin-Windows/windows.exe

dist/Release/Cygwin-Windows/windows.exe: ${OBJECTFILES}
	${MKDIR} -p dist/Release/Cygwin-Windows
	${LINK.c} -o dist/Release/Cygwin-Windows/windows ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/src/FileUtils.o: src/FileUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -O2 -o ${OBJECTDIR}/src/FileUtils.o src/FileUtils.c

${OBJECTDIR}/src/ProcessUtils.o: src/ProcessUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -O2 -o ${OBJECTDIR}/src/ProcessUtils.o src/ProcessUtils.c

${OBJECTDIR}/src/Main.o: src/Main.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -O2 -o ${OBJECTDIR}/src/Main.o src/Main.c

${OBJECTDIR}/src/Launcher.o: src/Launcher.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -O2 -o ${OBJECTDIR}/src/Launcher.o src/Launcher.c

${OBJECTDIR}/src/StringUtils.o: src/StringUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -O2 -o ${OBJECTDIR}/src/StringUtils.o src/StringUtils.c

${OBJECTDIR}/src/RegistryUtils.o: src/RegistryUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -O2 -o ${OBJECTDIR}/src/RegistryUtils.o src/RegistryUtils.c

${OBJECTDIR}/src/ExtractUtils.o: src/ExtractUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -O2 -o ${OBJECTDIR}/src/ExtractUtils.o src/ExtractUtils.c

${OBJECTDIR}/src/SystemUtils.o: src/SystemUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -O2 -o ${OBJECTDIR}/src/SystemUtils.o src/SystemUtils.c

${OBJECTDIR}/src/JavaUtils.o: src/JavaUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -O2 -o ${OBJECTDIR}/src/JavaUtils.o src/JavaUtils.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/Release
	${RM} dist/Release/Cygwin-Windows/windows.exe

# Subprojects
.clean-subprojects:
