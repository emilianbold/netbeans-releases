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
#include "WindowsUtils.h"
#include "jni_WindowsRegistry.h"

HKEY getHKEY(jint jSection) {
    switch (jSection) {
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_CLASSES_ROOT:
            return HKEY_CLASSES_ROOT;
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_CURRENT_USER:
            return HKEY_CURRENT_USER;
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_LOCAL_MACHINE:
            return HKEY_LOCAL_MACHINE;
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_USERS:
            return HKEY_USERS;
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_CURRENT_CONFIG:
            return HKEY_CURRENT_CONFIG;
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_DYN_DATA:
            return HKEY_DYN_DATA;
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_PERFORMANCE_DATA:
            return HKEY_PERFORMANCE_DATA;
        default:
            return NULL;
    }
}

int queryValue(HKEY section, const char* key, const char* name, DWORD* type, DWORD* size, byte** value, int expand) {
    int   result     = 1;
    
    HKEY   hkey      = 0;
    DWORD  tempType  = 0;
    DWORD  tempSize  = 0;
    byte*  tempValue = NULL;
    
    if (RegOpenKeyEx(section, key, 0, KEY_QUERY_VALUE, &hkey) == ERROR_SUCCESS) {
        if (RegQueryValueEx(hkey, name, NULL, &tempType, NULL, &tempSize) == ERROR_SUCCESS) {
            tempValue = (byte*) malloc(tempSize + 8);
            
            if (tempValue != NULL) {
                memset(tempValue, 0, tempSize + 8);
                
                if (RegQueryValueEx(hkey, name, NULL, &tempType, tempValue, &tempSize) == ERROR_SUCCESS) {
                    if (expand && (tempType == REG_EXPAND_SZ)) {
                        int   expandedSize        = strlen((char*) tempValue) + 2;
                        byte* expandedValue       = (byte*) malloc(expandedSize);
                        int   expandedCharsNumber = ExpandEnvironmentStrings((char*) tempValue, (char*) expandedValue, tempSize);
                        
                        if (expandedCharsNumber > tempSize) {
                            expandedValue       = (byte*) realloc(expandedValue, expandedCharsNumber * sizeof(byte));
                            expandedCharsNumber = ExpandEnvironmentStrings((char*) tempValue, (char*) expandedValue, expandedCharsNumber);
                        }
                        
                        FREE(tempValue);
                        tempValue = expandedValue;
                    }
                } else {
                    FREE(tempValue);
                    result = 0;
                }
            }
        } else {
            result = 0;
        }
    } else {
        result = 0;
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    if (type != NULL) {
        *type = tempType;
    }
    if (size != NULL) {
        *size = tempSize;
    }
    if ((value != NULL) && (tempValue != NULL)) {
        *value = tempValue;
    }
    
    return result;
}

int setValue(HKEY section, const char* key, const char* name, DWORD type, const byte* data, int size, int expand) {
    int result = 1;
    
    HKEY hkey  = 0;
    
    if (RegOpenKeyEx(section, key, 0, KEY_SET_VALUE, &hkey) == ERROR_SUCCESS) {
        if (!(RegSetValueEx(hkey, name, 0, type, data, size) == ERROR_SUCCESS)) {
            result = 0;
        }
    } else {
        result = 0;
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    return result;
}
