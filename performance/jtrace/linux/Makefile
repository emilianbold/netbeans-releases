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

JDK_HOME = /usr/local/jdk

INCLUDES = -I. -I$(JDK_HOME)/include -I$(JDK_HOME)/include/linux

SRC_DIR = ../src

all: libjtrace.so

libjtrace.so: $(SRC_DIR)/jtrace.cpp
	cc $(INCLUDES) $(SRC_DIR)/jtrace.cpp -shared -o libjtrace.so

clean:
	rm -f *.so
