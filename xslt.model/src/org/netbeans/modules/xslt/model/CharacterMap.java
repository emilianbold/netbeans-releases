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
package org.netbeans.modules.xslt.model;

import java.util.List;


/**
 * <pre>
 * &lt;xs:element name="character-map" substitutionGroup="xsl:declaration">
 *      &lt;xs:complexType>
 *          &lt;xs:complexContent>
 *              &lt;xs:extension base="xsl:element-only-versioned-element-type">
 *                  &lt;xs:sequence>
 *                      &lt;xs:element ref="xsl:output-character" minOccurs="0" maxOccurs="unbounded"/>
 *                  &lt;/xs:sequence>
 *                  &lt;xs:attribute name="name" type="xsl:QName" use="required"/>
 *                  &lt;xs:attribute name="use-character-maps" type="xsl:QNames" default=""/>
 *              &lt;/xs:extension>
 *          &lt;/xs:complexContent>
 *      &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 * 
 * @author ads
 *
 */
public interface CharacterMap extends Declaration, QualifiedNameable, 
    UseCharacterMapsSpec 
{

    /**
     * @return unmodifiable list of OutputCharacter components
     */
    List<OutputCharacter> getOutputCharacters();
    
    /**
     * Append <code>outputCharacter</code> component to the end of children list.
     * @param outputCharacter new component
     */
    void appendOutputCharacter( OutputCharacter outputCharacter );
    
    /**
     * Insert <code>outputCharacter</code> at <code>position</code> in children list.
     * @param outputCharacter new component
     * @param position index in children list
     */
    void addOutputCharacter( OutputCharacter outputCharacter , int position );
    
    /**
     * Removes <code>outputCharacter</code> child.
     * @param outputCharacter child component
     */
    void removeOutputCharacter( OutputCharacter outputCharacter );
}
