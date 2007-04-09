#include <jni.h>

#include "../../.common/src/CommonUtils.h"
#include "UnixUtils.h"
#include "jni_UnixNativeUtils.h"

JNIEXPORT jlong JNICALL Java_org_netbeans_installer_utils_system_UnixNativeUtils_getFreeSpace0(JNIEnv* jEnv, jobject jObject, jstring jPath) {
    char* path   = getChars(jEnv, jPath);
    jlong result = (jlong) getFreeSpace(path);
    
    FREE(path);
    return result;
}
