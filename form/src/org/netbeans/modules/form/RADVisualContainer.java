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

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.form.RADVisualComponent.MenuType;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;

import org.netbeans.modules.form.layoutsupport.*;
import org.openide.ErrorManager;


public class RADVisualContainer extends RADVisualComponent implements ComponentContainer {
    private ArrayList<RADVisualComponent> subComponents = new ArrayList(10);
    private LayoutSupportManager layoutSupport; // = new LayoutSupportManager();
    private LayoutNode layoutNode; // [move to LayoutSupportManager?]

    private RADComponent containerMenu;

    private Method containerDelegateGetter;
    private boolean noContainerDelegate;

    private static Map<MenuType, Class[]> supportedMenus;
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

        if (layoutSupport != null) // need new layout support for new container bean
            layoutSupport = new LayoutSupportManager(this, getFormModel().getCodeStructure());
    }

    void setInModel(boolean in) {
        boolean alreadyIn = isInModel();
        super.setInModel(in);
        if (in && !alreadyIn && layoutSupport != null) {
            // deferred initialization from pre-creation
            try {
                layoutSupport.initializeLayoutDelegate();
            }
            catch (Exception ex) {
                // [not reported - but very unlikely to happen - only for new container with custom layout]
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                layoutSupport.setUnknownLayoutDelegate(false);
            }
        }
    }

    public void setLayoutSupportDelegate(LayoutSupportDelegate layoutDelegate)
        throws Exception
    {
        layoutSupport.setLayoutDelegate(layoutDelegate,false);
        setLayoutNodeReference(null);
    }

    public LayoutSupportManager getLayoutSupport() {
        return layoutSupport;
    }

//    public boolean isLayoutSupportSet() {
//        return layoutSupport.getLayoutDelegate() != null;
//    }

    public static boolean isFreeDesignContainer(RADComponent metacomp) {
        return metacomp instanceof RADVisualContainer
               && ((RADVisualContainer)metacomp).getLayoutSupport() == null;
    }

    public static boolean isInFreeDesign(RADComponent metacomp) {
        if (metacomp instanceof RADVisualComponent) {
            RADVisualContainer parent = (RADVisualContainer) metacomp.getParentComponent();
            if (parent != null && parent.getLayoutSupport() == null
                    && metacomp != parent.getContainerMenu()) {
                return true;
            }
        }
        return false;
    }

    void setOldLayoutSupport(boolean old) {
        if (old) {
            if (layoutSupport == null) {
                layoutSupport = new LayoutSupportManager(this, getFormModel().getCodeStructure());
            }
        }
        else {
            if (layoutSupport != null) { // clean the layout delegate and related code structre objects
                try {
                    layoutSupport.setLayoutDelegate(null, false);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
                refillContainerInstance();
            }
            layoutSupport = null;
            setLayoutNodeReference(null);
        }
    }

    private void refillContainerInstance() {
        Container cont = getContainerDelegate(getBeanInstance());
        cont.removeAll();
        cont.setLayout(null); // Issue 77904
        for (RADVisualComponent sub : subComponents) {
            Component comp = (Component) sub.getBeanInstance();
            FakePeerSupport.attachFakePeer(comp);
            if (comp instanceof Container)
                FakePeerSupport.attachFakePeerRecursively((Container)comp);
            cont.add(comp);
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
        
        if (value instanceof String)
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

    public RADComponent getContainerMenu() {
        return containerMenu;
    }

    public boolean canAddComponent(Class compClass) {
        if (isMenuTypeComponent()) {
            // this is a menu container accepting certain types of menus
            Class[] possibleClasses = getPossibleSubmenus(getMenuType(getBeanClass()));
            if (possibleClasses != null) {
                for (Class cls : possibleClasses) {
                    if (cls.isAssignableFrom(compClass)) {
                        return true;
                    }
                }
            }
            return false;
        } else if (getContainerMenu() == null && canHaveMenu(compClass)) {
            // visual container that can have a menubar
            return true;
        } else if (getMenuType(compClass) != null && !JSeparator.class.isAssignableFrom(compClass)) {
            // otherwise don't accept menu components
            return false;
        } else if (Component.class.isAssignableFrom(compClass)) {
            // visual component can be added to visual container
            // exception: avoid adding components to scroll pane that already contains something
            if (JScrollPane.class.isAssignableFrom(getBeanClass())
                    && ((JScrollPane)getBeanInstance()).getViewport().getView() != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    boolean canHaveMenu(Class menuClass) {
        return (JMenuBar.class.isAssignableFrom(menuClass)
                  && RootPaneContainer.class.isAssignableFrom(getBeanClass()))
               ||
               (MenuBar.class.isAssignableFrom(menuClass)
                  && Frame.class.isAssignableFrom(getBeanClass())
                  && !JFrame.class.isAssignableFrom(getBeanClass()));
    }

    private static Class[] getPossibleSubmenus(MenuType menuContainerType) {
        if (supportedMenus == null) {
            supportedMenus = new HashMap<MenuType, Class[]>();
            supportedMenus.put(MenuType.JMenuBar, new Class[] { JMenu.class });
            supportedMenus.put(MenuType.JMenu,
                               new Class[] { JMenuItem.class,
                                             JCheckBoxMenuItem.class,
                                             JRadioButtonMenuItem.class,
                                             JMenu.class,
                                             JSeparator.class });
            supportedMenus.put(MenuType.JPopupMenu,
                               new Class[] { JMenuItem.class,
                                             JCheckBoxMenuItem.class,
                                             JRadioButtonMenuItem.class,
                                             JMenu.class,
                                             JSeparator.class });
        }
        return supportedMenus.get(menuContainerType);
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
            RADComponent metacomp = initComponents[i];
            if (i == 0 && !isMenuTypeComponent() && canHaveMenu(metacomp.getBeanClass())) {
                containerMenu = metacomp;
            } else {
                subComponents.add((RADVisualComponent)metacomp);
            }
            metacomp.setParentComponent(this);
        }

        if (layoutSupport == null)
            refillContainerInstance();
    }

    public void reorderSubComponents(int[] perm) {
        RADVisualComponent[] components = new RADVisualComponent[subComponents.size()];
        LayoutConstraints[] constraints;
        if (layoutSupport != null) {
            layoutSupport.removeAll();
            constraints = new LayoutConstraints[subComponents.size()];
        }
        else constraints = null;

        for (int i=0; i < perm.length; i++) {
            RADVisualComponent metacomp = subComponents.get(i);
            components[perm[i]] = metacomp;
            if (constraints != null)
                constraints[perm[i]] = layoutSupport.getStoredConstraints(metacomp);
        }

        subComponents.clear();
        subComponents.addAll(java.util.Arrays.asList(components));

        if (layoutSupport != null) {
            layoutSupport.addComponents(components, constraints, 0);
        }
        else {
            refillContainerInstance();
        }
    }

    public void add(RADComponent comp) {
        add(comp, -1);
    }

    public void add(RADComponent metacomp, int index) {
        RADVisualComponent visual;
        if (index <= 0 && !isMenuTypeComponent() && canHaveMenu(metacomp.getBeanClass())) {
            containerMenu = metacomp;
            visual = null;
        } else {
            visual = (RADVisualComponent) metacomp;
            if (index == -1) {
                index = subComponents.size();
                subComponents.add(visual);
            } else {
                subComponents.add(index, visual);
            }
            if (layoutSupport == null) {
                Component comp = (Component) visual.getBeanInstance();
                FakePeerSupport.attachFakePeer(comp);
                if (comp instanceof Container)
                    FakePeerSupport.attachFakePeerRecursively((Container)comp);
                getContainerDelegate(getBeanInstance()).add(comp, index);
            }
        }

        metacomp.setParentComponent(this);
        if (visual != null) { // force constraints properties creation
            visual.getConstraintsProperties();
        }
    }

    public void remove(RADComponent comp) {
        if (comp == containerMenu) {
            containerMenu = null;
            comp.setParentComponent(null);
        } else if (comp instanceof RADVisualComponent) {
            int index = subComponents.indexOf(comp);
            if (layoutSupport != null) {
                layoutSupport.removeComponent((RADVisualComponent) comp, index);
            }
            else {
                getContainerDelegate(getBeanInstance()).remove(index);
            }
            if (subComponents.remove(comp))
                comp.setParentComponent(null);
        }
    }

    public int getIndexOf(RADComponent comp) {
        if (comp != null && comp == containerMenu)
            return subComponents.size();

        return subComponents.lastIndexOf(comp);
    }
}
