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

#include <jni.h>
#include <windows.h>
#include <winreg.h>
#include <winnt.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <malloc.h>

#include "../../.common/src/CommonUtils.h"

#ifndef _WindowsUtils_H
#define _WindowsUtils_H

#ifdef __cplusplus
extern "C" {
#endif

HKEY getHKEY(jint jSection);

int queryValue(HKEY section, const unsigned short * key, const unsigned short * name, DWORD* type, DWORD* size, byte** value, int expand);

int setValue(HKEY section, const unsigned short * key, const unsigned short * name, DWORD type, const byte* data, int size, int expand);

#ifdef __cplusplus
}
#endif
#endif // _WindowsUtils_H
