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
import javax.swing.undo.*;
import javax.swing.border.Border;
import java.util.*;
import java.text.MessageFormat;

import org.openide.*;
import org.openide.src.*;
import org.openide.nodes.Node;
//import org.openide.cookies.InstanceCookie;
import org.openide.util.Mutex;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.editors2.BorderDesignSupport;
import org.netbeans.modules.form.codestructure.CodeStructure;

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

    private static final int NO_TARGET = 0;
    private static final int TARGET_LAYOUT = 1;
    private static final int TARGET_BORDER = 2;
    private static final int TARGET_MENU = 3;
    private static final int TARGET_VISUAL = 4;
    private static final int TARGET_OTHER = 5;

    FormModel formModel;

    CreatorCodeUndoableEdit undoEdit;

    MetaComponentCreator(FormModel model) {
        formModel = model;
    }

    /** Creates and adds a new metacomponent to FormModel. The new component
     * is added to target component (if it is ComponentContainer).
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
     * is ComponentContainer).
     * @param ic InstanceCookie for creating the bean instance
     * @param constraints constraints object (for visual components only)
     * @param targetComp component into which the new component is added
     * @return the component if it was successfully created and added (all
     *         errors are reported immediately)
     */
//    public RADComponent createComponent(InstanceCookie ic,
//                                        RADComponent targetComp,
//                                        Object constraints)
//    {
//        CreationFactory.InstanceSource source;
//        try {
//            source = new CreationFactory.InstanceSource(ic);
//        }
//        catch (Exception ex) {
//            showInstErrorMessage(ex);
//            return null;
//        }
//
//        return createAndAddComponent(source, targetComp, constraints);
//    }

    /** Creates a copy of a metacomponent and adds it to FormModel. The new 
     * component is added to target component (if it is ComponentContainer)
     * or applied to it (if it is layout or border).
     * @param sourceComp metacomponent to be copied
     * @param targetComp target component (where the new component is added)
     * @return the component if it was successfully created and added (all
     *         errors are reported immediately)
     */
    public RADComponent copyComponent(final RADComponent sourceComp,
                                      final RADComponent targetComp)
    {
        final int targetPlacement = getTargetPlacement(sourceComp.getBeanClass(),
                                                       targetComp,
                                                       false, false);
        if (targetPlacement == NO_TARGET)
            return null;

        // hack needed due to screwed design of menu metacomponents
        if (targetPlacement == TARGET_MENU
                && !(sourceComp instanceof RADMenuItemComponent))
            return null;

        try { // Look&Feel UI defaults remapping needed
            return (RADComponent) FormLAF.executeWithLookAndFeel(
                new Mutex.ExceptionAction() {
                    public Object run() throws Exception {
                        return copyComponent2(sourceComp,
                                              targetComp,
                                              targetPlacement);
                    }
                }
            );
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean canAddComponent(Class beanClass,
                                          RADComponent targetComp)
    {
        int targetPlacement = getTargetPlacement(beanClass, targetComp,
                                                 false, false);
        return targetPlacement == TARGET_OTHER
                || targetPlacement == TARGET_MENU
                || targetPlacement == TARGET_VISUAL;
    }

    public static boolean canApplyComponent(Class beanClass,
                                            RADComponent targetComp)
    {
        int targetPlacement = getTargetPlacement(beanClass, targetComp,
                                                 false, false);
        return targetPlacement == TARGET_BORDER
                || targetPlacement == TARGET_LAYOUT;
    }

    // --------

    private FormDesigner getDesigner() {
        return formModel.getFormDesigner();
    }

    private RADComponent createAndAddComponent(
                             final CreationFactory.InstanceSource source,
                             final RADComponent targetComp,
                             final Object constraints)
    {
        Class beanClass = source.getInstanceClass();
        if (beanClass == null)
            return null;

        // check adding form class to itself
        if (formModel.getFormBaseClass().isAssignableFrom(beanClass)) {
            // it might be...
            SourceElement formSource =
                FormEditorSupport.getFormDataObject(formModel).getSource();
            if (formSource != null) {
                ClassElement formClass = formSource.getClasses()[0];
                if (formClass != null
                    &&  formClass.getVMName().equals(beanClass.getName()))
                {
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                            FormUtils.getBundleString("MSG_ERR_CannotAddForm"), // NOI18N
                            NotifyDescriptor.WARNING_MESSAGE));
                    return null;
                }
            }
        }

        final int targetPlacement =
            getTargetPlacement(beanClass, targetComp, true, true);

        if (targetPlacement == NO_TARGET)
            return null;

        try { // Look&Feel UI defaults remapping needed
            return (RADComponent) FormLAF.executeWithLookAndFeel(
                new Mutex.ExceptionAction() {
                    public Object run() throws Exception {
                        return createAndAddComponent2(source,
                                                      targetComp, targetPlacement,
                                                      constraints);
                    }
                }
            );
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
            return null;
        }
    }

    private RADComponent createAndAddComponent2(
                             CreationFactory.InstanceSource source,
                             RADComponent targetComp,
                             int targetPlacement,
                             Object constraints)
    {
        if (targetPlacement == TARGET_LAYOUT)
            return setContainerLayout(source, targetComp);

        if (targetPlacement == TARGET_BORDER)
            return setComponentBorder(source, targetComp);

        // turn on undo/redo recording on code structure (if allowed)
        CodeStructure codeStructure = formModel.getCodeStructure();
        if (formModel.isUndoRedoRecording()
            && !codeStructure.isUndoRedoRecording())
        {
            codeStructure.setUndoRedoRecording(true);
            undoEdit = new CreatorCodeUndoableEdit();
            undoEdit.codeUndoRedoStart = codeStructure.markForUndo();
            if (!formModel.isCompoundEditInProgress())
                formModel.fireFormChanged(); // to start compound undo [a bit ugly...]
            formModel.addUndoableEdit(undoEdit);
        }
        else undoEdit = null;

        RADComponent newMetaComp = null;

        if (targetPlacement == TARGET_MENU)
            newMetaComp = addMenuComponent(source, targetComp);

        else if (targetPlacement == TARGET_VISUAL)
            newMetaComp = addVisualComponent(source, targetComp, constraints);

        else if (targetPlacement == TARGET_OTHER)
            newMetaComp = addOtherComponent(source, targetComp);

        else finishCreatorCodeUndoableEdit(); // should not happen

        // finish undo setup
        undoEdit = null;

        return newMetaComp;
    }

    private RADComponent copyComponent2(RADComponent sourceComp,
                                        RADComponent targetComp,
                                        int targetPlacement)
    {
        // if layout or border is to be copied from a meta component, we just
        // apply the cloned instance, but don't copy the meta component
        if (targetPlacement == TARGET_LAYOUT)
            return copyAndApplyLayout(sourceComp, targetComp);

        if (targetPlacement == TARGET_BORDER)
            return copyAndApplyBorder(sourceComp, targetComp);

        // in other cases let's copy the source meta component

        if (sourceComp instanceof RADVisualComponent)
            LayoutSupportManager.storeConstraints(
                                     (RADVisualComponent) sourceComp);

        // turn on undo/redo recording on code structure (if allowed)
        CodeStructure codeStructure = formModel.getCodeStructure();
        if (formModel.isUndoRedoRecording()
            && !codeStructure.isUndoRedoRecording())
        {
            codeStructure.setUndoRedoRecording(true);
            undoEdit = new CreatorCodeUndoableEdit();
            undoEdit.codeUndoRedoStart = codeStructure.markForUndo();
        }
        else undoEdit = null;

        // copy the source metacomponent
        RADComponent newMetaComp = makeCopy(sourceComp, targetPlacement);

        if (newMetaComp == null) { // copying failed (for a mystic reason)
            if (undoEdit != null) {
                codeStructure.setUndoRedoRecording(false);
                undoEdit = null;
            }
            return null;
        }

        if (undoEdit != null) { // finish undo/redo recording on code structure
            undoEdit.codeUndoRedoEnd = codeStructure.markForUndo();
            codeStructure.setUndoRedoRecording(false);
            if (!formModel.isCompoundEditInProgress())
                formModel.fireFormChanged(); // to start compound undo [a bit ugly...]
            formModel.addUndoableEdit(undoEdit);
            undoEdit = null;
        }

        if (targetPlacement == TARGET_MENU) {
            addMenuComponent(newMetaComp, targetComp);
        }
        else if (targetPlacement == TARGET_VISUAL) {
            RADVisualComponent newVisual = (RADVisualComponent) newMetaComp;
            Object constraints;
            if (targetComp != null) {
                constraints = ((RADVisualContainer)targetComp)
                    .getLayoutSupport().getStoredConstraints(newVisual);
            }
            else constraints = null;

            newMetaComp = addVisualComponent(newVisual,
                                             targetComp,
                                             constraints);
            // might be null if layout support did not accept the component
        }
        else if (targetPlacement == TARGET_OTHER) {
            addOtherComponent(newMetaComp, targetComp);
        }
        else finishCreatorCodeUndoableEdit();

        return newMetaComp;
    }

    /** This method is responsible for decision whether a bean can be added to
     * (or applied on) a target component in FormModel. It returns a constant
     * of corresponding target operation. This method is used in two modes.
     * It is more strict for copy/cut/paste operations (paramaters canUseParent
     * and defaultToOthers are set to false), and less strict for visual
     * ("click") operations (canUseParent and defaultToOthers set to true).
     */
    private static int getTargetPlacement(Class beanClass,
                                          RADComponent targetComp,
                                          boolean canUseParent,
                                          boolean defaultToOthers)
    {
        if (LayoutSupportDelegate.class.isAssignableFrom(beanClass)
              || LayoutManager.class.isAssignableFrom(beanClass))
        {   // layout manager
            if (targetComp == null)
                return TARGET_OTHER;

            RADVisualContainer targetCont;
            if (targetComp instanceof RADVisualContainer)
                targetCont = (RADVisualContainer) targetComp;
            else if (canUseParent)
                targetCont = targetComp instanceof RADVisualComponent ?
                    (RADVisualContainer) targetComp.getParentComponent() :
                    null;
            else
                targetCont = null;

            return targetCont != null
                       && !targetCont.getLayoutSupport().isDedicated() ?
                TARGET_LAYOUT : NO_TARGET;
        }

        if (Border.class.isAssignableFrom(beanClass)) { // border
            if (targetComp == null)
                return TARGET_OTHER;

            return targetComp instanceof RADVisualComponent ?
//                       && JComponent.class.isAssignableFrom(beanClass)
                TARGET_BORDER : NO_TARGET;
        }

        if (MenuComponent.class.isAssignableFrom(beanClass)
              || JMenuItem.class.isAssignableFrom(beanClass)
              || JMenuBar.class.isAssignableFrom(beanClass)
              || JPopupMenu.class.isAssignableFrom(beanClass))
        {   // menu
            if (targetComp == null)
                return TARGET_MENU;

            if (targetComp instanceof RADMenuComponent) {
                // adding to a menu
                return ((RADMenuComponent)targetComp).canAddItem(beanClass) ?
                    TARGET_MENU : NO_TARGET;
            }
            else { // adding to a visual container?
                RADVisualContainer targetCont;
                if (targetComp instanceof RADVisualContainer)
                    targetCont = (RADVisualContainer) targetComp;
                else if (canUseParent)
                    targetCont = targetComp instanceof RADVisualComponent ?
                        (RADVisualContainer) targetComp.getParentComponent() :
                        null;
                else
                    targetCont = null;

                if (targetCont != null) { // yes, this is a visual container
                    if (targetCont.getContainerMenu() == null
                            && targetCont.canHaveMenu(beanClass))
                        return TARGET_MENU;
                }
                else return NO_TARGET; // unknown container

                return defaultToOthers ? TARGET_MENU : NO_TARGET;
                // [Temporary solution - better would be to let the menu be
                // tried as a visual component (Now, it would not have special
                // features of menu components like adding submenus, menu items,
                // etc). This should be fixed. Meanwhile, we return here...]
            }
        }

        else if (JSeparator.class.isAssignableFrom(beanClass)
                 || Separator.class.isAssignableFrom(beanClass))
        {   // separator
            if (targetComp == null)
                return TARGET_VISUAL;

            if (targetComp instanceof RADMenuComponent) {
                // adding to a menu
                return ((RADMenuComponent)targetComp).canAddItem(beanClass) ?
                    TARGET_MENU : NO_TARGET;
            }
        }

        if (Component.class.isAssignableFrom(beanClass)) {
            // visual component
            if (targetComp == null)
                return TARGET_VISUAL;

            if (java.awt.Window.class.isAssignableFrom(beanClass)
                    || java.applet.Applet.class.isAssignableFrom(beanClass))
                return defaultToOthers ? TARGET_VISUAL : NO_TARGET;

            if (!(targetComp instanceof RADVisualComponent))
                return NO_TARGET; // no visual target

            if (!canUseParent && !(targetComp instanceof RADVisualContainer))
                return NO_TARGET; // no visual container target

            return TARGET_VISUAL;
        }

        if (targetComp == null || defaultToOthers)
            return TARGET_OTHER;

        return NO_TARGET;
    }

    // ---------

    private RADComponent makeCopy(RADComponent sourceComp, int targetPlacement) {
        RADComponent newComp;

        if (sourceComp instanceof RADVisualContainer)
            newComp = new RADVisualContainer();
        else if (sourceComp instanceof RADVisualComponent) {
            if (targetPlacement == TARGET_MENU)
                newComp = new RADMenuItemComponent();
            else
                newComp = new RADVisualComponent();
        }
        else if (sourceComp instanceof RADMenuComponent)
            newComp = new RADMenuComponent();
        else if (sourceComp instanceof RADMenuItemComponent) {
            if (targetPlacement == TARGET_VISUAL)
                newComp = new RADVisualComponent();
            else
                newComp = new RADMenuItemComponent();
        }
        else
            newComp = new RADComponent();

        newComp.initialize(formModel);
        if (sourceComp != sourceComp.getFormModel().getTopRADComponent())
            newComp.setStoredName(sourceComp.getName());

        try {
            newComp.initInstance(sourceComp.getBeanClass());
        }
        catch (Exception ex) { // this is rather unlikely to fail
            ErrorManager em = ErrorManager.getDefault();
            em.annotate(ex,
                        FormUtils.getBundleString("MSG_ERR_CannotCopyInstance")); // NOI18N
            em.notify(ex);
            return null;
        }

        // 1st - copy subcomponents
        if (sourceComp instanceof ComponentContainer) {
            RADComponent[] sourceSubs =
                ((ComponentContainer)sourceComp).getSubBeans();
            RADComponent[] newSubs = new RADComponent[sourceSubs.length];

            for (int i=0; i < sourceSubs.length; i++) {
                RADComponent newSubComp = makeCopy(sourceSubs[i], -1);
                if (newSubComp == null)
                    return null;
                newSubs[i] = newSubComp;
            }

            ((ComponentContainer)newComp).initSubComponents(newSubs);

            // 2nd - clone layout support
            if (sourceComp instanceof RADVisualContainer) {
                RADVisualComponent[] newComps =
                    new RADVisualComponent[newSubs.length];
                System.arraycopy(newSubs, 0, newComps, 0, newSubs.length);

                LayoutSupportManager sourceLayout =
                    ((RADVisualContainer)sourceComp).getLayoutSupport();

                ((RADVisualContainer)newComp).getLayoutSupport()
                    .copyLayoutDelegateFrom(sourceLayout, newComps);
            }
        }

        // 3rd - copy changed properties
        java.util.List sourceList = new ArrayList();
        java.util.List namesList = new ArrayList();

        Iterator it = sourceComp.getBeanPropertiesIterator(
                                   FormProperty.CHANGED_PROPERTY_FILTER,
                                   false);
        while (it.hasNext()) {
            RADProperty prop = (RADProperty) it.next();
            sourceList.add(prop);
            namesList.add(prop.getName());
        }

        RADProperty[] sourceProps = new RADProperty[sourceList.size()];
        sourceList.toArray(sourceProps);
        String[] propNames = new String[namesList.size()];
        namesList.toArray(propNames);
        RADProperty[] newProps = newComp.getBeanProperties(propNames);
        int copyMode = FormUtils.DISABLE_CHANGE_FIRING;
        if (formModel == sourceComp.getFormModel())
            copyMode |= FormUtils.PASS_DESIGN_VALUES;

        FormUtils.copyProperties(sourceProps, newProps, copyMode);

        // temporary hack for AWT menus - to update their Swing design parallels
        if (newComp instanceof RADMenuItemComponent)
            formModel.fireComponentPropertyChanged(newComp, null, null, null);

        // 4th - copy aux values
        Map auxValues = sourceComp.getAuxValues();
        if (auxValues != null)
            for (it = auxValues.keySet().iterator(); it.hasNext(); ) {
                String auxName = (String) it.next();
                Object auxValue = auxValues.get(auxName);
                try {
                    newComp.setAuxValue(auxName,
                                        FormUtils.cloneObject(auxValue));
                }
                catch (Exception e) { // ignore problem with aux value
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }

        // 5th - copy layout constraints
        if (sourceComp instanceof RADVisualComponent
            && newComp instanceof RADVisualComponent)
        {
            Map constraints = ((RADVisualComponent)sourceComp).getConstraintsMap();
            Map newConstraints = new HashMap();

            for (it = constraints.keySet().iterator(); it.hasNext(); ) {
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

    // --------

    private RADComponent addVisualComponent(
                             CreationFactory.InstanceSource source,
                             RADComponent targetComp,
                             Object constraints)
    {
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

            if (newMetaCont == null)
                break; // not a container, the component is done

            // initialize layout support (the new component is a container)
            boolean layoutInitialized = false;
            Throwable layoutEx = null;
            try {
                layoutInitialized = newMetaCont.getLayoutSupport()
                                      .initializeLayoutDelegate(false);
            }
            catch (RuntimeException ex) { // silently ignore, try again as non-container
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                newMetaComp.removeCodeExpression(); // created in initComponentInstance
                newMetaComp = null;
                newMetaCont = null;
                continue;
            }
            catch (Exception ex) {
                layoutEx = ex;
            }
            catch (LinkageError ex) {
                layoutEx = ex;
            }

            if (!layoutInitialized) {
                if (layoutEx == null) {
                    // no LayoutSupportDelegate found for the container
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                            FormUtils.getBundleString(
                                        "MSG_ERR_NoLayoutSupportFound2"), // NOI18N
                        NotifyDescriptor.WARNING_MESSAGE));
                }
                else { // layout support initialization failed
                    ErrorManager em = ErrorManager.getDefault();
                    em.annotate(
                        layoutEx, 
                        FormUtils.getBundleString("MSG_ERR_LayoutInitFailed2")); // NOI18N
                    em.notify(layoutEx);
                }

                newMetaCont.getLayoutSupport().setUnknownLayoutDelegate(false);
            }
        }

        // for some components, we initialize their properties with some
        // non-default values e.g. a label on buttons, checkboxes
        defaultComponentInit(newMetaComp);

        finishCreatorCodeUndoableEdit();

        Class beanClass = newMetaComp.getBeanClass();
        if (java.awt.Window.class.isAssignableFrom(beanClass)
                || java.applet.Applet.class.isAssignableFrom(beanClass))
            targetComp = null;
        
        return addVisualComponent(newMetaComp, targetComp, constraints);
    }

    private RADComponent addVisualComponent(RADVisualComponent newMetaComp,
                                            RADComponent targetComp,
                                            Object constraints)
    {
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

            try {
                formModel.addVisualComponent(newMetaComp, parentCont, constr);
            }
            catch (RuntimeException ex) {
                // LayoutSupportDelegate may not accept the component
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return null;
            }
        }
        else formModel.addComponent(newMetaComp, null);

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

        finishCreatorCodeUndoableEdit();

        addOtherComponent(newMetaComp, targetComp);
        return newMetaComp;
    }

    private void addOtherComponent(RADComponent newMetaComp,
                                   RADComponent targetComp)
    {
        ComponentContainer targetCont = 
            targetComp instanceof ComponentContainer
                && !(targetComp instanceof RADVisualContainer)
                && !(targetComp instanceof RADMenuComponent) ?
            (ComponentContainer) targetComp : null;

        formModel.addComponent(newMetaComp, targetCont);
    }

    private RADComponent setContainerLayout(
                             CreationFactory.InstanceSource source,
                             RADComponent targetComp)
    {
        Class layoutClass = source.getInstanceClass();
        LayoutManager layoutInstance;
        if (LayoutManager.class.isAssignableFrom(layoutClass)
            && source.getInstanceCookie() != null)
        {
            try {
                layoutInstance = (LayoutManager)
                                 source.getInstanceCookie().instanceCreate();
            }
            catch (Exception ex) {
                showInstErrorMessage(ex);
                return null;
            }
            catch (LinkageError ex) {
                showInstErrorMessage(ex);
                return null;
            }
        }
        else layoutInstance = null;

        return setContainerLayout(layoutClass, layoutInstance, targetComp);
    }

    private RADComponent setContainerLayout(Class layoutClass,
                                            LayoutManager layoutInstance,
                                            RADComponent targetComp)
    {
        // get container on which the layout is to be set
        RADVisualContainer metacont;
        if (targetComp instanceof RADVisualContainer)
            metacont = (RADVisualContainer) targetComp;
        else {
            metacont = (RADVisualContainer) targetComp.getParentComponent();
            if (metacont == null)
                return null;
        }

        LayoutSupportDelegate layoutDelegate = null;
        Throwable t = null;
        try {
            if (LayoutManager.class.isAssignableFrom(layoutClass)) {
                // LayoutManager -> find LayoutSupportDelegate for it
                layoutDelegate = LayoutSupportRegistry
                                     .createSupportForLayout(layoutClass);
            }
            else if (LayoutSupportDelegate.class.isAssignableFrom(layoutClass)) {
                // LayoutSupportDelegate -> use it directly
                layoutDelegate = LayoutSupportRegistry
                                     .createSupportInstance(layoutClass);
            }
        }
        catch (Exception ex) {
            t = ex;
        }
        catch (LinkageError ex) {
            t = ex;
        }
        if (t != null) {
            String msg = FormUtils.getFormattedBundleString(
                "FMT_ERR_LayoutInit", // NOI18N
                new Object[] { layoutClass.getName() });

            ErrorManager em = ErrorManager.getDefault();
            em.annotate(t, msg);
            em.notify(t);
            return null;
        }

        if (layoutDelegate == null) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(
                    FormUtils.getFormattedBundleString(
                        "FMT_ERR_LayoutNotFound", // NOI18N
                        new Object[] { layoutClass.getName() }),
                    NotifyDescriptor.WARNING_MESSAGE));

            return null;
        }

        try {
            formModel.setContainerLayout(metacont,
                                         layoutDelegate,
                                         layoutInstance);
        }
        catch (Exception ex) {
            t = ex;
        }
        catch (LinkageError ex) {
            t = ex;
        }
        if (t != null) {
            String msg = FormUtils.getFormattedBundleString(
                "FMT_ERR_LayoutInit", // NOI18N
                new Object[] { layoutClass.getName() });

            ErrorManager em = ErrorManager.getDefault();
            em.annotate(t, msg);
            em.notify(t);
            return null;
        }

        return metacont;
    }

    private RADComponent copyAndApplyLayout(RADComponent sourceComp,
                                            RADComponent targetComp)
    {
        try {
            LayoutManager lmInstance = (LayoutManager)
                                       sourceComp.cloneBeanInstance(null);
            // we clone the instance as we need the property values copied
            // for the LayoutSupportDelegate initialization which is done
            // and the delegate set before we can copy the properties...

            RADVisualContainer targetCont = (RADVisualContainer)
                setContainerLayout(sourceComp.getBeanClass(),
                                   lmInstance,
                                   targetComp);

            // copy properties additionally to handle design values
            Node.Property[] sourceProps = sourceComp.getKnownBeanProperties();
            Node.Property[] targetProps =
                targetCont.getLayoutSupport().getAllProperties();
            int copyMode = FormUtils.CHANGED_ONLY
                           | FormUtils.DISABLE_CHANGE_FIRING;
            if (formModel == sourceComp.getFormModel())
                copyMode |= FormUtils.PASS_DESIGN_VALUES;

            FormUtils.copyProperties(sourceProps, targetProps, copyMode);
        }
        catch (Exception ex) { // ignore
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        catch (LinkageError ex) { // ignore
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return targetComp;
    }

    private RADComponent setComponentBorder(
                             CreationFactory.InstanceSource source,
                             RADComponent targetComp)
    {
        FormProperty prop = getBorderProperty(targetComp);
        if (prop == null)
            return null;

        try { // set border property
            Object border = CreationFactory.createInstance(source);
            prop.setValue(border);
        }
        catch (Exception ex) {
            showInstErrorMessage(ex);
            return null;
        }
        catch (LinkageError ex) {
            showInstErrorMessage(ex);
            return null;
        }

        getDesigner().setSelectedComponent(targetComp);
        return targetComp;
    }

    private void setComponentBorderProperty(Object borderInstance,
                                            RADComponent targetComp)
    {
        FormProperty prop = getBorderProperty(targetComp);
        if (prop == null)
            return;

        try { // set border property
            prop.setValue(borderInstance);
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
            return;
        }

        getDesigner().setSelectedComponent(targetComp);
    }

    private RADComponent copyAndApplyBorder(RADComponent sourceComp,
                                            RADComponent targetComp)
    {
        try {
            Border borderInstance = (Border) sourceComp.createBeanInstance();
            BorderDesignSupport designBorder =
                new BorderDesignSupport(borderInstance);

            Node.Property[] sourceProps = sourceComp.getKnownBeanProperties();
            Node.Property[] targetProps = designBorder.getProperties();
            int copyMode = FormUtils.CHANGED_ONLY | FormUtils.DISABLE_CHANGE_FIRING;
            if (formModel == sourceComp.getFormModel())
                copyMode |= FormUtils.PASS_DESIGN_VALUES;

            FormUtils.copyProperties(sourceProps, targetProps, copyMode);

            setComponentBorderProperty(designBorder, targetComp);
        }
        catch (Exception ex) { // ignore
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        catch (LinkageError ex) { // ignore
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return targetComp;
    }

    private FormProperty getBorderProperty(RADComponent targetComp) {
        FormProperty prop;
        if (JComponent.class.isAssignableFrom(targetComp.getBeanClass())
                && (prop = targetComp.getBeanProperty("border")) != null) // NOI18N
            return prop;

        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
            FormUtils.getBundleString("MSG_BorderNotApplicable"), // NOI18N
            NotifyDescriptor.INFORMATION_MESSAGE));

        return null;
    }

    private RADComponent addMenuComponent(CreationFactory.InstanceSource source,
                                          RADComponent targetComp)
    {
        // create new metacomponent
        RADMenuComponent newMenuComp;
        RADMenuItemComponent newMenuItemComp;
        if ((RADMenuItemComponent.recognizeType(source.getInstanceClass())
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

        // for some components, we initialize their properties with some
        // non-default values e.g. a label on buttons, checkboxes
        defaultMenuInit(newMenuItemComp);

        finishCreatorCodeUndoableEdit();

        addMenuComponent(newMenuItemComp, targetComp);

        // for new menu bars we do some additional special things...
        if (newMenuComp != null) {
            int type = newMenuComp.getMenuItemType();
            if (type == RADMenuItemComponent.T_MENUBAR
                    || type == RADMenuItemComponent.T_JMENUBAR)
            { // create first menu for the new menu bar
                org.openide.util.datatransfer.NewType[]
                    newTypes = newMenuComp.getNewTypes();
                if (newTypes.length > 0) {
                    try {
                        newTypes[0].create();
                    }
                    catch (java.io.IOException e) {} // ignore
                }
            }
        }

        return newMenuItemComp;
    }

    private void addMenuComponent(RADComponent newMenuComp,
                                  RADComponent targetComp)
    {
        Class beanClass = newMenuComp.getBeanClass();
        ComponentContainer menuContainer = null;

        if (targetComp instanceof RADMenuComponent) {
            // adding to a menu
            if (newMenuComp instanceof RADMenuItemComponent
                    && ((RADMenuComponent)targetComp).canAddItem(beanClass))
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

        formModel.addComponent(newMenuComp, menuContainer);
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
        catch (Exception ex) {
            showInstErrorMessage(ex);
        }
        catch (LinkageError ex) {
            showInstErrorMessage(ex);
        }
        return false;
    }

    // --------

    private static void showInstErrorMessage(Throwable ex) {
        ErrorManager em = ErrorManager.getDefault();
        em.annotate(ex,
                    FormUtils.getBundleString("MSG_ERR_CannotInstantiate")); // NOI18N
        em.notify(ex);
    }

/*    private static Class getBeanClass(InstanceCookie ic) {
        try {
            return ic.instanceClass();
        }
        catch (Exception ex) {
            // report exception...
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
        catch (LinkageError ex) {
            // report exception...
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
    } */

    // --------
    // default component initialization

    static void defaultComponentInit(RADComponent radComp) {
        Object comp = radComp.getBeanInstance();
        String varName = radComp.getName();
        String propName = null;
        Object propValue = null;

        if (comp instanceof AbstractButton) { // JButton, JToggleButton, JCheckBox, JRadioButton
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
            javax.swing.table.TableModel tm = ((JTable)comp).getModel();
            if (tm == null
                || (tm instanceof javax.swing.table.DefaultTableModel
                    && tm.getRowCount() == 0 && tm.getColumnCount() == 0))
            {
                propValue =
                    new org.netbeans.modules.form.editors2.TableModelEditor.NbTableModel(
                        new javax.swing.table.DefaultTableModel(
                            new String[] {
                                "Title 1", "Title 2", "Title 3", "Title 4" }, // NOI18N
                            4));
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
            propValue = Boolean.TRUE;
        }
        else if (comp instanceof Button) {
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

        if (propName != null) {
            FormProperty prop = radComp.getBeanProperty(propName);
            if (prop != null) {
                try {
                    prop.setChangeFiring(false);
                    prop.setValue(propValue);
                    prop.setChangeFiring(true);
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

        if (comp instanceof JMenuItem) {
            if ("".equals(((JMenuItem)comp).getText())) { // NOI18N
                String value = "{0}"; // NOI18N
                propName = "text"; // NOI18N
                if (comp instanceof JCheckBoxMenuItem)
                    value = FormUtils.getBundleString("FMT_LAB_JCheckBoxMenuItem"); // NOI18N
                else if (comp instanceof JMenu)
                    value = FormUtils.getBundleString("FMT_LAB_JMenu"); // NOI18N
                else if (comp instanceof JRadioButtonMenuItem)
                    value = FormUtils.getBundleString("FMT_LAB_JRadioButtonMenuItem"); // NOI18N
                else
                    value = FormUtils.getBundleString("FMT_LAB_JMenuItem"); // NOI18N

                propValue = MessageFormat.format(value, new Object[] { varName });
            }
        }
        else if (comp instanceof MenuItem) {
            if ("".equals(((MenuItem)comp).getLabel())) { // NOI18N
                String value = "{0}"; // NOI18N
                propName = "label"; // NOI18N
                if (comp instanceof PopupMenu)
                    value = FormUtils.getBundleString("FMT_LAB_PopupMenu"); // NOI18N
                else if (comp instanceof Menu)
                    value = FormUtils.getBundleString("FMT_LAB_Menu"); // NOI18N
                else if (comp instanceof CheckboxMenuItem)
                    value = FormUtils.getBundleString("FMT_LAB_CheckboxMenuItem"); // NOI18N
                else
                    value = FormUtils.getBundleString("FMT_LAB_MenuItem"); // NOI18N

                propValue = MessageFormat.format(value, new Object[] { varName });
            }
        }

        if (propName != null) {
            RADProperty prop = menuComp.getBeanProperty(propName);
            if (prop != null) {
                try {
                    prop.setChangeFiring(false);
                    prop.setValue(propValue);
                    prop.setChangeFiring(true);
                }
                catch (Exception e) {} // never mind, ignore
            }
        }
    }

    //---------

    private void finishCreatorCodeUndoableEdit() {
        if (undoEdit != null) { // finish undo/redo recording on code structure
            CodeStructure codeStructure = formModel.getCodeStructure();
            undoEdit.codeUndoRedoEnd = codeStructure.markForUndo();
            codeStructure.setUndoRedoRecording(false);
        }
    }

    private class CreatorCodeUndoableEdit extends AbstractUndoableEdit {
        private Object codeUndoRedoStart;
        private Object codeUndoRedoEnd;

        public void undo() throws CannotUndoException {
            super.undo();

            if (codeUndoRedoStart != null
                    && !codeUndoRedoStart.equals(codeUndoRedoEnd))
                formModel.getCodeStructure().undoToMark(codeUndoRedoStart);
        }

        public void redo() throws CannotRedoException {
            super.redo();

            if (codeUndoRedoEnd != null
                    && !codeUndoRedoEnd.equals(codeUndoRedoStart))
                formModel.getCodeStructure().redoToMark(codeUndoRedoEnd);
        }

        public String getUndoPresentationName() {
            return ""; // NOI18N
        }
        public String getRedoPresentationName() {
            return ""; // NOI18N
        }

        public void die() {
            // it's very important to release undo changes from CodeStructure
            if (codeUndoRedoStart != null && codeUndoRedoEnd != null)
                formModel.getCodeStructure().releaseUndoableChanges(
                                  codeUndoRedoStart, codeUndoRedoEnd);
        }
    }
}
