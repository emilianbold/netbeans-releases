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
#include <wchar.h>
#include <stdio.h>
#include <stdlib.h>
#include <shellapi.h>
#include "FileUtils.h"
#include "StringUtils.h"
#include "JavaUtils.h"
#include "RegistryUtils.h"
#include "Launcher.h"
#include "ProcessUtils.h"
#include "StringUtils.h"
#include "ExtractUtils.h"
#include "Main.h"

const DWORD NUMBER_OF_HELP_ARGUMENTS = 9;

const WCHAR * outputFileArg       = L"--output";
const WCHAR * javaArg             = L"--javahome";
const WCHAR * debugArg            = L"--debug";
const WCHAR * tempdirArg          = L"--tempdir";
const WCHAR * classPathPrepend    = L"--classpath-prepend";
const WCHAR * classPathAppend     = L"--classpath-append";
const WCHAR * extractArg          = L"--extract";
const WCHAR * helpArg             = L"--help";
const WCHAR * silentArg           = L"--silent";
const WCHAR * nospaceCheckArg     = L"--nospacecheck";

const WCHAR * javaParameterPrefix = L"-J";

const WCHAR * NEW_LINE            = L"\n";

const WCHAR * CLASSPATH_SEPARATOR = L";";
const WCHAR * CLASS_SUFFIX = L".class";

DWORD I18N_PROPERTIES_NUMBER;

DWORD silentMode = 0;

DWORD getArgumentIndex(WCHAR **cmd, int argumentsNumber, const WCHAR *arg, DWORD removeArgument) {
    DWORD i=0;
    for(i=0;i<argumentsNumber;i++) {
        if(cmd[i]!=NULL) { // argument has not been cleaned yet
            if(wcscmp(arg, cmd[i])==0) { //argument is the same as the desired
                if(removeArgument) FREE(cmd[i]); // free it .. we don`t need it anymore
                return i;
            }
        }
    }
    return argumentsNumber;
}

WCHAR * getArgumentValue(WCHAR **cmd, int argumentsNumber, const WCHAR *arg, DWORD removeArgument) {
    
    WCHAR * result = NULL;
    DWORD i = getArgumentIndex(cmd, argumentsNumber, arg, removeArgument);
    if((i+1) < argumentsNumber) { //we have at least one more argument
        result = appendStringW(NULL, cmd[i+1]);
        if(removeArgument) FREE(cmd[i+1]);
    }
    
    return result;
}


void setOutputFile(WCHAR *path) {
    HANDLE out = INVALID_HANDLE_VALUE ;
    
    out = CreateFileW(path, GENERIC_WRITE | GENERIC_READ, FILE_SHARE_READ | FILE_SHARE_WRITE, 0, CREATE_ALWAYS, 0, 0);
    if(out!=INVALID_HANDLE_VALUE) {
        SetStdHandle(STD_OUTPUT_HANDLE, out);
        SetStdHandle(STD_ERROR_HANDLE, out);
        setStdoutHandle(out);
        setStderrHandle(out);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "[CMD Argument] Redirect output to file : ", 0);
        writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), path, 1);
    } else  {
        writeErrorA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "[CMD Argument] Can`t create file: ", path, GetLastError());
    }
}

void setOutput(WCHAR **cmd, int argumentsNumber) {
    if(getArgumentIndex(cmd, argumentsNumber, debugArg, 1) < argumentsNumber) {
        outputLevel = OUTPUT_LEVEL_DEBUG;
    }
    
    WCHAR * file = getArgumentValue(cmd, argumentsNumber, outputFileArg, 1);
    if(file!=NULL) {
        DWORD exists = fileExists(file);
        if((exists && !isDirectory(file) )|| !exists) {
            setOutputFile(file);
        }
        FREE(file);
    }
    writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(),
    (outputLevel == OUTPUT_LEVEL_DEBUG) ?
    "[CMD Argument] Using debug output." :
        "Using normal output." , 1);
        
}

void setFreeSpaceChecking(WCHAR ** commandLine, int argumentsNumber) {
    if(getArgumentIndex(commandLine, argumentsNumber, nospaceCheckArg, 1) < argumentsNumber) {
        checkForFreeSpace = 0;
    } else {
        checkForFreeSpace = 1;
    }
}




void loadLocalizationStrings(DWORD * status, HANDLE hFileRead, DWORD bufsize, SizedString * restOfBytes) {
    
    // load localized messages
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "Loading I18N Strings.", 1);
    *status = loadI18NStrings(hFileRead, restOfBytes, bufsize);
    
    if((*status)!=ERROR_OK) {
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStderrHandle(), "Error! Can`t load i18n strings!!", 1);
        WCHAR * exe = getExeName();
        showMessageW(1, getI18nProperty(INTEGRITY_ERROR_PROP), exe);
        FREE(exe);
    }
}

void createTMPDir(DWORD * status, LauncherProperties * props,  WCHAR ** cmd, int argumentsNumber) {
    WCHAR * tmpDirectory = NULL;
    DWORD createRndSubDir = 1;
    
    WCHAR * argTempDir = getArgumentValue(cmd, argumentsNumber, extractArg, 1);
    
    if(argTempDir!=NULL) {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "[CMD Argument] Extract data to directory: ", 0);
        writeMessageW(OUTPUT_LEVEL_DEBUG, getStderrHandle(), argTempDir, 2);
        props->extractOnly = 1;
        createRndSubDir = 0;
    } else {
        // check if have --tempdir argument specified
        argTempDir = getArgumentValue(cmd, argumentsNumber, tempdirArg, 1);
        if(argTempDir!=NULL) {
            writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "[CMD Argument] Using tmp directory: ", 0);
            writeMessageW(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), argTempDir, 2);
        }
        
    }
    
    createTempDirectory(status, argTempDir, &tmpDirectory, createRndSubDir);
    if((*status)!=ERROR_OK) {
        showMessageW(1, getI18nProperty(CANT_CREATE_TEMP_DIR_PROP), tmpDirectory);
    }
    FREE(argTempDir);
    props->tmpDir = tmpDirectory;
}

void extract(DWORD * status, LauncherProperties *props, HANDLE hFileRead, DWORD bufsize, SizedString * restOfBytes) {
    if((*status)!=ERROR_OK) return;
    writeMessageA(OUTPUT_LEVEL_NORMAL, getStderrHandle(), "Starting extracting data", 1);
    extractData(status, hFileRead, restOfBytes, bufsize, props);
    
    if((*status) == ERROR_FREESPACE) {
        showMessageW(2, getI18nProperty(NOT_ENOUGH_FREE_SPACE_PROP), props->tmpDir, tempdirArg);
    }
    else if((*status) == ERROR_INTEGRITY) {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "Error! Can`t extract data from bundle. Seems to be integrirty error!", 1);
        showMessageW(1, getI18nProperty(INTEGRITY_ERROR_PROP), props->exePath);
    }
    writeMessageA(OUTPUT_LEVEL_NORMAL, getStderrHandle(), "... extracting data finished", 1);
    return;
}


void trySetCompatibleJava(DWORD * status, WCHAR * location, LauncherProperties * props) {
    if(location!=NULL) {
        JavaProperties * javaProps = NULL;
        *status = getJavaProperties(location, props, &javaProps);
        
        if(*status==ERROR_OK) {
            writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "... some java at ", 0);
            writeMessageW(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), location, 1);
            // some java there, check compatibility
            writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "... checking compatibility of java : ", 0);
            writeMessageW(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), javaProps->javaHome, 1);
            if(isJavaCompatible(javaProps, props->compatibleJava, props->compatibleJavaNumber)) {
                writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "... compatible", 1);
                props->java = javaProps;
            } else {
                * status = ERROR_JVM_UNCOMPATIBLE;
                writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "... uncompatible", 1);
                freeJavaProperties(&javaProps);
            }
        } else {
            writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "... no java at ", 0);
            writeMessageW(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), location, 1);
            if (*status==ERROR_INPUTOUPUT) {
                *status = ERROR_JVM_NOT_FOUND;
            }
        }
        
        if( *status != ERROR_OK) { // check private JRE
            DWORD privateJreStatus = *status;
            WCHAR * privateJre = appendStringW(NULL, location);
            privateJre = appendStringW(privateJre, L"\\jre");
            writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "... check private jre at ", 0);
            writeMessageW(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), privateJre, 1);
            
            if(getJavaProperties(privateJre, props, &javaProps)==ERROR_OK) {
                writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "... checking compatibility of private jre : ", 0);
                writeMessageW(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), javaProps->javaHome, 1);
                if(isJavaCompatible(javaProps, props->compatibleJava, props->compatibleJavaNumber)) {
                    props->java = javaProps;
                    * status = ERROR_OK;
                } else {
                    freeJavaProperties(&javaProps);
                }
            }
            FREE(privateJre);
        }
    } else {
        * status = ERROR_JVM_NOT_FOUND;
    }
}

void resolveTestJVM(LauncherProperties * props) {
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "Resolving testJVM classpath...", 1);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... first step : ", 0);
    writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), props->testJVMFile->path, 1);
    resolvePath(props, props->testJVMFile);
    WCHAR * testJVMFile = props->testJVMFile->resolved;
    
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... second     : ", 0);
    writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), props->testJVMFile->resolved, 1);
    
    WCHAR * testJVMClassPath = NULL;
    if(isDirectory(testJVMFile)) { // the directory of the class file is set
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... testJVM is : directory ", 1);
        testJVMClassPath = appendStringW(NULL, testJVMFile);
    } else { // testJVMFile is either .class file or .jar/.zip file with the neccessary class file
        WCHAR * dir = getParentDirectory(testJVMFile);
        WCHAR * ptr = testJVMFile;
        do {
            ptr = wcsstr(ptr, CLASS_SUFFIX); // check if ptr contains .class
            if(ptr==NULL) { // .jar or .zip file
                writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... testJVM is : ZIP/JAR file", 1);
                testJVMClassPath = appendStringW(NULL, testJVMFile);
                break;
            }
            ptr += getLengthW(CLASS_SUFFIX); // shift to the right after the ".class"
            
            if(ptr==NULL || getLengthW(ptr)==0) { // .class was at the end of the ptr
                writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... testJVM is : .class file ", 1);
                testJVMClassPath = appendStringW(NULL, dir);
                break;
            }
        } while(1);
        FREE(dir);
    }
    
    FREE(props->testJVMFile->resolved);
    props->testJVMFile->resolved = testJVMClassPath;
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... resolved   : ", 0);
    writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), props->testJVMFile->resolved, 1);
}

void findSuitableJava(DWORD * status, LauncherProperties * props, WCHAR ** cmd, int argumentsNumber) {
    if((*status)!=ERROR_OK) return;
    
    //resolve testJVM file
    resolveTestJVM(props);
    
    if(!fileExists(props->testJVMFile->resolved)) {
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStderrHandle(), "Can`t find TestJVM classpath : ", 0);
        writeMessageW(OUTPUT_LEVEL_NORMAL, getStderrHandle(), props->testJVMFile->resolved, 1);
        showMessageW(1, getI18nProperty(JVM_NOT_FOUND_PROP), javaArg);
        * status = ERROR_JVM_NOT_FOUND;
        return;
    } else {
        
        // try to get java location from command line arguments
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "", 1);
        writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "Finding JAVA...", 1);
        
        WCHAR * java = NULL;
        WCHAR * javaHome = getArgumentValue(cmd, argumentsNumber, javaArg, 1);
        
        if(javaHome!=NULL) { // using user-defined JVM via command-line parameter
            writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "[CMD Argument] Try to use java from ", 0);
            writeMessageW(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), javaHome, 1);
            
            trySetCompatibleJava(status, javaHome, props);
            if( *status == ERROR_JVM_NOT_FOUND || *status == ERROR_JVM_UNCOMPATIBLE) {
                showMessageW(1, getI18nProperty((*status == ERROR_JVM_NOT_FOUND) ? JVM_USER_DEFINED_ERROR_PROP : JVM_UNSUPPORTED_VERSION_PROP), javaHome);
            }
            FREE(javaHome);
        } else { // no user-specified java argument
            findSystemJava(props);
            if( props->java ==NULL) {
                showMessageW(1, getI18nProperty(JVM_NOT_FOUND_PROP), javaArg);
                * status = ERROR_JVM_NOT_FOUND;
            }
        }
        
        if(props->java!=NULL) {
            writeMessageA(OUTPUT_LEVEL_NORMAL, getStderrHandle(), "Compatible jvm is found on the system", 1);
            printJavaProperties(props->java);
        } else {
            writeMessageA(OUTPUT_LEVEL_NORMAL, getStderrHandle(), "No compatible jvm was found on the system", 1);
        }
    }
    return;
}

void resolvePath(LauncherProperties * props, LauncherResource * file) {
    if(file==NULL) return;
    if(file->resolved!=NULL) return;
    
    WCHAR * result = NULL;
    switch (file->type) {
        case 2:
            if(props->java!=NULL) {
                result = appendStringW(NULL, props->java->javaHome); // relative to javahome
            }
            break;
        case 3:
            // relative to user home
            result = getCurrentUserHome();
            break;
        case 4:
            result = appendStringW(NULL, props->exeDir); // launcher parent
            break;
        case 5:
            result = appendStringW(NULL, props->tmpDir); // launcher tmpdir
            break;
        case 0: // absolute path, nothing to add
        case 1: // bundled file with full path path, nothing to add
        default:
            break; // the same as absolute, nothing to add
    }
    if(result!=NULL) {
        result = appendStringW(result, L"\\");
    }
    file->resolved = appendStringW(result, file->path);
    DWORD i=0;
    for(i=0;i<getLengthW(file->resolved);i++) {
        if(file->resolved[i]==L'/') {
            file->resolved[i]=L'\\';
        }
    }
}

void setClasspathElements(DWORD * status, LauncherProperties * props, WCHAR ** cmd, int argumentsNumber) {
    if((*status)!=ERROR_OK) return;
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "Modifying classpath ...", 1);
    WCHAR * preCP = NULL;
    WCHAR * appCP = NULL;
    // add some libraries to the beginning of the classpath
    while((preCP = getArgumentValue(cmd, argumentsNumber, classPathPrepend, 1))!=NULL) {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... adding entry to the beginning of classpath : ", 0);
        writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), preCP, 1);
        if (props->classpath != NULL) {
            preCP = appendStringW(preCP, CLASSPATH_SEPARATOR);
        }
        WCHAR *last = props->classpath;
        WCHAR *tmp = appendStringW(preCP, props->classpath);
        FREE(props->classpath);
        props->classpath = tmp;
    }
    DWORD i = 0 ;
    for(i=0;i<props->jars->size;i++) {
        props->classpath = appendStringW(props->classpath, CLASSPATH_SEPARATOR);
        resolvePath(props, props->jars->items[i]);
        props->classpath = appendStringW(props->classpath, props->jars->items[i]->resolved);
    }
    
    // add some libraries to the end of the classpath
    while((appCP = getArgumentValue(cmd, argumentsNumber, classPathAppend, 1))!=NULL) {
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... adding entry to the end of classpath : ", 0);
        writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), appCP, 1);
        if (props->classpath != NULL) {
            props->classpath = appendStringW(props->classpath, CLASSPATH_SEPARATOR);
        }
        props->classpath = appendStringW(props->classpath, appCP);
        FREE(appCP);
    }
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... finished", 1);
}

void setAdditionalArguments(DWORD * status, LauncherProperties * props, WCHAR ** cmd, int argumentsNumber) {
    if((*status)!=ERROR_OK) return;
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(),
    "Parsing rest of command line arguments to add them to java or application parameters... ", 1);
    WCHAR ** javaArgs;
    WCHAR ** appArgs;
    DWORD i=0;
    DWORD jArg = 0; // java arguments number
    DWORD aArg = 0; // app arguments number
    
    // get number for array creation
    for(i=0;i<argumentsNumber;i++) {
        if(cmd[i]!=NULL) {
            if(wcsstr(cmd[i], javaParameterPrefix)!=NULL) {
                jArg++;
            } else {
                aArg++;
            }
        }
    }
    //fill the array
    if(jArg>0) {
        javaArgs = newppWCHAR(jArg + props->jvmArguments->size);
        DWORD j=0;
        for (i=0;i<props->jvmArguments->size;i++) {
            javaArgs[i] = props->jvmArguments->items[i];
        }
        FREE(props->jvmArguments->items);
    } else {
        javaArgs = NULL;
    }
    
    if(aArg>0) {
        appArgs = newppWCHAR(aArg + props->appArguments->size);
        for (i=0; i < props->appArguments->size; i++) {
            appArgs [i]= props->appArguments->items[i];
        }
        FREE(props->appArguments->items);
    } else {
        appArgs = NULL;
    }
    jArg = aArg = 0;
    
    for(i=0;i<argumentsNumber;i++) {
        if(cmd[i]!=NULL) {
            if(wcsstr(cmd[i], javaParameterPrefix)!=NULL) {
                javaArgs [ props->jvmArguments->size + jArg] = appendStringW(NULL, cmd[i] + getLengthW(javaParameterPrefix));
                writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... adding JVM argument : ", 0);
                writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), javaArgs [ props->jvmArguments->size + jArg], 1);
                jArg ++ ;
            } else {
                appArgs  [ props->appArguments->size + aArg] = appendStringW(NULL, cmd[i]);
                writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... adding APP argument : ", 0);
                writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), appArgs  [ props->appArguments->size + aArg], 1);
                aArg++;
            }
            FREE(cmd[i]);
        }
    }
    props->appArguments->size  = props->appArguments->size + aArg;
    props->jvmArguments->size  = props->jvmArguments->size + jArg;
    if(props->jvmArguments->items==NULL) props->jvmArguments->items = javaArgs;
    if(props->appArguments->items==NULL) props->appArguments->items = appArgs;
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... finished parsing parameters", 1);
}
void appendCommandLineArgument( WCHAR ** command, const WCHAR * arg) {
    if(wcsstr(arg, L" ")) {
        *command = appendStringW(*command, L"\"");
        *command = appendStringW(*command, arg);
        *command = appendStringW(*command, L"\"");
    } else {
        *command = appendStringW(*command, arg);
    }
    *command = appendStringW(*command, L" ");
}

void setLauncherCommand(DWORD * status, LauncherProperties *props) {
    if((*status)!=ERROR_OK) return;
    
    if(props->java==NULL) {
        *status = ERROR_JVM_NOT_FOUND;
        return;
    }
    
    WCHAR * command = NULL;
    appendCommandLineArgument(&command, props->java->javaExe);
    command = appendStringW(command, L"-Djava.io.tmpdir=");
    WCHAR * javaIOTmpdir = getParentDirectory(props->tmpDir);
    appendCommandLineArgument(&command, javaIOTmpdir);
    FREE(javaIOTmpdir);
    
    DWORD i=0;
    for(i=0;i<props->jvmArguments->size;i++) {
        appendCommandLineArgument(&command, props->jvmArguments->items[i]);
    }
    
    appendCommandLineArgument(&command, L"-classpath");
    appendCommandLineArgument(&command, props->classpath);
    appendCommandLineArgument(&command, props->mainClass);
    
    for(i=0;i<props->appArguments->size; i++) {
        appendCommandLineArgument(&command, props->appArguments->items[i]);
    }
    props->command = command;
}

DWORD executeMainClass(DWORD * status, LauncherProperties * props) {
    if((*status) != ERROR_OK) return MAXDWORD;
    
    DWORD exitCode = 0;
    writeMessageA(OUTPUT_LEVEL_NORMAL, getStdoutHandle(), "Executing main class", 1);
    if(checkFreeSpace(props->tmpDir, 0)) {
        
        HANDLE hErrorRead;
        HANDLE hErrorWrite;
        CreatePipe(&hErrorRead, &hErrorWrite, NULL, 0);
        
        hideLauncherWindows();
        exitCode = executeCommand(props->command, NULL, INFINITE, getStdoutHandle(), hErrorWrite, NORMAL_PRIORITY_CLASS);
        
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... main class has finished his work. Exit code is ", 0);
        char * s = DWORDtoCHAR(exitCode);
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), s, 1);
        FREE(s);
        char * error = readHandle(hErrorRead);
        if(getLengthA(error)>1) {            
            WCHAR * errorW = toWCHAR(error);            
            showMessageW(1, getI18nProperty(JAVA_PROCESS_ERROR_PROP), errorW);
            FREE(errorW);
        }
        CloseHandle(hErrorWrite);
        CloseHandle(hErrorRead);
        FREE(error);
        Sleep(1);
    } else {
        *status = ERROR_FREESPACE;
        writeMessageA(OUTPUT_LEVEL_DEBUG, getStderrHandle(), "... there is not enough space in tmp dir to execute main jar", 1);
        exitCode = MAXDWORD;
    }
    
    return exitCode;
}

DWORD isOnlyHelp(WCHAR ** cmd, int argumentsNumber) {
    if(getArgumentIndex(cmd, argumentsNumber, helpArg, 1) < argumentsNumber) {        
        
        WCHARList * help = newWCHARList(NUMBER_OF_HELP_ARGUMENTS);
        
        int counter = 0;        
        help->items[counter++] = formatMessageW(1, getI18nProperty(ARG_JAVA_PROP),javaArg);
        help->items[counter++] = formatMessageW(1, getI18nProperty(ARG_TMP_PROP),tempdirArg);
        help->items[counter++] = formatMessageW(1, getI18nProperty(ARG_EXTRACT_PROP),extractArg);        
        help->items[counter++] = formatMessageW(1, getI18nProperty(ARG_OUTPUT_PROPERTY), outputFileArg);
        help->items[counter++] = formatMessageW(1, getI18nProperty(ARG_DEBUG_PROP),debugArg);
        help->items[counter++] = formatMessageW(1, getI18nProperty(ARG_CPA_PROP),classPathAppend);
        help->items[counter++] = formatMessageW(1, getI18nProperty(ARG_CPP_PROP),classPathPrepend);
        help->items[counter++] = formatMessageW(1, getI18nProperty(ARG_DISABLE_SPACE_CHECK), nospaceCheckArg);
        help->items[counter++] = formatMessageW(1, getI18nProperty(ARG_HELP_PROP),helpArg);
        
        WCHAR * helpString = NULL;
        for(counter=0;counter<NUMBER_OF_HELP_ARGUMENTS;counter++) {
            helpString = appendStringW(appendStringW(helpString, help->items[counter]),NEW_LINE);
        }
        freeWCHARList(&help);        
        showMessageW(0, helpString);
        FREE(helpString);        
        return 1;
    }
    return 0;
}

void setRunningMode(WCHAR ** commandLine, int argumentsNumber) {
    if(getArgumentIndex(commandLine, argumentsNumber, silentArg, 0) < argumentsNumber) {
        silentMode = 1;
    } else {
        silentMode = 0;
    }
}

DWORD isSilent() {
    return silentMode;
}

LauncherProperties * createLauncherProperties() {
    LauncherProperties *props = (LauncherProperties*)malloc(sizeof(LauncherProperties));
    props->jvmArguments=NULL;
    props->appArguments=NULL;
    props->extractOnly = 0;
    props->mainClass = NULL;
    props->testJVMClass = NULL;
    props->classpath = NULL;
    props->jars=NULL;
    props->testJVMFile=NULL;
    props->tmpDir = NULL;
    props->compatibleJava=NULL;
    props->compatibleJavaNumber=0;
    props->java=NULL;
    props->command=NULL;
    props->jvms=NULL;
    props->exePath = getExeName();
    props->exeDir  = getExeDirectory();
    return props;
}
freeLauncherResourceList(LauncherResourceList ** list) {
    if(*list!=NULL) {
        if((*list)->items!=NULL) {
            DWORD i=0;
            for(i=0;i<(*list)->size;i++) {
                freeLauncherResource(&((*list)->items[i]));
            }
            FREE((*list)->items);
        }
        FREE((*list));
    }
}


void freeLauncherProperties(LauncherProperties **props) {
    
    if((*props)!=NULL) {
        DWORD i=0;
        
        freeWCHARList(& ( (*props)->appArguments));
        freeWCHARList(& ( (*props)->jvmArguments));
        
        FREE((*props)->mainClass);
        FREE((*props)->testJVMClass);
        FREE((*props)->classpath);
        freeLauncherResourceList(&((*props)->jars));
        
        freeLauncherResourceList(&((*props)->jvms));
        
        freeLauncherResource(&((*props)->testJVMFile));
        
        FREE((*props)->tmpDir);
        for(i=0;i<(*props)->compatibleJavaNumber;i++) {
            JavaCompatible * jc = (*props)->compatibleJava[i];
            if(jc!=NULL) {
                FREE(jc->minVersion);
                FREE(jc->maxVersion);
                FREE(jc->vendor);
                FREE(jc->osName);
                FREE(jc->osArch);
                FREE((*props)->compatibleJava[i]);
            }
        }
        FREE((*props)->compatibleJava);
        freeJavaProperties(&((*props)->java));
        
        FREE((*props)->command);
        FREE((*props)->exePath);
        FREE((*props)->exeDir);
        FREE((*props));
    }
    return;
}

DWORD processLauncher(WCHAR ** commandLine, int argumentsNumber) {
    DWORD exitCode = 0;
    I18N_PROPERTIES_NUMBER = 0;
    
    setOutput(commandLine, argumentsNumber);
    setFreeSpaceChecking(commandLine, argumentsNumber);
    // size of buffer for reading data from exe file
    DWORD bufsize = 65536;
    DWORD status = ERROR_OK;
    
    HANDLE hFileRead = getLauncherHandler(&status);
    
    if(status != ERROR_OK) {
        WCHAR * err = getErrorDescription(GetLastError());
        showMessageW(1, L"%s", err);
        FREE(err);
    }
    DWORD size = getRunningFileSize();
    DWORD findJavaSize = size / 4;
    setProgressRange(size + findJavaSize);
    
    if(status == ERROR_OK) {
        SizedString * restOfBytes = createSizedString();
        //skip laucher stub
        skipStub(&status, hFileRead, bufsize);
        if(status == ERROR_OK) {
            loadLocalizationStrings(&status, hFileRead, bufsize, restOfBytes);
            if(status == ERROR_OK) {
                if(!isOnlyHelp(commandLine, argumentsNumber)) {
                    setTitleString(getI18nProperty(MSG_TITLE));
                    showLauncherWindows();
                    // create temp direcotry NBIXXXXX at <TEMP> direcotry
                    LauncherProperties * props = createLauncherProperties();
                    setDetailString(getI18nProperty(MSG_CREATE_TMPDIR));
                    createTMPDir(&status, props, commandLine, argumentsNumber);
                    
                    if(status == ERROR_OK) {
                        setDetailString(getI18nProperty(MSG_EXTRACT_DATA));
                        //extract data and load launcher properties
                        extract(&status, props, hFileRead, bufsize, restOfBytes);
                        
                        if(!props->extractOnly) {
                            if(status == ERROR_OK) {
                                setDetailString(getI18nProperty(MSG_JVM_SEARCH));
                                findSuitableJava(&status, props, commandLine, argumentsNumber);
                                addProgressPosition(findJavaSize);
                                if(props->java!=NULL) {
                                    setDetailString(getI18nProperty(MSG_SET_OPTIONS));
                                    setClasspathElements(&status, props, commandLine, argumentsNumber);
                                    
                                    setAdditionalArguments(&status, props, commandLine, argumentsNumber);
                                    
                                    setLauncherCommand(& status, props);
                                    
                                    setDetailString(getI18nProperty(MSG_RUNNING));
                                    Sleep(500);
                                    exitCode = executeMainClass(& status, props);
                                }
                            }
                            writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... deleting temporary directory ", 1);
                            deleteDirectory(props->tmpDir);
                        } else {
                            writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... exraction finished. ", 1);
                        }
                    }
                    
                    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... free launcher properties", 1);
                    freeLauncherProperties(&props);
                }
                writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... free i18n strings", 1);
                freeI18NMessages();
            }
        }
        freeSizedString(&restOfBytes);
    }
    LocalFree(commandLine);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "... closing file and std handles", 1);
    CloseHandle(hFileRead);
    closeStdHandles();
    
    return exitCode;
}
