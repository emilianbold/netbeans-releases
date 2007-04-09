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
	build/Debug/GNU-Windows/src/FileUtils.o \
	build/Debug/GNU-Windows/src/ProcessUtils.o \
	build/Debug/GNU-Windows/src/Main.o \
	build/Debug/GNU-Windows/src/Launcher.o \
	build/Debug/GNU-Windows/src/StringUtils.o \
	build/Debug/GNU-Windows/src/RegistryUtils.o \
	build/Debug/GNU-Windows/src/ExtractUtils.o \
	build/Debug/GNU-Windows/src/SystemUtils.o \
	build/Debug/GNU-Windows/src/JavaUtils.o

# C Compiler Flags
CFLAGS=

# CC Compiler Flags
CCFLAGS=
CXXFLAGS=

# Fortran Compiler Flags
FFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=\
	-lole32 \
	-luuid \
	-lkernel32 \
	-lcomctl32 \
	-luserenv

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS} dist/nlw.exe

dist/nlw.exe: ${OBJECTFILES}
	${MKDIR} -p dist
	${LINK.c} -mwindows -mno-cygwin build/icon.o -o dist/nlw -s ${OBJECTFILES} ${LDLIBSOPTIONS} 

build/Debug/GNU-Windows/src/FileUtils.o: src/FileUtils.c 
	${MKDIR} -p build/Debug/GNU-Windows/src
	$(COMPILE.c) -g -s -o build/Debug/GNU-Windows/src/FileUtils.o src/FileUtils.c

build/Debug/GNU-Windows/src/ProcessUtils.o: src/ProcessUtils.c 
	${MKDIR} -p build/Debug/GNU-Windows/src
	$(COMPILE.c) -g -s -o build/Debug/GNU-Windows/src/ProcessUtils.o src/ProcessUtils.c

build/Debug/GNU-Windows/src/Main.o: src/Main.c 
	${MKDIR} -p build/Debug/GNU-Windows/src
	$(COMPILE.c) -g -s -o build/Debug/GNU-Windows/src/Main.o src/Main.c

build/Debug/GNU-Windows/src/Launcher.o: src/Launcher.c 
	${MKDIR} -p build/Debug/GNU-Windows/src
	$(COMPILE.c) -g -s -o build/Debug/GNU-Windows/src/Launcher.o src/Launcher.c

build/Debug/GNU-Windows/src/StringUtils.o: src/StringUtils.c 
	${MKDIR} -p build/Debug/GNU-Windows/src
	$(COMPILE.c) -g -s -o build/Debug/GNU-Windows/src/StringUtils.o src/StringUtils.c

build/Debug/GNU-Windows/src/RegistryUtils.o: src/RegistryUtils.c 
	${MKDIR} -p build/Debug/GNU-Windows/src
	$(COMPILE.c) -g -s -o build/Debug/GNU-Windows/src/RegistryUtils.o src/RegistryUtils.c

build/Debug/GNU-Windows/src/ExtractUtils.o: src/ExtractUtils.c 
	${MKDIR} -p build/Debug/GNU-Windows/src
	$(COMPILE.c) -g -s -o build/Debug/GNU-Windows/src/ExtractUtils.o src/ExtractUtils.c

build/Debug/GNU-Windows/src/SystemUtils.o: src/SystemUtils.c 
	${MKDIR} -p build/Debug/GNU-Windows/src
	$(COMPILE.c) -g -s -o build/Debug/GNU-Windows/src/SystemUtils.o src/SystemUtils.c

build/Debug/GNU-Windows/src/JavaUtils.o: src/JavaUtils.c 
	${MKDIR} -p build/Debug/GNU-Windows/src
	$(COMPILE.c) -g -s -o build/Debug/GNU-Windows/src/JavaUtils.o src/JavaUtils.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/Debug
	${RM} dist/nlw.exe

# Subprojects
.clean-subprojects:
