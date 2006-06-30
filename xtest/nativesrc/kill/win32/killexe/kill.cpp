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
 */

// killexe.cpp : Defines the entry point for the console application.
//


#include <stdio.h>
#include <stdlib.h>
#include <Windows.h>
#include "killproc.h"

#define KILL_EXIT_CODE 768

#define KILL_OK 0
#define KILL_ERROR 1

int dump_process(long pid) {
	char event[30];
	HANDLE hEvent;
	BOOL result;
	sprintf(event, "ThreadDumpEvent%d", pid);
	hEvent = OpenEvent(EVENT_MODIFY_STATE, FALSE,  event);
	if (hEvent == NULL) {
		return KILL_ERROR;
	} else {
		result = PulseEvent(hEvent);
		if (result != 0) {
			return KILL_OK;
		} else {
			return KILL_ERROR;
		}
	}
}


void printUsage() {
	printf("kill usage:\n");
	printf("kill.exe [-3|-9] <PID>\n");
	printf("where <PID> is PID of process to be killed or dumped\n");		
}

int main(int argc, char* argv[])
{
	char *pidString;
	long pid;
	if (argc != 2 && (argc !=3 || (strcmp(argv[1], "-3") && strcmp(argv[1], "-9")))) {
		printUsage();
		return -1;
	}
	pidString = argv[argc-1];
	pid = atol(pidString);

	if (pid < 1) {
		printf("bad argument supplied, PID has to be a number > 0\n");
		return KILL_ERROR;
	}
	/*printf("Killing process with pid = %d\n",pid);*/
	if (argc == 3 && (strcmp(argv[1], "-3")==0)) {
		return dump_process(pid);
	} else {
		return KillProcessEx(pid, TRUE);
	}
}

