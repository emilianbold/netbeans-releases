# -*- Makefile -*-
#
# The contents of this file are subject to the terms of the Common Development
# and Distribution License (the License). You may not use this file except in
# compliance with the License.
#
# You can obtain a copy of the License at http://www.netbeans.org/cddl.html
# or http://www.netbeans.org/cddl.txt.
#
# When distributing Covered Code, include this CDDL Header Notice in each file
# and include the License file at http://www.netbeans.org/cddl.txt.
# If applicable, add the following below the CDDL Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# The Original Software is Forte for Java, Community Edition. The Initial
# Developer of the Original Software is Sun Microsystems, Inc. Portions
# Copyright 1997-2006 Sun Microsystems, Inc. All Rights Reserved.
#

JDK_HOME = c:\jdk1.3.1_03

INCLUDES = -I. -I$(JDK_HOME)/include -I$(JDK_HOME)/include/win32

SRC_DIR = ../src

all: jtrace.dll

jtrace.dll: $(SRC_DIR)/jtrace.cpp
	cl -LD -MD -DWIN32_LEAN_AND_MEAN $(INCLUDES) $(SRC_DIR)/jtrace.cpp -o $@

clean:
	del *.dll *.obj
