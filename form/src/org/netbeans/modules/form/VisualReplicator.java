/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.awt.*;
import javax.swing.*;
import java.util.*;

import org.openide.ErrorManager;

import org.netbeans.modules.form.fakepeer.FakePeerSupport;
import org.netbeans.modules.form.layoutsupport.*;
//import org.netbeans.modules.form.layoutdesign.LayoutDesigner.VisualMapper;
import org.netbeans.modules.form.layoutdesign.support.SwingLayoutBuilder;

/**
 * This class replicates (clones) the reference instances from meta-components
 * of a form. This way an equal and independent hierarchy of real instances is
 * built. Components cloned this way are used in the ComponentLayer presenting
 * the form in designer, or by the TestAction. Thanks to mapping from meta
 * components to clones (and viceversa), effective incremental updates of
 * changes from metadata are possible.
 * Note: After updating replicated components, revalidate() and repaint()
 * should be called on the top component.
 *
 * @author Tomas Pavek
 */

public class VisualReplicator { //implements VisualMapper

//    Object topClonedComponent;
    private RADVisualComponent topMetaComponent;

    private Map idToClone = new HashMap();
    private Map cloneToId = new HashMap();

    private Map layoutBuilders = new HashMap();

    private int designRestrictions;

    // design restrictions flags
    static final int ATTACH_FAKE_PEERS = 1;
    static final int DISABLE_FOCUSING = 2;

    // restrictions
    private Class requiredTopClass;
    private Class[] forbiddenClasses;

    // ---------

    public VisualReplicator() {
    }

    public VisualReplicator(Class requiredClass,
                            Class[] forbiddenClasses,
                            int designRestrictions)
    {
        setRequiredTopVisualClass(requiredClass);
        setForbiddenTopVisualClasses(forbiddenClasses);
        setDesignRestrictions(designRestrictions);
//        this.layoutBuilder = layoutBuilder;
    }

    // ---------
    // mapping

    public Object getClonedComponent(RADComponent metacomponent) {
        return idToClone.get(metacomponent.getId());
    }

    public Object getClonedComponent(String id) {
        return idToClone.get(id);
    }

    public String getClonedComponentId(Object component) {
        return (String) cloneToId.get(component);
    }

    // ---------

    private FormModel getFormModel() {
        return getTopMetaComponent().getFormModel();
    }

    SwingLayoutBuilder getLayoutBuilder(String containerId) {
        SwingLayoutBuilder builder = (SwingLayoutBuilder) layoutBuilders.get(containerId);
        if (builder == null) {
            RADVisualContainer metacont = (RADVisualContainer)
                getFormModel().getMetaComponent(containerId);
            Container cont = (Container) getClonedComponent(containerId);
            Container contDelegate = metacont.getContainerDelegate(cont);

            builder = new SwingLayoutBuilder(getFormModel().getLayoutModel(),
                                             contDelegate, containerId);
            layoutBuilders.put(containerId, builder);
        }
        return builder;
    }

    // ---------
    // getters & setters

//    public Object getTopClonedComponent() {
//        return topClonedComponent;
//    }

    public RADVisualComponent getTopMetaComponent() {
        return topMetaComponent;
    }

    public void setTopMetaComponent(RADVisualComponent metacomponent) {
//        topClonedComponent = null;
        topMetaComponent = metacomponent;
        idToClone.clear();
        cloneToId.clear();
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
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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
        Container cont = (Container) getClonedComponent(metacont);
        Container contDelegate = metacont.getContainerDelegate(cont);
        LayoutSupportManager laysup = metacont.getLayoutSupport();
        SwingLayoutBuilder layoutBuilder;

        // clear the container first before setting/changing the layout
        if (laysup != null) { // old layout support
            layoutBuilder = null;
            layoutBuilders.remove(metacont.getId());
            laysup.clearContainer(cont, contDelegate);
        }
        else { // new layout support
            layoutBuilder = getLayoutBuilder(metacont.getId());
            layoutBuilder.clearContainer();
        }

        // update visual components
        RADVisualComponent[] metacomps = metacont.getSubComponents();
        Component[] comps = new Component[metacomps.length];
        String[] compIds = new String[metacomps.length];

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
            FakePeerSupport.attachFakePeer(comp);
            if (comp instanceof Container)
                FakePeerSupport.attachFakePeerRecursively((Container)comp);

            comps[i] = comp;
            compIds[i] = metacomp.getId();
        }

        // set the layout and re-add the components
        if (laysup != null) { // old layout support
            laysup.setLayoutToContainer(cont, contDelegate);
            if (comps.length > 0)
                laysup.addComponentsToContainer(cont, contDelegate, comps, 0);
            laysup.arrangeContainer(cont, contDelegate);
        }
        else { // new layout support
            layoutBuilder.setupContainerLayout(comps, compIds);
        }
    }

    public void updateAddedComponents(ComponentContainer metacont) {
        Container container = null;
        if (metacont instanceof RADComponent) {
            Object contClone = getClonedComponent((RADComponent)metacont);
            if (contClone instanceof Container) {
                if (metacont instanceof RADVisualContainer) {
                    RADVisualContainer visualMetaCont = (RADVisualContainer)metacont;
                    if (visualMetaCont.getLayoutSupport() == null) {
                        // don't try incremental update with new layout support
                        updateContainerLayout(visualMetaCont);
                        //return;
                    }
                    container = visualMetaCont.getContainerDelegate((Container)contClone);
                }
                else container = (Container)contClone;
            }
        }

        RADComponent[] subComps = metacont.getSubBeans();
        for (int i=0; i < subComps.length; i++) {
            Object compClone = getClonedComponent(subComps[i]);
            if (compClone == null)
                addComponent(subComps[i]);
            else if (compClone instanceof Component) {
                Container cloneCont = ((Component)compClone).getParent();
                if (cloneCont != container && cloneToId.get(cloneCont) != null)
                    return; // the clone is placed in another container in
            }               // replicator, there's going to be another update
        }
    }

    // for adding just one component, for adding more components use
    // updateAddedComponents
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
            LayoutSupportManager laysup = metacont.getLayoutSupport();

            if (laysup != null) { // old layout support
                Container cont = (Container) getClonedComponent(metacont);
                if (cont == null) return;
                Container contDelegate = metacont.getContainerDelegate(cont);
                laysup.addComponentsToContainer(
                        cont,
                        contDelegate,
                        new Component[] { (Component) clone },
                        ((RADVisualComponent)metacomp).getComponentIndex());
                laysup.arrangeContainer(cont, contDelegate);
            }
//            else { // new layout support
//                getLayoutBuilder(metacont.getId()).addComponentsToContainer(
//                        new Component[] { (Component) clone },
//                        new String[] { metacomp.getId() } );
//            }
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

    public void removeComponent(RADComponent metacomp,
                                ComponentContainer metacont)
    {
        if (metacomp == null)
            return;

        Object clone = getClonedComponent(metacomp);
        if (clone == null)
            return;

        if (clone instanceof JMenuBar) { // JMenuBar meta component was removed
            // reset JMenuBar in JRootPane
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
        else if (clone instanceof Component) { // visual meta component was removed
            Component comp = (Component) clone;
            // do we know the parent container of the removed meta component?
            RADVisualContainer parentCont =
                metacont instanceof RADVisualContainer ?
                    (RADVisualContainer) metacont : null;
            Container cont = parentCont != null ?
                             (Container) getClonedComponent(parentCont) : null;

            if (cont == null) {
                // we don't know the meta container (layout support), so will
                // just simply remove the component from its parent
                if (comp.getParent() != null)
                    comp.getParent().remove(comp);
            }
            else { // let the layout support remove the visual component
                Container contDelegate = parentCont.getContainerDelegate(cont);
                LayoutSupportManager laysup = parentCont.getLayoutSupport();
                if (laysup != null) { // old layout support
                    if (!laysup.removeComponentFromContainer(
                                    cont, contDelegate, comp))
                    {   // layout delegate cannot remove individual components,
                        // we must clear the container and add the components again
                        laysup.clearContainer(cont, contDelegate);

                        RADVisualComponent[] metacomps = parentCont.getSubComponents();
                        if (metacomps.length > 0) {
                            // we assume the metacomponent is already removed
                            Component[] comps = new Component[metacomps.length];
                            for (int i=0; i < metacomps.length; i++) {
                                comp = (Component) getClonedComponent(metacomps[i]);
                                // becaues the components were removed, we must
                                // re-attach their fake peers (if needed)
                                FakePeerSupport.attachFakePeer(comp);
                                if (comp instanceof Container)
                                    FakePeerSupport.attachFakePeerRecursively(
                                                               (Container)comp);
                                comps[i] = comp;
                            }
                            laysup.addComponentsToContainer(cont, contDelegate, comps, 0);
                        }
                    }
                }
                else { // new layout support
                    getLayoutBuilder(parentCont.getId()).removeComponentsFromContainer(
                        new Component[] { comp },
                        new String[] { metacomp.getId() } );
                }
            }
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

        // Scrollbar hack - to change some properties of AWT Scrollbar we
        // must create a new instance of Scrollbar (peer must be recreated)
        // [maybe this should be done for all AWT components]
        if (targetComp instanceof java.awt.Scrollbar) {
            // remove the component and add a new clone
            removeComponent(metacomp, null);
            addComponent(metacomp);
            return;
        }

        // keep double buffering turned off for JComponent in fake peer container
        // and also keep debugGraphicsOptions turned off
        if (targetComp instanceof JComponent
            && (getDesignRestrictions() & ATTACH_FAKE_PEERS) != 0
            && (("doubleBuffered".equals(property.getName()) // NOI18N
                 && hasAwtParent(metacomp))
              || "debugGraphicsOptions".equals(property.getName()))) // NOI18N
            return;

        // Mnemonics support - start -
        if ("text".equals(property.getName()) // NOI18N
            && (targetComp instanceof AbstractButton
                || targetComp instanceof JLabel)
            && JavaCodeGenerator.isUsingMnemonics(property.getRADComponent()))
        {
            try {
                String str = (String) property.getRealValue();
                if (targetComp instanceof JLabel)
                    org.openide.awt.Mnemonics.setLocalizedText(
                                                (JLabel)targetComp, str);
                else
                    org.openide.awt.Mnemonics.setLocalizedText(
                                                (AbstractButton)targetComp, str);
                return;
            }
            catch (Exception ex) {} // ignore and continue
        }
        // Mnemonics support - end -

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
            Object value = property.getValue();
            
            if (value instanceof RADComponent.ComponentReference) {
                value = ((RADComponent.ComponentReference)value).getComponent();
            }
            
            if (value instanceof RADConnectionPropertyEditor.RADConnectionDesignValue
                && (((RADConnectionPropertyEditor.RADConnectionDesignValue)value).type
                == RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_BEAN)) {
                value = ((RADConnectionPropertyEditor.RADConnectionDesignValue)value).getRADComponent();
            }

            if (value instanceof RADComponent) {
                // the value is another component (relative property )
                Object propertyComp =
                    getClonedComponent((RADComponent)value);
                if (propertyComp == null) // there's no cloned instance yet
                    propertyComp = createClone((RADComponent)value);

                value = propertyComp;
            }
            else {
                value = property.getRealValue();
                
                if (value == FormDesignValue.IGNORED_VALUE)
                    return; // ignore the value, as it is not a real value

                value = FormUtils.cloneObject(value, property.getPropertyContext().getFormModel());
            }

            writeMethod.invoke(targetComp, new Object[] { value });

            if (targetComp instanceof Component)
                ((Component)targetComp).invalidate();
        }
        catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }

    // ---------
    // executive private methods

    // recursive method
    private Object cloneComponent(RADComponent metacomp,
                                  java.util.List relativeProperties)
        throws Exception
    {
        Object clone; // mapped clone instance
        Object compClone = null; // clone of the component, might appear under 'clone'

        if (needsConversion(metacomp)) { // clone with conversion
            clone = cloneComponentWithConversion(
                    metacomp,
                    metacomp == getTopMetaComponent() ? requiredTopClass : null,
                    relativeProperties);

            if (clone instanceof RootPaneContainer
                && !(metacomp.getBeanInstance() instanceof RootPaneContainer))
            {   // the cloned component was put to the content pane
                compClone = ((RootPaneContainer)clone).getContentPane().getComponent(0);
            }
        }
        else if ("java.awt.ScrollPane".equals(metacomp.getBeanClass().getName()) // NOI18N
            && ((getDesignRestrictions() & ATTACH_FAKE_PEERS) != 0)
            && (System.getProperty("java.version").startsWith("1.4"))) // NOI18N
        {   // Issue 36629 - ScrollPane attempts to place
            // components with a lightweight peer into a Panel
            clone = new java.awt.ScrollPane() {
                        public void addNotify() {
                            if (getComponentCount()>0) {
                                Component comp = getComponent(0);
                                remove(0);
                                super.addNotify();
                                FakePeerSupport.attachFakePeer(comp);
                                add(comp);
                            }
                        }
                    };
        }
        else { // just clone the bean
            clone = metacomp.cloneBeanInstance(relativeProperties);
        }

        if (compClone == null)
            compClone = clone;

        idToClone.put(metacomp.getId(), clone);
        cloneToId.put(clone, metacomp.getId());

        if (compClone instanceof java.beans.DesignMode)
            ((java.beans.DesignMode)compClone).setDesignTime(
                                                 getDesignRestrictions() != 0);

        if (metacomp instanceof RADVisualContainer) {
            RADVisualContainer metacont = (RADVisualContainer) metacomp;
            final Container cont = (Container) compClone;
            final Container contDelegate = metacont.getContainerDelegate(cont);

            // clone menu
            if (metacont.getContainerMenu() != null) {
                Object menu = cloneComponent(metacont.getContainerMenu(),
                                             relativeProperties);
                setContainerMenu(cont, menu);
            }

            // clone subcomponents
            RADVisualComponent[] metacomps = metacont.getSubComponents();
            final Component[] comps = new Component[metacomps.length];
            String[] compIds = new String[metacomps.length];
            for (int i=0; i < metacomps.length; i++) {
                comps[i] = (Component) cloneComponent(metacomps[i],
                                                      relativeProperties);
                compIds[i] = metacomps[i].getId();
            }

            // set layout
            final LayoutSupportManager laysup = metacont.getLayoutSupport();
            if (laysup != null) { // old layout support
                laysup.setLayoutToContainer(cont, contDelegate);
                if (comps.length > 0) { // add cloned subcomponents to container
                    if (!(cont instanceof JToolBar))
                        laysup.addComponentsToContainer(cont, contDelegate, comps, 0);
                    else { // a L&F workaround for JToolBar (MetalToobarUI)
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                laysup.addComponentsToContainer(cont, contDelegate,
                                                                comps, 0);
                            }
                        });
                    }
                }
                laysup.arrangeContainer(cont, contDelegate);
            }
            else { // new layout support
                getLayoutBuilder(metacont.getId()).setupContainerLayout(comps, compIds);
            }
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

                addToMenu(compClone, menuItem);
            }
        }

        if (clone instanceof Component) {
            int restrictions = getDesignRestrictions();
            if ((restrictions & ATTACH_FAKE_PEERS) != 0) {
                FakePeerSupport.attachFakePeer((Component)clone);
                if (clone instanceof Container)
                    FakePeerSupport.attachFakePeerRecursively((Container)clone);

                if (clone instanceof JComponent) {
                    // turn off double buffering for JComponent in fake peer container
                    if (hasAwtParent(metacomp))
                        setDoubleBufferedRecursively((JComponent)clone, false);
                    // make sure debug graphics options is turned off
                    ((JComponent)clone).setDebugGraphicsOptions(
                                            DebugGraphics.NONE_OPTION);
                }
            }

            if ((restrictions & DISABLE_FOCUSING) != 0) {
                disableFocusing((Component)clone);

                // patch for JDK 1.4 - hide glass pane of JInternalFrame
                if (clone instanceof JInternalFrame)
                    ((JInternalFrame)clone).getGlassPane().setVisible(false);
            }
        }

        // Mnemonics support - start -
        if ((clone instanceof AbstractButton || clone instanceof JLabel)
            && JavaCodeGenerator.isUsingMnemonics(metacomp))
        {
            FormProperty prop = metacomp.getBeanProperty("text"); // NOI18N
            if (prop != null && prop.isChanged()) {
                try {
                    String str = (String) prop.getRealValue();
                    if (clone instanceof JLabel)
                        org.openide.awt.Mnemonics.setLocalizedText(
                                                    (JLabel)clone, str);
                    else
                        org.openide.awt.Mnemonics.setLocalizedText(
                                                    (AbstractButton)clone, str);
                }
                catch (Exception ex) {} // ignore
            }
        }
        // Mnemonics support - end -

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

    private static void disableFocusing(Component comp) {
        comp.setFocusable(false);
        if (comp instanceof Container) {
            Container cont = (Container) comp;
            for (int i=0, n=cont.getComponentCount(); i < n; i++)
                disableFocusing(cont.getComponent(i));
        }
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
            if (menuItem instanceof JMenuItem)
                ((JMenu)menu).add((JMenuItem)menuItem);
            else
                ((JMenu)menu).addSeparator();
        }
        else if (menu instanceof MenuBar) {
            ((MenuBar)menu).add((Menu)menuItem);
        }
        else if (menu instanceof Menu) {
            if (menuItem instanceof MenuItem)
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
            {   // conversion to RootPaneContainer is required => add the
                // clone to the content pane
                Component clone = (Component)
                    metacomp.cloneBeanInstance(relativeProperties);
                ((RootPaneContainer)component).getContentPane().add(clone);
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

            // set the content pane
            ((JRootPane)component).setContentPane(contentCont);
        }
        else if (MenuItem.class.isAssignableFrom(beanClass)
                 && JMenuItem.class.isAssignableFrom(requiredClass)) {
            ((JMenuItem)component).setText(
                            ((MenuItem)metacomp.getBeanInstance()).getLabel());
            
            ((JMenuItem)component).setFont(
                            ((MenuItem)metacomp.getBeanInstance()).getFont());
        }

        // just try to copy all possible properties
        FormUtils.copyPropertiesToBean(metacomp.getKnownBeanProperties(),
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

    private static boolean hasAwtParent(RADComponent metacomp) {
        RADComponent parent = metacomp.getParentComponent();
        while (parent != null) {
            Class beanClass = parent.getBeanClass();
            if (Component.class.isAssignableFrom(beanClass)
                && !JComponent.class.isAssignableFrom(beanClass)
                && !RootPaneContainer.class.isAssignableFrom(beanClass))
            {   // this is AWT component
                return true;
            }

            parent = parent.getParentComponent();
        }
        return false;
    }

    private static void setDoubleBufferedRecursively(JComponent component,
                                                     boolean value)
    {
        component.setDoubleBuffered(value);
        Component[] subcomps = component.getComponents();
        for (int i=0; i < subcomps.length; i++)
            if (subcomps[i] instanceof JComponent)
                setDoubleBufferedRecursively((JComponent)subcomps[i], value);
    }

    // -------

    // method for setting "relative" component properties additionaly
    private void copyRelativeProperties(java.util.List relativeProperties) {
        for (int i=0; i < relativeProperties.size(); i++) {
            RADProperty property = (RADProperty) relativeProperties.get(i);
            try {
                Object value = property.getValue();
                if (value instanceof RADComponent.ComponentReference)
                    value =
                        ((RADComponent.ComponentReference)value).getComponent();

                if (value instanceof RADConnectionPropertyEditor.RADConnectionDesignValue) {
                    RADConnectionPropertyEditor.RADConnectionDesignValue connection =
                        (RADConnectionPropertyEditor.RADConnectionDesignValue)value;
                    assert connection.type == RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_BEAN;
                    value = connection.getRADComponent();
                }

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
        Object comp = idToClone.remove(metacomp.getId());
        if (comp != null)
            cloneToId.remove(comp);

        if (metacomp instanceof ComponentContainer) {
            layoutBuilders.remove(metacomp.getId());
            RADComponent[] subcomps = ((ComponentContainer)metacomp).getSubBeans();
            for (int i=0; i < subcomps.length; i++)
                removeMapping(subcomps[i]);
        }
    }
}
