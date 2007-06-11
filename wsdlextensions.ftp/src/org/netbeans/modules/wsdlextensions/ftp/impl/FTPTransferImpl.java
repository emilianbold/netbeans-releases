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

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.ftp.FTPTransfer;
import org.netbeans.modules.wsdlextensions.ftp.FTPComponent;
import org.netbeans.modules.wsdlextensions.ftp.FTPQName;
import org.w3c.dom.Element;

/**
 * @author jim.fu@sun.com
 */
public class FTPTransferImpl extends FTPComponentImpl implements FTPTransfer {
    
    public FTPTransferImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public FTPTransferImpl(WSDLModel model){
        this(model, createPrefixedElement(FTPQName.TRANSFER.getQName(), model));
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

    public String getSendTo() {
        return getAttribute(FTPAttribute.FTP_SENDTO_PROPERTY);
    }

    public void setSendTo(String s) {
        setAttribute(FTP_SENDTO_PROPERTY, FTPAttribute.FTP_SENDTO_PROPERTY, s);
    }

    public boolean getSendToHasPatterns() {
        String isPattStr = getAttribute(FTPAttribute.FTP_SENDTO_HAS_PATTS_PROPERTY);
        return isPattStr != null && isPattStr.equals("true"); 
    }

    public void setSendToHasPatterns(boolean b) {
        setAttribute(FTP_SENDTO_HAS_PATTS_PROPERTY, FTPAttribute.FTP_SENDTO_HAS_PATTS_PROPERTY, b ? "true" : "false");
    }

    public String getReceiveFrom() {
        return getAttribute(FTPAttribute.FTP_RECEIVEFROM_PROPERTY);
    }

    public void setReceiveFrom(String s) {
        setAttribute(FTP_RECEIVEFROM_PROPERTY, FTPAttribute.FTP_RECEIVEFROM_PROPERTY, s);
    }

    public boolean getReceiveFromHasPatterns() {
        String isPattStr = getAttribute(FTPAttribute.FTP_RECEIVEFROM_HAS_PATTS_PROPERTY);
        return isPattStr != null && isPattStr.equals("true"); 
    }

    public void setReceiveFromHasPatterns(boolean b) {
        setAttribute(FTP_RECEIVEFROM_HAS_PATTS_PROPERTY, FTPAttribute.FTP_RECEIVEFROM_HAS_PATTS_PROPERTY, b ? "true" : "false");
    }

    public String getPreSendCommand() {
        return getAttribute(FTPAttribute.FTP_PRE_SEND_CMD_PROPERTY);
    }

    public void setPreSendCommand(String s) {
        setAttribute(FTP_PRE_SEND_CMD_PROPERTY, FTPAttribute.FTP_PRE_SEND_CMD_PROPERTY, s);
    }

    public String getPreSendLocation() {
        return getAttribute(FTPAttribute.FTP_PRE_SEND_LOC_PROPERTY);
    }

    public void setPreSendLocation(String s) {
        setAttribute(FTP_PRE_SEND_LOC_PROPERTY, FTPAttribute.FTP_PRE_SEND_LOC_PROPERTY, s);
    }

    public boolean getPreSendLocationHasPatterns() {
        String isPattStr = getAttribute(FTPAttribute.FTP_PRE_SEND_LOC_HAS_PATTS_PROPERTY);
        return isPattStr != null && isPattStr.equals("true"); 
    }

    public void setPreSendLocationHasPatterns(boolean b) {
        setAttribute(FTP_PRE_SEND_LOC_HAS_PATTS_PROPERTY, FTPAttribute.FTP_PRE_SEND_LOC_HAS_PATTS_PROPERTY, b ? "true" : "false");
    }

    public String getPreReceiveCommand() {
        return getAttribute(FTPAttribute.FTP_PRE_RECEIVE_CMD_PROPERTY);
    }

    public void setPreReceiveCommand(String s) {
        setAttribute(FTP_PRE_RECEIVE_CMD_PROPERTY, FTPAttribute.FTP_PRE_RECEIVE_CMD_PROPERTY, s);
    }

    public String getPreReceiveLocation() {
        return getAttribute(FTPAttribute.FTP_PRE_RECEIVE_LOC_PROPERTY);
    }

    public void setPreReceiveLocation(String s) {
        setAttribute(FTP_PRE_RECEIVE_LOC_PROPERTY, FTPAttribute.FTP_PRE_RECEIVE_LOC_PROPERTY, s);
    }

    public boolean getPreReceiveLocationHasPatterns() {
        String isPattStr = getAttribute(FTPAttribute.FTP_PRE_RECEIVE_LOC_HAS_PATTS_PROPERTY);
        return isPattStr != null && isPattStr.equals("true"); 
    }

    public void setPreReceiveLocationHasPatterns(boolean b) {
        setAttribute(FTP_PRE_RECEIVE_LOC_HAS_PATTS_PROPERTY, FTPAttribute.FTP_PRE_RECEIVE_LOC_HAS_PATTS_PROPERTY, b ? "true" : "false");
    }

    public String getPostSendCommand() {
        return getAttribute(FTPAttribute.FTP_POST_SEND_CMD_PROPERTY);
    }

    public void setPostSendCommand(String s) {
        setAttribute(FTP_POST_SEND_CMD_PROPERTY, FTPAttribute.FTP_POST_SEND_CMD_PROPERTY, s);
    }

    public String getPostSendLocation() {
        return getAttribute(FTPAttribute.FTP_POST_SEND_LOC_PROPERTY);
    }

    public void setPostSendLocation(String s) {
        setAttribute(FTP_POST_SEND_LOC_PROPERTY, FTPAttribute.FTP_POST_SEND_LOC_PROPERTY, s);
    }

    public boolean getPostSendLocationHasPatterns() {
        String isPattStr = getAttribute(FTPAttribute.FTP_POST_SEND_LOC_PROPERTY);
        return isPattStr != null && isPattStr.equals("true"); 
    }

    public void setPostSendLocationHasPatterns(boolean b) {
        setAttribute(FTP_POST_SEND_LOC_PROPERTY, FTPAttribute.FTP_POST_SEND_LOC_PROPERTY, b ? "true" : "false");
    }

    public String getPostReceiveCommand() {
        return getAttribute(FTPAttribute.FTP_POST_RECEIVE_CMD_PROPERTY);
    }

    public void setPostReceiveCommand(String s) {
        setAttribute(FTP_POST_RECEIVE_CMD_PROPERTY, FTPAttribute.FTP_POST_RECEIVE_CMD_PROPERTY, s);
    }

    public String getPostReceiveLocation() {
        return getAttribute(FTPAttribute.FTP_POST_RECEIVE_LOC_PROPERTY);
    }

    public void setPostReceiveLocation(String s) {
        setAttribute(FTP_POST_RECEIVE_LOC_PROPERTY, FTPAttribute.FTP_POST_RECEIVE_LOC_PROPERTY, s);
    }

    public boolean getPostReceiveLocationHasPatterns() {
        String isPattStr = getAttribute(FTPAttribute.FTP_POST_RECEIVE_LOC_HAS_PATTS_PROPERTY);
        return isPattStr != null && isPattStr.equals("true"); 
    }

    public void setPostReceiveLocationHasPatterns(boolean b) {
        setAttribute(FTP_POST_RECEIVE_LOC_HAS_PATTS_PROPERTY, FTPAttribute.FTP_POST_RECEIVE_LOC_HAS_PATTS_PROPERTY, b ? "true" : "false");
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

    public boolean getAppend() {
        String append = getAttribute(FTPAttribute.FTP_APPEND_PROPERTY);
         return append != null && append.equals("true");
    }

    public void setAppend(boolean b) {
        setAttribute(FTP_APPEND_PROPERTY, FTPAttribute.FTP_APPEND_PROPERTY, b ? "true" : "false");
    }

    public boolean getSenderUseProxy() {
        String useProxy = getAttribute(FTPAttribute.FTP_SENDER_USEPROXY_PROPERTY);
        return useProxy != null && useProxy.equals("true");
    }

    public void setSenderUseProxy(boolean useProxy) {
        setAttribute(FTP_SENDER_USEPROXY_PROPERTY, FTPAttribute.FTP_SENDER_USEPROXY_PROPERTY, useProxy ? "true" : "false");
    }

    public String getSenderProxyURL() {
        return getAttribute(FTPAttribute.FTP_SENDER_PROXY_PROPERTY);
    }

    public void setSenderProxyURL(String proxyURL) {
        setAttribute(FTP_SENDER_PROXY_PROPERTY, FTPAttribute.FTP_SENDER_PROXY_PROPERTY, proxyURL);
    }
    
    public boolean getSenderUsePassive() {
        String usePassive = getAttribute(FTPAttribute.FTP_SENDER_USEPASSIVE_PROPERTY);
        return usePassive != null && usePassive.equals("true");
    }

    public void setSenderUsePassive(boolean usePassive) {
        setAttribute(FTP_SENDER_USEPASSIVE_PROPERTY, FTPAttribute.FTP_SENDER_USEPASSIVE_PROPERTY, usePassive ? "true" : "false");
    }

    public boolean getReceiverUseProxy() {
        String useProxy = getAttribute(FTPAttribute.FTP_RECEIVER_USEPROXY_PROPERTY);
        return useProxy != null && useProxy.equals("true");
    }

    public void setReceiverUseProxy(boolean useProxy) {
        setAttribute(FTP_RECEIVER_USEPROXY_PROPERTY, FTPAttribute.FTP_RECEIVER_USEPROXY_PROPERTY, useProxy ? "true" : "false");
    }

    public String getReceiverProxyURL() {
        return getAttribute(FTPAttribute.FTP_RECEIVER_PROXY_PROPERTY);
    }

    public void setReceiverProxyURL(String proxyURL) {
        setAttribute(FTP_RECEIVER_PROXY_PROPERTY, FTPAttribute.FTP_RECEIVER_PROXY_PROPERTY, proxyURL);
    }
    
    public boolean getReceiverUsePassive() {
        String usePassive = getAttribute(FTPAttribute.FTP_RECEIVER_USEPASSIVE_PROPERTY);
        return usePassive != null && usePassive.equals("true");
    }

    public void setReceiverUsePassive(boolean usePassive) {
        setAttribute(FTP_RECEIVER_USEPASSIVE_PROPERTY, FTPAttribute.FTP_RECEIVER_USEPASSIVE_PROPERTY, usePassive ? "true" : "false");
    }

    public boolean getMessageCorrelateEnabled() {
        String s = getAttribute(FTPAttribute.FTP_MSG_CORRELATE_PROPERTY);
        return s != null && s.equals("true");
    }

    public void setMessageCorrelateEnabled(boolean b) {
        setAttribute(FTP_MSG_CORRELATE_PROPERTY, FTPAttribute.FTP_MSG_CORRELATE_PROPERTY, b ? "true" : "false");
    }

}
