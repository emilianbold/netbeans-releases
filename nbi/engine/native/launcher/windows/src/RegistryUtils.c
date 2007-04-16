/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */
#include "RegistryUtils.h"
#include "StringUtils.h"
#include "FileUtils.h"

WCHAR * getStringValue(HKEY root, WCHAR *key, WCHAR *valueName) {
    
    HKEY hkey = 0 ;
    WCHAR *result = NULL;
    DWORD  type  = 0;
    DWORD  size  = 0;
    byte*  value = NULL;
    
    if(RegOpenKeyExW(root, key, 0, KEY_READ, &hkey) == ERROR_SUCCESS) {
        
        if (RegQueryValueExW(hkey, valueName, NULL, &type, NULL, &size) == ERROR_SUCCESS) {
            
            value = (byte*) malloc((size + 1) * sizeof(WCHAR));
            memset(value, 0, sizeof(WCHAR) * (size + 1));
            if (RegQueryValueExW(hkey, valueName, NULL, &type, value, &size) == ERROR_SUCCESS) {
                if(type == REG_SZ) {
                    result = (WCHAR *)value;
                }
            }
            if(result==NULL) {
                free(value);
            }            
        }
    }
    
    if(hkey!=0) {
        RegCloseKey(hkey);
    }
    return result;
}
WCHAR * getStringValuePC(HKEY root, WCHAR *parentkey, WCHAR *childkey, WCHAR *valueName) {
    WCHAR * key = appendStringW(appendStringW(appendStringW(NULL, parentkey), L"\\"), childkey);
    WCHAR *value = getStringValue(root, key, valueName);
    FREE(key);
    return value;
}
