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
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import javax.faces.model.DataModelListener;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.base.CategoryDescriptors;
import com.sun.rave.faces.data.CachedRowSetDataModel;
import org.openide.util.Exceptions;

public class CachedRowSetDataModelBeanInfo extends SimpleBeanInfo {

    public CachedRowSetDataModelBeanInfo() throws NoSuchMethodException {}

    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(CachedRowSetDataModel.class, null);
            //beanDescriptor.setValue(Constants.BeanDescriptor.TRAY_COMPONENT, Boolean.FALSE);
    }

    //public int getDefaultEventIndex() {}

    @Override
    public int getDefaultPropertyIndex() {
        return 0;
    }

    @Override
    public EventSetDescriptor[] getEventSetDescriptors() {
        try {
            EventSetDescriptor[] eventSetDescriptors = new EventSetDescriptor[] {
                    new EventSetDescriptor(CachedRowSetDataModel.class, "dataModel", //NOI18N
                    DataModelListener.class,
                    new String[] { "rowSelected" }, //NOI18N
                    "addDataModelListener", "removeDataModelListener") //NOI18N
            };

            return eventSetDescriptors;
        } catch (IntrospectionException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

    @Override
    public Image getIcon(int iconKind) {
        switch (iconKind) {
            case BeanInfo.ICON_COLOR_16x16:
                return loadImage("CachedRowSetDataModelIconColor16.png"); //NOI18N
            case BeanInfo.ICON_COLOR_32x32:
                return loadImage("CachedRowSetDataModelIconColor32.gif"); //NOI18N
            case BeanInfo.ICON_MONO_16x16:
                return loadImage("CachedRowSetDataModelIconMono16.gif"); //NOI18N
            case BeanInfo.ICON_MONO_32x32:
                return loadImage("CachedRowSetDataModelIconMono32.gif"); //NOI18N
        }
        return null;
    }

    public synchronized PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] propertyDescriptors;

            PropertyDescriptor cachedRowSet =
                new PropertyDescriptor("cachedRowSet", CachedRowSetDataModel.class, "getCachedRowSet",
                "setCachedRowSet"); //NOI18N
            cachedRowSet.setValue(Constants.PropertyDescriptor.CATEGORY,
                CategoryDescriptors.ADVANCED);

            propertyDescriptors = new PropertyDescriptor[] {
                cachedRowSet
            };
            return propertyDescriptors;
        } catch (IntrospectionException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }
}
