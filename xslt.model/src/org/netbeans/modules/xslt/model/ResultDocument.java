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
 * &lt;xs:element name="result-document" substitutionGroup="xsl:instruction">
 *      &lt;xs:complexType>
 *          &lt;xs:complexContent mixed="true">
 *              &lt;xs:extension base="xsl:sequence-constructor">
 *                  &lt;xs:attribute name="format" type="xsl:avt"/>
 *                  &lt;xs:attribute name="href" type="xsl:avt"/>
 *                  &lt;xs:attribute name="type" type="xsl:QName"/>
 *                  &lt;xs:attribute name="validation" type="xsl:validation-type"/>
 *                  &lt;xs:attribute name="method" type="xsl:avt"/>
 *                  &lt;xs:attribute name="byte-order-mark" type="xsl:avt"/>
 *                  &lt;xs:attribute name="cdata-section-elements" type="xsl:avt"/>
 *                  &lt;xs:attribute name="doctype-public" type="xsl:avt"/>
 *                  &lt;xs:attribute name="doctype-system" type="xsl:avt"/>
 *                  &lt;xs:attribute name="encoding" type="xsl:avt"/>
 *                  &lt;xs:attribute name="escape-uri-attributes" type="xsl:avt"/>
 *                  &lt;xs:attribute name="include-content-type" type="xsl:avt"/>
 *                  &lt;xs:attribute name="indent" type="xsl:avt"/>
 *                  &lt;xs:attribute name="media-type" type="xsl:avt"/>
 *                  &lt;xs:attribute name="normalization-form" type="xsl:avt"/>
 *                  &lt;xs:attribute name="omit-xml-declaration" type="xsl:avt"/>
 *                  &lt;xs:attribute name="standalone" type="xsl:avt"/>
 *                  &lt;xs:attribute name="undeclare-prefixes" type="xsl:avt"/>
 *                  &lt;xs:attribute name="use-character-maps" type="xsl:QNames"/>
 *                  &lt;xs:attribute name="output-version" type="xsl:avt"/>
 *              &lt;/xs:extension>
 *          &lt;/xs:complexContent>
 *      &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 * @author ads
 *
 */
public interface ResultDocument extends Instruction, SequenceConstructor, TypeSpec, 
    ValidationSpec, UseCharacterMapsSpec, FormatSpec
{
    String STANDALONE                   = Output.STANDALONE;
    
    String UNDECLARE_PREFIXES           = Output.UNDECLARE_PREFIXES; 
    
    String HREF                         = XslModelReference.HREF;
    
    String METHOD                       = Output.METHOD;
    
    String INDENT                       = Output.INDENT;
    
    String ENCODING                     = Output.ENCODING;
    
    String BYTE_ORDER_MARK              = Output.BYTE_ORDER_MARK;
    
    String CDATA_SECTION_ELEMENTS       = Output.CDATA_SECTION_ELEMENTS;
    
    String DOCTYPE_PUBLIC               = Output.DOCTYPE_PUBLIC;
    
    String DOCTYPE_SYSTEM               = Output.DOCTYPE_SYSTEM;
    
    String ESCAPE_URI_ATTRIBUTES        = Output.ESCAPE_URI_ATTRIBUTES; 
    
    String INCLUDE_CONTENT_TYPE         = Output.INCLUDE_CONTENT_TYPE;
    
    String MEDIA_TYPE                   = Output.MEDIA_TYPE;
    
    String NORMALIZATION_FORM           = Output.NORMALIZATION_FORM;
    
    String OMIT_XML_DECLARATION         = Output.OMIT_XML_DECLARATION;
    
    String OUTPUT_VERSION               = "output-version";          // NOI18N
    
    /**
     * @return "standalone" attribute value
     */
    AttributeValueTemplate getStandalone();
    
    /**
     * Set "standalone" attribute value.
     * @param avt new value
     */
    void setStandalone( AttributeValueTemplate avt );
    
    /**
     * @return "undeclare-prefixes" attribute value
     */
    AttributeValueTemplate getUndeclarePrefixes();
    
    /**
     * Set "undeclare-prefixes" attribute value. 
     * @param avt new value
     */
    void setUndeclarePrefixes( AttributeValueTemplate avt );
    
    /**
     * @return "href" attribute value
     */
    AttributeValueTemplate getHref();
    
    /**
     * Set "href" attribute value. 
     * @param avt new value
     */
    void setHref( AttributeValueTemplate avt );
    
    /**
     * @return "method" attribute value
     */
    AttributeValueTemplate getMethod();
    
    /**
     * Set "method" attribute value. 
     * @param avt new value
     */
    void setMethod( AttributeValueTemplate avt );
    
    /**
     * @return "indent" attribute value
     */
    AttributeValueTemplate getIndent();
    
    /**
     * Set "indent" attribute value. 
     * @param avt new value
     */
    void setIndent( AttributeValueTemplate avt );
    
    /**
     * @return "encoding" attribute value
     */
    AttributeValueTemplate getEncoding();
    
    /**
     * Set "encoding" attribute value. 
     * @param avt new value
     */
    void setEncoding( AttributeValueTemplate avt );
    
}
