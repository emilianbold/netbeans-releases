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
CND_CONF=Release
CND_DISTDIR=dist
CND_BUILDDIR=build

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=${CND_BUILDDIR}/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/src/Main.o \
	${OBJECTDIR}/src/StringUtils.o \
	${OBJECTDIR}/src/RegistryUtils.o \
	${OBJECTDIR}/src/SystemUtils.o \
	${OBJECTDIR}/src/ProcessUtils.o \
	${OBJECTDIR}/src/Launcher.o \
	${OBJECTDIR}/src/JavaUtils.o \
	${OBJECTDIR}/src/FileUtils.o \
	${OBJECTDIR}/src/ExtractUtils.o


# C Compiler Flags
CFLAGS=

# CC Compiler Flags
CCFLAGS=
CXXFLAGS=

# Fortran Compiler Flags
FFLAGS=

# Assembler Flags
ASFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	"${MAKE}"  -f nbproject/Makefile-${CND_CONF}.mk ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/windows.exe

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/windows.exe: ${OBJECTFILES}
	${MKDIR} -p ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}
	${LINK.c} -o ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/windows ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/src/Main.o: src/Main.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/Main.o src/Main.c

${OBJECTDIR}/src/StringUtils.o: src/StringUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/StringUtils.o src/StringUtils.c

${OBJECTDIR}/src/RegistryUtils.o: src/RegistryUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/RegistryUtils.o src/RegistryUtils.c

${OBJECTDIR}/src/SystemUtils.o: src/SystemUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/SystemUtils.o src/SystemUtils.c

${OBJECTDIR}/src/ProcessUtils.o: src/ProcessUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/ProcessUtils.o src/ProcessUtils.c

${OBJECTDIR}/src/Launcher.o: src/Launcher.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/Launcher.o src/Launcher.c

${OBJECTDIR}/src/JavaUtils.o: src/JavaUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/JavaUtils.o src/JavaUtils.c

${OBJECTDIR}/src/FileUtils.o: src/FileUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/FileUtils.o src/FileUtils.c

${OBJECTDIR}/src/ExtractUtils.o: src/ExtractUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/ExtractUtils.o src/ExtractUtils.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r ${CND_BUILDDIR}/${CND_CONF}
	${RM} ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/windows.exe

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
