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
#include <sys/stat.h>
#include <sys/statvfs.h>
#include <stdlib.h>
#include <string.h>

#include "../../.common/src/CommonUtils.h"
#include "jni_UnixNativeUtils.h"


jboolean statMode(const char *path, int *mode) {
    struct stat sb;
    if (stat(path, &sb) == 0) {
        *mode = sb.st_mode;
        return 1;
    } else {
        return 0;
    }
}


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


JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_UnixNativeUtils_setPermissions0(JNIEnv *jEnv, jobject jObject, jstring jPath, jint jMode, jint jChange) {
    char* path = getChars(jEnv, jPath);    
    int currentMode = 0 ;
    char * msg = NULL;
    if(statMode(path, &currentMode)) {
        switch (jChange) {
            case MODE_CHANGE_SET:
                currentMode |= ACCESSPERMS;
                currentMode &= jMode;
                break;
            case MODE_CHANGE_ADD:
                currentMode |= jMode;
                break;
            case MODE_CHANGE_REMOVE:
                currentMode &= ~jMode;
                break;
            default:     
                msg = (char*) malloc(sizeof(char) * 60);
                memset(msg, 0, sizeof(char) * 60);
                sprintf(msg, "Selected change mode (%ld) is not supported", jChange);
                throwException(jEnv, msg);
                FREE(msg);
                FREE(path);                
                return;                
        }
        chmod(path, currentMode);
    } else {
        throwException(jEnv, "Can`t get file current permissions");
    }
    FREE(path);
}


JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_UnixNativeUtils_getPermissions0(JNIEnv *jEnv, jobject jObject, jstring jPath) {
    char* path = getChars(jEnv, jPath);
    int currentMode;
    if(statMode(path, &currentMode)) {
        return currentMode & (ACCESSPERMS);
    } else {
        throwException(jEnv, "Can`t get file current permissions");
    }
    
    FREE(path);
}
