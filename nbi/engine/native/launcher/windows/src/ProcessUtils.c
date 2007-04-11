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
 */
#include "ProcessUtils.h"
#include "StringUtils.h"
#include "FileUtils.h"


const DWORD DEFAULT_PROCESS_TIMEOUT = 30000; //30 sec

DWORD readBuf(HANDLE hRead, WCHAR * buf, DWORD * bytesRead, HANDLE hWrite) {
    ReadFile(hRead, buf, STREAM_BUF_LENGTH - 1, bytesRead, NULL);
    
    if((*bytesRead)>0 && hWrite!=INVALID_HANDLE_VALUE) {
        DWORD bytesWritten = 0;
        WriteFile(hWrite, buf, (*bytesRead), &bytesWritten, 0);
    }
    memset(buf, 0, sizeof(buf));
}

DWORD readNextData(HANDLE hRead, WCHAR * buf, HANDLE hWrite) {
    memset(buf, 0, sizeof(buf));
    
    DWORD bytesRead;
    DWORD bytesAvailable;
    
    PeekNamedPipe(hRead, buf, STREAM_BUF_LENGTH - 1, &bytesRead, &bytesAvailable, NULL);
    if (bytesRead != 0) {
        memset(buf, 0, sizeof(buf));
        if (bytesAvailable >= STREAM_BUF_LENGTH) {
            while (bytesRead >= STREAM_BUF_LENGTH-1) {
                readBuf(hRead, buf, &bytesRead, hWrite);
            }
        }
        else {
            readBuf(hRead, buf, &bytesRead, hWrite);
        }
        return bytesRead;
    }
    return 0;
}

// get already running process stdout
DWORD readProcessStream(PROCESS_INFORMATION pi, HANDLE currentProcessStdin, HANDLE currentProcessStdout, HANDLE currentProcessStderr, DWORD timeOut, HANDLE hWriteInput, HANDLE hWriteOutput, HANDLE hWriteError) {
    DWORD started = GetTickCount();
    WCHAR buf[STREAM_BUF_LENGTH];
    DWORD exitCode=0;
    DWORD total = 0;
    DWORD outRead =0;
    DWORD errRead =0;
    DWORD inRead =0;
    while(1) {
        outRead = readNextData(currentProcessStdout, buf, hWriteOutput);
        errRead = readNextData(currentProcessStderr, buf, hWriteError);
        inRead  = readNextData(hWriteInput, buf, currentProcessStdin);
        GetExitCodeProcess(pi.hProcess, &exitCode);
        if (exitCode != STILL_ACTIVE) break;
        
        if(outRead == 0 && errRead==0 && inRead==0 && timeOut!=INFINITE) {
            if((GetTickCount() - started) > timeOut) break;
        }
        //avoid extra using of CPU resources
        Sleep(1);
    }
    return exitCode;
}
char * readHandle(HANDLE hRead) {
    char * output = NULL;
    char * buf = newpChar(STREAM_BUF_LENGTH);
    DWORD total = 0;
    DWORD read;
    DWORD bytesRead;
    DWORD bytesAvailable;
    
    while(1) {
        PeekNamedPipe(hRead, buf, STREAM_BUF_LENGTH - 1, &bytesRead, &bytesAvailable, NULL);
        if(bytesAvailable==0) break;
        ReadFile(hRead, buf, STREAM_BUF_LENGTH - 1, &read, NULL);
        if(read==0) break;
        output = appendStringN(output, total, buf, read);
        total+=read;
    }
    FREE(buf);
    return output;
}




// run process and get its standart output
// command - executing command
// timeLimitMillis - timeout of the process running without any output
// dir - working directory
// return ERROR_ON_EXECUTE_PROCESS for serios error
// return ERROR_PROCESS_TIMEOUT for timeout

DWORD executeCommand(DWORD * status, WCHAR * command, WCHAR * dir, DWORD timeLimitMillis, HANDLE hWriteOutput, HANDLE hWriteError, DWORD priority) {
    STARTUPINFOW si;
    SECURITY_ATTRIBUTES sa;
    SECURITY_DESCRIPTOR sd;
    PROCESS_INFORMATION pi;
    
    HANDLE newProcessInput;
    HANDLE newProcessOutput;
    HANDLE newProcessError;
    
    HANDLE currentProcessStdout;
    HANDLE currentProcessStdin;
    HANDLE currentProcessStderr;
    
    InitializeSecurityDescriptor(&sd, SECURITY_DESCRIPTOR_REVISION);
    SetSecurityDescriptorDacl(&sd, TRUE, NULL, FALSE);
    sa.lpSecurityDescriptor = &sd;
    sa.nLength = sizeof(SECURITY_ATTRIBUTES);
    sa.bInheritHandle = TRUE;
    
    
    if (!CreatePipe(&newProcessInput, &currentProcessStdin, &sa, 0)) {
        writeErrorA(OUTPUT_LEVEL_NORMAL, getStderrHandle(), "Can`t create pipe for input. ", NULL , GetLastError());
        *status = ERROR_ON_EXECUTE_PROCESS;
        return (*status);
    }
    
    if (!CreatePipe(&currentProcessStdout, &newProcessOutput, &sa, 0)) {
        writeErrorA(OUTPUT_LEVEL_NORMAL, getStderrHandle(), "Can`t create pipe for output. ", NULL , GetLastError());
        CloseHandle(newProcessInput);
        CloseHandle(currentProcessStdin);
        *status = ERROR_ON_EXECUTE_PROCESS;
        return (*status);
    }
    
    if (!CreatePipe(&currentProcessStderr, &newProcessError, &sa, 0)) {
        writeErrorA(OUTPUT_LEVEL_NORMAL, getStderrHandle(), "Can`t create pipe for error. ", NULL , GetLastError());
        CloseHandle(newProcessInput);
        CloseHandle(currentProcessStdin);
        CloseHandle(newProcessOutput);
        CloseHandle(currentProcessStdout);
        *status = ERROR_ON_EXECUTE_PROCESS;
        return (*status);
    }
    
    
    GetStartupInfoW(&si);
    
    si.dwFlags = STARTF_USESTDHANDLES|STARTF_USESHOWWINDOW;
    si.wShowWindow = SW_HIDE;
    si.hStdOutput = newProcessOutput;
    si.hStdError = newProcessError;
    si.hStdInput = newProcessInput;
    
    WCHAR * directory = (dir!=NULL) ? dir : getCurrentDirectory();
    writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "Create new process: ", 1);
    writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "          command : ", 0);
    writeMessageW(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), command, 1);
    writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "        directory : ", 0);
    writeMessageW(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), directory, 1);
    
    DWORD exitCode = ERROR_OK;
    if (CreateProcessW(NULL, command, NULL, NULL, TRUE,
    CREATE_NEW_CONSOLE | CREATE_NO_WINDOW | CREATE_DEFAULT_ERROR_MODE | priority,
    NULL, directory, &si, &pi)) {
        *status = ERROR_OK;
        
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... process created", 1);
        DWORD timeOut = ((timeLimitMillis<=0) ? DEFAULT_PROCESS_TIMEOUT: timeLimitMillis);
        
        exitCode = readProcessStream(pi, currentProcessStdin, currentProcessStdout, currentProcessStderr, timeOut, newProcessInput, hWriteOutput, hWriteError);
        
        if(exitCode==STILL_ACTIVE) {
            //actually we have reached the timeout of the process and need to terminate it
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "... process is timeouted", 1);
            GetExitCodeProcess(pi.hProcess, &exitCode);
            
            if(exitCode==STILL_ACTIVE) {
                TerminateProcess(pi.hProcess, 0);
                writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "... terminate process", 1);
                //Terminating process...It worked too much without any stdout/stdin/stderr
                *status = ERROR_PROCESS_TIMEOUT;//terminated by timeout
            }
        } else {
            //application finished its work... succesfully or not - it doesn`t matter            
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... process finished his work", 1);
        }
        CloseHandle(pi.hThread);
        CloseHandle(pi.hProcess);
    }  else {
        writeErrorA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "... can`t create process.", NULL, GetLastError());
        *status = ERROR_ON_EXECUTE_PROCESS;
    }
    
    
    CloseHandle(newProcessInput);
    CloseHandle(newProcessOutput);
    CloseHandle(newProcessError);
    CloseHandle(currentProcessStdin);
    CloseHandle(currentProcessStdout);
    CloseHandle(currentProcessStderr);
    return exitCode;
}



