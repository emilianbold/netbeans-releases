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

#include <windows.h>
#include <wchar.h>

typedef UINT  (WINAPI * WAIT_PROC)(HANDLE, DWORD);
typedef BOOL  (WINAPI * CLOSE_PROC)(HANDLE);
typedef BOOL  (WINAPI * DELETE_PROC)(LPCWSTR);
typedef VOID  (WINAPI * EXIT_PROC)(DWORD);
typedef VOID  (WINAPI * SLEEP_PROC)(DWORD);

const DWORD SLEEP_DELAY   = 200;
const DWORD MAX_ATTEPTS   = 15;
const DWORD THREAD_FINISHED = 100;
const DWORD INITIAL_DELAY = 2000; // 2 seconds is seems to be enough to finish java process
const WCHAR * LINE_SEPARATOR = L"\r\n";
const WCHAR * UNC_PREFIX     = L"\\\\?\\";
const DWORD UNC_PREFIX_LENGTH = 4;

typedef struct {
    WAIT_PROC	waitObject;
    CLOSE_PROC	closeHandle;
    DELETE_PROC	deleteFile;
    EXIT_PROC	exitProcess;
    SLEEP_PROC  sleep;
    
    HANDLE		hProcess;
    WCHAR		szFileName[MAX_PATH];
    
} INJECT;

DWORD WINAPI RemoteThread(INJECT *remote) {
    remote->waitObject(remote->hProcess, INFINITE);
    remote->closeHandle(remote->hProcess);
    DWORD count = 0 ;
    while(!remote->deleteFile(remote->szFileName) && (count++) < MAX_ATTEPTS) {
        remote->sleep(SLEEP_DELAY);
    }
    remote->exitProcess(0);
    return 0;
}

HANDLE GetRemoteProcess() {
    STARTUPINFO si = { sizeof(si) };
    PROCESS_INFORMATION pi;
    
    if(CreateProcess(0, "explorer.exe", 0, 0, FALSE, CREATE_SUSPENDED|CREATE_NO_WINDOW|IDLE_PRIORITY_CLASS, 0, 0, &si, &pi)) {
        CloseHandle(pi.hThread);
        return pi.hProcess;
    }
    else {
        return 0;
    }
}

BOOL removeItself() {
    INJECT local, *remote;
    BYTE   *code;
    HMODULE hKernel32;
    HANDLE  hRemoteProcess;
    HANDLE  hCurProc;
    
    DWORD	dwThreadId;
    HANDLE	hThread = 0;
    DWORD sizeOfCode = 200;
    
    hRemoteProcess = GetRemoteProcess();
    
    if(hRemoteProcess == 0)
        return FALSE;
    
    
    code = VirtualAllocEx(hRemoteProcess, 0, sizeof(INJECT) + sizeOfCode, MEM_RESERVE|MEM_COMMIT, PAGE_EXECUTE_READWRITE);
    
    if(code == 0) {
        CloseHandle(hRemoteProcess);
        return FALSE;
    }
    
    hKernel32 = GetModuleHandleW(L"kernel32.dll");
    remote = (INJECT *)(code + sizeOfCode);
    
    local.waitObject      = (WAIT_PROC)  GetProcAddress(hKernel32, "WaitForSingleObject");
    local.closeHandle	  = (CLOSE_PROC) GetProcAddress(hKernel32, "CloseHandle");
    local.exitProcess	  = (EXIT_PROC)  GetProcAddress(hKernel32, "ExitProcess");
    local.deleteFile      = (DELETE_PROC)GetProcAddress(hKernel32, "DeleteFileW");
    local.sleep           = (SLEEP_PROC) GetProcAddress(hKernel32, "Sleep");
    
    // duplicate our own process handle for remote process to wait on
    hCurProc = GetCurrentProcess();
    DuplicateHandle(hCurProc, hCurProc, hRemoteProcess, &local.hProcess, 0, FALSE, DUPLICATE_SAME_ACCESS);
    
    // find name of current executable
    GetModuleFileNameW(NULL, local.szFileName, MAX_PATH);
    
    // write in code to execute, and the remote structure
    WriteProcessMemory(hRemoteProcess, code,    RemoteThread, sizeOfCode, 0);
    WriteProcessMemory(hRemoteProcess, remote, &local, sizeof(local), 0);
    
    // execute the code in remote process
    hThread = CreateRemoteThread(hRemoteProcess, 0, 0, (LPTHREAD_START_ROUTINE) code, remote, 0, &dwThreadId);
    
    if(hThread != 0) {
        CloseHandle(hThread);
    }
    
    return TRUE;
}

typedef struct _list {
    WCHAR * item;
    struct _list * next;
} LIST;


WCHAR * toWCHAR(char * charBuffer, DWORD size) {
    DWORD i=0;
    BOOL hasBOM        = (*charBuffer == '\xFF' && *(charBuffer+1) == '\xFE');
    BOOL hasReverseBOM = (*charBuffer == '\xFE' && *(charBuffer+1) == '\xFF');
    
    char * realStringPtr = charBuffer;
    if (hasBOM || hasReverseBOM) {
        size-= 2;
        realStringPtr+= 2;
        if(hasReverseBOM) {
            char c;
            for (i = 0 ; i < size/2 ; i++) {
                c = charBuffer [2 * i] ;
                charBuffer [2 * i] = charBuffer [2 * i + 1] ;
                charBuffer [2 * i + 1] = c;
            }
        }
    }
    
    WCHAR * buffer = (WCHAR*) malloc(sizeof(WCHAR) * (size/2+1));
    memset(buffer , 0, sizeof(WCHAR) * (size/2+1));
    for(i=0;i<size/2;i++) {
        realStringPtr[2*i] = (realStringPtr[2*i]) & 0xFF;
        realStringPtr[2*i+1] = (realStringPtr[2*i+1])& 0xFF;
        buffer [i] = realStringPtr[2*i] + ((realStringPtr[2*i+1])& 0xFF);
    }
    
    return buffer;
}

DWORD getLinesNumber(WCHAR *str) {
    DWORD result = 0;
    WCHAR *ptr = str;
    WCHAR *ptr2 = str;
    DWORD sepLength = wcslen(LINE_SEPARATOR);
    if(ptr!=NULL) {
        while((ptr2 = wcsstr(ptr, LINE_SEPARATOR))!=NULL) {
            ptr = ptr2 + sepLength;
            result++;
            
            if(ptr==NULL)  break;
        }
        if(ptr!=NULL && wcslen(ptr) > 0) {
            result ++;
        }
    }
    return result;
}

void getLines(WCHAR *str, WCHAR *** list, DWORD * number) {
    *number = getLinesNumber(str);
    *list = (WCHAR**) malloc(sizeof(WCHAR*) * (*number));
    WCHAR *ptr = str;
    WCHAR *ptr2 = NULL;
    DWORD length = 0;
    DWORD sepLength = wcslen(LINE_SEPARATOR);
    DWORD counter = 0;
    if(ptr!=NULL) {
        while(counter < (*number)) {
            if((ptr2 = wcsstr(ptr, LINE_SEPARATOR))!=NULL) {
                ptr2 = wcsstr(ptr, LINE_SEPARATOR) + sepLength;
                length = wcslen(ptr) - wcslen(ptr2) - sepLength;
                (*list) [counter ] = (WCHAR*) malloc((sizeof(WCHAR*)*(length+1)));
                memset((*list)[counter ], 0, sizeof(WCHAR) * (length +1) );
                wcsncat((*list) [counter ], ptr, length);
                ptr = ptr2;
            } else if((length = wcslen(ptr)) > 0) {
                (*list)[counter ] = (WCHAR*) malloc((sizeof(WCHAR*)*(length+1)));
                memset((*list)[counter ], 0, sizeof(WCHAR) * (length +1) );
                wcsncat((*list) [counter ], ptr, length);
                ptr = NULL;
            }
            counter++;
            if(ptr==NULL)  break;
        }
    }
}


void readStringList(HANDLE fileHandle, WCHAR *** list, DWORD *number) {
    DWORD size = GetFileSize(fileHandle, NULL); // hope it much less than 2GB
    DWORD read = 0;
    DWORD i=0;
    char * charBuffer = (char*) malloc(sizeof(char) * (size + 1));
    memset(charBuffer, 0, sizeof(char) * (size + 1));
    if(ReadFile(fileHandle, charBuffer, size, &read, 0) && read >=2) {
        WCHAR * buffer = toWCHAR(charBuffer, size);
        getLines(buffer, list, number);
        free(buffer);
    }
    free(charBuffer);
}

void deleteFile(WCHAR * filePath) {
    DWORD count = 0 ;
    WIN32_FILE_ATTRIBUTE_DATA attrs;
    DWORD length = wcslen(filePath) + UNC_PREFIX_LENGTH + 1;
    WCHAR * file = (WCHAR*) malloc(sizeof(WCHAR) * length);
    memset(file, 0, length);
    wsprintfW(file,L"%s%s",UNC_PREFIX, filePath);
    
    if(GetFileAttributesExW(file, GetFileExInfoStandard, &attrs)) {
        if(attrs.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
            while((!RemoveDirectoryW(file) || GetFileAttributesExW(file,GetFileExInfoStandard, &attrs)) && 
            ((count++) < MAX_ATTEPTS))
                Sleep(SLEEP_DELAY);
        else
            while((!DeleteFileW(file) || GetFileAttributesExW(file,GetFileExInfoStandard, &attrs)) && 
            ((count++) < MAX_ATTEPTS))
                Sleep(SLEEP_DELAY);
    }
    free(file);
}

DWORD WINAPI deleteFileThread(void * ptr) {
    WCHAR * file = (WCHAR*) ptr;
    deleteFile(file);
    return THREAD_FINISHED;
}

DWORD getFreeIndexForNextThread(HANDLE * list, DWORD max, DWORD * counter) {
    DWORD code = 0;
    DWORD maxReached = 0;
    
    while(1) {
        if((*counter)==max) {
            maxReached = 1;
            *counter = 0;
        }
        code = 0;
        if(list[*counter]==INVALID_HANDLE_VALUE) {
            break;
        } else if(GetExitCodeThread(list[*counter], &code)!=0 && code==THREAD_FINISHED) {
            break;
        } else {
            *counter = (*counter) + 1;
            if((*counter)==max && maxReached == 1) {
                *counter = WaitForMultipleObjects(max, list, FALSE, INFINITE) - WAIT_OBJECT_0;
            }
        }
    }
}
// should be less or equals to MAXIMUM_WAIT_OBJECTS
#define MAXIMUM_THREADS MAXIMUM_WAIT_OBJECTS

int WINAPI WinMain( HINSTANCE hInstance, HINSTANCE hi, PSTR pszCmdLine, int nCmdShow) {
    int argumentsNumber = 0;
    DWORD i=0;
    DWORD  threadCounter = 0;
    DWORD dwThread;
    HANDLE * runningThreads = (HANDLE *) malloc(sizeof(HANDLE) * MAXIMUM_THREADS);
    
    for(i=0;i<MAXIMUM_THREADS;i++) {
        runningThreads[i] = INVALID_HANDLE_VALUE;
    }
    
    WCHAR ** commandLine = CommandLineToArgvW(GetCommandLineW(), &argumentsNumber);
    if(argumentsNumber==2) {
        WCHAR * filename = commandLine[1];
        HANDLE fileList = CreateFileW(filename, GENERIC_READ, FILE_SHARE_READ | FILE_SHARE_WRITE, 0, OPEN_EXISTING, FILE_FLAG_DELETE_ON_CLOSE, 0);
        if(fileList!=0) {
            WCHAR ** files = NULL;
            DWORD number = 0;
            DWORD allThreadsUsed=0;
            readStringList(fileList, &files, &number);
            CloseHandle(fileList);
            
            if(files!=NULL) {                
                Sleep(INITIAL_DELAY);
                for(i=0;i<number;i++) {
                    WCHAR * file = files[i];
                    if(file!=NULL) {
                        if(wcslen(file)>0)  {
                            getFreeIndexForNextThread(runningThreads, MAXIMUM_THREADS, &threadCounter);
                            runningThreads [threadCounter] = CreateThread( NULL, 0, &deleteFileThread, (LPVOID) file, 0, &dwThread );
                            threadCounter++;
                            if(threadCounter==MAXIMUM_THREADS) allThreadsUsed = 1;
                        }
                    }
                }
                DWORD objectsToWait = allThreadsUsed ? MAXIMUM_THREADS : threadCounter;
                WaitForMultipleObjects(objectsToWait, runningThreads, TRUE, INFINITE);
                
                for(i=0;i<number;i++) {
                    if(files[i]!=NULL) free(files[i]);
                }
                
                free(files);
            }
        }
    }
    LocalFree(commandLine);
    free(runningThreads);
    removeItself();
    return 0;
}
