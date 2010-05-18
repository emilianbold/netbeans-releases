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
