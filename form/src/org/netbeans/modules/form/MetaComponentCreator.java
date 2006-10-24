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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.swing.*;
import javax.swing.undo.*;
import javax.swing.border.Border;
import java.util.*;
import java.text.MessageFormat;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;

import org.openide.*;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.layoutdesign.*;
import org.netbeans.modules.form.layoutdesign.support.SwingLayoutBuilder;
import org.netbeans.modules.form.editors2.BorderDesignSupport;
import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.form.project.ClassPathUtils;

/**
 * This class represents an access point for adding new components to FormModel.
 * Its responsibility is to create new meta components (from provided bean
 * classes) and add them to the FormModel. In some cases, no new component is
 * created, just modified (e.g. when a border is applied). This class is
 * intended to process user actions, so all errors are caught and reported here.
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

    private RADVisualComponent preMetaComp;
    private LayoutComponent preLayoutComp;

    MetaComponentCreator(FormModel model) {
        formModel = model;
    }

    /** Creates and adds a new metacomponent to FormModel. The new component
     * is added to target component (if it is ComponentContainer).
     * @param classSource ClassSource describing the component class
     * @param constraints constraints object (for visual components only)
     * @param targetComp component into which the new component is added
     * @return the metacomponent if it was successfully created and added (all
     *         errors are reported immediately)
     */
    public RADComponent createComponent(ClassSource classSource,
                                        RADComponent targetComp,
                                        Object constraints)
    {
        Class compClass = prepareClass(classSource);
        if (compClass == null)
            return null; // class loading failed

        return createAndAddComponent(compClass, targetComp, constraints);
    }

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
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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
    // Visual component can be precreated before added to form to provide for
    // better visual feedback when being added. The precreated component may
    // end up as added or canceled. If it is added to the form (by the user),
    // addPrecreatedComponent methods gets called. If adding is canceled for
    // whatever reason, releasePrecreatedComponent is called.

    RADVisualComponent precreateVisualComponent(ClassSource classSource) {
        final Class compClass = prepareClass(classSource);

        if (compClass == null
              || java.awt.Window.class.isAssignableFrom(compClass)
              || java.applet.Applet.class.isAssignableFrom(compClass)
              || getTargetPlacement(compClass, null, true, false)
                    != TARGET_VISUAL)
            return null;

        if (preMetaComp != null)
            releasePrecreatedComponent();

        try { // Look&Feel UI defaults remapping needed
            FormLAF.executeWithLookAndFeel(
                new Mutex.ExceptionAction() {
                    public Object run() throws Exception {
                        preMetaComp = createVisualComponent(compClass);
                        return preMetaComp;
                    }
                }
            );
            return preMetaComp;
        }
        catch (Exception ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
    }

    RADVisualComponent getPrecreatedMetaComponent() {
        return preMetaComp;
    }

    LayoutComponent getPrecreatedLayoutComponent() {
        if (preMetaComp != null) {
            if (preLayoutComp == null) {
                preLayoutComp = createLayoutComponent(preMetaComp);
            }
            return preLayoutComp;
        }
        return null;
    }

    LayoutComponent createLayoutComponent(RADVisualComponent metacomp) {
        Dimension initialSize = prepareDefaultLayoutSize(
                (Component)metacomp.getBeanInstance(),
                metacomp instanceof RADVisualContainer);
        boolean isLayoutContainer = shouldBeLayoutContainer(metacomp);
        if (isLayoutContainer) {
            RADVisualContainer metacont = (RADVisualContainer)metacomp;
            Container cont = metacont.getContainerDelegate(metacont.getBeanInstance());
            if (initialSize == null) {
                initialSize = cont.getPreferredSize();
            }
            Insets insets = cont.getInsets();
            initialSize.width -= insets.left + insets.right;
            initialSize.height -= insets.top + insets.bottom;
            initialSize.width = Math.max(initialSize.width, 0); // Issue 83945
            initialSize.height = Math.max(initialSize.height, 0);
        }
        // test code logging - only for precreation
        if (metacomp == preMetaComp) {
            LayoutDesigner ld = FormEditor.getFormDesigner(formModel).getLayoutDesigner();
            if ((ld != null) && ld.logTestCode()) {
                if (initialSize == null) {
                    ld.testCode.add("lc = new LayoutComponent(\"" + metacomp.getId() + "\", " + isLayoutContainer + ");"); //NOI18N
                } else {
                    ld.testCode.add("lc = new LayoutComponent(\"" + metacomp.getId() + "\", " + isLayoutContainer + ", " + //NOI18N 
                                                                initialSize.width + ", " + initialSize.height + ");"); //NOI18N
                } 
            }
        }
        return initialSize == null ?
            new LayoutComponent(metacomp.getId(), isLayoutContainer) :
            new LayoutComponent(metacomp.getId(), isLayoutContainer,
                                initialSize.width, initialSize.height);
    }

    static boolean shouldBeLayoutContainer(RADComponent metacomp) {
        return metacomp instanceof RADVisualContainer
               && ((RADVisualContainer)metacomp).getLayoutSupport() == null;
    }

    boolean addPrecreatedComponent(RADComponent targetComp,
                                Object constraints)
    {
        if (preMetaComp == null)
            return false;
        if (checkFormClass(preMetaComp.getBeanClass())) {
            addVisualComponent2(preMetaComp,
                                targetComp,
                                constraints);

            preMetaComp = null;
            preLayoutComp = null;
            return true;
        } else {
            releasePrecreatedComponent();
            return false;
        }
    }

    void releasePrecreatedComponent() {
        if (preMetaComp != null) {
            preMetaComp = null;
            preLayoutComp = null;
        }
    }

    // --------

    private RADComponent createAndAddComponent(final Class compClass,
                                               final RADComponent targetComp,
                                               final Object constraints)
    {
        // check adding form class to itself
        if (!checkFormClass(compClass))
            return null;

        final int targetPlacement =
            getTargetPlacement(compClass, targetComp, true, true);

        if (targetPlacement == NO_TARGET)
            return null;

        try { // Look&Feel UI defaults remapping needed
            return (RADComponent) FormLAF.executeWithLookAndFeel(
                new Mutex.ExceptionAction() {
                    public Object run() throws Exception {
                        return createAndAddComponent2(compClass,
                                                      targetComp,
                                                      targetPlacement,
                                                      constraints);
                    }
                }
            );
        }
        catch (Exception ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
    }

    private RADComponent createAndAddComponent2(Class compClass,
                                                RADComponent targetComp,
                                                int targetPlacement,
                                                Object constraints)
    {
        if (targetPlacement == TARGET_LAYOUT)
            return setContainerLayout(compClass, null, targetComp);

        if (targetPlacement == TARGET_BORDER)
            return setComponentBorder(compClass, targetComp);

        RADComponent newMetaComp = null;

        if (targetPlacement == TARGET_MENU)
            newMetaComp = addMenuComponent(compClass, targetComp);

        else if (targetPlacement == TARGET_VISUAL) {
            newMetaComp = addVisualComponent(compClass, targetComp, constraints);
            if (java.awt.Window.class.isAssignableFrom(compClass) ||
                java.applet.Applet.class.isAssignableFrom(compClass)) {
                targetComp = null;
            }
        } else if (targetPlacement == TARGET_OTHER)
            newMetaComp = addOtherComponent(compClass, targetComp);

        if (newMetaComp instanceof RADVisualComponent
            && (shouldBeLayoutContainer(targetComp)
                || (shouldBeLayoutContainer(newMetaComp))))
        {   // container with new layout...
            createAndAddLayoutComponent((RADVisualComponent)newMetaComp,
                                        (RADVisualContainer)targetComp,
                                        null);
        }

        return newMetaComp;
    }
    
    private void createAndAddLayoutComponent(RADVisualComponent radComp,
            RADVisualContainer targetCont, LayoutComponent prototype) {
        LayoutModel layoutModel = formModel.getLayoutModel();
        LayoutComponent layoutComp = layoutModel.getLayoutComponent(radComp.getId());
        if (layoutComp == null) {
            layoutComp = createLayoutComponent(radComp);
        }
        javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
        boolean autoUndo = true;
        try {
            LayoutComponent parent = shouldBeLayoutContainer(targetCont) ?
                layoutModel.getLayoutComponent(targetCont.getId()) : null;
            layoutModel.addNewComponent(layoutComp, parent, prototype);
            autoUndo = false;
        } finally {
            formModel.addUndoableEdit(ue);
            if (autoUndo) {
                formModel.forceUndoOfCompoundEdit();
            }
        }
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

        // copy the source metacomponent
        RADComponent newMetaComp = makeCopy(sourceComp, targetPlacement);

        if (newMetaComp == null) { // copying failed (for a mystic reason)
            return null;
        }

        I18nSupport.internationalizeComponent(newMetaComp);

        if (targetPlacement == TARGET_MENU) {
            addMenuComponent(newMetaComp, targetComp);
        }
        else if (targetPlacement == TARGET_VISUAL) {
            RADVisualComponent newVisual = (RADVisualComponent) newMetaComp;
            Object constraints;
            boolean addLayoutComponent = false;
            if (targetComp != null) {
                RADVisualContainer targetCont = (RADVisualContainer)targetComp;
                LayoutSupportManager layoutSupport = targetCont.getLayoutSupport();
                if (layoutSupport == null) {
                    constraints = null;
                    addLayoutComponent = true;
                } else {
                    constraints = layoutSupport.getStoredConstraints(newVisual);
                }
            }
            else constraints = null;

            newMetaComp = addVisualComponent2(newVisual, targetComp, constraints);
            // might be null if layout support did not accept the component

            if (addLayoutComponent) {
                RADComponent parent = sourceComp.getParentComponent();
                LayoutComponent source = null;
                if ((parent instanceof RADVisualContainer)
                    && (((RADVisualContainer)parent).getLayoutSupport() == null)) {
                    source = sourceComp.getFormModel().getLayoutModel().getLayoutComponent(sourceComp.getId());
                }
                createAndAddLayoutComponent(newVisual, (RADVisualContainer)targetComp, source);
            }
        }
        else if (targetPlacement == TARGET_OTHER) {
            addOtherComponent(newMetaComp, targetComp);
        }

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

            return targetCont != null && !targetCont.hasDedicatedLayoutSupport() ?
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
            newComp.setInModel(true); // need code epxression created (issue 68897)
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
                
                if (sourceLayout != null) {
                    RADVisualContainer newCont = (RADVisualContainer)newComp;
                    newCont.setOldLayoutSupport(true);
                    newCont.getLayoutSupport()
                        .copyLayoutDelegateFrom(sourceLayout, newComps);
                } else {
                    Map sourceToTargetIds = new HashMap(sourceSubs.length);
                    for (int i=0; i<sourceSubs.length; i++) {
                        sourceToTargetIds.put(sourceSubs[i].getId(), newSubs[i].getId());
                    }
                    LayoutModel sourceLayoutModel = sourceComp.getFormModel().getLayoutModel();
                    String sourceContainerId = sourceComp.getId();
                    String targetContainerId = newComp.getId();
                    formModel.getLayoutModel().copyModelFrom(sourceLayoutModel, sourceToTargetIds,
                        sourceContainerId, targetContainerId);
                }
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
            for (it = auxValues.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry)it.next();
                String auxName = (String)entry.getKey();
                Object auxValue = entry.getValue();
                try {
                    newComp.setAuxValue(auxName,
                                        FormUtils.cloneObject(auxValue, formModel));
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

            for (it = constraints.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry)it.next();
                Object layoutClassName = entry.getKey();
                LayoutConstraints clonedConstr =
                    ((LayoutConstraints)entry.getValue())
                        .cloneConstraints();
                newConstraints.put(layoutClassName, clonedConstr);
            }
            ((RADVisualComponent)newComp).setConstraintsMap(newConstraints);
        }

        // 6th - copy events 
	Event[] sourceEvents = sourceComp.getKnownEvents();				
	String[] eventNames = new String[sourceEvents.length];
	String[][] eventHandlers = new String[sourceEvents.length][];	    	
	for (int eventsIdx=0; eventsIdx < sourceEvents.length; eventsIdx++) {	    	    
	    eventNames[eventsIdx] = sourceEvents[eventsIdx].getName(); 
	    eventHandlers[eventsIdx] = sourceEvents[eventsIdx].getEventHandlers();	    	    
	}	
		
	FormEvents formEvents = formModel.getFormEvents();	    	
	Event[] targetEvents = newComp.getEvents(eventNames);	
	for (int targetEventsIdx = 0; targetEventsIdx < targetEvents.length; targetEventsIdx++) {			    
	    
	    Event targetEvent = targetEvents[targetEventsIdx];
	    if (targetEvent == null) 
		continue; // [uknown event error - should be reported!]

	    String[] handlers = eventHandlers[targetEventsIdx];
	    for (int handlersIdx = 0; handlersIdx < handlers.length; handlersIdx++) {		
		String newHandlerName;
		String oldHandlerName = handlers[handlersIdx];		
		String sourceVariableName = sourceComp.getName();
		String targetVariableName = newComp.getName();

		int idx = oldHandlerName.indexOf(sourceVariableName);
		if (idx >= 0) {
		    newHandlerName = oldHandlerName.substring(0, idx)
				   + targetVariableName
				   + oldHandlerName.substring(idx + sourceVariableName.length());
		} else {
		    newHandlerName = targetVariableName 
				   + oldHandlerName;						
		}
		newHandlerName = formEvents.findFreeHandlerName(newHandlerName);		
				
		String bodyText = null;
		if(sourceComp.getFormModel() != formModel) {
		    // copying to different form -> let's copy also the event handler content
		    JavaCodeGenerator javaCodeGenerator = 
			    ((JavaCodeGenerator)FormEditor.getCodeGenerator(sourceComp.getFormModel()));
		    bodyText = javaCodeGenerator.getEventHandlerText(oldHandlerName);
		}		
		
		try {		    		   
		    formEvents.attachEvent(targetEvent, newHandlerName, bodyText);
		}
		catch (IllegalArgumentException ex) {
		    // [incompatible handler error - should be reported!]
		    ex.printStackTrace();
		}
	    }
	}	
	
        return newComp;
    }

    // --------

    private RADComponent addVisualComponent(Class compClass,
                                            RADComponent targetComp,
                                            Object constraints)
    {
        RADVisualComponent newMetaComp = createVisualComponent(compClass);

//        Class beanClass = newMetaComp.getBeanClass();
        if (java.awt.Window.class.isAssignableFrom(compClass)
                || java.applet.Applet.class.isAssignableFrom(compClass))
            targetComp = null;

        return addVisualComponent2(newMetaComp, targetComp, constraints);
    }

    private RADVisualComponent createVisualComponent(Class compClass) {
        RADVisualComponent newMetaComp = null;
        RADVisualContainer newMetaCont =
            FormUtils.isContainer(compClass) ? new RADVisualContainer() : null;

        while (newMetaComp == null) {
            // initialize metacomponent and its bean instance
            newMetaComp = newMetaCont == null ?
                new RADVisualComponent() : newMetaCont;

            newMetaComp.initialize(formModel);
            if (!initComponentInstance(newMetaComp, compClass))
                return null; // failure (reported)

            if (newMetaCont == null)
                break; // not a container, the component is done

            // prepare layout support (the new component is a container)
            boolean knownLayout = false;
            Throwable layoutEx = null;
            try {
		newMetaCont.setOldLayoutSupport(true);
                LayoutSupportManager laysup = newMetaCont.getLayoutSupport();
                knownLayout = laysup.prepareLayoutDelegate(false, false);
                if ((knownLayout && !laysup.isDedicated() && formModel.isFreeDesignDefaultLayout())
                    || (!knownLayout && SwingLayoutBuilder.isRelevantContainer(laysup.getPrimaryContainerDelegate())))
                {   // general containers should use the new layout support when created
                    newMetaCont.setOldLayoutSupport(false);
                    FormEditor.getFormEditor(formModel).updateProjectForNaturalLayout();
                    knownLayout = true;
                }
            }
            catch (RuntimeException ex) { // silently ignore, try again as non-container
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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

            if (!knownLayout) {
                if (layoutEx == null) {
                    // no LayoutSupportDelegate found for the container
                    System.err.println("[WARNING] No layout support found for "+compClass.getName()); // NOI18N
                    System.err.println("          Just a limited basic support will be used."); // NOI18N
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

        newMetaComp.setStoredName(formModel.getCodeStructure().getExternalVariableName(compClass, null, false));

        // for some components, we initialize their properties with some
        // non-default values e.g. a label on buttons, checkboxes
        defaultVisualComponentInit(newMetaComp);

        // hack: automatically enclose some components into scroll pane
        // [PENDING check for undo/redo!]
        if (shouldEncloseByScrollPane(newMetaComp.getBeanInstance())) {
            RADVisualContainer metaScroll = (RADVisualContainer)
                createVisualComponent(JScrollPane.class);
            // Mark this scroll pane as automatically created.
            // Some action (e.g. delete) behave differently on
            // components in such scroll panes.
            metaScroll.setAuxValue("autoScrollPane", Boolean.TRUE); // NOI18N
            metaScroll.add(newMetaComp);
            Container scroll = (Container) metaScroll.getBeanInstance();
            Component inScroll = (Component) newMetaComp.getBeanInstance();
            metaScroll.getLayoutSupport().addComponentsToContainer(
                    scroll, scroll, new Component[] { inScroll }, 0);
            newMetaComp = metaScroll;
        }

        return newMetaComp;
    }

    private static boolean shouldEncloseByScrollPane(Object bean) {
        return (bean instanceof JList) || (bean instanceof JTable)
            || (bean instanceof JTree) || (bean instanceof JTextArea)
            || (bean instanceof JTextPane) || (bean instanceof JEditorPane);
    }

    private RADComponent addVisualComponent2(RADVisualComponent newMetaComp,
                                             RADComponent targetComp,
                                             Object constraints)
    {
        // Issue 65254: beware of nested JScrollPanes
        if ((targetComp != null) && JScrollPane.class.isAssignableFrom(targetComp.getBeanClass())) {
            Object bean = newMetaComp.getBeanInstance();
            if (bean instanceof JScrollPane) {
                if (newMetaComp.getAuxValue("autoScrollPane") != null) { // NOI18N
                    RADVisualContainer metaCont = (RADVisualContainer)newMetaComp;
                    newMetaComp = metaCont.getSubComponent(0);
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
            try {
                formModel.addVisualComponent(newMetaComp, parentCont, constraints, true);
            }
            catch (RuntimeException ex) {
                // LayoutSupportDelegate may not accept the component
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return null;
            }
        }
        else formModel.addComponent(newMetaComp, null, true);

        return newMetaComp;
    }
    
    private RADComponent addOtherComponent(Class compClass,
                                           RADComponent targetComp)
    {
        RADComponent newMetaComp = new RADComponent();
        newMetaComp.initialize(formModel);
        if (!initComponentInstance(newMetaComp, compClass))
            return null;

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

        formModel.addComponent(newMetaComp, targetCont, true);
    }

//    private RADComponent setContainerLayout(Class layoutClass,
//                                            RADComponent targetComp)
//    {
//        return setContainerLayout(layoutClass, null, targetComp);
//    }

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
                layoutDelegate = LayoutSupportRegistry.getRegistry(formModel)
                                     .createSupportForLayout(layoutClass);
            }
            else if (LayoutSupportDelegate.class.isAssignableFrom(layoutClass)) {
                // LayoutSupportDelegate -> use it directly
                layoutDelegate = LayoutSupportRegistry.getRegistry(formModel)
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

    private RADComponent setComponentBorder(Class borderClass,
                                            RADComponent targetComp)
    {
        FormProperty prop = getBorderProperty(targetComp);
        if (prop == null)
            return null;

        try { // set border property
            Object border = CreationFactory.createInstance(borderClass);
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

        FormDesigner designer = FormEditor.getFormDesigner(formModel);
        if (designer != null)
            designer.setSelectedComponent(targetComp);

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

        FormDesigner designer = FormEditor.getFormDesigner(formModel);
        if (designer != null)
            designer.setSelectedComponent(targetComp);
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

    private RADComponent addMenuComponent(Class compClass,
                                          RADComponent targetComp)
    {
        // create new metacomponent
        RADMenuComponent newMenuComp;
        RADMenuItemComponent newMenuItemComp;
        if ((RADMenuItemComponent.recognizeType(compClass)
                 & RADMenuItemComponent.MASK_CONTAINER) != 0) {
            newMenuComp = new RADMenuComponent();
            newMenuItemComp = newMenuComp;
        }
        else {
            newMenuComp = null;
            newMenuItemComp = new RADMenuItemComponent();
        }

        newMenuItemComp.initialize(formModel);
        if (!initComponentInstance(newMenuItemComp, compClass))
            return null;
        if (newMenuComp != null)
            newMenuComp.initSubComponents(new RADComponent[0]);

        // for some components, we initialize their properties with some
        // non-default values e.g. a label on buttons, checkboxes
        defaultMenuInit(newMenuItemComp);

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

        formModel.addComponent(newMenuComp, menuContainer, true);
    }

    // --------
    
    private Class prepareClass(final ClassSource classSource) {
        if (classSource.getCPRootCount() == 0) { // Just some optimization
            return prepareClass0(classSource);
        } else {
            try {
                return (Class)FormLAF.executeWithLookAndFeel(
                    new Mutex.ExceptionAction() {
                        public Object run() throws Exception {
                            FormLAF.setCustomizingUIClasses(true); // Issue 80198
                            try {
                                Class clazz = prepareClass0(classSource);
                                // Force creation of the default instance in the correct L&F context
                                BeanSupport.getDefaultInstance(clazz);
                                return clazz;
                            } finally {
                                FormLAF.setCustomizingUIClasses(false);
                            }
                        }
                    }
                );
            } catch (Exception ex) {
                // should not happen
                ex.printStackTrace();
                return null;
            }
        }
    }

    private Class prepareClass0(ClassSource classSource) {
        Throwable error = null;
        FileObject formFile = FormEditor.getFormDataObject(formModel).getFormFile();
        String className = classSource.getClassName();
        Class loadedClass = null;
        try {
            if (!ClassPathUtils.checkUserClass(className, formFile)) {
                ClassPathUtils.updateProject(formFile, classSource);
            }
            loadedClass = ClassPathUtils.loadClass(className, formFile);
        }
        catch (Exception ex) {
            error = ex;
        }
        catch (LinkageError ex) {
            error = ex;
        }

        if (loadedClass == null) {
            showClassLoadingErrorMessage(error, classSource);
        }
        
        return loadedClass;
    }

    private boolean checkFormClass(Class compClass) {
        if (formModel.getFormBaseClass().isAssignableFrom(compClass)) {
            
            String formClassBinaryName = getClassBinaryName(
                    FormEditor.getFormDataObject(formModel).getPrimaryFile());
            
            if (formClassBinaryName.equals(compClass.getName())) {
                DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                        FormUtils.getBundleString("MSG_ERR_CannotAddForm"), // NOI18N
                        NotifyDescriptor.WARNING_MESSAGE));
                return false;
            }
        }
        return true;
    }
    
    private static String getClassBinaryName(final FileObject fo) {
        final String[] result = new String[1];
        JavaSource js = JavaSource.forFileObject(fo);
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {
                public void cancel() {
                }
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    for (Tree t: controller.getCompilationUnit().getTypeDecls()) {
                        if (t.getKind() == Tree.Kind.CLASS &&
                                fo.getName().equals(((ClassTree) t).getSimpleName().toString())) {
                            TreePath classTreePath = controller.getTrees().getPath(controller.getCompilationUnit(), t);
                            Element classElm = controller.getTrees().getElement(classTreePath);
                            result[0] = classElm != null
                                    ? controller.getElements().getBinaryName((TypeElement) classElm).toString()
                                    : ""; // NOI18N
                            break;
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            Logger.getLogger(MetaComponentCreator.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return result[0];
    }

    private static void showClassLoadingErrorMessage(Throwable ex,
                                                     ClassSource classSource)
    {
        ErrorManager em = ErrorManager.getDefault();
        String msg = FormUtils.getFormattedBundleString(
            "FMT_ERR_CannotLoadClass4", // NOI18N
            new Object[] { classSource.getClassName(),
                           ClassPathUtils.getClassSourceDescription(classSource) });
        em.annotate(ex, msg);
        em.notify(ErrorManager.USER, ex); // Issue 65116 - don't show the exception to the user
        em.notify(ErrorManager.INFORMATIONAL, ex); // Make sure the exception is in the console and log file
    }

    private boolean initComponentInstance(RADComponent metacomp,
                                          Class compClass)
    {

        try {
            metacomp.initInstance(compClass);
        }
        catch (Exception ex) {
            showInstErrorMessage(ex);
            return false;
        }
        catch (LinkageError ex) {
            showInstErrorMessage(ex);
            return false;
        }
        return true;
    }

    private static void showInstErrorMessage(Throwable ex) {
        ErrorManager em = ErrorManager.getDefault();
        em.annotate(ex,
                    FormUtils.getBundleString("MSG_ERR_CannotInstantiate")); // NOI18N
        em.notify(ex);
    }

    // --------
    // default component initialization

    static void defaultVisualComponentInit(RADComponent radComp) {
        Object comp = radComp.getBeanInstance();
        String varName = radComp.getName();
        // Map of propertyNames -> propertyValues
        Map changes = new HashMap();
        String propName = null;
        Object propValue = null;

        if (comp instanceof AbstractButton) { // JButton, JToggleButton, JCheckBox, JRadioButton
            if ("".equals(((AbstractButton)comp).getText())) { // NOI18N
                propName = "text"; // NOI18N
                propValue = varName;
                changes.put(propName, propValue);
            }
            if (comp instanceof JCheckBox || comp instanceof JRadioButton) {
                if (((JToggleButton)comp).getBorder() instanceof javax.swing.plaf.UIResource) {
                    changes.put("border", BorderFactory.createEmptyBorder()); // NOI18N
                    changes.put("margin", new Insets(0, 0, 0, 0)); // NOI18N
                }
            }
        }
        else if (comp instanceof JLabel) {
            if ("".equals(((JLabel)comp).getText())) { // NOI18N
                propName = "text"; // NOI18N
                propValue = varName;
                changes.put(propName, propValue);
            }
        }
        else if (comp instanceof JTable) {
            javax.swing.table.TableModel tm = ((JTable)comp).getModel();
            if (tm == null
                || (tm instanceof javax.swing.table.DefaultTableModel
                    && tm.getRowCount() == 0 && tm.getColumnCount() == 0))
            {
                String prefix = NbBundle.getMessage(MetaComponentCreator.class, "FMT_CreatorTableTitle"); // NOI18N
                prefix += ' ';
                propValue =
                    new org.netbeans.modules.form.editors2.TableModelEditor.NbTableModel(
                        new javax.swing.table.DefaultTableModel(
                            new String[] {
                                prefix + 1, prefix + 2, prefix + 3, prefix + 4 },
                            4));
                propName = "model"; // NOI18N
                changes.put(propName, propValue);
            }
        }
        else if (comp instanceof JTextField) {
            if ("".equals(((JTextField)comp).getText())) { // NOI18N
                propName = "text"; // NOI18N
                propValue = varName;
                changes.put(propName, propValue);
            }
        }
        else if (comp instanceof JInternalFrame) {
            propName = "visible"; // NOI18N
            propValue = Boolean.TRUE;
            changes.put(propName, propValue);
        }
        else if (comp instanceof Button) {
            if ("".equals(((Button)comp).getLabel())) { // NOI18N
                propName = "label"; // NOI18N
                propValue = varName;
                changes.put(propName, propValue);
            }
        }
        else if (comp instanceof Checkbox) {
            if ("".equals(((Checkbox)comp).getLabel())) { // NOI18N
                propName = "label"; // NOI18N
                propValue = varName;
                changes.put(propName, propValue);
            }
        }
        else if (comp instanceof Label) {
            if ("".equals(((Label)comp).getText())) { // NOI18N
                propName = "text"; // NOI18N
                propValue = varName;
                changes.put(propName, propValue);
            }
        }
        else if (comp instanceof TextField) {
            if ("".equals(((TextField)comp).getText())) { // NOI18N
                propName = "text"; // NOI18N
                propValue = varName;
                changes.put(propName, propValue);
            }
        } else if (comp instanceof JComboBox) {
            ComboBoxModel model = ((JComboBox)comp).getModel();
            if ((model == null) || (model.getSize() == 0)) {
                String prefix = NbBundle.getMessage(MetaComponentCreator.class, "FMT_CreatorComboBoxItem"); // NOI18N
                prefix += ' ';
                propValue = new DefaultComboBoxModel(new String[] {
                    prefix + 1, prefix + 2, prefix + 3, prefix + 4
                });
                propName = "model"; // NOI18N
                changes.put(propName, propValue);
            }

        } else if (comp instanceof JList) {
            ListModel model = ((JList)comp).getModel();
            if ((model == null) || (model.getSize() == 0)) {
                String prefix = NbBundle.getMessage(MetaComponentCreator.class, "FMT_CreatorListItem"); // NOI18N
                prefix += ' ';
                DefaultListModel defaultModel = new DefaultListModel();
                for (int i=1; i<6; i++) {
                    defaultModel.addElement(prefix + i); // NOI18N
                }
                propValue = defaultModel;
                propName = "model"; // NOI18N
                changes.put(propName, propValue);
            }
        } else if (comp instanceof JTextArea) {
            JTextArea textArea = (JTextArea)comp;
            if (textArea.getRows() == 0) {
                propName = "rows"; // NOI18N
                propValue = new Integer(5);
                changes.put(propName, propValue);
            }
            if (textArea.getColumns() == 0) {
                propName = "columns"; // NOI18N
                propValue = new Integer(20);
                changes.put(propName, propValue);
            }
        }

        Iterator iter = changes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry change = (Map.Entry)iter.next();
            propName = (String)change.getKey();
            propValue = change.getValue();
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

    private Dimension prepareDefaultLayoutSize(Component comp, boolean isContainer) {
        int width = -1;
        int height = -1;
        if (comp instanceof JToolBar) {
            width = 100;
            height = 25;
        }
        else if (isContainer) {
            Dimension pref = comp.getPreferredSize();
            if (pref.width < 16 && pref.height < 12) {
                if (comp instanceof Window || comp instanceof java.applet.Applet) {
                    width = 400;
                    height = 300;
                }
                else {
                    width = 100;
                    height = 100;
                }
            }
            else {
                Dimension designerSize = FormEditor.getFormDesigner(formModel).getDesignerSize();
                if (pref.width > designerSize.width || pref.height > designerSize.height) {
                    width = Math.min(pref.width, designerSize.width - 25);
                    height = Math.min(pref.height, designerSize.height - 25);
                }
            }
        }
        else if (comp instanceof JSeparator) {
            width = 50;
            height = 10;
        }

        if (width < 0 || height < 0)
            return null;

        Dimension size = new Dimension(width, height);
        if (comp instanceof JComponent) {
            ((JComponent)comp).setPreferredSize(size);
        }
        return size;
    }
}
