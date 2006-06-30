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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * CmpResource.java
 *
 * Created on November 17, 2004, 4:49 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

import org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface CmpResource extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String JNDI_NAME = "JndiName";	// NOI18N
    public static final String DEFAULT_RESOURCE_PRINCIPAL = "DefaultResourcePrincipal";	// NOI18N
    public static final String PROPERTY = "PropertyElement";	// NOI18N
    public static final String CREATE_TABLES_AT_DEPLOY = "CreateTablesAtDeploy";	// NOI18N
    public static final String DROP_TABLES_AT_UNDEPLOY = "DropTablesAtUndeploy";	// NOI18N
    public static final String DATABASE_VENDOR_NAME = "DatabaseVendorName";	// NOI18N
    public static final String SCHEMA_GENERATOR_PROPERTIES = "SchemaGeneratorProperties";	// NOI18N
        
    /** Setter for jndi-name property
     * @param value property value
     */
    public void setJndiName(java.lang.String value);
    /** Getter for jndi-name property.
     * @return property value
     */
    public java.lang.String getJndiName();
    
    /** Setter for default-resource-principal property
     * @param value property value
     */
    public void setDefaultResourcePrincipal(DefaultResourcePrincipal value);
    /** Getter for default-resource-principal property.
     * @return property value
     */
    public DefaultResourcePrincipal getDefaultResourcePrincipal();
    
    public DefaultResourcePrincipal newDefaultResourcePrincipal();
    
    public PropertyElement[] getPropertyElement();
    public PropertyElement getPropertyElement(int index);
    public void setPropertyElement(PropertyElement[] value);
    public void setPropertyElement(int index, PropertyElement value);
    public int addPropertyElement(PropertyElement value);
    public int removePropertyElement(PropertyElement value); 
    public int sizePropertyElement();
    public PropertyElement newPropertyElement();
    
    /** Setter for create-tables-at-deploy property
     * @param value property value
     */
    public void setCreateTablesAtDeploy(java.lang.String value);
    /** Getter for create-tables-at-deploy property.
     * @return property value
     */
    public java.lang.String getCreateTablesAtDeploy();
    
    /** Setter for drop-tables-at-undeploy property
     * @param value property value
     */
    public void setDropTablesAtUndeploy(java.lang.String value);
    /** Getter for drop-tables-at-undeploy property.
     * @return property value
     */
    public java.lang.String getDropTablesAtUndeploy();
    
    /** Setter for database-vendor-name property
     * @param value property value
     */
    public void setDatabaseVendorName(java.lang.String value);
    /** Getter for database-vendor-name property.
     * @return property value
     */
    public java.lang.String getDatabaseVendorName();
    /** Setter for schema-generator-properties property
     * @param value property value
     */
    public void setSchemaGeneratorProperties(SchemaGeneratorProperties value);
    /** Getter for schema-generator-properties property.
     * @return property value
     */
    public SchemaGeneratorProperties getSchemaGeneratorProperties(); 
    
    public SchemaGeneratorProperties newSchemaGeneratorProperties();
    
}
