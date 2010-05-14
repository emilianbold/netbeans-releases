/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dm.virtual.db.ui.model;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.ArrayList;
import java.util.List;

import org.openide.util.NbBundle;

/**
 * Exposes getters for virtual database column properties. 
 * 
 * @author Ahimanikya Satapathy
 */
public class VirtualColumnBeanInfo extends SimpleBeanInfo {

    private static BeanDescriptor beanDescriptor = null;
    private static PropertyDescriptor[] properties = null;
    private static EventSetDescriptor[] eventSets = null;
    private static MethodDescriptor[] methods = null;

    @Override
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(VirtualColumn.class);
        }

        return beanDescriptor;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        if (properties == null) {
            List myProps = new ArrayList();
            try {
                PropertyDescriptor pd = new PropertyDescriptor("name", VirtualColumn.class, "getName", null); // NOI18N
                String label = NbBundle.getMessage(VirtualColumnBeanInfo.class, "LBL_column_name"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("ordinalPosition", VirtualColumn.class, "getOrdinalPosition", null); // NOI18N
                String label = NbBundle.getMessage(VirtualColumnBeanInfo.class, "LBL_column_position");// NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("precision", VirtualColumn.class, "getPrecision", null); // NOI18N
                String label = NbBundle.getMessage(VirtualColumnBeanInfo.class, "LBL_prec_length"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("scale", VirtualColumn.class, "getScale", null); // NOI18N
                String label = NbBundle.getMessage(VirtualColumnBeanInfo.class, "LBL_scale"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("sqlType", VirtualColumn.class, "getJdbcTypeString", null); // NOI18N
                String label = NbBundle.getMessage(VirtualColumnBeanInfo.class, "LBL_sql_type"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }
            try {
                PropertyDescriptor pd = new PropertyDescriptor("nullable", VirtualColumn.class, "isNullable", null); // NOI18N
                String label = NbBundle.getMessage(VirtualColumnBeanInfo.class, "LBL_nullable"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("primaryKey", VirtualColumn.class, "isPrimaryKey", null); // NOI18N
                String label = NbBundle.getMessage(VirtualColumnBeanInfo.class, "LBL_primary_key"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("foreignKey", VirtualColumn.class, "isForeignKey", null); // NOI18N
                String label = NbBundle.getMessage(VirtualColumnBeanInfo.class, "LBL_foreign_key"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }
            try {
                PropertyDescriptor pd = new PropertyDescriptor("defaultValue", VirtualColumn.class, "getDefaultValue", null); // NOI18N
                String label = NbBundle.getMessage(VirtualColumnBeanInfo.class, "LBL_default_value"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            properties = (PropertyDescriptor[]) myProps.toArray(new PropertyDescriptor[myProps.size()]);
        }

        return properties;
    }

    @Override
    public EventSetDescriptor[] getEventSetDescriptors() {
        if (eventSets == null) {
            eventSets = new EventSetDescriptor[0];
        }

        return eventSets;
    }

    @Override
    public MethodDescriptor[] getMethodDescriptors() {
        if (methods == null) {
            methods = new MethodDescriptor[0];
        }

        return methods;
    }
}