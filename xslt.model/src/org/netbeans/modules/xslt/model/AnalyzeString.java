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
 * &lt;xs:element name="analyze-string" substitutionGroup="xsl:instruction">
 *      &lt;xs:complexType>
 *          &lt;xs:complexContent>
 *              &lt;xs:extension base="xsl:element-only-versioned-element-type">
 *                  &lt;xs:sequence>
 *                      &lt;xs:element ref="xsl:matching-substring" minOccurs="0"/>
 *                      &lt;xs:element ref="xsl:non-matching-substring" minOccurs="0"/>
 *                      &lt;xs:element ref="xsl:fallback" minOccurs="0" maxOccurs="unbounded"/>  
 *                  &lt;/xs:sequence>
 *                  &lt;xs:attribute name="select" type="xsl:expression" use="required"/>
 *                  &lt;xs:attribute name="regex" type="xsl:avt" use="required"/>
 *                  &lt;xs:attribute name="flags" type="xsl:avt" default=""/>
 *              &lt;/xs:extension>
 *          &lt;/xs:complexContent>
 *      &lt;/xs:complexType>
 * &lt;/xs:element>
 *
 * </pre>
 * @author ads
 *
 */
public interface AnalyzeString extends SelectSpec, SequenceElement {
    
    String REGEX = "regex";         // NOI18N
    
    String FLAGS = "flags";         // NOI18N
    
    /**
     * @return MatchingSubstring child component
     */
    MatchingSubstring getMatchingSubstring();
    
    /**
     * Set new MatchingSubstring child component.
     * @param child new MatchingSubstring component
     */
    void setMatchingSubstring( MatchingSubstring child );
    
    /**
     * @return NonMatchingSubstring child component
     */
    NonMatchingSubstring getNonMatchingSubstring();
    
    /**
     * Set new NonMatchingSubstring child component.
     * @param child
     */
    void setNonMatchingSubstring( NonMatchingSubstring child );
    
    /**
     * @return unmodifiable list of fallback children components
     */
    List<Fallback> getFallbacks();
    
    /**
     * Append new fallback child in the end of fallback children list.
     * @param fallback new fallback child
     */
    void appendFallback( Fallback fallback );
    
    /**
     * Insert new fallback child element at <code>position</code>.
     * @param fallback new fallback child
     * @param position index in fallback children list
     */
    void addFallback( Fallback fallback , int position );
    
    /**
     * Removes <code>fallback</code> child.
     * @param fallback child component
     */
    void removeFallback( Fallback fallback );
    
    /**
     * @return "regex" attribute value
     */
    AttributeValueTemplate getRegex();
    
    /**
     * Set "regex" attribute value.
     * @param avt new attribute value
     */
    void setRegex( AttributeValueTemplate avt );
    
    /**
     * @return "regex" attribute value
     */
    AttributeValueTemplate getFlags();
    
    /**
     * Set "flags" attribute value.
     * @param avt new attribute value
     */
    void setFlags( AttributeValueTemplate avt );
}
