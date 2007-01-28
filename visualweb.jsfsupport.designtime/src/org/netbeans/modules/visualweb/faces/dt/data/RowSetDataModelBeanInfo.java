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

package org.netbeans.modules.visualweb.faces.dt.data;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import javax.faces.model.DataModelListener;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.base.CategoryDescriptors;
import com.sun.rave.faces.data.RowSetDataModel;

public class RowSetDataModelBeanInfo extends SimpleBeanInfo {

    private static EventSetDescriptor[] eventSetDescriptors;
    private static MethodDescriptor[] methodDescriptors;
    private static PropertyDescriptor[] propertyDescriptors;

    public RowSetDataModelBeanInfo() throws NoSuchMethodException {}

    private BeanDescriptor beanDescriptor;
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(RowSetDataModel.class, null);
            //beanDescriptor.setValue(Constants.BeanDescriptor.TRAY_COMPONENT, Boolean.FALSE);
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
                    new EventSetDescriptor(RowSetDataModel.class, "dataModel", //NOI18N
                    DataModelListener.class,
                    new String[] {
                    "rowSelected"
                }

                , //NOI18N
                    "addDataModelListener", "removeDataModelListener") //NOI18N
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
                return loadImage("RowSetDataModelIconColor16.png"); //NOI18N
            case BeanInfo.ICON_COLOR_32x32:
                return loadImage("RowSetDataModelIconColor32.gif"); //NOI18N
            case BeanInfo.ICON_MONO_16x16:
                return loadImage("RowSetDataModelIconMono16.gif"); //NOI18N
            case BeanInfo.ICON_MONO_32x32:
                return loadImage("RowSetDataModelIconMono32.gif"); //NOI18N
        }
        return null;
    }

    public synchronized MethodDescriptor[] getMethodDescriptors() {
        try {
            if (methodDescriptors == null) {
                methodDescriptors = new MethodDescriptor[] {
                    new MethodDescriptor(RowSetDataModel.class.getMethod("clear", null)), //NOI18N
                    new MethodDescriptor(RowSetDataModel.class.getMethod("commit", null)), //NOI18N
                    new MethodDescriptor(RowSetDataModel.class.getMethod("reset", null)), //NOI18N
                    new MethodDescriptor(RowSetDataModel.class.getMethod("rollback", null)), //NOI18N
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

                PropertyDescriptor dataCacheKey =
                    new PropertyDescriptor("dataCacheKey", RowSetDataModel.class, "getDataCacheKey",
                    "setDataCacheKey"); // NOI18N
                dataCacheKey.setValue(Constants.PropertyDescriptor.CATEGORY,
                    CategoryDescriptors.DATA);

                PropertyDescriptor rowSet =
                    new PropertyDescriptor("rowSet", RowSetDataModel.class, "getRowSet",
                    "setRowSet"); //NOI18N
                rowSet.setValue(Constants.PropertyDescriptor.CATEGORY,
                    CategoryDescriptors.ADVANCED);

                PropertyDescriptor schemaName =
                    new PropertyDescriptor("schemaName", RowSetDataModel.class, "getSchemaName",
                    "setSchemaName"); // NOI18N
                schemaName.setValue(Constants.PropertyDescriptor.CATEGORY,
                    CategoryDescriptors.DATA);

                PropertyDescriptor tableName =
                    new PropertyDescriptor("tableName", RowSetDataModel.class, "getTableName",
                    "setTableName"); // NOI18N
                tableName.setValue(Constants.PropertyDescriptor.CATEGORY,
                    CategoryDescriptors.DATA);

                propertyDescriptors = new PropertyDescriptor[] {
                    dataCacheKey,
                    rowSet,
                    schemaName,
                    tableName,
                };

            }
            return propertyDescriptors;
        } catch (IntrospectionException e) {
            return null;
        }
    }
}
