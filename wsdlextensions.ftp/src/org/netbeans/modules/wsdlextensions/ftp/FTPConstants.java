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
package org.netbeans.modules.wsdlextensions.ftp;


/**
 * FTPConstants
 */
public class FTPConstants {
    // Use Types
    public static final String LITERAL = "literal";
    public static final String ENCODED = "encoded";    
    
    public static int POLL_REQUEST = 0;
    public static int POLL_RESPONSE = 1;
    public static int PUT_REQUEST = 2;
    public static int PUT_RESPONSE = 3;

    // Boolean values
    public static final String BOOLEAN_FALSE = "false";
    public static final String BOOLEAN_TRUE  = "true";    
    
    public static final String NOT_SET = "<Not Set>";
    
    public static final String TEXT = "text";
    public static final String BINARY = "binary";    
    public static final String XML = "xml";
    public static final String ENCODED_DATA = "encoded data";

    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_PORT = "21";            

    public static final String DIRECTION = "Direction";
    
    public static final String DELIM_LINE_FEED = "LINE FEED";
    
    public static final int XML_MESSAGE_TYPE = 0;
    public static final int TEXT_MESSAGE_TYPE = 1;
    public static final int ENCODED_MESSAGE_TYPE = 2;
    public static final int BINARY_MESSAGE_TYPE = 3;
    

    public static final String BASE64_BINARY = "base64Binary";    
    public static final String XSD_BASE64_BINARY = "xsd:base64Binary";
    public static final String XSD_STRING = "xsd:string";    

    public static final String CHARSET_DEFAULT = "<default>";

    public static final String[] LIST_STYLES = new String [] {
                    "UNIX",
                    "AS400",
                    "AS400-UNIX",
                    "HCLFTPD 6.0.1.3",
                    "HCLFTPD 5.1",
                    "HP NonStop/Tandem",
                    "MPE",
                    "MSFTPD 2.0",
                    "MSP PDS (Fujitsu)",
                    "MSP PS (Fujitsu)",
                    "MVS GDG",
                    "MVS PDS",
                    "MVS Sequential",
                    "Netware 4.11",
                    "NT 3.5",
                    "NT 4.0",
                    "UNIX (EUC-JP)",
                    "UNIX (SJIS)",
                    "VM/ESA",
                    "VMS",
                    "VOS3 PDS (Hitachi)",
                    "VOS3 PS (Hitachi)",
                    "VOSK (Hitachi)"
    };

    public static final String[] SEC_FTP_TYPES = new String [] {
                "None",
                "ExplicitSSL",
                "ImplicitSSL"
    };

    public static final String[] TRANSFER_MODES = new String [] {
                "ASCII",
                "BINARY",
                "EBCDIC"
    };

    public static final String[] PRE_CMD_PUT_GET = new String [] {
            "NONE",
            "COPY",
            "RENAME"
    };
    public static final String[] POST_CMD_GET = new String [] {
            "NONE",
            "DELETE",
            "RENAME"
    };

    public static final String[] POST_CMD_PUT = new String [] {
            "NONE",
            "RENAME"
    };

    public static boolean stringValueIsTrue (String val) {        
        if (val == null || val.equals(BOOLEAN_FALSE) ) {
            return false;
        } else {
            return true;
        }
    };
    public static final String WSDL_PROP_MSGREPO = "com.netbeans.modules.wsdlextensions.ftp.message.repo";
    public static final String WSDL_PROP_REQRESPCORRELATE = "com.netbeans.modules.wsdlextensions.ftp.reqresp.correlate";
}
