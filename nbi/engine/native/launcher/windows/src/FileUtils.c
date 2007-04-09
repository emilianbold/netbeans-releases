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
#include <windows.h>
#include <wchar.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <w32api/userenv.h>
#include "FileUtils.h"
#include "StringUtils.h"

HANDLE stdoutHandle = INVALID_HANDLE_VALUE;
HANDLE stderrHandle = INVALID_HANDLE_VALUE;
DWORD outputLevel = OUTPUT_LEVEL_NORMAL;
DWORD newLine = 1;
const WCHAR * FILE_SEP = L"\\";
DWORD checkForFreeSpace = 1;

char TIME_STRING [30];
typedef struct int64s {
    unsigned long Low, High;
} int64t;


void setStdoutHandle(HANDLE hndl) {
    stdoutHandle = hndl;
}
HANDLE getStdoutHandle() {
    if(stdoutHandle == INVALID_HANDLE_VALUE) setStdoutHandle(GetStdHandle(STD_OUTPUT_HANDLE));
    return stdoutHandle;
}

void setStderrHandle(HANDLE hndl) {
    stderrHandle = hndl;
}
HANDLE getStderrHandle() {
    if(stderrHandle == INVALID_HANDLE_VALUE) setStderrHandle(GetStdHandle(STD_ERROR_HANDLE));
    return stderrHandle;
}
void writeTimeStamp(HANDLE hd, DWORD need) {
    DWORD written;
    if(need==1) {
        SYSTEMTIME t;
        GetLocalTime(&t);
        sprintf(TIME_STRING, "[%02u-%02u-%02u %02u:%02u:%02u.%03u]> ", t.wYear, t.wMonth, t.wDay, t.wHour, t.wMinute, t.wSecond, t.wMilliseconds);
        WriteFile(hd, TIME_STRING, sizeof(char) * getLengthA(TIME_STRING), & written, NULL);
        memset(TIME_STRING, ' ', getLengthA(TIME_STRING));
    }
}

void writeMessageA(DWORD level, HANDLE hd, const char * message, DWORD needEndOfLine) {
    if(level<outputLevel) return;
    writeTimeStamp(hd, newLine);
    DWORD written;
    WriteFile(hd, message, sizeof(char) * getLengthA(message), & written, NULL);
    if(needEndOfLine>0) {
        newLine = 0;
        while((needEndOfLine--)>0) {
            writeMessageA(level, hd, "\n", 0);
            newLine = 1;
        }
        flushHandle(hd);
    } else {
        newLine = 0;
    }
}

void writeMessageW(DWORD level, HANDLE hd, const WCHAR * message, DWORD needEndOfLine) {
    if(level<outputLevel) return;
    char * msg = toChar(message);
    writeMessageA(level, hd, msg, needEndOfLine);
    FREE(msg);
}

void writeErrorA(DWORD level, HANDLE hd, const char * message, const WCHAR * param, DWORD errorCode) {
    WCHAR * err = getErrorDescription(errorCode);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), message, 0);
    writeMessageW(OUTPUT_LEVEL_DEBUG, getStderrHandle(), param, 1);
    writeMessageW(OUTPUT_LEVEL_DEBUG, getStderrHandle(), err, 1);
    FREE(err);
}

void flushHandle(HANDLE hd) {
    FlushFileBuffers(hd);
}
void closeStdHandles() {
    flushHandle(getStdoutHandle());
    flushHandle(getStderrHandle());
    CloseHandle(getStdoutHandle());
    CloseHandle(getStderrHandle());
}


double getFreeSpace(WCHAR *path) {
    const double E32 = 4294967296.;
    double size = 0.0;
    int64t bytes;
    WCHAR * dst = appendStringW(NULL, path);
    while(!fileExists(dst)) {
        WCHAR * parent = getParentDirectory(dst);
        FREE(dst);
        dst = parent;
        if(dst==NULL) break;
    }
    if(dst==NULL) return 0.; // no parent ? strange
    
    if (GetDiskFreeSpaceExW(dst, (PULARGE_INTEGER) &bytes, NULL, NULL)) {
        size = ((double) (bytes.High)) * E32 + ((double) bytes.Low);
    }
    
    FREE(dst);
    return size;
}

DWORD checkFreeSpace(WCHAR *path, DWORD size) {
    if(checkForFreeSpace) {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... checking free space at ", 0);
        writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), path, 1);
        double space = getFreeSpace(path);
        
        char * ch = doubleToChar(size);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "...  required : ", 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), ch, 1);
        FREE(ch);
        
        ch = doubleToChar(space);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... available : ", 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), ch, 1);
        FREE(ch);
        
        return ( space >= ((double)size)) ? 1 : 0;
    } else {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... free space checking is disabled", 1);
        return 1;
    }
}

DWORD fileExists(WCHAR *path) {
    WIN32_FILE_ATTRIBUTE_DATA attrs;
    return GetFileAttributesExW(path, GetFileExInfoStandard, &attrs);
}

DWORD isDirectory(WCHAR *path) {
    WIN32_FILE_ATTRIBUTE_DATA attrs;
    if(GetFileAttributesExW(path, GetFileExInfoStandard, &attrs)) {
        return (attrs.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY);
    }
    else {
        return 0;
    }
}
WCHAR * getParentDirectory(WCHAR * dir) {
    WCHAR * ptr = dir;
    while(1) {
        if(wcsstr(ptr, FILE_SEP)==NULL) {
            break;
        }
        ptr = wcsstr(ptr, FILE_SEP) + 1;
    }
    WCHAR * res = appendStringNW(NULL, 0, dir, getLengthW(dir) - getLengthW(ptr) - 1);
    return res;
}
WCHAR * normalizePath(WCHAR * dir) {
    WCHAR * directory = NULL;
    WCHAR * ptr1, *ptr2;
    ptr1 = wcsstr(dir, L":\\");
    ptr2 = wcsstr(dir, L":/");
    if(ptr1==NULL && ptr2==NULL) { //relative path
        directory = appendStringW(getCurrentDirectory(), FILE_SEP);
        directory = appendStringW(directory, dir);
    } else {
        directory = appendStringW(NULL, dir);
    }
    DWORD len = getLengthW(directory);
    DWORD i=0;
    for(i=0;i<len;i++) {
        if(directory[i]=='/') directory[i] = '\\';
    }
    return directory;
}

DWORD createDirectory(WCHAR * directory) {
    DWORD status = ERROR_OK;
    
    // now directory is the absolute path with normalized slashes
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "Getting parent directory of ", 0);
    writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), directory, 1);
    WCHAR * parent = getParentDirectory(directory);
    DWORD parentAttrs;
    if(parent!=NULL) {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "    parent = ", 0);
        writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), parent, 1);
        
        if(!fileExists(parent)) {
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... doesn`t exist. Create it...", 1);
            status = createDirectory(parent);
        } else {
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "    ... exist. ", 1);
        }
        if(status == ERROR_OK) {
            parentAttrs = GetFileAttributesW(parent);
            if(parentAttrs == INVALID_FILE_ATTRIBUTES) {
                status = ERROR_INPUTOUPUT;
            }
        }
    } else {
        status = ERROR_INPUTOUPUT;
    }
    if(status==ERROR_OK) {
        SECURITY_ATTRIBUTES secattr;
        secattr.nLength = sizeof(SECURITY_ATTRIBUTES);
        secattr.lpSecurityDescriptor = 0;
        secattr.bInheritHandle = 1;
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "... creating directory itself... ", 0);
        writeMessageW(OUTPUT_LEVEL_DEBUG, getStderrHandle(), directory, 1);
        status = (getFreeSpace(parent)>0.0) ? ERROR_OK : ERROR_FREESPACE;
        if(status == ERROR_OK) {
            status = (CreateDirectoryExW(parent, directory, &secattr)) ? ERROR_OK : ERROR_INPUTOUPUT;
            if(status!=ERROR_OK) {
                status = (CreateDirectoryW(directory, &secattr)) ? ERROR_OK : ERROR_INPUTOUPUT;
            }
            status = (fileExists(directory)) ? ERROR_OK : ERROR_INPUTOUPUT;
            
            if(status==ERROR_OK) {
                SetFileAttributesW(directory, parentAttrs);
            } else {
                writeErrorA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "Error! Can`t create directory : ", directory, GetLastError());
            }
        }
    }
    FREE(parent);
    return status ;
}

WCHAR newRandDigit() {
    return ((rand()%10)+'0');
}
void createTempDirectory(DWORD * status, WCHAR * argTempDir, WCHAR ** resultDir, DWORD createRndSubDir) {
    WCHAR * t = (argTempDir!=NULL) ? appendStringW(NULL, argTempDir) : getSystemTemporaryDirectory();
    
    WCHAR * nbiTmp = normalizePath(t);
    FREE(t);
    if(createRndSubDir) {
        nbiTmp = appendStringW(nbiTmp, L"\\NBI");
        WCHAR * randString = newpWCHAR(6);
        srand(GetTickCount());
        DWORD i=0;
        for(i=0;i<5;i++) {
            randString[i]=newRandDigit();
        }
        nbiTmp = appendStringW(appendStringW(nbiTmp, randString), L".tmp");
        free(randString);
    }
    
    writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "Using temp directory for extracting data : ", 0);
    writeMessageW(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), nbiTmp, 1);
    
    if(fileExists(nbiTmp) ) {
        if(!isDirectory(nbiTmp)) {
            *status = ERROR_INPUTOUPUT;
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), ".. exists but not a directory", 1);
        } else {
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "Output directory already exist so don`t create it", 1);
        }
    }
    else {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "creating directory...", 1);
        *status = createDirectory(nbiTmp);
        if(*status == ERROR_OK) {
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "Directory created : ", 0);
            writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), nbiTmp, 1);
            // set hidden attribute
            if(createRndSubDir) {
                writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "Setting hidden attributes to ", 0);
                writeMessageW(OUTPUT_LEVEL_DEBUG, getStderrHandle(), nbiTmp, 1);
                DWORD wAttrs = GetFileAttributesW(nbiTmp);
                SetFileAttributesW(nbiTmp, wAttrs | FILE_ATTRIBUTE_HIDDEN);
            }
        } else {
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "Error! Can`t create nbi temp directory : ", 0);
            writeMessageW(OUTPUT_LEVEL_DEBUG, getStderrHandle(), nbiTmp, 1);
        }
    }
    
    *resultDir = nbiTmp;
    return;
}


void deleteDirectory(WCHAR * dir) {
    DWORD attrs = GetFileAttributesW(dir);
    DWORD dwError;
    if(attrs==INVALID_FILE_ATTRIBUTES) {
        writeErrorA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "Error! Can`t get attributes of the dir : ", dir, GetLastError());
        return;
    }
    if(!SetFileAttributesW(dir, attrs & FILE_ATTRIBUTE_NORMAL)) {
        writeErrorA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "Error! Can`t set attributes of the dir : ", dir, GetLastError());
    }
    
    
    if(attrs & FILE_ATTRIBUTE_DIRECTORY) {
        WIN32_FIND_DATAW FindFileData;
        HANDLE hFind = INVALID_HANDLE_VALUE;
        
        WCHAR * DirSpec = appendStringW(appendStringW(NULL, dir), L"\\*" );
        
        // Find the first file in the directory.
        hFind = FindFirstFileW(DirSpec, &FindFileData);
        
        if (hFind == INVALID_HANDLE_VALUE) {
            writeErrorA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "Error! Can`t file with pattern ", DirSpec, GetLastError());
        }
        else {
            // List all the other files in the directory.
            while (FindNextFileW(hFind, &FindFileData) != 0) {
                if(wcscmp(FindFileData.cFileName, L".")!=0 &&
                wcscmp(FindFileData.cFileName, L"..")!=0 ) {
                    WCHAR * child = appendStringW(appendStringW(appendStringW(NULL, dir), FILE_SEP), FindFileData.cFileName);
                    deleteDirectory(child);
                    free(child);
                }
            }
            
            dwError = GetLastError();
            FindClose(hFind);
            if (dwError != ERROR_NO_MORE_FILES) {
                writeErrorA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "Error! Can`t find file with pattern : ", DirSpec, dwError);
            }
        }
        DWORD count = 0 ;
        // 20 tries in 2 seconds to delete the directory
        while(!RemoveDirectoryW(dir) && count++ < 20) Sleep(100);
        free(DirSpec);
    }
    else {
        DWORD count = 0 ;
        // 20 tries in 2 seconds to delete the file
        while(!DeleteFileW(dir) && count++ < 20) Sleep(100);
    }
}




WCHAR * getSystemTemporaryDirectory() {
    WCHAR * expanded = newpWCHAR(MAX_PATH);
    
    if(GetTempPathW(MAX_PATH, expanded)!=0) {
        return expanded;
    }
    
    DWORD result = GetEnvironmentVariableW(L"TEMP", expanded, MAX_PATH);
    if(result<=0 || result>MAX_PATH) {
        //writeOutputLn("Can`t find variable TEMP");
        result = GetEnvironmentVariableW(L"TMP", expanded, MAX_PATH);
        if(result<=0 || result>MAX_PATH) {
            //writeOutputLn("Can`t find variable TMP");
            result = GetEnvironmentVariableW(L"USERPROFILE", expanded, MAX_PATH);
            if(result>0 && result<=MAX_PATH) {
                expanded = appendStringW(expanded, L"\\Local Settings\\Temp");
            } else{
                memset(expanded, 0, sizeof(WCHAR) * MAX_PATH);
                WCHAR * curdir = getCurrentDirectory();
                wcsncpy(expanded, curdir, MAX_PATH);
                free(curdir);
            }
        }
    }
    return expanded;
}

WCHAR * getExeName() {
    WCHAR szPath[MAX_PATH];
    
    if( !GetModuleFileNameW( NULL, szPath, MAX_PATH ) ) {
        writeErrorA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "Error! Can`t get running program path.", NULL, GetLastError());
        return NULL;
    } else {
        return appendStringNW(NULL, 0, szPath, getLengthW(szPath));
    }
}


WCHAR * getExeDirectory() {
    WCHAR szPath[MAX_PATH];
    if(GetModuleFileNameW( NULL, szPath, MAX_PATH )) {
        WCHAR * lastSlash = szPath;
        while(wcsstr(lastSlash, FILE_SEP)!=NULL) {
            lastSlash = wcsstr(lastSlash, FILE_SEP) + 1;
        }
        long length = getLengthW(szPath) - getLengthW(lastSlash) - 1;
        return appendStringNW(NULL, 0 , szPath, length);
    } else {
        writeErrorA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "Error! Can`t get running program path.", NULL, GetLastError());
        return NULL;
    }
}

WCHAR * getCurrentDirectory() {
    WCHAR * buf = newpWCHAR(MAX_PATH);
    
    if(GetCurrentDirectoryW(MAX_PATH, buf)!=0) {
        return buf;
    } else {
        free(buf);
        return NULL;
    }
}
WCHAR * getCurrentUserHome() {
    HANDLE hUser;
    WCHAR * buf = NULL;
    DWORD res = 0;
    if (OpenProcessToken(GetCurrentProcess(), TOKEN_QUERY, &hUser)) {
        DWORD size = MAX_PATH;
        buf = newpWCHAR(MAX_PATH);
        GetUserProfileDirectoryW(hUser, buf, &size);
        if (size > MAX_PATH) {
            FREE(buf);
            buf = newpWCHAR(size + 1);
            if(!GetUserProfileDirectoryW(hUser, buf, &size)) {
                FREE(buf);
            }
        }
        CloseHandle(hUser);
    }
    if(buf==NULL) {
        DWORD size = MAX_PATH;
        buf = newpWCHAR(size);
        size = GetEnvironmentVariableW(L"USERPROFILE", buf, MAX_PATH);
        if( size > MAX_PATH) {
            FREE(buf);
            buf = newpWCHAR(size + 1);
            GetEnvironmentVariableW(L"USERPROFILE", buf, size);
        } else if (size==0) {
            FREE(buf);
        }
    }
    return buf;
}
double getFileSize(WCHAR * path) {
    WIN32_FILE_ATTRIBUTE_DATA wfad;
    double res;
    if (GetFileAttributesExW(path,
    GetFileExInfoStandard,
    &wfad)) {
        res = (double)MAXDWORD + (double)1;
        res*= wfad.nFileSizeHigh ;
        res+= wfad.nFileSizeLow;
        return res;
    } else {
        return -1.0;
    }
}
