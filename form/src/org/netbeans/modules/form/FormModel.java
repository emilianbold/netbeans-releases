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

import java.util.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.*;
import org.netbeans.modules.form.layoutsupport.*;

import org.netbeans.modules.form.codestructure.CodeStructure;

/**
 * Holds all data of a form.
 * 
 * @author Tran Duc Trung, Tomas Pavek
 */

public class FormModel
{
    // the top metacomponent of the form (null if form is based on Object)
    private RADComponent topRADComponent;

    // the class on which the form is based (which is extended in the java file)
    private Class formBaseClass;

    // other components - out of the main hierarchy under topRADComponent
    private ArrayList otherComponents = new ArrayList(10);

    // holds both topRADComponent and otherComponents
    private ComponentContainer modelContainer;

    // name of the form (name of the DataObject)
    private String formName;

    private boolean readOnly = false;
    private boolean formLoaded = false;

    private boolean undoRedoRecording = false;
    private CompoundEdit compoundEdit;

    private FormEventHandlers eventHandlers;

    // list of listeners registered on FormModel
    private final EventListenerList listenerList = new EventListenerList();

    private MetaComponentCreator metaCreator;

    private CodeStructure codeStructure = new CodeStructure(false);
    private CodeGenerator codeGenerator; // [this reference should be removed]

    // -------------
    // initialization

    FormModel() {
    }

    /** This methods sets the form base class (which is in fact the superclass
     * of the form class in source java file). It is used for initializing
     * the top meta component, and is also presented as the top component
     * in designer and inspector.
     */
    public void setFormBaseClass(Class formClass) throws Exception {
        if (formBaseClass != null)
            throw new IllegalStateException("Form type already initialized."); // NOI18N

        RADComponent topComp;
        if (java.awt.Component.class.isAssignableFrom(formClass)) {
            topComp = FormUtils.isContainer(formClass) ?
                          new RADVisualFormContainer() :
                          new RADVisualComponent();
        }
        else if (java.lang.Object.class != formClass)
            topComp = new RADFormContainer();
        else topComp = null;

        if (topComp != null) {
            topRADComponent = topComp;
            topComp.initialize(this);
            topComp.initInstance(formClass);
            topComp.setInModel(true);
        }

        formBaseClass = formClass;
//        topRADComponent = topComp;
    }

    public Class getFormBaseClass() {
        return formBaseClass;
    }

    void setName(String name) {
        formName = name;
    }

    void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    // -----------
    // getters

    public final String getName() {
        return formName;
    }

    public final boolean isReadOnly() {
        return readOnly;
    }

    public final boolean isFormLoaded() {
        return formLoaded;
    }

    public final FormDesigner getFormDesigner() {
        return FormEditorSupport.getFormDesigner(this);
    }

    // for compatibility with previous version
    public final FormDataObject getFormDataObject() {
        return FormEditorSupport.getFormDataObject(this);
    }

    public ComponentContainer getModelContainer() {
        if (modelContainer == null)
            modelContainer = new ModelContainer();
        return modelContainer;
    }

    public final RADComponent getTopRADComponent() {
        return topRADComponent;
    }

    public RADComponent findRADComponent(String name) {
        Iterator allComps = getMetaComponents().iterator();
        while (allComps.hasNext()) {
            RADComponent comp = (RADComponent) allComps.next();
            if (name.equals(comp.getName()))
                return comp;
        }

        return null;
    }

    /** Returns all meta components in the model. The components are collected
     * recursively, and placed in a List.
     */
    public java.util.List getMetaComponents() {
        ArrayList list = new ArrayList();
        collectMetaComponents(getModelContainer(), list);
        return list; //Collections.unmodifiableList(list);
    }

    /** Collects and returns all components in the main visual hierarchy.
     */
    public RADVisualComponent[] getVisualComponents() {
        ArrayList list = new ArrayList();
        if (topRADComponent instanceof RADVisualComponent)
            list.add(topRADComponent);
        if (topRADComponent instanceof RADVisualContainer)
            collectVisualMetaComponents((RADVisualContainer)topRADComponent, list);

        return (RADVisualComponent[])
               list.toArray(new RADVisualComponent[list.size()]);
    }

    /** Returns all "other components" (not in the main hierarchy).
     * @param recursively whether also all sub-componets should be collected
     */
    public RADComponent[] getOtherComponents(boolean recursively) {
        ArrayList list = new ArrayList();
        for (Iterator it=otherComponents.iterator(); it.hasNext(); ) {
            RADComponent comp = (RADComponent) it.next();
            list.add(comp);
            if (recursively && comp instanceof ComponentContainer)
                collectMetaComponents((ComponentContainer) comp, list);
        }

        return (RADComponent[]) list.toArray(new RADComponent[list.size()]);
    }

    // for compatibility with previous version
    public RADComponent[] getNonVisualComponents() {
        return (RADComponent[]) otherComponents.toArray(
                                new RADComponent[otherComponents.size()]); 
    }

    private static void collectMetaComponents(ComponentContainer cont,
                                              java.util.List list) {
        RADComponent[] comps = cont.getSubBeans();
        for (int i = 0; i < comps.length; i++) {
            RADComponent comp = comps[i];
            list.add(comp);
            if (comp instanceof ComponentContainer)
                collectMetaComponents((ComponentContainer) comp, list);
        }
    }

    private static void collectVisualMetaComponents(RADVisualContainer cont,
                                                    java.util.List list) {
        RADVisualComponent[] comps = cont.getSubComponents();
        for (int i = 0; i < comps.length; i++) {
            RADComponent comp = comps[i];
            list.add(comp);
            if (comp instanceof RADVisualContainer)
                collectVisualMetaComponents((RADVisualContainer) comp, list);
        }
    }

    // -----------
    // adding/deleting components, setting layout, etc

    /** Returns MetaComponentCreator which is responsible for creating new
     * components and adding them to the model.
     */
    public MetaComponentCreator getComponentCreator() {
        if (metaCreator == null)
            metaCreator = new MetaComponentCreator(this);
        return metaCreator;
    }

    public void addComponent(RADComponent metacomp,
                             ComponentContainer parentContainer)
    {
        if (parentContainer != null) {
            parentContainer.add(metacomp);
        }
        else {
            metacomp.setParentComponent(null);
            otherComponents.add(metacomp);
        }

        boolean newlyAdded = !metacomp.isInModel();
        if (newlyAdded)
            setInModelRecursively(metacomp, true);

        fireComponentAdded(metacomp, newlyAdded);
    }

    public void addVisualComponent(RADVisualComponent metacomp,
                                   RADVisualContainer parentContainer,
                                   LayoutConstraints constraints)
    {
        LayoutSupportManager layoutSupport = parentContainer.getLayoutSupport();
        RADVisualComponent[] compArray = new RADVisualComponent[] { metacomp };
        LayoutConstraints[] constrArray = new LayoutConstraints[] { constraints };

        // this may throw a RuntimeException if the components are not accepted
        layoutSupport.acceptNewComponents(compArray, constrArray);

        parentContainer.add(metacomp);

        layoutSupport.addComponents(compArray, constrArray);

        boolean newlyAdded = !metacomp.isInModel();
        if (newlyAdded)
            setInModelRecursively(metacomp, true);

        fireComponentAdded(metacomp, newlyAdded);
    }

    public void setContainerLayout(RADVisualContainer metacont,
                                   LayoutSupportDelegate layoutDelegate,
                                   java.awt.LayoutManager lmInstance)
        throws Exception
    {
        LayoutSupportDelegate current =
            metacont.getLayoutSupport().getLayoutDelegate();

        metacont.setLayoutSupportDelegate(layoutDelegate, lmInstance);

        fireContainerLayoutExchanged(metacont, current, layoutDelegate);
    }

    public void removeComponentFromContainer(RADComponent metacomp) {
        RADComponent parent = metacomp.getParentComponent();
        ComponentContainer parentContainer =
            parent instanceof ComponentContainer ?
                (ComponentContainer) parent : getModelContainer();

        int index = parentContainer.getIndexOf(metacomp);
        parentContainer.remove(metacomp);

        fireComponentRemoved(metacomp, parentContainer, index, false,
                             null, null);
    }

    public void removeComponent(RADComponent metacomp) {
        boolean compoundUndoableEditStarted;
        if (eventHandlers != null) {
            compoundUndoableEditStarted = undoRedoRecording
                                          && startCompoundEdit();
            removeEventHandlersRecursively(metacomp);
        }
        else compoundUndoableEditStarted = false;

        RADComponent parent = metacomp.getParentComponent();
        ComponentContainer parentContainer =
            parent instanceof ComponentContainer ?
                (ComponentContainer) parent : getModelContainer();

        int index = parentContainer.getIndexOf(metacomp);
        parentContainer.remove(metacomp);

        // turn on undo/redo recording on code structure (if allowed)
        Object codeStructureMark1 = null, codeStructureMark2 = null;
        boolean codeStructureUndoRedo = codeStructure.isUndoRedoRecording();
        if (undoRedoRecording && !codeStructureUndoRedo) {
            codeStructure.setUndoRedoRecording(true);
            codeStructureMark1 = codeStructure.markForUndo();
        }

        metacomp.removeCodeExpression();
        metacomp.setInModel(false);
        if (metacomp instanceof ComponentContainer)
            releaseComponent(metacomp);

        // turn off undo/redo recording on code structure (if turned on)
        if (undoRedoRecording && !codeStructureUndoRedo) {
            codeStructureMark2 = codeStructure.markForUndo();
            if (codeStructureMark2.equals(codeStructureMark1))
                codeStructureMark2 = codeStructureMark1 = null;

            codeStructure.setUndoRedoRecording(false);
        }

        fireComponentRemoved(metacomp, parentContainer, index, true,
                             codeStructureMark1, codeStructureMark2);

        if (compoundUndoableEditStarted)
            endCompoundEdit();
    }

    // removes all event handlers attached to given component and all
    // its subcomponents
    private void removeEventHandlersRecursively(RADComponent comp) {
        if (comp instanceof ComponentContainer) {
            RADComponent[] subcomps = ((ComponentContainer)comp).getSubBeans();
            for (int i=0; i<subcomps.length; i++)
                removeEventHandlersRecursively(subcomps[i]);
        }

        EventSet[] eventSets = comp.getEventHandlers().getEventSets();
        for (int i = 0; i < eventSets.length; i++) {
            Event[] events = eventSets[i].getEvents();
            for (int j = 0; j < events.length; j++) {
                if (events[j].getHandlers().size() > 0) {
                    eventHandlers.removeEventHandler(events[j]);
                }
            }
        }
    }

    private static void releaseComponent(RADComponent metacomp) {
        RADComponent[] comps = ((ComponentContainer)metacomp).getSubBeans();
        for (int i=0, n=comps.length; i < n; i++) {
            metacomp = comps[i];
            metacomp.releaseCodeExpression();
            metacomp.setInModel(false);
            if (metacomp instanceof ComponentContainer)
                releaseComponent(metacomp);
        }
    }

    static void setInModelRecursively(RADComponent metacomp, boolean inModel) {
        metacomp.setInModel(inModel);
        if (metacomp instanceof ComponentContainer) {
            RADComponent[] comps = ((ComponentContainer)metacomp).getSubBeans();
            for (int i=0; i < comps.length; i++)
                setInModelRecursively(comps[i], inModel);
        }
    }

    // ----------
    // undo and redo

    public void setUndoRedoRecording(boolean record) {
        t("turning undo/redo recording "+(record?"on":"off")); // NOI18N
        undoRedoRecording = record;
    }

    public boolean isUndoRedoRecording() {
        return undoRedoRecording;
    }

    public boolean startCompoundEdit() {
        if (compoundEdit == null) {
            t("starting compound edit"); // NOI18N
            compoundEdit = new CompoundEdit();
            return true;
        }
        return false;
    }

    public CompoundEdit endCompoundEdit() {
        if (compoundEdit != null) {
            t("ending compound edit"); // NOI18N
            compoundEdit.end();
            if (undoRedoRecording && compoundEdit.isSignificant())
                FormEditorSupport.getFormUndoManager(this)
                                    .addEdit(compoundEdit);
            CompoundEdit edit = compoundEdit;
            compoundEdit = null;
            return edit;
        }
        return null;
    }

    public boolean isCompoundEditInProgress() {
        return compoundEdit != null && compoundEdit.isInProgress();
    }

    public void addUndoableEdit(UndoableEdit edit) {
        t("adding undoable edit"); // NOI18N
        if (isCompoundEditInProgress())
            compoundEdit.addEdit(edit);
        else
            FormEditorSupport.getFormUndoManager(this)
                .undoableEditHappened(new UndoableEditEvent(this, edit));
    }

    // ----------
    // listeners registration, firing methods

    public void addFormModelListener(FormModelListener l) {
        listenerList.add(FormModelListener.class, l);
    }

    public void removeFormModelListener(FormModelListener l) {
        listenerList.remove(FormModelListener.class, l);
    }

    /** Fires an event informing about general form change. */
    public void fireFormChanged() {
        t("firing form change"); // NOI18N

        FormModelEvent e = new FormModelEvent(this);
        e.setChangeType(FormModelEvent.FORM_CHANGED);
        
  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
  		((FormModelListener)listeners[i+1]).formChanged(e);
  	    }
  	}
    }

    /** Fires an event informing about that the form has been just loaded. */
    public void fireFormLoaded() {
        t("firing form loaded"); // NOI18N
        formLoaded = true;
        if (!readOnly && !Boolean.getBoolean("netbeans.form.no_undo")) // NOI18N
            setUndoRedoRecording(true);
        initializeCodeGenerator();

        FormModelEvent e = new FormModelEvent(this);
        e.setChangeType(FormModelEvent.FORM_LOADED);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
  		((FormModelListener)listeners[i+1]).formLoaded(e);
  	    }
  	}
    }

    /** Fires an event informing about that the form is just about to be saved. */
    public void fireFormToBeSaved() {
        t("firing form to be saved"); // NOI18N

        FormModelEvent e = new FormModelEvent(this);
        e.setChangeType(FormModelEvent.FORM_TO_BE_SAVED);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
  		((FormModelListener)listeners[i+1]).formToBeSaved(e);
  	    }
  	}
    }

    /** Fires an event informing about that the form is just about to be closed. */
//    public void fireFormToBeClosed() {
//        t("firing form to be closed fired"); // NOI18N
//
//        FormModelEvent e = new FormModelEvent(this);
//        e.setChangeType(FormModelEvent.FORM_TO_BE_CLOSED);
//
//  	Object[] listeners = listenerList.getListenerList();
//  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
//  	    if (listeners[i] == FormModelListener.class) {
//  		((FormModelListener)listeners[i+1]).formToBeClosed(e);
//  	    }
//  	}
//    }

    /** Fires an event informing about changing layout manager of a container.
     * An undoable edit is created and registered automatically. */
    public void fireContainerLayoutExchanged(RADVisualContainer metacont,
                                             LayoutSupportDelegate oldLayout,
                                             LayoutSupportDelegate newLayout)
    {
        t("firing container layout exchange, container: " // NOI18N
          + (metacont != null ? metacont.getName() : "null")); // NOI18N

        FormModelEvent e = new FormModelEvent(this);
        e.setLayout(metacont, oldLayout, newLayout);
        e.setChangeType(FormModelEvent.CONTAINER_LAYOUT_EXCHANGED);

        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
                l.containerLayoutExchanged(e);
                l.formChanged(e);
            }
        }

        if (undoRedoRecording && metacont != null && oldLayout != newLayout)
            addUndoableEdit(e.getUndoableEdit());
    }

    /** Fires an event informing about changing a property of container layout.
     * An undoable edit is created and registered automatically. */
    public void fireContainerLayoutChanged(RADVisualContainer metacont,
                                           String propName,
                                           Object oldValue,
                                           Object newValue)
    {
        t("firing container layout change, container: " // NOI18N
          + (metacont != null ? metacont.getName() : "null") // NOI18N
          + ", property: " + propName); // NOI18N

        FormModelEvent e = new FormModelEvent(this);
        e.setComponentAndContainer(metacont, metacont);
        e.setProperty(propName, oldValue, newValue);
        e.setChangeType(FormModelEvent.CONTAINER_LAYOUT_CHANGED);

        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
                l.containerLayoutChanged(e);
                l.formChanged(e);
            }
        }

        if (undoRedoRecording
            && metacont != null && propName != null && oldValue != newValue)
        {
            addUndoableEdit(e.getUndoableEdit());
        }
    }

    /** Fires an event informing about changing a property of component layout
     * constraints. An undoable edit is created and registered automatically. */
    public void fireComponentLayoutChanged(RADVisualComponent metacomp,
                                           String propName,
                                           Object oldValue,
                                           Object newValue)
    {
        t("firing component layout change: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null")); // NOI18N

        FormModelEvent e = new FormModelEvent(this);
        e.setComponentAndContainer(metacomp, null);
        e.setProperty(propName, oldValue, newValue);
        e.setChangeType(FormModelEvent.COMPONENT_LAYOUT_CHANGED);

        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
                l.componentLayoutChanged(e);
                l.formChanged(e);
            }
        }

        if (undoRedoRecording
            && metacomp != null && propName != null && oldValue != newValue)
        {
            addUndoableEdit(e.getUndoableEdit());
        }
    }

    /** Fires an event informing about adding a component to the form.
     * An undoable edit is created and registered automatically. */
    public void fireComponentAdded(RADComponent metacomp,
                                   boolean addedNew)
    {
        t("firing component added: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null")); // NOI18N

        FormModelEvent e = new FormModelEvent(this);
        e.setAddData(metacomp, null, addedNew);
        e.setChangeType(FormModelEvent.COMPONENT_ADDED);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
                if (addedNew)
                    l.componentAdded(e);
  		l.componentAddedToContainer(e);
                l.formChanged(e);
  	    }
  	}

        if (undoRedoRecording && metacomp != null)
            addUndoableEdit(e.getUndoableEdit());
    }

    /** Fires an event informing about removing a component from the form.
     * An undoable edit is created and registered automatically. */
    public void fireComponentRemoved(RADComponent metacomp,
                                     ComponentContainer metacont,
                                     int index,
                                     boolean removedFromModel,
                                     Object codeStructureMark1,
                                     Object codeStructureMark2)
    {
        t("firing component removed: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null")); // NOI18N

        FormModelEvent e = new FormModelEvent(this);
        e.setRemoveData(metacomp, metacont, index, removedFromModel,
                        codeStructureMark1, codeStructureMark2);
        e.setChangeType(FormModelEvent.COMPONENT_REMOVED);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
  		l.componentRemovedFromContainer(e);
                if (codeStructureMark1 != null)
                    l.componentRemoved(e);
                l.formChanged(e);
  	    }
  	}

        if (undoRedoRecording && metacomp != null && metacont != null)
            addUndoableEdit(e.getUndoableEdit());
    }

    /** Fires an event informing about reordering components in a container.
     * An undoable edit is created and registered automatically. */
    public void fireComponentsReordered(ComponentContainer metacont,
                                        int[] perm)
    {
        t("firing components reorder in container: " // NOI18N
          + (metacont instanceof RADComponent ?
             ((RADComponent)metacont).getName() : "<top>")); // NOI18N

        FormModelEvent e = new FormModelEvent(this);
        e.setComponentAndContainer(null, metacont);
        e.setReordering(perm);
        e.setChangeType(FormModelEvent.COMPONENTS_REORDERED);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
  		l.componentsReordered(e);
                l.formChanged(e);
  	    }
  	}

        if (undoRedoRecording && metacont != null)
            addUndoableEdit(e.getUndoableEdit());
    }

    /** Fires an event informing about changing a property of a component.
     * An undoable edit is created and registered automatically. */
    public void fireComponentPropertyChanged(RADComponent metacomp,
                                             String propName,
                                             Object oldValue,
                                             Object newValue)
    {
        t("firing component property change, component: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "<null component>") // NOI18N
          + ", property: " + propName); // NOI18N

        FormModelEvent e = new FormModelEvent(this);
        e.setComponentAndContainer(metacomp, null);
        e.setProperty(propName, oldValue, newValue);
        e.setChangeType(FormModelEvent.COMPONENT_PROPERTY_CHANGED);

        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
                l.componentPropertyChanged(e);
                l.formChanged(e);
            }
        }

        if (undoRedoRecording
            && metacomp != null && propName != null && oldValue != newValue)
        {
            addUndoableEdit(e.getUndoableEdit());
        }
    }

    /** Fires an event informing about changing a synthetic property of
     * a component. An undoable edit is created and registered automatically. */
    public void fireSyntheticPropertyChanged(RADComponent metacomp,
                                             String propName,
                                             Object oldValue,
                                             Object newValue)
    {
        t("firing synthetic property change, component: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null") // NOI18N
          + ", property: " + propName); // NOI18N

        FormModelEvent e = new FormModelEvent(this);
        e.setComponentAndContainer(metacomp, null);
        e.setProperty(propName, oldValue, newValue);
        e.setChangeType(FormModelEvent.SYNTHETIC_PROPERTY_CHANGED);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
  		l.syntheticPropertyChanged(e);
                l.formChanged(e);
  	    }
  	}

        if (undoRedoRecording
            && metacomp != null && propName != null && oldValue != newValue)
        {
            addUndoableEdit(e.getUndoableEdit());
        }
    }

    /** Fires an event informing about attaching a new event to event handler
     * (or also with creating new event handler). An undoable edit is created
     * and registered automatically. */
    public void fireEventHandlerAdded(Event event,
                                      EventHandler handler,
                                      String bodyText, // only if addedNew == true
                                      boolean createdNew)
    {
        t("event handler added: "+handler.getName()); // NOI18N
        FormModelEvent e = new FormModelEvent(this);
        e.setEvent(event, handler, bodyText, createdNew);
        e.setChangeType(FormModelEvent.EVENT_HANDLER_ADDED);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
  		l.eventHandlerAdded(e);
                l.formChanged(e);
  	    }
  	}

        if (undoRedoRecording && event != null && handler != null)
            addUndoableEdit(e.getUndoableEdit());
    }

    /** Fires an event informing about detaching an event from event handler
     * (or also with removing the event handler). An undoable edit is created
     * and registered automatically. */
    public void fireEventHandlerRemoved(Event event,
                                        EventHandler handler,
                                        String bodyText,
                                        boolean handlerDeleted)
    {
        t("firing event handler removed: "+handler.getName()); // NOI18N
        FormModelEvent e = new FormModelEvent(this);
        e.setEvent(event, handler, bodyText, handlerDeleted);
        e.setChangeType(FormModelEvent.EVENT_HANDLER_REMOVED);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
  		l.eventHandlerRemoved(e);
                l.formChanged(e);
  	    }
  	}

        if (undoRedoRecording && event != null && handler != null)
            addUndoableEdit(e.getUndoableEdit());
    }

    /** Fires an event informing about renaming an event handler. An undoable
     * edit is created and registered automatically. */
    public void fireEventHandlerRenamed(EventHandler handler, String oldName) {
        t("event handler renamed: "+handler.getName()); // NOI18N
        FormModelEvent e = new FormModelEvent(this);
        e.setEvent(handler, oldName);
        e.setChangeType(FormModelEvent.EVENT_HANDLER_RENAMED);

  	Object[] listeners = listenerList.getListenerList();
  	for (int i = listeners.length - 2; i >= 0; i -= 2) {
  	    if (listeners[i] == FormModelListener.class) {
                FormModelListener l = (FormModelListener)listeners[i+1];
  		l.eventHandlerRenamed(e);
                l.formChanged(e);
  	    }
  	}

        if (undoRedoRecording && handler != null && oldName != null)
            addUndoableEdit(e.getUndoableEdit());
    }

    // -------------

    FormEventHandlers getFormEventHandlers() {
        if (eventHandlers == null)
            eventHandlers = new FormEventHandlers(getCodeGenerator());
        return eventHandlers;
    }

    CodeStructure getCodeStructure() {
        return codeStructure;
    }

    CodeGenerator getCodeGenerator() {
//        return FormEditorSupport.getCodeGenerator(this);
        if (codeGenerator == null)
            codeGenerator = new JavaCodeGenerator();
        return codeGenerator;
    }

    void initializeCodeGenerator() {
        getCodeGenerator().initialize(this);
    }

    // ---------------
    // ModelContainer innerclass

    final class ModelContainer implements ComponentContainer {
        public RADComponent[] getSubBeans() {
            int n = otherComponents.size();
            if (topRADComponent != null)
                n++;
            RADComponent[] comps = new RADComponent[n];
            otherComponents.toArray(comps);
            if (topRADComponent != null)
                comps[n-1] = topRADComponent;
            return comps;
        }

        public void initSubComponents(RADComponent[] initComponents) {
            otherComponents.clear();
            for (int i = 0; i < initComponents.length; i++)
                if (initComponents[i] != topRADComponent)
                    otherComponents.add(initComponents[i]);
        }

        public void reorderSubComponents(int[] perm) {
            RADComponent[] components = new RADComponent[otherComponents.size()];
            for (int i=0; i < perm.length; i++)
                components[perm[i]] = (RADComponent) otherComponents.get(i);

            otherComponents.clear();
            otherComponents.addAll(Arrays.asList(components));
        }

        public void add(RADComponent comp) {
            comp.setParentComponent(null);
            otherComponents.add(comp);
        }

        public void remove(RADComponent comp) {
            if (otherComponents.remove(comp))
                comp.setParentComponent(null);
        }

        public int getIndexOf(RADComponent comp) {
            int index = otherComponents.indexOf(comp);
            if (index < 0 && comp == topRADComponent)
                index = otherComponents.size();
            return index;
        }
    }

    // ---------------

    /** For debugging purposes only. */
    static private int traceCount = 0;
    /** For debugging purposes only. */
    static private final boolean TRACE = false;
    /** For debugging purposes only. */
    static void t(String str) {
        if (TRACE)
            if (str != null)
                System.out.println("FormModel "+(++traceCount)+": "+str); // NOI18N
            else
                System.out.println(""); // NOI18N
    }
}
