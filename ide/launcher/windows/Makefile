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
# Copyright 1997-2004 Sun Microsystems, Inc. All Rights Reserved.
#

LIBS = advapi32.lib user32.lib

CFLAGS = -nologo -W4
!ifdef DEBUG
    CFLAGS = $(CFLAGS) -Zi
!endif

all : netbeans.exe nb.exe

netbeans.exe : netbeans.cpp netbeans.res
	cl $(CFLAGS) $(LIBS) -Fe$@ /DWINMAIN $**

nb.exe : netbeans.cpp
	cl $(CFLAGS) $(LIBS) -Fe$@ $**

netbeans.res : netbeans.rc
	rc /fo$@ $**

clean :
	-del *.res *.obj *.pdb *.ilk *~ 2>NUL

distclean : clean
	-del *.exe 2>NUL

testinst : all
	copy *.exe "..\..\..\..\nbbuild\netbeans\bin\"

test : testinst
	..\..\..\..\nbbuild\netbeans\bin\netbeans.exe

test2 : testinst
	..\..\..\..\nbbuild\netbeans\bin\nb.exe
