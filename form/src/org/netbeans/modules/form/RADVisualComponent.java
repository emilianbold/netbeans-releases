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

import java.util.*;
import java.beans.*;

import org.openide.nodes.*;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;

/**
 *
 * @author Ian Formanek
 */

public class RADVisualComponent extends RADComponent {

    // -----------------------------------------------------------------------------
    // Private properties

    // [??]
    private HashMap constraints = new HashMap();
//    transient private RADVisualContainer parent;

    private Node.Property[] constraintsProperties;
    private ConstraintsListener constraintsListener;

    // -----------------------------------------------------------------------------
    // Initialization

//    void initParent(RADVisualContainer parent) {
//        this.parent = parent;
//    }

/*    protected void setBeanInstance(Object beanInstance) {
        if (beanInstance instanceof java.awt.Component) {
            boolean attached = FakePeerSupport.attachFakePeer(
                                            (java.awt.Component)beanInstance);
            if (attached && beanInstance instanceof java.awt.Container)
                FakePeerSupport.attachFakePeerRecursively(
                                            (java.awt.Container)beanInstance);
        }

        super.setBeanInstance(beanInstance);
    } */

    // -----------------------------------------------------------------------------
    // Public interface

    /** @return The JavaBean visual component represented by this RADVisualComponent */
    public java.awt.Component getComponent() { // [is it needed ???]
        return (java.awt.Component) getBeanInstance();
    }

    public final RADVisualContainer getParentContainer() {
        return (RADVisualContainer) getParentComponent();
    }

    /** @return The index of this component within visual components of its parent */
    public final int getComponentIndex() {
        RADVisualContainer parent = (RADVisualContainer) getParentComponent();
        return parent != null ? parent.getIndexOf(this) : -1;
//        return ((ComponentContainer)getParentComponent()).getIndexOf(this);
    }

    final LayoutSupportManager getParentLayoutSupport() {
        RADVisualContainer parent = (RADVisualContainer) getParentComponent();
        return parent != null ? parent.getLayoutSupport() : null;
    }

    // -----------------------------------------------------------------------------
    // Layout constraints management

    /** Sets component's constraints description for given layout-support class. 
     */
    public void setLayoutConstraints(Class layoutDelegateClass,
                                     LayoutConstraints constr)
    {
        if (constr != null)
            constraints.put(layoutDelegateClass.getName(), constr);
    }

    /** Gets component's constraints description for given layout-support class.
     */
    public LayoutConstraints getLayoutConstraints(Class layoutDelegateClass) {
        return (LayoutConstraints)
               constraints.get(layoutDelegateClass.getName());
    }

    HashMap getConstraintsMap() {
        return constraints;
    }

    void setConstraintsMap(Map map) {
        for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
            Object layoutClassName = it.next();
            constraints.put(layoutClassName, map.get(layoutClassName));
        }
    }

    // ---------------
    // Properties

    protected void createPropertySets(List propSets) {
        super.createPropertySets(propSets);

        if (constraintsProperties == null)
            createConstraintsProperties();

        if (constraintsProperties.length > 0)
            propSets.add(propSets.size() - 1,
                         new Node.PropertySet("layout", // NOI18N
                    FormEditor.getFormBundle().getString("CTL_LayoutTab"), // NOI18N
                    FormEditor.getFormBundle().getString("CTL_LayoutTabHint")) { // NOI18N

                public Node.Property[] getProperties() {
                    return getConstraintsProperties();
                }
            });
    }

    /** Called to modify original properties obtained from BeanInfo.
     * Properties may be added, removed etc. - due to specific needs
     * of subclasses. Here used for adding ButtonGroupProperty.
     */
    protected void changePropertiesExplicitly(List prefProps,
                                              List normalProps,
                                              List expertProps) {

        super.changePropertiesExplicitly(prefProps, normalProps, expertProps);

        if (getBeanInstance() instanceof java.awt.TextComponent) {
            // hack for AWT text components - "text" property should be first
            for (int i=0, n=normalProps.size(); i < n; i++) {
                RADProperty prop = (RADProperty) normalProps.get(i);
                if ("text".equals(prop.getName())) { // NOI18N
                    normalProps.remove(i);
                    normalProps.add(0, prop);
                    break;
                }
            }
        }

        // hack for buttons - add a fake property for ButtonGroup
//        if (getBeanInstance() instanceof javax.swing.AbstractButton)
//            try {
//                Node.Property prop = new ButtonGroupProperty(this);
//                nameToProperty.put(prop.getName(), prop);
//                if (getBeanInstance() instanceof javax.swing.JToggleButton)
//                    prefProps.add(prop);
//                else
//                    normalProps.add(prop);
//            }
//            catch (IntrospectionException ex) {} // should not happen

//        if (getBeanInstance() instanceof javax.swing.JLabel)
//            try {
//                PropertyDescriptor pd = new PropertyDescriptor("displayedMnemonic",
//                    javax.swing.JLabel.class, "getDisplayedMnemonic", "setDisplayedMnemonic");
//                normalProps.add(createProperty(pd));
//            }
//            catch (IntrospectionException ex) {} // should not happen
    }

    protected void clearProperties() {
        super.clearProperties();
        constraintsProperties = null;
    }

    public Node.Property[] getConstraintsProperties() {
        if (constraintsProperties == null)
            createConstraintsProperties();
        return constraintsProperties;
    }

    public void resetConstraintsProperties() {
        if (constraintsProperties != null) {
            for (int i=0; i < constraintsProperties.length; i++)
                nameToProperty.remove(constraintsProperties[i].getName());

            constraintsProperties = null;
            beanPropertySets = null;

            RADComponentNode node = getNodeReference();
            if (node != null)
                node.fireComponentPropertySetsChange();
        }
    }

    private void createConstraintsProperties() {
        constraintsProperties = null;

        LayoutSupportManager layoutSupport = getParentLayoutSupport();
        if (layoutSupport != null) {
            LayoutConstraints constr = layoutSupport.getConstraints(this);
            if (constr != null)
                constraintsProperties = constr.getProperties();
        }

        if (constraintsProperties == null) {
            constraintsProperties = NO_PROPERTIES;
            return;
        }

        for (int i=0; i < constraintsProperties.length; i++) {
            if (constraintsProperties[i] instanceof FormProperty) {
                FormProperty prop = (FormProperty)constraintsProperties[i];

                // we suppose the constraint property is not a RADProperty...
                prop.addVetoableChangeListener(getConstraintsListener());
                prop.addPropertyChangeListener(getConstraintsListener());

                prop.setPropertyContext(
                    new RADProperty.RADPropertyContext(this));

                if (isReadOnly()) {
                    int type = prop.getAccessType() | FormProperty.NO_WRITE;
                    prop.setAccessType(type);
                }

                nameToProperty.put(prop.getName(), prop);
            }
        }
    }

    private ConstraintsListener getConstraintsListener() {
        if (constraintsListener == null)
            constraintsListener = new ConstraintsListener();
        return constraintsListener;
    }

    private class ConstraintsListener implements VetoableChangeListener,
                                                 PropertyChangeListener
    {
        public void vetoableChange(PropertyChangeEvent ev)
            throws PropertyVetoException
        {
            Object source = ev.getSource();
            String eventName = ev.getPropertyName();
            if (source instanceof FormProperty
                && (FormProperty.PROP_VALUE.equals(eventName)
                    || FormProperty.PROP_VALUE_AND_EDITOR.equals(eventName)))
            {
                LayoutSupportManager layoutSupport = getParentLayoutSupport();
                int index = getComponentIndex();
                LayoutConstraints constraints =
                    layoutSupport.getConstraints(index);

                ev = new PropertyChangeEvent(constraints,
                                             ((FormProperty)source).getName(),
                                             ev.getOldValue(),
                                             ev.getNewValue());

                layoutSupport.componentLayoutChanged(index, ev);
            }
        }

        public void propertyChange(PropertyChangeEvent ev) {
            Object source = ev.getSource();
            if (source instanceof FormProperty
                && FormProperty.CURRENT_EDITOR.equals(ev.getPropertyName()))
            {
                LayoutSupportManager layoutSupport = getParentLayoutSupport();
                int index = getComponentIndex();
                LayoutConstraints constraints =
                    layoutSupport.getConstraints(index);

                ev = new PropertyChangeEvent(constraints, null, null, null);

                try {
                    layoutSupport.componentLayoutChanged(index, ev);
                }
                catch (PropertyVetoException ex) {} // should not happen
            }
        }
    }

    // -----------------------------------------------------------------------------
    // Debug methods

//    public String toString() {
//        String ret = super.toString() + ", constraints: ---------------\n"; // NOI18N
//        for (Iterator it = constraints.keySet().iterator(); it.hasNext();) {
//            Object key = it.next();
//            ret = ret + "class: "+ key + ", constraints: "+constraints.get(key) + "\n"; // NOI18N
//        }
//        return ret + "---------------------------"; // NOI18N
//    }
}
