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

package org.netbeans.modules.form;

import java.beans.*;
import java.util.*;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;

/**
 * Property editor allowing to choose a component from all components of
 * a form (FormModel). Choice can be restricted to certain bean types.
 *
 * @author Tomas Pavek
 */

public class ComponentChooserEditor implements PropertyEditor,
                                               FormAwareEditor,
                                               XMLPropertyEditor
{
    public static final int ALL_COMPONENTS = 0;
    public static final int VISUAL_COMPONENTS = 1;
    public static final int OTHER_COMPONENTS = 2;

    private static String noneText = null;

    private FormModel formModel;
    private List components;
    private Class[] beanTypes = null;
    private int componentCategory = 0;

    private Object value;

    private Vector listeners;

    // --------------
    // PropertyEditor implementation

    public void setValue(Object value) {
        this.value = value;
        firePropertyChange();
    }

    public Object getValue() {
        if (value instanceof RADComponent)
            return value;

        if (value instanceof String) {
            String str = (String) value;

            getComponents();
            Iterator it = components.iterator();
            while (it.hasNext()) {
                RADComponent comp = (RADComponent) it.next();
                if (comp.getName().equals(str)) {
                    value = comp;
                    break;
                }
            }
        }

        return value;
    }

    public String[] getTags() {
        getComponents();

        int count = components.size() + 1;
        String[] names = new String[count];
        names[0] = noneString(); // "<none>"

        if (count > 1) {
            for (int i=1; i < count; i++)
                names[i] = ((RADComponent)components.get(i-1)).getName();
            Arrays.sort(names,1,count-1);
        }

        return names;
    }

    public String getAsText() {
        if (value instanceof String)
            return (String) value;

        if (value instanceof RADComponent)
            return ((RADComponent)value).getName();

        return noneString(); // "<none>"
    }

    public void setAsText(String str) {
        if (str == null || str.equals("") || str.equals(noneString()))
            setValue(null);

        setValue(str);
//        if (components != null) {
//            for (Iterator it=components.iterator(); it.hasNext(); ) {
//                RADComponent comp = (RADComponent)it.next();
//                if (comp.getName().equals(str))
//                    setValue(comp);
//            }
//        }
    }

    public String getJavaInitializationString() {
        if (value instanceof RADComponent)
            return ((RADComponent)value).getName();

        return value != null ? value.toString() : null;
    }

    public synchronized void addPropertyChangeListener(
                                 PropertyChangeListener listener)
    {
	if (listeners == null)
	    listeners = new java.util.Vector();
	listeners.addElement(listener);
    }

    public synchronized void removePropertyChangeListener(
                                 PropertyChangeListener listener)
    {
	if (listeners != null)
            listeners.removeElement(listener);
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
    public void setFormModel(FormModel model) {
        formModel = model;
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
        Object value = getValue();
        if (value instanceof RADComponent) {
            org.w3c.dom.Element el = doc.createElement(XML_COMPONENT);
            el.setAttribute(ATTR_NAME, ((RADComponent)value).getName());
            return el;
        }
        return null;
    }

    public void readFromXML(org.w3c.dom.Node element)
        throws java.io.IOException
    {
        if (!XML_COMPONENT.equals(element.getNodeName()))
            throw new java.io.IOException();

        org.w3c.dom.NamedNodeMap attributes;
        org.w3c.dom.Node nameAttr;
        String compName;

        if ((attributes = element.getAttributes()) != null
              && (nameAttr = attributes.getNamedItem(ATTR_NAME)) != null
              && (compName = nameAttr.getNodeValue()) != null)
        {
//              && formModel != null)
            value = compName;
//
//            getComponents();
//            for (Iterator it=components.iterator(); it.hasNext(); ) {
//                RADComponent comp = (RADComponent) it.next();
//                if (comp.getName().equals(compName)) {
//                    setValue(comp);
//                    break;
//                }
//            }
        }
    }

    // ---------

    protected void getComponents() {
        if (components == null)
            components = new ArrayList();
        else
            components.clear();

        RADComponent[] comps;
        if (componentCategory == VISUAL_COMPONENTS)
            comps = formModel.getVisualComponents();
        else if (componentCategory == OTHER_COMPONENTS)
            comps = formModel.getOtherComponents(true);
        else {
            java.util.List allComps = formModel.getMetaComponents();
            comps = (RADComponent[])
                    allComps.toArray(new RADComponent[allComps.size()]);
        }

        for (int i=0; i < comps.length; i++)
            if (checkBeanType(comps[i]))
                components.add(comps[i]);
    }

    protected boolean checkBeanType(RADComponent comp) {
        if (beanTypes == null)
            return true;

        boolean match = false;
        for (int i=0; i < beanTypes.length && !match; i++)
            match = beanTypes[i].isAssignableFrom(comp.getBeanClass());

        return match;
    }

    // ------

    protected final void firePropertyChange() {
	Vector targets;
	synchronized (this) {
	    if (listeners == null)
	    	return;
	    targets = (Vector) listeners.clone();
	}

        PropertyChangeEvent evt = new PropertyChangeEvent(
                                          this, null, null, null);

	for (int i=0; i < targets.size(); i++)
            ((PropertyChangeListener)targets.elementAt(i)).propertyChange(evt);
    }

    private String noneString() {
        if (noneText == null)
            noneText = FormEditor.getFormBundle().getString("CTL_NoComponent"); // NOI18N
        return noneText;
    }

}
