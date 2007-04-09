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
CC=gcc
CCC=g++
CXX=g++
FC=g77

# Include project Makefile
include Makefile

# Object Files
OBJECTFILES= \
	build/Release/GNU-Windows/src/FileUtils.o \
	build/Release/GNU-Windows/src/ProcessUtils.o \
	build/Release/GNU-Windows/src/Main.o \
	build/Release/GNU-Windows/src/Launcher.o \
	build/Release/GNU-Windows/src/StringUtils.o \
	build/Release/GNU-Windows/src/RegistryUtils.o \
	build/Release/GNU-Windows/src/ExtractUtils.o \
	build/Release/GNU-Windows/src/SystemUtils.o \
	build/Release/GNU-Windows/src/JavaUtils.o

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
.build-conf: ${BUILD_SUBPROJECTS} dist/Release/GNU-Windows/windows.exe

dist/Release/GNU-Windows/windows.exe: ${OBJECTFILES}
	${MKDIR} -p dist/Release/GNU-Windows
	${LINK.c} -o dist/Release/GNU-Windows/windows ${OBJECTFILES} ${LDLIBSOPTIONS} 

build/Release/GNU-Windows/src/FileUtils.o: src/FileUtils.c 
	${MKDIR} -p build/Release/GNU-Windows/src
	$(COMPILE.c) -O2 -o build/Release/GNU-Windows/src/FileUtils.o src/FileUtils.c

build/Release/GNU-Windows/src/ProcessUtils.o: src/ProcessUtils.c 
	${MKDIR} -p build/Release/GNU-Windows/src
	$(COMPILE.c) -O2 -o build/Release/GNU-Windows/src/ProcessUtils.o src/ProcessUtils.c

build/Release/GNU-Windows/src/Main.o: src/Main.c 
	${MKDIR} -p build/Release/GNU-Windows/src
	$(COMPILE.c) -O2 -o build/Release/GNU-Windows/src/Main.o src/Main.c

build/Release/GNU-Windows/src/Launcher.o: src/Launcher.c 
	${MKDIR} -p build/Release/GNU-Windows/src
	$(COMPILE.c) -O2 -o build/Release/GNU-Windows/src/Launcher.o src/Launcher.c

build/Release/GNU-Windows/src/StringUtils.o: src/StringUtils.c 
	${MKDIR} -p build/Release/GNU-Windows/src
	$(COMPILE.c) -O2 -o build/Release/GNU-Windows/src/StringUtils.o src/StringUtils.c

build/Release/GNU-Windows/src/RegistryUtils.o: src/RegistryUtils.c 
	${MKDIR} -p build/Release/GNU-Windows/src
	$(COMPILE.c) -O2 -o build/Release/GNU-Windows/src/RegistryUtils.o src/RegistryUtils.c

build/Release/GNU-Windows/src/ExtractUtils.o: src/ExtractUtils.c 
	${MKDIR} -p build/Release/GNU-Windows/src
	$(COMPILE.c) -O2 -o build/Release/GNU-Windows/src/ExtractUtils.o src/ExtractUtils.c

build/Release/GNU-Windows/src/SystemUtils.o: src/SystemUtils.c 
	${MKDIR} -p build/Release/GNU-Windows/src
	$(COMPILE.c) -O2 -o build/Release/GNU-Windows/src/SystemUtils.o src/SystemUtils.c

build/Release/GNU-Windows/src/JavaUtils.o: src/JavaUtils.c 
	${MKDIR} -p build/Release/GNU-Windows/src
	$(COMPILE.c) -O2 -o build/Release/GNU-Windows/src/JavaUtils.o src/JavaUtils.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/Release
	${RM} dist/Release/GNU-Windows/windows.exe

# Subprojects
.clean-subprojects:
