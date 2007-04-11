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
#include <stdio.h>
#include <stdlib.h>
#include <commctrl.h>
#include "Launcher.h"
#include "FileUtils.h"
#include "StringUtils.h"
#include "SystemUtils.h"
#include "ExtractUtils.h"
#include "Main.h"


#define HAVE_WCHAR_H 1


TCHAR mainWindowTitle[80] = TEXT("NetBeans installer");
TCHAR mainClassName[80] = TEXT("Main NBI Window Class");

HWND hwndPB = NULL;
HWND hwndMain = NULL;
HWND hwndDetail = NULL;
HWND hwndTitle = NULL;
HINSTANCE globalInstance;
double totalProgressSize = 0;
double currentProgressSize = 0;
double steps = 1000;
int iCmdShowGlobal = 0;
HANDLE initializationSuccess, initializationFailed;

LRESULT CALLBACK WndProc(HWND hwnd, UINT umsg, WPARAM wParam, LPARAM lParam) {
    switch (umsg) {
        case WM_CLOSE:
            DestroyWindow(hwnd);
            return 0;
            
        case WM_DESTROY:
            PostQuitMessage(0);
            return 0;
            
            
    }
    
    return DefWindowProc(hwnd, umsg, wParam, lParam);
}

BOOL InitInstance(HINSTANCE hInstance, int iCmdShow, HWND * MainWindowHandle) {
    if(isSilent()) return TRUE;
    
    int systemWidth = GetSystemMetrics(SM_CXSCREEN);
    int systemHeight = GetSystemMetrics(SM_CYSCREEN);
    
    int w = 400;
    int h = 140;
    int x = (systemWidth - w)/2;
    int y = (systemHeight - h)/2;
    
    InitCommonControls();
    
    HWND hwndMain = CreateWindow( mainClassName,   mainWindowTitle, WS_OVERLAPPED,  x, y, w, h, NULL, NULL, hInstance, NULL);
    
    RECT rcClient;
    int cyVScroll;
    
    cyVScroll = GetSystemMetrics(SM_CYVSCROLL);
    GetClientRect(hwndMain, &rcClient);
    
    hwndTitle = CreateWindowExW(0,  WC_STATICW,  WC_STATICW, WS_CHILD | WS_VISIBLE,
    rcClient.left + 10,  15, rcClient.right - 20, 30,
    hwndMain, NULL, hInstance, NULL);
    SendMessage(hwndTitle, WM_SETFONT, (WPARAM)GetStockObject(DEFAULT_GUI_FONT), MAKELPARAM(TRUE, 0));
    setTitleString(NULL);
    
    iCmdShowGlobal = iCmdShow;
    
    hwndDetail = CreateWindowExW(0,  WC_STATICW,  WC_STATICW, WS_CHILD | WS_VISIBLE,
    rcClient.left + 10,  50, rcClient.right - 20, 30,
    hwndMain, NULL, hInstance, NULL);
    SendMessage(hwndDetail, WM_SETFONT, (WPARAM)GetStockObject(DEFAULT_GUI_FONT), MAKELPARAM(TRUE, 0));
    
    hwndPB = CreateWindowExW(0, PROGRESS_CLASSW, NULL, WS_CHILD | WS_VISIBLE | PBS_SMOOTH,
    rcClient.left + 10,  rcClient.bottom - cyVScroll - 10, rcClient.right - 20, cyVScroll,
    hwndMain, NULL, hInstance, NULL);
    totalProgressSize = STUB_FILL_SIZE;
    if (! hwndMain)  return FALSE;
    
    setDetailString(NULL);
    
    UpdateWindow(hwndMain);
    * MainWindowHandle = hwndMain;
    return TRUE;
}

void messageLoop(){
    MSG message;
    while(GetMessage(&message, NULL, 0, 0) > 0) {
        TranslateMessage(&message);
        DispatchMessage(&message);
    }
}

BOOL InitApplication(HINSTANCE hInstance) {
    if(isSilent()) return TRUE;
    
    WNDCLASS wndclass;
    
    wndclass.style = CS_HREDRAW | CS_VREDRAW;
    wndclass.lpfnWndProc = (WNDPROC)WndProc;
    wndclass.cbClsExtra = 0;
    wndclass.cbWndExtra = 0;
    wndclass.hIcon = NULL;
    wndclass.hInstance = hInstance;
    wndclass.hCursor = NULL;
    wndclass.hbrBackground = (HBRUSH)(COLOR_BTNFACE + 1);
    wndclass.lpszMenuName = NULL;
    wndclass.lpszClassName = mainClassName;
    return RegisterClass(&wndclass);
}



void addProgressPosition(DWORD add) {
    if(isSilent()) return;
    if ( add > 0 ) {
        currentProgressSize += (double) add;
        double pos = currentProgressSize / totalProgressSize;
        SendMessage(hwndPB, PBM_SETPOS, (long) pos, 0);
        UpdateWindow(hwndPB);
        UpdateWindow(hwndMain);
    }
}

void setProgressRange(double range) {
    if(isSilent()) return;
    totalProgressSize = range / steps;
    currentProgressSize = 0;
    SendMessage(hwndPB, PBM_SETRANGE, 0, MAKELPARAM(0, steps));
    SendMessage(hwndPB, PBM_SETSTEP, 1, 0);
    UpdateWindow(hwndPB);
    UpdateWindow(hwndMain);
}

void setTitleString(WCHAR * message) {
    if(isSilent()) return;
    SetWindowTextW(hwndTitle, message);
    UpdateWindow(hwndTitle);
    UpdateWindow(hwndMain);
}

void setDetailString(WCHAR * message) {
    if(isSilent()) return;
    SetWindowTextW(hwndDetail, message);
    UpdateWindow(hwndDetail);
    UpdateWindow(hwndMain);
}

void closeLauncherWindows() {
    if(isSilent()) return;
    
    if(hwndMain != NULL) {
        DestroyWindow(hwndPB);
        DestroyWindow(hwndDetail);
        DestroyWindow(hwndTitle);
        DestroyWindow(hwndMain);
        UnregisterClass(mainClassName, globalInstance);
        hwndMain  = NULL;
    }
}

void hideLauncherWindows() {
    if(isSilent()) return;
    
    if(hwndMain != NULL) {
        ShowWindow(hwndMain, HIDE_WINDOW);
    }
}


void showLauncherWindows() {
    ShowWindow(hwndMain, iCmdShowGlobal);
    SetForegroundWindow(hwndMain);
    UpdateWindow(hwndMain);
    
}

void showMessageW(const DWORD varArgsNumber, const WCHAR* message, ...) {
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "\n<ShowMessage>\n", 0);
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
    writeMessageW(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), result, 1);
    if(!isSilent()) {
        hideLauncherWindows();
        MessageBoxW(NULL, result, getI18nProperty(MSG_MESSAGEBOX_TITLE), MB_OK);
    }
}


void showMessageA(const DWORD varArgsNumber, const char* message, ...) {
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), "\n<ShowMessage>\n", 0);
    DWORD totalLength=getLengthA(message);
    va_list ap;
    va_start(ap, message);
    DWORD counter=0;
    while((counter++)<varArgsNumber) {
        char * arg = va_arg( ap, char * );
        totalLength+=getLengthA(arg);
    }
    va_end(ap);
    
    char * result = newpChar(totalLength + 1);
    va_start(ap, message);
    vsprintf(result, message, ap);
    va_end(ap);
    writeMessageA(OUTPUT_LEVEL_DEBUG, getStdoutHandle(), result, 1);
    char * prop = toChar(getI18nProperty(MSG_MESSAGEBOX_TITLE));
    if(!isSilent()) MessageBoxA(NULL, result, prop, MB_OK);
    FREE(prop);
}

/*
 WCHAR* GetStringFromStringTable( UINT uStringID ) {
 WCHAR   *pwchMem, *pwchCur;
 UINT      idRsrcBlk = uStringID / 16 + 1;
 int       strIndex  = uStringID % 16;
 HINSTANCE hModule = NULL;
 HRSRC     hResource = NULL;
 int i=0;
 LANGID lang = LANGIDFROMLCID(GetUserDefaultLCID());

 hResource = FindResourceExW( GetModuleHandleW(NULL), (LPWSTR)RT_STRING,
 MAKEINTRESOURCEW(idRsrcBlk), lang);

 if( hResource != NULL ) {
 pwchMem = (WCHAR *)LoadResource( hModule, hResource );

 if( pwchMem != NULL ) {
 pwchCur = pwchMem;
 for(i = 0; i<16; i++ ) {
 if( *pwchCur ) {
 int cchString = *pwchCur;  // String size in characters.
 pwchCur++;
 if( i == strIndex ) {
 WCHAR * tmp = appendStringNW(NULL, 0, pwchCur, cchString);
 return tmp;
 }
 pwchCur += cchString;
 }
 else
 pwchCur++;
 }
 }
 }
 return NULL;

 }*/

typedef struct _guiArguments {
    HINSTANCE hInstance;
    HINSTANCE hi;
    int nCmdShow;
} GuiArguments;


DWORD WINAPI guiThread(void * ptr)  {
    GuiArguments * guiArg = (GuiArguments * ) ptr;
    HINSTANCE hInstance = guiArg->hInstance;
    HINSTANCE hi = guiArg->hi;
    int nCmdShow = guiArg->nCmdShow;
    if (!hi && !InitApplication(hInstance)) {
        SetEvent(initializationFailed);
    } else if (!InitInstance(hInstance, nCmdShow, & hwndMain)) {
        SetEvent(initializationFailed);
    } else {
        SetEvent(initializationSuccess);
        messageLoop();
    }
}


DWORD createGui(HINSTANCE hInstance, HINSTANCE hi, int nCmdShow) {
    GuiArguments * guiArgs = (GuiArguments*) malloc(sizeof(GuiArguments));
    guiArgs->hInstance=hInstance;
    guiArgs->hi=hi;
    guiArgs->nCmdShow=nCmdShow;
    initializationSuccess = CreateEventW(NULL, TRUE, FALSE, L"Application Initialization Successfull");
    if(initializationSuccess==NULL) {
        return 0;
    }
    
    initializationFailed = CreateEventW(NULL, TRUE, FALSE, L"Application Initialization Failed");
    if(initializationFailed==NULL) {
        return 0;
    }
    
    DWORD threadId;
    CreateThread( NULL, 0, &guiThread, (LPVOID) guiArgs, 0, &threadId );
    HANDLE * events = (HANDLE *) malloc(sizeof(HANDLE)*2);
    events[0] = initializationSuccess;
    events[1] = initializationFailed;
    DWORD result = WaitForMultipleObjects(2, events, FALSE, INFINITE);
    FREE(events);
    return (result == WAIT_OBJECT_0);
}

WCHARList * getCommandlineArguments() {
    int argumentsNumber = 0;
    int i=0;
    
    WCHAR ** commandLine = CommandLineToArgvW(GetCommandLineW(), &argumentsNumber);
    
    // the first is always the running program..  we don`t need it
    // it is that same as GetModuleFileNameW says
    WCHARList * commandsList = newWCHARList((DWORD) (argumentsNumber - 1) );
    
    for(i=0;i<argumentsNumber - 1;i++) {
        
        commandsList->items[i] = appendStringW(NULL, commandLine[i + 1]);
    }
    
    LocalFree(commandLine);
    return commandsList;
}
int WINAPI WinMain( HINSTANCE hInstance, HINSTANCE hi, PSTR pszCmdLine, int nCmdShow) {
    DWORD exitCode = 1;
    DWORD status = ERROR_OK;
    globalInstance = hInstance;
    if(is9x()) {
        showMessageA(0, "Windows 9X platform is not supported");
        status = EXIT_CODE_SYSTEM_ERROR;
    } else {
        if(isOnlyLauncher()) {
            showMessageW(0, L"It is only a launcher stub!");
            status = EXIT_CODE_STUB;
        } else {
            setStdoutHandle(GetStdHandle(STD_OUTPUT_HANDLE));
            WCHARList * commandsList = getCommandlineArguments();
            setRunningMode(commandsList);
            if(!createGui(hInstance, hi, nCmdShow)) {
                status = EXIT_CODE_GUI_INITIALIZATION_ERROR;
            } else {
                exitCode = processLauncher(&status, commandsList);
                closeLauncherWindows();
            }
            freeWCHARList(&commandsList);
        }
    }
    return (status==ERROR_OK) ? exitCode : status;
}
