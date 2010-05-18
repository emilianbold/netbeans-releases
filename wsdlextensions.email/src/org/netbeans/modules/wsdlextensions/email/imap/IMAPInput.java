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

package org.netbeans.modules.wsdlextensions.email.imap;

/**
* @author Sainath Adiraju
*/
	public interface IMAPInput extends IMAPComponent{

        public static final String EMAIL_MESSAGE = "message";
        public static final String EMAIL_SUBJECT = "subject";
		public static final String EMAIL_FROM = "from";
		public static final String EMAIL_CHAR_SET = "charset";
        public static final String EMAIL_TO = "to";
        public static final String EMAIL_CC = "cc";
        public static final String EMAIL_BCC = "bcc";
        public static final String EMAIL_USE = "use";
        public static final String EMAIL_ENCODINGSTYLE = "encodingStyle";
        public static final String EMAIL_NEWSGROUP = "newsgroups";
        public static final String EMAIL_HANDLENMATT = "handleNMAttachments";
        public static final String EMAIL_SAVEATTDIR = "saveAttachmentsToDir";
	
        public String getCharset();
        public void setCharset(String val);

        public String getFrom();
        public void setFrom(String from);

        public String getMessage();
        public void setMessage(String message);

        public String getSubject();
        public void setSubject(String subject);

        public void setTo(String to);
        public String getTo();

        public void setCc(String cc);
        public String getCc();

        public void setBcc(String bcc);
        public String getBcc();

        public String getUse();
        public void setUse(String val);

        public String getEncodingStyle();
        public void setEncodingStyle(String val);

        public String getNewsgroups();
        public void setNewsgroups(String newsgroups);

        public boolean getHandleNMAttachments();
        public void setHandleNMAttachments(boolean val);

        public String getSaveAttachmentsToDir();
        public void setSaveAttachmentsToDir(String val);
        
}

