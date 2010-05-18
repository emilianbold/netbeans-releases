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

import org.netbeans.modules.xslt.model.enums.TBoolean;


/**
 * <pre>
 * &lt;xs:element name="sort">
 *      &lt;xs:complexType>
 *          &lt;xs:complexContent mixed="true">
 *              &lt;xs:extension base="xsl:sequence-constructor">
 *                  &lt;xs:attribute name="select" type="xsl:expression"/>  
 *                  &lt;xs:attribute name="lang" type="xsl:avt"/>        
 *                  &lt;xs:attribute name="data-type" type="xsl:avt" default="text"/>        
 *                  &lt;xs:attribute name="order" type="xsl:avt" default="ascending"/>        
 *                  &lt;xs:attribute name="case-order" type="xsl:avt"/>
 *                  &lt;xs:attribute name="collation" type="xsl:avt"/>
 *                  &lt;xs:attribute name="stable" type="xsl:yes-or-no"/>
 *              &lt;/xs:extension>
 *          &lt;/xs:complexContent>
 *      &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 *
 * @author ads
 *
 */
public interface Sort extends ApplyTemplateChild, SelectSpec, SequenceConstructor, 
    LangSpec, CollationSpec
{

    String STABLE       = "stable";             // NOI18N
    
    String ORDER        = "order";              // NOI18N
    
    String CASE_ORDER   = "case-order";         // NOI18N
    
    String DATA_TYPE    = "data-type";          // NOI18N
    
    /**
     * @return "stable" attribute value
     */
    TBoolean getStable();
    
    /**
     * Set "stable" attribute value.
     * @param value new value
     */
    void setStable( TBoolean value );
    
    /**
     * @return "order" attribute value
     */
    AttributeValueTemplate getOrder();
    
    /**
     * Set "order" attribute value.
     * @param value new value
     */
    void setOrder( AttributeValueTemplate value );
    
    /**
     * @return "data-type" attribute value
     */
    AttributeValueTemplate getDataType();
    
    /**
     * Set "data-type" attribute value.
     * @param value new value
     */
    void setDataType( AttributeValueTemplate value );
    
    /**
     * @return "case-order" attribute value
     */
    AttributeValueTemplate getCaseOrder();
    
    /**
     * Set "case-order" attribute value.
     * @param value new value
     */
    void setCaseOrder( AttributeValueTemplate value );
}
