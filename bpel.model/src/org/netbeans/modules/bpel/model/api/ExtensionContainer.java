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
 * @author ads
 *         <p>
 *         Java class for tExtensions complex type.
 *         <p>
 *         The following schema fragment specifies the expected content
 *         contained within this class.
 *
 * <pre>
 *   &lt;xsd:element name="extensions" type="tExtensions"/>
 *   &lt;xsd:complexType name="tExtensions">
 *       &lt;xsd:complexContent>
 *           &lt;xsd:extension base="tExtensibleElements">
 *               &lt;xsd:sequence>
 *                   &lt;xsd:element ref="extension" minOccurs="1" maxOccurs="unbounded"/>
 *               &lt;/xsd:sequence>
 *           &lt;/xsd:extension>
 *       &lt;/xsd:complexContent>
 *   &lt;/xsd:complexType>
 * </pre>
 */
public interface ExtensionContainer extends ExtensibleElements {

    /**
     * @return Children array of Extensions.
     */
    Extension[] getExtensions();

    /**
     * Returns <code>i</code>-th Extension child element.
     * 
     * @param i
     *            Index in Extensions children array.
     * @return <code>i</code>-th child Extension.
     */
    Extension getExtension( int i );

    /**
     * Removes <code>i</code>-th Extension child element.
     * 
     * @param i
     *            Index in Extensions children array.
     */
    void removeExtension( int i );

    /**
     * Add new Extension element to the end Extensions children array.
     * 
     * @param extension New Extension element. 
     */
    void addExtension( Extension extension );

    /**
     * Set New Extension element to the <code>i</code>-th position.
     * 
     * @param extension
     *            New Extension element.
     * @param i
     *            Index in Extensions children array.
     */
    void setExtension( Extension extension, int i );

    /**
     * Insert New Extension element to the <code>i</code>-th position.
     * 
     * @param extension
     *            New Extension element.
     * @param i
     *            Index in Extensions children array.
     */
    void insertExtension( Extension extension, int i );

    /**
     * Set new children array of Extensions.
     * 
     * @param extensions New array of extansions chldren. 
     */
    void setExtensions( Extension[] extensions );

    /**
     * @return size of children array of Extensions.
     */
    int sizeOfExtensions();
}
