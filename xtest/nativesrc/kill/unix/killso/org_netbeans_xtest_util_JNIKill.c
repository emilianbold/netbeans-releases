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

