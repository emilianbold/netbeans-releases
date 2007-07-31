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

package org.netbeans.modules.form.editors2;

import javax.swing.border.*;
import java.util.*;
import java.beans.*;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.nodes.*;
import org.netbeans.modules.form.*;
import org.openide.ErrorManager;

/**
 * A support class holding metadata for borders (javax.swing.border.Border),
 * similar to RADComponent.
 *
 * @author Tomas Pavek
 */

public class BorderDesignSupport implements FormDesignValue
{
    private Border theBorder;
    private boolean borderNeedsUpdate;
    private boolean propertiesNeedInit;
    private CreationDescriptor creationDesc;
    private FormPropertyContext propertyContext = null;
    private FormProperty[] properties = null;
    // -------------------------
    // constructors

    public BorderDesignSupport(Class borderClass)
        throws Exception
    {
        creationDesc = CreationFactory.getDescriptor(borderClass);
        if (creationDesc == null) {
            creationDesc = new CreationDescriptor(borderClass);
            CreationFactory.registerDescriptor(creationDesc);
        }

        theBorder = (Border) CreationFactory.createInstance(borderClass);
    }
    
    public BorderDesignSupport(Border border) {
        creationDesc = CreationFactory.getDescriptor(border.getClass());
        if (creationDesc == null) {
            creationDesc = new CreationDescriptor(border.getClass());
            CreationFactory.registerDescriptor(creationDesc);
        }
        setBorder(border);
    }

    public BorderDesignSupport(BorderDesignSupport borderDesignSupport, FormPropertyContext propertyContext)
        throws Exception
    {
        this(borderDesignSupport.getBorderClass());
        createProperties();
        setPropertyContext(propertyContext);        
        int copyMode = FormUtils.CHANGED_ONLY | FormUtils.DISABLE_CHANGE_FIRING;
            
        FormUtils.copyProperties(borderDesignSupport.getProperties(),
                                 this.properties,
                                 copyMode);
    }

    // --------------------------

    public FormDesignValue copy(FormProperty formProperty) {
        FormModel formModel = formProperty.getPropertyContext().getFormModel();
        try {
            return new BorderDesignSupport(this, new FormPropertyContext.EmptyImpl()); //BorderEditor.createFormPropertyContext(formModel));    
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }
    
    public Border getBorder() {
        if (borderNeedsUpdate)
            updateBorder();
        return theBorder;
    }

    public void setBorder(Border border) {
        theBorder = border;
        if (properties != null) {
            for (int i=0; i < properties.length; i++)
                try {
                    properties[i].reinstateProperty();
                }
                catch (IllegalAccessException e1) {
                }
                catch (InvocationTargetException e2) {
                }
            propertiesNeedInit = false;
        }
        else propertiesNeedInit = true;
        borderNeedsUpdate = false;
    }

    public Class getBorderClass() {
        return creationDesc.getDescribedClass();
    }

    public String getDisplayName() {
        return org.openide.util.Utilities.getShortClassName(theBorder.getClass());
//        String longName = theBorder.getClass().getName();
//        int dot = longName.lastIndexOf('.');
//        return dot < 0 ? longName : longName.substring(dot + 1);
    }

    /** Sets FormPropertyContext for properties. This should be called before
     * properties are created or used after property context had changed.
     */
    public void setPropertyContext(FormPropertyContext propertyContext) {
        if (properties != null && this.propertyContext != propertyContext) {
            for (int i=0; i < properties.length; i++)
                if (!properties[i].getValueType().isPrimitive())
                    properties[i].setPropertyContext(propertyContext);
        }

        this.propertyContext = propertyContext;
    }

    // FormPropertyContainer implementation
    public Node.Property[] getProperties() {
        if (properties == null)
            createProperties();
        return properties;
    }

    public Node.Property getPropertyOfName(String name) {
        Node.Property[] props = getProperties();
        for (int i=0; i < props.length; i++)
            if (props[i].getName().equals(name))
                return props[i];

        return null;
    }

    private void createProperties() {
        BeanInfo bInfo;
        try {
            bInfo = FormUtils.getBeanInfo(theBorder.getClass());
        } catch (IntrospectionException ex) {
            return;
        }
        PropertyDescriptor[] props = bInfo.getPropertyDescriptors();

        List<FormProperty> nodeProps = new ArrayList<FormProperty>();
        for (int i = 0; i < props.length; i++) {
            PropertyDescriptor pd = props[i];
            if (!pd.isHidden()
                && (pd.getWriteMethod() != null 
                    || CreationFactory.containsProperty(creationDesc,
                                                        pd.getName())))
            {
                BorderProperty prop =
                    new BorderProperty(pd.getPropertyType().isPrimitive() ?
                                           null : propertyContext,
                                       pd);

                if (propertiesNeedInit)
                    try {
                        prop.reinstateProperty();
                    }
                    catch (IllegalAccessException e1) {
                    }
                    catch (InvocationTargetException e2) {
                    }

                nodeProps.add(prop);
            }
        }
        properties = new FormProperty[nodeProps.size()];
        nodeProps.toArray(properties);
        propertiesNeedInit = false;
    }

    public String getJavaInitializationString() {
        if (properties == null)
            createProperties();

        CreationDescriptor.Creator creator =
            creationDesc.findBestCreator(properties,
                CreationDescriptor.CHANGED_ONLY | CreationDescriptor.PLACE_ALL);

        return creator.getJavaCreationCode(properties, Border.class, null);
    }

    void updateBorder() {
        if (properties == null)
            createProperties();

        CreationDescriptor.Creator creator =
            creationDesc.findBestCreator(properties,
                CreationDescriptor.CHANGED_ONLY | CreationDescriptor.PLACE_ALL);

        try {
            theBorder = (Border) CreationFactory.createInstance(
                creationDesc.getDescribedClass(),
                properties,
                CreationDescriptor.CHANGED_ONLY | CreationDescriptor.PLACE_ALL);

            // set other properties (not used in constructor)
            FormProperty[] otherProps = CreationFactory.getRemainingProperties(
                                                         creator, properties);
            for (int i=0; i < otherProps.length; i++)
                otherProps[i].reinstateTarget();
        }
        catch (Exception ex) { // should not happen (at least for standard borders)
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    public Object getDesignValue() {
        return getBorder();
    }

    public Object getDesignValue(Object target) {
        return null;
    }

    public String getDescription() {
        return getDisplayName();
    }

    // -----------------------

    public class BorderProperty extends FormProperty {
        private PropertyDescriptor desc;

        public BorderProperty(FormPropertyContext propertyContext,
                              PropertyDescriptor desc)
        {
            super(propertyContext,
                  desc.getName(),
                  desc.getPropertyType(),
                  desc.getDisplayName(),
                  desc.getShortDescription());

            this.desc = desc;

            if (desc.getWriteMethod() == null)
                setAccessType(DETACHED_WRITE);
            else if (desc.getReadMethod() == null)
                setAccessType(DETACHED_READ);
        }

        public Object getTargetValue()
            throws IllegalAccessException, InvocationTargetException
        {
            Method readMethod = desc.getReadMethod();
            return readMethod.invoke(theBorder, new Object[0]);
        }

        public void setTargetValue(Object value)
            throws IllegalAccessException, IllegalArgumentException,
                   InvocationTargetException
        {
            Method writeMethod = desc.getWriteMethod();
            writeMethod.invoke(theBorder, new Object[] { value });
        }

        @Override
        protected Object getRealValue(Object value) {
            Object realValue = super.getRealValue(value);

            if (realValue == FormDesignValue.IGNORED_VALUE
                  && "title".equals(desc.getName())) // NOI18N
                realValue = ((FormDesignValue)value).getDescription();

            return realValue;
        }

        @Override
        public boolean supportsDefaultValue () {
            return true;
        }

        @Override
        public Object getDefaultValue() {
            Method readMethod = desc.getReadMethod();
            Object value = null;
            if (readMethod != null)
                try {
                    value = readMethod.invoke(
                        BeanSupport.getDefaultInstance(theBorder.getClass()),
                        new Object[0]);
                }
                catch (Exception ex) { // do nothing
                }
            return value;
        }

        @Override
        public PropertyEditor getExpliciteEditor() {
            try {
                return desc.createPropertyEditor(theBorder);
            } 
            catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                return null;
            }
        }
	
        @Override
	protected Method getWriteMethod() {
	    return desc.getWriteMethod();	    
	}
    
        @Override
        protected void propertyValueChanged(Object old, Object current) {
            super.propertyValueChanged(old, current);
            borderNeedsUpdate = (getAccessType() & DETACHED_WRITE) != 0;
        }
    }
}
