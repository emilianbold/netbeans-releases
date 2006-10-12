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

package org.netbeans.modules.j2ee.dd.api.ejb;

//
// This interface has all of the bean info accessor methods.
//
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

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
        public void setMessagingType(String value) throws VersionNotSupportedException;

	public String getMessagingType() throws VersionNotSupportedException;
        
        public void setMessageDestinationType(String value) throws VersionNotSupportedException;

	public String getMessageDestinationType() throws VersionNotSupportedException;
                
        public void setMessageDestinationLink(String value) throws VersionNotSupportedException;

	public String getMessageDestinationLink() throws VersionNotSupportedException;
        
        public void setActivationConfig(ActivationConfig value) throws VersionNotSupportedException;

	public ActivationConfig getActivationConfig() throws VersionNotSupportedException;
        
        public ActivationConfig newActivationConfig() throws VersionNotSupportedException;

        // EJB 3.0
        
	void setMappedName(String value) throws VersionNotSupportedException;
	String getMappedName() throws VersionNotSupportedException;
	void setTimeoutMethod(NamedMethod valueInterface) throws VersionNotSupportedException;
	NamedMethod getTimeoutMethod() throws VersionNotSupportedException;
	void setAroundInvoke(int index, AroundInvoke valueInterface) throws VersionNotSupportedException;
	AroundInvoke getAroundInvoke(int index) throws VersionNotSupportedException;
	int sizeAroundInvoke() throws VersionNotSupportedException;
	void setAroundInvoke(AroundInvoke[] value) throws VersionNotSupportedException;
	AroundInvoke[] getAroundInvoke() throws VersionNotSupportedException;
	int addAroundInvoke(AroundInvoke valueInterface) throws VersionNotSupportedException;
	int removeAroundInvoke(AroundInvoke valueInterface) throws VersionNotSupportedException;
	void setPersistenceContextRef(int index, PersistenceContextRef valueInterface) throws VersionNotSupportedException;
	PersistenceContextRef getPersistenceContextRef(int index) throws VersionNotSupportedException;
	int sizePersistenceContextRef() throws VersionNotSupportedException;
	void setPersistenceContextRef(PersistenceContextRef[] value) throws VersionNotSupportedException;
	PersistenceContextRef[] getPersistenceContextRef() throws VersionNotSupportedException;
	int addPersistenceContextRef(PersistenceContextRef valueInterface) throws VersionNotSupportedException;
	int removePersistenceContextRef(PersistenceContextRef valueInterface) throws VersionNotSupportedException;
	void setPersistenceUnitRef(int index, PersistenceUnitRef valueInterface) throws VersionNotSupportedException;
	PersistenceUnitRef getPersistenceUnitRef(int index) throws VersionNotSupportedException;
	int sizePersistenceUnitRef() throws VersionNotSupportedException;
	void setPersistenceUnitRef(PersistenceUnitRef[] value) throws VersionNotSupportedException;
	PersistenceUnitRef[] getPersistenceUnitRef() throws VersionNotSupportedException;
	int addPersistenceUnitRef(PersistenceUnitRef valueInterface) throws VersionNotSupportedException;
	int removePersistenceUnitRef(PersistenceUnitRef valueInterface) throws VersionNotSupportedException;
	void setPostConstruct(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException;
	LifecycleCallback getPostConstruct(int index) throws VersionNotSupportedException;
	int sizePostConstruct() throws VersionNotSupportedException;
	void setPostConstruct(LifecycleCallback[] value) throws VersionNotSupportedException;
	LifecycleCallback[] getPostConstruct() throws VersionNotSupportedException;
	int addPostConstruct(LifecycleCallback valueInterface) throws VersionNotSupportedException;
	int removePostConstruct(LifecycleCallback valueInterface) throws VersionNotSupportedException;
	void setPreDestroy(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException;
	LifecycleCallback getPreDestroy(int index) throws VersionNotSupportedException;
	int sizePreDestroy() throws VersionNotSupportedException;
	void setPreDestroy(LifecycleCallback[] value) throws VersionNotSupportedException;
	LifecycleCallback[] getPreDestroy() throws VersionNotSupportedException;
	int addPreDestroy(LifecycleCallback valueInterface) throws VersionNotSupportedException;
	int removePreDestroy(LifecycleCallback valueInterface) throws VersionNotSupportedException;
	NamedMethod newNamedMethod() throws VersionNotSupportedException;
	AroundInvoke newAroundInvoke() throws VersionNotSupportedException;
	PersistenceContextRef newPersistenceContextRef() throws VersionNotSupportedException;
	PersistenceUnitRef newPersistenceUnitRef() throws VersionNotSupportedException;
	LifecycleCallback newLifecycleCallback() throws VersionNotSupportedException;

        }
 
