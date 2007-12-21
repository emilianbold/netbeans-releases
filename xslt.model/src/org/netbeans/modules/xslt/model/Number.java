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
 * &lt;xs:element name="number" substitutionGroup="xsl:instruction">
 *      &lt;xs:complexType>
 *          &lt;xs:complexContent mixed="true">
 *              &lt;xs:extension base="xsl:versioned-element-type">
 *                  &lt;xs:attribute name="value" type="xsl:expression"/>
 *                  &lt;xs:attribute name="select" type="xsl:expression"/>
 *                  &lt;xs:attribute name="level" type="xsl:level" default="single"/>
 *                  &lt;xs:attribute name="count" type="xsl:pattern"/>
 *                  &lt;xs:attribute name="from" type="xsl:pattern"/>
 *                  &lt;xs:attribute name="format" type="xsl:avt" default="1"/>
 *                  &lt;xs:attribute name="lang" type="xsl:avt"/>
 *                  &lt;xs:attribute name="letter-value" type="xsl:avt"/>
 *                  &lt;xs:attribute name="ordinal" type="xsl:avt"/>        
 *                  &lt;xs:attribute name="grouping-separator" type="xsl:avt"/>
 *                  &lt;xs:attribute name="grouping-size" type="xsl:avt"/>
 *              &lt;/xs:extension>
 *          &lt;/xs:complexContent>
 *      &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 * @author ads
 *
 */
public interface Number extends ContentElement, SelectSpec, Instruction, LangSpec,
    FormatSpec 
{

    String VALUE                = "value";          // NOI18N
    
    String LEVEL                = "level";          // NOI18N
    
    String COUNT                = "count";          // NOI18N
    
    String FROM                 = "from";           // NOI18N
    
    String LETTER_VALUE         = "letter-value";   // NOI18N
        
    String ORDINAL              = "ordinal";        // NOI18N     
    
    String GROUPING_SIZE        = "grouping-size";  // NOI18N
    
    String GROUPING_SEPARATOR   = DecimalFormat.GROUPING_SEPARATOR;
}
