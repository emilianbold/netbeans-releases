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
#ifndef _StringUtils_H
#define	_StringUtils_H

#include <windows.h>
#include <stdarg.h>
#ifdef	__cplusplus
extern "C" {
#endif
    
    

#define  JVM_NOT_FOUND_PROP           "nlw.jvm.notfoundmessage"
#define  JVM_USER_DEFINED_ERROR_PROP  "nlw.jvm.usererror"
#define  JVM_UNSUPPORTED_VERSION_PROP "nlw.jvm.unsupportedversion"
#define  NOT_ENOUGH_FREE_SPACE_PROP   "nlw.freespace"
#define  CANT_CREATE_TEMP_DIR_PROP    "nlw.tmpdir"
#define  INTEGRITY_ERROR_PROP         "nlw.integrity"
#define  OUTPUT_ERROR_PROP            "nlw.output.error"
#define  JAVA_PROCESS_ERROR_PROP      "nlw.java.process.error"
    
#define  ARG_OUTPUT_PROPERTY          "nlw.arg.output"
#define  ARG_JAVA_PROP                "nlw.arg.javahome"
#define  ARG_DEBUG_PROP               "nlw.arg.debug"
#define  ARG_TMP_PROP                 "nlw.arg.tempdir"
#define  ARG_CPA_PROP                 "nlw.arg.classpatha"
#define  ARG_CPP_PROP                 "nlw.arg.classpathp"
#define  ARG_EXTRACT_PROP             "nlw.arg.extract"
#define  ARG_DISABLE_SPACE_CHECK      "nlw.arg.disable.space.check"
#define  ARG_HELP_PROP                "nlw.arg.help"

#define MSG_CREATE_TMPDIR     "nlw.msg.create.tmpdir"
#define MSG_EXTRACT_DATA      "nlw.msg.extract"    
#define MSG_JVM_SEARCH        "nlw.msg.jvmsearch"    
#define MSG_SET_OPTIONS       "nlw.msg.setoptions"
#define MSG_RUNNING           "nlw.msg.running"
#define MSG_TITLE             "nlw.msg.title"    
#define MSG_MESSAGEBOX_TITLE  "nlw.msg.messagebox.title"
    

    
#define FREE(x) { if((x)!=NULL) {free(x); (x)=NULL;}}
    
    typedef struct _string {
        char * bytes;
        DWORD length;
    } SizedString ;
    
    
    typedef struct _streamstring {
        WCHAR * bytes;
        DWORD length;
        struct _streamstring * next;
    } StreamString;
    
    typedef struct _i18nstrings {
        char  ** properties; //property name as ASCII
        WCHAR ** strings; //value as UNICODE
    } I18NStrings;
    
    
    extern I18NStrings * i18nMessages;
    extern DWORD I18N_PROPERTIES_NUMBER;
    
    void initializeI18NMessages();
    WCHAR * getI18nProperty(char * name);
    WCHAR * getDefaultString(char *name);
    
    WCHAR * addString(WCHAR *  initial, WCHAR *addString, long number, WCHAR * totalWCHARs, WCHAR * capacity);
    char *  appendStringN(char *  initial, DWORD initialLength, const char * addString, DWORD addStringLength);
    WCHAR *  appendStringNW(WCHAR *  initial, DWORD initialLength, const WCHAR * addString, DWORD addStringLength);
    char * appendString(char *  initial, const char * addString);
    WCHAR * appendStringW(WCHAR *  initial, const WCHAR * addString);
    
    void freeStreamString(StreamString **s);
    
    char *toChar(const WCHAR * string);
    char *toCharN(const WCHAR * string, DWORD length);
    WCHAR * toWCHAR(char * string);
    WCHAR * toWCHARn(char * string, DWORD length);
    
    WCHAR *createWCHAR(SizedString * sz);
    
    SizedString * createSizedString();
    
    char * DWORDtoCHAR(DWORD);
    WCHAR * DWORDtoWCHAR(DWORD);
    char * doubleToChar(double dl);
    
    void freeSizedString(SizedString ** s);
        
    WCHAR * getLocaleName();
    
    WCHAR * newpWCHAR(DWORD length);
    char * newpChar(DWORD length);
    
    WCHAR ** newppWCHAR(DWORD length);
    char ** newppChar(DWORD length);
    
    DWORD getLineSeparatorNumber(char *str);
    DWORD getLengthA(const char * message);
    DWORD getLengthW(const WCHAR * message);
    
    WCHAR * getErrorDescription(DWORD dw);
    WCHAR * formatMessageW(const DWORD varArgsNumber, const WCHAR* message, ...);
#ifdef	__cplusplus
}
#endif

#endif	/* _StringUtils_H */
