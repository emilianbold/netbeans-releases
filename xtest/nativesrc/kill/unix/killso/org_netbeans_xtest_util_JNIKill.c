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

#include <sys/types.h>
#include <signal.h>

#include <unistd.h>


#define KILL_SIGNAL 9
#define DUMP_SIGNAL 3

/*
 * Class:     org.netbeans.xtest.util.JNIKill
 * Method:    killProcess
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_xtest_util_JNIKill_killProcess__J
 (JNIEnv *environment, jobject instance, jlong pid)
{
    pid_t process_pid;
    int result;
    process_pid = (pid_t) pid;
    printf("Trying to kill %d\n",process_pid);
    result = kill(process_pid,KILL_SIGNAL);
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
    pid_t process_pid; 
    jlong jpid; 
    process_pid = getpid();
    printf("my pid = %d\n",process_pid);
    
    jpid = (jlong) process_pid;
    /*jlong long_pid = (jlong) pid;
    return long_pid;*/
    return jpid;

}

/*
 * Class:     org_netbeans_xtest_util_JNIKill
 * Method:    startDumpThread
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_xtest_util_JNIKill_startDumpThread
  (JNIEnv *environment, jobject instance)
{
    return JNI_TRUE;
}

/*
 * Class:     org_netbeans_xtest_util_JNIKill
 * Method:    dumpMe
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_xtest_util_JNIKill_dumpMe
  (JNIEnv *environment, jobject instance)
{
    pid_t process_pid;
    int result;
    process_pid = getpid();
    printf("Trying to self-dump %d\n",process_pid);
    result = kill(process_pid,DUMP_SIGNAL);
    if (result == 0) {
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}

/*
 * Class:     org_netbeans_xtest_util_JNIKill
 * Method:    requestDump
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_xtest_util_JNIKill_requestDump
  (JNIEnv *environment, jobject instance, jlong pid)
{
    pid_t process_pid;
    int result;
    process_pid = (pid_t) pid;
    printf("Trying to dump %d\n",process_pid);
    result = kill(process_pid,DUMP_SIGNAL);
    if (result == 0) {
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}


/*JNI function definitions end*/

