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
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.base.CategoryDescriptors;
import com.sun.sql.rowset.CachedRowSetXImpl;

public class CachedRowSetXImplBeanInfo extends SimpleBeanInfo {

    private static EventSetDescriptor[] eventSetDescriptors;
    private static MethodDescriptor[] methodDescriptors;
    private static PropertyDescriptor[] propertyDescriptors;

    public CachedRowSetXImplBeanInfo() throws NoSuchMethodException {}

    private BeanDescriptor beanDescriptor;
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(CachedRowSetXImpl.class, null);
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
                    new EventSetDescriptor(CachedRowSetXImpl.class, "rowSet", //NOI18N
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
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("absolute", //NOI18N
                        new Class[] {int.class})),
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("acceptChanges", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("acceptChanges", //NOI18N
                        new Class[] {Connection.class})),
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("afterLast", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("beforeFirst", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("cancelRowUpdates", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("clearParameters", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("clearWarnings", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("close", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("columnUpdated", //NOI18N
                        new Class[] {int.class})),
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("columnUpdated", //NOI18N
                        new Class[] {String.class})),
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("commit", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("createCopy", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("createCopyNoConstraints", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("createCopySchema", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("createShared", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("deleteRow", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("execute", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("findColumn", //NOI18N
                        new Class[] {String.class})),
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("first", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("getMetaData", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("getKeyColumns", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("getOriginal", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("getOriginalRow", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("getRow", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("getStatement", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("getWarnings", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("insertRow", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("isAfterLast", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("isBeforeFirst", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("isFirst", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("isLast", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("last", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("moveToCurrentRow", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("moveToInsertRow", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("next", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("nextPage", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("populate", //NOI18N
                        new Class[] {ResultSet.class})),
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("previous", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("previousPage", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("refreshRow", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("relative", //NOI18N
                        new Class[] {int.class})),

                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("release", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("restoreOriginal", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("rollback", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("rollback", //NOI18N
                        new Class[] {Savepoint.class})),
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("rowDeleted", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("rowInserted", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("rowUpdated", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("setKeyColumns", //NOI18N
                        new Class[] {int[].class})),
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("setOriginalRow", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("toCollection", null)),//NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("toCollection", //NOI18N
                        new Class[] {int.class})),
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("toCollection", //NOI18N
                        new Class[] {String.class})),
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("undoDelete", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("undoInsert", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("undoUpdate", null)), //NOI18N
                    new MethodDescriptor(CachedRowSetXImpl.class.getMethod("wasNull", null)) //NOI18N

                };
            }

            return methodDescriptors;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public synchronized PropertyDescriptor[] getPropertyDescriptors() {
        try {
            if (propertyDescriptors == null) {
                // command
                PropertyDescriptor command = new PropertyDescriptor(
                    "command", CachedRowSetXImpl.class); //NOI18N
                command.setBound(true);
                // concurrency
                PropertyDescriptor concurrency = new PropertyDescriptor(
                    "concurrency", CachedRowSetXImpl.class); //NOI18N
                concurrency.setBound(true);
                concurrency.setPropertyEditorClass(
                    ConcurrencyPropertyEditor.class);
                //dataSourceName
                PropertyDescriptor dataSourceName =
                    new PropertyDescriptor("dataSourceName", CachedRowSetXImpl.class); //NOI18N
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
                    "maxRows", CachedRowSetXImpl.class); //NOI18N
                maxRows.setBound(true);
                // pageSize
                PropertyDescriptor pageSize = new PropertyDescriptor(
                    "pageSize", CachedRowSetXImpl.class); //NOI18N
                // password
                PropertyDescriptor password = new PropertyDescriptor(
                    "password", CachedRowSetXImpl.class); //NOI18N
                password.setBound(true);
                // showDeleted
                PropertyDescriptor showDeleted = new PropertyDescriptor(
                    "showDeleted", CachedRowSetXImpl.class); //NOI18N
                // size
                PropertyDescriptor size = new PropertyDescriptor(
                    "size", CachedRowSetXImpl.class, "size", null); //NOI18N
                // transactionIsolation
                PropertyDescriptor transactionIsolation =
                    new PropertyDescriptor("transactionIsolation", CachedRowSetXImpl.class); //NOI18N
                transactionIsolation.setBound(true);
                transactionIsolation.setPropertyEditorClass(
                    TransactionIsolationPropertyEditor.class);
                // catalogName
                PropertyDescriptor catalogName = new PropertyDescriptor(
                    "catalogName", CachedRowSetXImpl.class); //NOI18N
                catalogName.setValue(Constants.PropertyDescriptor.CATEGORY,
                    CategoryDescriptors.ADVANCED);
                // schemaName
                PropertyDescriptor schemaName = new PropertyDescriptor(
                    "schemaName", CachedRowSetXImpl.class); //NOI18N
                schemaName.setValue(Constants.PropertyDescriptor.CATEGORY, CategoryDescriptors.ADVANCED);
                // tableName
                PropertyDescriptor tableName = new PropertyDescriptor(
                    "tableName", CachedRowSetXImpl.class); //NOI18N
                // columnCatalogNames
                PropertyDescriptor columnCatalogNames = new PropertyDescriptor(
                   "columnCatalogNames", CachedRowSetXImpl.class); // NOI18N
                columnCatalogNames.setValue(Constants.PropertyDescriptor.CATEGORY,
                    CategoryDescriptors.ADVANCED);
                columnCatalogNames.setValue(Constants.PropertyDescriptor.CATEGORY,
                    CategoryDescriptors.ADVANCED);
                // columnSchemaNames
                PropertyDescriptor columnSchemaNames = new PropertyDescriptor(
                    "columnSchemaNames", CachedRowSetXImpl.class); //NOI18N
                columnSchemaNames.setValue(Constants.PropertyDescriptor.CATEGORY,
                    CategoryDescriptors.ADVANCED);
                // columnTableNames
                PropertyDescriptor columnTableNames = new PropertyDescriptor(
                    "columnTableNames", CachedRowSetXImpl.class); //NOI18N
                columnTableNames.setValue(Constants.PropertyDescriptor.CATEGORY,
                    CategoryDescriptors.ADVANCED);
                // columnNames
                PropertyDescriptor columnNames = new PropertyDescriptor(
                    "columnNames", CachedRowSetXImpl.class); //NOI18N
                columnNames.setValue(Constants.PropertyDescriptor.CATEGORY,
                    CategoryDescriptors.ADVANCED);
                // type
                PropertyDescriptor type =
                    new PropertyDescriptor("type", CachedRowSetXImpl.class); //NOI18N
                type.setBound(true);
                type.setPropertyEditorClass(
                    TypePropertyEditor.class);
                // url
                PropertyDescriptor url = new PropertyDescriptor("url", CachedRowSetXImpl.class); //NOI18N
                url.setBound(true);
                // username
                PropertyDescriptor username = new PropertyDescriptor(
                    "username", CachedRowSetXImpl.class); //NOI18N
                username.setBound(true);
                // insertableColumns
                PropertyDescriptor insertableColumns = new PropertyDescriptor(
                    "insertableColumns", CachedRowSetXImpl.class); //NOI18N
                insertableColumns.setValue(Constants.PropertyDescriptor.CATEGORY,
                    CategoryDescriptors.ADVANCED);
                // updatableColumns
                PropertyDescriptor updatableColumns = new PropertyDescriptor(
                    "updatableColumns", CachedRowSetXImpl.class); //NOI18N
                updatableColumns.setValue(Constants.PropertyDescriptor.CATEGORY,
                    CategoryDescriptors.ADVANCED);
                // printStatements
                PropertyDescriptor printStatements = new PropertyDescriptor(
                    "printStatements", CachedRowSetXImpl.class); //NOI18N
                printStatements.setValue(Constants.PropertyDescriptor.CATEGORY,
                    CategoryDescriptors.ADVANCED);

                propertyDescriptors = new PropertyDescriptor[] {
                    catalogName,
                    columnCatalogNames,
                    columnSchemaNames,
                    columnTableNames,
                    columnNames,
                    command,
                    concurrency,
                    dataSourceName,
                    new PropertyDescriptor("fetchSize", CachedRowSetXImpl.class), //NOI18N
                    //new PropertyDescriptor("maxFieldSize", CachedRowSetXImpl.class),
                    insertableColumns,
                    maxRows,
                    pageSize,
                    password,
                    printStatements,
                    schemaName,
                    showDeleted,
                    size,
                    //new PropertyDescriptor("queryTimeout", CachedRowSetXImpl.class), //NOI18N
                    transactionIsolation,
                    type,
                    tableName,
                    //new PropertyDescriptor("typeMap", CachedRowSetXImpl.class),  //NOI18N
                    updatableColumns,
                    url,
                    username
                    //, new PropertyDescriptor("readOnly", CachedRowSetXImpl.class)  //NOI18N
                };
            }
            return propertyDescriptors;
        } catch (IntrospectionException e) {
            return null;
        }
    }
}
