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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * DebugProtocol.java
 * Created on October 10, 2003, 2:06 PM
 */

package com.sun.errorhandler;

/**
 * @author  Winston Prakash
 */
import java.net.*;
import java.io.*;
import java.util.StringTokenizer;

/** Agreed up on protocol to communicate with the server */

public class DebugProtocol {
  
    public static String DEBUG_CLIENT_ID = "DEBUG_CLIENT_ID";  // NOI18N
    public static String DEBUG_CLIENT_NAME = "Creator Debug Client";  // NOI18N
    public static String DEBUG_REQUEST_START = "DEBUG_REQUEST_START";  // NOI18N
    public static String DEBUG_REQUEST_END = "DEBUG_REQUEST_END";  // NOI18N
    public static String DEBUG_CLASS_NAME = "ClassName";  // NOI18N
    public static String DEBUG_FILE_NAME = "FileName";  // NOI18N
    public static String DEBUG_METHOD_NAME = "MethodName";  // NOI18N
    public static String DEBUG_LINE_NUMBER = "LineNumber";  // NOI18N
    public static String DEBUG_DELIMITER = ":";  // NOI18N
}
