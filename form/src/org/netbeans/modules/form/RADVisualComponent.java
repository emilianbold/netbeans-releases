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
import java.awt.Component;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;

import org.openide.nodes.*;
import org.netbeans.modules.form.layoutsupport.LayoutSupport;
import org.netbeans.modules.form.compat2.layouts.DesignLayout;

/**
 *
 * @author Ian Formanek
 */
public class RADVisualComponent extends RADComponent {

    // -----------------------------------------------------------------------------
    // Private properties

    private HashMap constraints = new HashMap();
    transient private RADVisualContainer parent;

    private Node.Property[] constraintsProperties;
    private PropertyChangeListener constraintsListener;

    // -----------------------------------------------------------------------------
    // Initialization

    void initParent(RADVisualContainer parent) {
        this.parent = parent;
    }

    // -----------------------------------------------------------------------------
    // Public interface

    /** @return The JavaBean visual component represented by this RADVisualComponent */
    public Component getComponent() {
        return(Component)getBeanInstance();
    }

    public RADVisualContainer getParentContainer() {
        return parent;
    }

    /** @return The index of this component within all the subcomponents of its parent */
    public int getComponentIndex() {
        return getParentContainer().getIndexOf(this);
    }

    // -----------------------------------------------------------------------------
    // Layout constraints management

    /** Sets component's constraints description for given layout-support class. 
     */
    public void setConstraintsDesc(Class layoutClass,
                                   LayoutSupport.ConstraintsDesc constr) {
        constraints.put(layoutClass.getName(), constr);
    }

    /** Gets component's constraints description for given layout-support class.
     */
    public LayoutSupport.ConstraintsDesc getConstraintsDesc(Class layoutClass) {
        return (LayoutSupport.ConstraintsDesc)constraints.get(layoutClass.getName());
    }

    public LayoutSupport.ConstraintsDesc getCurrentConstraintsDesc() {
        if (parent == null) return null;
        LayoutSupport laySup = parent.getLayoutSupport();
        if (laySup == null) return null;

        return laySup.getConstraints(this);
    }

    /** Setter for attaching old version of constraints description
     * (DesignLayout.ConstraintsDescription) to this component.
     */ 
    public void setConstraints(Class layoutClass,
                               DesignLayout.ConstraintsDescription constr) {
        constraints.put(layoutClass.getName(), constr);
    }

    /** Getter for obtaining old version of constraints description
     * (DesignLayout.ConstraintsDescription) attached to this component.
     */
    public DesignLayout.ConstraintsDescription getConstraints(Class layoutClass) {
        return(DesignLayout.ConstraintsDescription)constraints.get(layoutClass.getName());
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

    public Node.Property[] getConstraintsProperties() {
        if (constraintsProperties == null)
            createConstraintsProperties();
        return constraintsProperties;
    }

    void createConstraintsProperties() {
        LayoutSupport.ConstraintsDesc constr = getCurrentConstraintsDesc();
        constraintsProperties = constr != null ?
                                constr.getProperties() : null;
        if (constraintsProperties == null) {
            constraintsProperties = new Node.Property[0];
            return;
        }

        for (int i=0; i < constraintsProperties.length; i++) {
            if (constraintsProperties[i] instanceof FormProperty) {
                FormProperty prop = (FormProperty)constraintsProperties[i];
                // we suppose constraints property is not RADProperty...
                prop.addPropertyChangeListener(getConstraintsListener());
                // temporarily no property context - until we solve
                // persistence for advanced constraints properties
//                prop.setPropertyContext(new RADProperty.RADPropertyContext(this));
                if (isReadOnly()) {
                    int type = prop.getAccessType() | FormProperty.NO_WRITE;
                    prop.setAccessType(type);
                }
            }
        }
    }

    /** Called when parent's layout is changed (contstraints are changed too).
     */
    void resetConstraintsProperties() {
        constraintsProperties = null;
        beanPropertySets = null;

        RADComponentNode node = getNodeReference();
        if (node != null) node.fireComponentPropertySetsChange();
    }

    private PropertyChangeListener getConstraintsListener() {
        if (constraintsListener == null)
            constraintsListener = new ConstraintsListener();
        return constraintsListener;
    }

    class ConstraintsListener extends PropertyListener {
        public void propertyChange(PropertyChangeEvent evt) {
            super.propertyChange(evt);

            // re-add components to reflect changed constraints
            RADVisualContainer parentCont = getParentContainer();
            LayoutSupport laysup = parentCont.getLayoutSupport();
            RADVisualComponent[] components = parentCont.getSubComponents();

            for (int i=0; i < components.length; i++)
                laysup.removeComponent(components[i]);
            for (int i=0; i < components.length; i++)
                laysup.addComponent(components[i],
                                    laysup.getConstraints(components[i]));

            getFormModel().fireFormChanged();
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
