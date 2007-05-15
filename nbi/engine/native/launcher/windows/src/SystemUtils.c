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

#include <windows.h>
#include <stdio.h>
#include <stdlib.h>

#include "SystemUtils.h"
void getOSVersion(DWORD *id, DWORD *major, DWORD *minor) {
    OSVERSIONINFO ver;
    ver.dwOSVersionInfoSize = sizeof(ver);
    GetVersionEx(&ver);
    *id = ver.dwPlatformId;
    *major = ver.dwMajorVersion;
    *minor =  ver.dwMinorVersion;
    return;
}

DWORD is9x() {
    DWORD id, major, minor;
    getOSVersion(& id, & major, & minor);
    return (id == VER_PLATFORM_WIN32_WINDOWS) ? 1 : 0;
}

DWORD isNT() {    
    DWORD id, major, minor;
    getOSVersion(& id, & major, & minor);
    return (id == VER_PLATFORM_WIN32_NT && major == 4 && minor == 0) ? 1 : 0;
}
DWORD is2k() {
    DWORD id, major, minor;
    getOSVersion(& id, & major, & minor);
    return (id == VER_PLATFORM_WIN32_NT && major == 5 && minor == 0) ? 1 : 0;
}

DWORD isXP() {
    DWORD id, major, minor;
    getOSVersion(& id, & major, & minor);
    return (id == VER_PLATFORM_WIN32_NT && major == 5 && minor == 1) ? 1 : 0;
}

DWORD is2003() {
    DWORD id, major, minor;
    getOSVersion(& id, & major, & minor);
    return (id == VER_PLATFORM_WIN32_NT && major == 5 && minor == 2) ? 1 : 0;
}
DWORD isVista() {
    DWORD id, major, minor;
    getOSVersion(& id, & major, & minor);
    return (id == VER_PLATFORM_WIN32_NT && major == 6) ? 1 : 0;
}

