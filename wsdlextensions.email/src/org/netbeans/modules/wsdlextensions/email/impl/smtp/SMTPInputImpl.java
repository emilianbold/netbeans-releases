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
package org.netbeans.modules.wsdlextensions.email.impl.smtp;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPInput;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPComponent;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPQName;
import org.netbeans.modules.wsdlextensions.email.impl.EMAILAttribute;

import org.w3c.dom.Element;

/**
 * @author Sainath Adiraju
 */
public class SMTPInputImpl extends SMTPComponentImpl implements SMTPInput {

    public SMTPInputImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public SMTPInputImpl(WSDLModel model) {
        this(model, createPrefixedElement(SMTPQName.INPUT.getQName(), model));
    }

    public void accept(SMTPComponent.Visitor visitor) {
        visitor.visit(this);
    }

    public String getMessage() {
        return getAttribute(EMAILAttribute.EMAIL_MESSAGE_NAME);
    }

    public void setMessage(String message) {
        setAttribute(SMTPInput.SMTP_MESSAGE, EMAILAttribute.EMAIL_MESSAGE_NAME, message);
    }

    public String getSubject() {
        return getAttribute(EMAILAttribute.EMAIL_SUBJECT_NAME);
    }

    public void setSubject(String subject) {
        setAttribute(SMTPInput.SMTP_SUBJECT, EMAILAttribute.EMAIL_SUBJECT_NAME, subject);
    }

    public String getFrom() {
        return getAttribute(EMAILAttribute.EMAIL_FROM_NAME);
    }

    public void setFrom(String from) {
        setAttribute(SMTPInput.SMTP_FROM, EMAILAttribute.EMAIL_FROM_NAME, from);
    }

    public String getCharset() {
        return getAttribute(EMAILAttribute.EMAIL_CHARSET_NAME);
    }

    public void setCharset(String val) {
        setAttribute(SMTPInput.SMTP_CHARSET, EMAILAttribute.EMAIL_CHARSET_NAME, val);
    }

    public void setTo(String to) {
        setAttribute(SMTPInput.SMTP_TO, EMAILAttribute.EMAIL_TO_NAME, to);
    }

    public String getTo() {
        return getAttribute(EMAILAttribute.EMAIL_TO_NAME);
    }

    public void setCc(String cc) {
        setAttribute(SMTPInput.SMTP_CC, EMAILAttribute.EMAIL_CC_NAME, cc);
    }

    public String getCc() {
        return getAttribute(EMAILAttribute.EMAIL_CC_NAME);
    }

    public void setBcc(String bcc) {
        setAttribute(SMTPInput.SMTP_BCC, EMAILAttribute.EMAIL_BCC_NAME, bcc);
    }

    public String getBcc() {
        return getAttribute(EMAILAttribute.EMAIL_BCC_NAME);
    }

    public String getUse() {
        return getAttribute(EMAILAttribute.EMAIL_USE);
    }

    public void setUse(String val) {
        setAttribute(SMTPInput.SMTP_USE, EMAILAttribute.EMAIL_USE, val);
    }

    public String getEncodingStyle() {
        return getAttribute(EMAILAttribute.EMAIL_ENCODING_STYLE);
    }

    public void setEncodingStyle(String val) {
        setAttribute(SMTPInput.SMTP_ENCODING_STYLE, EMAILAttribute.EMAIL_ENCODING_STYLE, val);
    }

    public String getNewsgroups() {
        return getAttribute(EMAILAttribute.EMAIL_NEWSGROUP);
    }

    public void setNewsgroups(String newsGroup) {
        setAttribute(SMTPInput.SMTP_NEWSGROUP, EMAILAttribute.EMAIL_NEWSGROUP, newsGroup);
    }

    public String getSendOption() {
        return getAttribute(EMAILAttribute.EMAIL_SEND_OPTION);
    }

    public void setSendOption(String val) {
        setAttribute(SMTPInput.SMTP_SENDOPTION, EMAILAttribute.EMAIL_SEND_OPTION, val);
    }

    public String getEmbedImagesInHtml() {
        return getAttribute(EMAILAttribute.EMAIL_EMBED_HTML_IMG);
    }

    public void setEmbedImagesInHtml(String val) {
        setAttribute(SMTPInput.SMTP_EMBEDIMGHTML, EMAILAttribute.EMAIL_EMBED_HTML_IMG, val);
    }

    public String getHandleNMAttachments() {
        return getAttribute(EMAILAttribute.EMAIL_HANDLE_NM_ATT);
    }

    public void setHandleNMAttachments(String val) {
        setAttribute(SMTPInput.SMTP_HANDLENMATT, EMAILAttribute.EMAIL_HANDLE_NM_ATT, val);
    }

    public boolean isEmbedImagesInHtml() {
        String s = getAttribute(EMAILAttribute.EMAIL_EMBED_HTML_IMG);
        return s != null && Boolean.parseBoolean(s);
    }

    public boolean isHandleNMAttachments() {
        String s = getAttribute(EMAILAttribute.EMAIL_HANDLE_NM_ATT);
        return s != null && Boolean.parseBoolean(s);
    }

}
