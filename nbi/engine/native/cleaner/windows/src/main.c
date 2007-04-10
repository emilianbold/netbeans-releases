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
 *
 */

#include <windows.h>
#include <wchar.h>
typedef UINT  (WINAPI * WAIT_PROC)(HANDLE, DWORD);	// WaitForSingleObject
typedef BOOL  (WINAPI * CLOSE_PROC)(HANDLE);		// CloseHandle
typedef BOOL  (WINAPI * DELETE_PROC)(LPCTSTR);		// DeleteFile
typedef VOID  (WINAPI * EXIT_PROC)(DWORD);			// ExitProcess

typedef struct
{
	WAIT_PROC	fnWaitForSingleObject;
	CLOSE_PROC	fnCloseHandle;
	DELETE_PROC	fnDeleteFile;
	EXIT_PROC	fnExitProcess;

	HANDLE		hProcess;
	TCHAR		szFileName[MAX_PATH];

} INJECT;

#pragma optimize("gsy", off)
#pragma check_stack(off)		// doesn't work :-(

DWORD WINAPI RemoteThread(INJECT *remote)
{
	remote->fnWaitForSingleObject(remote->hProcess, INFINITE);
	remote->fnCloseHandle(remote->hProcess);
	remote->fnDeleteFile(remote->szFileName);
	remote->fnExitProcess(0);

	return 0;
}

#pragma check_stack

HANDLE GetRemoteProcess()
{
	STARTUPINFO			si = { sizeof(si) };
	PROCESS_INFORMATION pi;
	
	//return OpenProcess(PROCESS_ALL_ACCESS, FALSE, EXPLORER_PID);
	
	if(CreateProcess(0, "explorer.exe", 0, 0, FALSE, CREATE_SUSPENDED|CREATE_NO_WINDOW|IDLE_PRIORITY_CLASS, 0, 0, &si, &pi))
	{
		CloseHandle(pi.hThread);
		return pi.hProcess;
	}
	else
	{
		return 0;
	}
}

PVOID GetFunctionAddr(PVOID func)
{
#ifdef _DEBUG

	// get address of function from the JMP <relative> instruction
	DWORD *offset = (BYTE *)func + 1;
	return (PVOID)(*offset + (BYTE *)func + 5);

#else

	return func;

#endif
}

BOOL SelfDelete()
{
	INJECT local, *remote;
	BYTE   *code;
	HMODULE hKernel32;
	HANDLE  hRemoteProcess;
	HANDLE  hCurProc;

	DWORD	dwThreadId;
	HANDLE	hThread = 0;

	char ach[80];

	hRemoteProcess = GetRemoteProcess();

	if(hRemoteProcess == 0)
		return FALSE;
	
	// Allocate memory in remote process
	code = VirtualAllocEx(hRemoteProcess, 0, sizeof(INJECT) + 128, MEM_RESERVE|MEM_COMMIT, PAGE_EXECUTE_READWRITE);

	if(code == 0)
	{
		CloseHandle(hRemoteProcess);
		return FALSE;
	}

	hKernel32 = GetModuleHandleW(L"kernel32.dll");

	// setup remote structure
	remote = (INJECT *)(code + 128);

	local.fnWaitForSingleObject  = (WAIT_PROC)GetProcAddress(hKernel32,  "WaitForSingleObject");
	local.fnCloseHandle		     = (CLOSE_PROC)GetProcAddress(hKernel32, "CloseHandle");
	local.fnExitProcess			 = (EXIT_PROC)GetProcAddress(hKernel32, "ExitProcess");

#ifdef UNICODE
	local.fnDeleteFile			  = (DELETE_PROC)GetProcAddress(hKernel32, "DeleteFileW");
#else
	local.fnDeleteFile			  = (DELETE_PROC)GetProcAddress(hKernel32, "DeleteFileA");
#endif

	// duplicate our own process handle for remote process to wait on
	hCurProc = GetCurrentProcess();
	DuplicateHandle(hCurProc, hCurProc, hRemoteProcess, &local.hProcess, 0, FALSE, DUPLICATE_SAME_ACCESS);
	
	// find name of current executable
	GetModuleFileName(NULL, local.szFileName, MAX_PATH);

	// write in code to execute, and the remote structure
	WriteProcessMemory(hRemoteProcess, code,    GetFunctionAddr(RemoteThread), 128, 0);
	WriteProcessMemory(hRemoteProcess, remote, &local, sizeof(local), 0);

	wsprintf(ach, "%x %x\n", code, remote);
	OutputDebugString(ach);

	// execute the code in remote process
	hThread = CreateRemoteThread(hRemoteProcess, 0, 0, (LPTHREAD_START_ROUTINE) code, remote, 0, &dwThreadId);

	if(hThread != 0)
	{
		CloseHandle(hThread);
	}
	
	return TRUE;
}

int main(void)
{
	SelfDelete();

	return 0;
}
