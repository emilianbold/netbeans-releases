# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html
# or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License file at
# nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
# particular file as subject to the "Classpath" exception as provided
# by Sun in the GPL Version 2 section of the License file that
# accompanied this code. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# Contributor(s):
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.
#
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 2, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 2] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 2 code and therefore, elected the GPL
# Version 2 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.

INSTALLDIR = /dev/null

#SOEXT-Windows_XP := dll
SOEXT-Linux := so
SOEXT-SunOS := so
SOEXT-Mac_OS_X := dylib

#CFLAGS-Windows_XP := -g -c -mno-cygwin
CFLAGS-Linux := -c -g -fPIC
CFLAGS-SunOS := -c -g -fPIC
CFLAGS-Mac_OS_X := -c -g -fPIC

ifdef 64BITS
    FLAG64 := -m64
endif
ifdef DEBUG
	DBGFLAG := -DDEBUG=${DEBUG}
endif

# The following sets UNAME_S to a platform dependent string. Since
# this gets evaluated each use, its assigned to "OSNAME" and that
# is the actual value used in the rest of the Makefile.
UNAME_S := $(shell uname -s)
UNAME_P := $(shell uname -p)
UNAME_M := $(shell uname -m)

OSNAME = $(UNAME_S)
ifeq ($(findstring _NT-,$(UNAME_S)), _NT-)  # catches Cygwin and MinGW
	OSNAME = Windows
endif
ifeq (Darwin,$(UNAME_S))
	OSNAME = Mac_OS_X
endif

LDFLAGS_LIB-Linux = -g -shared -fPIC
LDFLAGS_LIB-SunOS = -g -shared -fPIC
LDFLAGS_LIB-Mac_OS_X = -dynamiclib -g -fPIC

LDFLAGS_APP-Linux = -g -fPIC
LDFLAGS_APP-SunOS = -g -fPIC
LDFLAGS_APP-Mac_OS_X = -g -fPIC

SOEXT := ${SOEXT-${OSNAME}}
CFLAGS := ${CFLAGS-${OSNAME}} ${FLAG64} ${DBGFLAG}
LDFLAGS_LIB = ${LDFLAGS_LIB-${OSNAME}} ${FLAG64}
LDFLAGS_APP = ${LDFLAGS_APP-${OSNAME}} ${FLAG64}

ifdef 64BITS
    PLATFORM_64 := _64
else
    PLATFORM_64 :=
endif

PLATFORM-i386  :=  x86${PLATFORM_64}
PLATFORM-i686  :=  x86${PLATFORM_64}
PLATFORM-x86_64  :=  x86${PLATFORM_64}
PLATFORM-sparc  :=  sparc${PLATFORM_64}
ifeq (${UNAME_P}, unknown)
	PLATFORM = ${PLATFORM-${UNAME_M}}
else
	PLATFORM = ${PLATFORM-${UNAME_P}}
endif

testit :sh= uname -s

DIST_BASE=../release/bin
BUILD_BASE=./build

DIST_PATH=${DIST_BASE}/$(OSNAME)-$(PLATFORM)
BUILD_PATH=${BUILD_BASE}/$(OSNAME)-$(PLATFORM)

MEM_APP_NAME=mmonitor
MEM_LIB_NAME=magent
SYN_APP_NAME=smonitor
SYN_LIB_NAME=sagent

MEM_LIB_PATH=${DIST_PATH}/${MEM_LIB_NAME}.${SOEXT}
MEM_APP_PATH=${DIST_PATH}/${MEM_APP_NAME}
SYN_LIB_PATH=${DIST_PATH}/${SYN_LIB_NAME}.${SOEXT}
SYN_APP_PATH=${DIST_PATH}/${SYN_APP_NAME}

MEM_LIB_OBJ=${BUILD_PATH}/${MEM_LIB_NAME}.o
MEM_APP_OBJ=${BUILD_PATH}/${MEM_APP_NAME}.o
SYN_LIB_OBJ=${BUILD_PATH}/${SYN_LIB_NAME}.o
SYN_APP_OBJ=${BUILD_PATH}/${SYN_APP_NAME}.o

all: check init ${MEM_LIB_PATH} ${MEM_APP_PATH} ${SYN_LIB_PATH} ${SYN_APP_PATH}

${BUILD_PATH}/%.o : %.c
	gcc ${CFLAGS} -o ${BUILD_PATH}/$*.o $*.c

# memory monitor
${MEM_APP_PATH}: ${MEM_APP_OBJ}
	gcc ${LDFLAGS_APP} -o ${MEM_APP_PATH} ${MEM_APP_OBJ}

# memory monitor agent
${MEM_LIB_PATH}: ${MEM_LIB_OBJ}
	gcc ${LDFLAGS_LIB} -o ${MEM_LIB_PATH} ${MEM_LIB_OBJ}

# sync monitor 
${SYN_APP_PATH}: ${SYN_APP_OBJ}
	gcc -lrt ${LDFLAGS_APP} -o ${SYN_APP_PATH} ${SYN_APP_OBJ}

# sync monitor agent
${SYN_LIB_PATH}: ${SYN_LIB_OBJ}
	gcc -ldl ${LDFLAGS_LIB} -o ${SYN_LIB_PATH} ${SYN_LIB_OBJ}

clean:
	rm -rf ${BUILD_PATH} ${DIST_PATH}

cleanall:
	rm -rf ${BUILD_BASE} ${DIST_BASE}

install: all
	@if [ -n "$(INSTALLDIR)" -a -d $(INSTALLDIR) ]; \
	then \
		mkdir -p $(INSTALLDIR); \
		echo "Installing ${MEM_LIB_PATH} in $(INSTALLDIR)"; \
		cp ${MEM_LIB_PATH} $(INSTALLDIR); \
		echo "Installing ${SYN_LIB_PATH} in $(INSTALLDIR)"; \
		cp ${SYN_LIB_PATH} $(INSTALLDIR); \
	else \
		echo "Error: Missing INSTALLDIR"; \
		exit 1; \
	fi

init:
	@mkdir -p ${BUILD_PATH} ${DIST_PATH}

check:
	@if [ -n "$(testit)" ]; \
	then \
		echo "Error: You must use GNU make"; \
		exit 1; \
	fi

test:
	@echo "UNAME_S = [${UNAME_S}]"
	@echo "UNAME_M = [${UNAME_M}]"
	@echo "UNAME_P = [${UNAME_P}]"
	@echo "OSNAME = [${OSNAME}]"
	@echo "PLATFORM = [${PLATFORM}]"
	@echo "SOEXT = [${SOEXT}]"
	@echo "CFLAGS = [${CFLAGS}]"
	@echo "LDFLAGS_LIB = [${LDFLAGS_LIB}]"
	@echo "LDFLAGS_APP = [${LDFLAGS_APP}]"
	@echo "${MEM_LIB_NAME} = [${MEM_LIB_PATH}]"
	@echo "${MEM_APP_NAME} = [${MEM_APP_PATH}]"
	@echo "${SYN_LIB_NAME} = [${SYN_LIB_PATH}]"
	@echo "${SYN_APP_NAME} = [${SYN_APP_PATH}]"
