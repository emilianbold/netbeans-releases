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

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;

import org.openide.nodes.Node;

import org.netbeans.modules.form.layoutsupport.*;


public class RADVisualContainer extends RADVisualComponent implements ComponentContainer {
    private ArrayList subComponents = new ArrayList(10);
    private LayoutSupport layoutSupport;
    private LayoutNode layoutNode;

    private RADMenuComponent containerMenu;

    public void setInstance(Object beanInstance) {
        super.setInstance(beanInstance);
        initLayoutSupport();
    }

    public void initLayoutSupport() {
        // first try to find special support dedicated to container type
        Class layoutSupportClass =
            LayoutSupportRegistry.getLayoutSupportForContainer(getBeanClass());

        if (layoutSupportClass == null) {
            Container containerDelegate = getContainerDelegate(getBeanInstance());

            // if the container already has child componenents, we rather
            // refuse to give layout support for it because this certainly will
            // spoil the whole thing.  The upper code will discard this
            // container and further consider it as component
            
            if (containerDelegate.getComponentCount() > 0)
                return;
            
            // try to find support for LayoutManager used by the container
            
            LayoutManager lm = containerDelegate.getLayout();
            if (lm == null)
                return;

            layoutSupportClass =
                LayoutSupportRegistry.getLayoutSupportForLayout(lm.getClass());
        }

        try {
            LayoutSupport laySup = layoutSupportClass == null ? null :
                LayoutSupportRegistry.createLayoutSupport(layoutSupportClass);
            setLayoutSupport(laySup);
        }
        catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                e.printStackTrace();
        }
    }

    public String getContainerDelegateGetterName() {
        Object value = getBeanInfo().getBeanDescriptor()
            .getValue("containerDelegate"); // NOI18N
        
        if (value != null && value instanceof String)
            return (String) value;
        else
            return null;
    }

    public String getJavaContainerDelegateString() {
        String delegateGetter = getContainerDelegateGetterName();
        if (delegateGetter != null) {
            return getName() + "." + delegateGetter + "()"; // NOI18N
        }
        else
            return getName();
    }
    
    /**
     * @return The JavaBean visual container represented by this
     * RADVisualComponent
     */
    
    public Container getContainerDelegate(Object container) {
        if (container instanceof javax.swing.RootPaneContainer)
            return ((javax.swing.RootPaneContainer)container).getContentPane();
        else if (container instanceof javax.swing.JRootPane)
            return ((javax.swing.JRootPane)container).getContentPane();
        
        Container containerDelegate = (Container) container;
        
        String delegateGetter = getContainerDelegateGetterName();
        if (delegateGetter != null) {
            try {
                java.lang.reflect.Method m = getBeanClass().getMethod(
                    delegateGetter, new Class [0]); // NOI18N
                containerDelegate = (Container) m.invoke(container, new Object [0]);
            } catch (Exception e) {
                // IGNORE
            }
        }
        
        return containerDelegate;
    }

    public void setLayoutNodeReference(LayoutNode node) {
        this.layoutNode = node;
    }

    public LayoutNode getLayoutNodeReference() {
        return layoutNode;
    }

    boolean shouldHaveLayoutNode() {
        return layoutSupport != null && layoutSupport.getLayoutClass() != null;
    }

//    void setContainerMenu(RADMenuComponent menu) {
//        containerMenu = menu;
//    }

    RADMenuComponent getContainerMenu() {
        return containerMenu;
    }

    boolean canHaveMenu(Class menuClass) {
        return (JMenuBar.class.isAssignableFrom(menuClass)
                  && RootPaneContainer.class.isAssignableFrom(getBeanClass()))
               ||
               (MenuBar.class.isAssignableFrom(menuClass)
                  && Frame.class.isAssignableFrom(getBeanClass())
                  && !JFrame.class.isAssignableFrom(getBeanClass()));
    }

    // -----------------------------------------------------------------------------
    // Layout Manager management

    public void setLayoutSupport(LayoutSupport laySup) {
        RADVisualComponent[] components = getSubComponents();
        LayoutSupport.ConstraintsDesc[] oldConstraints = null;
        LayoutSupport.ConstraintsDesc[] newConstraints = null;

        // remove components from current layout
        if (layoutSupport != null)
            for (int i = 0; i < components.length; i++) {
                RADVisualComponent comp = components[i];
                LayoutSupport.ConstraintsDesc constr =
                    layoutSupport.getConstraints(comp);

                if (oldConstraints == null)
                    oldConstraints =
                        new LayoutSupport.ConstraintsDesc[components.length];
                oldConstraints[i] = constr;

                layoutSupport.removeComponent(comp);
            }

        // set new layout
        layoutSupport = laySup;
        if (layoutSupport != null) {
            layoutSupport.initialize(this);

            // convert constraints if needed
            if (oldConstraints != null &&
                    layoutSupport.getConstraints(components[0]) == null) {
                Container cont = (Container)
                    getContainerDelegate(getFormModel().getFormDesigner().getComponent(this));
                Component[] comps = cont != null ? cont.getComponents() : null;
                newConstraints = layoutSupport.convertConstraints(oldConstraints, comps);
            }

            // add components to the new layout
            for (int i = 0; i < components.length; i++) {
                RADVisualComponent comp = components[i];
                LayoutSupport.ConstraintsDesc constr = newConstraints != null ? 
                                            newConstraints[i] :
                                            layoutSupport.getConstraints(comp);
                layoutSupport.addComponent(comp, constr);
                comp.resetConstraintsProperties();
            }
        }

        setLayoutNodeReference(null);
    }

    public LayoutSupport getLayoutSupport() {
        return layoutSupport;
    }

    public boolean isLayoutChanged() {
        if (layoutSupport == null)
            return false; // throw new IllegalStateException();

        boolean nullLayout = layoutSupport.getClass() == NullLayoutSupport.class;
        if (layoutSupport.getLayoutClass() == null && !nullLayout)
            return false; // dedicated LayoutSupport

        Object defaultContainer = BeanSupport.getDefaultInstance(getBeanClass());
        Container defaultDelegate = getContainerDelegate(defaultContainer);
        LayoutManager defaultLM = defaultDelegate.getLayout();

        if (defaultLM == null)
            return !nullLayout;

        if (nullLayout
                || !layoutSupport.getLayoutClass().isAssignableFrom(
                    defaultLM.getClass()))
            return true;

        Node.Property[] props =
            LayoutSupportRegistry.getLayoutProperties(layoutSupport);

        for (int i=0; i < props.length; i++) {
            if (props[i] instanceof FormProperty
                    && ((FormProperty)props[i]).isChanged())
                return true;
        }

        return false; // default layout, no changed properties
    }

    /** Called to obtain a Java code to be used to generate code to access the
     * container for adding subcomponents.  It is expected that the returned
     * code is either ""(in which case the form is the container) or is a name
     * of variable or method call ending with
     * "."(e.g. "container.getContentPane().").
     * @return the prefix code for generating code to add subcomponents to this
     * container
     */
    public String getContainerGenName() {
        String delegateGetter = getContainerDelegateGetterName();
        if (delegateGetter != null)
            return getName() + "." + delegateGetter + "()."; // NOI18N
        else
            return getName() + "."; // NOI18N
    }

    // -----------------------------------------------------------------------------
    // SubComponents Management

    /** @return visual subcomponents (not the menu component) */
    public RADVisualComponent[] getSubComponents() {
        RADVisualComponent[] components = new RADVisualComponent [subComponents.size()];
        subComponents.toArray(components);
        return components;
    }

    // the following methods implement ComponentContainer interface

    /** @return all subcomponents (including the menu component) */
    public RADComponent[] getSubBeans() {
        int n = subComponents.size();
        if (containerMenu != null)
            n++;

        RADComponent[] components = new RADComponent[n];
        subComponents.toArray(components);
        if (containerMenu != null)
            components[n-1] = containerMenu;

        return components;
    }

    public void initSubComponents(RADComponent[] initComponents) {
        subComponents = new ArrayList(initComponents.length);
        for (int i = 0; i < initComponents.length; i++) {
            RADComponent comp = initComponents[i];

            if (comp instanceof RADVisualComponent)
                subComponents.add(comp);
            else if (comp instanceof RADMenuComponent)
                containerMenu = (RADMenuComponent) comp; // [what with the current menu?]
            else
                continue; // [just ignore?]

            comp.setParentComponent(this);
        }
    }

    public void reorderSubComponents(int[] perm) {
        for (int i = 0; i < perm.length; i++) {
            int from = i;
            int to = perm[i];
            if (from == to) continue;
            Object value = subComponents.remove(from);
            if (from < to) {
                subComponents.add(to - 1, value);
            } else {
                subComponents.add(to, value);
            }
        }
        //XXXgetDesignLayout().updateLayout();
//        getFormModel().fireComponentsReordered(this);
    }

    public void add(RADComponent comp) {
        if (comp instanceof RADVisualComponent)
            subComponents.add(comp);
        else if (comp instanceof RADMenuComponent)
            containerMenu = (RADMenuComponent) comp;  // [what with the current menu?]
        else
            return; // [just ignore?]

        comp.setParentComponent(this);

//        if (getNodeReference() != null) { // it can be null in the case when copying containers with components
//            getNodeReference().updateChildren();
//            ((RADChildren)getNodeReference().getChildren()).updateKeys();
//        }
    }

    public void remove(RADComponent comp) {
        if (comp instanceof RADVisualComponent) {
            layoutSupport.removeComponent(((RADVisualComponent)comp));
            if (subComponents.remove(comp))
                comp.setParentComponent(null);
        }
        else if (comp == containerMenu) {
            containerMenu = null;
            comp.setParentComponent(null);
        }
        else return;

//        getNodeReference().updateChildren();
//        ((RADChildren)getNodeReference().getChildren()).updateKeys();
    }

    public int getIndexOf(RADComponent comp) {
//        if (!(comp instanceof RADVisualComponent)) throw new IllegalArgumentException();
        return subComponents.indexOf(comp);
    }
}
