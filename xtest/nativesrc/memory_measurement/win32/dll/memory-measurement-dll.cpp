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

/*
 * Memory footprint measurement library 
 */


#include <windows.h>
#include <stdio.h>
#include <psapi.h>
#include <jni.h>

#include "org_netbeans_junit_MemoryMeasurement.h"

/*
 * Measures size of process with pid 'pid 
 */
JNIEXPORT jlong JNICALL Java_org_netbeans_junit_MemoryMeasurement_getProcessMemoryFootPrintNative
(JNIEnv *env, jobject instance, jlong pid) {
	
	HANDLE hProcess;
    PROCESS_MEMORY_COUNTERS pmc;
	long return_value = org_netbeans_junit_MemoryMeasurement_UNKNOWN_VALUE;

	// get process handler
	hProcess = OpenProcess(  PROCESS_QUERY_INFORMATION |
                                    PROCESS_VM_READ,
                                    FALSE, (DWORD)pid );
    if (NULL == hProcess)
        return org_netbeans_junit_MemoryMeasurement_UNKNOWN_VALUE;

    if ( GetProcessMemoryInfo( hProcess, &pmc, sizeof(pmc)) )  {
		return_value = (long) pmc.PagefileUsage;
	}

    CloseHandle( hProcess );

	return return_value;

}


