/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.openide.util.Exceptions;

public class RowSetDataModelBeanInfo extends SimpleBeanInfo {

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
            EventSetDescriptor[] eventSetDescriptors = new EventSetDescriptor[] {
                    new EventSetDescriptor(RowSetDataModel.class, "dataModel", //NOI18N
                    DataModelListener.class,
                    new String[] {
                    "rowSelected"
                }

                , //NOI18N
                    "addDataModelListener", "removeDataModelListener") //NOI18N
                };

            return eventSetDescriptors;
        } catch (IntrospectionException e) {
            Exceptions.printStackTrace(e);
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
            MethodDescriptor[] methodDescriptors = new MethodDescriptor[] {
                    new MethodDescriptor(RowSetDataModel.class.getMethod("clear", null)), //NOI18N
                    new MethodDescriptor(RowSetDataModel.class.getMethod("commit", null)), //NOI18N
                    new MethodDescriptor(RowSetDataModel.class.getMethod("reset", null)), //NOI18N
                    new MethodDescriptor(RowSetDataModel.class.getMethod("rollback", null)), //NOI18N
                };

            return methodDescriptors;
        } catch (NoSuchMethodException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

    public synchronized PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] propertyDescriptors;

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

            return propertyDescriptors;
        } catch (IntrospectionException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }
}
