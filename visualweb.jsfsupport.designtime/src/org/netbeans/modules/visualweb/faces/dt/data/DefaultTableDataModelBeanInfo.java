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

import com.sun.rave.faces.data.DefaultTableDataModel;
import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import javax.faces.model.DataModelListener;

public class DefaultTableDataModelBeanInfo extends SimpleBeanInfo {

    private static EventSetDescriptor[] eventSetDescriptors;
    private static MethodDescriptor[] methodDescriptors;
    private static PropertyDescriptor[] propertyDescriptors;

    public DefaultTableDataModelBeanInfo() throws NoSuchMethodException {}

    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(DefaultTableDataModel.class, null);
    }

    //public int getDefaultEventIndex() {}

    public int getDefaultPropertyIndex() {
        return 0;
    }

    public EventSetDescriptor[] getEventSetDescriptors() {
        try {
            if (eventSetDescriptors == null) {
                eventSetDescriptors = new EventSetDescriptor[] {
                    new EventSetDescriptor(DefaultTableDataModel.class, "dataModel", //NOI18N
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

    public synchronized MethodDescriptor[] getMethodDescriptors() {
        try {
            if (methodDescriptors == null) {
                methodDescriptors = new MethodDescriptor[] {
                    new MethodDescriptor(DefaultTableDataModel.class.getMethod("getRowData", null)), //NOI18N
                    new MethodDescriptor(DefaultTableDataModel.class.getMethod("getRowCount", null)) //NOI18N
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
                PropertyDescriptor rowData =
                    new PropertyDescriptor("rowData", DefaultTableDataModel.class, "getRowData", null); //NOI18N
                rowData.setHidden(true);

                PropertyDescriptor rowIndex =
                    new PropertyDescriptor("rowIndex", DefaultTableDataModel.class); //NOI18N
                rowIndex.setHidden(true);

                PropertyDescriptor rowCount =
                    new PropertyDescriptor("rowCount", DefaultTableDataModel.class, "getRowCount", null); //NOI18N

                PropertyDescriptor wrappedData =
                    new PropertyDescriptor("wrappedData", DefaultTableDataModel.class); //NOI18N
                wrappedData.setHidden(true);

                propertyDescriptors = new PropertyDescriptor[] {
                    rowData,
                    rowIndex,
                    rowCount,
                    wrappedData
                };
            }
            return propertyDescriptors;
        } catch (IntrospectionException e) {
            return null;
        }
    }
}
