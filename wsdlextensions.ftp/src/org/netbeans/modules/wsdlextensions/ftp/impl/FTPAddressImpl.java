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

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.ftp.FTPAddress;
import org.netbeans.modules.wsdlextensions.ftp.FTPComponent;
import org.netbeans.modules.wsdlextensions.ftp.FTPQName;
import org.w3c.dom.Element;

/**
 */
public class FTPAddressImpl extends FTPComponentImpl implements FTPAddress {
    public FTPAddressImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public FTPAddressImpl(WSDLModel model){
        this(model, createPrefixedElement(FTPQName.ADDRESS.getQName(), model));
    }
    
    public void accept(FTPComponent.Visitor visitor) {
        visitor.visit(this);
    }

    public void setFTPURL(String ftpURL) {
        setAttribute(FTPAddress.FTP_URL_PROPERTY, FTPAttribute.FTP_URL_PROPERTY, ftpURL);
    }

    public String getFTPURL() {
        return getAttribute(FTPAttribute.FTP_URL_PROPERTY);
    }

    public String getDirListStyle() {
        return getAttribute(FTPAttribute.FTP_DIRLSTSTYLE_PROPERTY);
    }

    public void setDirListStyle(String style) {
        setAttribute(FTP_DIRLSTSTYLE_PROPERTY, FTPAttribute.FTP_DIRLSTSTYLE_PROPERTY, style);
    }

    public String getUserDefDirListStyle() {
        return getAttribute(FTPAttribute.FTP_UD_DIRLSTSTYLE_PROPERTY);
    }

    public void setUserDefDirListStyle(String style) {
        setAttribute(FTP_UD_DIRLSTSTYLE_PROPERTY, FTPAttribute.FTP_UD_DIRLSTSTYLE_PROPERTY, style);
    }

    public String getUserDefDirListHeuristics() {
        return getAttribute(FTPAttribute.FTP_UD_HEURISTICS_PROPERTY);
    }

    public void setUserDefDirListHeuristics(String heuristicsLoc) {
        setAttribute(FTP_UD_HEURISTICS_PROPERTY, FTPAttribute.FTP_UD_HEURISTICS_PROPERTY, heuristicsLoc);
    }

    public String getTrnasferMode() {
        return getAttribute(FTPAttribute.FTP_TRANSMODE_PROPERTY);
    }

    public void setTransferMode(String mode) {
        setAttribute(FTP_TRANSMODE_PROPERTY, FTPAttribute.FTP_TRANSMODE_PROPERTY, mode);
    }

    public boolean getUseUserDefinedHeuristics() {
        String heuristic = getAttribute(FTPAttribute.FTP_USE_UD_HEURISTICS_PROPERTY);
        return heuristic != null && heuristic.equals("true");
    }

    public void setUseUserDefinedHeuristics(boolean useUserDefined) {
        setAttribute(FTP_USE_UD_HEURISTICS_PROPERTY, FTPAttribute.FTP_USE_UD_HEURISTICS_PROPERTY, useUserDefined ? "true" : "false");
    }

    public String getCmdChannelTimeout() {
        return getAttribute(FTPAttribute.FTP_CMD_CH_TIMEOUT_PROPERTY);
    }

    public void setCmdChannelTimeout(String s) {
        setAttribute(FTP_CMD_CH_TIMEOUT_PROPERTY, FTPAttribute.FTP_CMD_CH_TIMEOUT_PROPERTY, s);
    }

    public String getDataChannelTimeout() {
        return getAttribute(FTPAttribute.FTP_DATA_CH_TIMEOUT_PROPERTY);
    }

    public void setDataChannelTimeout(String s) {
        setAttribute(FTP_DATA_CH_TIMEOUT_PROPERTY, FTPAttribute.FTP_DATA_CH_TIMEOUT_PROPERTY, s);
    }

}
