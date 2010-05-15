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
 * &lt;xs:element name="with-param">
 *      &lt;xs:complexType>
 *          &lt;xs:complexContent mixed="true">
 *              &lt;xs:extension base="xsl:sequence-constructor">
 *                  &lt;xs:attribute name="name" type="xsl:QName" use="required"/>
 *                  &lt;xs:attribute name="select" type="xsl:expression"/>
 *                  &lt;xs:attribute name="as" type="xsl:sequence-type"/>
 *                  &lt;xs:attribute name="tunnel" type="xsl:yes-or-no"/>   
 *              &lt;/xs:extension>
 *          &lt;/xs:complexContent>
 *      &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 * @author ads
 *
 */
public interface WithParam extends SequenceConstructor, SelectSpec, AsSpec, 
    ApplyTemplateChild, TunnelSpec, NextMatchChild 
{

    String NAME = QualifiedNameable.NAME;
    
    /**
     * @return reference to the param component
     */
    XslReference<Param> getName();
    
    /**
     * Set new reference to param component.
     * @param name new reference value
     */
    void setName( XslReference<Param> name );
}
