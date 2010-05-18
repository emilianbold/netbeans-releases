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
