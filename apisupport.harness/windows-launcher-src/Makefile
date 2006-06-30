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
