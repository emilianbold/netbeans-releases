/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/* $Id$ */

package org.netbeans.modules.form;

import java.beans.*;
import java.lang.reflect.*;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 * Implementation of properties for (meta)components (class RADComponent).
 * RADComponent is used to get the component instance and
 * PropertyDescriptor provides read and write methods to get and set
 * property values.
 *
 * @author Tomas Pavek
 */
public class RADProperty extends FormProperty {

    private RADComponent component;
    private PropertyDescriptor desc;

    RADProperty(RADComponent metacomp, PropertyDescriptor propdesc) {
        super(new RADPropertyContext(metacomp),
              propdesc.getName(),
              propdesc.getPropertyType(),
              propdesc.getDisplayName(),
              propdesc.getShortDescription());

        component = metacomp;
        desc = propdesc;

        if (desc.getWriteMethod() == null)
            setAccessType(NO_WRITE);
        else if (desc.getReadMethod() == null)
            setAccessType(DETACHED_READ);
    }

    public RADComponent getRADComponent() {
        return component;
    }

    public PropertyDescriptor getPropertyDescriptor() {
        return desc;
    }

    // -------------------------------

    public Object getTargetValue() throws IllegalAccessException,
                                          InvocationTargetException {
        Method readMethod = desc.getReadMethod();
        if (readMethod == null) {
            throw new IllegalAccessException("Not a readable property: "+desc.getName());
        }
        return readMethod.invoke(component.getBeanInstance(), new Object[0]);
    }

    public void setTargetValue(Object value) throws IllegalAccessException,
                                                 IllegalArgumentException,
                                                 InvocationTargetException {
        Method writeMethod = desc.getWriteMethod();
        if (writeMethod == null) {
            throw new IllegalAccessException("Not a writeable property: "+desc.getName());
        }
        writeMethod.invoke(component.getBeanInstance(), new Object[] { value });
    }

    public void setValue(Object value) throws IllegalAccessException,
                                              IllegalArgumentException,
                                              InvocationTargetException {
        super.setValue(value);

        component.debugChangedValues(); // do we need this??
    }

    protected Object getRealValue(Object value) {
        Object realValue = super.getRealValue(value);

        if (realValue == FormDesignValue.IGNORED_VALUE
              && component.getBeanInstance() instanceof java.awt.Component 
              && "text".equals(desc.getName())) // NOI18N
            realValue = ((FormDesignValue)value).getDescription();

        return realValue;
    }

    public boolean supportsDefaultValue() {
        return BeanSupport.NO_VALUE != BeanSupport.getDefaultPropertyValue(
                                         component.getBeanClass(), getName());
    }

    public Object getDefaultValue() {
        return BeanSupport.getDefaultPropertyValue(component.getBeanClass(), getName());
    }

    // ----------

    public boolean canWrite() {
         return component.isReadOnly() ? false : super.canWrite();
    }

    // ----------

    public PropertyEditor getExpliciteEditor() {
        if (desc.getPropertyEditorClass() != null) {
            try {
                return (PropertyEditor) desc.getPropertyEditorClass().newInstance();
            }
            catch (Exception ex) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace();
            }
        }
        return null;
    }

    public void setPreCode(String value) {
        if ((preCode == null && value != null)
                || (preCode != null && !preCode.equals(value))) {
            preCode = value;
            if (isChangeFiring() && component.getFormModel() != null)
                component.getFormModel().fireComponentPropertyChanged(
                    component, desc.getName(), null, null);
        }
    }

    public void setPostCode(String value) {
        if ((postCode == null && value != null)
                || (postCode != null && !postCode.equals(value))) {
            postCode = value;
            if (isChangeFiring() && component.getFormModel() != null)
                component.getFormModel().fireComponentPropertyChanged(
                    component, desc.getName(), null, null);
        }
    }

    // ----------------------------------

    protected void firePropertyValueChange(Object old, Object current) {
        super.firePropertyValueChange(old, current);

        if (isChangeFiring() && component.getFormModel() != null)
            component.getFormModel().fireComponentPropertyChanged(component,
                                                  desc.getName(), old, current);
//        RADComponentNode node = component.getNodeReference();
//        if (node != null)
//            node.firePropertyChangeHelper(getName(), old, current);
    }

//    protected void fireCurrentEditorChange(PropertyEditor old, PropertyEditor current) {
//        super.fireCurrentEditorChange(old, current);
//
//        RADComponentNode node = component.getNodeReference();
//        if (node != null)
//            node.notifyPropertySetsChange();
//    }

    // -------------------
    // innerclasses

    static class RADPropertyContext extends FormPropertyContext.DefaultSupport {
        RADComponent component;

        RADPropertyContext(RADComponent comp) {
            component = comp;
        }

        public FormModel getFormModel() {
            return component.getFormModel();
        }
    }

    // Descriptor for fake-properties (not real, design-time only) that
    // need to pretend they are of certain type although without both
    // getter and setter. Used e.g. by ButtonGroupProperty.
    static class FakePropertyDescriptor extends PropertyDescriptor {
        Class propType;

        FakePropertyDescriptor(String name, Class type) throws IntrospectionException {
            super(name,null,null);
            propType = type;
        }

        public Class getPropertyType() {
            return propType;
        }
    }
}
