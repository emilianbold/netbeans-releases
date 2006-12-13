#
# The contents of this file are subject to the terms of the Common Development
# and Distribution License (the License). You may not use this file except in
# compliance with the License.
#
# You can obtain a copy of the License at http://www.netbeans.org/cddl.html
# or http://www.netbeans.org/cddl.txt.

# When distributing Covered Code, include this CDDL Header Notice in each file
# and include the License file at http://www.netbeans.org/cddl.txt.
# If applicable, add the following below the CDDL Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
# Microsystems, Inc. All Rights Reserved.
#

#
# The cnd.dll comes premade in a C/C++ pack Windows distribution. This
# Makefile and the sources are included only for users interested in
# rebuilding the dll or for users wondering what is included in the
# dll.
#
# The cnd.dll includes a signal handler which is needed to ensure we ca
# sucessfully interupt the inferior process in a debugging session.

all: cnd.dll

cnd.dll: CndSigHandler.o
	gcc -Wl,--out-implib,libcnd.import.a -shared -o cnd.dll CndSigHandler.o

CndsigHandler.o: CndSigHandler.c
	gcc -c CndSigHandler.c 

clean:
	rm -f CndSigHandler.o cnd.dll libcnd.import.a

