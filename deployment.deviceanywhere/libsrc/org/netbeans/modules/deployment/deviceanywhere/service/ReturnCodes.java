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
 */

package org.netbeans.modules.deployment.deviceanywhere.service;

public class ReturnCodes {
    
    // for all services
    public static final int SUCCESS = 0;
    public static final int INTERNAL_ERROR = 1;
    public static final int LOGIN_FAILED = 2;
    
    // for uploadApplication
    public static final int INVALID_APPLICATION_NAME = 3;
    public static final int JAD_FILE_PARSE_ERROR = 4;
    
    // for startDownloadScript
    public static final int DEVICE_NOT_FOUND = 5;
    public static final int APPLICATION_NOT_FOUND = 6;
    
}
