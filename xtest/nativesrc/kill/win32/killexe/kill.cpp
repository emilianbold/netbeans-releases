/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

// killexe.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <stdio.h>
#include <stdlib.h>
#include <Windows.h>

#define KILL_EXIT_CODE 9

#define KILL_OK 0
#define KILL_ERROR 1

int kill_process(long pid) {

    DWORD wpid = (DWORD) pid;
    BOOL result;
    HANDLE w_process;
	
	/*
    printf("Trying to kill %d\n",wpid);
	*/

	w_process = OpenProcess(PROCESS_TERMINATE, FALSE, wpid);
	if (w_process == NULL) {
		return -1;
	}
	
	result = TerminateProcess(w_process,KILL_EXIT_CODE);

    if (result != 0) {
        return KILL_OK;
    } else {
        return KILL_ERROR;
    }
}


void printUsage() {
	printf("kill usage:\n");
	printf("kill.exe <PID>\n");
	printf("where <PID> is PID of process to be killed\n");		
}

int main(int argc, char* argv[])
{
	char *pidString;
	long pid;
	if (argc != 2) {
		printUsage();
		return -1;
	}
	pidString = argv[1];
	pid = atol(pidString);

	if (pid < 1) {
		printf("bad argument supplied, PID has to be a number > 0\n");
		return KILL_ERROR;
	}
	/*printf("Killing process with pid = %d\n",pid);*/
	return kill_process(pid);

}

