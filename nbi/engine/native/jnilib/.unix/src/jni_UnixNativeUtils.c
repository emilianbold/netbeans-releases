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
#include <sys/types.h>
#include <sys/statvfs.h>
#include <stdlib.h>
#include <string.h>

#include "../../.common/src/CommonUtils.h"
#include "jni_UnixNativeUtils.h"

JNIEXPORT jlong JNICALL Java_org_netbeans_installer_utils_system_UnixNativeUtils_getFreeSpace0(JNIEnv* jEnv, jobject jObject, jstring jPath) {
    char* path   = getChars(jEnv, jPath);
    jlong result = 0;
    
    struct statvfs fsstat;    
    if(memset(&fsstat, 0, sizeof(struct statvfs)) != NULL) {
        if(statvfs(path, &fsstat) == 0) {
            result = (jlong) fsstat.f_frsize;
            result *= (jlong) fsstat.f_bfree;
        }
    }
    
    
    FREE(path);
    return result;
}
