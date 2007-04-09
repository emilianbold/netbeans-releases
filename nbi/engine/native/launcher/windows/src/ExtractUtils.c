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
 *
 * $Id$
 */

#include <windows.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "FileUtils.h"
#include "StringUtils.h"
#include "JavaUtils.h"
#include "RegistryUtils.h"
#include "ExtractUtils.h"
#include "Launcher.h"


HANDLE launcherHandle = INVALID_HANDLE_VALUE;
const DWORD   STUB_FILL_SIZE      = 85000;


DWORD skipLauncherStub(HANDLE hFileRead, DWORD stubSize, DWORD bufferSize) {
    DWORD status = ERROR_OK;
    if(hFileRead!=INVALID_HANDLE_VALUE) {
        // just read stub data.. no need to write it anywhere
        DWORD read = 0;
        char * offsetbuf = newpChar(stubSize);
        DWORD sizeLeft = stubSize;
        while(ReadFile(hFileRead, offsetbuf, sizeLeft, &read, 0) && sizeLeft && read) {
            sizeLeft-=read;
            addProgressPosition(read);
            if(sizeLeft==0) break;
            if(read==0) { // we need some more bytes to read but we can`t to read
                status = ERROR_INTEGRITY;
                break;
            }
        }
        free(offsetbuf);
    }
    return status;
}


void skipStub(DWORD * status, HANDLE hFileRead, DWORD bufsize) {
    *status = skipLauncherStub(hFileRead, STUB_FILL_SIZE, bufsize);
    if((*status)!=ERROR_OK) {
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStderrHandle(),
        "Error! Can`t process launcher stub", 1);
        WCHAR * exe = getExeName();
        showMessageW(1, getI18nProperty(INTEGRITY_ERROR_PROP), exe);
        FREE(exe);
    }
}

void modifyRestBytes(SizedString* rest, DWORD start) {
    
    DWORD len = rest->length - start;
    char * restBytesNew = NULL;
    if(len>0) {
        restBytesNew = newpChar(len);
        DWORD i;
        for(i=start;i<rest->length;i++) {
            restBytesNew[i-start] = (rest->bytes) [i];
        }
    }
    free(rest->bytes);
    rest->bytes = restBytesNew;
    rest->length = len;
}

DWORD readStringFromBuf(SizedString *rest, SizedString * result, DWORD isUnicode) {
    if((rest->length)!=0) {
        // we have smth in the restBytes that we have read but haven`t yet proceeded
        DWORD i=0;
        for(i=0;i<rest->length;i++) {
            DWORD check = ((rest->bytes)[i]==0);
            if(isUnicode) {
                if ( (i/2)*2==i) {// i is even
                    check = check && (i < rest->length-1 && ((rest->bytes)[i+1]==0));
                } else {
                    check = 0;
                }
            }
            if( check ) { // we have found null character in the rest bytes
                result->bytes = appendStringN(NULL, 0, rest->bytes, i);
                result->length = i;
                modifyRestBytes(rest, i + 1 + isUnicode);
                return ERROR_OK;
            }
        }
        //here we have found no \0 character in the rest of bytes...
    }
    return ERROR_INPUTOUPUT;
}

DWORD readString(HANDLE hFileRead, SizedString *rest, SizedString * result, DWORD bufferSize, DWORD isUnicode) {
    if(readStringFromBuf(rest, result, isUnicode)==ERROR_OK) {
        return ERROR_OK; // all OK
    }
    
    //we need to read file for more data to find \0 character...
    DWORD read=0;
    char * buf = newpChar(bufferSize);
    
    
    char * resultString = NULL;
    DWORD resultLength = 0;//*restBytesNumber;
    DWORD status = ERROR_OK;
    
    while (ReadFile(hFileRead, buf, bufferSize, &read, 0) && read) {
        addProgressPosition(read);
        rest->bytes = appendStringN(rest->bytes, rest->length, buf, read);
        rest->length = rest->length + read;
        if(readStringFromBuf(rest, result, isUnicode)==ERROR_OK) {
            //if(result->bytes!=NULL) {
            //we have find \0 character
            break;
        }
        memset(buf, 0, sizeof(char) * bufferSize);
        if(read==0) { // we have nothing to read.. smth wrong
            status = ERROR_INTEGRITY;
            break;
        }
    }
    FREE(buf);
    return status;
}



DWORD readNumber(HANDLE hFileRead, SizedString *rest, DWORD bufferSize, DWORD * result) {
    SizedString * numberString = createSizedString();
    DWORD status = readString(hFileRead, rest, numberString, bufferSize, 0);
    if(status!=ERROR_OK) {
        freeSizedString(&numberString);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(),
        "Error!! Can`t read number string. Most probably integrity error.", 1);
        return status;
    }
    if(numberString->bytes==NULL) {
        freeSizedString(&numberString);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(),
        "Error!! Can`t read number string (it can`t be NULL). Most probably integrity error.", 1);
        return ERROR_INTEGRITY;
    }
    DWORD i =0;
    DWORD number = 0;
    status = ERROR_OK;
    for(;i<numberString->length;i++) {
        char c = numberString->bytes[i];
        if(c>='0' && c<='9') {
            number = number * 10 + (c - '0');
        } else if(c==0) {
            // we have reached the end of number section
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(),
            "Can`t read number from string (it contains zero character):", 1);
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), numberString->bytes, 1);
            status = ERROR_INTEGRITY;
            break;
        } else {
            // unexpected...
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(),
            "Can`t read number from string (unexpected error):", 1);
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), numberString->bytes, 1);
            status = ERROR_INTEGRITY;
            break;
        }
    }
    freeSizedString(&numberString);
    
    *result = number;
    return status;
}



DWORD readStringWithDebugW(HANDLE hFileRead, SizedString * rest, DWORD bufferSize, WCHAR ** dest, char * paramName) {
    if(paramName!=NULL) {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "Reading ", 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), paramName, 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), " ...", 1);
    }
    SizedString *sizedStr = createSizedString();
    DWORD status = readString(hFileRead, rest, sizedStr, bufferSize, 1);
    if(status!=ERROR_OK) {
        freeSizedString(&sizedStr);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "Can`t read ", 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(),  paramName, 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(),  ". Seems to be integritiy error", 1);
        return status;
    }
    *dest = createWCHAR(sizedStr);
    freeSizedString(&sizedStr);
    if(paramName!=NULL) {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "    ", 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), paramName, 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), " : ", 0);
        if((*dest)!=NULL) {
            writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), *dest, 2);
        } else {
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "NULL", 2);
        }
    }
    return status;
}

DWORD readStringWithDebugA(HANDLE hFileRead, SizedString * rest, DWORD bufferSize, char ** dest, char * paramName) {
    if(paramName!=NULL) {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "Reading ", 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), paramName, 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), " ...", 1);
    }
    
    SizedString *sizedStr = createSizedString();
    DWORD status = readString(hFileRead, rest, sizedStr, bufferSize, 0);
    if(status!=ERROR_OK) {
        freeSizedString(&sizedStr);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(),
        "Can`t read ", 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(),  paramName, 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(),  ". Seems to be integritiy error", 1);
        return status;
    }
    *dest = appendString(NULL, sizedStr->bytes);
    if(paramName!=NULL) {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "    ", 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), paramName, 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), " : ", 0);
        if((*dest)==NULL) {
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "NULL", 2);
        } else {
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), *dest, 2);
        }
    }
    freeSizedString(&sizedStr);
    return status;
}


DWORD readNumberWithDebug(HANDLE hFileRead, SizedString * rest, DWORD bufferSize, DWORD * dest, char * paramName) {
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "Reading number ", 0);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), paramName, 0);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), " ...", 1);
    
    DWORD status = readNumber(hFileRead, rest, bufferSize, dest);
    
    if(status!=ERROR_OK) {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(),
        "Can`t read number string for ", 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(),  paramName, 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(),  ". Seems to be integritiy error", 1);
        return status;
    }
    char * num = DWORDtoCHAR(*dest);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "    ", 0);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), paramName, 0);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), " = ", 0);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), num, 2);
    FREE(num);
    
    return status;
}


// returns: ERROR_OK, ERROR_INPUTOUPUT, ERROR_INTEGRITY
DWORD extractDataToFile(HANDLE hFileRead, WCHAR *output, SizedString *rest, DWORD fileSize, DWORD bufferSize ) {
    DWORD size = fileSize;
    HANDLE hFileWrite = CreateFileW(output, GENERIC_READ | GENERIC_WRITE, 0, 0, CREATE_ALWAYS, 0, hFileRead);
    
    if (hFileWrite == INVALID_HANDLE_VALUE) {
        WCHAR * err = getErrorDescription(GetLastError());
        showMessageW(2, getI18nProperty(OUTPUT_ERROR_PROP), output, err);
        FREE(err);
        
        return ERROR_INPUTOUPUT;
    }
    if(rest->length!=0 && rest->bytes!=NULL) {
        
        //check if the data stored in restBytes is more than we neen
        // restBytesToWrite = min(*restBytesNumber, size);
        DWORD restBytesToWrite = (rest->length < size) ? rest->length : size;
        DWORD usedBytes = restBytesToWrite;
        
        char *ptr = rest->bytes;
        
        DWORD write = 0;
        while (restBytesToWrite >0) {
            WriteFile(hFileWrite, ptr, restBytesToWrite, &write, 0);
            restBytesToWrite -= write;
            ptr +=write;
        }
        modifyRestBytes(rest, usedBytes);
        size-=usedBytes;
    }

    DWORD status = ERROR_OK;
    if(size>0) {
        char * buf = newpChar(bufferSize);
        DWORD bufsize = (bufferSize < size) ? bufferSize : size;
        DWORD read = 0 ;
        //  printf("Using buffer size: %u/%u\n", bufsize, bufferSize);
        while (ReadFile(hFileRead, buf, bufsize, &read, 0) && read && size) {
            addProgressPosition(read);
            WriteFile(hFileWrite, buf, read, &read, 0);
            size-=read;
            //  printf("Read bytes = %u, size left= %u, bufsize = %u\n", read, size, bufsize);
            if(size < bufsize && size>0) {
                //    printf("buffer size changed to: %u\n", size);
                bufsize = size;
            }
            memset(buf, 0, sizeof(char) * bufferSize);
            if(size==0) {
                break;
            }            
        }
        if(size>0 || read==0) {
            // we could not read requested size
            status = ERROR_INTEGRITY;
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(),
            "Can`t read data from file : not enought data", 1);
        }
        FREE(buf);
    }
    CloseHandle(hFileWrite);
    return status;
}

//returns : ERROR_OK, ERROR_INTEGRITY, ERROR_FREE_SPACE
DWORD extractFileToDir(HANDLE hFileRead, WCHAR *dir, SizedString *rest, DWORD bufferSize, WCHAR ** resultFile) {
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... extracting file ...", 1);
    
    WCHAR * fileName = NULL;
    DWORD status = readStringWithDebugW(hFileRead, rest, bufferSize, & fileName, "... file name");
    
    DWORD fileLength;
    status = readNumberWithDebug(hFileRead, rest, bufferSize, &fileLength, "... length");
    
    if(status!=ERROR_OK) return status;
    
    if(fileName!=NULL) {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "      directory = ", 0);
        writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), dir, 1);
        
        WCHAR * output = appendStringW(appendStringW(appendStringW(NULL, dir), FILE_SEP), fileName);
        free(fileName);
        double spaceNeed = (double) fileLength;
        if(checkFreeSpace(dir, spaceNeed)) {
            status = extractDataToFile(hFileRead, output, rest, fileLength, bufferSize);
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "       ... extracted", 1);
            *resultFile = output;
        } else {
            status = ERROR_FREESPACE;
        }
    } else {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "Error! File name can`t be null. Seems to be integrity error!", 1);
        *resultFile = NULL;
        status = ERROR_INTEGRITY;
    }
    return status;
}
HANDLE getLauncherHandler(DWORD * status) {
    if(launcherHandle==INVALID_HANDLE_VALUE) {
        WCHAR *inputfile = getExeName();
        launcherHandle = CreateFileW(inputfile, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
        FREE(inputfile);
        if(launcherHandle==INVALID_HANDLE_VALUE) {
            * status = ERROR_INPUTOUPUT;
        }
    }
    return launcherHandle;
}

DWORD loadI18NStrings(HANDLE hFileRead, SizedString * rest, DWORD bufferSize) {
    DWORD i=0;
    DWORD j=0;
    //read number of locales
    
    DWORD numberOfLocales = 0;
    DWORD status;
    status = readNumberWithDebug(hFileRead, rest, bufferSize, &numberOfLocales, "number of locales");
    if(status!=ERROR_OK) return status;
    if(numberOfLocales==0) return ERROR_INTEGRITY;
    
    DWORD numberOfProperties;
    status = readNumberWithDebug(hFileRead, rest, bufferSize, &numberOfProperties, "i18n properties");
    if(status!=ERROR_OK)         return status;
    if(numberOfProperties==0) return ERROR_INTEGRITY;
    
    i18nMessages = (I18NStrings * ) malloc(sizeof(I18NStrings) * numberOfProperties);
    
    I18N_PROPERTIES_NUMBER = numberOfProperties;
    i18nMessages->properties = newppChar(I18N_PROPERTIES_NUMBER);
    i18nMessages->strings = newppWCHAR(I18N_PROPERTIES_NUMBER);
    
    
    for(i=0; status==ERROR_OK && i<numberOfProperties;i++) {
        // read property name as ASCII
        i18nMessages->properties[i] = NULL;
        i18nMessages->strings[i] = NULL;
        status = readStringWithDebugA(hFileRead, rest, bufferSize, & (i18nMessages->properties[i]), "property name");
    }
    if(status!=ERROR_OK) return status;
    
    DWORD isLocaleMatches;
    WCHAR * localeName;
    WCHAR * currentLocale = getLocaleName();
    
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "Current System Locale : ", 0);
    writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), currentLocale, 1);
    
    for(j=0;j<numberOfLocales;j++) { //  for all locales in file...
        // read locale name as UNICODE ..
        // it should be like en_US or smth like that
        localeName = NULL;
        status = readStringWithDebugW(hFileRead, rest, bufferSize, &localeName, "locale name");
        if(status!=ERROR_OK) break;
        
        isLocaleMatches = (localeName==NULL) ?  1 :  (wcsstr(currentLocale, localeName)!=-1);
        
        //read properties names and value
        for(i=0;i<numberOfProperties;i++) {
            // read property value as UNICODE
            
            WCHAR * value = NULL;
            char * s1 =  DWORDtoCHAR(i + 1);
            char * s2 =  DWORDtoCHAR(numberOfProperties);
            char * s3 = appendString(NULL , "value ");
            s3 = appendString(s3 , s1);
            s3 = appendString(s3, "/");
            s3 = appendString(s3, s2);
            FREE(s1);
            FREE(s2);
            status =  readStringWithDebugW(hFileRead, rest, bufferSize, &value, s3);
            
            FREE(s3);
            if(status!=ERROR_OK) break;
            if(isLocaleMatches) {
                //it is a know property
                FREE(i18nMessages->strings[i]);
                i18nMessages->strings[i] = appendStringW(NULL, value);
            }
            FREE(value);
        }
        FREE(localeName);
    }
    FREE(currentLocale);
    return status;
}

DWORD isOnlyLauncher() {
    return (getRunningFileSize() < STUB_FILL_SIZE);
}

DWORD getRunningFileSize() {
    WCHAR szPath[MAX_PATH];
    
    if(GetModuleFileNameW( NULL, szPath, MAX_PATH )) {
        return (DWORD) getFileSize(szPath);
    } else {
        return 0;
    }
}
LauncherResource * newLauncherResource() {
    LauncherResource * file = (LauncherResource *) malloc(sizeof(LauncherResource));
    file->path=NULL;
    file->resolved=NULL;
    file->type=0;
    return file;
}
WCHARList * newWCHARList(DWORD number) {
    WCHARList * list = (WCHARList*) malloc(sizeof(WCHARList));
    list->size  = number;
    if(number>0) {
        list->items = newppWCHAR(number);
        DWORD i=0;
        for(i=0;i<number;i++) {
            list->items[i] = NULL;
        }
    } else {
        list->items = NULL;
    }
    return list;
}

void freeWCHARList(WCHARList ** plist) {
    WCHARList * list;
    list = * plist;
    if(list!=NULL) {
        DWORD i=0;
        if(list->items!=NULL) {
            for(i=0;i<list->size;i++) {
                FREE(list->items[i]);
            }
            FREE(list->items);
        }
        FREE(*plist);
    }
}

LauncherResourceList * newLauncherResourceList(DWORD number) {
    LauncherResourceList * list = (LauncherResourceList*) malloc(sizeof(LauncherResourceList));
    list->size  = number;
    if(number > 0) {
        list->items = (LauncherResource**) malloc(sizeof(LauncherResource*) * number);
        DWORD i=0;
        for(i=0;i<number;i++) {
            list->items[i] = NULL;
        }
    } else {
        list->items = NULL;
    }
    return list;
}

void freeLauncherResource(LauncherResource ** file) {
    if(*file!=NULL) {
        FREE((*file)->path);
        FREE((*file)->resolved);
        FREE(*file);
    }
}


void extractLauncherResource(DWORD * status, HANDLE hFileRead, SizedString * rest, DWORD bufferSize, WCHAR *outputdir, LauncherResource ** file, char * name) {
    * file = newLauncherResource();
    char * typeStr = appendString(appendString(NULL, name), " type");
    *status = readNumberWithDebug(hFileRead, rest, bufferSize, & ((*file)->type) , typeStr);
    if((*status)==ERROR_OK) {
        free(typeStr);
        if((*file)->type==0) { //bundled
            *status = extractFileToDir(hFileRead, outputdir, rest, bufferSize, & ((*file)->path));
            if ((*status)!=ERROR_OK) {
                writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "Error extracting file!", 1);
                return;
            } else {
                writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "file was succesfully extracted to ", 0);
                writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), (*file)->path, 1);
            }
        } else {
            *status = readStringWithDebugW(hFileRead, rest, bufferSize, & ((*file)->path), name);
            if((*status)!=ERROR_OK) {
                writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "Error reading ", 1);
                writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), name, 1);
            }
        }
    }  else {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "Error reading ", 0);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), typeStr, 0);
        free(typeStr);
    }
}

void readWCHARList(DWORD * status, HANDLE hFileRead, SizedString * rest, DWORD bufferSize, WCHARList ** list, char * name) {
    DWORD number = 0;
    * list = NULL;
    DWORD i =0;
    char * numberStr = appendString(appendString(NULL, "number of "), name);
    *status = readNumberWithDebug(hFileRead, rest, bufferSize, &number, numberStr);
    FREE(numberStr);
    
    if((*status)!=ERROR_OK) return;
    
    * list = newWCHARList(number);
    for(i=0;i < (*list)->size ;i++) {
        char * nextStr = appendString(appendString(NULL, "next item in "), name);
        *status = readStringWithDebugW(hFileRead, rest, bufferSize, &((*list)->items[i]), nextStr);
        FREE(nextStr);
        if((*status)!=ERROR_OK) return;
    }
}
void readLauncherResourceList(DWORD * status, HANDLE hFileRead, SizedString * rest, DWORD bufferSize, WCHAR * outputdir, LauncherResourceList ** list, char * name) {
    DWORD num = 0;
    DWORD i=0;
    char * numberStr = appendString(appendString(NULL, "number of "), name);
    *status = readNumberWithDebug(hFileRead, rest, bufferSize, &num, numberStr);
    FREE(numberStr);
    if((*status)!=ERROR_OK) return;
    
    * list = newLauncherResourceList(num);
    for(i=0;i<(*list)->size;i++) {
        extractLauncherResource(status, hFileRead, rest, bufferSize, outputdir, & ((*list)->items[i]), name);
        if((*status)!=ERROR_OK) {
            char * str = appendString(appendString(NULL, "Error processing "), name);
            writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), str, 1);
            FREE(str);
            return;
        }
    }
}


void extractData(DWORD * status, HANDLE hFileRead, SizedString * rest, DWORD bufferSize, LauncherProperties *props) {
    if((*status)!=ERROR_OK) return;
    
    WCHAR * outputdir = props->tmpDir;
    DWORD i=0;
    
    readWCHARList(status, hFileRead, rest, bufferSize, &(props->jvmArguments), "jvm arguments");
    if((*status)!=ERROR_OK) return;
    
    readWCHARList(status, hFileRead, rest, bufferSize, &(props->appArguments), "app arguments");
    if((*status)!=ERROR_OK) return;
    
    *status = readStringWithDebugW(hFileRead, rest, bufferSize, &(props->mainClass), "Main Class");
    if((*status)!=ERROR_OK) return;
    
    *status = readStringWithDebugW(hFileRead, rest, bufferSize, &(props->testJVMClass), "TestJVM Class");
    if((*status)!=ERROR_OK) return;
    
    readLauncherResourceList(status, hFileRead, rest, bufferSize, outputdir, &(props->jvms), "JVMs");
    if((*status)!=ERROR_OK) return;
    
    *status = readNumberWithDebug(hFileRead, rest, bufferSize, &(props->compatibleJavaNumber),
    "compatible java");
    if((*status)!=ERROR_OK) return;
    
    char * str = NULL;
    
    if ( props->compatibleJavaNumber > 0 ) {
        props->compatibleJava = (JavaCompatible **) malloc(sizeof(JavaCompatible *) * props->compatibleJavaNumber);
        for(i=0;i<props->compatibleJavaNumber;i++) {
            
            props->compatibleJava [i] = newJavaCompatible() ;
            
            *status = readStringWithDebugA(hFileRead, rest, bufferSize, &str,
            "min java version");
            if((*status)!=ERROR_OK) return;
            props->compatibleJava[i]->minVersion = getJavaVersionFromString(str, status);
            FREE(str);
            if((*status)!=ERROR_OK) return;
            
            str = NULL;
            *status = readStringWithDebugA(hFileRead, rest, bufferSize, &str,
            "max java version");
            if((*status)!=ERROR_OK) return;
            props->compatibleJava[i]->maxVersion = getJavaVersionFromString(str, status);
            FREE(str);
            if((*status)!=ERROR_OK) return;
            
            *status = readStringWithDebugA(hFileRead, rest, bufferSize, &(props->compatibleJava[i]->vendor) ,
            "vendor");
            if((*status)!=ERROR_OK) return;
            
            *status = readStringWithDebugA(hFileRead, rest, bufferSize, &(props->compatibleJava[i]->osName) ,
            "os name");
            if((*status)!=ERROR_OK) return;
            
            *status = readStringWithDebugA(hFileRead, rest, bufferSize, &(props->compatibleJava[i]->osArch) ,
            "os arch");
            if((*status)!=ERROR_OK) return;
            
        }
    }
    
    extractLauncherResource(status, hFileRead, rest, bufferSize, outputdir, &(props->testJVMFile), "testJVM file");
    if((*status)!=ERROR_OK) {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "Error extracting testJVM file!", 1);
        return ;
    }
    
    readLauncherResourceList(status, hFileRead, rest, bufferSize, outputdir, &(props->jars), "bundled and external files");
    if((*status)!=ERROR_OK) return;
    
    return;
}
