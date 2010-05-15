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
package org.netbeans.modules.wsdlextensions.email.smtp;

/**
 *
 * Represents the attachment element under the wsdl port for SMTP binding
 */
public interface SMTPAttachment extends SMTPComponent {

    // Attribute names
    public static final String ATTR_ATTACHMENT_CONTENT_PART = "attachmentContentPart";
    public static final String ATTR_ATTACHMENT_FILE_NAME_PART = "attachmentFileNamePart";
    public static final String ATTR_READ_FROM_FILE = "readFromFile";
    public static final String ATTR_CONTENT_TYPE = "contentType";
    public static final String ATTR_DISPOSITION = "disposition";

    public void setAttachmentContentPart(String attachmentContentPart);

    public String getAttachmentContentPart();

    public void setAttachmentFileNamePart(String attachmentFileNamePart);

    public String getAttachmentFileNamePart();

    public void setReadFromFile(String filePath);

    public String getReadFromFile();

    public void setContentType(String contentType);

    public String getContentType();

    public void setDisposition(String disposition);

    public String getDisposition();
}
