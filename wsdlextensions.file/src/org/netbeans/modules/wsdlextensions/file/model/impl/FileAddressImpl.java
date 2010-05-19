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

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

import org.netbeans.modules.wsdlextensions.file.model.FileAddress;
import org.netbeans.modules.wsdlextensions.file.model.FileComponent;
import org.netbeans.modules.wsdlextensions.file.model.FileQName;

/**
 * @author sweng
 * @author jfu
 */
public class FileAddressImpl extends FileComponentImpl implements FileAddress {
    public FileAddressImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public FileAddressImpl(WSDLModel model){
        this(model, createPrefixedElement(FileQName.ADDRESS.getQName(), model));
    }
    
    public void accept(FileComponent.Visitor visitor) {
        visitor.visit(this);
    }

    public void setRelativePath(boolean val) {
        setAttribute(ATTR_FILE_RELATIVE_PATH, FileAttribute.FILE_ADDRESS_RELATIVEPATH_PROPERTY, val? "true" : "false");
    }
    
    public boolean getRelativePath() {
        String s = getAttribute(FileAttribute.FILE_ADDRESS_RELATIVEPATH_PROPERTY);
        return s != null && s.equals("true");
    }

    public void setFileDirectory(String val) {
        setAttribute(ATTR_FILE_ADDRESS, FileAttribute.FILE_ADDRESS_FILEDIRECTORY_PROPERTY, val);
    }
    
    public String getFileDirectory() {
        return getAttribute(FileAttribute.FILE_ADDRESS_FILEDIRECTORY_PROPERTY);
    }
    
    public void setPathRelativeTo(String val) {
        setAttribute(ATTR_FILE_PATH_RELATIVE_TO, FileAttribute.FILE_ADDRESS_PATHRELATIVETO_PROPERTY, val);
    }
    
    public String getPathRelativeTo() {
        return getAttribute(FileAttribute.FILE_ADDRESS_PATHRELATIVETO_PROPERTY);
    }

    public void setLockName(String val) {
        setAttribute(ATTR_FILE_LOCK_NAME, FileAttribute.FILE_ADDRESS_LOCK_NAME, val);
    }

    public String getLockName() {
        return getAttribute(FileAttribute.FILE_ADDRESS_LOCK_NAME);
    }

    public void setWorkArea(String val) {
        setAttribute(ATTR_FILE_WORK_AREA, FileAttribute.FILE_ADDRESS_WORK_AREA, val);
    }

    public String getWorkArea() {
        return getAttribute(FileAttribute.FILE_ADDRESS_WORK_AREA);
    }

    public void setSeqName(String val) {
        setAttribute(ATTR_FILE_SEQ_NAME, FileAttribute.FILE_ADDRESS_SEQ_NAME, val);
    }

    public String getSeqName() {
        return getAttribute(FileAttribute.FILE_ADDRESS_SEQ_NAME);
    }

    public void setPersistenceBaseLoc(String val) {
        setAttribute(ATTR_FILE_PERSIST_BASELOC, FileAttribute.FILE_ADDRESS_PERSIST_BASE_PROPERTY, val);
    }

    public String getPersistenceBaseLoc() {
        return getAttribute(FileAttribute.FILE_ADDRESS_PERSIST_BASE_PROPERTY);
    }

    public void setRecursive(boolean val) {
        setAttribute(ATTR_FILE_RECURSIVE, FileAttribute.FILE_ADDRESS_RECURSIVE, val? "true" : "false");
    }

    public boolean getRecursive() {
        String s = getAttribute(FileAttribute.FILE_ADDRESS_RECURSIVE);
        return s != null && s.equals("true");
    }

    public void setRecursiveExclude(String val) {
        setAttribute(ATTR_FILE_RECURSIVE_EXCLUDE, FileAttribute.FILE_ADDRESS_RECURSIVE_EXCLUDE, val);
    }

    public String getRecursiveExclude() {
        return getAttribute(FileAttribute.FILE_ADDRESS_RECURSIVE_EXCLUDE);
    }
    
}
