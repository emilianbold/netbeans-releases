# -*- Makefile -*-
#
#                 Sun Public License Notice
# 
# The contents of this file are subject to the Sun Public License
# Version 1.0 (the "License"). You may not use this file except in
# compliance with the License. A copy of the License is available at
# http://www.sun.com/
# 
# The Original Code is Forte for Java, Community Edition. The Initial
# Developer of the Original Code is Sun Microsystems, Inc. Portions
# Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
#

JDK_HOME = c:\jdk1.3.1_03

INCLUDES = -I. -I$(JDK_HOME)/include -I$(JDK_HOME)/include/win32

SRC_DIR = ../src

all: jtrace.dll

jtrace.dll: $(SRC_DIR)/jtrace.cpp
	cl -LD -MD -DWIN32_LEAN_AND_MEAN $(INCLUDES) $(SRC_DIR)/jtrace.cpp -o $@

clean:
	del *.dll *.obj
