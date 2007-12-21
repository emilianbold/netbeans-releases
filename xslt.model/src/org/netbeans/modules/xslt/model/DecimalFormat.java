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


/**
 * <pre>
 * &lt;xs:element name="decimal-format" substitutionGroup="xsl:declaration">
 *      &lt;xs:complexType>
 *          &lt;xs:complexContent>
 *              &lt;xs:extension base="xsl:element-only-versioned-element-type">
 *                  &lt;xs:attribute name="name" type="xsl:QName"/>
 *                  &lt;xs:attribute name="decimal-separator" type="xsl:char" default="."/>
 *                  &lt;xs:attribute name="grouping-separator" type="xsl:char" default=","/>
 *                  &lt;xs:attribute name="infinity" type="xs:string" default="Infinity"/>
 *                  &lt;xs:attribute name="minus-sign" type="xsl:char" default="-"/>
 *                  &lt;xs:attribute name="NaN" type="xs:string" default="NaN"/>
 *                  &lt;xs:attribute name="percent" type="xsl:char" default="%"/>
 *                  &lt;xs:attribute name="per-mille" type="xsl:char" default="&#x2030;"/>
 *                  &lt;xs:attribute name="zero-digit" type="xsl:char" default="0"/>
 *                  &lt;xs:attribute name="digit" type="xsl:char" default="#"/>
 *                  &lt;xs:attribute name="pattern-separator" type="xsl:char" default=";"/>
 *              &lt;/xs:extension>
 *          &lt;/xs:complexContent>
 *      &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 * @author ads
 *
 */
public interface DecimalFormat extends Declaration, QualifiedNameable {
    
    String DECIMAL_SEPARATOR = "decimal-separator";     // NOI18N
    
    String INFINITY = "infinity";                       // NOI18N
    
    String MINUS_SIGN = "minus-sign";                   // NOI18N
    
    String NAN = "NaN";                                 // NOI18N
    
    String PERCENT = "percent";                         // NOI18N
    
    String ZERO_DIGIT = "zero-digit";                   // NOI18N
    
    String PER_MILLE = "per-mille";                     // NOI18N
    
    String DIGIT = "digit";                             // NOI18N
    
    String PATTERN_SEPARATOR = "pattern-separator";     // NOI18N
    
    String GROUPING_SEPARATOR = "grouping-separator";   // NOI18N
    
    /**
     * @return "infinity" attribute value
     */
    String getInfinity();
    
    /**
     * Set "infinity" attribute value.
     * @param value new value
     */
    void setInfinity( String value );
    
    /**
     * @return "NaN" attribute value
     */
    String getNaN();
    
    /**
     * Set "NaN" attribute value.
     * @param nan new value
     */
    void setNaN( String nan );

}
