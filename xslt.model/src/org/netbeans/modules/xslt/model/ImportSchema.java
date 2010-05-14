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

import org.netbeans.modules.xml.schema.model.SchemaModel;


/**
 * <pre>
 * &lt;xs:element name="import-schema" substitutionGroup="xsl:declaration">
 *      &lt;xs:complexType>
 *          &lt;xs:complexContent>
 *              &lt;xs:extension base="xsl:element-only-versioned-element-type">
 *                  &lt;xs:sequence>
 *                      &lt;xs:element ref="xs:schema" minOccurs="0" maxOccurs="1"/>
 *                  &lt;/xs:sequence>
 *                  &lt;xs:attribute name="namespace" type="xs:anyURI"/>
 *                  &lt;xs:attribute name="schema-location" type="xs:anyURI"/>                  
 *              &lt;/xs:extension>
 *          &lt;/xs:complexContent>
 *      &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 * @author ads
 *
 */
public interface ImportSchema extends Declaration  {
    
    String NAMESPACE = NamespaceSpec.NAMESPACE;
    
    String SCHEMA_LOCATION = "schema-location";     // NOI18N
    
    /**
     * @return embedded schema model
     */
    SchemaModel getSchemaModel();
    
    /**
     * @return "namespace" attribute value
     */
    String getNamespace();
    
    /**
     * Set new "namespace" attribute value for imported schema.
     * @param namespaceUri new value
     */
    void setNamespace( String namespaceUri );
    
    /**
     * @return "schema-location" attribute value
     */
    String getSchemaLocation();
    
    /**
     * Set "schema-location" attribute value.
     * @param location new value
     */
    void setSchemaLocation( String location );

}
