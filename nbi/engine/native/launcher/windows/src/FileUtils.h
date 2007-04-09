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
 * $Id$
 */
#include <windows.h>

#ifndef _FileUtils_H
#define	_FileUtils_H

#ifdef	__cplusplus
extern "C" {
#endif
    
    #define OUTPUT_LEVEL_DEBUG 0
#define OUTPUT_LEVEL_NORMAL 1
    
    
    
    extern HANDLE stdoutHandle;
    extern HANDLE stderrHandle;
    extern DWORD outputLevel;
    extern DWORD checkForFreeSpace;
    extern const WCHAR * FILE_SEP;
    double getFreeSpace(WCHAR *path);
    DWORD checkFreeSpace(WCHAR *path, DWORD size);    
    WCHAR * getParentDirectory(WCHAR * dir);
    DWORD createDirectory(WCHAR * dir);
    void createTempDirectory(DWORD * status, WCHAR * argTempDir, WCHAR ** resultDir, DWORD createRndSubDir);
    void deleteDirectory(WCHAR * dir);
    WCHAR * getExeName();
    WCHAR * getSystemTemporaryDirectory();
    WCHAR * getExeDirectory();
    DWORD isDirectory(WCHAR *path);
    WCHAR * getCurrentDirectory();
    WCHAR * getCurrentUserHome();
    
    HANDLE getStdoutHandle();
    HANDLE getStderrHandle();
    
    
    void writeMessageW(DWORD level, HANDLE hd, const WCHAR * message, DWORD needEndOfLine);
    void writeMessageA(DWORD level, HANDLE hd, const char * message, DWORD needEndOfLine);
    void writeErrorA(DWORD level, HANDLE hd, const char * message, const WCHAR * param, DWORD errorCode);
    
    void flushHandle(HANDLE hd);
    
  
    void  setStdoutHandle(HANDLE hndl);
    void  setStderrHandle(HANDLE hndl);
    
    DWORD fileExists(WCHAR * path);
    
    #ifdef	__cplusplus
}
#endif

#endif	/* _FileUtils_H */
