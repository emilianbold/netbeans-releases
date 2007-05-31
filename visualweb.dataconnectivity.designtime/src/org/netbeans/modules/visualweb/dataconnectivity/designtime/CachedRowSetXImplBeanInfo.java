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
import org.netbeans.modules.visualweb.faces.dt.data.*;
import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Savepoint;
import javax.sql.RowSetListener;

public class CachedRowSetXImplBeanInfo extends SimpleBeanInfo {

    private static EventSetDescriptor[] eventSetDescriptors;
    private static MethodDescriptor[] methodDescriptors;
    private static PropertyDescriptor[] propertyDescriptors;
    protected static Class beanClass = CachedRowSetXImpl.class;

    public CachedRowSetXImplBeanInfo() throws NoSuchMethodException {}
    

    protected BeanDescriptor beanDescriptor;
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(beanClass, null);
            /*
            beanDescriptor.setValue(
                ConstantsExt.BeanDescriptor.INSTANCE_INITIALIZATION_PERSISTENCE_MODE, 
                ConstantsExt.BeanDescriptor.INSTANCE_INITIALIZATION_PERSISTENCE_MODES.INITIALIZATION_STATEMENT);
            
            beanDescriptor.setValue(
                ConstantsExt.BeanDescriptor.INITIALIZATION_STATEMENT_FACTORY,
                new InstanceInitializationFactory());
             */
            
            //beanDescriptor.setValue(Constants.BeanDescriptor.TRAY_COMPONENT, Boolean.TRUE);
            //beanDescriptor.setValue(Constants.BeanDescriptor.CLEANUP_METHOD, "close");
        }
        return beanDescriptor;
    }

    //public int getDefaultEventIndex() {}

    public int getDefaultPropertyIndex() {
        return 0;
    }

    public EventSetDescriptor[] getEventSetDescriptors() {
        try {
            if (eventSetDescriptors == null) {
                eventSetDescriptors = new EventSetDescriptor[] {
                    new EventSetDescriptor(beanClass, "rowSet", //NOI18N
                    RowSetListener.class,
                    new String[] {
                    //!JK "cursorMoved", "rowChanged", "rowSetChanged, rowSetPopulated"
                    "cursorMoved", "rowChanged", "rowSetChanged"
                }

                , //NOI18N
                    "addRowSetListener", "removeRowSetListener") //NOI18N
                };
            }

            return eventSetDescriptors;
        } catch (IntrospectionException e) {
            return null;
        }
    }

    public Image getIcon(int iconKind) {
        switch (iconKind) {
            case BeanInfo.ICON_COLOR_16x16:
                return loadImage("CachedRowSetXImplIconColor16.png"); //NOI18N
            case BeanInfo.ICON_COLOR_32x32:
                return loadImage("CachedRowSetXImplIconColor32.gif"); //NOI18N
            case BeanInfo.ICON_MONO_16x16:
                return loadImage("CachedRowSetXImplIconMono16.gif"); //NOI18N
            case BeanInfo.ICON_MONO_32x32:
                return loadImage("CachedRowSetXImplIconMono32.gif"); //NOI18N
        }
        return null;
    }

    public synchronized MethodDescriptor[] getMethodDescriptors() {
        try {
            if (methodDescriptors == null) {
                methodDescriptors = new MethodDescriptor[] {
                    new MethodDescriptor(beanClass.getMethod("absolute", //NOI18N
                        new Class[] {int.class})),
                    new MethodDescriptor(beanClass.getMethod("acceptChanges", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("acceptChanges", //NOI18N
                        new Class[] {Connection.class})),
                    new MethodDescriptor(beanClass.getMethod("afterLast", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("beforeFirst", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("cancelRowUpdates", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("clearParameters", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("clearWarnings", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("close", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("columnUpdated", //NOI18N
                        new Class[] {int.class})),
                    new MethodDescriptor(beanClass.getMethod("columnUpdated", //NOI18N
                        new Class[] {String.class})),
                    new MethodDescriptor(beanClass.getMethod("commit", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("createCopy", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("createCopyNoConstraints", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("createCopySchema", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("createShared", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("deleteRow", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("execute", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("findColumn", //NOI18N
                        new Class[] {String.class})),
                    new MethodDescriptor(beanClass.getMethod("first", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("getMetaData", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("getKeyColumns", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("getOriginal", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("getOriginalRow", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("getRow", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("getStatement", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("getWarnings", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("insertRow", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("isAfterLast", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("isBeforeFirst", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("isFirst", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("isLast", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("last", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("moveToCurrentRow", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("moveToInsertRow", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("next", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("nextPage", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("populate", //NOI18N
                        new Class[] {ResultSet.class})),
                    new MethodDescriptor(beanClass.getMethod("previous", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("previousPage", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("refreshRow", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("relative", //NOI18N
                        new Class[] {int.class})),

                    new MethodDescriptor(beanClass.getMethod("release", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("restoreOriginal", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("rollback", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("rollback", //NOI18N
                        new Class[] {Savepoint.class})),
                    new MethodDescriptor(beanClass.getMethod("rowDeleted", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("rowInserted", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("rowUpdated", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("setKeyColumns", //NOI18N
                        new Class[] {int[].class})),
                    new MethodDescriptor(beanClass.getMethod("setOriginalRow", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("toCollection", null)),//NOI18N
                    new MethodDescriptor(beanClass.getMethod("toCollection", //NOI18N
                        new Class[] {int.class})),
                    new MethodDescriptor(beanClass.getMethod("toCollection", //NOI18N
                        new Class[] {String.class})),
                    new MethodDescriptor(beanClass.getMethod("undoDelete", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("undoInsert", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("undoUpdate", null)), //NOI18N
                    new MethodDescriptor(beanClass.getMethod("wasNull", null)) //NOI18N

                };
            }

            return methodDescriptors;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public synchronized PropertyDescriptor[] getPropertyDescriptors() {
        try {
            if (propertyDescriptors != null) {
                return propertyDescriptors;
            }
            // command
            PropertyDescriptor command = new PropertyDescriptor(
                "command", beanClass); //NOI18N
            command.setBound(true);
            // concurrency
            PropertyDescriptor concurrency = new PropertyDescriptor(
                "concurrency", beanClass); //NOI18N
            concurrency.setBound(true);
            concurrency.setPropertyEditorClass(
                ConcurrencyPropertyEditor.class);
            //dataSourceName
            PropertyDescriptor dataSourceName =
                new PropertyDescriptor("dataSourceName", beanClass); //NOI18N
            dataSourceName.setBound(true);
            // EAT: The original prop editor
            dataSourceName.setPropertyEditorClass(
            DataSourceNamePropertyEditor.class);
            // EAT: new one, once it works :(
            // dataSourceName.setPropertyEditorClass(
            //     com.sun.jsfcl.std.property.SingleChoiceReferenceDataPropertyEditor.class);
            // dataSourceName.setValue(
            //     Constants.PropertyDescriptor.REFERENCE_DATA_NAME,
            //     com.sun.jsfcl.std.reference.DataSourceNameReferenceData.NAME);
            // maxRows
            PropertyDescriptor maxRows = new PropertyDescriptor(
                "maxRows", beanClass); //NOI18N
            maxRows.setBound(true);
            // pageSize
            PropertyDescriptor pageSize = new PropertyDescriptor(
                "pageSize", beanClass); //NOI18N
            // password
            PropertyDescriptor password = new PropertyDescriptor(
                "password", beanClass); //NOI18N
            password.setBound(true);
            // showDeleted
            PropertyDescriptor showDeleted = new PropertyDescriptor(
                "showDeleted", beanClass); //NOI18N
            // transactionIsolation
            PropertyDescriptor transactionIsolation =
                new PropertyDescriptor("transactionIsolation", beanClass); //NOI18N
            transactionIsolation.setBound(true);
            transactionIsolation.setPropertyEditorClass(
                TransactionIsolationPropertyEditor.class);
            // tableName
            PropertyDescriptor tableName = new PropertyDescriptor(
                "tableName", beanClass); //NOI18N
            // type
            PropertyDescriptor type =
                new PropertyDescriptor("type", beanClass); //NOI18N
            type.setBound(true);
            type.setPropertyEditorClass(
                TypePropertyEditor.class);
            // url
            PropertyDescriptor url = new PropertyDescriptor("url", beanClass); //NOI18N
            url.setBound(true);
            // username
            PropertyDescriptor username = new PropertyDescriptor(
                "username", beanClass); //NOI18N
            username.setBound(true);
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


            propertyDescriptors = new PropertyDescriptor[] {
                command,
                concurrency,
                dataSourceName,
                new PropertyDescriptor("fetchSize", beanClass), //NOI18N
                maxRows,
                pageSize,
                password,
                showDeleted,
                transactionIsolation,
                type,
                tableName,
                url,
                username,
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
            
            return propertyDescriptors;
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }
}
