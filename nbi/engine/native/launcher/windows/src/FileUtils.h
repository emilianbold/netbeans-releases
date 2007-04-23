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
#ifndef _FileUtils_H
#define	_FileUtils_H

#include <windows.h>
#include "Errors.h"
#include "Types.h"

#ifdef	__cplusplus
extern "C" {
#endif
    
#define OUTPUT_LEVEL_DEBUG 0
#define OUTPUT_LEVEL_NORMAL 1
    
    
    extern const WCHAR * FILE_SEP;
    
    int64t * getFreeSpace(WCHAR *path);
    int64t * getFileSize(WCHAR * path);
    void checkFreeSpace(LauncherProperties * props, WCHAR * tmpDir, int64t * size);
    WCHAR * getParentDirectory(WCHAR * dir);
    void createDirectory(LauncherProperties * props, WCHAR * directory);
    void createTempDirectory(LauncherProperties * props, WCHAR * argTempDir, DWORD createRndSubDir);
    void deleteDirectory(LauncherProperties * props,WCHAR * dir);
    WCHAR * getExePath();
    WCHAR * getExeName();
    WCHAR * getExeDirectory();
    
    WCHAR * getSystemTemporaryDirectory();    
    DWORD isDirectory(WCHAR *path);
    WCHAR * getCurrentDirectory();
    WCHAR * getCurrentUserHome();
        
    
    void writeMessageW(LauncherProperties * props, DWORD level,DWORD isErr,  const WCHAR * message, DWORD needEndOfLine);
    void writeMessageA(LauncherProperties * props,DWORD level, DWORD isErr,  const char  * message, DWORD needEndOfLine);
    void writeErrorA(LauncherProperties * props,DWORD level,   DWORD isErr,  const char  * message, const WCHAR * param, DWORD errorCode);
    void writeDWORD(LauncherProperties * props,DWORD level,    DWORD isErr,  const char  * message, DWORD value, DWORD needEndOfLine);
    void writeint64t(LauncherProperties * props,DWORD level,   DWORD isErr,  const char  * message, int64t * value, DWORD needEndOfLine);
    
    void flushHandle(HANDLE hd);
    DWORD fileExists(WCHAR * path);
    
    #ifdef	__cplusplus
}
#endif

#endif	/* _FileUtils_H */
