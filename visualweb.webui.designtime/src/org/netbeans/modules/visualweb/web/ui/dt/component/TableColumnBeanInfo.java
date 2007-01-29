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
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import com.sun.rave.designtime.Constants;
import com.sun.rave.web.ui.component.*;

/**
 * BeanInfo for the {@link org.netbeans.modules.visualweb.web.ui.dt.component.TableColumn} component.
 *
 * @author Winston Prakash
 */
public class TableColumnBeanInfo extends TableColumnBeanInfoBase {
    public TableColumnBeanInfo(){
        BeanDescriptor beanDescriptor = super.getBeanDescriptor();
        // Suppose to show up in a right-click "Add >" menu item.
        beanDescriptor.setValue(Constants.BeanDescriptor.PREFERRED_CHILD_TYPES, new String[] {
            //TableColumn.class.getName(),
            StaticText.class.getName(),
            TextField.class.getName(),
            TextArea.class.getName(),
            Button.class.getName(),
            Label.class.getName(),
            Hyperlink.class.getName(),
            ImageHyperlink.class.getName(),
            DropDown.class.getName(),
            Checkbox.class.getName(),
            RadioButton.class.getName(),
            ImageComponent.class.getName(),
            PanelGroup.class.getName(),
            Message.class.getName()
        });
        //Doesn't work well yet
        //beanDescriptor.setValue(
        //    Constants.BeanDescriptor.INLINE_EDITABLE_PROPERTIES,
        //    //new String[] { "*headerText://a" }); // NOI18N
        //    new String[] { "*headerText" }); // NOI18N

        PropertyDescriptor[] descriptors = this.getPropertyDescriptors();
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i].getName().equals("actionListener")) //NOI18N
                descriptors[i].setHidden(true);
        }
    }
}
