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

import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignUtil;
import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import com.sun.rave.designtime.Constants;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;

/**
 * BeanInfo for the {@link org.netbeans.modules.visualweb.web.ui.dt.component.Field} component.
 *
 * @author gjmurphy
 */
public class FieldBeanInfo extends FieldBeanInfoBase {

    public FieldBeanInfo() {
    }

    private EventSetDescriptor[] eventSetDescriptors;

    public EventSetDescriptor[] getEventSetDescriptors() {
        if (eventSetDescriptors == null) {
            eventSetDescriptors = DesignUtil.generateInputEventSetDescriptors(this);
            for (int i = 0; i < eventSetDescriptors.length; i++) {
                if (eventSetDescriptors[i].getName().equals("validator")) {
                    eventSetDescriptors[i].setValue(
                            Constants.EventDescriptor.DEFAULT_EVENT_BODY,
                            DesignMessageUtil.getMessage(FieldBeanInfo.class, "fieldHandler"));
                }
            }
        }
        return eventSetDescriptors;
    }

    public int getDefaultEventIndex() {
        return 0;
    }
}
