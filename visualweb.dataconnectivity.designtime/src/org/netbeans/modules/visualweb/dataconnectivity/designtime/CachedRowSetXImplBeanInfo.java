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
package org.netbeans.modules.visualweb.dataconnectivity.designtime;

import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.base.CategoryDescriptors;
import com.sun.sql.rowset.CachedRowSetXImpl;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

/**
 *
 * @author David Van Couvering
 */
public class CachedRowSetXImplBeanInfo extends CachedRowSetImplBeanInfo {
    private PropertyDescriptor[] propertyDescriptors;
    
    public CachedRowSetXImplBeanInfo() throws NoSuchMethodException {
        super();
        super.setBeanClass(CachedRowSetXImpl.class);
    }
    
    public synchronized PropertyDescriptor[] getPropertyDescriptors() {
        if ( propertyDescriptors != null ) {
            return propertyDescriptors;
        }
     
        //
        // Take what the generic descriptor does and add the extensions
        // supported by CachedRowSetX
        //
        try {
            // catalogName
            PropertyDescriptor catalogName = new PropertyDescriptor(
                "catalogName", beanClass); //NOI18N
            catalogName.setValue(Constants.PropertyDescriptor.CATEGORY,
                CategoryDescriptors.ADVANCED);
            // columnCatalogNames
            PropertyDescriptor columnCatalogNames = new PropertyDescriptor(
               "columnCatalogNames", beanClass); // NOI18N
            columnCatalogNames.setValue(Constants.PropertyDescriptor.CATEGORY,
                CategoryDescriptors.ADVANCED);
            columnCatalogNames.setValue(Constants.PropertyDescriptor.CATEGORY,
                CategoryDescriptors.ADVANCED);
            // columnSchemaNames
            PropertyDescriptor columnSchemaNames = new PropertyDescriptor(
                "columnSchemaNames", beanClass); //NOI18N
            columnSchemaNames.setValue(Constants.PropertyDescriptor.CATEGORY,
                CategoryDescriptors.ADVANCED);
            // columnTableNames
            PropertyDescriptor columnTableNames = new PropertyDescriptor(
                "columnTableNames", beanClass); //NOI18N
            columnTableNames.setValue(Constants.PropertyDescriptor.CATEGORY,
                CategoryDescriptors.ADVANCED);
            // columnNames
            PropertyDescriptor columnNames = new PropertyDescriptor(
                "columnNames", beanClass); //NOI18N
            columnNames.setValue(Constants.PropertyDescriptor.CATEGORY,
            CategoryDescriptors.ADVANCED);
            // insertableColumns
            PropertyDescriptor insertableColumns = new PropertyDescriptor(
                "insertableColumns", beanClass); //NOI18N
            insertableColumns.setValue(Constants.PropertyDescriptor.CATEGORY,
                CategoryDescriptors.ADVANCED);
            // updatableColumns
            PropertyDescriptor updatableColumns = new PropertyDescriptor(
                "updatableColumns", beanClass); //NOI18N
            updatableColumns.setValue(Constants.PropertyDescriptor.CATEGORY,
                CategoryDescriptors.ADVANCED);
            // printStatements
            PropertyDescriptor printStatements = new PropertyDescriptor(
                "printStatements", beanClass); //NOI18N
            printStatements.setValue(Constants.PropertyDescriptor.CATEGORY,
                CategoryDescriptors.ADVANCED);            
            // schemaName
            PropertyDescriptor schemaName = new PropertyDescriptor(
                "schemaName", beanClass); //NOI18N
            schemaName.setValue(Constants.PropertyDescriptor.CATEGORY, 
                CategoryDescriptors.ADVANCED);

            // size
            PropertyDescriptor size = new PropertyDescriptor(
                "size", beanClass, "size", null); //NOI18N

            PropertyDescriptor[] parentDescriptors = 
                super.getPropertyDescriptors();

            
            PropertyDescriptor[] myDescriptors = new PropertyDescriptor[] {
                catalogName,
                columnCatalogNames,
                columnSchemaNames,
                columnTableNames,
                columnNames,
                insertableColumns,
                updatableColumns,
                printStatements,
                schemaName,
                size
                
            };
            
            int arraySize = parentDescriptors.length + myDescriptors.length;            
            propertyDescriptors = new PropertyDescriptor[arraySize];
            
            System.arraycopy(parentDescriptors, 0, 
                    propertyDescriptors, 0, parentDescriptors.length);
            
            System.arraycopy(myDescriptors, 0, propertyDescriptors,
                    parentDescriptors.length, myDescriptors.length);
            
            return propertyDescriptors;
        } catch ( IntrospectionException e ) {
            throw new RuntimeException(e);
        }
        
    }
}
