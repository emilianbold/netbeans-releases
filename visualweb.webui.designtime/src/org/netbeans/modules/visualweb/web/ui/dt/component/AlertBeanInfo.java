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

import com.sun.rave.faces.event.Action;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import com.sun.rave.designtime.Constants;
import java.lang.reflect.Method;

/**
 * BeanInfo for the {@link org.netbeans.modules.visualweb.web.ui.dt.component.Alert} component.
 *
 * @author gjmurphy
 */
public class AlertBeanInfo extends AlertBeanInfoBase {

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDescriptor = super.getBeanDescriptor();
        beanDescriptor.setValue(
                Constants.BeanDescriptor.INLINE_EDITABLE_PROPERTIES,
                new String[] { "*summary://div[@class='AlrtErrTxt']", "detail://div[@class='AlrtMsgTxt']" }); // NOI18N
        PropertyDescriptor[] descriptors = this.getPropertyDescriptors();
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i].getName().equals("linkAction")) //NOI18N
                descriptors[i].setHidden(true);
        }
        return beanDescriptor;
    }

    EventSetDescriptor[] eventSetDescriptors;

    public EventSetDescriptor[] getEventSetDescriptors() {
        if (eventSetDescriptors == null) {
            try {
                PropertyDescriptor actionDescriptor = null;
                PropertyDescriptor[] propertyDescriptors = this.getPropertyDescriptors();
                for (int i = 0; i < propertyDescriptors.length && actionDescriptor == null; i++) {
                    if (propertyDescriptors[i].getName().equals("linkAction")) //NOI18N
                        actionDescriptor = propertyDescriptors[i];
                }
                EventSetDescriptor actionEventDescriptor =
                        new EventSetDescriptor("linkAction", Action.class,  //NOI18N
                        new Method[] {Action.class.getMethod("action", new Class[] {})},  //NOI18N
                        null, null);
                actionEventDescriptor.setDisplayName(actionDescriptor.getDisplayName());
                actionEventDescriptor.setValue(Constants.EventSetDescriptor.BINDING_PROPERTY,
                        actionDescriptor);
                actionEventDescriptor.setShortDescription(actionDescriptor.getShortDescription());
                eventSetDescriptors = new EventSetDescriptor[] {actionEventDescriptor};
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return eventSetDescriptors;
    }



}
