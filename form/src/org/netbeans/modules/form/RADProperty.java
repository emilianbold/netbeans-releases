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

import java.beans.*;
import java.lang.reflect.*;
import org.netbeans.modules.form.editors.AbstractFormatterFactoryEditor;
import org.openide.ErrorManager;

import org.netbeans.modules.form.editors.*;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;

/**
 * Implementation of properties for (meta)components (class RADComponent).
 * RADComponent is used to get the component instance and
 * PropertyDescriptor provides read and write methods to get and set
 * property values.
 *
 * @author Tomas Pavek
 */
public class RADProperty extends FormProperty {

    public static final String SYNTH_PREFIX = "$$$_"; // NOI18N
    public static final String SYNTH_PRE_CODE = SYNTH_PREFIX + PROP_PRE_CODE + "_"; // NOI18N
    public static final String SYNTH_POST_CODE = SYNTH_PREFIX + PROP_POST_CODE + "_"; // NOI18N

    private RADComponent component;
    private PropertyDescriptor desc;
    private Object defaultValue;

    public RADProperty(RADComponent metacomp, PropertyDescriptor propdesc) {
        super(new FormPropertyContext.Component(metacomp),//new RADPropertyContext(metacomp),
              propdesc.getName(),
              propdesc.getPropertyType(),
              propdesc.getDisplayName(),
              propdesc.getShortDescription());

        component = metacomp;
        desc = propdesc;

        if (desc.getWriteMethod() == null) {
            setAccessType(NO_WRITE);
        } else if (desc.getReadMethod() == null) {
            setAccessType(DETACHED_READ);
        } // assuming a bean property is at least readable or writeable

        defaultValue = BeanSupport.NO_VALUE;
        if (canReadFromTarget()) {
            try {
                defaultValue = getTargetValue();
            } catch (Exception ex) {}
        }
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
            throw new IllegalAccessException("Not a readable property: "+desc.getName()); // NOI18N
        }
        return readMethod.invoke(component.getBeanInstance(), new Object[0]);
    }

    public void setTargetValue(Object value) throws IllegalAccessException,
                                                 IllegalArgumentException,
                                                 InvocationTargetException {
        Method writeMethod = desc.getWriteMethod();
        if (writeMethod == null) {
            throw new IllegalAccessException("Not a writeable property: "+desc.getName()); // NOI18N
        }

        Object beanInstance = component.getBeanInstance();

        // Ugly hack for Scrollbar - Scrollbar.setOrientation(...) method tries
        // to re-create the (native) peer, which we cannot allow. So we detach
        // the peer first before calling the method. This is the only place
        // where we can do it. It could be probably done for all AWT
        // components, but I don't know about any other which would need it.
        java.awt.peer.ComponentPeer scrollbarPeerHack =
            "setOrientation".equals(writeMethod.getName()) // NOI18N
                    && beanInstance instanceof java.awt.Scrollbar ?
            FakePeerSupport.detachFakePeer((java.awt.Component)beanInstance)
            : null;

        try {
            // invoke the setter method
            writeMethod.invoke(component.getBeanInstance(),
                               new Object[] { value });
        }
        catch (InvocationTargetException ex) {
            // annotate exception
            String message = FormUtils.getFormattedBundleString(
                "MSG_ERR_WRITING_TO_PROPERTY", // NOI18N
                new Object[] { getDisplayName() });

            Throwable tex = ex.getTargetException();
            if(tex instanceof IllegalArgumentException) {
                ErrorManager.getDefault().annotate(
                    tex, ErrorManager.WARNING, null,
                    message, null, null);                
                throw (IllegalArgumentException) tex;
            } else if(tex instanceof IllegalAccessException) {
                ErrorManager.getDefault().annotate(
                    tex, ErrorManager.WARNING, null,
                    message, null, null);                
                throw (IllegalAccessException) tex;
            } else if(value==null && tex instanceof NullPointerException) {
                IllegalArgumentException iae = new IllegalArgumentException();
                ErrorManager.getDefault().annotate(
                    iae, ErrorManager.WARNING, null,
                    message, null, null);                
                throw iae;                
            }
            
            ErrorManager.getDefault().annotate(
                ex, ErrorManager.WARNING, null,
                message, null, null);

            throw ex;
        }

        if (scrollbarPeerHack != null) // restore the Scrollbar's fake peer
            FakePeerSupport.attachFakePeer((java.awt.Component)beanInstance,
                                           scrollbarPeerHack);
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
        return defaultValue != BeanSupport.NO_VALUE;
    }

    public Object getDefaultValue() {
        Object specialDefaultValue = FormUtils.getSpecialDefaultPropertyValue(
                component.getBeanInstance(), getName());
        return specialDefaultValue != BeanSupport.NO_VALUE
                ? specialDefaultValue : defaultValue;
    }

    // ----------

    public boolean canWrite() {
         return component.isReadOnly() ? false : super.canWrite();
    }

    // ----------

    public PropertyEditor getExpliciteEditor() {
        PropertyEditor prEd = null;

        PropertyDescriptor descriptor = getPropertyDescriptor();
        if (descriptor.getPropertyType() == Integer.TYPE
            && ("mnemonic".equals(descriptor.getName()) // NOI18N
                || "displayedMnemonic".equals(descriptor.getName()))) { // NOI18N
                prEd = new MnemonicEditor();
        } else {
            if ("editor".equals(descriptor.getName()) && (javax.swing.JSpinner.class.isAssignableFrom(component.getBeanClass()))) { // NOI18N
                prEd = new SpinnerEditorEditor();
            } else if ("formatterFactory".equals(descriptor.getName()) && (javax.swing.JFormattedTextField.class.isAssignableFrom(component.getBeanClass()))) { // NOI18N
                prEd = new AbstractFormatterFactoryEditor();
            } else {
                prEd = createEnumEditor(descriptor);
            }
        }

        if (prEd == null) {
            try {
                prEd = desc.createPropertyEditor(component.getBeanInstance());
            }
            catch (Exception ex) {
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            }
        }

        return prEd;
    }

    protected PropertyEditor createEnumEditor(PropertyDescriptor descriptor) {
        Object[] enumerationValues;

        if (!"debugGraphicsOptions".equals(descriptor.getName()) // NOI18N
            || !javax.swing.JComponent.class.isAssignableFrom(
                                              component.getBeanClass()))
        {   // get the enumeration values by standard means
            enumerationValues = (Object[])
                                descriptor.getValue("enumerationValues"); // NOI18N
        }
        else { // hack: debugGraphicsOptions is problematic because its
               // default value (0) does not correspond to any of the
               // enumerated constants (NONE_OPTION is -1)
            enumerationValues = new Object[] {
                "NONE_OPTION", new Integer(-1), "DebugGraphics.NONE_OPTION", // NOI18N
                "NO_CHANGES", new Integer(0), "0", // NOI18N
                "LOG_OPTION", new Integer(1), "DebugGraphics.LOG_OPTION", // NOI18N
                "FLASH_OPTION", new Integer(2), "DebugGraphics.FLASH_OPTION", // NOI18N
                "BUFFERED_OPTION", new Integer(4), "DebugGraphics.BUFFERED_OPTION" }; // NOI18N
        }

        if (enumerationValues == null
            && "defaultCloseOperation".equals(descriptor.getName()) // NOI18N
            && (javax.swing.JDialog.class.isAssignableFrom(
                                           component.getBeanClass())
                || javax.swing.JInternalFrame.class.isAssignableFrom(
                                           component.getBeanClass())))
        {   // hack: enumeration definition is missing in standard Swing
            // for JDialog and JInternalFrame defaultCloseOperation property
            enumerationValues = new Object[] {
                "DISPOSE_ON_CLOSE", new Integer(2), // NOI18N
                        "WindowConstants.DISPOSE_ON_CLOSE", // NOI18N
                "DO_NOTHING_ON_CLOSE", new Integer(0), // NOI18N
                        "WindowConstants.DO_NOTHING_ON_CLOSE", // NOI18N
                "HIDE_ON_CLOSE", new Integer(1), // NOI18N
                         "WindowConstants.HIDE_ON_CLOSE" }; // NOI18N
        }

        return enumerationValues != null ?
                 new EnumEditor(enumerationValues) : null;
    }

    protected Method getWriteMethod() {	    
	return desc.getWriteMethod();	    
    }
    
    public void setPreCode(String value) {
        if ((preCode == null && value != null)
                || (preCode != null && !preCode.equals(value))) {
            Object old = preCode;
            preCode = value;
            if (isChangeFiring() && component.getFormModel() != null)
                component.getFormModel().fireSyntheticPropertyChanged(
                    component, SYNTH_PRE_CODE + getName(), old, value);
        }
    }

    public void setPostCode(String value) {
        if ((postCode == null && value != null)
                || (postCode != null && !postCode.equals(value))) {
            Object old = postCode;
            postCode = value;
            if (isChangeFiring() && component.getFormModel() != null)
                component.getFormModel().fireSyntheticPropertyChanged(
                    component, SYNTH_POST_CODE + getName(), old, value);
        }
    }

    // ----------------------------------

/*    protected void firePropertyValueChange(Object old, Object current) {
        super.firePropertyValueChange(old, current);

        if (isChangeFiring() && component.getFormModel() != null)
            component.getFormModel().fireComponentPropertyChanged(component,
                                                  desc.getName(), old, current);
    }

    protected void fireCurrentEditorChange(PropertyEditor old, PropertyEditor current) {
        super.fireCurrentEditorChange(old, current);

        if (isChangeFiring() && component.getFormModel() != null)
            component.getFormModel().fireComponentPropertyChanged(component,
                                                  desc.getName(), null, null);
    } */

    // -------------------
    // innerclasses

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
