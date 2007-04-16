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
#ifndef _Errors_H
#define	_Errors_H

#ifdef	__cplusplus
extern "C" {
#endif

    
#define ERROR_OK                            0
#define ERROR_INTEGRITY                     1000
#define ERROR_FREESPACE                     1001
#define ERROR_INPUTOUPUT                    1002
#define ERROR_JVM_UNCOMPATIBLE              1003
#define ERROR_JVM_NOT_FOUND                 1004
#define ERROR_ON_EXECUTE_PROCESS            1005
#define ERROR_PROCESS_TIMEOUT               1006
#define ERROR_USER_TERMINATED               1007
    
#define EXIT_CODE_EVENTS_INITIALIZATION_ERROR 1022
#define EXIT_CODE_GUI_INITIALIZATION_ERROR  1023
#define EXIT_CODE_STUB                      1024
#define EXIT_CODE_SYSTEM_ERROR              1025


#ifdef	__cplusplus
}
#endif

#endif	/* _Errors_H */

