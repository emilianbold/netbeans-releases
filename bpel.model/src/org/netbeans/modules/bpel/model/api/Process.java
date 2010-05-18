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

package org.netbeans.modules.bpel.model.api;

import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.AtomicTxType;
import org.netbeans.modules.bpel.model.api.support.TBoolean;

/**
 * <p>
 * Java class for tProcess complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 *   &lt;xsd:complexType name="tProcess">
 *       &lt;xsd:complexContent>
 *           &lt;xsd:extension base="tExtensibleElements">
 *               &lt;xsd:sequence>
 *                   &lt;xsd:element ref="extensions" minOccurs="0"/>
 *                   &lt;xsd:element ref="import" minOccurs="0" maxOccurs="unbounded"/>
 *                   &lt;xsd:element ref="partnerLinks" minOccurs="0"/>
 *                   &lt;xsd:element ref="messageExchanges" minOccurs="0"/>
 *                   &lt;xsd:element ref="variables" minOccurs="0"/>
 *                   &lt;xsd:element ref="correlationSets" minOccurs="0"/>
 *                   &lt;xsd:element ref="faultHandlers" minOccurs="0"/>
 *                  &lt;xsd:element ref="eventHandlers" minOccurs="0"/>
 *                   &lt;xsd:group ref="activity" minOccurs="1"/>
 *               &lt;/xsd:sequence>
 *               &lt;xsd:attribute name="name" type="xsd:NCName" use="required"/>
 *               &lt;xsd:attribute name="targetNamespace" type="xsd:anyURI" use="required"/>
 *               &lt;xsd:attribute name="queryLanguage" type="xsd:anyURI" default="urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0"/>
 *               &lt;xsd:attribute name="expressionLanguage" type="xsd:anyURI" default="urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0"/>
 *               &lt;xsd:attribute name="suppressJoinFailure" type="tBoolean" default="no"/>
 *               &lt;xsd:attribute name="exitOnStandardFault" type="tBoolean" default="no"/>
 *           &lt;/xsd:extension>
 *       &lt;/xsd:complexContent>
 *   &lt;/xsd:complexType>
 * </pre>
 */
public interface Process extends JoinFailureSuppressor, NamedElement, BaseScope, ExpressionLanguageSpec, QueryLanguageSpec {

    /**
     * targetNamespace attribute name.
     */
    String TARGET_NAMESPACE = "targetNamespace"; // NOI18N

    /**
     * atomic attribute's name.
     */
    String ATOMIC = "atomic"; // NOI18N
    String ATOMIC_TX_TYPE = "atomicTxType"; // NOI18N

    /**
     * persistenceOptOut attribute's name
     */
    String PERSISTENCE_OPT_OUT = "persistenceOptOut"; // NOI18N
    
    /**
     * waitingRequestLifeSpan attribute's name
     */
    String WAITING_REQUEST_LIFE_SPAN = "waitingRequestLifeSpan"; // NOI18N

    /**
     * Gets the value of the targetNamespace property.
     * 
     * @return possible object is {@link String }
     */
    String getTargetNamespace();

    /**
     * Sets the value of the targetNamespace property.
     * 
     * @param value
     *            allowed object is {@link String }
     * @throws VetoException {@link VetoException}
     *             will be thrown if <code>value</code> if not acceptable as
     *             targetNamespace attribute here.
     */
    void setTargetNamespace( String value ) throws VetoException;

    /**
     * Removes expressionLanguage attribute.
     */
    void removeExpressionLanguage();

    /**
     * Gets the "extensions" entity child .
     * 
     * @return possible object is {@link Extensions }
     */
    ExtensionContainer getExtensionContainer();

    /**
     * Sets the "extensions" entity child .
     * 
     * @param value
     *            allowed object is {@link Extensions }
     */
    void setExtensionContainer( ExtensionContainer value );

    /**
     * Removes "extensions" entity as children if it exists.
     */
    void removeExtensionContainer();

    /**
     * @return Array of "import" children in this process.
     */
    Import[] getImports();

    /**
     * Adds new import <code>imp</code> to this parent.
     * 
     * @param imp
     *            New child for addition.
     */
    void addImport( Import imp );

    /**
     * Replace <code>i</code>-th place impport with new import
     * <code>imp</code>.
     * 
     * @param imp
     *            New child for setting.
     * @param i
     *            Index in children list.
     */
    void setImport( Import imp, int i );

    /**
     * Inserts new import <code>imp</code> at the <code>i</code>-th place.
     * 
     * @param imp
     *            New child for addition.
     * @param i
     *            Index in children list.
     */
    void insertImport( Import imp, int i );

    /**
     * Removes <code>i</code>-th import from children list.
     * 
     * @param i Index in children list.
     */
    void removeImport( int i );

    /**
     * Set new list of children of imports.
     * 
     * @param imports
     *            New array of children.
     */
    void setImports( Import[] imports );

    /**
     * @param i Index in children list.
     * @return <code>i</code>-th child in imports array.
     */
    Import getImport( int i );

    /**
     * @return size of "imports" children.
     */
    int sizeOfImports();
    
    /**
     * Indicates if the process is atomic. 
     * It isn't an extension (not standard) attribute of the process.
     * 
     * atomic=true - acknowledges Message(MEx) to its partner immediately 
     * after executing Receive activity. 
     * 
     * atomic=false - acknowledges Message(MEx) to its partner 
     * when the instance completes.
     * 
     * @return the value of the atomic flag
     */
    TBoolean isAtomic();

    /**
     * Set atomic attribute. See the description of the isAtomic() method.
     * @param value
     */
    void setAtomic(TBoolean value);

    // AtomicTxType 
    AtomicTxType getAtomicTxType();
    void removeAtomicTxType();
    void setAtomicTxType(AtomicTxType value);

    /**
     * It is process level ignoreMissingFromData attribute with the similar meaning as 
     * ignoreMissingFromData attribute at the copy level but for the whole process.
     * @see org.netbeans.modules.bpel.model.api.Copy#getIgnoreMissingFromData()
     * 
     * if "ignoreMissingFromData" at process level is yes then copys level ignoreMissingFromData attribute value is ignored
     * if "ignoreMissingFromData" at process level is no or isn't set then copys level ignoreMissingFromData attribute value is used
     *
     * @return "ignoreMissingFromData" attribute value.
     */
    TBoolean getIgnoreMissingFromData();
    
    /**
     * Setter for "ignoreMissingFromData" attribute.
     * @param value New "ignoreMissingFromData" attribute value.
     */
    void setIgnoreMissingFromData( TBoolean value );
    
    /**
     * Removes "ignoreMissingFromData" attribute.
     */
    void removeIgnoreMissingFromData();
    
    /**
     * Getter for "persistenceOptOut" attribute.
     * @return "persistenceOptOut" attribute value.
     */
    TBoolean isPersistenceOptOut();
    
    /**
     * Setter for "persistenceOptOut" attribute.
     * @param value New "persistenceOptOut" attribute value.
     */
    void setPersistenceOptOut( TBoolean value );
    
    /**
     * Removes "persistenceOptOut" attribute.
     */
    void removePersistenceOptOut();
    
    /**
     * 
     * @param value New "waitingRequestLifeSpan" attribute value
     */
    void setWaitingRequestLifeSpan(Integer value) throws VetoException;

    /**
     * 
     * @return "waitingRequestLifeSpan" attriubute value
     */
    Integer getWaitingRequestLifeSpan();
    
    /**
     * Removes "waitingRequestLifeSpan" attribute
     */
    void removeWaitingRequestLifeSpan();
    
    
}
