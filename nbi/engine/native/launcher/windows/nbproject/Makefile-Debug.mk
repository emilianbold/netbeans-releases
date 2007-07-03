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
OBJECTDIR=build/Debug/Cygwin-Windows

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

${OBJECTDIR}/src/FileUtils.o: src/FileUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -g -Werror -s -o ${OBJECTDIR}/src/FileUtils.o src/FileUtils.c

${OBJECTDIR}/src/ProcessUtils.o: src/ProcessUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -g -Werror -s -o ${OBJECTDIR}/src/ProcessUtils.o src/ProcessUtils.c

${OBJECTDIR}/src/Main.o: src/Main.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -g -Werror -s -o ${OBJECTDIR}/src/Main.o src/Main.c

${OBJECTDIR}/src/Launcher.o: src/Launcher.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -g -Werror -s -o ${OBJECTDIR}/src/Launcher.o src/Launcher.c

${OBJECTDIR}/src/StringUtils.o: src/StringUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -g -Werror -s -o ${OBJECTDIR}/src/StringUtils.o src/StringUtils.c

${OBJECTDIR}/src/RegistryUtils.o: src/RegistryUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -g -Werror -s -o ${OBJECTDIR}/src/RegistryUtils.o src/RegistryUtils.c

${OBJECTDIR}/src/ExtractUtils.o: src/ExtractUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -g -Werror -s -o ${OBJECTDIR}/src/ExtractUtils.o src/ExtractUtils.c

${OBJECTDIR}/src/SystemUtils.o: src/SystemUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -g -Werror -s -o ${OBJECTDIR}/src/SystemUtils.o src/SystemUtils.c

${OBJECTDIR}/src/JavaUtils.o: src/JavaUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -g -Werror -s -o ${OBJECTDIR}/src/JavaUtils.o src/JavaUtils.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/Debug
	${RM} dist/nlw.exe

# Subprojects
.clean-subprojects:
