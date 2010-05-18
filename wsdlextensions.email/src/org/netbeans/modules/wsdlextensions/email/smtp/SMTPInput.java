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

package org.netbeans.modules.wsdlextensions.email.smtp;

/**
* @author Sainath Adiraju
*/
	public interface SMTPInput extends SMTPComponent{

        public static final String SMTP_MESSAGE = "message";
        public static final String SMTP_SUBJECT = "subject";
		public static final String SMTP_FROM = "from";
		public static final String SMTP_CHARSET = "charset";
        public static final String SMTP_TO = "to";
        public static final String SMTP_CC = "cc";
        public static final String SMTP_BCC = "bcc";
        public static final String SMTP_USE = "use";
        public static final String SMTP_NEWSGROUP = "newsgroups";
		public static final String SMTP_SENDOPTION = "sendOption";
		public static final String SMTP_EMBEDIMGHTML = "embedImagesInHtml";
		public static final String SMTP_HANDLENMATT = "handleNMAttachments";
        public static final String SMTP_ENCODING_STYLE = "encodingStyle";
	
		public  String getCharset(); 	
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
        
        public String getSendOption();       
        public void setSendOption(String val);        

        public boolean isEmbedImagesInHtml();
        public String getEmbedImagesInHtml();
        public void setEmbedImagesInHtml(String val);

        public boolean isHandleNMAttachments();
        public String getHandleNMAttachments();
        public void setHandleNMAttachments(String val);
		
}

