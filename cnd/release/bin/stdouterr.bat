@echo off
if "%OS%" == "Windows_NT" setlocal
rem The contents of this file are subject to the terms of the Common Development
rem and Distribution License (the License). You may not use this file except in
rem compliance with the License.
rem
rem You can obtain a copy of the License at http://www.netbeans.org/cddl.html
rem or http://www.netbeans.org/cddl.txt.

rem When distributing Covered Code, include this CDDL Header Notice in each file
rem and include the License file at http://www.netbeans.org/cddl.txt.
rem If applicable, add the following below the CDDL Header, with the fields
rem enclosed by brackets [] replaced by your own identifying information:
rem "Portions Copyrighted [year] [name of copyright owner]"
rem
rem The Original Software is NetBeans. The Initial Developer of the Original
rem Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
rem Microsystems, Inc. All Rights Reserved.
setlocal

if ""%1"" == """" goto end
    %* >&2
:end
