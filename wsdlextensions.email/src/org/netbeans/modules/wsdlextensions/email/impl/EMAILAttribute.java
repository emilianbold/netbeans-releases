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

package org.netbeans.modules.wsdlextensions.email.impl;

import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * @author Sainath Adiraju
 *
 */
public enum EMAILAttribute implements Attribute {

	EMAIL_MESSAGE_NAME("message"),
	EMAIL_SUBJECT_NAME("subject"),
	EMAIL_FROM_NAME("from"),
	EMAIL_CHARSET_NAME("charset"),
	EMAIL_TO_NAME("to"),
	EMAIL_CC_NAME("cc"),
	EMAIL_BCC_NAME("bcc"),
	EMAIL_USE("use"),
    EMAIL_ENCODING_STYLE("encodingStyle"),
	EMAIL_RECEIVER_LOCATIONURL("location"),
	EMAIL_SERVER_NAME("emailServer"),
	EMAIL_NEWSGROUP("newsgroups"),
	EMAIL_HANDLE_NM_ATT("handleNMAttachments"),
	EMAIL_SAVE_ATT_DIR("saveAttachmentsToDir"),
	EMAIL_PORT("port"),
	EMAIL_USERNAME("userName"),
	EMAIL_PASSWORD("password"),
	EMAIL_USESSL("useSSL"),
	EMAIL_MAIL_FOLDER("mailFolder"),
	EMAIL_MAXMSG_COUNT("maxMessageCount"),
	EMAIL_MSGACK_MODE("messageAckMode"),
	EMAIL_MSGACK_OPERATION("messageAckOperation"),
    EMAIL_EMBED_HTML_IMG("embedImagesInHtml"),
    EMAIL_SEND_OPTION("sendOption"),
	EMAIL_POLLING_INTERVAL("pollingInterval"),
    EMAIL_ATTACHMENT_PART_NAME("partName"),
    EMAIL_ATTACHMENT_CONTENT_TYPE("contentType"),
    EMAIL_ATTACHMENT_DISPOSITION("disposition"),
    EMAIL_ATTACHMENT_READ_FROM_FILE("readFromFile");


	private String name;
    private Class type;
    private Class subtype;
    
    EMAILAttribute(String name) {
        this(name, String.class);
    }
    
    EMAILAttribute(String name, Class type) {
        this(name, type, null);
    }
    
    EMAILAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    public String toString() {
		return name;
	}
    
    public Class getType() {
        return type;
    }
    
    public String getName() {
		return name;
	}
    
    public Class getMemberType() { return subtype; }
}
