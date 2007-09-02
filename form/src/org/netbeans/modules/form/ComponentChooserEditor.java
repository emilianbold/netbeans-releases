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
import java.util.*;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;

/**
 * Property editor allowing to choose a component from all components in form
 * (FormModel). Choice can be restricted to certain bean types.
 *
 * @author Tomas Pavek
 */

public class ComponentChooserEditor implements PropertyEditor,
                                               FormAwareEditor,
                                               XMLPropertyEditor,
                                               NamedPropertyEditor
{
    public static final int ALL_COMPONENTS = 0;
    public static final int NONVISUAL_COMPONENTS = 3;
//    public static final int VISUAL_COMPONENTS = 1;
//    public static final int OTHER_COMPONENTS = 2;

    private static final String NULL_REF = "null"; // NOI18N
    private static final String INVALID_REF = "default"; // NOI18N

    private static String noneText = null;
    private static String invalidText = null;
    private static String defaultText = null;

    private FormModel formModel;
    private List components;
    private Class[] beanTypes = null;
    private int componentCategory = 0;

    private Object defaultValue;
    private ComponentRef value;

    private PropertyChangeSupport changeSupport;

    public ComponentChooserEditor() {
    }

    public ComponentChooserEditor(Class[] componentTypes) {
        beanTypes = componentTypes;
    }

    // --------------
    // PropertyEditor implementation

    public void setValue(Object value) {
        defaultValue = null;
        if (value == null || value instanceof ComponentRef)
            this.value = (ComponentRef) value;
        
        else if (value instanceof RADComponent)
            this.value = new ComponentRef((RADComponent)value);
        else if (value instanceof String)
            this.value = new ComponentRef((String)value);
        else {
            this.value = null;
            defaultValue = value;
        }    
            
            //return;

        firePropertyChange();
    }

    public Object getValue() {
        if (value != null && INVALID_REF.equals(value.getDescription()))
            return BeanSupport.NO_VALUE; // special - invalid value was loaded
        
        return isDefaultValue() ? defaultValue : value; 
    }

    public String[] getTags() {
        List compList = getComponents();

        int extraValues = 0;        
        int count = 0;
        String[] names;                                    
        
        if( isDefaultValue() ) {
            extraValues = 2;        
            count = compList.size() + extraValues;
            names = new String[count];                                    
            names[0] = defaultString();            
        } else {
            extraValues = 1;        
            count = compList.size() + extraValues;
            names = new String[count];                                                
        } 
        names[extraValues - 1] = noneString();
        
        if (count > extraValues) {
            for (int i=extraValues; i < count; i++)
                names[i] = ((RADComponent)compList.get(i-extraValues)).getName();
            Arrays.sort(names, 1, count);
        }

        return names;
    }

    private boolean isDefaultValue() {
        return value == null && defaultValue != null;
    }    
    
    public String getAsText() {
        if (isDefaultValue())
            return defaultString();
        if (value == null)
            return noneString();
        if (value.getComponent() == null)
            return invalidString();

        String str = value.getDescription();
        return NULL_REF.equals(str) ? noneString() : str;
    }

    public void setAsText(String str) {
        if (str == null || str.equals("") || str.equals(noneString())) // NOI18N
            setValue(null);
        else {
            if(defaultString().equals(str)) {           
                // XXX 
                setValue(defaultValue);
            } else {
                setValue(str);    
            }
            
        }
            
    }

    public String getJavaInitializationString() {
        return value != null ? value.getJavaInitString() : null;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (this) {
            if (changeSupport == null)
                changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (changeSupport != null)
            changeSupport.removePropertyChangeListener(l);
    }

    public boolean isPaintable() {
        return false;
    }

    public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) {
    }

    public java.awt.Component getCustomEditor() {
        return null;
    }

    public boolean supportsCustomEditor() {
        return false;
    }

    // ----------------

    // FormAwareEditor implementation
    public void setContext(FormModel model, FormProperty prop) {
        formModel = model;
    }

    // FormAwareEditor implementation
    public void updateFormVersionLevel() {
    }

    public FormModel getFormModel() {
        return formModel;
    }

    public void setBeanTypes(Class[] types) {
        beanTypes = types;
    }

    public Class[] getBeanTypes() {
        return beanTypes;
    }

    public void setComponentCategory(int cat) {
        componentCategory = cat;
    }

    public int getComponentCategory() {
        return componentCategory;
    }

    // ----------------
    // XMLPropertyEditor implementation

    private static final String XML_COMPONENT = "ComponentRef"; // NOI18N
    private static final String ATTR_NAME = "name"; // NOI18N

    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        String nameStr;
        if (value != null)
            nameStr = value.getComponent() != null ?
                      value.getDescription() : INVALID_REF;
        else
            nameStr = NULL_REF;
        
        org.w3c.dom.Element el = doc.createElement(XML_COMPONENT);
        el.setAttribute(ATTR_NAME, nameStr);
        return el;
    }

    public void readFromXML(org.w3c.dom.Node element)
        throws java.io.IOException
    {
        if (!XML_COMPONENT.equals(element.getNodeName()))
            throw new java.io.IOException();

        org.w3c.dom.NamedNodeMap attributes;
        org.w3c.dom.Node nameAttr;
        String name;

        if ((attributes = element.getAttributes()) != null
              && (nameAttr = attributes.getNamedItem(ATTR_NAME)) != null
              && (name = nameAttr.getNodeValue()) != null)
        {
            value = new ComponentRef(name);
        }
    }

    // ---------

    protected List getComponents() {
        if (components == null)
            components = new ArrayList();
        else
            components.clear();

        if (formModel != null) {
            Collection<RADComponent> comps;
            if (componentCategory == NONVISUAL_COMPONENTS)
                comps = formModel.getNonVisualComponents();
            else
                comps = formModel.getAllComponents();

            for (RADComponent metacomp : comps)
                if (acceptBean(metacomp))
                    components.add(metacomp);
        }

        return components;
    }

    protected boolean acceptBean(RADComponent comp) {
        if (beanTypes == null)
            return true;

        boolean match = false;
        for (int i=0; i < beanTypes.length && !match; i++)
            match = beanTypes[i].isAssignableFrom(comp.getBeanClass());

        return match;
    }

    protected String noneString() {
        if (noneText == null)
            noneText = FormUtils.getBundleString("CTL_NoComponent"); // NOI18N
        return noneText;
    }

    protected String defaultString() {
        if (defaultText == null)
            defaultText = FormUtils.getBundleString("CTL_DefaultComponent"); // NOI18N
        return defaultText;
    }
    
    protected String invalidString() {
        if (invalidText == null)
            invalidText = FormUtils.getBundleString("CTL_InvalidReference"); // NOI18N
        return invalidText;
    }

    // ------

    protected final void firePropertyChange() {
        if (changeSupport != null)
            changeSupport.firePropertyChange(null, null, null);
    }

    // NamedPropertyEditor implementation
    public String getDisplayName() {
        return NbBundle.getBundle(getClass()).getString("CTL_ComponentChooserEditor_DisplayName"); // NOI18N
    }

    // ------------

    private class ComponentRef extends FormDesignValueAdapter implements RADComponent.ComponentReference
    {
        private String componentName;
        private RADComponent component;

        ComponentRef(String name) {
            componentName = name;
        }

        ComponentRef(RADComponent metacomp) {
            componentName = metacomp.getName();
            component = metacomp;
        }

        public boolean equals(Object obj) {
            boolean equal;
            
            if (obj instanceof ComponentRef) {
                ComponentRef ref = (ComponentRef)obj;
                
                equal = (ref.component == component);
                if (componentName == null) {
                    equal = equal && (ref.componentName == null);
                } else {
                    equal = equal && componentName.equals(ref.componentName);
                }
            } else {
                equal = (obj instanceof RADComponent && obj == component);
            }
            
            return equal;
        }

        String getJavaInitString() {
            checkComponent();

            if (component != null) {
                if (component == component.getFormModel().getTopRADComponent())
                    return "this"; // NOI18N
            }
            else if (!NULL_REF.equals(componentName))
                return null; // invalid reference

            return componentName;
        }

        public RADComponent getComponent() {
            checkComponent();
            return component;
        }

        /** FormDesignValue implementation. */
        public String getDescription() {
            checkComponent();
            return componentName;
        }

        /** FormDesignValue implementation. */
        public Object getDesignValue() {
            checkComponent();
            return component != null ?
                   component.getBeanInstance(): IGNORED_VALUE;
        }

        private void checkComponent() {
            if (component == null
                && !NULL_REF.equals(componentName)
                && !INVALID_REF.equals(componentName))
            {
                List compList = getComponents();
                Iterator it = compList.iterator();
                while (it.hasNext()) {
                    RADComponent comp = (RADComponent) it.next();
                    if (comp.getName().equals(componentName)) {
                        if (comp.isInModel())
                            component = comp;
                        break;
                    }
                }
            }
            else if (component != null) {
                if (!component.isInModel())
                    component = null;
                else
                    componentName = component.getName();
            }
        }
    }
}
