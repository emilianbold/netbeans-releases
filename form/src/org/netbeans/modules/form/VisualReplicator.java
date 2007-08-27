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
import java.lang.reflect.Method;
import javax.swing.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdesktop.beansbinding.BindingGroup;

import org.openide.ErrorManager;

import org.netbeans.modules.form.fakepeer.FakePeerSupport;
import org.netbeans.modules.form.layoutsupport.*;
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

public class VisualReplicator {

    private RADComponent topMetaComponent;

    private Map<String,Object> idToClone = new HashMap<String,Object>();
    private Map<Object,String> cloneToId = new HashMap<Object,String>();

    private Map<String,SwingLayoutBuilder> layoutBuilders = new HashMap<String,SwingLayoutBuilder>();

    private BindingGroup bindingGroup;
    private BindingDesignSupport bindingSupport;

    private boolean designRestrictions;

    private ViewConverter[] converters;

    // ---------

    public VisualReplicator(boolean designRestrictions,
                            ViewConverter[] converters,
                            BindingDesignSupport bindingSupport) {
        this.designRestrictions = designRestrictions;
        this.converters = converters;
        this.bindingSupport = bindingSupport;
    }

    // ---------
    // mapping

    public Object getClonedComponent(RADComponent metacomp) {
        return metacomp != null ? idToClone.get(metacomp.getId()) : null;
    }

    public Object getClonedComponent(String id) {
        return idToClone.get(id);
    }

    public String getClonedComponentId(Object component) {
        return cloneToId.get(component);
    }

    public Map<String,Object> getMapToClones() {
        return Collections.unmodifiableMap(idToClone);
    }

    // ---------

    private FormModel getFormModel() {
        return getTopMetaComponent().getFormModel();
    }

    SwingLayoutBuilder getLayoutBuilder(String containerId) {
        SwingLayoutBuilder builder = layoutBuilders.get(containerId);
        if (builder == null) {
            RADVisualContainer metacont = (RADVisualContainer)
                getFormModel().getMetaComponent(containerId);
            Container cont = (Container) getClonedComponent(containerId);
            Container contDelegate = metacont.getContainerDelegate(cont);

            builder = new SwingLayoutBuilder(getFormModel().getLayoutModel(),
                                             contDelegate, containerId,
                                             getDesignRestrictions());
            layoutBuilders.put(containerId, builder);
        }
        return builder;
    }

    private BindingGroup getBindingGroup() {
        if (bindingGroup == null) {
            bindingGroup = new BindingGroup();
            bindingGroup.bind();
        }
        return bindingGroup;
    }

    // ---------
    // getters & setters

    public RADComponent getTopMetaComponent() {
        return topMetaComponent;
    }

    public void setTopMetaComponent(RADComponent metacomponent) {
        topMetaComponent = metacomponent;
        idToClone.clear();
        cloneToId.clear();
        layoutBuilders.clear();
        bindingGroup = null;
    }

    public boolean getDesignRestrictions() {
        return designRestrictions;
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

            Map<String,Object> mapToClones = new HashMap<String,Object>(getMapToClones());
            FormModel formModel = getFormModel();
            Set<Map.Entry<String,Object>> entries = mapToClones.entrySet();
            for (Map.Entry<String,Object> entry : entries) {
                String id = entry.getKey();
                Object comp = entry.getValue();
                RADComponent rc = formModel.getMetaComponent(id);
                if (rc != null && (comp == null || !rc.getBeanClass().isAssignableFrom(comp.getClass()))) {
                    // was converted
                    entry.setValue(rc.getBeanInstance());
                }
            }
            BindingGroup group = getBindingGroup();
            boolean restrictions = getDesignRestrictions();
            for (String id : mapToClones.keySet()) {
                RADComponent comp = formModel.getMetaComponent(id);
                if (restrictions) { // this is an updated view (designer)
                    bindingSupport.establishUpdatedBindings(
                            comp, false, mapToClones, group, false);
                    // BindingDesignSupport will unbind and remove these bindings
                    // automatically if user removes a binding or whole component
                } else { // this is a one-off view (preview)
                    BindingDesignSupport.establishOneOffBindings(
                            comp, false, mapToClones, group);
                }
            }
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
        } else if (metacont instanceof RADMenuComponent) { // AWT menu
            // using Swing equivalents of AWT menus for visualization in designer...
            Object menu = getClonedComponent((RADComponent)metacont);
            if (menu instanceof Container) {
                Container cont = (Container) menu;
                cont.removeAll();
                for (RADComponent metacomp : ((RADMenuComponent)metacont).getSubBeans()) {
                    addToMenu(cont, getClonedComponent(metacomp));
                }
            }
        }
    }

    public void updateContainerLayout(RADVisualContainer metacont) {
        Container cont = (Container) getClonedComponent(metacont);
        if (cont == null) // The container is not cloned by the replicator
            return;       // see issue 63654
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
            }

            // make the component visible according to the explicitly set property
            // or component's original visibility
            Boolean visible = null;
            FormProperty visibilityProp = (RADProperty) metacomp.getPropertyByName(
                    "visible", RADProperty.class, false); // NOI18N
            if (visibilityProp != null && visibilityProp.isChanged()) {
                Object value;
                try {
                    value = visibilityProp.getRealValue();
                } catch (Exception ex) { // should not happen
                    value = null;
                }
                if (value instanceof Boolean)
                    visible = (Boolean) value;
            }
            if (visible == null) {
                Component defaultComp = (Component) BeanSupport.getDefaultInstance(comp.getClass());
                if (defaultComp != null)
                    visible = defaultComp.isVisible() ? Boolean.TRUE : Boolean.FALSE;
            }
            if (visible != null) {
                comp.setVisible(visible.booleanValue());
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
            // Re-attach fake peers
            contDelegate.removeAll();
            for (int i=0; i<comps.length; i++) {
                FakePeerSupport.attachFakePeer(comps[i]);
                if (comps[i] instanceof Container)
                    FakePeerSupport.attachFakePeerRecursively((Container)comps[i]);
            }

            setupContainerLayout(layoutBuilder, comps, compIds);
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
                        // layout is built, but we continue to also add e.g. menu bar
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
            Container cont = (Container) getClonedComponent(metacont);
            if (metacomp == metacont.getContainerMenu()) {
                setContainerMenu(cont, clone);
            } else if (metacont.isMenuTypeComponent()) {
                addToMenu(cont, clone);
            } else {
                LayoutSupportManager laysup = metacont.getLayoutSupport();
                if (laysup != null) { // old layout support
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

    public void removeComponent(RADComponent metacomp, ComponentContainer metacont) {
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
                    // Re-attach fake peers
                    contDelegate.removeAll();
                    RADVisualComponent[] metacomps = parentCont.getSubComponents();
                    for (int i=0; i<metacomps.length; i++) {
                        Component component = (Component)getClonedComponent(metacomps[i]);
                        FakePeerSupport.attachFakePeer(component);
                        if (component instanceof Container)
                            FakePeerSupport.attachFakePeerRecursively((Container)component);
                    }

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
            && getDesignRestrictions() // & ATTACH_FAKE_PEERS) != 0
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

        Method writeMethod = FormUtils.getPropertyWriteMethod(property, targetComp.getClass());
        if (writeMethod == null)
            return;

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
                Object newValue = null;
                if (value instanceof FormDesignValue) {
                    newValue = ((FormDesignValue)value).getDesignValue(targetComp);
                }
                if (newValue == null) {
                    value = property.getRealValue();

                    if (value == FormDesignValue.IGNORED_VALUE)
                        return; // ignore the value, as it is not a real value

                    value = FormUtils.cloneObject(value, property.getPropertyContext().getFormModel());
                } else {
                    value = newValue;
                }
            }

            writeMethod.invoke(targetComp, new Object[] { value });

            if (targetComp instanceof Component) {
                ((Component)targetComp).invalidate();
            }
        } catch (CloneNotSupportedException ex) { // ignore cloning failure
        } catch (Exception ex) {
            Logger.getLogger(VisualReplicator.class.getName()).log(Level.INFO, null, ex); // NOI18N
        }
    }

    public void updateBinding(MetaBinding newBinding) {
        if (newBinding != null && bindingSupport != null) {
            RADComponent metaTarget = newBinding.getTarget();
            // Converted components may not have the right properties to bind to
            Object target = isConverted(metaTarget) ? metaTarget.getBeanInstance() : getClonedComponent(metaTarget);
            if (target != null) {
                RADComponent metaSource = newBinding.getSource();
                Object source = isConverted(metaSource) ? metaSource.getBeanInstance() : getClonedComponent(metaSource);
                if (source == null) // source not cloned - let's use the bean instance directly
                    source = newBinding.getSource().getBeanInstance();
                bindingSupport.addBinding(newBinding, source, target, getBindingGroup(), false);
            }
        }
    }

    // ---------
    // executive private methods

    // recursive method
    private Object cloneComponent(RADComponent metacomp,
                                  java.util.List relativeProperties)
        throws Exception
    {
        Object clone = null; // cloned instance to return
        Object compClone = null; // clone of the component itself, might be "inside"
            // the returned clone - e.g. JPanel enclosed in JFrame by a converter

        for (ViewConverter converter : converters) {
            ViewConverter.Convert convert = converter.convert(
                        metacomp.getBeanInstance(),
                        metacomp == getTopMetaComponent(),
                        getDesignRestrictions());
            if (convert != null) {
                clone = convert.getConverted();
                compClone = convert.getEnclosed();
                FormUtils.copyPropertiesToBean(metacomp.getKnownBeanProperties(),
                                               compClone != null ? compClone : clone,
                                               relativeProperties);
                break;
            }
        }

        if (clone == null) { // no converter applied, clone the standard way
            clone = metacomp.cloneBeanInstance(relativeProperties);
        }

        if (clone == null) {
            return null;
        }

        if (compClone == null) {
            compClone = clone;
        }

        idToClone.put(metacomp.getId(), compClone);
        cloneToId.put(compClone, metacomp.getId());

        if (compClone instanceof java.beans.DesignMode) {
            ((java.beans.DesignMode)compClone).setDesignTime(getDesignRestrictions());
        }

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
                RADComponent sub = metacomps[i];
                Component subClone = (Component) getClonedComponent(sub);
                if (subClone == null) {
                    subClone = (Component) cloneComponent(sub, relativeProperties);
                }
                comps[i] = subClone;
                compIds[i] = sub.getId();
            }

            if (metacont.isMenuTypeComponent()) {
                for (Component comp : comps) {
                    addToMenu(cont, comp);
                }
            } else { // set layout
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
                    setupContainerLayout(getLayoutBuilder(metacont.getId()), comps, compIds);
                }
            }
        }
        else if (metacomp instanceof RADMenuComponent) {
            RADComponent[] metacomps = ((RADMenuComponent)metacomp).getSubBeans();
            for (int i = 0; i < metacomps.length; i++) {
                RADComponent sub = metacomps[i];
                Object menuItem = getClonedComponent(sub);
                if (menuItem == null) {
                    menuItem = cloneComponent((RADMenuItemComponent)sub, relativeProperties);
                }
                addToMenu(compClone, menuItem);
            }
        }

        if (clone instanceof Component && getDesignRestrictions()) {
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
            disableFocusing((Component)clone);

            // patch for JDK 1.4 - hide glass pane of JInternalFrame
            if (clone instanceof JInternalFrame)
                ((JInternalFrame)clone).getGlassPane().setVisible(false);
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

    private boolean isConverted(RADComponent metacomp) {
        Object comp = getClonedComponent(metacomp);
        return comp != null && !metacomp.getBeanClass().isAssignableFrom(comp.getClass());
    }

    private void setupContainerLayout(SwingLayoutBuilder layoutBuilder, Component[] comps, String[] compIds) {
        Throwable th = null;
        try {
            layoutBuilder.setupContainerLayout(comps, compIds);
        } catch (Exception ex) {
            th = ex;
        } catch (Error err) {
            th = err;
        }
        if (th != null) {
            ErrorManager.getDefault().notify(th);
            getFormModel().forceUndoOfCompoundEdit();            
        }
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

                    Method writeMethod = FormUtils.getPropertyWriteMethod(property, targetComp.getClass());
                    if (writeMethod != null) {
                        writeMethod.invoke(targetComp,
                                           new Object[] { propertyComp });
                    }
                    else if (propertyComp instanceof ButtonGroup
                             && targetComp instanceof AbstractButton)
                    {   // special case - add button to button group
                        ((ButtonGroup)propertyComp).remove((AbstractButton)targetComp);
                        ((ButtonGroup)propertyComp).add((AbstractButton)targetComp);
                    }
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

    // -----

    public static class DefaultConverter implements ViewConverter {
        public Convert convert(Object component, boolean root, boolean designRestrictions) {
            Class compClass = component.getClass();
            Class convClass = null;
            if (designRestrictions) { // convert windows and AWT menus for design view
                if ((RootPaneContainer.class.isAssignableFrom(compClass)
                            && Window.class.isAssignableFrom(compClass))
                        || Frame.class.isAssignableFrom(compClass)) {
                    convClass = JRootPane.class;
                } else if (Window.class.isAssignableFrom(compClass)
                           || java.applet.Applet.class.isAssignableFrom(compClass)) {
                    convClass = Panel.class;
                } else if (MenuComponent.class.isAssignableFrom(compClass)) {
                    convClass = convertMenuClassToSwing(compClass);
                }
            } else if (root) { // need to enclose in JFrame/Frame for preview
                if (RootPaneContainer.class.isAssignableFrom(compClass)
                        || JComponent.class.isAssignableFrom(compClass)) { // Swing
                    if (!JFrame.class.isAssignableFrom(compClass)) {
                        convClass = JFrame.class;
                    }
                } else if (Component.class.isAssignableFrom(compClass)) { // AWT
                    if (!Frame.class.isAssignableFrom(compClass)) {
                        convClass = Frame.class;
                    }
                }
            }
            if (convClass == null) {
                return null; // no conversion needed
            }

            try {
                Component converted = (Component) CreationFactory.createDefaultInstance(convClass);
                Component enclosed = null;

                if (converted instanceof JFrame) {
                    if (JComponent.class.isAssignableFrom(compClass)) {
                        enclosed = (Component) CreationFactory.createDefaultInstance(compClass);
                        ((JFrame)converted).getContentPane().add(enclosed);
                    }
                } else if (converted instanceof JRootPane) { // RootPaneContainer or Frame converted to JRootPane
                    Container contentCont = (Container) CreationFactory.createDefaultInstance(
                            RootPaneContainer.class.isAssignableFrom(compClass) ? JPanel.class : Panel.class);
                    ((JRootPane)converted).setContentPane(contentCont);
                } else if (MenuItem.class.isAssignableFrom(compClass)) { // converted AWT menu
                    ((JMenuItem)converted).setText(((MenuItem)component).getLabel());
                    ((JMenuItem)converted).setFont(((MenuItem)component).getFont());
                }

                return new ConvertResult(converted, enclosed);
            } catch (Exception ex) { // some instance creation failed, very unlikely to happen
                Logger.getLogger(VisualReplicator.class.getName()).log(Level.INFO, null, ex);
                return null;
            }
        }

        public boolean canVisualize(Class componentClass) {
            return false; // not able to visualize non-visual components
              // AWT menus are converted, but never used as the root in the design view
        }
    }

    private static class ConvertResult implements ViewConverter.Convert {
        private Object converted;
        private Object enclosed;
        ConvertResult(Object converted, Object enclosed) {
            this.converted = converted;
            this.enclosed = enclosed;
        }
        public Object getConverted() {
            return converted;
        }
        public Object getEnclosed() {
            return enclosed;
        }
    }
}
