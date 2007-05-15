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

#ifndef _CommonUtils_H
#define	_CommonUtils_H

#define FREE(x) {if((x) != NULL) {free((x)); x = NULL;}}

#define LOG_DEBUG    4
#define LOG_MESSAGE  3
#define LOG_WARNING  2
#define LOG_ERROR    1
#define LOG_CRITICAL 0

#ifdef __cplusplus
extern "C" {
#endif
    
jbyteArray getStringBytes(JNIEnv* jEnv, jstring jString);

jstring newStringFromBytes(JNIEnv* jEnv, jbyteArray jByteArray, int length);

jstring getString(JNIEnv* jEnv, const char* chars);

jstring getStringWithLength(JNIEnv* jEnv, const char* chars, int length);

char* getChars(JNIEnv* jEnv, jstring jString);

char* getStringFromMethod(JNIEnv* jEnv, jobject object, const char* methodName);

jint getIntFromMethod(JNIEnv* jEnv, jobject object, const char* methodName);

jboolean isInstanceOf(JNIEnv* jEnv, jobject object, const char* className);

void throwException(JNIEnv* jEnv, const char* message);

void writeLog(JNIEnv* jEnv, int level, const char* message);

int mkdirs(JNIEnv* jEnv, const char *path);

unsigned char* getByteFromMultiString(JNIEnv *jEnv, jobjectArray jObjectArray, unsigned long* size);

#ifdef __cplusplus
}
#endif
#endif /* _CommonUtils_H */
