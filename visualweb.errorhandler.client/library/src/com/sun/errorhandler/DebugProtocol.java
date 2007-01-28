/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
