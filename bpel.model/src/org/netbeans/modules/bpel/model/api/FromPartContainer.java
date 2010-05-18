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
 * 
 * <pre>
 *  &lt;xsd:element name="fromParts" type="tFromParts"/>
 *   &lt;xsd:complexType name="tFromParts">
 *       &lt;xsd:complexContent>
 *           &lt;xsd:extension base="tExtensibleElements">
 *               &lt;xsd:sequence>
 *                   &lt;xsd:element ref="fromPart" minOccurs="1" maxOccurs="unbounded"/>
 *               &lt;/xsd:sequence>
 *           &lt;/xsd:extension>
 *       &lt;/xsd:complexContent>
 *   &lt;/xsd:complexType>
 * </pre>
 * @author ads
 */
public interface FromPartContainer extends BpelContainer {

    /**
     * @return FromPart's children array.
     */
    FromPart[] getFromParts();

    /**
     * Getter for <code>i</code>-th FromPart child.
     *
     * @param i
     *            Index in FromPart's children array.
     * @return <code>i</code>-th FromPart child.
     */
    FromPart getFromPart( int i );

    /**
     * Setter for <code>i</code>-th FromPart child.
     *
     * @param part
     *            New FromPart child.
     * @param i
     *            Index in FromPart's children array.
     */
    void setFromPart( FromPart part, int i );

    /**
     * Insert new <code>part</code> inside children list on the <code>i</code>-th
     * place.
     * 
     * @param part
     *            New FromPart child.
     * @param i
     *            Index in FromPart's children array.
     */
    void insertFromPart( FromPart part, int i );

    /**
     * Adds new FromPart child at the end of FromPart's children list.
     * 
     * @param part  New FromPart child.
     */
    void addFromPart( FromPart part );

    /**
     * Set new FromPart's children array.
     * 
     * @param parts
     *            New array.
     */
    void setFromParts( FromPart[] parts );

    /**
     * @return size of FromPart's children array.
     */
    int sizeOfFromParts();

    /**
     * Removes <code>i</code>-th FromPart child.
     * 
     * @param i
     *            Index in FromPart's children array.
     */
    void removeFromPart( int i );
}
