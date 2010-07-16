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
CC=gcc
CCC=g++
CXX=g++
FC=
AS=gas

# Macros
CND_PLATFORM=GNU-Solaris-Sparc
CND_CONF=Solaris_sparc
CND_DISTDIR=dist

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=build/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/src/loop.o \
	${OBJECTDIR}/src/pty_fork.o \
	${OBJECTDIR}/src/pty.o \
	${OBJECTDIR}/src/error.o

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
	${MAKE}  -f nbproject/Makefile-Solaris_sparc.mk dist/Solaris_sparc/GNU-Solaris-Sparc/ptysupport

dist/Solaris_sparc/GNU-Solaris-Sparc/ptysupport: ${OBJECTFILES}
	${MKDIR} -p dist/Solaris_sparc/GNU-Solaris-Sparc
	${LINK.c} -o ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/ptysupport ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/src/loop.o: nbproject/Makefile-${CND_CONF}.mk src/loop.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/loop.o src/loop.c

${OBJECTDIR}/src/pty_fork.o: nbproject/Makefile-${CND_CONF}.mk src/pty_fork.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/pty_fork.o src/pty_fork.c

${OBJECTDIR}/src/pty.o: nbproject/Makefile-${CND_CONF}.mk src/pty.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/pty.o src/pty.c

${OBJECTDIR}/src/error.o: nbproject/Makefile-${CND_CONF}.mk src/error.c 
	${MKDIR} -p ${OBJECTDIR}/src
	${RM} $@.d
	$(COMPILE.c) -O2 -MMD -MP -MF $@.d -o ${OBJECTDIR}/src/error.o src/error.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r build/Solaris_sparc
	${RM} dist/Solaris_sparc/GNU-Solaris-Sparc/ptysupport

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
