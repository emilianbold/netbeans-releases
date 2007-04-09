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

#ifndef _Main_H
#define	_Main_H
#ifdef	__cplusplus
extern "C" {
#endif

#define EXIT_CODE_INITIALIZATION_ERROR (1023)
    
void addProgressPosition(DWORD add);
void setProgressRange(double size);
void setDetailString(WCHAR * message);
void setTitleString(WCHAR * message);

void showLauncherWindows();
void closeLauncherWindows();
void hideLauncherWindows();

void showMessageW(const DWORD number, const WCHAR* message,...);
void showMessageA(const DWORD varArgsNumber, const char * message, ...);

#ifdef	__cplusplus
}
#endif

#endif	/* _Main_H */

