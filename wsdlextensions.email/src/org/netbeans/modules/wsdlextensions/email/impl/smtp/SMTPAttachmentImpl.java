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

package org.netbeans.modules.wsdlextensions.email.impl.smtp;

import org.netbeans.modules.wsdlextensions.email.smtp.SMTPAttachment;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPComponent;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPQName;

import static org.netbeans.modules.wsdlextensions.email.smtp.SMTPAttachment.*;
import org.w3c.dom.Element;

/**
 *
 * @author Shivanand Kini
 */

public class SMTPAttachmentImpl extends SMTPComponentImpl implements SMTPAttachment {

    public SMTPAttachmentImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SMTPAttachmentImpl(WSDLModel model){
        this(model, createPrefixedElement(SMTPQName.ADDRESS.getQName(), model));
    }
    
    public void accept(SMTPComponent.Visitor visitor) {
        visitor.visit(this);
    }

    public void setReadFromFile(String filePath) {
        setAttribute(ATTR_READ_FROM_FILE, filePath);
    }

    public String getReadFromFile() {
        return getAttribute(ATTR_READ_FROM_FILE);
    }

    public void setContentType(String contentType) {
        setAttribute(ATTR_CONTENT_TYPE, contentType);
    }

    public String getContentType() {
        return getAttribute(ATTR_CONTENT_TYPE);
    }

    public void setDisposition(String disposition) {
        setAttribute(ATTR_DISPOSITION, disposition);
    }

    public String getDisposition() {
        return getAttribute(ATTR_DISPOSITION);
    }

    public void setAttachmentContentPart(String attachmentContentPart) {
        setAttribute(ATTR_ATTACHMENT_CONTENT_PART, attachmentContentPart);
    }

    public String getAttachmentContentPart() {
        return getAttribute(ATTR_ATTACHMENT_CONTENT_PART);
    }

    public void setAttachmentFileNamePart(String attachmentFileNamePart) {
        setAttribute(ATTR_ATTACHMENT_FILE_NAME_PART, attachmentFileNamePart);
    }

    public String getAttachmentFileNamePart() {
        return getAttribute(ATTR_ATTACHMENT_FILE_NAME_PART);
    }

}
