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
#ifndef _ProcessUtils_H
#define	_ProcessUtils_H

#include <windows.h>
#include <stdio.h>
#include "Errors.h"
#include "Types.h"
#include "ExtractUtils.h"
#include "FileUtils.h"



#ifdef	__cplusplus
extern "C" {
#endif
    
    #define STREAM_BUF_LENGTH 1024
    
    extern const DWORD DEFAULT_PROCESS_TIMEOUT;
    
    char * readHandle(HANDLE hRead);
    
    void executeCommand(LauncherProperties * props, WCHAR * command, WCHAR * dir, DWORD timeLimitMillis, HANDLE hWriteOutput, HANDLE hWriteError, DWORD priority);
    
#ifdef	__cplusplus
}
#endif

#endif	/* _ProcessUtils_H */
