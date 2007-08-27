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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.beansbinding.Converter;
import org.jdesktop.beansbinding.Validator;
import org.openide.nodes.Node;

import org.openide.nodes.PropertySupport;

public class BindingProperty extends PropertySupport.ReadWrite<MetaBinding> {

    private RADComponent bindingComponent;
    private BindingDescriptor bindingDescriptor;
    private MetaBinding binding;
    private Property nameProperty;
    private Property nullValueProperty;
    private Property incompleteValueProperty;
    private Property validatorProperty;
    private Property converterProperty;

    public BindingProperty(RADComponent metacomp, BindingDescriptor desc) {
        super(desc.getPath(), MetaBinding.class, desc.getDisplayName(), desc.getShortDescription());
        bindingComponent = metacomp;
        bindingDescriptor = desc;
        FormProperty prop = (FormProperty)bindingComponent.getPropertyByName(bindingDescriptor.getPath());
        if (prop == null) {
            // Can we have a component with a binding property and no regular property?
            prop = bindingComponent.getAllBeanProperties()[0];
        }
        if (prop != null) {
            String name = FormUtils.getBundleString("MSG_Binding_NullProperty"); // NOI18N
            nullValueProperty = new Property(prop, "nullValue", desc.getValueType(), name, name, false); // NOI18N
            name = FormUtils.getBundleString("MSG_Binding_IncompletePathProperty"); // NOI18N
            incompleteValueProperty = new Property(prop, "incompletePathValue", desc.getValueType(), name, name, false); // NOI18N
            name = FormUtils.getBundleString("MSG_Binding_Validator"); // NOI18N
            validatorProperty = new Property(prop, "validator", Validator.class, name, name, true); // NOI18N
            name = FormUtils.getBundleString("MSG_Binding_Converter"); // NOI18N
            converterProperty = new Property(prop, "converter", Converter.class, name, name, true); // NOI18N
            name = FormUtils.getBundleString("MSG_Binding_Name"); // NOI18N
            nameProperty = new Property(prop, "name", String.class, name, name, true); // NOI18N
        }
    }

    @Override
    public String getHtmlDisplayName() {
        return binding != null ? "<b>" + getDisplayName() : null; // NOI18N
    }

    public MetaBinding getValue() {
        return binding;
    }

    public void setValue(MetaBinding val) {
        MetaBinding old = binding;
        if ((old == null) && (val != null)) FormEditor.updateProjectForBeansBinding(bindingComponent.getFormModel());
        binding = val;
        FormEditor.getBindingSupport(getFormModel()).changeBindingInModel(old, binding);

        getFormModel().fireBindingChanged(
                getBindingComponent(), getBindingPath(), old, binding);
        RADComponentNode node = getBindingComponent().getNodeReference();
        if (node != null) {
            node.firePropertyChangeHelper(
                null, null, null); // this will cause resetting the bean property (e.g. JTable.model)
        }
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public void restoreDefaultValue() {
        setValue(null);
        try {
            validatorProperty.restoreDefaultValue();
            converterProperty.restoreDefaultValue();
            nameProperty.restoreDefaultValue();
            nullValueProperty.setValue(null);
            incompleteValueProperty.setValue(null);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        Node.Property prop = bindingComponent.getPropertyByName(bindingDescriptor.getPath());
        if ((prop != null) && prop.supportsDefaultValue()) {
            try {
                prop.restoreDefaultValue();
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
        }
    }

    @Override
    public boolean isDefaultValue() {
        return (getValue() == null);
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new BindingPropertyEditor();
    }

    public RADComponent getBindingComponent() {
        return bindingComponent;
    }

    public BindingDescriptor getBindingDescriptor() {
        return bindingDescriptor;
    }

    String getBindingPath() {
        return bindingDescriptor.getPath();
    }

    Class getBindingValueType() {
        return bindingDescriptor.getValueType();
    }

    private FormModel getFormModel() {
        return bindingComponent.getFormModel();
    }

    FormProperty getNullValueProperty() {
        return nullValueProperty;
    }

    FormProperty getIncompleteValueProperty() {
        return incompleteValueProperty;
    }

    FormProperty getValidatorProperty() {
        return validatorProperty;
    }

    FormProperty getConverterProperty() {
        return converterProperty;
    }

    FormProperty getNameProperty() {
        return nameProperty;
    }

    // -----

    private class BindingPropertyEditor extends PropertyEditorSupport { //implements ExPropertyEditor

        private BindingCustomizer customizer;
        private ActionListener customizerListener;

        @Override
        public String getAsText() {
            RADComponent boundComp = null;
            String path = null;
            if (binding != null) {
                boundComp = binding.getSource();
                path = binding.getSourcePath();
            }

            if (boundComp == null)
                return ""; // NOI18N

            return path != null ?
                   boundComp.getName() + "[" + path + "]" : // NOI18N
                   boundComp.getName();
        }

        @Override
        public void setAsText(String text) {
            if ("".equals(text)) { // NOI18N
                setValue(null);
            }
            else {
                int idx = text.indexOf('['); // NOI18N
                String compName = idx >= 0 ? text.substring(0, idx) : text;
                RADComponent boundComp = getFormModel().findRADComponent(compName);
                if (boundComp != null) {
                    String path = idx >= 0 ? text.substring(idx+1, text.length()-1) : ""; // NOI18N
                    if (!path.equals("")) { // NOI18N
                        if (boundComp != getBindingComponent() || !path.equals(getBindingPath())) {
                            setValue(new MetaBinding(boundComp, path, getBindingComponent(), getBindingPath()));
                        }
                    }
                    else if (boundComp != getBindingComponent()
                             && Collection.class.isAssignableFrom(getBindingValueType())
                             && getBindingValueType().equals(boundComp.getBeanClass()))
                    {   // bind directly to the component
                        setValue(new MetaBinding(boundComp, null, getBindingComponent(), getBindingPath()));
                    }
                }
            }
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public Component getCustomEditor() {
            if (customizer == null) {
                customizer = new BindingCustomizer(BindingProperty.this);
                customizerListener = new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        setValue(customizer.getBinding());
                    }
                };
            }
            customizer.setBinding((MetaBinding)getValue());
            return customizer.getDialog(customizerListener);
        }

    }
    
    static class Property extends FormProperty {
        private Object value;
        private boolean supportsDefaultValue;

        Property(FormProperty prop, String name, Class type, String displayName, String description, boolean supportsDefaultValue) {
            // PENDING override getContextPath
            super(new FormPropertyContext.SubProperty(prop), name, type, displayName, description);
            this.supportsDefaultValue = supportsDefaultValue;
        }

        public Object getTargetValue() throws IllegalAccessException, InvocationTargetException {
            return value;
        }

        public void setTargetValue(Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            this.value = value;
        }

        @Override
        public boolean supportsDefaultValue () {
            return supportsDefaultValue;
        }

    }
    
}
