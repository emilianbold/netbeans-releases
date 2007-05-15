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
/* Header for class org_netbeans_installer_utils_system_WindowsNativeUtils */

#ifndef _Included_org_netbeans_installer_utils_system_WindowsNativeUtils
#define _Included_org_netbeans_installer_utils_system_WindowsNativeUtils

#ifdef __cplusplus
extern "C" {
#endif

#undef org_netbeans_installer_utils_system_WindowsNativeUtils_MIN_UID_INDEX
#define org_netbeans_installer_utils_system_WindowsNativeUtils_MIN_UID_INDEX 1L

#undef org_netbeans_installer_utils_system_WindowsNativeUtils_MAX_UID_INDEX
#define org_netbeans_installer_utils_system_WindowsNativeUtils_MAX_UID_INDEX 100L

/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    isCurrentUserAdmin0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_isCurrentUserAdmin0
  (JNIEnv *, jobject);

/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    getFreeSpace0
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_getFreeSpace0
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    createShortcut0
 * Signature: (Lorg/netbeans/installer/utils/system/shortcut/FileShortcut;)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_createShortcut0
  (JNIEnv *, jobject, jobject);


/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    deleteFileOnReboot0
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_deleteFileOnReboot0
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    notifyAssociationChanged0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_notifyAssociationChanged0
  (JNIEnv *, jobject);


/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    checkAccessTokenAccessLevel0
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_checkAccessTokenAccessLevel0
  (JNIEnv *, jobject, jstring, jint);


/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    notifyEnvironmentChanged0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_notifyEnvironmentChanged0
  (JNIEnv *, jobject);


#ifdef __cplusplus
}
#endif

#endif
