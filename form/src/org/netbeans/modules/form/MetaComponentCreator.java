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
import javax.swing.border.Border;
import java.util.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
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
    private static final int TARGET_MENU = 3; // for AWT menus
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
                        return copyComponent2(sourceComp, null,
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

    public boolean moveComponent(RADComponent metacomp, RADComponent targetComp) {
        int targetPlacement = getTargetPlacement(metacomp.getBeanClass(), targetComp,
                                                 false, false);
        if (targetPlacement == NO_TARGET) {
            return false;
        }

        formModel.removeComponent(metacomp, false);
        return copyComponent2(metacomp, metacomp, targetComp, targetPlacement) != null;
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

    public RADVisualComponent precreateVisualComponent(ClassSource classSource) {
        final Class compClass = prepareClass(classSource);

        // no preview component if this is a window, applet, or not visual
        if (compClass == null
              || java.awt.Window.class.isAssignableFrom(compClass)
              || java.applet.Applet.class.isAssignableFrom(compClass)
              || (getTargetPlacement(compClass, null, true, false) != TARGET_VISUAL))
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

    public RADVisualComponent getPrecreatedMetaComponent() {
        return preMetaComp;
    }

    public LayoutComponent getPrecreatedLayoutComponent() {
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

    public boolean addPrecreatedComponent(RADComponent targetComp,
                                          Object constraints)
    {
        if (preMetaComp == null) {
            return false;
        }
        if (checkFormClass(preMetaComp.getBeanClass())) {
            int targetPlacement = getTargetPlacement(preMetaComp.getBeanClass(), targetComp, true, false);
            if (targetPlacement == TARGET_VISUAL) {
                addVisualComponent2(preMetaComp, targetComp, constraints, true);
                ResourceSupport.switchComponentToResources(preMetaComp);
            }
            releasePrecreatedComponent();
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

        if (targetPlacement == TARGET_MENU) {
            newMetaComp = addMenuComponent(compClass, targetComp);
        } else if (targetPlacement == TARGET_VISUAL) {
            newMetaComp = addVisualComponent(compClass, targetComp, constraints);
            if (java.awt.Window.class.isAssignableFrom(compClass) ||
                java.applet.Applet.class.isAssignableFrom(compClass)) {
                targetComp = null;
            }
        } else if (targetPlacement == TARGET_OTHER)
            newMetaComp = addOtherComponent(compClass, targetComp);

        if (newMetaComp instanceof RADVisualComponent
            && !((RADVisualComponent)newMetaComp).isMenuComponent()
            && (shouldBeLayoutContainer(targetComp)
                || (shouldBeLayoutContainer(newMetaComp))))
        {   // container with new layout...
            createAndAddLayoutComponent((RADVisualComponent)newMetaComp,
                                        (RADVisualContainer)targetComp);
        }

        if (newMetaComp != null) {
            ResourceSupport.switchComponentToResources(newMetaComp);
        }

        return newMetaComp;
    }
    
    private void createAndAddLayoutComponent(RADVisualComponent radComp, RADVisualContainer targetCont) {
        LayoutComponent layoutComp = createLayoutComponent(radComp);
        String targetContId = shouldBeLayoutContainer(targetCont) ? targetCont.getId() : null;

        javax.swing.undo.UndoableEdit ue = formModel.getLayoutModel().getUndoableEdit();
        boolean autoUndo = true;
        try {
            FormEditor.getFormDesigner(formModel).getLayoutDesigner()
                    .addUnspecifiedComponent(layoutComp, targetContId);
            autoUndo = false;
        } finally {
            formModel.addUndoableEdit(ue);
            if (autoUndo) {
                formModel.forceUndoOfCompoundEdit();
            }
        }
    }

    private RADComponent copyComponent2(RADComponent sourceComp,
                                        RADComponent copiedComp,
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

        boolean newlyAdded;
        if (copiedComp == null) { // copy the source metacomponent
            copiedComp = makeCopy(sourceComp);
            if (copiedComp == null) { // copying failed (for a mystic reason)
                return null;
            }
            ResourceSupport.switchComponentToResources(copiedComp);
            newlyAdded = true;
        } else {
            newlyAdded = false;
        }

        if (targetPlacement == TARGET_MENU) {
            addMenuComponent(copiedComp, targetComp, newlyAdded);
        }
        else if (targetPlacement == TARGET_VISUAL) {
            RADVisualComponent newVisual = (RADVisualComponent) copiedComp;
            Object constraints;
            if (targetComp != null) {
                RADVisualContainer targetCont = (RADVisualContainer)targetComp;
                LayoutSupportManager layoutSupport = targetCont.getLayoutSupport();
                if (layoutSupport == null) {
                    constraints = null;
                } else {
                    constraints = layoutSupport.getStoredConstraints(newVisual);
                }
            }
            else constraints = null;

            copiedComp = addVisualComponent2(newVisual, targetComp, constraints, newlyAdded);
            // might be null if layout support did not accept the component
        }
        else if (targetPlacement == TARGET_OTHER) {
            addOtherComponent(copiedComp, targetComp, newlyAdded);
        }

        return copiedComp;
    }

    /**
     * This method is responsible for decision whether a bean can be added to
     * (or applied on) given target component in FormModel. It returns a constant
     * representing the target operation and type of metacomponent to be created.
     * Determining the target placement can be used in two modes. It is more
     * strict for copy/cut/paste operations (paramaters canUseParent and
     * defaultToOthers are set to false), and less strict for visual (drag&drop)
     * operations (canUseParent and defaultToOthers are set to true).
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

            RADVisualContainer targetCont = getVisualContainer(targetComp, canUseParent);
            return targetCont != null && !targetCont.hasDedicatedLayoutSupport() ?
                TARGET_LAYOUT : NO_TARGET;
        }

        if (Border.class.isAssignableFrom(beanClass)) { // border
            if (targetComp == null)
                return TARGET_OTHER;

            return targetComp instanceof RADVisualComponent ?
                TARGET_BORDER : NO_TARGET;
        }

        if (MenuComponent.class.isAssignableFrom(beanClass)
                || Separator.class.isAssignableFrom(beanClass))
        { // AWT menu
            if (targetComp == null)
                return TARGET_MENU;

            if (targetComp instanceof RADMenuComponent) {
                // adding to a menu
                return ((RADMenuComponent)targetComp).canAddItem(beanClass) ?
                    TARGET_MENU : NO_TARGET;
            } else { // adding to a visual container?
                RADVisualContainer targetCont = getVisualContainer(targetComp, canUseParent);
                while (targetCont != null) {
                    if (targetCont.getContainerMenu() != null) {
                        return NO_TARGET; // already has a menubar
                    } else if (targetCont.canHaveMenu(beanClass)) {
                        return TARGET_MENU;
                    } else if (canUseParent) {
                        targetCont = targetCont.getParentContainer();
                    } else {
                        targetCont = null;
                    }
                }
                return defaultToOthers && !Separator.class.isAssignableFrom(beanClass)
                        ? TARGET_MENU : NO_TARGET;
            }
        }

        if (FormUtils.isVisualizableClass(beanClass)) {
            // visual component
            if (targetComp == null) {
                return TARGET_VISUAL;
            }

            if (java.awt.Window.class.isAssignableFrom(beanClass)
                    || java.applet.Applet.class.isAssignableFrom(beanClass)
                    || !java.awt.Component.class.isAssignableFrom(beanClass)) { // visualized component without parent
                return defaultToOthers ? TARGET_VISUAL : NO_TARGET;
            }

            RADVisualContainer targetCont = getVisualContainer(targetComp, canUseParent);
            if (targetCont != null && targetCont.canAddComponent(beanClass)) {
                return TARGET_VISUAL;
            }
        }

        if (targetComp == null || defaultToOthers) {
            return TARGET_OTHER;
        }

        return NO_TARGET;
    }

    private static RADVisualContainer getVisualContainer(RADComponent targetComp, boolean canUseParent) {
        if (targetComp instanceof RADVisualContainer) {
            return (RADVisualContainer) targetComp;
        } else if (canUseParent && targetComp instanceof RADVisualComponent) {
            return (RADVisualContainer) targetComp.getParentComponent();
        } else {
            return null;
        }
    }

    static boolean isTransparentLayoutComponent(RADComponent metacomp) {
        return metacomp != null
               && metacomp.getBeanClass() == JScrollPane.class
               && metacomp.getAuxValue("autoScrollPane") != null; // NOI18N
    }

    // ---------

    private RADComponent makeCopy(RADComponent sourceComp/*, int targetPlacement*/) {
        RADComponent newComp;

        if (sourceComp instanceof RADVisualContainer) {
            newComp = new RADVisualContainer();
        } else if (sourceComp instanceof RADVisualComponent) {
            newComp = new RADVisualComponent();
        } else if (sourceComp instanceof RADMenuComponent) {
            newComp = new RADMenuComponent();
        } else if (sourceComp instanceof RADMenuItemComponent) {
            newComp = new RADMenuItemComponent();
        } else {
            newComp = new RADComponent();
        }

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
                RADComponent newSubComp = makeCopy(sourceSubs[i]);
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
                    formModel.getLayoutModel().copyContainerLayout(sourceLayoutModel,
                            sourceContainerId, sourceToTargetIds, targetContainerId);
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

        // hack for AWT menus - to update their Swing design parallels
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

        return addVisualComponent2(newMetaComp, targetComp, constraints, true);
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
                    FormEditor.updateProjectForNaturalLayout(formModel);
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
        return (RADVisualComponent) defaultVisualComponentInit(newMetaComp);
    }

    private RADVisualComponent addVisualComponent2(RADVisualComponent newMetaComp,
                                                   RADComponent targetComp,
                                                   Object constraints,
                                                   boolean newlyAdded)
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
        
        defaultTargetInit(newMetaComp, parentCont);

        // add the new metacomponent to the model
        if (parentCont != null) {
            try {
                formModel.addVisualComponent(newMetaComp, parentCont, constraints, newlyAdded);
            }
            catch (RuntimeException ex) {
                // LayoutSupportDelegate may not accept the component
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return null;
            }
        }
        else formModel.addComponent(newMetaComp, null, newlyAdded);

        return newMetaComp;
    }
    
    private RADComponent addOtherComponent(Class compClass,
                                           RADComponent targetComp)
    {
        RADComponent newMetaComp = new RADComponent();
        newMetaComp.initialize(formModel);
        if (!initComponentInstance(newMetaComp, compClass))
            return null;

        addOtherComponent(newMetaComp, targetComp, true);
        return newMetaComp;
    }

    private void addOtherComponent(RADComponent newMetaComp,
                                   RADComponent targetComp,
                                   boolean newlyAdded)
    {
        ComponentContainer targetCont = 
            targetComp instanceof ComponentContainer
                && !(targetComp instanceof RADVisualContainer)
                && !(targetComp instanceof RADMenuComponent) ?
            (ComponentContainer) targetComp : null;

        formModel.addComponent(newMetaComp, targetCont, newlyAdded);
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

        // set some initial label
        if (newMenuItemComp.getBeanInstance() instanceof MenuItem) {
            MenuItem menu = (MenuItem) newMenuItemComp.getBeanInstance();
            if ("".equals(menu.getLabel())) { // NOI18N
                String label;
                if (menu instanceof PopupMenu) {
                    label = FormUtils.getBundleString("FMT_LAB_PopupMenu"); // NOI18N
                } else if (menu instanceof Menu) {
                    label = FormUtils.getBundleString("FMT_LAB_Menu"); // NOI18N
                } else if (menu instanceof CheckboxMenuItem) {
                    label = FormUtils.getBundleString("FMT_LAB_CheckboxMenuItem"); // NOI18N
                } else {
                    label = FormUtils.getBundleString("FMT_LAB_MenuItem"); // NOI18N
                }
                RADProperty prop = newMenuItemComp.getBeanProperty("label"); // NOI18N
                try {
                    prop.setChangeFiring(false);
                    prop.setValue(label);
                    prop.setChangeFiring(true);
                } catch (Exception e) { // never mind, ignore
                }
            }
        }

        addMenuComponent(newMenuItemComp, targetComp, true);

        // for added new menu bar we add one menu so it is not empty
        if (newMenuComp != null) {
            int type = newMenuComp.getMenuItemType();
            if (type == RADMenuItemComponent.T_MENUBAR) {
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
                                  RADComponent targetComp,
                                  boolean newlyAdded)
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

        formModel.addComponent(newMenuComp, menuContainer, newlyAdded);
    }

    // --------
    
    Class prepareClass(final ClassSource classSource) {
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
                                if (clazz != null) {
                                    // Force creation of the default instance in the correct L&F context
                                    BeanSupport.getDefaultInstance(clazz);
                                }
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

    private RADComponent defaultVisualComponentInit(RADVisualComponent newMetaComp) {
        Object comp = newMetaComp.getBeanInstance();
        String varName = newMetaComp.getName();
        // Map of propertyNames -> propertyValues
        Map changes = new HashMap();

        if (comp instanceof JLabel) {
            if ("".equals(((JLabel)comp).getText())) { // NOI18N
                changes.put("text", varName); // NOI18N
            }
        } else if (comp instanceof JTextField) {
            if ("".equals(((JTextField)comp).getText())) { // NOI18N
                changes.put("text", varName); // NOI18N
            }
        } else if (comp instanceof JMenuItem) {
            if ("".equals(((JMenuItem)comp).getText())) { // NOI18N
                String value;
                if (comp instanceof JCheckBoxMenuItem) {
                    value = FormUtils.getBundleString("FMT_LAB_JCheckBoxMenuItem"); // NOI18N
                } else if (comp instanceof JMenu) {
                    value = FormUtils.getBundleString("FMT_LAB_JMenu"); // NOI18N
                } else if (comp instanceof JRadioButtonMenuItem) {
                    value = FormUtils.getBundleString("FMT_LAB_JRadioButtonMenuItem"); // NOI18N
                } else {
                    value = FormUtils.getBundleString("FMT_LAB_JMenuItem"); // NOI18N
                }
                changes.put("text", value); // NOI18N
            }
            if(comp instanceof JCheckBoxMenuItem) {
                changes.put("selected", new Boolean(true)); // NOI18N
            }
            if(comp instanceof JRadioButtonMenuItem) {
                changes.put("selected", new Boolean(true)); // NOI18N
            }
        } else if (comp instanceof AbstractButton) { // JButton, JToggleButton, JCheckBox, JRadioButton
            if ("".equals(((AbstractButton)comp).getText())) { // NOI18N
                changes.put("text", varName); // NOI18N
            }
            if (comp instanceof JCheckBox || comp instanceof JRadioButton) {
                if (((JToggleButton)comp).getBorder() instanceof javax.swing.plaf.UIResource) {
                    changes.put("border", BorderFactory.createEmptyBorder()); // NOI18N
                    changes.put("margin", new Insets(0, 0, 0, 0)); // NOI18N
                }
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
                Object propValue =
                    new org.netbeans.modules.form.editors2.TableModelEditor.NbTableModel(
                        new javax.swing.table.DefaultTableModel(
                            new String[] {
                                prefix + 1, prefix + 2, prefix + 3, prefix + 4 },
                            4));
                changes.put("model", propValue); // NOI18N
            }
        }
        else if (comp instanceof JToolBar) {
            changes.put("rollover", true); // NOI18N
        }
        else if (comp instanceof JInternalFrame) {
            changes.put("visible", true); // NOI18N
        }
        else if (comp instanceof Button) {
            if ("".equals(((Button)comp).getLabel())) { // NOI18N
                changes.put("label", varName); // NOI18N
            }
        }
        else if (comp instanceof Checkbox) {
            if ("".equals(((Checkbox)comp).getLabel())) { // NOI18N
                changes.put("label", varName); // NOI18N
            }
        }
        else if (comp instanceof Label) {
            if ("".equals(((Label)comp).getText())) { // NOI18N
                changes.put("text", varName); // NOI18N
            }
        }
        else if (comp instanceof TextField) {
            if ("".equals(((TextField)comp).getText())) { // NOI18N
                changes.put("text", varName); // NOI18N
            }
        } else if (comp instanceof JComboBox) {
            ComboBoxModel model = ((JComboBox)comp).getModel();
            if ((model == null) || (model.getSize() == 0)) {
                String prefix = NbBundle.getMessage(MetaComponentCreator.class, "FMT_CreatorComboBoxItem"); // NOI18N
                prefix += ' ';
                Object propValue = new DefaultComboBoxModel(new String[] {
                    prefix + 1, prefix + 2, prefix + 3, prefix + 4
                });
                changes.put("model", propValue); // NOI18N
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
                changes.put("model", defaultModel); // NOI18N
            }
        } else if (comp instanceof JTextArea) {
            JTextArea textArea = (JTextArea)comp;
            if (textArea.getRows() == 0) {
                changes.put("rows", new Integer(5)); // NOI18N
            }
            if (textArea.getColumns() == 0) {
                changes.put("columns", new Integer(20)); // NOI18N
            }
        }

        Iterator iter = changes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry change = (Map.Entry)iter.next();
            String propName = (String)change.getKey();
            Object propValue = change.getValue();
            FormProperty prop = newMetaComp.getBeanProperty(propName);
            if (prop != null) {
                try {
                    prop.setChangeFiring(false);
                    prop.setValue(propValue);
                    prop.setChangeFiring(true);
                }
                catch (Exception e) {} // never mind, ignore
            }
        }

        // more initial modifications...
        if (shouldEncloseByScrollPane(newMetaComp.getBeanInstance())) {
            // hack: automatically enclose some components into scroll pane
            // [PENDING check for undo/redo!]
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
        } else if (newMetaComp instanceof RADVisualContainer && newMetaComp.getBeanInstance() instanceof JMenuBar) {
            // for menubars create initial menu [temporary?]
            RADVisualContainer menuCont = (RADVisualContainer) newMetaComp;
            Container menuBar = (Container) menuCont.getBeanInstance();
            RADVisualComponent menuComp = createVisualComponent(JMenu.class);
            try {
                menuComp.getBeanProperty("text") // NOI18N
                        .setValue(FormUtils.getBundleString("CTL_DefaultFileMenu")); // NOI18N
            } catch (Exception ex) { }
            Component menu = (Component) menuComp.getBeanInstance();
            menuCont.add(menuComp);
            menuCont.getLayoutSupport().addComponentsToContainer(
                    menuBar, menuBar, new Component[] { menu }, 0);

            menuComp = createVisualComponent(JMenu.class);
            try {
                menuComp.getBeanProperty("text") // NOI18N
                        .setValue(FormUtils.getBundleString("CTL_DefaultEditMenu")); // NOI18N
            } catch (Exception ex) { }
            menu = (Component) menuComp.getBeanInstance();
            menuCont.add(menuComp);
            menuCont.getLayoutSupport().addComponentsToContainer(
                    menuBar, menuBar, new Component[] { menu }, 0);
        }

        return newMetaComp;
    }

    private static boolean shouldEncloseByScrollPane(Object bean) {
        return (bean instanceof JList) || (bean instanceof JTable)
            || (bean instanceof JTree) || (bean instanceof JTextArea)
            || (bean instanceof JTextPane) || (bean instanceof JEditorPane);
    }

    /**
     * Initial setting for components that can't be done until knowing where
     * they are to be added to (type of target container). E.g. button
     * properties are adjusted when added to a toolbar.
     */
    private static void defaultTargetInit(RADComponent metacomp, RADComponent target) {
        Object targetComp = target != null ? target.getBeanInstance() : null;

        if (metacomp.getBeanClass().equals(JSeparator.class) && targetComp instanceof JToolBar) {
            // hack: change JSeparator to JToolBar.Separator
            try {
                metacomp.initInstance(JToolBar.Separator.class);
            } catch (Exception ex) {} // should not fail with JDK class
            return;
        }

        Object comp = metacomp.getBeanInstance();
        Map<String, Object> changes = null;

        if (comp instanceof AbstractButton && targetComp instanceof JToolBar) {
            if (changes == null) {
                changes = new HashMap<String, Object>();
            }
            changes.put("focusable", false); // NOI18N
            changes.put("horizontalTextPosition", SwingConstants.CENTER); // NOI18N
            changes.put("verticalTextPosition", SwingConstants.BOTTOM); // NOI18N
        }

        if (changes != null) {
            for (Map.Entry<String, Object> e : changes.entrySet()) {
                FormProperty prop = metacomp.getBeanProperty(e.getKey());
                if (prop != null) {
                    try {
                        prop.setChangeFiring(false);
                        prop.setValue(e.getValue());
                        prop.setChangeFiring(true);
                    }
                    catch (Exception ex) {} // never mind, ignore
                }
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
