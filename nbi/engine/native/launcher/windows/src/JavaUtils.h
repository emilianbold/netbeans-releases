/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

#ifndef _JavaUtils_H
#define	_JavaUtils_H

#include <windows.h>
#include "Launcher.h"
#include "Types.h"
#include "Errors.h"

#ifdef	__cplusplus
extern "C" {
#endif
    
// java.version
// java.vm.version
// java.vendor
// os.name
// os.arch
#define TEST_JAVA_PARAMETERS 5    
#define MAX_LEN_VALUE_NAME 16383

WCHAR * getJavaResource(WCHAR * location, const WCHAR * suffix);

void getJavaProperties(WCHAR * location, LauncherProperties * props, JavaProperties ** javaProps);

void findSystemJava(LauncherProperties * props);

JavaVersion * getJavaVersionFromString(char * string, DWORD * result);

char compareJavaVersion(JavaVersion * first, JavaVersion * second);

DWORD isJavaCompatible(JavaProperties *currentJava, JavaCompatible ** compatibleJava, DWORD number);

void printJavaProperties(LauncherProperties * props, JavaProperties * javaProps);

void freeJavaProperties(JavaProperties ** props);

JavaCompatible * newJavaCompatible();

#ifdef	__cplusplus
}
#endif

#endif	/* _JavaUtils_H */
