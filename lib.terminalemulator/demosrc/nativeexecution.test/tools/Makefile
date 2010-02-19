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

APP_NAME=ptysatellite

#CFLAGS-Windows_XP := -c -mno-cygwin
CFLAGS-Linux := -c -fPIC
CFLAGS-SunOS := -c -fPIC
CFLAGS-Mac_OS_X := -c -fPIC

ifdef 64BITS
    FLAG64 := -m64
else
    FLAG64 := -m32
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

CFLAGS := ${CFLAGS-${OSNAME}}

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

DIST=../release/bin/ptysupport
BUILD=./build

PLATFORM_DIR=$(OSNAME)-$(PLATFORM)
APP_PATH=${DIST}/${PLATFORM_DIR}/${APP_NAME}
APP_OUT=${BUILD}/${PLATFORM_DIR}/${APP_NAME}.o

all: check init ${APP_PATH}

#appliction:
${APP_PATH}: ${APP_OUT}
	gcc ${LDFLAGS_APP} ${FLAG64} -o ${APP_PATH} ${APP_OUT}

${APP_OUT}: ${APP_NAME}.c
	gcc ${CFLAGS} ${FLAG64} -o ${APP_OUT} ${APP_NAME}.c

clean:
	rm -rf ${BUILD}/${PLATFORM_DIR} ${DIST}/${PLATFORM_DIR}

cleanall:
	rm -rf ${BUILD} ${DIST}

install: all
	@if [ -n "$(INSTALLDIR)" -a -d $(INSTALLDIR) ]; \
	then \
		mkdir -p $(INSTALLDIR); \
		cp ${LIB_PATH} $(INSTALLDIR); \
		echo "Installing ${LIB_NAME}-${PLATFORM_DIR}.$(SOEXT) in $(INSTALLDIR)"; \
	else \
		echo "Error: Missing INSTALLDIR"; \
		exit 1; \
	fi

init:
	@mkdir -p ${BUILD}/${PLATFORM_DIR} ${DIST}/${PLATFORM_DIR}

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
	@echo "CFLAGS = [${CFLAGS}]"
	@echo "${APP_NAME} = [${APP_PATH}]"
