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

/*JNI function definitions end*/
