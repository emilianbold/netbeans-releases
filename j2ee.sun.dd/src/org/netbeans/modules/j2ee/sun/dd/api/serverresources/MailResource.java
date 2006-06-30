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
 * MailResource.java
 *
 * Created on November 21, 2004, 3:00 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;
/**
 * @author Nitya Doraisamy
 */
public interface MailResource {

        public static final String JNDINAME = "JndiName";	// NOI18N
	public static final String STOREPROTOCOL = "StoreProtocol";	// NOI18N
	public static final String STOREPROTOCOLCLASS = "StoreProtocolClass";	// NOI18N
	public static final String TRANSPORTPROTOCOL = "TransportProtocol";	// NOI18N
	public static final String TRANSPORTPROTOCOLCLASS = "TransportProtocolClass";	// NOI18N
	public static final String HOST = "Host";	// NOI18N
	public static final String USER = "User";	// NOI18N
	public static final String FROM = "From";	// NOI18N
	public static final String DEBUG = "Debug";	// NOI18N
	public static final String ENABLED = "Enabled";	// NOI18N
	public static final String DESCRIPTION = "Description";	// NOI18N
	public static final String PROPERTY = "PropertyElement";	// NOI18N
        
	/** Setter for jndi-name property
        * @param value property value
        */
	public void setJndiName(java.lang.String value);
        /** Getter for jndi-name property
        * @return property value
        */
	public java.lang.String getJndiName();
        /** Setter for store-protocol property
        * @param value property value
        */
	public void setStoreProtocol(java.lang.String value);
        /** Getter for store-protocol property
        * @param value property value
        */
	public java.lang.String getStoreProtocol();
        /** Setter for store-protocol-class property
        * @param value property value
        */
	public void setStoreProtocolClass(java.lang.String value);
        /** Getter for store-protocol-class property
        * @param value property value
        */
	public java.lang.String getStoreProtocolClass();
        /** Setter for transport-protocol property
        * @param value property value
        */
	public void setTransportProtocol(java.lang.String value);
        /** Getter for transport-protocol property
        * @param value property value
        */
	public java.lang.String getTransportProtocol();
        /** Setter for transport-protocol-class property
        * @param value property value
        */
	public void setTransportProtocolClass(java.lang.String value);
        /** Getter for transport-protocol-class property
        * @param value property value
        */
	public java.lang.String getTransportProtocolClass();
        /** Setter for host property
        * @param value property value
        */
	public void setHost(java.lang.String value);
        /** Getter for host property
        * @param value property value
        */
	public java.lang.String getHost();
        /** Setter for user property
        * @param value property value
        */
	public void setUser(java.lang.String value);
        /** Getter for user property
        * @param value property value
        */
	public java.lang.String getUser();
        /** Setter for from property
        * @param value property value
        */
	public void setFrom(java.lang.String value);
        /** Getter for from property
        * @param value property value
        */
	public java.lang.String getFrom();
        /** Setter for debug property
        * @param value property value
        */
	public void setDebug(java.lang.String value);
        /** Getter for debug property
        * @param value property value
        */
	public java.lang.String getDebug();
        /** Setter for enabled property
        * @param value property value
        */
	public void setEnabled(java.lang.String value);
        /** Getter for enabled property
        * @param value property value
        */
	public java.lang.String getEnabled();
        /** Setter for description attribute
        * @param value attribute value
        */
	public void setDescription(String value);
        /** Getter for description attribute
        * @return attribute value
        */
	public String getDescription();

	public void setPropertyElement(int index, PropertyElement value);
	public PropertyElement getPropertyElement(int index);
	public int sizePropertyElement();
	public void setPropertyElement(PropertyElement[] value);
	public PropertyElement[] getPropertyElement();
	public int addPropertyElement(PropertyElement value);
	public int removePropertyElement(PropertyElement value);
	public PropertyElement newPropertyElement();

}
