/*
 *
 *          Copyright (c) 2005, SeeBeyond Technology Corporation,
 *          All Rights Reserved
 *
 *          This program, and all the routines referenced herein,
 *          are the proprietary properties and trade secrets of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 *          Except as provided for by license agreement, this
 *          program shall not be duplicated, used, or disclosed
 *          without  written consent signed by an officer of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 */
package org.netbeans.modules.dm.virtual.db.ui.model;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openide.util.NbBundle;

/**
 * Concrete class to expose delimited-specific virtual table properties. 
 * 
 * @author Ahimanikya Satapathy
 */
public class DelimitedFlatfileBeanInfo extends VirtualTableBeanInfo {

    private static BeanDescriptor beanDescriptor = null;
    private static PropertyDescriptor[] properties = null;

    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(DelimitedFlatfile.class);
        }

        return beanDescriptor;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        if (properties == null) {
            List myProps = new ArrayList(Arrays.asList(super.getPropertyDescriptors()));
            try {
                PropertyDescriptor pd = new PropertyDescriptor("fieldDelimiter", DelimitedFlatfile.class, "getFieldDelimiter", null); // NOI18N
                String label = NbBundle.getMessage(DelimitedFlatfileBeanInfo.class, "LBL_field_delimiter"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }
            try {
                PropertyDescriptor pd = new PropertyDescriptor("fieldQualifier", DelimitedFlatfile.class, "getTextQualifier", null); // NOI18N
                String label = NbBundle.getMessage(DelimitedFlatfileBeanInfo.class, "LBL_prec_qualifier"); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            properties = (PropertyDescriptor[]) myProps.toArray(new PropertyDescriptor[myProps.size()]);
        }

        return properties;
    }
}