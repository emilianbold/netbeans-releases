/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

#include <jni.h>
#include <windows.h>
#include <lmcons.h>
#include <shlguid.h>
#include <shlobj.h>

#include "../../.common/src/CommonUtils.h"
#include "WindowsUtils.h"
#include "jni_WindowsNativeUtils.h"

////////////////////////////////////////////////////////////////////////////////
// Globals
// double equivalent for the maximum value for a signed 32-bit integer, will be
// used to emulate 64-bit integer
const double E32 = 4294967296.;

// defines a functional pointer. will be used to get a handle of the available
// disk space calculation function
typedef BOOL(WINAPI *P_GDFSE) (LPCWSTR, PULARGE_INTEGER, PULARGE_INTEGER, PULARGE_INTEGER);

// emulates a 64 bits integer. computations will actually be made on doubles.
typedef struct int64s {
    unsigned long Low, High;
} int64t;

////////////////////////////////////////////////////////////////////////////////
// Functions

JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_isCurrentUserAdmin0(JNIEnv* jEnv, jobject jObject) {
    BOOL result = FALSE;
    
    PACL pACL = NULL;
    PSID psidAdmin = NULL;
    HANDLE token = NULL;
    HANDLE duplToken = NULL;
    PSECURITY_DESCRIPTOR adminDescriptor = NULL;
    
    SID_IDENTIFIER_AUTHORITY SystemSidAuthority = SECURITY_NT_AUTHORITY;
    DWORD aclSize;
    
    const DWORD ACCESS_READ  = 1;
    const DWORD ACCESS_WRITE = 2;
    
    GENERIC_MAPPING mapping;
    
    PRIVILEGE_SET ps;
    DWORD status;
    DWORD structSize = sizeof(PRIVILEGE_SET);
    
    // MS KB 118626
    while (TRUE) {
        if (!OpenThreadToken(GetCurrentThread(), TOKEN_DUPLICATE | TOKEN_QUERY, TRUE, &token)) {
            if (GetLastError() != ERROR_NO_TOKEN) {
                throwException(jEnv, "Native error");
                break;
            }
            
            if (!OpenProcessToken(GetCurrentProcess(), TOKEN_DUPLICATE | TOKEN_QUERY, &token)) {
                throwException(jEnv, "Native error");
                break;
            }
        }
        
        if (!DuplicateToken(token, SecurityImpersonation, &duplToken)) {
            throwException(jEnv, "Native error");
            break;
        }
        
        if (!AllocateAndInitializeSid(&SystemSidAuthority, 2, SECURITY_BUILTIN_DOMAIN_RID, DOMAIN_ALIAS_RID_ADMINS, 0, 0, 0, 0, 0, 0, &psidAdmin)) {
            throwException(jEnv, "Native error");
            break;
        }
        
        adminDescriptor = (PSECURITY_DESCRIPTOR) LocalAlloc(LPTR, SECURITY_DESCRIPTOR_MIN_LENGTH);
        if (adminDescriptor == NULL) {
            throwException(jEnv, "Native error");
            break;
        }
        if (!InitializeSecurityDescriptor(adminDescriptor, SECURITY_DESCRIPTOR_REVISION)) {
            throwException(jEnv, "Native error");
            break;
        }
        
        aclSize = sizeof(ACL) + sizeof(ACCESS_ALLOWED_ACE) + GetLengthSid(psidAdmin) - sizeof(DWORD);
        
        pACL = (PACL) LocalAlloc(LPTR, aclSize);
        if (pACL == NULL) {
            throwException(jEnv, "Native error");
            break;
        }
        if (!InitializeAcl(pACL, aclSize, ACL_REVISION2)) {
            throwException(jEnv, "Native error");
            break;
        }
        
        if (!AddAccessAllowedAce(pACL, ACL_REVISION2, ACCESS_READ | ACCESS_WRITE , psidAdmin)) {
            throwException(jEnv, "Native error");
            break;
        }
        if (!SetSecurityDescriptorDacl(adminDescriptor, TRUE, pACL, FALSE)) {
            throwException(jEnv, "Native error");
            break;
        }
        
        SetSecurityDescriptorGroup(adminDescriptor, psidAdmin, FALSE);
        SetSecurityDescriptorOwner(adminDescriptor, psidAdmin, FALSE);
        
        if (!IsValidSecurityDescriptor(adminDescriptor)) {
            throwException(jEnv, "Native error");
            break;
        }
        
        mapping.GenericRead    = ACCESS_READ;
        mapping.GenericWrite   = ACCESS_WRITE;
        mapping.GenericExecute = 0;
        mapping.GenericAll     = ACCESS_READ | ACCESS_WRITE;
        
        if (!AccessCheck(adminDescriptor, duplToken, ACCESS_READ, &mapping, &ps, &structSize, &status, &result)) {
            throwException(jEnv, "Native error");
            break;
        }
        
        break;
    }
    
    if (pACL) {
        LocalFree(pACL);
    }
    if (adminDescriptor) {
        LocalFree(adminDescriptor);
    }
    if (psidAdmin) {
        FreeSid(psidAdmin);
    }
    if (duplToken) {
        CloseHandle(duplToken);
    }
    if (token) {
        CloseHandle(token);
    }
    
    return result;
}

JNIEXPORT jlong JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_getFreeSpace0(JNIEnv* jEnv, jobject jObject, jstring jPath) {
    WCHAR*  path = getWideChars(jEnv, jPath);
    jlong  size = 0;
    
    P_GDFSE pGetDiskFreeSpaceEx = NULL;
    
    // get the handle of the disk space calculation function
    pGetDiskFreeSpaceEx = (P_GDFSE) GetProcAddress(GetModuleHandle("kernel32.dll"), "GetDiskFreeSpaceExW");
    
    // if the handle was obtained successfully, get the disk space
    if (pGetDiskFreeSpaceEx) {
        int64t bytes;
        
        // if the calculation as successfull return the double equivalent of
        // the emulated 64-bit integer
        if (pGetDiskFreeSpaceEx(path, (PULARGE_INTEGER) &bytes, NULL, NULL)) {
            size = (jlong) ((double) bytes.High*E32 + bytes.Low);
        } else {
            throwException(jEnv, "Native error");
        }
    } else {
        throwException(jEnv, "Native error");
    }
    
    if (path != NULL) {
        free(path);
    }
    
    return size;
}

JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_createShortcut0(JNIEnv* jEnv, jobject jObject, jobject jShortcut) {
    unsigned short *shortcutPath     = getWideStringFromMethod(jEnv, jShortcut, "getPath");
    unsigned short *targetPath       = getWideStringFromMethod(jEnv, jShortcut, "getTargetPath");
    unsigned short *description      = getWideStringFromMethod(jEnv, jShortcut, "getDescription");
    unsigned short *iconPath         = getWideStringFromMethod(jEnv, jShortcut, "getIconPath");
    jint            iconIndex        = getIntFromMethod       (jEnv, jShortcut, "getIconIndex");
    unsigned short *workingDirectory = getWideStringFromMethod(jEnv, jShortcut, "getWorkingDirectoryPath");
    unsigned short *arguments        = getWideStringFromMethod(jEnv, jShortcut, "getArgumentsString");
    
    HRESULT     tempResult;
    IShellLinkW* shell;
    
    HRESULT comStart = CoInitialize(NULL);
    int errorCode = 0;
    
    tempResult = CoCreateInstance(&CLSID_ShellLink, NULL, CLSCTX_INPROC_SERVER, &IID_IShellLinkW, (void **) &shell);
    
    if (SUCCEEDED(tempResult)) {
        IPersistFile *persistFile;
        // we will save the shell link in persistent storage
        tempResult = shell->lpVtbl->QueryInterface(shell, &IID_IPersistFile, (void **) &persistFile);
        
        if (SUCCEEDED(tempResult)) {
            tempResult = shell->lpVtbl->SetPath(shell, targetPath);
            if (!SUCCEEDED(tempResult)) {
                throwException(jEnv, "Native error (-2)");
                errorCode = -2;
            }
            // make sure description length is less than MAX_PATH
            if ((errorCode == 0) && (description != NULL)) {
                if (wcslen(description) < MAX_PATH) {
                    if (!SUCCEEDED(shell->lpVtbl->SetDescription(shell, description))) {
                        throwException(jEnv, "Native error (-3)");
                        errorCode = -3;
                    }
                } else {
                    unsigned short *desc = (unsigned short *) malloc(sizeof(unsigned short) * MAX_PATH);
                    desc = wcsncpy(desc, description, MAX_PATH - 1);
                    if (!SUCCEEDED(shell->lpVtbl->SetDescription(shell, desc))) {
                        throwException(jEnv, "Native error (-4)");
                        errorCode = -4;
                    }
                    free(desc);
                }
            }
            if ((errorCode == 0) && (arguments != NULL)) {
                if (!SUCCEEDED(shell->lpVtbl->SetArguments(shell, arguments))) {
                    throwException(jEnv, "Native error (-5)");
                    errorCode = -5;
                }
            }
            if ((errorCode == 0) && (workingDirectory != NULL)) {
                if (!SUCCEEDED(shell->lpVtbl->SetWorkingDirectory(shell, workingDirectory))) {
                    throwException(jEnv, "Native error (-6)");
                    errorCode = -6;
                }
            }
            if ((errorCode == 0) && (iconPath != NULL)) {
                if (!SUCCEEDED(shell->lpVtbl->SetIconLocation(shell, iconPath, iconIndex))) {
                    throwException(jEnv, "Native error (-7)");
                    errorCode = -7;
                }
            }
            // use normal window.
            if (errorCode == 0) {
                if (!SUCCEEDED(shell->lpVtbl->SetShowCmd(shell, SW_NORMAL))) {
                    throwException(jEnv, "Native error (-8)");
                    errorCode = -8;
                }
            }
            if (errorCode == 0) {
                if (mkdirsW(jEnv, shortcutPath)) {
                    if (!SUCCEEDED(persistFile->lpVtbl->Save(persistFile, shortcutPath, TRUE))) {
                        throwException(jEnv, "Native error (-9)");
                        errorCode = -9;
                    }
                } else {
                    throwException(jEnv, "Native error (-10)");
                    errorCode = -10;
                }
            }
            
            if (errorCode == 0) {
                persistFile->lpVtbl->Release(persistFile);
            }
        } else {
            throwException(jEnv, "Native error (-11)");
            errorCode = -11;
        }
        shell->lpVtbl->Release(shell);
    } else {
        throwException(jEnv, "Native error (-12)");
        errorCode = -12;
    }
    
    if (comStart == S_OK) {
        CoUninitialize();
    }
    
    if(shortcutPath != NULL) {
        free(shortcutPath);
    }
    if(targetPath != NULL) {
        free(targetPath);
    }
    if(description != NULL) {
        free(description);
    }
    if(iconPath != NULL) {
        free(iconPath);
    }
    if(workingDirectory != NULL) {
        free(workingDirectory);
    }
    if(arguments != NULL) {
        free(arguments);
    }
}

JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_deleteFileOnReboot0(JNIEnv* jEnv, jobject jObject, jstring jPath) {
    unsigned short * path = getWideChars(jEnv, jPath);
    
    if (!MoveFileExW(path, NULL, MOVEFILE_DELAY_UNTIL_REBOOT)) {
        throwException(jEnv, "Native error");
    }
    
    FREE(path);
}

JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_createProcessWithoutHandles0(JNIEnv* jEnv, jobject jObject, jstring jCommand) {
    unsigned short * command = getWideChars(jEnv, jCommand);
    STARTUPINFOW si;
    PROCESS_INFORMATION pi;
    
    memset(&si, 0, sizeof(si));
    si.cb = sizeof(si);
    memset(&pi, 0, sizeof(pi));
    if(!CreateProcessW(NULL,   // executable name - use command line
            command,    // command line
            NULL,   // process security attribute
            NULL,   // thread security attribute
            FALSE,   // inherits system handles
            0,      // no creation flags
            NULL,   // use parent's environment block
            NULL,   // use parent's starting directory
            &si,    // (in) startup information
            &pi)) {   // (out) process information
        throwException(jEnv, "Cannot create process.\n");
    }
    
    
    FREE(command);
}
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_notifyAssociationChanged0(JNIEnv *jEnv, jobject jObj) {
    SHChangeNotify(SHCNE_ASSOCCHANGED, SHCNF_IDLIST, 0, 0);
}

JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_notifyEnvironmentChanged0(JNIEnv *jEnv, jobject jObj) {
    /* maximum 1 sec timeout for each window in the system */
    DWORD dwReturnValue = 0;
    LRESULT result = SendMessageTimeout(HWND_BROADCAST, WM_SETTINGCHANGE, 0, (LPARAM) "Environment", SMTO_ABORTIFHUNG, 1000, (PDWORD_PTR) &dwReturnValue);
    return (result!=0);
}


JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_checkAccessTokenAccessLevel0(JNIEnv *jEnv, jobject jObj, jstring jPath, jint jLevel) {
    unsigned short * path = getWideChars(jEnv, jPath);
    PSECURITY_DESCRIPTOR    pSD;
    DWORD nLength;
    
    PRIVILEGE_SET PrivilegeSet;
    DWORD PrivSetSize = sizeof (PRIVILEGE_SET);
    
    HANDLE hToken;
    
    GENERIC_MAPPING GenericMapping;
    DWORD DesiredAccess = (DWORD) jLevel ;
    
    BOOL bAccessGranted;
    DWORD GrantedAccess;
    
    // create memory for storing user's security descriptor
    GetFileSecurityW(path, OWNER_SECURITY_INFORMATION | GROUP_SECURITY_INFORMATION | DACL_SECURITY_INFORMATION, NULL, 0, &nLength);
    
    pSD = (PSECURITY_DESCRIPTOR) HeapAlloc(GetProcessHeap(), HEAP_ZERO_MEMORY, nLength);
    
    if (pSD == NULL) {
        throwException(jEnv, "Unable to allocate memory to store security descriptor.\n");
        return -1;
    }
    // Get the security descriptor
    if (!GetFileSecurityW(path, OWNER_SECURITY_INFORMATION | GROUP_SECURITY_INFORMATION | DACL_SECURITY_INFORMATION,  pSD, nLength, &nLength)) {
        throwException(jEnv, "Unable to obtain security descriptor.\n");
        free(path);
        return (-3);
    }
    free(path);
    
    /* Perform security impersonation of the user and open */
    /* the resulting thread token. */
    if (!ImpersonateSelf(SecurityImpersonation)) {
        throwException(jEnv, "Unable to perform security impersonation.\n");
        HeapFree(GetProcessHeap(), 0, pSD);
        return (-4);
    }
    
    if (!OpenThreadToken(GetCurrentThread(), TOKEN_DUPLICATE | TOKEN_QUERY, FALSE, &hToken)) {
        throwException(jEnv, "Unable to get current thread's token.\n");
        HeapFree(GetProcessHeap(), 0, pSD);
        return (-5);
    }
    RevertToSelf();
    
    memset(&GenericMapping, 0x00, sizeof (GENERIC_MAPPING));
    
    DesiredAccess = DesiredAccess | STANDARD_RIGHTS_READ;
    GenericMapping.GenericRead = FILE_GENERIC_READ;
    
    if(jLevel & FILE_WRITE_DATA) {
        GenericMapping.GenericWrite = FILE_GENERIC_WRITE;
    }
    
    MapGenericMask(&DesiredAccess, &GenericMapping);
    
    /* Perform access check using the token. */
    if (!AccessCheck(pSD, hToken, DesiredAccess, &GenericMapping, &PrivilegeSet, &PrivSetSize, &GrantedAccess, &bAccessGranted)) {
        throwException(jEnv, "Unable to perform access check.\n");
        CloseHandle(hToken);
        HeapFree(GetProcessHeap(), 0, pSD);
        return (-6);
    }
    /* Clean up. */
    HeapFree(GetProcessHeap(), 0, pSD);
    CloseHandle(hToken);
    return (bAccessGranted);
}


