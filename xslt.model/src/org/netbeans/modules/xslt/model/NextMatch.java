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
 * &lt;xs:element name="next-match" substitutionGroup="xsl:instruction">
 *      &lt;xs:complexType>
 *          &lt;xs:complexContent>
 *              &lt;xs:extension base="xsl:element-only-versioned-element-type">
 *                  &lt;xs:choice minOccurs="0" maxOccurs="unbounded">
 *                      &lt;xs:element ref="xsl:with-param"/>
 *                      &lt;xs:element ref="xsl:fallback"/>
 *                  &lt;/xs:choice>
 *              &lt;/xs:extension>
 *          &lt;/xs:complexContent>
 *      &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 * @author ads
 *
 */
public interface NextMatch extends Instruction {
    
    String CHILD_ELEMENTS = "fallback_or_with-param";        // NOI18N 

    /**
     * @return unmodifiable collection of children elements
     */
    List<NextMatchChild> getChildElements();
    
    /**
     * Append new <code>child</code> element at the end of children list.
     * @param child new child component
     */
    void appendChildElement( NextMatchChild child );
    
    /**
     * Insert new <code>child</code> element at <code>position</code>.
     * @param child new child element
     * @param position index in children list
     */
    void addChildElement( NextMatchChild child , int position );
    
    /**
     * Removes <code>child</code> from chilren list.
     * @param child child component
     */
    void removeChildElement( NextMatchChild child );
}
