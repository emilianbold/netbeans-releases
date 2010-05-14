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
 * Abstract BeanInfo implementation to expose read-only access to selected table
 * properties.
 * 
 * @author Ahimanikya Satapathy
 */
public abstract class VirtualTableBeanInfo extends SimpleBeanInfo {

    private static PropertyDescriptor[] properties = null;
    private static EventSetDescriptor[] eventSets = null;
    private static MethodDescriptor[] methods = null;

    @Override
    public abstract BeanDescriptor getBeanDescriptor();

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        if (properties == null) {
            List myProps = new ArrayList();

            try {
                PropertyDescriptor pd = new PropertyDescriptor("tableName", VirtualTable.class, "getTableName", null); // NOI18N
                String label = NbBundle.getMessage(VirtualTableBeanInfo.class, "LBL_table_name"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("fileType", VirtualTable.class, "getFileType", null); // NOI18N
                String label = NbBundle.getMessage(VirtualTableBeanInfo.class, "LBL_file_type"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("fileName", VirtualTable.class, "getFileName", null); // NOI18N
                String label = NbBundle.getMessage(VirtualTableBeanInfo.class, "LBL_file_name");// NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("encodingScheme", VirtualTable.class, "getEncodingScheme", null); // NOI18N
                String label = NbBundle.getMessage(VirtualTableBeanInfo.class, "LBL_encoding_scheme"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("rowsToSkip", VirtualTable.class, "getRowsToSkip", null); // NOI18N
                String label = NbBundle.getMessage(VirtualTableBeanInfo.class, "LBL_rows_to_skip"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("maxFaults", VirtualTable.class, "getMaxFaults", null); // NOI18N
                String label = NbBundle.getMessage(VirtualTableBeanInfo.class, "LBL_max_faults"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("isFirstLineHeader", VirtualTable.class, "isFirstLineHeader", null); // NOI18N
                String label = NbBundle.getMessage(VirtualTableBeanInfo.class, "LBL_isfirstlineheader"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("recordDelimiter", VirtualTable.class, "getRecordDelimiter", null); // NOI18N
                String label = NbBundle.getMessage(VirtualTableBeanInfo.class, "LBL_record_delimiter"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("trimWhiteSpace", VirtualTable.class, "enableWhiteSpaceTrimming", null); // NOI18N
                String label = NbBundle.getMessage(VirtualTableBeanInfo.class, "LBL_white_space"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
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
