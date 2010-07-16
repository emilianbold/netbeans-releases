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

package org.netbeans.modules.wsdlextensions.ftp.impl;

import org.netbeans.modules.wsdlextensions.ftp.FTPMessage;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.ftp.FTPComponent;
import org.netbeans.modules.wsdlextensions.ftp.FTPQName;
import org.w3c.dom.Element;

/**
 * @author jim.fu@sun.com
 */
public class FTPMessageImpl extends FTPComponentImpl implements FTPMessage {
    
    public FTPMessageImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public FTPMessageImpl(WSDLModel model){
        this(model, createPrefixedElement(FTPQName.MESSAGE.getQName(), model));
    }
    
    public void accept(FTPComponent.Visitor visitor) {
        visitor.visit(this);
    }

    public String getUse() {
        return getAttribute(FTPAttribute.FTP_USE_PROPERTY);
    }

    public void setUse(String use) {
        setAttribute(FTP_USE_PROPERTY, FTPAttribute.FTP_USE_PROPERTY, use);
    }

    public String getPollInterval() {
        return getAttribute(FTPAttribute.FTP_POLLINTERVAL_PROPERTY);
    }

    public void setPollInterval(String s) {
        setAttribute(FTP_POLLINTERVAL_PROPERTY, FTPAttribute.FTP_POLLINTERVAL_PROPERTY, s);
    }

    public String getPart() {
        return getAttribute(FTPAttribute.FTP_PART_PROPERTY);
    }

    public void setPart(String part) {
        setAttribute(FTP_PART_PROPERTY, FTPAttribute.FTP_PART_PROPERTY, part);
    }

    public String getEncodingStyle() {
        return getAttribute(FTPAttribute.FTP_ENCODINGSTYLE_PROPERTY);
    }

    public void setEncodingStyle(String encodingStyle) {
        setAttribute(FTP_ENCODINGSTYLE_PROPERTY, FTPAttribute.FTP_ENCODINGSTYLE_PROPERTY, encodingStyle);
    }

    public String getMessageRepository() {
        return getAttribute(FTPAttribute.FTP_SYMETRIC_MSG_REPO_PROPERTY);
    }

    public void setMessageRepository(String s) {
        setAttribute(FTP_SYMETRIC_MSG_REPO_PROPERTY, FTPAttribute.FTP_SYMETRIC_MSG_REPO_PROPERTY, s);
    }

    public boolean getArchiveEnabled() {
        String s = getAttribute(FTPAttribute.FTP_ARCHIVE_ENABLED_PROPERTY);
        return s == null || s.equals("true");
    }

    public void setArchiveEnabled(boolean b) {
        setAttribute(FTP_ARCHIVE_ENABLED_PROPERTY, FTPAttribute.FTP_ARCHIVE_ENABLED_PROPERTY, b ? "true" : "false");
    }

    public boolean getProtectEnabled() {
        String s = getAttribute(FTPAttribute.FTP_PROTECT_ENABLED_PROPERTY);
        return s != null && s.equals("true");
    }

    public void setProtectEnabled(boolean b) {
        setAttribute(FTP_PROTECT_ENABLED_PROPERTY, FTPAttribute.FTP_PROTECT_ENABLED_PROPERTY, b ? "true" : "false");
    }

    public boolean getMessageCorrelateEnabled() {
        String s = getAttribute(FTPAttribute.FTP_MSG_CORRELATE_PROPERTY);
        return s != null && s.equals("true");
    }

    public void setMessageCorrelateEnabled(boolean b) {
        setAttribute(FTP_MSG_CORRELATE_PROPERTY, FTPAttribute.FTP_MSG_CORRELATE_PROPERTY, b ? "true" : "false");
    }

    public String getMessageName() {
        return getAttribute(FTPAttribute.FTP_SYMETRIC_MSG_NAME_PROPERTY);
    }

    public void setMessageName(String s) {
        setAttribute(FTP_SYMETRIC_MSG_NAME_PROPERTY, FTPAttribute.FTP_SYMETRIC_MSG_NAME_PROPERTY, s);
    }

    public String getMessageNamePrefixIB() {
        return getAttribute(FTPAttribute.FTP_SYMETRIC_MSG_NAME_PREFIX_IB_PROPERTY);
    }

    public void setMessageNamePrefixIB(String s) {
        setAttribute(FTP_SYMETRIC_MSG_NAME_PREFIX_IB_PROPERTY, FTPAttribute.FTP_SYMETRIC_MSG_NAME_PREFIX_IB_PROPERTY, s);
    }

    public String getMessageNamePrefixOB() {
        return getAttribute(FTPAttribute.FTP_SYMETRIC_MSG_NAME_PREFIX_OB_PROPERTY);
    }

    public void setMessageNamePrefixOB(String s) {
        setAttribute(FTP_SYMETRIC_MSG_NAME_PREFIX_OB_PROPERTY, FTPAttribute.FTP_SYMETRIC_MSG_NAME_PREFIX_OB_PROPERTY, s);
    }

    public boolean getStagingEnabled() {
        String s = getAttribute(FTPAttribute.FTP_STAGING_ENABLED_PROPERTY);
        return s == null || s.trim().equals("true"); // defaults to true
    }

    public void setStagingEnabled(boolean b) {
        setAttribute(FTP_STAGING_ENABLED_PROPERTY, FTPAttribute.FTP_STAGING_ENABLED_PROPERTY, b ? "true" : "false");
    }

    public String getFileType() {
        return getAttribute(FTPAttribute.FTP_FILETYPE_PROPERTY);
    }

    public void setFileType(String s) {
        setAttribute(FTP_FILE_TYPE_PROPERTY, FTPAttribute.FTP_FILETYPE_PROPERTY, s);
    }

    public boolean getForwardAsAttachment() {
        String s = getAttribute(FTPAttribute.FTP_FWDATTACH_PROPERTY);
        return s != null && s.equals("true");
    }

    public void setForwardAsAttachment(boolean b) {
        setAttribute(FTP_FWD_ATTACH_PROPERTY, FTPAttribute.FTP_FWDATTACH_PROPERTY, b ? "true" : "false");
    }

    public String getCharacterEncoding() {
        return getAttribute(FTPAttribute.FTP_CHAR_ENCODE_PROPERTY);
    }

    public void setCharacterEncoding(String s) {
        setAttribute(FTP_CHAR_ENCODE_PROPERTY, FTPAttribute.FTP_CHAR_ENCODE_PROPERTY, s);
    }
}
