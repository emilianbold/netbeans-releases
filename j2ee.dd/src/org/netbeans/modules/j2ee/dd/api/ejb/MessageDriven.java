/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.api.ejb;

// 
// This interface has all of the bean info accessor methods.
// 
import org.netbeans.api.web.dd.common.VersionNotSupportedException;

public interface MessageDriven extends Ejb {
    
        public static final String MESSAGING_TYPE = "MessagingType";	// NOI18N
	public static final String TRANSACTION_TYPE = "TransactionType";	// NOI18N
	public static final String MESSAGE_DESTINATION_TYPE = "MessageDestinationType";	// NOI18N
	public static final String MESSAGE_DESTINATION_LINK = "MessageDestinationLink";	// NOI18N
	public static final String ACTIVATION_CONFIG = "ActivationConfig";	// NOI18N
        public static final String TRANSACTION_TYPE_BEAN = "Bean"; // NOI18N
        public static final String TRANSACTION_TYPE_CONTAINER = "Container"; // NOI18N
    
        public void setTransactionType(String value);

        public String getTransactionType();
        
        public void setMessageSelector(String value) throws VersionNotSupportedException;

        public String getMessageSelector() throws VersionNotSupportedException;

        public void setAcknowledgeMode(String value) throws VersionNotSupportedException;

        public String getAcknowledgeMode() throws VersionNotSupportedException;
        
        public void setMessageDrivenDestination(MessageDrivenDestination value) throws VersionNotSupportedException;

        public MessageDrivenDestination getMessageDrivenDestination() throws VersionNotSupportedException;
        
        //2.1
        public void setMessagingType(java.lang.String value) throws VersionNotSupportedException;

	public java.lang.String getMessagingType() throws VersionNotSupportedException;
        
        public void setMessageDestinationType(java.lang.String value) throws VersionNotSupportedException;

	public java.lang.String getMessageDestinationType() throws VersionNotSupportedException;
                
        public void setMessageDestinationLink(java.lang.String value) throws VersionNotSupportedException;

	public java.lang.String getMessageDestinationLink() throws VersionNotSupportedException;
        
        public void setActivationConfig(ActivationConfig value) throws VersionNotSupportedException;

	public ActivationConfig getActivationConfig() throws VersionNotSupportedException;
        
        public ActivationConfig newActivationConfig() throws VersionNotSupportedException;
                
        
}
 
