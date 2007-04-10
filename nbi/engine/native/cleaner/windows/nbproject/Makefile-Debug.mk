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
	build/Debug/GNU-Windows/src/main.o

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
	-lcomctl32

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS} dist/cleaner.exe

dist/cleaner.exe: ${OBJECTFILES}
	${MKDIR} -p dist
	${LINK.c} -mno-cygwin -mwindows -o dist/cleaner -s ${OBJECTFILES} ${LDLIBSOPTIONS} 

build/Debug/GNU-Windows/src/main.o: src/main.c 
	${MKDIR} -p build/Debug/GNU-Windows/src
	$(COMPILE.c) -g -o build/Debug/GNU-Windows/src/main.o src/main.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/Debug
	${RM} dist/cleaner.exe

# Subprojects
.clean-subprojects:
