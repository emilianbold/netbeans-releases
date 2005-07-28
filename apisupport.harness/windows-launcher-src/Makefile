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

all : app.exe app_w.exe

app.exe : app.cpp app.res
	gcc $(CFLAGS) $(LIBS) -mwindows -DWINMAIN app.cpp -o app.exe app.res
	mv app.exe ../release/launchers/

app_w.exe : app.cpp
	gcc $(CFLAGS) $(LIBS) app.cpp -o app_w.exe
	mv app_w.exe ../release/launchers/

app.res : app.rc
	windres -o$@ -Ocoff app.rc
  
clean :
	-rm *.res *.obj *.pdb *.ilk *~ 2>NUL

distclean : clean
	-rm *.exe 2>NUL

test :
	../release/launchers/app.exe

test_w :
	../release/launchers/app_w.exe
