/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * MessageSecurityBinding.java
 *
 * Created on November 18, 2004, 4:13 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author Nitya Doraisamy
 */
public interface MessageSecurityBinding extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    public static final String AUTHLAYER = "AuthLayer";	// NOI18N
    public static final String PROVIDERID = "ProviderId";	// NOI18N
    public static final String MESSAGE_SECURITY = "MessageSecurity";	// NOI18N
        
    public MessageSecurity [] getMessageSecurity (); 
    public MessageSecurity  getMessageSecurity (int index);
    public void setMessageSecurity (MessageSecurity [] value);
    public void setMessageSecurity (int index, MessageSecurity  value);
    public int addMessageSecurity (MessageSecurity  value);
    public int removeMessageSecurity (MessageSecurity  value);
    public int sizeMessageSecurity ();
    public MessageSecurity  newMessageSecurity ();
    
    /** Setter for auth-layer attribute
     * @param value attribute value
     */
    public void setAuthLayer(java.lang.String value);
    /** Getter for auth-layer attribute.
     * @return attribute value
     */
    public java.lang.String getAuthLayer();
    /** Setter for provider-id attribute
     * @param value attribute value
     */
    public void setProviderId(java.lang.String value);
    /** Getter for provider-id attribute.
     * @return attribute value
     */
    public java.lang.String getProviderId();

}
