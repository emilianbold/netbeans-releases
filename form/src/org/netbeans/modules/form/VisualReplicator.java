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
import java.util.*;

import org.netbeans.modules.form.fakepeer.FakePeerSupport;
import org.netbeans.modules.form.layoutsupport.*;

/**
 * This class replicates the instances from meta-components hierarchy,
 * allowing additional updates. It also maintains mapping from meta
 * components to clones, and viceversa.
 *
 * @author Tomas Pavek
 */

public class VisualReplicator {

    Object topClonedComponent;
    RADVisualComponent topMetaComponent;

    Map metaToClone = new HashMap();
    Map cloneToMeta = new HashMap();

    int designRestrictions;

    // design restrictions flags
    public static final int ATTACH_FAKE_PEERS = 1;
    public static final int DISABLE_FOCUSING = 2;

    // restrictions
    Class requiredTopClass;
    Class[] forbiddenClasses;


    public VisualReplicator() {
    }

    public VisualReplicator(Class requiredClass,
                            Class[] forbiddenClasses,
                            int designRestrictions) {
        setRequiredTopVisualClass(requiredClass);
        setForbiddenTopVisualClasses(forbiddenClasses);
        setDesignRestrictions(designRestrictions);
    }

    // ---------
    // mapping

    public Object getClonedComponent(RADComponent metacomponent) {
        return metaToClone.get(metacomponent);
    }

    public RADComponent getMetaComponent(Object component) {
        return (RADComponent) cloneToMeta.get(component);
    }

    // ---------
    // getters & setters

    public Object getTopClonedComponent() {
        return topClonedComponent;
    }

    public RADVisualComponent getTopMetaComponent() {
        return topMetaComponent;
    }

    public void setTopMetaComponent(RADVisualComponent metacomponent) {
        topClonedComponent = null;
        topMetaComponent = metacomponent;
        metaToClone.clear();
        cloneToMeta.clear();
    }

    public int getDesignRestrictions() {
        return designRestrictions;
    }

    public void setDesignRestrictions(int restrictions) {
        designRestrictions = restrictions;
    }

    public Class getRequiredTopVisualClass() {
        return requiredTopClass;
    }

    public void setRequiredTopVisualClass(Class requiredClass) {
        requiredTopClass = requiredClass;
    }

    public Class[] getForbiddenTopVisualClasses() {
        return forbiddenClasses;
    }

    public void setForbiddenTopVisualClasses(Class[] forbiddenClasses) {
        this.forbiddenClasses = forbiddenClasses;
    }

    // --------
    // executive public methods

    public Object createClone() {
        return createClone(getTopMetaComponent());
    }

    public Object createClone(RADComponent metacomp) {
        if (metacomp == null)
            return null;

        Object clone;
        ArrayList relativeProperties = new ArrayList();

        try {
            // clone the whole visual hierarchy recursively 
            clone = cloneComponent(metacomp, relativeProperties);

            // set relative properties additionally
            if (!relativeProperties.isEmpty())
                copyRelativeProperties(relativeProperties);
        }
        catch (Exception ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
            clone = null;
        }

        return clone;
    }

    public void reorderComponents(ComponentContainer metacont) {
        if (metacont instanceof RADVisualContainer) {
            updateContainerLayout((RADVisualContainer) metacont);
        }
        else if (metacont instanceof RADMenuComponent) {
            Container cont = (Container) getClonedComponent((RADComponent)metacont);
            if (cont == null) // should not happen
                return;

            cont.removeAll();

            RADComponent[] metacomps = ((RADMenuComponent)metacont).getSubBeans();
            for (int i = 0; i < metacomps.length; i++) {
                Component comp = (Component) getClonedComponent(metacomps[i]);
                if (comp != null)
                    addToMenu(cont, comp);
            }
        }
    }

    public void updateContainerLayout(RADVisualContainer metacont) {
        if (metacont == null)
            return;

        Container cont = (Container) getClonedComponent(metacont);
        if (cont == null) // should not happen
            return;
        Container contDelegate = metacont.getContainerDelegate(cont);

        LayoutSupportManager laysup = metacont.getLayoutSupport();
        laysup.clearContainer(cont, contDelegate);
        laysup.setLayoutToContainer(cont, contDelegate);

        RADVisualComponent[] metacomps = metacont.getSubComponents();
        Component[] comps = new Component[metacomps.length];
        for (int i = 0; i < metacomps.length; i++) {
            RADVisualComponent metacomp = metacomps[i];

            Component comp = (Component) getClonedComponent(metacomp);
            if (comp == null)
                comp = (Component) createClone(metacomp);
            else {
                if (comp.getParent() != null)
                    comp.getParent().remove(comp);
                comp.setVisible(true); // e.g. CardLayout hides components
            }

            // re-attach fake peer
            boolean attached = FakePeerSupport.attachFakePeer(comp);
            if (attached && comp instanceof Container)
                FakePeerSupport.attachFakePeerRecursively((Container)comp);

            comps[i] = comp;
        }

        // add removed subcomponents
        laysup.addComponentsToContainer(cont, contDelegate, comps, 0);

        if (laysup.supportsArranging())
            laysup.arrangeContainer(cont, contDelegate);
    }

    public void addComponent(RADComponent metacomp) {
        if (metacomp == null)
            return;
        if (getClonedComponent(metacomp) != null)
            return;

        if (metacomp instanceof RADVisualComponent) {
            Object clone = createClone(metacomp);
            if (!(clone instanceof Component))
                return;

            RADVisualContainer metacont = (RADVisualContainer)
                                          metacomp.getParentComponent();
            Container cont = (Container) getClonedComponent(metacont);
            if (cont == null) // should not happen
                return;
            Container contDelegate = metacont.getContainerDelegate(cont);

            LayoutSupportManager laysup = metacont.getLayoutSupport();
            laysup.addComponentsToContainer(
                          cont,
                          contDelegate,
                          new Component[] { (Component) clone },
                          ((RADVisualComponent)metacomp).getComponentIndex());
            if (laysup.supportsArranging())
                laysup.arrangeContainer(cont, contDelegate);
        }
        else if (metacomp instanceof RADMenuItemComponent) {
            Object clone = createClone(metacomp);

            RADComponent menuCont = metacomp.getParentComponent();
            if (menuCont == null)
                return; // should not happen

            Object cont = getClonedComponent(menuCont);
            if (menuCont instanceof RADVisualContainer)
                setContainerMenu((Container)cont, clone);
            else
                addToMenu(cont, clone);
        }
    }

    public void removeComponent(RADComponent metacomp) {
        if (metacomp == null)
            return;

        Object clone = getClonedComponent(metacomp);
        if (clone == null)
            return;

        if (clone instanceof JMenuBar) { // JMenuBar must be reset in JRootPane
            Container menuParent = ((Component)clone).getParent();
            Container cont = menuParent;
            while (cont != null && !(cont instanceof JRootPane))
                cont = cont.getParent();

            if (cont != null)
                ((JRootPane)cont).setJMenuBar(null);
            else if (menuParent != null)
                menuParent.remove((Component)clone);
            else return;
        }
        else if (clone instanceof Component) {
            Component comp = (Component) clone;
            if (comp.getParent() != null)
                comp.getParent().remove(comp);
            else return;
        }
        else if (clone instanceof MenuComponent) { // AWT menu
            MenuComponent menuComp = (MenuComponent) clone;
            MenuContainer menuCont = menuComp.getParent();
            if (menuCont != null)
                menuCont.remove(menuComp);
            else return;
        }

        removeMapping(metacomp);
    }

    public void updateComponentProperty(RADProperty property) {
        if (property == null)
            return;

        RADComponent metacomp = property.getRADComponent();

        // target component of the property
        Object targetComp = getClonedComponent(metacomp);
        if (targetComp == null)
            return;

        // Scrollbar hack - to change some properties of Scrollbar we
        // must create a new instance of Scrollbar (peer must be recreated)
        // [maybe this should be done for all AWT components]
        if (targetComp instanceof java.awt.Scrollbar) {
            // remove the component and add a new clone
            removeComponent(metacomp);
            addComponent(metacomp);
            return;
        }

        java.lang.reflect.Method writeMethod =
            property.getPropertyDescriptor().getWriteMethod();
        if (writeMethod == null)
            return;

        if (!writeMethod.getDeclaringClass().isAssignableFrom(
                                               targetComp.getClass()))
        {   // try to use same method of different (target) class
            try {
                writeMethod = targetComp.getClass().getMethod(
                                  writeMethod.getName(), 
                                  writeMethod.getParameterTypes());
            }
            catch (Exception ex) { // ignore
                return;
            }
        }

        try {
            Object value = property.getRealValue();
            if (value == FormDesignValue.IGNORED_VALUE)
                return; // ignore the value, as it is not a real value

            if (value instanceof RADComponent) {
                // the value is another component (relative property )
                Object propertyComp =
                    getClonedComponent((RADComponent)value);
                if (propertyComp == null) // there's no cloned instance yet
                    propertyComp = createClone((RADComponent)value);

                value = propertyComp;
            }
            else
                value = FormUtils.cloneObject(value);

            writeMethod.invoke(targetComp, new Object[] { value });

            // JScrollPane hack - for scrollbars policy properties
            // [this should be better handled by layout support in the future]
            if (targetComp instanceof JScrollPane)
                ((JScrollPane)targetComp).updateUI();
        }
        catch (Exception ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
        }
    }

    // ---------
    // executive private methods

    // recursive method
    private Object cloneComponent(RADComponent metacomp,
                                  java.util.List relativeProperties)
        throws Exception
    {
        Object clone;
        if (needsConversion(metacomp)) {
            clone = cloneComponentWithConversion(
                    metacomp,
                    metacomp == getTopMetaComponent() ? requiredTopClass : null,
                    relativeProperties);
        }
        else // simply clone the bean otherwise
            clone = metacomp.cloneBeanInstance(relativeProperties);

        metaToClone.put(metacomp, clone);
        cloneToMeta.put(clone, metacomp);

        if (clone instanceof java.beans.DesignMode)
            ((java.beans.DesignMode)clone).setDesignTime(true);

        if (clone instanceof Component) {
            int restrictions = getDesignRestrictions();
            if ((restrictions & ATTACH_FAKE_PEERS) != 0) {
                FakePeerSupport.attachFakePeer((Component)clone);
                if (clone instanceof Container)
                    FakePeerSupport.attachFakePeerRecursively((Container)clone);
            }
            if ((restrictions & DISABLE_FOCUSING) != 0
                    && clone instanceof JComponent) {
                ((JComponent)clone).setRequestFocusEnabled(false);
                ((JComponent)clone).setNextFocusableComponent((JComponent)clone);

                // patch for JDK 1.4 - hide glass pane of JInternalFrame
                if (clone instanceof JInternalFrame)
                    ((JInternalFrame)clone).getGlassPane().setVisible(false);
            }
        }

        if (metacomp instanceof RADVisualContainer) {
            RADVisualContainer metacont = (RADVisualContainer) metacomp;
            final Container cont = (Container) clone;
            final Container contDelegate = metacont.getContainerDelegate(cont);

            // copy menu
            RADMenuComponent menuComp = metacont.getContainerMenu();
            if (menuComp != null) {
                Object menu = cloneComponent(menuComp, relativeProperties);
                setContainerMenu(cont, menu);
            }

            // set layout
            final LayoutSupportManager laysup = metacont.getLayoutSupport();
            laysup.setLayoutToContainer(cont, contDelegate);

            // copy subcomponents
            RADVisualComponent[] metacomps = metacont.getSubComponents();
            final Component[] comps = new Component[metacomps.length];
            for (int i = 0; i < metacomps.length; i++)
                comps[i] = (Component) cloneComponent(metacomps[i],
                                                      relativeProperties);

            // add cloned subcomponents to container
            if (!(clone instanceof JToolBar))
                laysup.addComponentsToContainer(cont, contDelegate, comps, 0);
            else { // a L&F workaround for JToolBar (MetalToobarUI)
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        laysup.addComponentsToContainer(cont, contDelegate,
                                                        comps, 0);
                    }
                });
            }

            if (laysup.supportsArranging())
                laysup.arrangeContainer(cont, contDelegate);
        }
        else if (metacomp instanceof RADMenuComponent) {
            RADComponent[] metacomps = ((RADMenuComponent)metacomp).getSubBeans();
            for (int i = 0; i < metacomps.length; i++) {
                RADMenuItemComponent menuItemComp = (RADMenuItemComponent)
                                                    metacomps[i];
                int type = menuItemComp.getMenuItemType();
                Object menuItem = type != RADMenuItemComponent.T_JSEPARATOR 
                                  && type != RADMenuItemComponent.T_SEPARATOR ?
                    cloneComponent(menuItemComp, relativeProperties) : null;

                addToMenu(clone, menuItem);
            }
        }

        return clone;
    }

    private boolean needsConversion(RADComponent metacomp) {
        Class beanClass = metacomp.getBeanClass();

        if (forbiddenClasses != null) {
            for (int i=0; i < forbiddenClasses.length; i++) {
                if (forbiddenClasses[i].isAssignableFrom(beanClass))
                    return true;
            }
        }

        return metacomp == getTopMetaComponent()
               && requiredTopClass != null
               && !requiredTopClass.isAssignableFrom(beanClass);
    }

    private static void setContainerMenu(Container cont,
                                         Object menu) {
        if (cont instanceof RootPaneContainer) {
            if (menu instanceof JMenuBar)
                ((RootPaneContainer)cont).getRootPane()
                                            .setJMenuBar((JMenuBar)menu);
        }
        else if (cont instanceof JRootPane) {
            if (menu instanceof JMenuBar)
                ((JRootPane)cont).setJMenuBar((JMenuBar)menu);
        }
        else if (cont instanceof Frame) {
            if (menu instanceof MenuBar)
                ((Frame)cont).setMenuBar((MenuBar)menu);
        }
    }

    private static void addToMenu(Object menu, Object menuItem) {
        if (menu instanceof JMenuBar) {
            ((JMenuBar)menu).add((JMenu)menuItem);
        }
        else if (menu instanceof JMenu) {
            if (menuItem != null)
                ((JMenu)menu).add((JMenuItem)menuItem);
            else
                ((JMenu)menu).addSeparator();
        }
        else if (menu instanceof MenuBar) {
            ((MenuBar)menu).add((Menu)menuItem);
        }
        else if (menu instanceof Menu) {
            if (menuItem != null)
                ((Menu)menu).add((MenuItem)menuItem);
            else
                ((Menu)menu).addSeparator();
        }
    }

    // Special non-recursive method for cloning a component with conversion
    // to another class (e.g. JFrame -> JPanel).
    private static Component cloneComponentWithConversion(
                                        RADComponent metacomp,
                                        Class requiredClass,
                                        java.util.List relativeProperties)
    throws Exception {
        Class beanClass = metacomp.getBeanClass();

        if (requiredClass == null) {
            // required class not specified, do some default conversions
            if (RootPaneContainer.class.isAssignableFrom(beanClass)
                    || Frame.class.isAssignableFrom(beanClass))
                requiredClass = JRootPane.class;
            else if (Window.class.isAssignableFrom(beanClass)
                     || Panel.class.isAssignableFrom(beanClass))
                requiredClass = Panel.class;
            else if (Component.class.isAssignableFrom(beanClass))
                requiredClass = JPanel.class;
            else if (MenuComponent.class.isAssignableFrom(beanClass))
                requiredClass = convertMenuClassToSwing(beanClass);
            else
                requiredClass = Object.class;
        }

        Component component =
            (Component) CreationFactory.createDefaultInstance(requiredClass);

        if (component instanceof RootPaneContainer) {
            if (!RootPaneContainer.class.isAssignableFrom(beanClass)
                && !Window.class.isAssignableFrom(beanClass) // AWT
                && !java.applet.Applet.class.isAssignableFrom(beanClass))
            { // RootPaneContainer container is required,
              // with the clone as content pane
                Container contentCont =
                    (Container) metacomp.cloneBeanInstance(relativeProperties);
                ((RootPaneContainer)component).setContentPane(contentCont);
                return component;
            }
        }
        else if (component instanceof JRootPane) {
            // RootPaneContainer or Frame converted to JRootPane
            Class contentClass =
                RootPaneContainer.class.isAssignableFrom(beanClass) ?
                    JPanel.class : Panel.class;
            Container contentCont =
                (Container) CreationFactory.createDefaultInstance(contentClass);

            // try to copy all possible properties to the content pane
            FormUtils.copyPropertiesToBean(metacomp.getAllBeanProperties(),
                                           contentCont,
                                           relativeProperties);
            // set the content pane
            ((JRootPane)component).setContentPane(contentCont);
            return component;
        }
        else if (MenuItem.class.isAssignableFrom(beanClass)
                 && JMenuItem.class.isAssignableFrom(requiredClass)) {
            ((JMenuItem)component).setText(
                            ((MenuItem)metacomp.getBeanInstance()).getLabel());
            
            ((JMenuItem)component).setFont(
                            ((MenuItem)metacomp.getBeanInstance()).getFont());
        }

        // just try to copy all possible properties
        FormUtils.copyPropertiesToBean(metacomp.getAllBeanProperties(),
                                       component,
                                       relativeProperties);
        return component;
    }

    // mapping of AWT menu component to a Swing equivalent
    private static Class convertMenuClassToSwing(Class menuClass) {
        if (MenuBar.class.isAssignableFrom(menuClass))
            return JMenuBar.class;
        if (PopupMenu.class.isAssignableFrom(menuClass))
            return JPopupMenu.class;
        if (Menu.class.isAssignableFrom(menuClass))
            return JMenu.class;
        if (CheckboxMenuItem.class.isAssignableFrom(menuClass))
            return JCheckBoxMenuItem.class;
        if (MenuItem.class.isAssignableFrom(menuClass))
            return JMenuItem.class;

        return menuClass;
    }

    // -------

    // method for setting "relative" component properties additionaly
    private void copyRelativeProperties(java.util.List relativeProperties) {
        for (int i=0; i < relativeProperties.size(); i++) {
            RADProperty property = (RADProperty) relativeProperties.get(i++);
            try {
                Object value = property.getValue();
                if (value instanceof RADComponent) {
                    // the value is another component (relative property )
                    Object propertyComp =
                        getClonedComponent((RADComponent)value);
                    if (propertyComp == null) // there's no cloned instance yet
                        propertyComp = cloneComponent((RADComponent)value,
                                                      relativeProperties);

                    // target component of the property
                    Object targetComp =
                        getClonedComponent(property.getRADComponent());

                    java.lang.reflect.Method writeMethod =
                        property.getPropertyDescriptor().getWriteMethod();
                    if (writeMethod != null)
                        writeMethod.invoke(targetComp,
                                           new Object[] { propertyComp });
                }
            }
            catch (Exception ex) {} // should not happen, ignore
        }
    }

    private void removeMapping(RADComponent metacomp) {
        Object comp = getClonedComponent(metacomp);
        if (comp != null) {
            metaToClone.remove(metacomp);
            cloneToMeta.remove(comp);
        }

        if (metacomp instanceof ComponentContainer) {
            RADComponent[] subcomps = ((ComponentContainer)metacomp).getSubBeans();
            for (int i=0; i < subcomps.length; i++)
                removeMapping(subcomps[i]);
        }
    }
}
