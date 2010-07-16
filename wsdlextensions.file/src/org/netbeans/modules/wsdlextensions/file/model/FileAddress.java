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
 * @author sweng
 * @author jfu
 */
public interface FileAddress extends FileComponent {

    public static final String ATTR_FILE_ADDRESS = "fileDirectory";
    public static final String ATTR_FILE_RELATIVE_PATH = "relativePath";
    public static final String ATTR_FILE_PATH_RELATIVE_TO = "pathRelativeTo";
    public static final String ATTR_FILE_PERSIST_BASELOC = "persistenceBaseLoc";
    public static final String ATTR_FILE_LOCK_NAME = "lockName";
    public static final String ATTR_FILE_WORK_AREA = "workArea";
    public static final String ATTR_FILE_SEQ_NAME = "seqName";
    public static final String ATTR_FILE_RECURSIVE = "recursive";
    public static final String ATTR_FILE_RECURSIVE_EXCLUDE = "recursiveExclude";
    
    public void setRelativePath(boolean val);
    public boolean getRelativePath();
    public void setFileDirectory(String val);
    public String getFileDirectory();
    public void setPathRelativeTo(String val);
    public String getPathRelativeTo();

    public void setPersistenceBaseLoc(String val);
    public String getPersistenceBaseLoc();

    public void setLockName(String val);
    public String getLockName();
    public void setWorkArea(String val);
    public String getWorkArea();
    public void setSeqName(String val);
    public String getSeqName();
    public void setRecursive(boolean val);
    public boolean getRecursive();
    public void setRecursiveExclude(String val);
    public String getRecursiveExclude();
}
