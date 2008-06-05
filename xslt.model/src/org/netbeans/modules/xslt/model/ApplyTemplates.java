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

import javax.xml.namespace.QName;


/**
 * <pre>
 * &lt;xs:element name="apply-templates" substitutionGroup="xsl:instruction">
 *  &lt;xs:complexType>
 *      &lt;xs:complexContent>
 *          &lt;xs:extension base="xsl:element-only-versioned-element-type">
 *              &lt;xs:choice minOccurs="0" maxOccurs="unbounded">
 *                  &lt;xs:element ref="xsl:sort"/>
 *                  &lt;xs:element ref="xsl:with-param"/>
 *              &lt;/xs:choice>
 *              &lt;xs:attribute name="select" type="xsl:expression" default="child::node()"/>
 *              &lt;xs:attribute name="mode" type="xsl:mode"/>
 *          &lt;/xs:extension>
 *      &lt;/xs:complexContent>
 *  &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 * 
 * @author ads
 *
 */
public interface ApplyTemplates extends Instruction, SelectSpec, WithParamContainer {
    
    String CHILD_ELEMENTS   = "sort_or_with-param";        // NOI18N
    
    String MODE             = "mode";                      // NOI18N

    /**
     * @return list of ApplyTemplates children components
     */
    List<ApplyTemplateChild> getChildrenElements();
    
    /**
     * Appends new <code>child</code> to the end of children list.
     * @param child new child component 
     */
    void appendChildElement( ApplyTemplateChild child );
    
    /**
     * Insert new <code>child</code> into <code>position</code>.
     * @param child new child element.
     * @param position position index
     */
    void addChildElement( ApplyTemplateChild child , int position );
    
    /**
     * Removes <code>child</code> component.  
     * @param child child in this parent
     */
    void removeChildElement( ApplyTemplateChild child );
    
    /**
     * Gets the mode of this template.
     * @return the mode
     */
    QName getMode();
    
    
    /**
     * Sets the mode of this template.
     * @param mode the new mode for this template
     */
    void setMode(QName  mode);
}
