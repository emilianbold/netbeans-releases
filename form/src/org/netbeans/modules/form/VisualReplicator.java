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
import org.netbeans.modules.form.layoutsupport.dedicated.*;

/**
 * This class replicates the instances from meta-components hierarchy,
 * allowing additional updates. It also maintains maps for mapping meta
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

    // design restrictions
    public static final int ATTACH_FAKE_PEERS = 1;
    public static final int DISABLED_FOCUSING = 2;

    // restrictions for top visual component
    Class requiredClass;
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
        return requiredClass;
    }

    public void setRequiredTopVisualClass(Class requiredClass) {
        this.requiredClass = requiredClass;
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

    public void updateContainerLayout(RADVisualContainer metacont) {
        if (metacont == null)
            return;

        Container cont = (Container) getClonedComponent(metacont);
        if (cont == null) // should not happen
            return;

        // this is temporary code - may not work properly for some containers
        Container contDelegate = metacont.getContainerDelegate(cont);
        contDelegate.removeAll();

        LayoutSupport laysup = metacont.getLayoutSupport();
        setContainerLayout(contDelegate, laysup);

        // add subcomponents again
        RADVisualComponent[] subcomps = metacont.getSubComponents();
        for (int i = 0; i < subcomps.length; i++) {
            RADVisualComponent subMetaComp = subcomps[i];

            Component comp = (Component) getClonedComponent(subMetaComp);
            if (comp == null)
                comp = (Component) createClone(subMetaComp);
            else if (comp.getParent() != null)
                comp.getParent().remove(comp);

            // re-attach fake peer
            boolean attached = FakePeerSupport.attachFakePeer(comp);
            if (attached && comp instanceof Container)
                FakePeerSupport.attachFakePeerRecursively((Container)comp);

            addComponentToContainer(metacont, cont, contDelegate,
                                    subMetaComp, comp);
        }

        if (laysup instanceof LayoutSupportArranging)
            ((LayoutSupportArranging)laysup).arrangeContainer(cont);
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

            RADVisualContainer metacont =
                ((RADVisualComponent)metacomp).getParentContainer();
            Container cont = (Container) getClonedComponent(metacont);
            if (cont == null) // should not happen
                return;

            addComponentToContainer(metacont, cont,
                                    metacont.getContainerDelegate(cont),
                                    (RADVisualComponent) metacomp,
                                    (Component) clone);
        }
    }

    public void removeComponent(RADComponent metacomp) {
        if (metacomp == null)
            return;

        Object clone = getClonedComponent(metacomp);
        if (clone == null)
            return;

        if (metacomp instanceof RADVisualComponent
                && clone instanceof Component) {
            Component comp = (Component) clone;
            if (comp.getParent() != null)
                comp.getParent().remove(comp);
            else return;
        }

        removeMapping(metacomp);
    }

    public void updateComponentProperty(RADProperty property) {
        if (property == null)
            return;

        // target component of the property
        Object targetComp = getClonedComponent(property.getRADComponent());
        if (targetComp == null)
            return;

        java.lang.reflect.Method writeMethod =
            property.getPropertyDescriptor().getWriteMethod();
        if (writeMethod == null || !writeMethod.getDeclaringClass()
                                    .isAssignableFrom(targetComp.getClass()))
            return;
            
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
    throws Exception {
        Object clone = metacomp == getTopMetaComponent() ?
            // do some conversion for the top component
            cloneVisualComponentWithConversion(getTopMetaComponent(),
                                               requiredClass,
                                               forbiddenClasses,
                                               relativeProperties) :
            // simply clone the bean otherwise
            metacomp.cloneBeanInstance(relativeProperties);

        metaToClone.put(metacomp, clone);
        cloneToMeta.put(clone, metacomp);

        if (clone instanceof java.beans.DesignMode)
            ((java.beans.DesignMode)clone).setDesignTime(true);

        if (clone instanceof Component) {
            int restrictions = getDesignRestrictions();
            if ((restrictions & ATTACH_FAKE_PEERS) != 0) {
                boolean attached = FakePeerSupport.attachFakePeer((Component)clone);
                if (attached && clone instanceof Container)
                    FakePeerSupport.attachFakePeerRecursively((Container)clone);
            }
            if ((restrictions & DISABLED_FOCUSING) != 0
                    && clone instanceof JComponent) {
                ((JComponent)clone).setRequestFocusEnabled(false);
                ((JComponent)clone).setNextFocusableComponent((JComponent)clone);
            }
        }

        if (metacomp instanceof RADVisualContainer) {
            RADVisualContainer metacont = (RADVisualContainer) metacomp;
            Container cont = (Container) clone;
            Container contDelegate = metacont.getContainerDelegate(cont);

            // set layout
            LayoutSupport laysup = metacont.getLayoutSupport();
            setContainerLayout(contDelegate, laysup);

            // copy subcomponents
            RADVisualComponent[] metacomps = metacont.getSubComponents();
            for (int i = 0; i < metacomps.length; i++) {
                Component comp = (Component) cloneComponent(metacomps[i],
                                                            relativeProperties);
                addComponentToContainer(metacont, cont, contDelegate,
                                        metacomps[i], comp);
            }

            if (laysup instanceof LayoutSupportArranging)
                ((LayoutSupportArranging)laysup).arrangeContainer(cont);
        }

        return clone;
    }

    private static void setContainerLayout(Container cont, // container delegate
                                           LayoutSupport laysup) {
        if (laysup != null) {
            if (laysup.getClass() == NullLayoutSupport.class)
                cont.setLayout(null);
            else {
                LayoutManager lm = laysup.cloneLayoutInstance(cont);
                if (lm != null)
                    cont.setLayout(lm);
                // if lm == null then do nothing - it's probably special
                // container with dedicated layout support (e.g. JSplitPane)
            }
        }
    }

    private static void addComponentToContainer(RADVisualContainer metacont,
                                                Container cont,
                                                Container contDelegate,
                                                RADVisualComponent metacomp,
                                                Component comp) {
        comp.setName(metacomp.getName());

        if (cont instanceof JScrollPane) {
            ((JScrollPane)cont).setViewportView(comp);
        }
        else if (cont instanceof JSplitPane) {
            LayoutSupport.ConstraintsDesc desc =
                metacomp.getConstraintsDesc(JSplitPaneSupport.class);
            if (desc instanceof JSplitPaneSupport.SplitConstraintsDesc)
                cont.add(comp, desc.getConstraintsObject());
        }
        else if (cont instanceof JTabbedPane) {
            LayoutSupport.ConstraintsDesc desc =
                metacomp.getConstraintsDesc(JTabbedPaneSupport.class);
            if (desc instanceof JTabbedPaneSupport.TabConstraintsDesc) {
                try {
                    FormProperty titleProperty = (FormProperty)desc.getProperties()[0];
                    FormProperty iconProperty = (FormProperty)desc.getProperties()[1];
                    ((JTabbedPane)cont).addTab(
                        (String) titleProperty.getRealValue(),
                        (Icon) iconProperty.getRealValue(),
                        comp);
                }
                catch (Exception ex) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        ex.printStackTrace();
                }
            }
        }
        else if (cont instanceof JLayeredPane) {
            LayoutSupport.ConstraintsDesc desc =
                    metacomp.getConstraintsDesc(JLayeredPaneSupport.class);
            if (desc instanceof JLayeredPaneSupport.LayeredConstraintsDesc) {
                cont.add(comp, desc.getConstraintsObject());
                Rectangle bounds =
                    ((JLayeredPaneSupport.LayeredConstraintsDesc)desc)
                        .getBounds();
                if (bounds.width == -1 || bounds.height == -1) {
                    Dimension pref = comp.isDisplayable() ?
                                     comp.getPreferredSize() :
                                     metacomp.getComponent().getPreferredSize();
                    if (bounds.width == -1)
                        bounds.width = pref.width;
                    if (bounds.height == -1)
                        bounds.height = pref.height;
                }
                comp.setBounds(bounds);
            }
        }
        else {
            LayoutSupport laysup = metacont.getLayoutSupport();
            if (laysup == null) { // this should not happen
                System.out.println("[WARNING] LayoutSupport is null in container: "+metacont.getName());
                return;
            }

            Object constr = null;
            LayoutSupport.ConstraintsDesc constrDesc =
                                            laysup.getConstraints(metacomp);
            if (constrDesc != null)
                constr = constrDesc.getConstraintsObject();
      
            if (contDelegate.getLayout() != null) {
                if (null == constr)
                    contDelegate.add(comp);
                else
                    contDelegate.add(comp, constr);
            }
            else if (constrDesc instanceof AbsoluteLayoutSupport.AbsoluteConstraintsDesc) {
                // null layout
                contDelegate.add(comp);
                Rectangle bounds =
                    ((AbsoluteLayoutSupport.AbsoluteConstraintsDesc)constrDesc)
                        .getBounds();
                if (bounds.width == -1 || bounds.height == -1) {
                    Dimension pref = comp.isDisplayable() ?
                                     comp.getPreferredSize() :
                                     metacomp.getComponent().getPreferredSize();
                    if (bounds.width == -1)
                        bounds.width = pref.width;
                    if (bounds.height == -1)
                        bounds.height = pref.height;
                }
                comp.setBounds(bounds);
            }
            else contDelegate.add(comp);
        }
    }

    // special non-recursive cloning method for top visual component with
    // additional conversion (e.g. JFrame -> JPanel)
    private static Component cloneVisualComponentWithConversion(
                                        RADVisualComponent metacomp,
                                        Class requiredClass,
                                        Class[] forbiddenClasses,
                                        java.util.List relativeProperties)
    throws Exception {
        Class beanClass = metacomp.getBeanClass();
        boolean beanClassForbidden = false;

        if (forbiddenClasses != null) {
            for (int i=0; i < forbiddenClasses.length; i++) {
                if (forbiddenClasses[i].isAssignableFrom(beanClass)) {
                    beanClassForbidden = true;
                    break;
                }
            }
        }

        if (!beanClassForbidden) {
            if (requiredClass == null
                    || requiredClass.isAssignableFrom(beanClass))
                return (Component) metacomp.cloneBeanInstance(relativeProperties);
        }
        else if (requiredClass == null) // required class not specified
            requiredClass = JComponent.class.isAssignableFrom(beanClass)
                            || RootPaneContainer.class.isAssignableFrom(beanClass)
                            || (!Window.class.isAssignableFrom(beanClass)
                                && !Panel.class.isAssignableFrom(beanClass)) ?
                JPanel.class : Panel.class;

        Component component =
            (Component) CreationFactory.createDefaultInstance(requiredClass);

        if (component instanceof RootPaneContainer
            && !RootPaneContainer.class.isAssignableFrom(beanClass) // Swing
            && !Window.class.isAssignableFrom(beanClass) // AWT
            && !java.applet.Applet.class.isAssignableFrom(beanClass)) // AWT
        {
            Container contentCont =
                (Container) metacomp.cloneBeanInstance(relativeProperties);
            ((RootPaneContainer)component).setContentPane(contentCont);
        }
        else // try to copy all possible properties
            FormUtils.copyPropertiesToBean(metacomp.getAllBeanProperties(),
                                           component,
                                           relativeProperties);

        return component;
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
