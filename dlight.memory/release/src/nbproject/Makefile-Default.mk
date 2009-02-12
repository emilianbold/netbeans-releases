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
FC=g77

# Macros
PLATFORM=GNU-Solaris-x86

# Include project Makefile
include src-Makefile.mk

# Object Directory
OBJECTDIR=build/Default/${PLATFORM}

# Object Files
OBJECTFILES=

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
.build-conf: ${BUILD_SUBPROJECTS}
	cd . && gmake -f Makefile all

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	cd . && gmake -f Makefile clean

# Subprojects
.clean-subprojects:
