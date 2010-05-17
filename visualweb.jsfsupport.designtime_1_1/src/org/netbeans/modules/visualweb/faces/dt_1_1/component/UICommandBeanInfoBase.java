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

package org.netbeans.modules.visualweb.faces.dt_1_1.component;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.markup.AttributeDescriptor;
import org.netbeans.modules.visualweb.faces.dt.PropertyDescriptorBase;
import com.sun.rave.faces.event.Action;
import java.beans.EventSetDescriptor;
import java.lang.reflect.Method;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;


public abstract class UICommandBeanInfoBase extends UIComponentBeanInfoBase {

    protected static ResourceBundle resources =
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.faces.dt_1_1.component.Bundle", Locale.getDefault(),
            UICommandBeanInfoBase.class.getClassLoader());

    public UICommandBeanInfoBase() {
        beanClass = javax.faces.component.UICommand.class;
        defaultPropertyName = "value";
        iconFileName_C16 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/UICommand_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/UICommand_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/UICommand_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/faces/dt_1_1/component/UICommand_M32";
    }


    private PropertyDescriptor[] propertyDescriptors;

    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propertyDescriptors != null) {
            return propertyDescriptors;
        }
        AttributeDescriptor attrib = null;

        try {

            PropertyDescriptor prop_action = new PropertyDescriptorBase("action",beanClass,"getAction","setAction");
            prop_action.setDisplayName(resources.getString("UICommand_action_DisplayName"));
            prop_action.setShortDescription(resources.getString("UICommand_action_Description"));
            prop_action.setPropertyEditorClass(com.sun.rave.propertyeditors.MethodBindingPropertyEditor.class);
            prop_action.setExpert(false);
            prop_action.setHidden(true);
            prop_action.setPreferred(false);
            attrib = new AttributeDescriptor("action",false,null,true);
            prop_action.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_action.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_actionListener = new PropertyDescriptorBase("actionListener",beanClass,"getActionListener","setActionListener");
            prop_actionListener.setDisplayName(resources.getString("UICommand_actionListener_DisplayName"));
            prop_actionListener.setShortDescription(resources.getString("UICommand_actionListener_Description"));
            prop_actionListener.setPropertyEditorClass(com.sun.rave.propertyeditors.MethodBindingPropertyEditor.class);
            prop_actionListener.setExpert(false);
            prop_actionListener.setHidden(false);
            prop_actionListener.setPreferred(false);
            attrib = new AttributeDescriptor("actionListener",false,null,true);
            prop_actionListener.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_actionListener.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_immediate = new PropertyDescriptorBase("immediate",beanClass,"isImmediate","setImmediate");
            prop_immediate.setDisplayName(resources.getString("UICommand_immediate_DisplayName"));
            prop_immediate.setShortDescription(resources.getString("UICommand_immediate_Description"));
            prop_immediate.setExpert(false);
            prop_immediate.setHidden(false);
            prop_immediate.setPreferred(false);
            attrib = new AttributeDescriptor("immediate",false,null,true);
            prop_immediate.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_immediate.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_value = new PropertyDescriptorBase("value",beanClass,"getValue","setValue");
            prop_value.setDisplayName(resources.getString("UICommand_value_DisplayName"));
            prop_value.setShortDescription(resources.getString("UICommand_value_Description"));
            prop_value.setPropertyEditorClass(org.netbeans.modules.visualweb.faces.dt.std.ValueBindingPropertyEditor.class);
            prop_value.setExpert(false);
            prop_value.setHidden(false);
            prop_value.setPreferred(false);
            attrib = new AttributeDescriptor("value",false,null,true);
            prop_value.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_value.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            prop_value.setValue("ignoreIsBound", "true");

            List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
            propertyDescriptorList.add(prop_action);
            propertyDescriptorList.add(prop_actionListener);
            propertyDescriptorList.add(prop_immediate);
            propertyDescriptorList.add(prop_value);

            propertyDescriptorList.addAll(Arrays.asList(super.getPropertyDescriptors()));
            propertyDescriptors = propertyDescriptorList.toArray(new PropertyDescriptor[propertyDescriptorList.size()]);
            return propertyDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }
    
    private EventSetDescriptor[] eventSetDescriptors;
    
    public EventSetDescriptor[] getEventSetDescriptors() {
        if (eventSetDescriptors == null) {
            try {
                eventSetDescriptors = new EventSetDescriptor[] {
                    new EventSetDescriptor("action", Action.class,  //NOI18N
                            new Method[] {Action.class.getMethod("action", new Class[] {})},  //NOI18N
                            null, null),
                    new EventSetDescriptor("actionListener", ActionListener.class,  //NOI18N
                            new Method[] {ActionListener.class.getMethod("processAction", new Class[] {ActionEvent.class})},  //NOI18N
                            null, null)
                };
                eventSetDescriptors[0].setValue(Constants.EventSetDescriptor.BINDING_PROPERTY, getPropertyDescriptor("action"));  //NOI18N
                String defaultHandler = resources.getString("UICommand_actionHandler"); // NOI18N
                eventSetDescriptors[0].setValue(Constants.EventDescriptor.DEFAULT_EVENT_BODY, defaultHandler);
                eventSetDescriptors[1].setValue(Constants.EventSetDescriptor.BINDING_PROPERTY, getPropertyDescriptor("actionListener"));  //NOI18N
                eventSetDescriptors[1].setHidden(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return eventSetDescriptors;
    }

}

