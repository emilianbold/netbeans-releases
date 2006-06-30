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


#include "org_netbeans_xtest_util_JNIKill.h"
#include <stdio.h>

#include <Windows.h>


#define KILL_EXIT_CODE 9

/*
 * Class:     org.netbeans.xtest.util.JNIKill
 * Method:    killProcess
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_xtest_util_JNIKill_killProcess__J
 (JNIEnv *environment, jobject instance, jlong pid)
{
    DWORD wpid = (DWORD) pid;
    BOOL result;
    HANDLE w_process;
	
	/*
    printf("Trying to kill %d\n",wpid);
	*/

	w_process = OpenProcess(PROCESS_TERMINATE, FALSE, wpid);
	result = TerminateProcess(w_process,KILL_EXIT_CODE);

    if (result == 0) {
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
    
}
/*
 * Class:     org.netbeans.xtest.util.JNIKill
 * Method:    getMyPid
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_netbeans_xtest_util_JNIKill_getMyPID__
 (JNIEnv *environment, jobject instance)
{
    DWORD wpid;
    jlong jpid; 
    wpid = GetCurrentProcessId();
    /* printf("my pid = %d\n",wpid);    */
    jpid = (jlong) wpid;    
    return jpid;

}

static DWORD tid=~0;

void dumper(void *param) {
	char event[30];
	HANDLE hEvent;
	sprintf(event, "ThreadDumpEvent%d", GetCurrentProcessId());
    hEvent = CreateEvent(NULL, TRUE, FALSE, event);
    while (TRUE) {
        if (WaitForSingleObject(hEvent, INFINITE) == WAIT_OBJECT_0) {
            GenerateConsoleCtrlEvent(CTRL_BREAK_EVENT, 0);
        }
    }
}

/*
 * Class:     org_netbeans_xtest_util_JNIKill
 * Method:    startDumpThread
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_xtest_util_JNIKill_startDumpThread
  (JNIEnv *environment, jobject instance)
{
    if (tid!=~0) return FALSE;
    return CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)dumper, NULL, 0, &tid)!=NULL;
}

/*
 * Class:     org_netbeans_xtest_util_JNIKill
 * Method:    dumpMe
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_xtest_util_JNIKill_dumpMe
  (JNIEnv *environment, jobject instance)
{
    return GenerateConsoleCtrlEvent(CTRL_BREAK_EVENT, 0);
}

/*
 * Class:     org_netbeans_xtest_util_JNIKill
 * Method:    requestDump
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_xtest_util_JNIKill_requestDump
  (JNIEnv *environment, jobject instance, jlong pid)
{
	char event[30];
	HANDLE hEvent;
	sprintf(event, "ThreadDumpEvent%d", pid);
	hEvent = OpenEvent(EVENT_MODIFY_STATE, FALSE,  event);
	if (hEvent == NULL) return FALSE;
	return PulseEvent(hEvent);
}


/*JNI function definitions end*/
