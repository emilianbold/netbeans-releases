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
CC=gcc
CCC=g++
CXX=g++
FC=gfortran
AS=as

# Macros
CND_PLATFORM=GNU-MacOSX
CND_CONF=MacOS_x86
CND_DISTDIR=dist
CND_BUILDDIR=build

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=${CND_BUILDDIR}/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/src/pty_fork.o \
	${OBJECTDIR}/src/pty.o \
	${OBJECTDIR}/src/error.o \
	${OBJECTDIR}/src/loop.o


# C Compiler Flags
CFLAGS=-m32 --std=c99

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
	"${MAKE}"  -f nbproject/Makefile-${CND_CONF}.mk ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/ptysupport

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/ptysupport: ${OBJECTFILES}
	${MKDIR} -p ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}
	${LINK.c} -o ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/ptysupport -Wl,-S ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/src/pty_fork.o: src/pty_fork.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -s -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/pty_fork.o src/pty_fork.c

${OBJECTDIR}/src/pty.o: src/pty.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -s -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/pty.o src/pty.c

${OBJECTDIR}/src/error.o: src/error.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -s -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/error.o src/error.c

${OBJECTDIR}/src/loop.o: src/loop.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -s -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/loop.o src/loop.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r ${CND_BUILDDIR}/${CND_CONF}
	${RM} ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/ptysupport

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
