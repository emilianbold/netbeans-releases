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

package org.netbeans.modules.wsdlextensions.file.model.impl;

import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * @author sweng
 */
public enum FileAttribute implements Attribute {
    FILE_ADDRESS_FILEDIRECTORY_PROPERTY("fileDirectory"),
    FILE_ADDRESS_RELATIVEPATH_PROPERTY("relativePath"),
    FILE_ADDRESS_PATHRELATIVETO_PROPERTY("pathRelativeTo"),
    FILE_ADDRESS_LOCK_NAME("lockName"),
    FILE_ADDRESS_WORK_AREA("workArea"),
    FILE_ADDRESS_SEQ_NAME("seqName"),
    
    FILE_MESSAGE_FILETYPE_PROPERTY("fileType"),
    FILE_MESSAGE_ENCODINGSTYLE_PROPERTY("encodingStyle"),
    FILE_MESSAGE_USE_PROPERTY("use"),
    FILE_MESSAGE_PART_PROPERTY("part"),
    FILE_MESSAGE_POLLINTERVAL_PROPERTY("pollingInterval"),
    FILE_MESSAGE_FILENAME_PROPERTY("fileName"),
    FILE_MESSAGE_FILENAMEISPATTERN_PROPERTY("fileNameIsPattern"),
    FILE_MESSAGE_REMOVEOL_PROPERTY("removeEOL"),
    FILE_MESSAGE_ADDEOL_PROPERTY("addEOL"),
    FILE_MESSAGE_MULTIPLERECORDSPERFILE_MESSAGE__PROPERTY("multipleRecordsPerFile"),
    FILE_MESSAGE_RECORDDELIMITER_PROPERTY("recordDelimiter"),
    FILE_MESSAGE_MAXBYTESPERRECORD_PROPERTY("maxBytesPerRecord"),
    FILE_MESSAGE_PROTECT_PROPERTY("protect"),
    FILE_MESSAGE_ARCHIVE_PROPERTY("archive"),
    FILE_MESSAGE_STAGE_PROPERTY("stage"),
    FILE_MESSAGE_PROTECT_DIR_PROPERTY("protectDirectory"),
    FILE_MESSAGE_ARCHIVE_DIR_PROPERTY("archiveDirectory"),
    FILE_MESSAGE_STAGE_DIR_PROPERTY("stageDirectory"),
    FILE_MESSAGE_PROTECT_DIR_IS_RELATIVE("protectDirIsRelative"),
    FILE_MESSAGE_ARCHIVE_DIR_IS_RELATIVE("archiveDirIsRelative"),
    FILE_MESSAGE_STAGE_DIR_IS_RELATIVE("stageDirIsRelative");
    

    private String name;
    private Class type;
    private Class subtype;
    
    FileAttribute(String name) {
        this(name, String.class);
    }
    
    FileAttribute(String name, Class type) {
        this(name, type, null);
    }
    
    FileAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    public String toString() { return name; }
    
    public Class getType() { return type; }
    
    public String getName() { return name; }
    
    public Class getMemberType() { return subtype; }
}
