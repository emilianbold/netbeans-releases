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

import org.netbeans.modules.xslt.model.enums.Standalone;
import org.netbeans.modules.xslt.model.enums.TBoolean;


/**
 * <pre>
 * &lt;xs:element name="output" substitutionGroup="xsl:declaration">
 *      &lt;xs:complexType>
 *          &lt;xs:complexContent mixed="true">
 *              &lt;xs:extension base="xsl:generic-element-type">
 *                  &lt;xs:attribute name="name" type="xsl:QName"/>
 *                  &lt;xs:attribute name="method" type="xsl:method"/>
 *                  &lt;xs:attribute name="byte-order-mark" type="xsl:yes-or-no"/>
 *                  &lt;xs:attribute name="cdata-section-elements" type="xsl:QNames"/>
 *                  &lt;xs:attribute name="doctype-public" type="xs:string"/>
 *                  &lt;xs:attribute name="doctype-system" type="xs:string"/>
 *                  &lt;xs:attribute name="encoding" type="xs:string"/>
 *                  &lt;xs:attribute name="escape-uri-attributes" type="xsl:yes-or-no"/>
 *                  &lt;xs:attribute name="include-content-type" type="xsl:yes-or-no"/>
 *                  &lt;xs:attribute name="indent" type="xsl:yes-or-no"/>
 *                  &lt;xs:attribute name="media-type" type="xs:string"/>
 *                  &lt;xs:attribute name="normalization-form" type="xs:NMTOKEN"/>
 *                  &lt;xs:attribute name="omit-xml-declaration" type="xsl:yes-or-no"/>
 *                  &lt;xs:attribute name="standalone" type="xsl:yes-or-no-or-omit"/>
 *                  &lt;xs:attribute name="undeclare-prefixes" type="xsl:yes-or-no"/>
 *                  &lt;xs:attribute name="use-character-maps" type="xsl:QNames"/>
 *                  &lt;xs:attribute name="version" type="xs:NMTOKEN"/>
 *              &lt;/xs:extension>
 *          &lt;/xs:complexContent>
 *      &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 * 
 * @author ads
 *
 */
public interface Output extends QualifiedNameable, ContentElement, Declaration,
    UseCharacterMapsSpec
{
    String STANDALONE               = "standalone";               // NOI18N
    
    String UNDECLARE_PREFIXES       = "undeclare-prefixes";       // NOI18N
    
    String METHOD                   = "method";                   // NOI18N
    
    String INDENT                   = "indent";                   // NOI18N
    
    String ENCODING                 = "encoding";                 // NOI18N
    
    String BYTE_ORDER_MARK          = "byte-order-mark";          // NOI18N
    
    String CDATA_SECTION_ELEMENTS   = "cdata-section-elements";   // NOI18N
    
    String DOCTYPE_PUBLIC           = "doctype-public";           // NOI18N
    
    String DOCTYPE_SYSTEM           = "doctype-system";           // NOI18N
    
    String ESCAPE_URI_ATTRIBUTES    = "escape-uri-attributes";    // NOI18N
    
    String INCLUDE_CONTENT_TYPE     = "include-content-type";     // NOI18N
    
    String MEDIA_TYPE               = "media-type";               // NOI18N
    
    String NORMALIZATION_FORM       = "normalization-form";       // NOI18N
    
    String OMIT_XML_DECLARATION     = "omit-xml-declaration";     // NOI18N
    
    String VERSION                  = Stylesheet.VERSION;
    
    /**
     * @return "standalone" attribute value
     */
    Standalone getStandalone();
    
    /**
     * Set "standalone" attribute value. 
     * @param value new value.
     */
    void setStandalone( Standalone value );
    
    /**
     * @return "undeclare-prefixes" attribute value
     */
    TBoolean getUndeclarePrefixes();
    
    /**
     * Set "undeclare-prefixes" attribute value
     * @param value new value 
     */
    void setUndeclarePrefixes( TBoolean value );
    
    /**
     * @return "indent" attribute value
     */
    TBoolean getIndent();
    
    /**
     * Set "indent" attribute value.
     * @param value new value
     */
    void setIndent( TBoolean value );
    
    /**
     * @return "encoding" attribute value
     */
    String getEncoding();
    
    /**
     * Set new "encoding" attribute value. 
     * @param encoding new value
     */
    void setEncoding( String encoding );
}
