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

SOEXT-Windows_XP := dll
SOEXT-Linux := so
SOEXT-SunOS := so
SOEXT-Mac_OS_X := dylib

CFLAGS-Windows_XP := -g -c -mno-cygwin
CFLAGS-Linux := -c -g -fPIC
CFLAGS-SunOS := -c -g -fPIC
CFLAGS-Mac_OS_X := -c -g -fPIC

ifdef 64BITS
    FLAG64 := -m64
endif

LDFLAGS-Windows_XP = -mno-cygwin -Wl,--out-implib,build/$(OSNAME)-$(PLATFORM)/libGdbHelper.import.a -shared
LDFLAGS-Linux = -g -shared -fPIC
LDFLAGS-SunOS = -g -shared -fPIC
LDFLAGS-Mac_OS_X = -dynamiclib -g -fPIC -install_name GdbHelper-${OSNAME}-${PLATFORM}.${SOEXT}

# The following sets UNAME_S to a platform dependent string. Since
# this gets evaluated each use, its assigned to "OSNAME" and that
# is the actual value used in the rest of the Makefile.
UNAME_S := $(shell uname -s)
UNAME_P := $(shell uname -p)
UNAME_M := $(shell uname -m)

OSNAME = $(UNAME_S)
ifeq ($(findstring _NT-,$(UNAME_S)), _NT-)  # catches Cygwin and MinGW
	OSNAME = Windows_XP
endif
ifeq (Darwin,$(UNAME_S))
	OSNAME = Mac_OS_X
endif

SOEXT := ${SOEXT-${OSNAME}}
CFLAGS := ${CFLAGS-${OSNAME}} ${FLAG64}
LDFLAGS = ${LDFLAGS-${OSNAME}} ${FLAG64}

ifdef 64BITS
    PLATFORM_64 := _64
else
    PLATFORM_64 :=
endif

PLATFORM-i386  :=  x86${PLATFORM_64}
PLATFORM-i686  :=  x86${PLATFORM_64}
PLATFORM-sparc  :=  sparc${PLATFORM_64}
ifeq (${UNAME_P}, unknown)
	PLATFORM = ${PLATFORM-${UNAME_M}}
else
	PLATFORM = ${PLATFORM-${UNAME_P}}
endif

testit :sh= uname -s

all: check init dist/$(OSNAME)-$(PLATFORM)/GdbHelper-$(OSNAME)-$(PLATFORM).${SOEXT} dist/$(OSNAME)-$(PLATFORM)/unbuffer-$(OSNAME)-$(PLATFORM).${SOEXT}

dist/$(OSNAME)-$(PLATFORM)/GdbHelper-$(OSNAME)-$(PLATFORM).${SOEXT}: \
		build/$(OSNAME)-$(PLATFORM)/GdbHelper.o
	gcc ${LDFLAGS} -o dist/$(OSNAME)-$(PLATFORM)/GdbHelper-$(OSNAME)-$(PLATFORM).${SOEXT} \
		build/$(OSNAME)-$(PLATFORM)/GdbHelper.o

build/$(OSNAME)-$(PLATFORM)/GdbHelper.o: GdbHelper.c
	gcc ${CFLAGS} -o build/$(OSNAME)-$(PLATFORM)/GdbHelper.o GdbHelper.c

dist/$(OSNAME)-$(PLATFORM)/unbuffer-$(OSNAME)-$(PLATFORM).${SOEXT}: \
		build/$(OSNAME)-$(PLATFORM)/unbuffer.o
	gcc ${LDFLAGS} -o dist/$(OSNAME)-$(PLATFORM)/unbuffer-$(OSNAME)-$(PLATFORM).${SOEXT} \
		build/$(OSNAME)-$(PLATFORM)/unbuffer.o

build/$(OSNAME)-$(PLATFORM)/unbuffer.o: unbuffer.c
	gcc ${CFLAGS} -o build/$(OSNAME)-$(PLATFORM)/unbuffer.o unbuffer.c 

clean:
	rm -rf build/$(OSNAME)-$(PLATFORM) dist/$(OSNAME)-$(PLATFORM)

cleanall:
	rm -rf build dist

install: all
	@if [ -n "$(INSTALLDIR)" -a -d $(INSTALLDIR) ]; \
	then \
		mkdir -p $(INSTALLDIR); \
		cp dist/$(OSNAME)-$(PLATFORM)/GdbHelper-$(OSNAME)-$(PLATFORM).${SOEXT} $(INSTALLDIR); \
		echo "Installing GdbHelper-$(OSNAME)-$(PLATFORM).$(SOEXT) in $(INSTALLDIR)"; \
	else \
		echo "Error: Missing INSTALLDIR"; \
		exit 1; \
	fi

init:
	@mkdir -p build/$(OSNAME)-$(PLATFORM) dist/$(OSNAME)-$(PLATFORM)

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
	@echo "LDFLAGS = [${LDFLAGS}]"
	@echo "GdbHelper = [GdbHelper-${OSNAME}-${PLATFORM}.${SOEXT}]"
