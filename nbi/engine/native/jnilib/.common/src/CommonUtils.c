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
 * $Id$
 */
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "CommonUtils.h"

jbyteArray getStringBytes(JNIEnv* jEnv, jstring jString) {
    jbyteArray result = NULL;
    
    if (jString != NULL) {
        jmethodID jGetBytesMethod = (*jEnv)->GetMethodID(jEnv, (*jEnv)->GetObjectClass(jEnv, jString), "getBytes", "()[B");
        
        if (jGetBytesMethod != NULL) {
            jbyteArray jBuffer = (jbyteArray) (*jEnv)->CallObjectMethod(jEnv, jString, jGetBytesMethod);
            
            if (jBuffer != NULL) {
                jsize jLength = (*jEnv)->GetArrayLength(jEnv, jBuffer);
                
                result = (*jEnv)->NewByteArray(jEnv, jLength + 1);
                if (jLength != 0) {
                    jbyte* jChars = (*jEnv)->GetByteArrayElements(jEnv, jBuffer, NULL);
                    (*jEnv)->SetByteArrayRegion(jEnv, result, 0, jLength, jChars);
                    (*jEnv)->ReleaseByteArrayElements(jEnv, jBuffer, jChars, JNI_ABORT);
                }
                (*jEnv)->DeleteLocalRef(jEnv, jBuffer);
            }
            //(*jEnv)->DeleteLocalRef(jEnv, getBytesMethod);
        }
    }
    
    return result;
}

jstring newStringFromBytes(JNIEnv* jEnv, jbyteArray jByteArray, int length) {
    jstring result = NULL;
    
    jclass jStringClass = (*jEnv)->FindClass(jEnv, "java/lang/String");
    if (jStringClass != NULL) {
        jmethodID jStringConstructor = (*jEnv)->GetMethodID(jEnv, jStringClass, "<init>", "([BII)V");
        
        if (jStringConstructor != NULL) {
            result = (jstring) (*jEnv)->NewObject(jEnv, jStringClass, jStringConstructor, jByteArray, 0, length);
            //(*jEnv)->DeleteLocalRef(jEnv, stringConstructor);
        }
        (*jEnv)->DeleteLocalRef(jEnv, jStringClass);
    }
    
    return result;
}

jstring getString(JNIEnv* jEnv, const char* chars) {
    return (jstring) getStringWithLength(jEnv, chars, strlen(chars));
}

jstring getStringWithLength(JNIEnv* jEnv, const char* chars, int length) {
    jstring result = NULL;
    
    if (chars != NULL) {
        if (length == 0) {
            result = (*jEnv)->NewString(jEnv, (const jchar *) L"", 0);
        } else {
            jbyteArray jByteArray = (*jEnv)->NewByteArray(jEnv, length);
            
            if (jByteArray != NULL) {
                (*jEnv)->SetByteArrayRegion(jEnv, jByteArray, 0, length, (jbyte*) chars);
                result = newStringFromBytes(jEnv, jByteArray, length);
                (*jEnv)->DeleteLocalRef(jEnv, jByteArray);
            }
        }
    }
    
    return result;
}

char* getChars(JNIEnv* jEnv, jstring jString) {
    char* result = NULL;
    
    jbyteArray jByteArray = getStringBytes(jEnv, jString);
    if (jByteArray != NULL) {
        jbyte* jBytes = (*jEnv)->GetByteArrayElements(jEnv, jByteArray, NULL);
        
        long index = 0;
        if (jBytes != NULL) {
            int length = strlen((char*) jBytes);
            
            result = (char*) malloc(sizeof(char) * (length + 1));
            if (result != NULL) {
                memset(result, 0, length);
                strncpy(result, (char*) jBytes, length);
                result[length] = 0;
            }
            (*jEnv)->ReleaseByteArrayElements(jEnv, jByteArray, jBytes, JNI_ABORT);
        }
        (*jEnv)->DeleteLocalRef(jEnv, jByteArray);
    }
    
    return result;
}

char* getStringFromMethod(JNIEnv* jEnv, jobject object, const char* methodName) {
    char* result = NULL;
    
    jclass clazz = (*jEnv)->GetObjectClass(jEnv, object);
    if (clazz != NULL) {
        jmethodID method = (*jEnv)->GetMethodID(jEnv, clazz, methodName, "()Ljava/lang/String;");
        if (method != NULL) {
            jstring string = (jstring) (*jEnv)->CallObjectMethod(jEnv, object, method);
            if (string != NULL) {
                result = getChars(jEnv, string);
                (*jEnv)->DeleteLocalRef(jEnv, string);
            }
            //(*jEnv)->DeleteLocalRef(jEnv, method);
        }
        (*jEnv)->DeleteLocalRef(jEnv, clazz);
    }
    
    return result;
}

jboolean isInstanceOf(JNIEnv* jEnv, jobject object, const char* className) {    
    jboolean result = 0;
    jclass clazz = clazz = (*jEnv)->FindClass(jEnv, className);
    
    if (clazz != NULL) {
        result = (*jEnv)->IsInstanceOf(jEnv, object, clazz);
        (*jEnv)->DeleteLocalRef(jEnv, clazz);
    } 
    return result;
}

jint getIntFromMethod(JNIEnv* jEnv, jobject object, const char* methodName) {
    jlong value = 0;
    
    jclass clazz = (*jEnv)->GetObjectClass(jEnv, object);
    if (clazz != NULL) {
        jmethodID method = (*jEnv)->GetMethodID(jEnv, clazz, methodName, "()I");
        if (method != NULL) {
            value = (*jEnv)->CallIntMethod(jEnv, object, method);                       
        }
        (*jEnv)->DeleteLocalRef(jEnv, clazz);
    }
    
    return value;
}

void throwException(JNIEnv* jEnv, const char* message) {
    jclass clazz = (*jEnv)->FindClass(jEnv, "org/netbeans/installer/utils/exceptions/NativeException");
    if (clazz != NULL) {
        (*jEnv)->ThrowNew(jEnv, clazz, message);
        (*jEnv)->DeleteLocalRef(jEnv, clazz);
    }
}

void writeLog(JNIEnv* jEnv, int level, const char* message) {
    const char* prefix = "[jni] ";
    
    jclass clazz = (*jEnv)->FindClass(jEnv, "org/netbeans/installer/utils/LogManager");
    if (clazz != NULL) {
        jmethodID method = (*jEnv)->GetStaticMethodID(jEnv, clazz, "log", "(ILjava/lang/String;)V");
        if (method != NULL) {
            jstring jMessage = NULL;
            char* string = (char*) malloc(sizeof(char) * (strlen(message) + strlen(prefix)));
            
            string[0] = '\0';
            strcat(string, prefix);
            strcat(string, message);
            
            jMessage = getString(jEnv, string);
            
            if (jMessage != NULL) {
                (*jEnv)->CallStaticVoidMethod(jEnv, clazz, method, (jint) level, jMessage);
                (*jEnv)->DeleteLocalRef(jEnv, jMessage);
            }
            
            FREE(string);
            //(*jEnv)->DeleteLocalRef(jEnv, method);
        }
        (*jEnv)->DeleteLocalRef(jEnv, clazz);
    }
}

int mkdirs(JNIEnv* jEnv, const char *path) {
    int result = 1;
    
    // construct a java.io.File object
    jclass jFileClass = (*jEnv)->FindClass(jEnv, "java/io/File");
    if (jFileClass != NULL) {
        jmethodID jFileConstructor = (*jEnv)->GetMethodID(jEnv, jFileClass, "<init>", "(Ljava/lang/String;)V");
        jmethodID jGetParentFileMethod = (*jEnv)->GetMethodID(jEnv, jFileClass, "getParentFile", "()Ljava/io/File;");
        jmethodID jExistsMethod = (*jEnv)->GetMethodID(jEnv, jFileClass, "exists", "()Z");
        jmethodID jMkdirsMethod = (*jEnv)->GetMethodID(jEnv, jFileClass, "mkdirs", "()Z");
        
        if ((jFileConstructor != NULL) && (jGetParentFileMethod != NULL) && (jExistsMethod != NULL) && (jMkdirsMethod != NULL)) {
            jstring jPath  = getString(jEnv, path);
            
            if (jPath != NULL) {
                jobject jFile = (*jEnv)->NewObject(jEnv, jFileClass, jFileConstructor, jPath);
                
                if (jFile != NULL) {
                    jobject jParent = (*jEnv)->CallObjectMethod(jEnv, jFile, jGetParentFileMethod);
                    
                    if (jParent != NULL) {
                        result = (*jEnv)->CallBooleanMethod(jEnv, jParent, jExistsMethod);
                        if (!result) {
                            result = (*jEnv)->CallBooleanMethod(jEnv, jParent, jMkdirsMethod);
                        }
                        (*jEnv)->DeleteLocalRef(jEnv, jParent);
                    }
                    (*jEnv)->DeleteLocalRef(jEnv, jFile);
                }
                (*jEnv)->DeleteLocalRef(jEnv, jPath);
            }
            //(*jEnv)->DeleteLocalRef(jEnv, jFileConstructor);
            //(*jEnv)->DeleteLocalRef(jEnv, jGetParentFileMethod);
            //(*jEnv)->DeleteLocalRef(jEnv, jExistsMethod);
            //(*jEnv)->DeleteLocalRef(jEnv, jMkdirsMethod);
        }
        (*jEnv)->DeleteLocalRef(jEnv, jFileClass);
    }
    
    return result;
}

unsigned char* getByteFromMultiString(JNIEnv *jEnv, jobjectArray jObjectArray, unsigned long* size) {
    unsigned char* result = NULL;
    
    int     totalLength = 0;
    int     arrayLength = (*jEnv)->GetArrayLength(jEnv, jObjectArray);
    jstring jString     = NULL;
    
    int     i, j; // just counters
    
    for (i = 0; i < arrayLength; i++) {
        jString = (jstring) (*jEnv)->GetObjectArrayElement(jEnv, jObjectArray, i);
        totalLength += (*jEnv)->GetStringLength(jEnv, jString) + 1;
    }
    totalLength++; // add null to the end of array
    
    result = (unsigned char*) malloc(totalLength + 32);
    if (result != NULL) {
        int index = 0 ;
        
        for (i = 0; i < arrayLength; i++) {
            jString = (jstring) (*jEnv)->GetObjectArrayElement(jEnv, jObjectArray, i);
            
            if (jString != NULL) {
                char* chars = getChars(jEnv, jString);
                
                if (chars != NULL) {
                    for (j = 0; j < strlen(chars); j++) {
                        result[index++] = chars[j];
                    }
                    
                    FREE(chars);
                }
            }
            result[index++] = '\0';
        }
        result[index++] = '\0'; //double \0 at the end
        
        *size = index;
    }
    
    return result;
}
