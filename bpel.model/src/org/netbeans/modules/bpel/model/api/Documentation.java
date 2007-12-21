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

import org.netbeans.modules.bpel.model.api.events.VetoException;

/**
 * @author ads
 *         <p>
 *         Java class for documentation element declaration.
 *         <p>
 *         The following schema fragment specifies the expected content
 *         contained within this class.
 *
 * <pre>
 *   &lt;xsd:element name="documentation" type="tDocumentation"/>
 *   &lt;xsd:complexType name="tDocumentation" mixed="true">
 *       &lt;xsd:sequence>
 *           &lt;xsd:any processContents="lax" minOccurs="0" maxOccurs="unbounded"/>
 *       &lt;/xsd:sequence>
 *       &lt;xsd:attribute name="source" type="xsd:anyURI"/>
 *       &lt;xsd:attribute ref="xml:lang"/>
 *   &lt;/xsd:complexType>
 * </pre>
 */
public interface Documentation extends BpelContainer, ContentElement {

    String SOURCE = "source";       // NOI18N

    String LANGUAGE = "xml:lang";   //  NOI18N

    /**
     * @return "source" attribute value.
     */
    String getSource();

    /**
     * Set source attribute value to <code>uri</code>.
     * 
     * @param uri New "source" attribute value. 
     * @throws VetoException
     *             Will be thrown if uri is not acceptable as value here.
     */
    void setSource( String uri ) throws VetoException;
    
    /**
     * Removes source attribute.
     */
    void removeSource();

    /**
     * @return "xml:lang" attribute value.
     */
    String getLanguage();

    /**
     * Set "xml:lang" attribute value to <code>lang</code>.
     * 
     * @param lang New language value.
     * @throws VetoException ( @link VetoException ) will be thrown 
     * if new value is not acceptable here. 
     */
    void setLanguage( String lang ) throws VetoException;
    
    /**
     * Removes "xml:lang" attribute.
     */
    void removeLanguage();

}
