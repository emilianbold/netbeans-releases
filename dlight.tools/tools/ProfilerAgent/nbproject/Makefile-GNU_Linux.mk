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
AS=as

# Macros
CND_PLATFORM=GNU-Linux-x86
CND_CONF=GNU_Linux
CND_DISTDIR=dist

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=build/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/prof_agent.o

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
LDLIBSOPTIONS=-pthread -ldl -lrt

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	${MAKE}  -f nbproject/Makefile-GNU_Linux.mk prof_agent.so

prof_agent.so: ${OBJECTFILES}
	${LINK.c} ldscript.txt -shared -o prof_agent.so -fPIC ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/prof_agent.o: nbproject/Makefile-${CND_CONF}.mk prof_agent.c 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.c) -fPIC  -MMD -MP -MF $@.d -o ${OBJECTDIR}/prof_agent.o prof_agent.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r build/GNU_Linux
	${RM} prof_agent.so

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
