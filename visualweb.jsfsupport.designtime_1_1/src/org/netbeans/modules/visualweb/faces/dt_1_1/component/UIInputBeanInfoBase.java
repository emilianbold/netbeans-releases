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
import java.beans.EventSetDescriptor;
import java.lang.reflect.Method;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.validator.Validator;


public abstract class UIInputBeanInfoBase extends UIOutputBeanInfoBase {

    protected static ResourceBundle resources =
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.faces.dt_1_1.component.Bundle", Locale.getDefault(), UIInputBeanInfoBase.class.getClassLoader());


    public UIInputBeanInfoBase() {
        beanClass = javax.faces.component.UIInput.class;
        defaultPropertyName = "value";
    }


    private PropertyDescriptor[] propertyDescriptors;

    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propertyDescriptors != null) {
            return propertyDescriptors;
        }
        AttributeDescriptor attrib = null;

        try {

            PropertyDescriptor prop_immediate = new PropertyDescriptorBase("immediate",beanClass,"isImmediate","setImmediate");
            prop_immediate.setDisplayName(resources.getString("UIInput_immediate_DisplayName"));
            prop_immediate.setShortDescription(resources.getString("UIInput_immediate_Description"));
            prop_immediate.setExpert(false);
            prop_immediate.setHidden(false);
            prop_immediate.setPreferred(false);
            attrib = new AttributeDescriptor("immediate",false,null,true);
            prop_immediate.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_immediate.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_localValueSet = new PropertyDescriptorBase("localValueSet",beanClass,"isLocalValueSet","setLocalValueSet");
            prop_localValueSet.setDisplayName(resources.getString("UIInput_localValueSet_DisplayName"));
            prop_localValueSet.setShortDescription(resources.getString("UIInput_localValueSet_Description"));
            prop_localValueSet.setExpert(false);
            prop_localValueSet.setHidden(true);
            prop_localValueSet.setPreferred(false);
            prop_localValueSet.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_required = new PropertyDescriptorBase("required",beanClass,"isRequired","setRequired");
            prop_required.setDisplayName(resources.getString("UIInput_required_DisplayName"));
            prop_required.setShortDescription(resources.getString("UIInput_required_Description"));
            prop_required.setExpert(false);
            prop_required.setHidden(false);
            prop_required.setPreferred(false);
            attrib = new AttributeDescriptor("required",false,null,true);
            prop_required.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_required.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_submittedValue = new PropertyDescriptorBase("submittedValue",beanClass,"getSubmittedValue","setSubmittedValue");
            prop_submittedValue.setDisplayName(resources.getString("UIInput_submittedValue_DisplayName"));
            prop_submittedValue.setShortDescription(resources.getString("UIInput_submittedValue_Description"));
            prop_submittedValue.setExpert(false);
            prop_submittedValue.setHidden(true);
            prop_submittedValue.setPreferred(false);
            prop_submittedValue.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);
            
            PropertyDescriptor prop_validator = new PropertyDescriptorBase("validator",beanClass,"getValidator","setValidator");
            prop_validator.setDisplayName(resources.getString("UIInput_validator_DisplayName"));
            prop_validator.setShortDescription(resources.getString("UIInput_validator_Description"));
            prop_validator.setPropertyEditorClass(com.sun.rave.propertyeditors.ValidatorPropertyEditor.class);
            prop_validator.setExpert(false);
            prop_validator.setHidden(false);
            prop_validator.setPreferred(false);
            attrib = new AttributeDescriptor("validator",false,null,true);
            prop_validator.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_validator.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            
            PropertyDescriptor prop_valueChangeListener = new PropertyDescriptorBase("valueChangeListener",beanClass,"getValueChangeListener","setValueChangeListener");
            prop_valueChangeListener.setDisplayName(resources.getString("UIInput_valueChangeListener_DisplayName"));
            prop_valueChangeListener.setShortDescription(resources.getString("UIInput_valueChangeListener_Description"));
            prop_valueChangeListener.setPropertyEditorClass(com.sun.rave.propertyeditors.MethodBindingPropertyEditor.class);
            prop_valueChangeListener.setExpert(false);
            prop_valueChangeListener.setHidden(false);
            prop_valueChangeListener.setPreferred(false);
            attrib = new AttributeDescriptor("valueChangeListener",false,null,true);
            prop_valueChangeListener.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_valueChangeListener.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            
            List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
            propertyDescriptorList.add(prop_immediate);
            propertyDescriptorList.add(prop_localValueSet);
            propertyDescriptorList.add(prop_required);
            propertyDescriptorList.add(prop_submittedValue);
            propertyDescriptorList.add(prop_validator);
            propertyDescriptorList.add(prop_valueChangeListener);
            
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
                    new EventSetDescriptor("validate", Validator.class,  //NOI18N
                            new Method[] {
                        Validator.class.getMethod("validate",  //NOI18N
                                new Class[] {FacesContext.class, UIComponent.class, Object.class})},
                            null, null),
                    new EventSetDescriptor("valueChange", ValueChangeListener.class,  //NOI18N
                            new Method[] {
                        ValueChangeListener.class.getMethod("processValueChange",  //NOI18N
                                new Class[] {ValueChangeEvent.class})},
                            null, null),
                };
                eventSetDescriptors[0].setValue(Constants.EventSetDescriptor.BINDING_PROPERTY,
                        getPropertyDescriptor("validator"));  //NOI18N
                eventSetDescriptors[0].setValue(Constants.EventDescriptor.DEFAULT_EVENT_BODY, 
                        resources.getString("UIInput_validateHandler")); //NOI18N
                eventSetDescriptors[0].setValue(Constants.EventDescriptor.PARAMETER_NAMES, 
                        new String[] { "context", "component", "value" });
                eventSetDescriptors[0].setValue(Constants.EventDescriptor.REQUIRED_IMPORTS, 
                        new String[] { "javax.faces.application.FacesMessage", "javax.faces.validator.ValidatorException" });
                eventSetDescriptors[1].setValue(Constants.EventSetDescriptor.BINDING_PROPERTY,
                        getPropertyDescriptor("valueChangeListener"));  //NOI18N
                eventSetDescriptors[1].setValue(Constants.EventDescriptor.PARAMETER_NAMES, 
                        new String[] { "vce" });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return eventSetDescriptors;
    }
    
}

