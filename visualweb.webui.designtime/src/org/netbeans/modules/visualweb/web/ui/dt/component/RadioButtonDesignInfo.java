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

import javax.faces.component.UIComponentBase;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.web.ui.component.RadioButton;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;

/** Design time behavior for a {@link org.netbeans.modules.visualweb.web.ui.dt.component.RadioButton}
 * component. The following design-time behavior is defined:
 * <ul>
 * <li>When a new <code>RadioButton</code> is dropped, set its
 * <code>label</code> property to the component's display name.</li>
 * <li>When a new <code>RadioButton</code> is dropped, set its <code>name</code>
 * property to the value of the <code>name</code> property of the nearest
 * radio button in the same parent container, or if none, to the id of the parent
 * container.
 * </ul>
 *
 * @author gjmurphy
 */
public class RadioButtonDesignInfo extends AbstractDesignInfo {

    public RadioButtonDesignInfo() {
        super(RadioButton.class);
    }

    public Result beanCreatedSetup(DesignBean bean) {
        DesignProperty label = bean.getProperty("label"); //NOI18N
        DesignProperty name = bean.getProperty("name"); //NOI18N
        label.setValue(
            bean.getBeanInfo().getBeanDescriptor().getDisplayName());
        DesignBean parent = bean.getBeanParent();
        for (int i = 0; i < parent.getChildBeanCount(); i++) {
            DesignBean child = parent.getChildBean(i);
            if (child.getBeanInfo().getBeanDescriptor().getBeanClass() == RadioButton.class
                    && child.getProperty("name").isModified()
                    && child != bean)
                name.setValue(child.getProperty("name").getValue());
        }
        if (name.getValue() == null) {
            if (parent.getInstance() instanceof UIComponentBase)
                name.setValue("radioButton-group-" + ((UIComponentBase)parent.getInstance()).getId());
            else
                name.setValue(((UIComponentBase)bean.getInstance()).getId());
        }
        return Result.SUCCESS;
    }

    protected DesignProperty getDefaultBindingProperty(DesignBean targetBean) {
        return targetBean.getProperty("selectedValue"); //NOI18N
    }

    public void propertyChanged(DesignProperty property, Object oldValue) {
        // If the value of this component's "selected" property was set to equal
        // the value of its "selectedValue" property, this indicates that the user
        // wanted the widget to be preselected at run-time. If this was the case,
        // and "selectedValue" has been changed, updated "selected" accordingly.
        if (property.getPropertyDescriptor().getName().equals("selectedValue")) {
            DesignProperty selectedProperty = property.getDesignBean().getProperty("selected");
            if (oldValue != null && oldValue.equals(selectedProperty.getValue()))
                selectedProperty.setValue(property.getValue());
        }
        super.propertyChanged(property, oldValue);
    }

}
