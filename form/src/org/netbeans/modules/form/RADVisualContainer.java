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
import java.lang.reflect.Method;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.layoutdesign.LayoutModel;


public class RADVisualContainer extends RADVisualComponent implements ComponentContainer {
    private ArrayList subComponents = new ArrayList(10);
    private LayoutSupportManager layoutSupport; // = new LayoutSupportManager();
    private LayoutNode layoutNode; // [move to LayoutSupportManager?]

    private RADMenuComponent containerMenu;

    private Method containerDelegateGetter;
    private boolean noContainerDelegate;


//    public boolean initialize(FormModel formModel) {
//        if (super.initialize(formModel)) {
//            if (getBeanClass() != null)
//                layoutSupport.initialize(this, formModel.getCodeStructure());
//            return true;
//        }
//        return false;
//    }

    protected void setBeanInstance(Object beanInstance) {
//        if (layoutSupport != null && layoutSupport.getLayoutDelegate() != null)
//            layoutSupport.clearPrimaryContainer();

        containerDelegateGetter = null;
        noContainerDelegate = false;

        super.setBeanInstance(beanInstance);

        if (layoutSupport != null)
            layoutSupport.initialize(this, getFormModel().getCodeStructure());
    }

    public void setLayoutSupportDelegate(LayoutSupportDelegate layoutDelegate,
                                         LayoutManager lmInstance)
        throws Exception
    {
        layoutSupport.setLayoutDelegate(layoutDelegate, lmInstance, false);
        setLayoutNodeReference(null);
    }

    public LayoutSupportManager getLayoutSupport() {
        return layoutSupport;
    }

//    public boolean isLayoutSupportSet() {
//        return layoutSupport.getLayoutDelegate() != null;
//    }

    void setOldLayoutSupport(boolean old) {
        if (old) {
            if (layoutSupport == null) {
                layoutSupport = new LayoutSupportManager();
                if (getBeanClass() != null)
                    layoutSupport.initialize(this, getFormModel().getCodeStructure());
            }
        }
        else {
            if (layoutSupport != null) { // clean the layout delegate and related code structre objects
                try {
                    if (layoutSupport.getLayoutDelegate() != null) {
                        RADVisualComponent[] components = getSubComponents();
                        java.util.Map idToComponent = new java.util.HashMap();
                        FormModel formModel = getFormModel();
                        FormDesigner formDesigner = FormEditor.getFormDesigner(formModel);
                        for (int i=0; i<components.length; i++) {
                            idToComponent.put(components[i].getId(), formDesigner.getComponent(components[i]));
                        }
                        LayoutModel layoutModel = formModel.getLayoutModel();
                        Object layoutUndoMark = layoutModel.getChangeMark();
                        javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
                        layoutModel.createModel(getId(), (Container)formDesigner.getComponent(this), idToComponent);
                        if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                            formModel.addUndoableEdit(ue);
                        }
                    }
                    layoutSupport.setLayoutDelegate(null, null, false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            layoutSupport = null;
            setLayoutNodeReference(null);
        }
    }

//    public boolean hasOldLayoutSupport() {
//        return layoutSupport != null;
//    }

    public boolean hasDedicatedLayoutSupport() {
        return layoutSupport != null && layoutSupport.isDedicated();
    }

    /**
     * @return The JavaBean visual container represented by this
     * RADVisualComponent
     */
    
    public Container getContainerDelegate(Object container) {
        if (container instanceof RootPaneContainer
                && container.getClass().getName().startsWith("javax.swing.")) // NOI18N
            return ((RootPaneContainer)container).getContentPane();
        if (container.getClass().equals(JRootPane.class))
            return ((JRootPane)container).getContentPane();

        Container containerDelegate = (Container) container;
        Method m = getContainerDelegateMethod();
        if (m != null) {
            try {
                containerDelegate =
                    (Container) m.invoke(container, new Object[0]);
            }
            catch (Exception ex) {
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            }
        }
        return containerDelegate;
    }

    public Method getContainerDelegateMethod() {
        if (containerDelegateGetter == null && !noContainerDelegate) {
            String delegateGetterName = getContainerDelegateGetterName();
            if (delegateGetterName == null
                && (RootPaneContainer.class.isAssignableFrom(getBeanClass())
                    || JRootPane.class.isAssignableFrom(getBeanClass())))
                delegateGetterName = "getContentPane"; // NOI18N

            if (delegateGetterName != null) {
                try {
                    containerDelegateGetter =
                        getBeanClass().getMethod(
                            delegateGetterName, new Class[0]);
                }
                catch (NoSuchMethodException ex) {
                    org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                }
            }
            else noContainerDelegate = true;
        }
        return containerDelegateGetter;
    }

    String getContainerDelegateGetterName() {
        Object value = getBeanInfo().getBeanDescriptor()
                                        .getValue("containerDelegate"); // NOI18N
        
        if (value != null && value instanceof String)
            return (String) value;
        else
            return null;
    }

    public void setLayoutNodeReference(LayoutNode node) {
        this.layoutNode = node;
    }

    public LayoutNode getLayoutNodeReference() {
        return layoutNode;
    }

    boolean shouldHaveLayoutNode() {
        return layoutSupport != null && layoutSupport.shouldHaveNode();
    }

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
    // SubComponents Management

    /** @return visual subcomponents (not the menu component) */
    public RADVisualComponent[] getSubComponents() {
        RADVisualComponent[] components = new RADVisualComponent[subComponents.size()];
        subComponents.toArray(components);
        return components;
    }

    public RADVisualComponent getSubComponent(int index) {
        return (RADVisualComponent) subComponents.get(index);
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
        if (subComponents == null)
            subComponents = new ArrayList(initComponents.length);
        else {
            subComponents.clear();
            subComponents.ensureCapacity(initComponents.length);
        }

        for (int i=0; i < initComponents.length; i++) {
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
        if (layoutSupport != null) {
            layoutSupport.removeAll();

            RADVisualComponent[] components =
                new RADVisualComponent[subComponents.size()];
            LayoutConstraints[] constraints =
                new LayoutConstraints[subComponents.size()];

            for (int i=0; i < perm.length; i++) {
                RADVisualComponent comp = (RADVisualComponent) subComponents.get(i);
                components[perm[i]] = comp;
                constraints[perm[i]] = layoutSupport.getStoredConstraints(comp);
            }

            subComponents.clear();
            subComponents.addAll(java.util.Arrays.asList(components));

            layoutSupport.addComponents(components, constraints, 0);
        }
        else {
            RADVisualComponent[] components =
                new RADVisualComponent[subComponents.size()];
            for (int i=0; i < perm.length; i++) {
                RADVisualComponent comp = (RADVisualComponent) subComponents.get(i);
                components[perm[i]] = comp;
            }
            subComponents.clear();
            subComponents.addAll(java.util.Arrays.asList(components));
        }
    }

    public void add(RADComponent comp) {
        if (comp instanceof RADVisualComponent)
            subComponents.add(comp);
        else if (comp instanceof RADMenuComponent)
            containerMenu = (RADMenuComponent) comp;  // [what with the current menu?]
        else
            return; // [just ignore?]

        comp.setParentComponent(this);
    }

    public void remove(RADComponent comp) {
        if (comp instanceof RADVisualComponent) {
            if (layoutSupport != null) {
                layoutSupport.removeComponent((RADVisualComponent) comp,
                                              subComponents.indexOf(comp));
            }
            if (subComponents.remove(comp))
                comp.setParentComponent(null);
        }
        else if (comp == containerMenu) {
            containerMenu = null;
            comp.setParentComponent(null);
        }
        else return;
    }

    public int getIndexOf(RADComponent comp) {
        if (comp != null && comp == containerMenu)
            return subComponents.size();

        return subComponents.lastIndexOf(comp);
    }
}
