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

import org.netbeans.modules.xml.xam.Referenceable;




/**
 * <pre> 
 * &lt;xs:element name="attribute-set" substitutionGroup="xsl:declaration">
 *      &lt;xs:complexType>
 *          &lt;xs:complexContent>
 *              &lt;xs:extension base="xsl:element-only-versioned-element-type">
 *                  &lt;xs:sequence minOccurs="0" maxOccurs="unbounded">
 *                      &lt;xs:element ref="xsl:attribute"/>
 *                  &lt;/xs:sequence>
 *                  &lt;xs:attribute name="name" type="xsl:QName" use="required"/>
 *                  &lt;xs:attribute name="use-attribute-sets" type="xsl:QNames" default=""/>
 *              &lt;/xs:extension>
 *          &lt;/xs:complexContent>
 *      &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 *
 * @author ads
 *
 */
public interface AttributeSet extends QualifiedNameable, Referenceable, 
    Declaration, UseAttributesSetsSpec 
{
    String ATTRIBUTE_PROPERTY = "attribute";            // NOI18N

    /**
     * @return attributes children for this stylesheet.
     * Note that resulting collection is unmodifiable. 
     */
    List<Attribute> getAttributes();
    
    /**
     * Add new attribute <code>attr</code> element at <code>position</code>. 
     * @param attr new attribute element.
     * @param position position for new element.
     */
    void addAttribute(Attribute attr, int position);
    
    /**
     * Append new attribute element.
     * @param attr new attribute child element for appending.
     */
    void appendAttribute(Attribute attr);
    
    /**
     * Removes existing <code>attr</code> import child element.
     * @param attr attribute child element.
     */
    void removeAttribute(Attribute attr);
}
