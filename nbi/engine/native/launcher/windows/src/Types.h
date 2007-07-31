/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

#ifndef _Types_H
#define	_Types_H

#include <windows.h>
#ifdef	__cplusplus
extern "C" {
#endif
    
    typedef struct int64s {
        DWORD Low;
        DWORD High;
    } int64t;
    
    
    typedef struct {
        long major;
        long minor;
        long micro;
        long update;
        char build [128];
    } JavaVersion;
    
    typedef struct _javaProps {
        WCHAR * javaHome;
        WCHAR * javaExe;
        char  * vendor;
        char  * osName;
        char  * osArch;
        JavaVersion *version;
    } JavaProperties;
    
    typedef struct _javaCompatible {
        JavaVersion * minVersion;
        JavaVersion * maxVersion;
        char * vendor;
        char * osName;
        char * osArch;
    } JavaCompatible;
    
    typedef struct _launcherResource {
        WCHAR * path;
        WCHAR * resolved;
        DWORD   type;        
    } LauncherResource;
    
    typedef struct _launcherResourceList {
        LauncherResource ** items;
        DWORD size;
    } LauncherResourceList;
    
    typedef struct _WCHARList {
        WCHAR ** items;
        DWORD size;
    } WCHARList;
    
    typedef struct _string {
        char * bytes;
        DWORD length;
    } SizedString ;
    
    typedef struct _i18nstrings {
        char  ** properties; //property name as ASCII
        WCHAR ** strings; //value as UNICODE
    } I18NStrings;
        
    
    
    typedef struct _stringListEntry {
        WCHAR * string;        
        struct _stringListEntry * next;
    } StringListEntry;
    
    typedef struct _launchProps {
        
        LauncherResourceList * jars;
        LauncherResourceList * jvms;
        LauncherResourceList * other;
        
        LauncherResource * testJVMFile;
        
        WCHAR * testJVMClass;
        WCHAR * tmpDir;
        DWORD   tmpDirCreated;
        int64t * bundledSize;
        DWORD bundledNumber;
        JavaCompatible ** compatibleJava;
        DWORD             compatibleJavaNumber;
        
        DWORD checkForFreeSpace;
        DWORD silent;
        WCHARList * jvmArguments;
        WCHARList * appArguments;
        
        DWORD    extractOnly;
        WCHAR  * classpath;
        WCHAR  * mainClass;
        
        JavaProperties  * java;
        WCHAR  * command;
        WCHAR  * exePath;
        WCHAR  * exeDir;
        WCHAR  * exeName;
        DWORD status;
        DWORD exitCode;
        DWORD silentMode;
        HANDLE handler;
        DWORD outputLevel;
        WCHARList * commandLine;
        HANDLE stdoutHandle;
        HANDLE stderrHandle;
        DWORD bufsize;
        int64t * launcherSize;
        DWORD  isOnlyStub;
        WCHAR * userDefinedJavaHome;
        WCHAR * userDefinedTempDir;
        WCHAR * userDefinedExtractDir;
        WCHAR * userDefinedOutput;
        WCHAR * userDefinedLocale;
        SizedString * restOfBytes;
        I18NStrings * i18nMessages;
        DWORD I18N_PROPERTIES_NUMBER;
        StringListEntry * alreadyCheckedJava;
        WCHARList * launcherCommandArguments;       
        
    } LauncherProperties ;
    
    
    
#ifdef	__cplusplus
}
#endif

#endif	/* _Types_H */

