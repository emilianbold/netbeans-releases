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
#include <wchar.h>
#include <stdio.h>
#include "JavaUtils.h"
#include "RegistryUtils.h"
#include "StringUtils.h"
#include "FileUtils.h"
#include "ProcessUtils.h"
#include "Launcher.h"

const DWORD JAVA_VERSION_PROCESS_TIMEOUT = 5000; // 5sec
const DWORD MAX_LEN_VALUE_NAME = 16383;
const WCHAR * JAVA_EXE_SUFFIX = L"\\bin\\java.exe";
const WCHAR * JAVA_LIB_SUFFIX = L"\\lib";

WCHAR * JAVA_REGISTRY_KEYS [] = {
    L"SOFTWARE\\JavaSoft\\Java Runtime Environment",
    L"SOFTWARE\\JavaSoft\\Java Development Kit",
    L"SOFTWARE\\JRockit\\Java Runtime Environment",
    L"SOFTWARE\\JRockit\\Java Development Kit",
    L"SOFTWARE\\IBM\\Java Runtime Environment",
    L"SOFTWARE\\IBM\\Java2 Runtime Environment",
    L"SOFTWARE\\IBM\\Java Development Kit",
};
WCHAR * JAVA_HOME = L"JavaHome";
WCHAR * CURRENT_VERSION = L"CurrentVersion";

WCHAR * getJavaHomeValue(WCHAR *parentkey, WCHAR *subkey) {
    return getStringValuePC(HKEY_LOCAL_MACHINE, parentkey, subkey, JAVA_HOME);
}


WCHAR * getTestJVMFileName(WCHAR * testJVMFile) {
    WCHAR * filePtr = testJVMFile;
    
    if(filePtr==NULL) {
        return NULL;
    }
    while(wcsstr(filePtr, L"\\")!=NULL) {
        filePtr = wcsstr(filePtr, L"\\");
        filePtr++;
    }
    WCHAR * dotClass = wcsstr(filePtr, L".class");
    WCHAR * testJavaClass = NULL;
    if(dotClass!=NULL) {
        testJavaClass = appendStringNW(NULL, 0, filePtr, getLengthW(filePtr) - getLengthW(dotClass));
    }
    return testJavaClass;
}


//returns 0 if equals, 1 if first > second, -1 if first < second
char compareJavaVersion(JavaVersion * first, JavaVersion * second) {
    if(first==NULL) return (second==NULL) ? 0 : -1;
    if(second==NULL) return -1;
    if(first->major == second->major) {
        if(first->minor == second->minor) {
            if(first->micro == second->micro) {
                if(first->update == second->update) return 0;
                return (first->update > second->update) ? 1 : -1;
            }
            return (first->micro > second->micro) ? 1 : -1;
        }
        return (first->minor > second->minor) ? 1 : -1;
    } else {
        return (first->major > second->major) ? 1 : -1;
    }
}
DWORD isJavaCompatible(JavaProperties *currentJava, JavaCompatible ** compatibleJava, DWORD number) {
    JavaVersion * current = currentJava->version;
    DWORD i = 0 ;
    for(i=0;i<number;i++) {
        DWORD check = 1;
        
        check = (compareJavaVersion(current, compatibleJava[i]->minVersion) >= 0 &&
        compareJavaVersion(current, compatibleJava[i]->maxVersion) <= 0) ? check : 0;
        
        if (check) {
            if(compatibleJava[i]->vendor!=NULL) {
                check = (strstr(currentJava->vendor, compatibleJava[i]->vendor) != NULL) ? check : 0;
            }
            if (compatibleJava[i]->osName!=NULL) {
                check = (strstr(currentJava->osName, compatibleJava[i]->osName)!=NULL) ? check : 0;
            }
            
            if (compatibleJava[i]->osArch!=NULL) {
                check = (strstr(currentJava->osArch, compatibleJava[i]->osArch)!=NULL) ? check : 0;
            }
            if(check) {
                return 1;
            }
        }
    }
    return 0;
}

JavaVersion * getJavaVersionFromString(char * string, DWORD * result) {
    JavaVersion *vers = NULL;
    
    if(getLengthA(string)>=3) {
        //hope that at least we "major.minor" : 1.5
        if(string[1]=='.') {
            char c = string[0];
            if(c>='0' && c<='9') {
                long major = c - '0';
                c = string[2];
                if(c>='0' && c<='9') {
                    long minor = c - '0';
                    *result = ERROR_OK;
                    vers = (JavaVersion*) malloc(sizeof(JavaVersion));
                    vers->major  = major;
                    vers->minor  = minor;
                    vers->micro  = 0;
                    vers->update = 0;
                    memset(vers->build, 0, 128);
                    char *p = string + 3;
                    if(p!=NULL) {
                        if(p[0]=='.') { // micro...
                            p++;
                            while(p!=NULL) {
                                char c = p[0];
                                if(c>='0' && c<='9') {
                                    vers->micro = (vers->micro) * 10 + c - '0';
                                    p++;
                                    continue;
                                }
                                else if(c=='_') {//update
                                    p++;
                                    while(p!=NULL) {
                                        c = p[0];
                                        p++;
                                        if(c>='0' && c<='9') {
                                            vers->update = (vers->update) * 10 + c - '0';
                                            continue;
                                        } else {
                                            break;
                                        }
                                    }
                                } else {
                                    if(p!=NULL) p++;
                                }
                                if(c=='-' && p!=NULL) { // build number
                                    strncpy(vers->build, p, min(127, getLengthA(p)));
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    return vers;
}

DWORD getJavaPropertiesFromOutput(char *str, JavaProperties ** javaProps) {
    * javaProps = NULL;
    DWORD separators = getLineSeparatorNumber(str);
    if(separators != TEST_JAVA_PARAMETERS) return ERROR_INPUTOUPUT;
    
    char * start = str;
    char * end = strstr(start, "\n");
    
    char * javaVersion = appendStringN(NULL, 0, start, getLengthA(start) - getLengthA(end)-1);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "    java.version =  ", 0);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), javaVersion, 1);
    start = end + 1;
    end = strstr(start, "\n");
    
    
    char * javaVmVersion = appendStringN(NULL, 0, start, getLengthA(start) - getLengthA(end)-1);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "    java.vm.version = ", 0);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), javaVmVersion, 1);
    start = end + 1;
    end = strstr(start, "\n");
    
    char * javaVendor = appendStringN(NULL, 0, start, getLengthA(start) - getLengthA(end)-1);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "    java.vendor = ", 0);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), javaVendor, 1);
    start = end + 1;
    end = strstr(start, "\n");
    
    char * osName = appendStringN(NULL, 0, start, getLengthA(start) - getLengthA(end)-1);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "    os.name = ", 0);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), osName, 1);
    start = end + 1;
    end = strstr(start, "\n");
    
    char * osArch = appendStringN(NULL, 0, start, getLengthA(start) - getLengthA(end)-1);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "    os.arch = ", 0);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), osArch, 2);
    
    char * string = javaVersion;
    DWORD result = ERROR_INPUTOUPUT;
    
    if(javaVmVersion!=NULL) {
        string = strstr(javaVmVersion, javaVersion);
        if(string==NULL) {
            string = javaVersion;
        }
    }
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "getting java version from string : ", 0);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), string, 1);
    
    JavaVersion * vers = getJavaVersionFromString(string, & result);
    if(result==ERROR_OK) {
        * javaProps = (JavaProperties *) malloc(sizeof(JavaProperties));
        (*javaProps)->version = vers;
        (*javaProps)->vendor   = javaVendor;
        (*javaProps)->osName   = osName;
        (*javaProps)->osArch   = osArch;
        (*javaProps)->javaHome = NULL;
        (*javaProps)->javaExe  = NULL;
    } else {
        FREE(javaVendor);
        FREE(osName);
        FREE(osArch);
    }
    FREE(javaVmVersion);
    FREE(javaVersion);
    
    return result;
}


DWORD getJavaProperties(WCHAR * location, LauncherProperties * props, JavaProperties ** javaProps) {
    WCHAR *testJavaClass  = props->testJVMClass;
    WCHAR *javaExecutable = getJavaResource(location, JAVA_EXE_SUFFIX);
    WCHAR *libDirectory   = getJavaResource(location, JAVA_LIB_SUFFIX);
    DWORD result = ERROR_OK;
    if(fileExists(javaExecutable) && testJavaClass!=NULL && isDirectory(libDirectory)) {
        // <location>\bin\java.exe exists
        WCHAR * command = NULL;
        
        appendCommandLineArgument(&command, javaExecutable);
        appendCommandLineArgument(&command, L"-classpath");
        appendCommandLineArgument(&command, props->testJVMFile->resolved);
        appendCommandLineArgument(&command, testJavaClass);
        
        HANDLE hRead;
        HANDLE hWrite;
        CreatePipe(&hRead, &hWrite, NULL, 0);
        // Start the child process.
        result = executeCommand(command, NULL, JAVA_VERSION_PROCESS_TIMEOUT, hWrite, hWrite, NORMAL_PRIORITY_CLASS);
        if(result!= MAXDWORD && result!= EXIT_CODE_TIMEOUT) {
            char * output = readHandle(hRead);
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "           output :\n", 0);
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), output, 1);
            
            result = getJavaPropertiesFromOutput(output, javaProps);
            if(result==ERROR_OK) {
                (*javaProps)->javaHome = appendStringW(NULL, location);
                (*javaProps)->javaExe  = appendStringW(NULL, javaExecutable);
            }
            FREE(output);
        }
        free(command);
        CloseHandle(hWrite);
        CloseHandle(hRead);
    } else {
        result = ERROR_INPUTOUPUT;
    }
    
    FREE(libDirectory);
    FREE(javaExecutable);
    return result;
}



char * getJavaVersionFormatted(const JavaProperties * javaProps) {
    char *result = NULL;
    if(javaProps!=NULL) {
        JavaVersion * version = javaProps->version;
        if(version!=NULL) {
            result = newpChar(256);
            sprintf(result, "%ld.%ld.%ld",  version->major, version->minor, version->micro);
            
            if(version->update!=0) {
                sprintf(result, "%s_%02ld", result, version->update);
            }
            if(strncmp(version->build, "", 127)!=0) {
                sprintf(result, "%s-%s", result, version->build);
            }
        }
    }
    return result;
}


JavaCompatible * newJavaCompatible() {
    JavaCompatible * props = (JavaCompatible *) malloc(sizeof(JavaCompatible));
    props->minVersion = NULL;
    props->maxVersion = NULL;
    props->vendor = NULL;
    props->osName = NULL;
    return props;
}

void freeJavaProperties(JavaProperties ** props) {
    if(*props!=NULL) {
        FREE((*props)->version);
        FREE((*props)->javaHome);
        FREE((*props)->javaExe);
        FREE((*props)->vendor);
        FREE(*props);
    }
}

WCHAR * getJavaResource(WCHAR * location, const WCHAR * suffix) {
    return appendStringW(appendStringW(NULL, location), suffix);
}


void searchCurrentJavaRegistry(LauncherProperties * props) {
    DWORD i=0;
    WCHAR ** keys = JAVA_REGISTRY_KEYS;
    DWORD k=0;
    HKEY rootKeys [2] = {HKEY_LOCAL_MACHINE, HKEY_CURRENT_USER};
    DWORD rootKeysNumber = sizeof(rootKeys)/sizeof(HKEY);
    DWORD keysNumber = sizeof(JAVA_REGISTRY_KEYS)/sizeof(WCHAR*);
    writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), ".. search java in CurrentVersion values", 1);
    DWORD status = ERROR_OK;
    for ( k = 0; k < rootKeysNumber; k++) {
        for(i=0; i < keysNumber;i++) {
            WCHAR * value = getStringValue(rootKeys[k], keys[i], CURRENT_VERSION);
            if(value!=NULL) {
                WCHAR *javaHome = getStringValuePC(rootKeys[k], keys[i], value, JAVA_HOME);
                free(value);
                trySetCompatibleJava(&status, javaHome, props);
                FREE(javaHome);
                if(props->java!=NULL) {
                    return;
                }
                
            }
        }
    }
    
    
    // we found no CurrentVersion java... just search for other possible keys
    writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), ".. search java in other values", 1);
    WCHAR buffer [MAX_LEN_VALUE_NAME];
    
    for(k=0;k<rootKeysNumber;k++) {
        for(i=0;i<keysNumber;i++) {
            HKEY  hkey = 0;
            DWORD   index  = 0 ;
            if (RegOpenKeyExW(rootKeys[k], keys[i], 0, KEY_READ, &hkey) == ERROR_SUCCESS) {
                DWORD number = 0;
                if (RegQueryInfoKeyW(hkey, NULL, NULL, NULL, &number, NULL, NULL, NULL, NULL, NULL, NULL, NULL) == ERROR_SUCCESS) {
                    DWORD err = 0;
                    do {
                        DWORD size = MAX_LEN_VALUE_NAME;
                        buffer[0]  = 0;
                        err = RegEnumKeyExW(hkey, index, buffer, &size, NULL, NULL, NULL, NULL);
                        if (err == ERROR_SUCCESS) {
                            WCHAR  * javaHome = getJavaHomeValue(keys[i], buffer);
                            status = ERROR_OK;
                            trySetCompatibleJava(&status, javaHome, props);
                            FREE(javaHome);
                            if(props->java!=NULL) {
                                i = keysNumber; // to the end of cycles
                                k = rootKeysNumber;
                                break;
                            }
                            
                        }
                        index++;
                    } while (err == ERROR_SUCCESS);
                }
            }
            if (hkey != 0) {
                RegCloseKey(hkey);
            }
        }
    }
    return;
}


void searchJavaFromEnvVariables(LauncherProperties * props) {
    static WCHAR * ENVS [] = {
        L"JAVA_HOME",
        L"JAVAHOME",
        L"JAVA_PATH",
        L"JDK_HOME",
        L"JDKHOME",
        L"ANT_JAVA",
        L"JAVA",
        L"JDK"
    };
    
    WCHAR buffer [MAX_PATH];
    
    int size = sizeof(ENVS)/sizeof(WCHAR *);
    int i=0;
    int ret;
    
    for(i=0;i<size;i++) {
        buffer[0]='\0';
        ret = GetEnvironmentVariableW((WCHAR *) ENVS[i], (WCHAR *) buffer, MAX_PATH);
        if (ret > 0 && ret <= MAX_PATH) {
            writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "    <", 0);
            writeMessageW(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), ENVS[i], 0);
            writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "> = ", 0);
            writeMessageW(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), buffer, 1);
            DWORD status = ERROR_OK;
            trySetCompatibleJava(&status, buffer, props);
            if(props->java!=NULL) {
                break;
            }
        }
    }
}


void findSystemJava(LauncherProperties * props) {
    DWORD status = ERROR_OK;
    if ( props->jvms->size > 0 ) {
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "Search jvm using some predefined locations", 1);
        DWORD i=0;
        for(i=0;i<props->jvms->size;i++) {
            resolvePath(props, props->jvms->items[i]);
            trySetCompatibleJava(&status, props->jvms->items[i]->resolved, props);
            if(status==ERROR_OK) break;
        }
    }
    // search JVM in the registry
    if(props->java==NULL) {
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "Search java in registry", 1);
        searchCurrentJavaRegistry(props);
    }
    // search JVM in the environment
    if(props->java==NULL) {
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "Search java in environment variables", 1);
        searchJavaFromEnvVariables(props);
    }
}


void printJavaProperties(JavaProperties * javaProps) {
    if(javaProps!=NULL) {
        char * jv = getJavaVersionFormatted(javaProps);
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "Current Java:", 1);
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "       javaHome: ", 0);
        writeMessageW(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), javaProps->javaHome, 1);
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "        javaExe: ", 0);
        writeMessageW(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), javaProps->javaExe, 1);
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "        version: ", 0);
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), jv, 1);
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "         vendor: ", 0);
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), javaProps->vendor, 1);
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "        os.name: ", 0);
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), javaProps->osName, 1);
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "        os.arch: ", 0);
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), javaProps->osArch, 1);
        FREE(jv);
    }
}
