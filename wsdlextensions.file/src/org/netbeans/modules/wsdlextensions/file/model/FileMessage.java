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
package org.netbeans.modules.wsdlextensions.file.model;

/**
 * @author sweng
 */
public interface FileMessage extends FileComponent {
    public static final String ATTR_FILE_USE = "use";
    public static final String ATTR_FILE_ENCODING_STYLE = "encodingStyle";
    public static final String ATTR_FILE_TYPE = "fileType";
    public static final String ATTR_FILE_NAME_IS_PATTERN = "fileNameIsPattern";
    public static final String ATTR_FILE_NAME = "fileName";
    public static final String ATTR_POLLING_INTERVAL = "pollingInterval";
    public static final String ATTR_REMOVE_EOL = "removeEOL";
    public static final String ATTR_ADD_EOL = "addEOL";
    public static final String ATTR_MULTIPLE_RECORDS_PER_FILE = "multipleRecordsPerFile";
    public static final String ATTR_RECORD_DELIM = "recordDelimiter";
    public static final String ATTR_MAX_BYTES_PER_RECORD = "maxBytesPerRecord";
    public static final String ATTR_PART = "part";
    
    public void setFileUseType(String val);
    public String getFileUseType();
    public void setFileEncodingStyle(String val);
    public String getFileEncodingStyle();
    public void setFileType(String val);
    public String getFileType();
    public void setFileName(String val);
    public String getFileName();
    public void setFileNameIsPattern(boolean val);
    public boolean getFileNameIsPattern();
    public void setPollingInterval(long val);
    public Long getPollingInterval();
    public void setMaxBytesPerRecord(long val);
    public Long getMaxBytesPerRecord();
    public void setRecordDelimiter(String val);
    public String getRecordDelimiter();
    public void setAddEOL(boolean val);
    public boolean getAddEOL();
    public void setRemoveEOL(boolean val);
    public boolean getRemoveEOL();
    public void setMultipleRecordsPerFile(boolean val);
    public boolean getMultipleRecordsPerFile();
    public void setPart(String val);
    public String getPart();
}
