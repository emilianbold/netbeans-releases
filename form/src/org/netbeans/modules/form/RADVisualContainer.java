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

import org.openide.nodes.Node;
import org.netbeans.modules.form.compat2.layouts.DesignLayout;
import org.netbeans.modules.form.compat2.layouts.support.DesignSupportLayout;

import java.awt.Container;
import java.util.ArrayList;

/**
 * Initialization order: <UL>
 * <LI> Constructor: new RADVisualContainer();
 * <LI> FormManager2 init: initialize(FormManager2)
 * <LI> Bean init: setComponent(Class)
 * <LI> SubComponents init: initSubComponents(RADComponent[])
 * <LI> DesignLayout init: setDesignLayout(DesignLayout) </UL>
 
 * @author Ian Formanek
 */
public class RADVisualContainer extends RADVisualComponent implements ComponentContainer {
    private ArrayList subComponents;
    private DesignLayout designLayout;
    private DesignLayout previousLayout;
    private RADLayoutNode layoutNode;

    transient private Container containerDelegate;

    public void setComponent(Class beanClass) {
        super.setComponent(beanClass);
        Object value = getBeanInfo().getBeanDescriptor().getValue("containerDelegate"); // NOI18N
        if ((value != null) &&(value instanceof String) &&((String)value).equals("getContentPane")) { // NOI18N
            try {
                java.lang.reflect.Method m = beanClass.getMethod("getContentPane", new Class [0]); // NOI18N
                containerDelegate =(Container) m.invoke(getBeanInstance(), new Object [0]);
            } catch (Exception e) { // effectively ignored - simply no containerDelegate
            }
        }
    }

    /** @return The JavaBean visual container represented by this RADVisualComponent */
    public Container getContainer() {
        if (containerDelegate != null) {
            return containerDelegate;
        }
        return(Container)getBeanInstance();
    }

    public void setLayoutNodeReference(RADLayoutNode node) {
        this.layoutNode = node;
    }

    public RADLayoutNode getLayoutNodeReference() {
        return layoutNode;
    }

    // -----------------------------------------------------------------------------
    // Layout Manager management

    public DesignLayout getPreviousDesignLayout() {
        return previousLayout;
    }

    public DesignLayout getDesignLayout() {
        return designLayout;
    }

    /** Must be called after initSubComponents!!! */
    public void setDesignLayout(DesignLayout layout) {
        if (designLayout instanceof DesignSupportLayout) {
            throw new InternalError("Cannot change a design layout on this container"); // NOI18N
        }
        if (designLayout != null) {
            if (layout.getClass().equals(designLayout.getClass())) return;
            designLayout.setRADContainer(null);
        }
        if (layout == null) return;

        previousLayout = designLayout;
        designLayout = layout;
        designLayout.setRADContainer(this);

        RADVisualComponent[] children = getSubComponents();
        for (int i = 0; i < children.length; i++) {
            designLayout.addComponent(children[i]);
        }

        getContainer().validate();
        getContainer().repaint();

    }

    /** Called to obtain a Java code to be used to generate code to access the container for adding subcomponents.
     * It is expected that the returned code is either ""(in which case the form is the container) or is a name of variable
     * or method call ending with "."(e.g. "container.getContentPane().").
     * @return the prefix code for generating code to add subcomponents to this container
     */
    public String getContainerGenName() {
        if (containerDelegate != null) {
            return getName() + ".getContentPane()."; // NOI18N
        }
        return getName() + "."; // NOI18N
    }

    // -----------------------------------------------------------------------------
    // SubComponents Management

    public RADComponent[] getSubBeans() {
        RADVisualComponent[] components = new RADVisualComponent [subComponents.size()];
        subComponents.toArray(components);
        return components;
    }

    public RADVisualComponent[] getSubComponents() {
        RADVisualComponent[] components = new RADVisualComponent [subComponents.size()];
        subComponents.toArray(components);
        return components;
    }

    public void initSubComponents(RADComponent[] initComponents) {
        subComponents = new ArrayList(initComponents.length);
        for (int i = 0; i < initComponents.length; i++) {
            subComponents.add(initComponents[i]);
            ((RADVisualComponent)initComponents[i]).initParent(this);
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
        getDesignLayout().updateLayout();
        getFormManager().fireComponentsReordered(this);
    }

    public void add(RADComponent comp) {
        if (!(comp instanceof RADVisualComponent)) throw new IllegalArgumentException();
        subComponents.add(comp);
        ((RADVisualComponent)comp).initParent(this);
        if (getNodeReference() != null) { // it can be null in the case when copying containers with components
            ((RADChildren)getNodeReference().getChildren()).updateKeys();
        }
    }

    public void remove(RADComponent comp) {
        if (!(comp instanceof RADVisualComponent)) throw new IllegalArgumentException();
        designLayout.removeComponent(((RADVisualComponent)comp));
        int index = subComponents.indexOf(comp);
        if (index != -1) {
            subComponents.remove(index);
        }
        ((RADChildren)getNodeReference().getChildren()).updateKeys();
    }

    public int getIndexOf(RADComponent comp) {
        if (!(comp instanceof RADVisualComponent)) throw new IllegalArgumentException();
        return subComponents.indexOf(comp);
    }

    // -----------------------------------------------------------------------------
    // Debug methods

    public String toString() {
        String ret = super.toString() + ", layout: ---------------\n"; // NOI18N
        ret = ret + "current: "+ designLayout +"\n"; // NOI18N
        ret = ret + "previous: "+ previousLayout + "\n"; // NOI18N
        return ret + "---------------------------"; // NOI18N
    }

}
