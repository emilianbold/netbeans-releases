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
import java.text.MessageFormat;

import org.openide.cookies.InstanceCookie;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.compat2.layouts.DesignLayout;
import org.netbeans.modules.form.compat2.border.BorderInfo;

/**
 * This class represents an access point for adding new components to FormModel.
 * Its responsibility is to create new meta components (from provided bean
 * classes or instances) and add them to the FormModel. In some cases, no new
 * component is created, just modified (e.g. when a border is applied).
 * This class is intended to process user actions, so all errors are caught
 * and reported here.
 *
 * @author Tomas Pavek
 */

public class MetaComponentCreator {

    FormModel formModel;

    MetaComponentCreator(FormModel model) {
        formModel = model;
    }

    /** Creates and adds a new metacomponent to FormModel. The new component
     * is added to target component (if it is a ComponentContainer).
     * @param ic InstanceCookie for creating the bean instance
     * @param constraints constraints object (for visual components only)
     * @param targetComp component into which the new component is added
     * @return the component if it was successfully created and added (all
     *         errors are reported immediately)
     */
    public RADComponent createComponent(Class beanClass,
                                        RADComponent targetComp,
                                        Object constraints)
    {
        return createAndAddComponent(
                   new CreationFactory.InstanceSource(beanClass),
                   targetComp,
                   constraints);
    }

    /** Creates and adds a new metacomponent to FormModel, based on given
     * InstanceCookie. The new component is added to target component (if it
     * is a ComponentContainer).
     * @param ic InstanceCookie for creating the bean instance
     * @param constraints constraints object (for visual components only)
     * @param targetComp component into which the new component is added
     * @return the component if it was successfully created and added (all
     *         errors are reported immediately)
     */
    public RADComponent createComponent(InstanceCookie ic,
                                        RADComponent targetComp,
                                        Object constraints)
    {
        CreationFactory.InstanceSource source;
        try {
            source = new CreationFactory.InstanceSource(ic);
        }
        catch (Exception ex) {
            showInstErrorMessage(ex);
            return null;
        }

        return createAndAddComponent(source, targetComp, constraints);
    }

    /** Creates a copy of a metacomponent and adds it to FormModel. The new 
     * component is added to target component (if it is a ComponentContainer).
     * @param sourceComp metacomponent to be copied
     * @param targetCont container into which the new component is added
     * @return the component if it was successfully created and added (all
     *         errors are reported immediately)
     */
    public RADComponent copyComponent(RADComponent sourceComp,
                                      ComponentContainer targetCont)
    {
        if (sourceComp instanceof RADVisualComponent)
            LayoutSupportManager.storeConstraints(
                                     (RADVisualComponent) sourceComp);

        // copy the metacomponent
        RADComponent newMetaComp = makeCopy(sourceComp);
        if (newMetaComp == null)
            return null;

        // add the new metacomponent to target FormModel
        if (newMetaComp instanceof RADVisualComponent
                && targetCont instanceof RADVisualContainer)
        {   // visual component
            RADVisualComponent newVisual = (RADVisualComponent) newMetaComp;
            RADVisualContainer newParent = (RADVisualContainer) targetCont;
            LayoutConstraints newConstr = newParent.getLayoutSupport()
                                              .getStoredConstraints(newVisual);

            formModel.addVisualComponent(newVisual, newParent, newConstr);
//                          laysup.fixConstraints(constr));
        }
        else if (newMetaComp instanceof RADMenuItemComponent
                 && (targetCont instanceof RADMenuComponent
                     || targetCont instanceof RADVisualContainer))
        {   // menu component
            formModel.addComponent(newMetaComp, targetCont);
        }
        else if (targetCont == null
                 || targetCont == formModel.getModelContainer())
        {   // other component
            formModel.addComponent(newMetaComp, null);
        }
        else return null; // this should not happen

        return newMetaComp;
    }

    // --------

    private FormDesigner getDesigner() {
        return formModel.getFormDesigner();
    }

    private RADComponent createAndAddComponent(
                             CreationFactory.InstanceSource source,
                             RADComponent targetComp,
                             Object constraints)
    {
        Class beanClass = source.getInstanceClass();
        if (beanClass == null)
            return null;

        RADComponent newComp;

        if (LayoutSupportDelegate.class.isAssignableFrom(beanClass)
              || LayoutManager.class.isAssignableFrom(beanClass)
              || DesignLayout.class.isAssignableFrom(beanClass))
            newComp = setContainerLayout(source, targetComp);

        else if (BorderInfo.class.isAssignableFrom(beanClass)
              || javax.swing.border.Border.class.isAssignableFrom(beanClass))
            newComp = setComponentBorder(source, targetComp);

        else if (MenuComponent.class.isAssignableFrom(beanClass)
              || JMenuItem.class.isAssignableFrom(beanClass)
              || JMenuBar.class.isAssignableFrom(beanClass)
              || JPopupMenu.class.isAssignableFrom(beanClass)
              // or separator being added to a menu
              || (targetComp instanceof RADMenuComponent
                  && (JSeparator.class.isAssignableFrom(beanClass)
                      || Separator.class.isAssignableFrom(beanClass))))
            newComp = addMenuComponent(source, targetComp);

        else  if (Component.class.isAssignableFrom(beanClass))
            newComp = addVisualComponent(source, targetComp, constraints);

        else newComp = addOtherComponent(source, targetComp);

        return newComp;
    }

    // ---------

    private RADComponent makeCopy(RADComponent sourceComp) {
        RADComponent newComp;

        if (sourceComp instanceof RADVisualContainer)
            newComp = new RADVisualContainer();
        else if (sourceComp instanceof RADVisualComponent)
            newComp = new RADVisualComponent();
        else if (sourceComp instanceof RADMenuComponent)
            newComp = new RADMenuComponent();
        else if (sourceComp instanceof RADMenuItemComponent)
            newComp = new RADMenuItemComponent();
        else
            newComp = new RADComponent();

        newComp.initialize(formModel);
        newComp.setStoredName(sourceComp.getName());

        try {
            newComp.initInstance(sourceComp.getBeanClass());
        }
        catch (Exception ex) { // this is rather unlikely to fail
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
            
            String message = MessageFormat.format(
                FormEditor.getFormBundle().getString("FMT_ERR_CannotCopyInstance"), // NOI18N
                new Object [] { ex.getClass().getName(), ex.getMessage() });
            TopManager.getDefault().notify(new NotifyDescriptor.Message(
                message, NotifyDescriptor.ERROR_MESSAGE));

            return null;
        }

        // 1st - copy subcomponents
        if (sourceComp instanceof ComponentContainer) {
            RADComponent[] sourceSubs =
                ((ComponentContainer)sourceComp).getSubBeans();
            RADComponent[] newSubs = new RADComponent[sourceSubs.length];

            for (int i=0; i < sourceSubs.length; i++) {
                RADComponent newSubComp = makeCopy(sourceSubs[i]);
                if (newSubComp == null)
                    return null;
                newSubs[i] = newSubComp;
            }

            ((ComponentContainer)newComp).initSubComponents(newSubs);
        }

        // 2nd - clone LayoutSupportDelegate
        if (sourceComp instanceof RADVisualContainer) {
            RADVisualContainer newCont = (RADVisualContainer) newComp;
            LayoutSupportManager sourceLayout =
                ((RADVisualContainer)sourceComp).getLayoutSupport();

            newCont.getLayoutSupport().copyLayoutDelegateFrom(sourceLayout);
        }

        // 3rd - copy changed properties
        RADProperty[] sourceProps = sourceComp.getAllBeanProperties();
        RADProperty[] newProps = newComp.getAllBeanProperties();
        FormUtils.copyProperties(sourceProps, newProps, true, false);
        // temporary hack for AWT menus - to update their Swing design parallels
        if (newComp instanceof RADMenuItemComponent)
            formModel.fireComponentPropertyChanged(newComp, null, null, null);

        // 4th - copy aux values
        Map auxValues = sourceComp.getAuxValues();
        if (auxValues != null)
            for (Iterator it = auxValues.keySet().iterator(); it.hasNext(); ) {
                String auxName = (String) it.next();
                Object auxValue = auxValues.get(auxName);
                try {
                    newComp.setAuxValue(auxName,
                                        FormUtils.cloneObject(auxValue));
                }
                catch (Exception e) { // ignore problem with aux value
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        e.printStackTrace();
                }
            }

        // 5th - copy layout constraints
        if (sourceComp instanceof RADVisualComponent) {
            Map constraints = ((RADVisualComponent)sourceComp).getConstraintsMap();
            Map newConstraints = new HashMap();

            for (Iterator it = constraints.keySet().iterator(); it.hasNext(); ) {
                Object layoutClassName = it.next();
                LayoutConstraints clonedConstr =
                    ((LayoutConstraints) constraints.get(layoutClassName))
                        .cloneConstraints();
                newConstraints.put(layoutClassName, clonedConstr);
            }
            ((RADVisualComponent)newComp).setConstraintsMap(newConstraints);
        }

        // 6th - copy events - TODO (only if copied between forms)

        return newComp;
    }

    private RADComponent addVisualComponent(
                             CreationFactory.InstanceSource source,
                             RADComponent targetComp,
                             Object constraints)
    {
        if (targetComp != null
                && !(targetComp instanceof RADVisualComponent))
            return null;

        RADVisualComponent newMetaComp = null;
        RADVisualContainer newMetaCont =
            FormUtils.isContainer(source.getInstanceClass()) ?
                new RADVisualContainer() : null;

        while (newMetaComp == null) {
            // initialize metacomponent and its bean instance
            newMetaComp = newMetaCont == null ?
                new RADVisualComponent() : newMetaCont;

            newMetaComp.initialize(formModel);
            if (!initComponentInstance(newMetaComp, source))
                return null;

            if (newMetaCont != null) { // the new component is a container
                // initialize layout support
                newMetaCont.initLayoutSupport();
                if (newMetaCont.getLayoutSupport() == null) {
                    // no LayoutSupportDelegate found for the container,
                    // create RADVisualComponent only
                    newMetaCont = null;
                    newMetaComp = null;
                }
            }
        }

        // get parent container into which the new component will be added
        RADVisualContainer parentCont;
        if (targetComp != null) {
            parentCont = targetComp instanceof RADVisualContainer ?
                (RADVisualContainer) targetComp :
                (RADVisualContainer) targetComp.getParentComponent();
        }
        else parentCont = null;

        // add the new metacomponent to the model
        if (parentCont != null) {
            LayoutConstraints constr =
                constraints instanceof LayoutConstraints ?
                    (LayoutConstraints) constraints : null;
                
            formModel.addVisualComponent(newMetaComp, parentCont, constr);
        }
        else formModel.addComponent(newMetaComp, null);

        // for some components, we initialize their properties with some
        // non-default values e.g. a label on buttons, checkboxes
        defaultComponentInit(newMetaComp);
        getDesigner().setSelectedComponent(newMetaComp);
        return newMetaComp;
    }

    private RADComponent addOtherComponent(
                             CreationFactory.InstanceSource source,
                             RADComponent targetComp)
    {
        RADComponent newMetaComp = new RADComponent();
        newMetaComp.initialize(formModel);
        if (!initComponentInstance(newMetaComp, source))
            return null;

        ComponentContainer targetCont = 
            targetComp instanceof ComponentContainer
                && !(targetComp instanceof RADVisualContainer) ?
            (ComponentContainer) targetComp : null;

        formModel.addComponent(newMetaComp, targetCont);

        getDesigner().setSelectedComponent(newMetaComp);
        return newMetaComp;
    }

    private RADComponent setContainerLayout(
                             CreationFactory.InstanceSource source,
                             RADComponent targetComp)
    {
        if (targetComp == null)
            return addOtherComponent(source, null);

        if (!(targetComp instanceof RADVisualComponent))
            return null;

        // get container on which the layout will be set
        RADVisualContainer metacont =
            targetComp instanceof RADVisualContainer ?
                (RADVisualContainer) targetComp :
                (RADVisualContainer) targetComp.getParentComponent();

        if (metacont == null)
            return null;

        LayoutSupportDelegate layoutDelegate =
                              metacont.getLayoutSupport().getLayoutDelegate();
        if (layoutDelegate != null && layoutDelegate.isDedicated())
//                && layoutSupport.getLayoutClass() == null
//                && layoutSupport.getClass() != NullLayoutSupport.class)
            return null; // layout cannot be changed, should be reported!!

        Class beanClass = source.getInstanceClass();
        layoutDelegate = null;

        // there are three ways how new LayoutSupportDelegate can be created...
        try {
            if (LayoutManager.class.isAssignableFrom(beanClass)) {
                // LayoutManager -> find LayoutSupportDelegate for it
                Class layoutDelegateClass = LayoutSupportRegistry
                                    .getLayoutDelegateForLayout(beanClass);
                if (layoutDelegateClass != null)
                    layoutDelegate = LayoutSupportRegistry
                                     .createLayoutDelegate(layoutDelegateClass);
            }
            else if (LayoutSupportDelegate.class.isAssignableFrom(beanClass)) {
                // LayoutSupportDelegate -> use it directly
                if (source.getInstanceCookie() != null)
                    layoutDelegate = (LayoutSupportDelegate)
                        source.getInstanceCookie().instanceCreate();
                else layoutDelegate = 
                        LayoutSupportRegistry.createLayoutDelegate(beanClass);
            }
//            else if (DesignLayout.class.isAssignableFrom(beanClass)) {
//                // DesignLayout -> convert to LayoutSupportDelegate
//                DesignLayout dl = (DesignLayout) 
//                                  CreationFactory.createInstance(source);
//                layoutSupport =
//                    Compat31LayoutFactory.createCompatibleLayoutSupport(dl);
//            }
        }
        catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                e.printStackTrace();

            TopManager.getDefault().notify(
                new NotifyDescriptor.Message(
                    MessageFormat.format(
                        FormEditor.getFormBundle().getString("FMT_ERR_LayoutInit"), // NOI8N
                        new Object[] { beanClass.getName(),
                                        e.getClass().getName() }),
                    NotifyDescriptor.ERROR_MESSAGE));
            return null;
        }

        if (layoutDelegate == null) {
            TopManager.getDefault().notify(
                new NotifyDescriptor.Message(
                    MessageFormat.format(
                        FormEditor.getFormBundle().getString("FMT_ERR_LayoutNotFound"), // NOI8N
                        new Object[] { beanClass.getName() }),
                    NotifyDescriptor.ERROR_MESSAGE));
            return null;
        }

        formModel.setContainerLayout(metacont, layoutDelegate);

        getDesigner().setSelectedComponent(metacont);
        return metacont;
    }

    private RADComponent setComponentBorder(
                             CreationFactory.InstanceSource source,
                             RADComponent targetComp)
    {
        if (targetComp == null)
            return addOtherComponent(source, null);

        if (!(targetComp instanceof RADVisualComponent))
            return null;

        if (!(JComponent.class.isAssignableFrom(targetComp.getBeanClass()))) {
            TopManager.getDefault().notify(new NotifyDescriptor.Message(
                FormEditor.getFormBundle().getString("MSG_BorderNotApplicable"), // NOI8N
                NotifyDescriptor.INFORMATION_MESSAGE));
            return null;
        }

        RADProperty prop = targetComp.getPropertyByName("border"); // NOI18N
        if (prop == null)
            return null;

        try {
            Object border = CreationFactory.createInstance(source);
            prop.setValue(border);
        }
        catch (Exception ex) {
            showInstErrorMessage(ex);
            return null;
        }

        getDesigner().setSelectedComponent(targetComp);
        return targetComp;
    }

    private RADComponent addMenuComponent(CreationFactory.InstanceSource source,
                                          RADComponent targetComp)
    {
        Class beanClass = source.getInstanceClass();
        ComponentContainer menuContainer = null;

        if (targetComp instanceof RADMenuComponent) {
            // adding to a menu
            if (((RADMenuComponent)targetComp).canAddItem(beanClass))
                menuContainer = (ComponentContainer) targetComp;
        }
        else if (targetComp instanceof RADVisualComponent) {
            RADVisualContainer targetCont =
                targetComp instanceof RADVisualContainer ?
                    (RADVisualContainer) targetComp :
                    (RADVisualContainer) targetComp.getParentComponent();

            if (targetCont != null 
                    && targetCont.getContainerMenu() == null
                    && targetCont.canHaveMenu(beanClass))
                menuContainer = targetCont;
        }

        // create new metacomponent
        RADMenuComponent newMenuComp;
        RADMenuItemComponent newMenuItemComp;
        if ((RADMenuItemComponent.recognizeType(beanClass)
                 & RADMenuItemComponent.MASK_CONTAINER) != 0) {
            newMenuComp = new RADMenuComponent();
            newMenuItemComp = newMenuComp;
        }
        else {
            newMenuComp = null;
            newMenuItemComp = new RADMenuItemComponent();
        }

        newMenuItemComp.initialize(formModel);
        if (!initComponentInstance(newMenuItemComp, source))
            return null;
        if (newMenuComp != null)
            newMenuComp.initSubComponents(new RADComponent[0]);

        // add the new metacomponent to the model
        formModel.addComponent(newMenuItemComp, menuContainer);

        // for some components, we initialize their properties with some
        // non-default values e.g. a label on buttons, checkboxes
        defaultMenuInit(newMenuItemComp);

        // for new menu bars we do some additional special things...
        if (newMenuComp != null) {
            int type = newMenuComp.getMenuItemType();
            if (type == RADMenuItemComponent.T_MENUBAR
                    || type == RADMenuItemComponent.T_JMENUBAR)
            { // create first menu for the new menu bar
                org.openide.util.datatransfer.NewType[] newTypes
                                                   = newMenuComp.getNewTypes();
                if (newTypes.length > 0) {
                    try {
                        newTypes[0].create();
                    }
                    catch (java.io.IOException e) {} // ignore
                }
            }
        }

        getDesigner().setSelectedComponent(newMenuItemComp);
        return newMenuItemComp;
    }

    // --------

    private static boolean initComponentInstance(
                               RADComponent metacomp,
                               CreationFactory.InstanceSource source)
    {
        try {
            if (source.getInstanceCookie() != null) {
                Object instance = source.getInstanceCookie().instanceCreate();
                metacomp.setInstance(instance);
            }
            else metacomp.initInstance(source.getInstanceClass());

            return true;
        }
        catch (Throwable th) {
            if (th instanceof ThreadDeath)
                throw (ThreadDeath)th;
            else
                showInstErrorMessage(th);
        }
        return false;
    }

    // --------

    private static void showInstErrorMessage(Throwable ex) {
        if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
            ex.printStackTrace();

        String message = MessageFormat.format(
            FormEditor.getFormBundle().getString("FMT_ERR_CannotInstantiate"), // NOI18N
            new Object [] { ex.getClass().getName(), ex.getMessage() });
        TopManager.getDefault().notify(new NotifyDescriptor.Message(
            message, NotifyDescriptor.ERROR_MESSAGE));
    }

    private static Class getBeanClass(InstanceCookie ic) {
        try {
            return ic.instanceClass();
        }
        catch (Exception ex) {
            // report exception...
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
            return null;
        }
    }

    // --------

    static void defaultComponentInit(RADComponent radComp) {
        Object comp = radComp.getBeanInstance();
        String varName = radComp.getName();
        String propName = null;
        Object propValue = null;
        if (comp instanceof Button) {
            if ("".equals(((Button)comp).getLabel())) { // NOI18N
                propName = "label"; // NOI18N
                propValue = varName;
            }
        }
        else if (comp instanceof Checkbox) {
            if ("".equals(((Checkbox)comp).getLabel())) { // NOI18N
                propName = "label"; // NOI18N
                propValue = varName;
            }
        }
        else if (comp instanceof Label) {
            if ("".equals(((Label)comp).getText())) { // NOI18N
                propName = "text"; // NOI18N
                propValue = varName;
            }
        }
        else if (comp instanceof TextField) {
            if ("".equals(((TextField)comp).getText())) { // NOI18N
                propName = "text"; // NOI18N
                propValue = varName;
            }
        }
        else if (comp instanceof AbstractButton) { // JButton, JToggleButton, JCheckBox, JRadioButton
            if ("".equals(((AbstractButton)comp).getText())) { // NOI18N
                propName = "text"; // NOI18N
                propValue = varName;
            }
        }
        else if (comp instanceof JLabel) {
            if ("".equals(((JLabel)comp).getText())) { // NOI18N
                propName = "text"; // NOI18N
                propValue = varName;
            }
        }
        else if (comp instanceof JTable) {
            javax.swing.table.TableModel tm =((JTable)comp).getModel();
            if ((tm == null) ||((tm instanceof javax.swing.table.DefaultTableModel) &&
                                (tm.getRowCount() == 0) &&(tm.getColumnCount() == 0)))
            {
                propValue = new org.netbeans.beaninfo.editors.TableModelEditor.NbTableModel(new javax.swing.table.DefaultTableModel(
                    new String[] {"Title 1", "Title 2", "Title 3", "Title 4"}, // NOI18N
                    4
                    ));
                propName = "model"; // NOI18N
            }
        }
        else if ((comp instanceof JTextField) &&(!(comp instanceof JPasswordField))) { // JTextField and not JPasswordField
            if ("".equals(((JTextField)comp).getText())) { // NOI18N
                propName = "text"; // NOI18N
                propValue = varName;
            }
        }
        else if (comp instanceof JInternalFrame) {
            propName = "visible"; // NOI18N
            propValue = new Boolean(true);
        }

        if (propName != null) {
            RADProperty prop = radComp.getPropertyByName(propName);
            if (prop != null) {
                try {
                    prop.setValue(propValue);
                }
                catch (Exception e) {} // never mind, ignore
            }
        }
    }

    static void defaultMenuInit(RADMenuItemComponent menuComp) {
        Object comp = menuComp.getBeanInstance();
        String varName = menuComp.getName();
        String propName = null;
        Object propValue = null;

        if (comp instanceof MenuItem) {
            if ("".equals(((MenuItem)comp).getLabel())) { // NOI18N
                String value = "{0}"; // NOI18N
                propName = "label"; // NOI18N
                if (comp instanceof PopupMenu) {
                    value = FormEditor.getFormBundle().getString("FMT_LAB_PopupMenu");
                }
                else if (comp instanceof Menu) {
                    value = FormEditor.getFormBundle().getString("FMT_LAB_Menu");
                }
                else if (comp instanceof CheckboxMenuItem) {
                    value = FormEditor.getFormBundle().getString("FMT_LAB_CheckboxMenuItem");
                }
                else {
                    value = FormEditor.getFormBundle().getString("FMT_LAB_MenuItem");
                }

                propValue = MessageFormat.format(value, new Object[] { varName });
            }
        }
        else if (comp instanceof JMenuItem) {
            if ("".equals(((JMenuItem)comp).getText())) { // NOI18N
                String value = "{0}"; // NOI18N
                propName = "text"; // NOI18N
                if (comp instanceof JCheckBoxMenuItem) {
                    value = FormEditor.getFormBundle().getString("FMT_LAB_JCheckBoxMenuItem");
                }
                else if (comp instanceof JMenu) {
                    value = FormEditor.getFormBundle().getString("FMT_LAB_JMenu");
                }
                else if (comp instanceof JRadioButtonMenuItem) {
                    value = FormEditor.getFormBundle().getString("FMT_LAB_JRadioButtonMenuItem");
                }
                else {
                    value = FormEditor.getFormBundle().getString("FMT_LAB_JMenuItem");
                }

                propValue = MessageFormat.format(value, new Object[] { varName });
            }
        }
        if (propName != null) {
            RADProperty prop = menuComp.getPropertyByName(propName);
            if (prop != null) {
                try {
                    prop.setValue(propValue);
                }
                catch (Exception e) {} // never mind, ignore
            }
        }
    }
}
