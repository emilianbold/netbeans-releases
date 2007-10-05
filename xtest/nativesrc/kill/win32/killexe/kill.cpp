/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

