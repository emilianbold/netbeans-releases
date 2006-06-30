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
/*
 * MessageSecurity.java
 *
 * Created on November 18, 2004, 4:21 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author Nitya Doraisamy
 */
public interface MessageSecurity extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String MESSAGE = "Message";	// NOI18N
    public static final String REQUEST_PROTECTION = "RequestProtection";	// NOI18N
    public static final String REQUESTPROTECTIONAUTHSOURCE = "RequestProtectionAuthSource";	// NOI18N
    public static final String REQUESTPROTECTIONAUTHRECIPIENT = "RequestProtectionAuthRecipient";	// NOI18N
    public static final String RESPONSE_PROTECTION = "ResponseProtection";	// NOI18N
    public static final String RESPONSEPROTECTIONAUTHSOURCE = "ResponseProtectionAuthSource";	// NOI18N
    public static final String RESPONSEPROTECTIONAUTHRECIPIENT = "ResponseProtectionAuthRecipient";	// NOI18N

        
    public Message [] getMessage();
    public Message  getMessage(int index);
    public void setMessage(Message [] value);
    public void setMessage(int index, Message  value);
    public int addMessage(Message  value);
    public int removeMessage(Message  value);
    public int sizeMessage();
    public Message  newMessage();
    
    /** Setter for request-protection property
     * @param value property value
     */
    public void setRequestProtection(boolean value);
    /** Check for request-protection property.
     * @return property value
     */
    public boolean isRequestProtection();
    /** Setter for response-protection property
     * @param value property value
     */
    public void setResponseProtection(boolean value);
    /** Getter for response-protection property.
     * @return property value
     */
    public boolean isResponseProtection();
    
    public void setRequestProtectionAuthSource(java.lang.String value);
    public java.lang.String getRequestProtectionAuthSource();
    
    public void setRequestProtectionAuthRecipient(java.lang.String value);
    public java.lang.String getRequestProtectionAuthRecipient();
    
    public void setResponseProtectionAuthSource(java.lang.String value);
    public java.lang.String getResponseProtectionAuthSource();
    
    public void setResponseProtectionAuthRecipient(java.lang.String value);
    public java.lang.String getResponseProtectionAuthRecipient();
    
    
}
