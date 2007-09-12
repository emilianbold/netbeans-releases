/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.wsdlextensions.file.model;

/**
 * @author sweng
 */
public interface FileMessage extends FileComponent {
    /**
     * 
     */
    public static final String ATTR_FILE_USE = "use";
    /**
     * 
     */
    public static final String ATTR_FILE_ENCODING_STYLE = "encodingStyle";
    /**
     * 
     */
    public static final String ATTR_FILE_TYPE = "fileType";
    /**
     * 
     */
    public static final String ATTR_FILE_NAME_IS_PATTERN = "fileNameIsPattern";
    /**
     * 
     */
    public static final String ATTR_FILE_NAME = "fileName";
    /**
     * 
     */
    public static final String ATTR_POLLING_INTERVAL = "pollingInterval";
    /**
     * 
     */
    public static final String ATTR_REMOVE_EOL = "removeEOL";
    /**
     * 
     */
    public static final String ATTR_ADD_EOL = "addEOL";
    /**
     * 
     */
    public static final String ATTR_MULTIPLE_RECORDS_PER_FILE = "multipleRecordsPerFile";
    /**
     * 
     */
    public static final String ATTR_RECORD_DELIM = "recordDelimiter";
    /**
     * 
     */
    public static final String ATTR_MAX_BYTES_PER_RECORD = "maxBytesPerRecord";
    /**
     * 
     */
    public static final String ATTR_PART = "part";

    /**
     * 
     */
    public static final String ATTR_PROTECT_ENABLED = "protect";
    /**
     * 
     */
    public static final String ATTR_ARCHIVE_ENABLED = "archive";
    /**
     * 
     */
    public static final String ATTR_STAGING_ENABLED = "stage";

    /**
     * 
     */
    public static final String ATTR_PROTECT_DIR = "protectDirectory";
    public static final String ATTR_PROTECT_DIR_IS_RELATIVE = "protectDirIsRelative";
    /**
     * 
     */
    public static final String ATTR_ARCHIVE_DIR = "archiveDirectory";
    public static final String ATTR_ARCHIVE_DIR_IS_RELATIVE = "archiveDirIsRelative";
    /**
     * 
     */
    public static final String ATTR_STAGING_DIR = "stageDirectory";
    public static final String ATTR_STAGING_DIR_IS_RELATIVE = "stageDirIsRelative";
    
    /**
     * 
     * @param val 
     */
    public void setFileUseType(String val);
    /**
     * 
     * @return 
     */
    public String getFileUseType();
    /**
     * 
     * @param val 
     */
    public void setFileEncodingStyle(String val);
    /**
     * 
     * @return 
     */
    public String getFileEncodingStyle();
    /**
     * 
     * @param val 
     */
    public void setFileType(String val);
    /**
     * 
     * @return 
     */
    public String getFileType();
    /**
     * 
     * @param val 
     */
    public void setFileName(String val);
    /**
     * 
     * @return 
     */
    public String getFileName();
    /**
     * 
     * @param val 
     */
    public void setFileNameIsPattern(boolean val);
    /**
     * 
     * @return 
     */
    public boolean getFileNameIsPattern();
    /**
     * 
     * @param val 
     */
    public void setPollingInterval(long val);
    /**
     * 
     * @return 
     */
    public Long getPollingInterval();
    /**
     * 
     * @param val 
     */
    public void setMaxBytesPerRecord(long val);
    /**
     * 
     * @return 
     */
    public Long getMaxBytesPerRecord();
    /**
     * 
     * @param val 
     */
    public void setRecordDelimiter(String val);
    /**
     * 
     * @return 
     */
    public String getRecordDelimiter();
    /**
     * 
     * @param val 
     */
    public void setAddEOL(boolean val);
    /**
     * 
     * @return 
     */
    public boolean getAddEOL();
    /**
     * 
     * @param val 
     */
    public void setRemoveEOL(boolean val);
    /**
     * 
     * @return 
     */
    public boolean getRemoveEOL();
    /**
     * 
     * @param val 
     */
    public void setMultipleRecordsPerFile(boolean val);
    /**
     * 
     * @return 
     */
    public boolean getMultipleRecordsPerFile();
    /**
     * 
     * @param val 
     */
    public void setPart(String val);
    /**
     * 
     * @return 
     */
    public String getPart();

    public boolean getArchiveEnabled();
    public void setArchiveEnabled(boolean b);
    
    public boolean getProtectEnabled();
    public void setProtectEnabled(boolean b);

    public boolean getStagingEnabled();
    public void setStagingEnabled(boolean b);

    public String getArchiveDirectory();
    public void setArchiveDirectory(String s);
    
    public String getProtectDirectory();
    public void setProtectDirectory(String s);

    public String getStagingDirectory();
    public void setStagingDirectory(String s);

    public boolean getArchiveDirIsRelative();
    public void setArchiveDirIsRelative(boolean b);
    
    public boolean getProtectDirIsRelative();
    public void setProtectDirIsRelative(boolean b);

    public boolean getStagingDirIsRelative();
    public void setStagingDirIsRelative(boolean b);
}
