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

/**
 *
 */
package org.netbeans.modules.bpel.model.api;


/**
 * <pre>
 *  &lt;xsd:element name="messageExchanges" type="tMessageExchanges"/>
 *   &lt;xsd:complexType name="tMessageExchanges">
 *       &lt;xsd:complexContent>
 *           &lt;xsd:extension base="tExtensibleElements">
 *               &lt;xsd:sequence>
 *                   &lt;xsd:element ref="messageExchange" minOccurs="1" maxOccurs="unbounded"/>
 *               &lt;/xsd:sequence>
 *           &lt;/xsd:extension>
 *       &lt;/xsd:complexContent>
 *   &lt;/xsd:complexType>
 * </pre>
 * @author ads
 *
 */
public interface MessageExchangeContainer extends ExtensibleElements {

    /**
     * @return MessageExchange children for this container.
     */
    MessageExchange[] getMessageExchanges();

    /**
     * Getter for <code>i</code>-th MessageExchange child.
     * @param i Index for child.
     * @return MessageExchange object on the i-th position.
     */
    MessageExchange getMessageExchange( int i );

    /**
     * Setter for <code>i</code>-th MessageExchange child.
     * @param exchange New MessageExchange object.
     * @param i Index for child.
     */
    void setMessageExchange( MessageExchange exchange , int i );
    
    /**
     * Inserts new MessageExchange on the i-th position.
     * @param exchange New MessageExchange object.
     * @param i Index for child.
     */
    void insertMessageExchange( MessageExchange exchange, int i );
    
    /**
     * Removes i-th child.
     * @param i  Index for child.
     */
    void removeMessageExchange( int i );
    
    /**
     * Adds new MessageExchange child to the end of MessageExchange children list.
     * @param exchange New MessageExchange object.
     */
    void addMessageExchange( MessageExchange exchange );
    
    /**
     * Setter for array MessageExchange children.
     * @param exchanges New array of children.
     */
    void setMessageExchanges( MessageExchange[] exchanges );
    
    /**
     * @return Size of MessageExchange children.
     */
    int sizeOfMessageExchanges();
}
