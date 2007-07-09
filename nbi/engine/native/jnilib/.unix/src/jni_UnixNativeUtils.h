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
/* Header for class org_netbeans_installer_utils_system_UnixNativeUtils */

#ifndef _Included_org_netbeans_installer_utils_system_UnixNativeUtils
#define _Included_org_netbeans_installer_utils_system_UnixNativeUtils
#ifdef __cplusplus
extern "C" {
#endif


#define MODE_RU 1L
#define MODE_WU 2L
#define MODE_EU 4L
#define MODE_RG 8L
#define MODE_WG 16L
#define MODE_EG 32L
#define MODE_RO 64L
#define MODE_WO 128L
#define MODE_EO 256L
    
#define MODE_CHANGE_SET 1L
#define MODE_CHANGE_ADD 2L
#define MODE_CHANGE_REMOVE 4L
        
    

/*
 * Class:     org_netbeans_installer_utils_system_UnixNativeUtils
 * Method:    getFreeSpace0
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_netbeans_installer_utils_system_UnixNativeUtils_getFreeSpace0
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_UnixNativeUtils
 * Method:    setPermission0
 * Signature: (Ljava/lang/String;II)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_UnixNativeUtils_setPermissions0
  (JNIEnv *, jobject, jstring, jint, jint);

/*
 * Class:     org_netbeans_installer_utils_system_UnixNativeUtils
 * Method:    getPermissions0
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_UnixNativeUtils_getPermissions0
  (JNIEnv *, jobject, jstring);



#ifdef __cplusplus
}
#endif
#endif
