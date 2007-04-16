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
#ifndef _Main_H
#define	_Main_H

#include "Errors.h"

#ifdef	__cplusplus
extern "C" {
#endif
    
void addProgressPosition(LauncherProperties *props,DWORD add);
void setProgressRange(LauncherProperties *props, int64t * size);
void setErrorDetailString(LauncherProperties *props,const WCHAR * message);
void setErrorTitleString(LauncherProperties *props,const WCHAR * message);
void setButtonString(LauncherProperties *props,const WCHAR * message);
void setProgressTitleString(LauncherProperties *props,const WCHAR * message);

void showLauncherWindows(LauncherProperties *props);
void closeLauncherWindows(LauncherProperties *props);
void hideLauncherWindows(LauncherProperties *props);

void hide(LauncherProperties *props,HWND hwnd);
void show(LauncherProperties *props,HWND hwnd);

void showErrorW(LauncherProperties *props, const char * error, const DWORD varArgsNumber, ...);

void showMessageW(LauncherProperties *props,const WCHAR* message, const DWORD number, ...);
void showMessageA(LauncherProperties *props,const char * message, const DWORD varArgsNumber, ...);

#ifdef	__cplusplus
}
#endif

#endif	/* _Main_H */

