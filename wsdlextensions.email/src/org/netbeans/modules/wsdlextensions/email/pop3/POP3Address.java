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

package org.netbeans.modules.wsdlextensions.email.pop3;


/**
 *
 * Represents the address element under the wsdl port for EMAIL binding
 * @author Sainath Adiraju
*/
	public interface POP3Address extends POP3Component {

		// Attribute names
	   public static final String ATTR_EMAILSERVER = "emailServer";
	   public static final String ATTR_PORT = "port";
	   public static final String ATTR_USERNAME = "userName";
	   public static final String ATTR_PASSWORD = "password";
	   public static final String ATTR_USESSL = "useSSL";
	   //public static final String ATTR_MAIL_FOLDER = "mailFolder";
	   public static final String ATTR_MAXMSG_COUNT = "maxMessageCount";
	   public static final String ATTR_MSGACK_MODE = "messageAckMode";
	   public static final String ATTR_MSGACK_OPERATION = "messageAckOperation";
	   public static final String ATTR_POLLING_INTERVAL = "pollingInterval";

		public void setEmailServer(String val);
		public String getEmailServer();

		public void setPort(String val);
		public String getPort();

		public void setUserName(String val);
		public String getUserName();

		public void setPassword(String val);
		public String getPassword();

		public void setUseSSL(boolean val);
		public boolean getUseSSL();

		//public void setMailFolder(String val);
		//public String getMailFolder();

		public void setMaxMessageCount(String val);
		public String getMaxMessageCount();

		public void setMessageAckMode(String val);
		public String getMessageAckMode();

		public void setMessageAckOperation(String val);
		public String getMessageAckOperation();

		public void setPollingInterval(String val);
		public String getPollingInterval();

}
