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
package org.netbeans.modules.mashup.db.common;

/**
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public interface PropertyKeys {

    public static final String WIZARDCUSTOMFIELDDELIMITER = "WIZARDCUSTOMFIELDDELIMITER"; // NOI18N
    public static final String FIELDDELIMITER = "FIELDDELIMITER"; // NOI18N

    public static final String FILENAME = "FILENAME"; // NOI18N

    public static final String HEADERBYTESOFFSET = "HEADERBYTESOFFSET"; // NOI18N

    public static final String ISFIRSTLINEHEADER = "ISFIRSTLINEHEADER"; // NOI18N

    /* Constant: name of loadtype property ('DELIMITED', 'FIXEDWIDTH', etc.) */
    public static final String LOADTYPE = "LOADTYPE"; // NOI18N
    public static final String DELIMITED = "Delimited";
    public static final String FIXEDWIDTH = "FixedWidth";
    public static final String RSS = "RSS";
    public static final String WEB = "WEB";
    public static final String XML = "XML";
    public static final String WEBROWSET = "WEBROWSET";
    public static final String SPREADSHEET = "SPREADSHEET";
    public static final String JDBC = "REMOTE";
    
    public static final String TABLENUMBER = "TABLENUMBER";
    
    public static final String REFRESH = "REFRESH";
    
    public static final String URL = "URL";

    public static final String TYPE = "TYPE";
    
    public static final String ROWNAME = "ROWNAME";
    
    public static final String SHEET = "SHEET";
    
    public static final String READONLY = "READONLY";
    
    public static final String READWRITE = "READWRITE";
    
    public static final String MAXFAULTS = "MAXFAULTS"; // NOI18N

    public static final String QUALIFIER = "QUALIFIER"; // NOI18N

    public static final String RECORDDELIMITER = "RECORDDELIMITER"; // NOI18N

    public static final String ROWSTOSKIP = "ROWSTOSKIP"; // NOI18N

    public static final String WIZARDDEFAULTPRECISION = "WIZARDDEFAULTPRECISION"; // NOI18N
    public static final String WIZARDDEFAULTSQLTYPE = "WIZARDDEFAULTSQLTYPE"; // NOI18N

    public static final String WIZARDFIELDCOUNT = "WIZARDFIELDCOUNT"; // NOI18N

    public static final String WIZARDRECORDLENGTH = "WIZARDRECORDLENGTH"; // NOI18N

    public static final String WIZARDFILEPATH = "WIZARDFILEPATH"; // NOI18N
    
    public static final String TRIMWHITESPACE = "TRIMWHITESPACE";

}
