#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
#
# Oracle and Java are registered trademarks of Oracle and/or its affiliates.
# Other names may be trademarks of their respective owners.
#
# The contents of this file are subject to the terms of either the GNU General Public
# License Version 2 only ("GPL") or the Common Development and Distribution
# License("CDDL") (collectively, the "License"). You may not use this file except in
# compliance with the License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
# License for the specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header Notice in
# each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
# designates this particular file as subject to the "Classpath" exception as provided
# by Oracle in the GPL Version 2 section of the License file that accompanied this code.
# If applicable, add the following below the License Header, with the fields enclosed
# by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
# 
# Contributor(s):
# 
# The Original Software is NetBeans. The Initial Developer of the Original Software
# is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
# Rights Reserved.
# 
# If you wish your version of this file to be governed by only the CDDL or only the
# GPL Version 2, indicate your decision by adding "[Contributor] elects to include
# this software in this distribution under the [CDDL or GPL Version 2] license." If
# you do not indicate a single choice of license, a recipient has the option to
# distribute your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above. However, if you
# add GPL Version 2 code and therefore, elected the GPL Version 2 license, then the
# option applies only if the new code is made subject to such option by the copyright
# holder.
# 

# Environment
MKDIR=mkdir
CP=cp
CCADMIN=CCadmin
RANLIB=ranlib
CC=gcc.exe
CCC=g++.exe
CXX=g++.exe
FC=f77.exe

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=build/Default-64bit/Cygwin-Windows

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/_ext/D_/work/nbi/engine/native/jnilib/windows/../.common/src/CommonUtils.o \
	${OBJECTDIR}/src/jni_WindowsNativeUtils.o \
	${OBJECTDIR}/src/jni_WindowsRegistry.o \
	${OBJECTDIR}/src/WindowsUtils.o

# C Compiler Flags
CFLAGS=-mno-cygwin

# CC Compiler Flags
CCFLAGS=-mno-cygwin
CXXFLAGS=-mno-cygwin

# Fortran Compiler Flags
FFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=\
	-lole32 \
	-luuid

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS} dist/windows.dll

dist/windows.dll: ${OBJECTFILES}
	${MKDIR} -p dist
	${LINK.c} -Wl,--add-stdcall-alias -shared -o dist/windows.dll -s ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/_ext/D_/work/nbi/engine/native/jnilib/windows/../.common/src/CommonUtils.o: ../.common/src/CommonUtils.c 
	${MKDIR} -p ${OBJECTDIR}/_ext/D_/work/nbi/engine/native/jnilib/windows/../.common/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/win32 -o ${OBJECTDIR}/_ext/D_/work/nbi/engine/native/jnilib/windows/../.common/src/CommonUtils.o ../.common/src/CommonUtils.c

${OBJECTDIR}/src/jni_WindowsNativeUtils.o: src/jni_WindowsNativeUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/win32 -o ${OBJECTDIR}/src/jni_WindowsNativeUtils.o src/jni_WindowsNativeUtils.c

${OBJECTDIR}/src/jni_WindowsRegistry.o: src/jni_WindowsRegistry.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/win32 -o ${OBJECTDIR}/src/jni_WindowsRegistry.o src/jni_WindowsRegistry.c

${OBJECTDIR}/src/WindowsUtils.o: src/WindowsUtils.c 
	${MKDIR} -p ${OBJECTDIR}/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/win32 -o ${OBJECTDIR}/src/WindowsUtils.o src/WindowsUtils.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/Default-64bit
	${RM} dist/windows.dll

# Subprojects
.clean-subprojects:
