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
#include <windows.h>
#include <winnls.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "StringUtils.h"

//adds string the the initial string and modifies totalWCHARs and capacity
// initial - the beginning of the string
// size - pointer to the value that contains the length of the initial string
//        It is modified to store returning string length
// addString - additional string
//
I18NStrings * i18nMessages;

void freeI18NMessages() {
    if(i18nMessages!=NULL) {
        DWORD i=0;
        for(i=0;i<I18N_PROPERTIES_NUMBER;i++) {
            FREE(i18nMessages->properties[i]);
            FREE(i18nMessages->strings[i]);
        }
        FREE(i18nMessages->properties);
        FREE(i18nMessages->strings);
        free(i18nMessages);
    }
}

WCHAR * getI18nProperty(char * name) {
    if(name==NULL) return NULL;
    if(name!=NULL) {
        DWORD i;
        for(i=0;i<I18N_PROPERTIES_NUMBER;i++) {
            char * pr = i18nMessages->properties[i];
            if(pr!=NULL) { // hope so it`s true
                if(strcmp(name, pr)==0) {
                    return i18nMessages->strings[i];
                }
            }
        }
    }
    
    return getDefaultString(name);
}

WCHAR * getDefaultString(char *name) {
    if(strcmp(name, JVM_NOT_FOUND_PROP)==0) {
        return L"Can`t find suitable JVM. Specify it with %s argument";
    } else if(strcmp(name, NOT_ENOUGH_FREE_SPACE_PROP)==0) {
        return L"Not enought free space at %s";
    } else if(strcmp(name, CANT_CREATE_TEMP_DIR_PROP)==0) {
        return L"Can`t create temp directory %s";
    } else if(strcmp(name, INTEGRITY_ERROR_PROP)==0) {
        return L"Integrity error. File %s is corrupted";
    } else if(strcmp(name, JVM_USER_DEFINED_ERROR_PROP)==0) {
        return L"Can`t find JVM at %s";
    } else if(strcmp(name, JVM_UNSUPPORTED_VERSION_PROP)==0) {
        return L"Unsupported JVM at %s";
    } else if(strcmp(name, OUTPUT_ERROR_PROP)==0) {
        return L"Can`t create file %s.\nError: %s";
    } else if(strcmp(name, JAVA_PROCESS_ERROR_PROP)==0) {
        return L"Java error %s";
    } else if(strcmp(name, ARG_JAVA_PROP)==0) {
        return L"%s Using specified JVM";
    }  else if(strcmp(name, ARG_OUTPUT_PROPERTY)==0) {
        return L"%s Output all stdout/stderr to the file";
    } else if(strcmp(name, ARG_DEBUG_PROP)==0) {
        return L"%s Use debug output";
    } else if(strcmp(name, ARG_TMP_PROP)==0) {
        return L"%s Use specified temporary dir for extracting data";
    } else if(strcmp(name, ARG_CPA_PROP)==0) {
        return L"%s Append classpath";
    }  else if(strcmp(name, ARG_CPP_PROP)==0) {
        return L"%s Prepend classpath";
    } else if(strcmp(name, ARG_EXTRACT_PROP)==0) {
        return L"%s Extract all data";
    }  else if(strcmp(name, ARG_HELP_PROP)==0) {
        return L"%s Using this help";
    } else if(strcmp(name, ARG_DISABLE_SPACE_CHECK)==0) {
        return L"%s Disable free space check";
    } else if(strcmp(name, MSG_CREATE_TMPDIR)==0) {
        return L"Creating tmp directory...";
    } else if(strcmp(name, MSG_EXTRACT_DATA)==0) {
        return L"Extracting data...";
    } else if(strcmp(name, MSG_JVM_SEARCH)==0) {
        return L"Finding JVM...";
    } else if(strcmp(name, MSG_RUNNING)==0) {
        return L"Running JVM...";
    } else if(strcmp(name, MSG_SET_OPTIONS)==0) {
        return L"Setting command options...";
    } else if(strcmp(name, MSG_MESSAGEBOX_TITLE)==0) {
        return L"Message";
    }
    return NULL;
}

DWORD getLengthA(const char * message) {
    return (message!=NULL) ? strlen(message) : 0;
}

DWORD getLengthW(const WCHAR * message) {
    return (message!=NULL) ? wcslen(message) : 0;
}

//adds string the the initial string
char *  appendStringN(char *  initial, DWORD initialLength, const char * addString, DWORD addStringLength) {
    DWORD length = initialLength + addStringLength + 1;
    if (length > 1) {
        char * tmp = newpChar(length+1);
        DWORD i=0;
        if(initialLength!=0) {
            for(i=0;i<initialLength;i++) {
                tmp[i]=initial[i];
            }
            free(initial);
        }
        for(i=0;i<addStringLength;i++) {
            tmp[i+initialLength] = addString[i];
        }
        
        return tmp;
    } else {
        return NULL;
    }
}

char * appendString(char *  initial, const char * addString) {
    return appendStringN(initial, getLengthA(initial), addString, getLengthA(addString));
}


//adds string the the initial string
WCHAR *  appendStringNW(WCHAR *  initial, DWORD initialLength, const WCHAR * addString, DWORD addStringLength) {
    
    DWORD length = initialLength + addStringLength + 1;
    if(length>1) {
        
        WCHAR * tmp = newpWCHAR(length+1);
        DWORD i=0;
        
        if(initialLength!=0) {
            for(i=0;i<initialLength;i++) {
                tmp[i]=initial[i];
            }
            free(initial);
        }
        
        for(i=0;i<addStringLength;i++) {
            tmp[i+initialLength] = addString[i];
        }
        tmp[length] = 0;
        
        return tmp;
    } else {
        
        return NULL;
    }
}

WCHAR * appendStringW(WCHAR *  initial, const WCHAR * addString) {
    return appendStringNW(initial, getLengthW(initial), addString, getLengthW(addString));
}

char * DWORDtoCHAR(DWORD dw) {
    char * str = (char*) malloc(sizeof(char)*17);
    sprintf(str, "%u", dw);
    return str;
}

WCHAR * DWORDtoWCHAR(DWORD dw) {
    WCHAR * str = (WCHAR*) malloc(sizeof(WCHAR)*17);
    wsprintfW(str, L"%u", dw);
    return str;
}

char * int64ttoCHAR(int64t* value) {
    if(value->High==0) {
        return DWORDtoCHAR(value->Low);
    } else {
        char * str = newpChar(34);
        double d = ((double) value->High * (MAXDWORD + 1)) + ((double) value->Low);
        sprintf(str, "%.0lf", d);
        return str;
    }
}
WCHAR * int64ttoWCHAR(int64t*value) {
    if(value->High==0) {
        return DWORDtoWCHAR(value->Low);
    } else {
        WCHAR * str = newpWCHAR(34);
        double d = ((double) value->High * (MAXDWORD + 1)) + ((double) value->Low);
        wsprintfW(str, L"%.0lf", d);
        return str;
    }
}


char * doubleToChar(double dl) {
    char * str = newpChar(17);
    sprintf(str, "%.0lf", dl);
    return str;
}

void freeStreamString(StreamString **ss) {
    while ( (*ss) !=NULL) {
        FREE((*ss)->bytes);
        StreamString * tmp = (*ss)->next;
        FREE((*ss));
        * ss = tmp;
    }
}

DWORD getLineSeparatorNumber(char *str) {
    DWORD result = 0;
    char *ptr = str;
    if(ptr!=NULL) {
        while((ptr = strstr(ptr, "\n"))!=NULL) {
            ptr++;
            result++;
            if(ptr==NULL)  break;
        }
    }
    return result;
}

char *toCharN(const WCHAR * string, DWORD n) {
    if(string==NULL) return NULL;
    //static DWORD excludeCodepages [] = { 50220, 50221, 50222, 50225, 50227, 50229, 52936, 54936, 57002,  57003, 57004, 57005, 57006, 57007, 57008, 57009, 57010, 57011, 65000, 42};
    //int symbols = 0;
    DWORD len = getLengthW(string);
    if(n<len) len = n;
    DWORD length = WideCharToMultiByte(CP_ACP, 0, string, len, NULL, 0, 0, NULL);
    char * str = newpChar(length+1);
    WideCharToMultiByte(CP_ACP, 0, string, len, str, length, 0, NULL);
    return str;
}

char * toChar(const WCHAR * string) {
    return toCharN(string, getLengthW(string));
}

WCHAR *createWCHAR(SizedString * sz) {
    char * str = sz->bytes;
    if(str==NULL) return NULL;
    //static DWORD excludeCodepages [] = { 50220, 50221, 50222, 50225, 50227, 50229, 52936, 54936, 57002,  57003, 57004, 57005, 57006, 57007, 57008, 57009, 57010, 57011, 65000, 42};
    
    DWORD len = sz->length;
    
    int unicodeFlags;
    
    DWORD i;
    char * string = appendStringN(NULL, 0 , str, len);
    
    char * ptr = string;
    unicodeFlags = -1 ;
    if(len>=2) {
        BOOL hasBOM        = (*ptr == '\xFF' && *(ptr+1) == '\xFE');
        BOOL hasReverseBOM = (*ptr == '\xFE' && *(ptr+1) == '\xFF');
        
        if (IsTextUnicode(string, len, &unicodeFlags) || hasBOM || hasReverseBOM) {
            //text is unicode
            len-= 2;
            ptr+= 2;
            if (unicodeFlags & IS_TEXT_UNICODE_REVERSE_SIGNATURE || hasReverseBOM) {
                //we need to change bytes order
                char c;
                for (i = 0 ; i < len/2 ; i++) {
                    c = ptr [2 * i] ;
                    ptr [2 * i] = ptr [2 * i + 1] ;
                    ptr [2 * i + 1] = c;
                }
            }
        }
        
    }
    WCHAR * wstr = newpWCHAR(len/2+1);
    
    for(i=0;i<len/2;i++) {
        ptr[2*i] = (ptr[2*i]) & 0xFF;
        ptr[2*i+1] = (ptr[2*i+1])& 0xFF;
        wstr[i] = ptr[2*i] + (ptr[2*i+1]  << 8);
    }
    
    FREE(string);
    return wstr;
}
WCHAR *toWCHARn(char * str, DWORD n) {
    if(str==NULL) return NULL;
    //static DWORD excludeCodepages [] = { 50220, 50221, 50222, 50225, 50227, 50229, 52936, 54936, 57002,  57003, 57004, 57005, 57006, 57007, 57008, 57009, 57010, 57011, 65000, 42};
    DWORD len = getLengthA(str);
    if(n<len) len = n;
    DWORD length = MultiByteToWideChar(CP_ACP, 0, str, len, NULL, 0);
    WCHAR * wstr = newpWCHAR(length+1);
    MultiByteToWideChar(CP_ACP, 0, str, len, wstr, length);
    return wstr;
}

WCHAR * toWCHAR( char *string) {
    return toWCHARn(string, getLengthA(string));
}

SizedString * createSizedString() {
    SizedString * s = (SizedString*)malloc(sizeof(SizedString));
    s->bytes = NULL;
    s->length = 0;
    return s;
}

void freeSizedString(SizedString ** s) {
    if(*s!=NULL) {
        FREE((*s)->bytes);
        FREE((*s));
        *s = NULL;
    }
}


WCHAR * getLocaleName() {
    LANGID langID;
    LCID localeID;
    langID = LANGIDFROMLCID(GetUserDefaultLCID());
    localeID = MAKELCID(langID, SORT_DEFAULT);
    const DWORD MAX_LENGTH = 512;
    
    WCHAR * lang= newpWCHAR(MAX_LENGTH);
    GetLocaleInfoW(localeID, LOCALE_SISO639LANGNAME, lang, MAX_LENGTH);
    
    WCHAR * country = newpWCHAR(MAX_LENGTH);
    GetLocaleInfoW(localeID, LOCALE_SISO3166CTRYNAME, country, MAX_LENGTH);
    
    WCHAR * locale = appendStringW(appendStringW(appendStringW(NULL, lang), L"_"), country);
    FREE(country);
    FREE(lang);
    return locale;
}

WCHAR * newpWCHAR(DWORD length) {
    WCHAR * res = (WCHAR*) malloc(sizeof(WCHAR) * length);
    memset(res, 0, length * sizeof(WCHAR));
    return res;
}
WCHAR ** newppWCHAR(DWORD length) {
    return (WCHAR**) malloc(sizeof(WCHAR *) * length);
}


char * newpChar(DWORD length) {
    char * res = (char*) malloc(sizeof(char) * length);
    memset(res, 0, length * sizeof(char));
    return res;
}

char ** newppChar(DWORD length) {
    return (char**) malloc(sizeof(char*) * length);
}

int compare(int64t * size, DWORD value) {
    if (size->High > 0) return 1;
    if (size->Low > value)  return 1;
    if (size->Low == value) return 0;
    if (size->Low < value)  return -1;
}

void plus(int64t * size, DWORD value) {
    if(value!=0) {
        if((MAXDWORD - size->Low) >= (value - 1)) {
            size->Low = size->Low + value;
        } else {
            size->High = size->High + 1;
            size->Low  = value - (MAXDWORD - size->Low) - 1;
        }
    }
}

void minus(int64t * size, DWORD value) {
    if(value!=0) {
        if(size->Low < value) {
            size->High = size->High -1;
            size->Low = size->Low + (MAXDWORD - value) + 1;
        } else {
            size->Low = size->Low - value;
        }}
}
int64t * newint64_t(DWORD low, DWORD high) {
    int64t * res = (int64t *) malloc(sizeof(int64t));
    res->Low = low;
    res->High = high;
    return res;
}
WCHAR * getErrorDescription(DWORD dw) {
    WCHAR * lpMsgBuf;
    WCHAR * lpDisplayBuf;
    
    FormatMessageW( FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
    NULL, dw, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPWSTR) &lpMsgBuf, 0, NULL );
    
    lpDisplayBuf = newpWCHAR(getLengthW(lpMsgBuf) + 40);
    wsprintfW(lpDisplayBuf, L"Error code (%u): %s", dw, lpMsgBuf);
    
    LocalFree(lpMsgBuf);
    
    return lpDisplayBuf;
    
}

WCHAR * formatMessageW(const DWORD varArgsNumber, const WCHAR* message, ...) {
    DWORD totalLength=getLengthW(message);
    va_list ap;
    va_start(ap, message);
    DWORD counter=0;
    while((counter++)<varArgsNumber) {
        WCHAR * arg = va_arg( ap, WCHAR * );
        totalLength+=getLengthW(arg);
    }
    va_end(ap);
    
    WCHAR * result = newpWCHAR(totalLength + 1);
    va_start(ap, message);
    wvsprintfW(result, message, ap);
    va_end(ap);
    return result;
}
