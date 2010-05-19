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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.netbeans.modules.xslt.model.enums.Annotaions;
import org.netbeans.modules.xslt.model.enums.DefaultValidation;


/**
 * <pre>
 * &lt;xs:element name="stylesheet" substitutionGroup="xsl:transform"/>
 * 
 * &lt;xs:element name="transform">
 *      &lt;xs:complexType>
 *          &lt;xs:complexContent>
 *              &lt;xs:extension base="xsl:transform-element-base-type">
 *                  &lt;xs:sequence>
 *                      &lt;xs:element ref="xsl:import" minOccurs="0" maxOccurs="unbounded"/>
 *                      &lt;xs:choice minOccurs="0" maxOccurs="unbounded">
 *                          &lt;xs:element ref="xsl:declaration"/>
 *                          &lt;xs:element ref="xsl:variable"/>
 *                          &lt;xs:element ref="xsl:param"/>              
 *                      &lt;xs:any namespace="##other" processContents="lax"/> 
 *                      &lt;/xs:choice>
 *                  &lt;/xs:sequence>
 *                  &lt;xs:attribute name="id" type="xs:ID"/>
 *                  &lt;xs:attribute name="default-validation" type="xsl:validation-strip-or-preserve" default="strip"/>
 *                  &lt;xs:attribute name="input-type-annotations" type="xsl:input-type-annotations-type" default="unspecified"/>
 *              &lt;/xs:extension>
 *          &lt;/xs:complexContent>
 *      &lt;/xs:complexType>
 * &lt;/xs:element>
 * 
 * &lt;xs:complexType name="generic-element-type" mixed="true">
 *      &lt;xs:attribute name="default-collation" type="xsl:uri-list"/>
 *      &lt;xs:attribute name="exclude-result-prefixes" type="xsl:prefix-list-or-all"/>
 *      &lt;xs:attribute name="extension-element-prefixes" type="xsl:prefix-list"/>
 *      &lt;xs:attribute name="use-when" type="xsl:expression"/>
 *      &lt;xs:attribute name="xpath-default-namespace" type="xs:anyURI"/>
 *      &lt;xs:anyAttribute namespace="##other" processContents="lax"/>
 * &lt;/xs:complexType>
 * </pre>
 * 
 * In reality transform is the same as stylesheet.
 * So stylesheet object can have different tag names:
 * <pre>
 * stylesheet
 * and
 * transform.
 * </pre>
 * 
 * There is no methods for accessing to Template, Include, etc. child of stylesheet
 * because they all are Declaration. So Declaration accessor methods should be 
 * used instead. 
 *
 * @author ads
 *
 */
public interface Stylesheet extends XslComponent {
    
    String STYLESHEET_TOP_LEVEL_ELEMENTS    = "stylesheet_top_level_elements"; // NOI18N
    
    String IMPORT_PROPERTY                  = "import";                        // NOI18N
    
    String ID                               = "id";                            // NOI18N
    
    String VERSION                          = "version";                       // NOI18N
    
    String EXTENSION_ELEMENT_PREFIXES       = "extension-element-prefixes";    // NOI18N
    
    String EXCLUDE_RESULT_PREFIXES          = "exclude-result-prefixes";       // NOI18N
    
    String XPATH_DEFAULT_NAMESPACE          = "xpath-default-namespace";       // NOI18N
    
    String DEFAULT_VALIDATION               = "default-validation";            // NOI18N
    
    String DEFAULT_COLLATION                = "default-collation";             // NOI18N
    
    String INPUT_TYPE_ANNOTAIONS            = "input-type-annotations";        // NOI18N
    
    /**
     * Gets the ID of this stylesheet.
     * @return the ID
     */
    String getID();
    
    
    /**
     * Sets the ID of this stylesheet.
     * @param id the new ID for this stylesheet
     */
    void setID(String id);
    
    /**
     * @return "version" attribute value
     * @throws InvalidAttributeValueException in the case when attribute value is not
     * BigDecimal
     */
    BigDecimal getVersion() throws InvalidAttributeValueException;
    
    /**
     * Set "version" attribute value.
     * @param value new value
     */
    void setVersion( BigDecimal value );
    
    /**
     * @return "default-collation" attribute value
     */
    List<String> getDefaultCollation();
    
    /**
     * Set "default-collation" attribute value.
     * @param list new value
     */
    void setDefaultCollation( List<String> list );
    
    /**
     * @see constant {@link XslConstants.DEFAULT} as possible value here
     * @return "extension-element-prefixes" attribute value
     */
    List<String> getExtensionElementPrefixes();
    
    /**
     * Set "exclude-result-prefixes" attribute value.
     * @param list new value
     */
    void setExcludeResultPrefixes( List<String> list );
    
    /**
     * @see constant {@link XslConstants.DEFAULT}, {@link XslConstants.ALL} as 
     * possibles value here
     * @return "exclude-result-prefixes" attribute value
     */
    List<String> getExcludeResultPrefixes();
    
    /**
     * Set "extension-element-prefixes" attribute value.
     * @param list new value
     */
    void setExtensionElementPrefixes( List<String> list );
    
    /**
     * @return "xpath-default-namespace" attribute value
     */
    String getXpathDefaultNamespace();
    
    /**
     * Set "xpath-default-namespace" attribute value.
     * @param value new value
     */
    void setXpathDefaultNamespace(String value );
    
    /**
     * @return "default-validation" attribute value
     */
    DefaultValidation getDefaultValidation();
    
    /**
     * Set "default-validation" attribute value.
     * @param value new value 
     */
    void setDefaultValidation( DefaultValidation value ) ;
    
    /**
     * @return "input-type-annotations" attribute value
     */
    Annotaions getInputTypeAnnotations();
    
    /**
     * Set "input-type-annotations" attribute value.
     * @param value new value
     */
    void setInputTypeAnnotations( Annotaions value );
    
    
    /**
     * @return imports children for this stylesheet.
     * Note that resulting collection is unmodifiable. 
     */
    List<Import> getImports();
    
    /**
     * Add new import <code>impt</code> element at <code>position</code>. 
     * @param impt new import element.
     * @param position position for new element.
     */
    void addImport(Import impt, int position);
    
    /**
     * Append new import element.
     * @param impt new import child element for appending.
     */
    void appendImport(Import impt);
    
    /**
     * Removes existing <code>impt</code> import child element.
     * @param impt import child element.
     */
    void removeImport(Import impt);
    
    /**
     * @return declaration, varible or pram children for this stylesheet.
     * Note that this collection is unmodifiable.
     */
    List<StylesheetChild> getStylesheetChildren();

    /**
     * Add new <code>child</code> StylesheetChild element at <code>position</code>. 
     * @param child new StylesheetChild element.
     * @param position position for new element.
     */
    void addStylesheetChild(StylesheetChild child, int position);
    
    /**
     * Append new StylesheetChild element.
     * @param child new StylesheetChild child element for appending.
     */
    void appendStylesheetChild(StylesheetChild child);
    
    /**
     * Removes existing <code>child</code> StylesheetChild element.
     * @param child StylesheetChild child element.
     */
    void removeStylesheetChild(StylesheetChild child);
    
    /**
     * Return collection of all defined children in this stylesheet.
     * It includes children in imported xslt's and included xslt's.
     * @return collection of defined children
     */
    Collection<StylesheetChild> findAllDefinedChildren();

}
