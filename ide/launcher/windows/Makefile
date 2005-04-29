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
# Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
#

LIBS =
#LIBS = advapi32.lib user32.lib

CFLAGS = -s -mno-cygwin
#!ifdef DEBUG
#	CFLAGS = $(CFLAGS) -Zi
#!endif

all : netbeans.exe nb.exe

netbeans.exe : netbeans.cpp netbeans.res
	gcc $(CFLAGS) $(LIBS) -mwindows -DWINMAIN netbeans.cpp -o netbeans.exe netbeans.res

nb.exe : netbeans.cpp
	gcc $(CFLAGS) $(LIBS) netbeans.cpp -o nb.exe

netbeans.res : netbeans.rc
	windres -o$@ -Ocoff netbeans.rc
  
clean :
	-rm *.res *.obj *.pdb *.ilk *~ 2>NUL

distclean : clean
	-rm *.exe 2>NUL

testinst : all
	cp *.exe ../../../nbbuild/netbeans/bin/

test : testinst
	../../../nbbuild/netbeans/bin/netbeans.exe

test2 : testinst
	../../../nbbuild/netbeans/bin/nb.exe
