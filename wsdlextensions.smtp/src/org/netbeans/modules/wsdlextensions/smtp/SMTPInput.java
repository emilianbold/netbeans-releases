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

package org.netbeans.modules.wsdlextensions.smtp;

/**
* @author Sainath Adiraju
*/
	public interface  SMTPInput extends SMTPComponent{

        public static final String SMTP_MESSAGE = "messageName";
        public static final String SMTP_SUBJECT = "subjectName";
	public static final String SMTP_FROM = "from";
	public static final String SMTP_CHARSET = "charset";
        public static final String SMTP_TO = "to";
        public static final String SMTP_CC = "cc";
        public static final String SMTP_BCC = "bcc";
        public static final String SMTP_USE = "use";
        public static final String SMTP_ENCSTYLE = "encodingStyle";
	
	public  String getCharSet(); 
		
	public void setCharSet(); 

	public String getFrom();

	public void setFrom(); 

	public String getMessageName();

	public void setMessageName(); 

	public String getSubjectName();

	public void setSubjectName(); 

        public void setTo();
        
        public String  getTo();
        
        public void setCc();
        
        public String  getCc();
        
        public void setBcc();
        
        public String getBcc();
        
        public String getUse();
        
        public void setUse();
        
        public String getEncodingStyle();
        
        public void setEncodingStyle();
        
        

   
}

