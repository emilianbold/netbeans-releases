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
 * &lt;xs:element name="choose" substitutionGroup="xsl:instruction">
 *      &lt;xs:complexType>
 *          &lt;xs:complexContent>
 *              &lt;xs:extension base="xsl:element-only-versioned-element-type">
 *                  &lt;xs:sequence>
 *                      &lt;xs:element ref="xsl:when" maxOccurs="unbounded"/>
 *                      &lt;xs:element ref="xsl:otherwise" minOccurs="0"/>
 *                  &lt;/xs:sequence>
 *              &lt;/xs:extension>
 *          &lt;/xs:complexContent>
 *      &lt;/xs:complexType>
 * &lt;/xs:element>
 *
 * </pre>
 * @author ads
 *
 */
public interface Choose extends Instruction {

    String WHEN_PROPERTY = "when";              // NOI18N
    
    String OTHERWISE_PROPERTY = "otherwise";    // NOI18N

    /**
     * @return when children for this template.
     */
    List<When> getWhens();
    
    /**
     * Add new <code>when</code> element at <code>position</code>. 
     * @param when new when element.
     * @param position position for new element.
     */
    void addWhen(When when, int position);
    
    /**
     * Append new when element.
     * @param when new when child element for appending.
     */
    void appendWhen(When when);
    
    /**
     * Removes existing <code>when</code> child element.
     * @param when when child element.
     */
    void removeWhen(When when);
    
    /**
     * @return otherwise child element if any.
     */
    Otherwise getOtherwise();
    
    /**
     * Sets new otherwise child element. 
     * @param otherwise new Otherwise element.
     */
    void setOtherwise( Otherwise otherwise );
}
