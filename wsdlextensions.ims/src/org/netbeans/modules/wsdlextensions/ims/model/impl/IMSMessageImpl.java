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

package org.netbeans.modules.wsdlextensions.ims.model.impl;

import java.util.Collection;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

import org.netbeans.modules.wsdlextensions.ims.model.IMSMessage;
import org.netbeans.modules.wsdlextensions.ims.model.IMSComponent;
import org.netbeans.modules.wsdlextensions.ims.model.IMSQName;

import org.w3c.dom.Element;

public class IMSMessageImpl extends IMSComponentImpl implements IMSMessage {
    
    public IMSMessageImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public IMSMessageImpl(WSDLModel model){
        this(model, createPrefixedElement(IMSQName.MESSAGE.getQName(), model));
    }
    
    public void accept(IMSMessage.Visitor visitor) {
        visitor.visit(this);
    }

    public String getIrmLen() {
        return getAttribute(IMSAttribute.IRM_LEN);
    }

    public void setIrmLen(String len) {
        setAttribute(IRM_LEN, IMSAttribute.IRM_LEN, len.toString());
    }

	public String getIrmId() {
        return getAttribute(IMSAttribute.IRM_ID);
    }

    public void setIrmId(String id) {
        setAttribute(IMSMessage.IRM_ID, IMSAttribute.IRM_ID, id);
    }

	public String getIrmTimer() {
        return getAttribute(IMSAttribute.IRM_TIMER);
    }

    public void setIrmTimer(String timer) {
        setAttribute(IMSMessage.IRM_TIMER, IMSAttribute.IRM_TIMER, timer);
    }

	public String getIrmSocket() {
        return getAttribute(IMSAttribute.IRM_SOCKET);
    }

    public void setIrmSocket(String soct) {
        setAttribute(IMSMessage.IRM_SOCKET, IMSAttribute.IRM_SOCKET, soct);
    }

	public String getIrmClientId() {
        return getAttribute(IMSAttribute.IRM_CLIENT_ID);
    }

    public void setIrmClientId(String clientId) {
        setAttribute(IMSMessage.IRM_CLIENT_ID, IMSAttribute.IRM_CLIENT_ID, clientId);
    }

	public String getIrmMod() {
        return getAttribute(IMSAttribute.IRM_MOD);
    }

    public void setIrmMod(String mod) {
        setAttribute(IMSMessage.IRM_MOD, IMSAttribute.IRM_MOD, mod);
    }

	public String getIrmCommitMode() {
        return getAttribute(IMSAttribute.IRM_COMMIT_MODE);
    }

    public void setIrmCommitMode(String mode) {
        setAttribute(IMSMessage.IRM_COMMIT_MODE, IMSAttribute.IRM_COMMIT_MODE, mode);
    }

	public String getIrmSyncLevel() {
        return getAttribute(IMSAttribute.IRM_SYNC_LEVEL);
    }

    public void setIrmSyncLevel(String level) {
        setAttribute(IMSMessage.IRM_SYNC_LEVEL, IMSAttribute.IRM_SYNC_LEVEL, level);
    }

	public String getIrmAck() {
        return getAttribute(IMSAttribute.IRM_ACK);
    }

    public void setIrmAck(String ack) {
        setAttribute(IMSMessage.IRM_ACK, IMSAttribute.IRM_ACK, ack);
    }

	public String getIrmFlow() {
        return getAttribute(IMSAttribute.IRM_FLOW);
    }

    public void setIrmFlow(String flow) {
        setAttribute(IMSMessage.IRM_FLOW, IMSAttribute.IRM_FLOW, flow);
    }

	public String getIrmTranCode() {
        return getAttribute(IMSAttribute.IRM_TRAN_CODE);
    }

    public void setIrmTranCode(String tcode) {
        setAttribute(IMSMessage.IRM_TRAN_CODE, IMSAttribute.IRM_TRAN_CODE, tcode);
    }

	public String getIrmTranCodeSrc() {
        return getAttribute(IMSAttribute.IRM_TRAN_CODE_SRC);
    }

    public void setIrmTranCodeSrc(String tcode) {
        setAttribute(IMSMessage.IRM_TRAN_CODE_SRC, IMSAttribute.IRM_TRAN_CODE_SRC, tcode);
    }

	public String getIrmDestId() {
        return getAttribute(IMSAttribute.IRM_DEST_ID);
    }

    public void setIrmDestId(String dest) {
        setAttribute(IMSMessage.IRM_DEST_ID, IMSAttribute.IRM_DEST_ID, dest);
    }

	public String getIrmLterm() {
        return getAttribute(IMSAttribute.IRM_LTERM);
    }

    public void setIrmLterm(String lterm) {
        setAttribute(IMSMessage.IRM_LTERM, IMSAttribute.IRM_LTERM, lterm);
    }

	public String getIrmRacfGrpName() {
        return getAttribute(IMSAttribute.IRM_RACF_GRP_NAME);
    }

    public void setIrmRacfGrpName(String grpname) {
        setAttribute(IMSMessage.IRM_RACF_GRP_NAME, IMSAttribute.IRM_RACF_GRP_NAME, grpname);
    }

	public String getIrmRacfUserId() {
        return getAttribute(IMSAttribute.IRM_RACF_USER_ID);
    }

    public void setIrmRacfUserId(String userid) {
        setAttribute(IMSMessage.IRM_RACF_USER_ID, IMSAttribute.IRM_RACF_USER_ID, userid);
    }

	public String getIrmRacfPwd() {
        return getAttribute(IMSAttribute.IRM_RACF_PASS);
    }

    public void setIrmRacfPwd(String pwd) {
        setAttribute(IMSMessage.IRM_RACF_PASS, IMSAttribute.IRM_RACF_PASS, pwd);
    }

	public String getIrmHeaderEncod() {
        return getAttribute(IMSAttribute.IRM_HEADER_ENCODING);
    }

    public void setIrmHeaderEncod(String hdr) {
        setAttribute(IMSMessage.IRM_HEADER_ENCODING, IMSAttribute.IRM_HEADER_ENCODING, hdr);
    }

	public String getSendDataEncod() {
        return getAttribute(IMSAttribute.SEND_DATA_ENCODING);
    }

    public void setSendDataEncod(String data) {
        setAttribute(IMSMessage.SEND_DATA_ENCODING, IMSAttribute.SEND_DATA_ENCODING, data);
    }

	public String getReplyDataEncod() {
        return getAttribute(IMSAttribute.REPLY_DATA_ENCODING);
    }

    public void setReplyDataEncod(String data) {
        setAttribute(IMSMessage.REPLY_DATA_ENCODING, IMSAttribute.REPLY_DATA_ENCODING, data);
    }

    public String getUse() {
        return getAttribute(IMSAttribute.IMS_USE);
    }

    public void setUse(String use) {
        setAttribute(IMS_USE, IMSAttribute.IMS_USE, use);
    }
	
    public String getEncodingStyle() {
        return getAttribute(IMSAttribute.IMS_ENCODING_STYLE);
    }

    public void setEncodingStyle(String style) {
        setAttribute(IMS_ENCODING_STYLE, IMSAttribute.IMS_ENCODING_STYLE,style);
	}

	public String getMessagePart() {
        return getAttribute(IMSAttribute.IMS_PART);
    }

    public void setMessagePart(String part) {
        setAttribute(IMS_PART, IMSAttribute.IMS_PART, part);
    }
	
}
