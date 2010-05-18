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
package org.netbeans.modules.wsdlextensions.file.model;


/**
 * FileConstants
 */
public class FileConstants {
            
    // Use Types
    public static final String LITERAL = "literal";
    public static final String ENCODED = "encoded";    
    
    // Boolean values
    public static final String BOOLEAN_FALSE = "false";
    public static final String BOOLEAN_TRUE  = "true";    
    
    public static final String USER_HOME = "User Home";
    public static final String CURRENT_WORKING_DIR = "Current Working Dir";
    public static final String DEFAULT_SYSTEM_TEMP_DIR = "Default System Temp Dir";
    public static final String PICKED_UP_DIR = "Picked up Dir";
    
    public static final String NOT_SET = "<Not Set>";
    
    public static final String TEXT = "text";
    public static final String BINARY = "binary";    
    public static final String XML = "xml";
    public static final String ENCODED_DATA = "encoded data";

    public static int READ = 0;
    public static int WRITE = 1;
    public static int READ_WRITE = 2;
    public static int SOLICITED_READ = 3;

    public static final String READ_STR = "Read";
    public static final String WRITE_STR = "Write";
    public static final String READ_WRITE_STR = "Read/Write";

    public static final String TEMP_DIR = "Temp Directory";
    public static final String USER_DIR = "User Directory";
    public static final String POLLING_DIR = "Polling Directory";

    public static final String DIRECTION = "Direction";
    
    public static final String DELIM_LINE_FEED = "LINE FEED";
    
    public static final int XML_MESSAGE_TYPE = 0;
    public static final int TEXT_MESSAGE_TYPE = 1;
    public static final int ENCODED_MESSAGE_TYPE = 2;
    public static final int BINARY_MESSAGE_TYPE = 3;

    public static final String VERB_POLL = "poll";
    public static final String VERB_WRITE = "write";
    public static final String VERB_READ = "read";
    
    public static final String BASE64_BINARY = "base64Binary";    
    public static final String XSD_BASE64_BINARY = "xsd:base64Binary";
    public static final String XSD_STRING = "xsd:string";  
    
    public static final String CHARSET_DEFAULT = "<default>";
    
    public static boolean stringValueIsTrue (String val) {        
        if (val == null || val.equals(BOOLEAN_FALSE) ) {
            return false;
        } else {
            return true;
        }
    }    
}
