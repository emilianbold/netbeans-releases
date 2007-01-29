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
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.validator.Validator;
import com.sun.rave.designtime.Constants;

/**
 * BeanInfo for the {@link org.netbeans.modules.visualweb.web.ui.dt.component.EditableList} component.
 */
public class EditableListBeanInfo extends EditableListBeanInfoBase {

    /** Creates a new instance of EditableListBeanInfo */
    public EditableListBeanInfo() {
        BeanDescriptor beanDescriptor = super.getBeanDescriptor();
        PropertyDescriptor[] descriptors = this.getPropertyDescriptors();
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i].getName().equals("valueChangeListener")) //NOI18N
                descriptors[i].setHidden(true);
        }
    }

    private EventSetDescriptor[] eventSetDescriptors;

    public EventSetDescriptor[] getEventSetDescriptors() {
        try {
            if (eventSetDescriptors == null) {
                eventSetDescriptors = new EventSetDescriptor[] {
                        new EventSetDescriptor("valueChangeListener", ValueChangeListener.class,  //NOI18N
                                new Method[] {
                                    ValueChangeListener.class.getMethod("processValueChange",  //NOI18N
                                        new Class[] {ValueChangeEvent.class})},
                                null, null)
                };
            }
            PropertyDescriptor[] propertyDescriptors = this.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                if (propertyDescriptors[i].getName().equals("valueChangeListener")) { //NOI18N
                    eventSetDescriptors[0].setValue(Constants.EventSetDescriptor.BINDING_PROPERTY,
                            propertyDescriptors[i]);
                    eventSetDescriptors[0].setShortDescription(propertyDescriptors[i].getShortDescription());
                }
            }
            return eventSetDescriptors;
        } catch (Exception e) {
            return null;
        }
    }

    public int getDefaultEventIndex() {
        return 0;
    }
}
