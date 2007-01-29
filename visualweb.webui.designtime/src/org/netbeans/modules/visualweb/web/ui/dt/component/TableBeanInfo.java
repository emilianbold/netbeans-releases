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
package org.netbeans.modules.visualweb.web.ui.dt.component;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;

import com.sun.rave.designtime.Constants;
import com.sun.rave.web.ui.component.TableRowGroup;

/**
 * BeanInfo for the {@link org.netbeans.modules.visualweb.web.ui.dt.component.Table} component.
 *
 * @author Winston Prakash
 */

public class TableBeanInfo extends TableBeanInfoBase {

    public TableBeanInfo(){
        java.beans.PropertyEditorManager.registerEditor(Object.class, null);
        BeanDescriptor beanDescriptor = super.getBeanDescriptor();
        // Suppose to show up in a right-click "Add >" menu item.
        beanDescriptor.setValue(Constants.BeanDescriptor.PREFERRED_CHILD_TYPES,
                new String[] {TableRowGroup.class.getName()}
        );
        PropertyDescriptor[] descriptors = this.getPropertyDescriptors();
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i].getName().equals("actionListener")) //NOI18N
                descriptors[i].setHidden(true);
        }

        beanDescriptor.setValue(
            Constants.BeanDescriptor.INLINE_EDITABLE_PROPERTIES,
            new String[] { "*title://caption[@class='TblTtlTxt']", "footerText://span[@class='TblFtrRowTxt']" }); // NOI18N
    }

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDescriptor = super.getBeanDescriptor();
        // Do not allow component to be resized vertically
        //beanDescriptor.setValue(Constants.BeanDescriptor.RESIZE_CONSTRAINTS,
                //new Integer(Constants.ResizeConstraints.HORIZONTAL));
        return beanDescriptor;
    }
}

