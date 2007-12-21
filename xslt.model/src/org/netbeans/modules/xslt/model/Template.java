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
 * &lt;xs:element name="template" substitutionGroup="xsl:declaration">
 *      &lt;xs:complexType>
 *          &lt;xs:complexContent mixed="true">
 *              &lt;xs:extension base="xsl:versioned-element-type">
 *                  &lt;xs:sequence>
 *                      &lt;xs:element ref="xsl:param" minOccurs="0" maxOccurs="unbounded"/>
 *                      &lt;xs:group ref="xsl:sequence-constructor-group" minOccurs="0" maxOccurs="unbounded"/>
 *                  &lt;/xs:sequence>
 *                  &lt;xs:attribute name="match" type="xsl:pattern"/>
 *                  &lt;xs:attribute name="priority" type="xs:decimal"/>
 *                  &lt;xs:attribute name="mode" type="xsl:modes"/>
 *                  &lt;xs:attribute name="name" type="xsl:QName"/>
 *                  &lt;xs:attribute name="as" type="xsl:sequence-type" default="item()*"/>
 *              &lt;/xs:extension>
 *          &lt;/xs:complexContent>
 *      &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre> 
 *
 * @author ads
 *
 */
public interface Template extends QualifiedNameable, SequenceConstructor, 
    ParamContainer, Declaration, AsSpec, MatchSpec
{

    String PRIORITY         = "priority";                  // NOI18N
    
    String MODE             = ApplyTemplates.MODE;
    
    /**
     * @return priority attribute for this template.
     */
    Double getPriority();
    
    /**
     * Set new value for priority attribute.
     * @param priority
     */
    void setPriority( Double priority );
    
    /**
     * @return either a list, each member being either a QName or #default;
     * or the value #all
     */
    List<QName> getMode();
    
    /**
     * Set value for attribute "mode".
     * @param mode new value
     */
    void setMode( List<QName> mode );
}
