#include <jni.h>
#include <windows.h>
#include <winreg.h>
#include <winnt.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <malloc.h>

#include "../../.common/src/CommonUtils.h"

#ifndef _WindowsUtils_H
#define _WindowsUtils_H

#ifdef __cplusplus
extern "C" {
#endif

HKEY getHKEY(jint jSection);

int queryValue(HKEY section, const char* key, const char* name, DWORD* type, DWORD* size, byte** value, int expand);

int setValue(HKEY section, const char* key, const char* name, DWORD type, const byte* data, int size, int expand);

#ifdef __cplusplus
}
#endif
#endif // _WindowsUtils_H
