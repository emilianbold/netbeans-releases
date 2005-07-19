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

import java.util.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.*;

import org.openide.awt.UndoRedo;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.CodeStructure;
import org.netbeans.modules.form.layoutdesign.*;

/**
 * Holds all data of a form.
 * 
 * @author Tran Duc Trung, Tomas Pavek
 */

public class FormModel
{
    // name of the form (name of the DataObject)
    private String formName;

    private boolean readOnly = false;

    // the class on which the form is based (which is extended in the java file)
    private Class formBaseClass;

    // the top metacomponent of the form (null if form is based on Object)
    private RADComponent topRADComponent;

    // other components - out of the main hierarchy under topRADComponent
    private ArrayList otherComponents = new ArrayList(10);

    // holds both topRADComponent and otherComponents
    private ComponentContainer modelContainer;

    private LayoutModel layoutModel;

    private Map idToComponents = new HashMap();

//    private boolean formLoaded = false;

    private UndoRedo.Manager undoRedoManager;
    private boolean undoRedoRecording = false;
    private CompoundEdit compoundEdit;

    private FormEvents formEvents;

    // list of listeners registered on FormModel
    private ArrayList listeners;
    private EventBroker eventBroker;

    private MetaComponentCreator metaCreator;

    private CodeStructure codeStructure = new CodeStructure(false);
//    private CodeGenerator codeGenerator; // [this reference should be removed]
    
    private FormSettings settings = new FormSettings();

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
            if (FormUtils.isContainer(formClass)) {
                topComp = new RADVisualFormContainer();
                layoutModel = new LayoutModel();
            }
            else {
                topComp = new RADVisualComponent();
            }
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

//    public final boolean isFormLoaded() {
//        return formLoaded;
//    }

//    public final FormDesigner getFormDesigner() {
//        return FormEditorSupport.getFormDesigner(this);
//    }
//
//    // for compatibility with previous version
//    public final FormDataObject getFormDataObject() {
//        return FormEditorSupport.getFormDataObject(this);
//    }

    public final RADComponent getTopRADComponent() {
        return topRADComponent;
    }

    public ComponentContainer getModelContainer() {
        if (modelContainer == null)
            modelContainer = new ModelContainer();
        return modelContainer;
    }

    public final LayoutModel getLayoutModel() {
        return layoutModel;
    }

    public final RADComponent getMetaComponent(String id) {
        return (RADComponent) idToComponents.get(id);
    }

    public RADComponent findRADComponent(String name) {
        Iterator allComps = idToComponents.values().iterator(); // getMetaComponents().iterator();
        while (allComps.hasNext()) {
            RADComponent comp = (RADComponent) allComps.next();
            if (name.equals(comp.getName()))
                return comp;
        }
        return null;
    }

    /** Returns all meta components in the model.
     */
    public java.util.List getMetaComponents() {
        return new ArrayList(idToComponents.values());
//        ArrayList list = new ArrayList();
//        collectMetaComponents(getModelContainer(), list);
//        return list; //Collections.unmodifiableList(list);
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

    /** Returns all "other components" (not in the main visual hierarchy).
     * @param recursively whether also all sub-componets should be collected
     */
    public RADComponent[] getOtherComponents(boolean recursively) {
        java.util.List list;
        if (recursively) {
            list = new ArrayList();
            for (Iterator it=otherComponents.iterator(); it.hasNext(); ) {
                RADComponent comp = (RADComponent) it.next();
                list.add(comp);
                if (recursively && comp instanceof ComponentContainer)
                    collectMetaComponents((ComponentContainer) comp, list);
            }
        }
        else list = otherComponents;

        return (RADComponent[]) list.toArray(new RADComponent[list.size()]);
    }

    // for compatibility with previous version
//    public RADComponent[] getNonVisualComponents() {
//        return (RADComponent[]) otherComponents.toArray(
//                                new RADComponent[otherComponents.size()]); 
//    }

    public FormEvents getFormEvents() {
        if (formEvents == null)
            formEvents = new FormEvents(this);
        return formEvents;
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

    /**
     * @return MetaComponentCreator responsible for creating new components and
     *         adding them to the model.
     */
    public MetaComponentCreator getComponentCreator() {
        if (metaCreator == null)
            metaCreator = new MetaComponentCreator(this);
        return metaCreator;
    }

    /** Adds a new component to given (non-visual) container in the model. If
     * the container is not specified, the component is added to the
     * "other components". */
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

    /** Adds a new visual component to given container managed by the old
     * layout support. */
    public void addVisualComponent(RADVisualComponent metacomp,
                                   RADVisualContainer parentContainer,
                                   LayoutConstraints constraints)
    {
        LayoutSupportManager layoutSupport = parentContainer.getLayoutSupport();
        if (layoutSupport != null) {
            RADVisualComponent[] compArray = new RADVisualComponent[] { metacomp };
            LayoutConstraints[] constrArray = new LayoutConstraints[] { constraints };

            // this may throw a RuntimeException if the components are not accepted
            layoutSupport.acceptNewComponents(compArray, constrArray, -1);

            parentContainer.add(metacomp);

            layoutSupport.addComponents(compArray, constrArray, -1);

            boolean newlyAdded = !metacomp.isInModel();
            if (newlyAdded)
                setInModelRecursively(metacomp, true);

            fireComponentAdded(metacomp, newlyAdded);
        }
        else {
            addComponent(metacomp, parentContainer);
        }
    }

    public void setContainerLayout(RADVisualContainer metacont,
                                   LayoutSupportDelegate layoutDelegate,
                                   java.awt.LayoutManager initInstance)
        throws Exception
    {
        LayoutSupportManager currentLS = metacont.getLayoutSupport();
        LayoutSupportDelegate currentDel =
            currentLS != null ? currentLS.getLayoutDelegate() : null;

        if (currentLS == null) { // switching to old layout support
            layoutModel.changeContainerToComponent(metacont.getId());
            metacont.setOldLayoutSupport(true);
        }
        metacont.setLayoutSupportDelegate(layoutDelegate, initInstance);

        fireContainerLayoutExchanged(metacont, currentDel, layoutDelegate);
    }

    public void setNaturalContainerLayout(RADVisualContainer metacont) {
        LayoutSupportManager currentLS = metacont.getLayoutSupport();
        if (currentLS == null)
            return; // already set (no old layout support)

        LayoutSupportDelegate currentDel = currentLS.getLayoutDelegate();

        metacont.setOldLayoutSupport(false);

        if (!layoutModel.changeComponentToContainer(metacont.getId())) {
            layoutModel.addRootComponent(
                    new LayoutComponent(metacont.getId(), true));
        }

        fireContainerLayoutExchanged(metacont, currentDel, null);
    }

    public void removeComponent(RADComponent metacomp, boolean fromModel) {
        Object layoutStartMark = layoutModel.getChangeMark();
        UndoableEdit ue = layoutModel.getUndoableEdit();
        removeLayoutComponentsRecursively(metacomp, fromModel);
        removeComponentImpl(metacomp, fromModel);
        // [TODO need effective multi-component remove from LayoutModel (start in ComponentInspector.DeleteActionPerformer)]
        if (!layoutStartMark.equals(layoutModel.getChangeMark())) {
            addUndoableEdit(ue); // is added to a compound edit
        }
    }

    void removeComponentImpl(RADComponent metacomp, boolean fromModel) {
        if (fromModel && formEvents != null) {
            removeEventHandlersRecursively(metacomp);
        }

        RADComponent parent = metacomp.getParentComponent();
        ComponentContainer parentContainer =
            parent instanceof ComponentContainer ?
                (ComponentContainer) parent : getModelContainer();

        int index = parentContainer.getIndexOf(metacomp);
        parentContainer.remove(metacomp);

        Object codeStructureMark1 = null, codeStructureMark2 = null;
        if (fromModel) {
            // turn on undo/redo recording on code structure (if allowed)
            boolean codeStructureUndoRedo = codeStructure.isUndoRedoRecording();
            if (undoRedoRecording && !codeStructureUndoRedo) {
                codeStructure.setUndoRedoRecording(true);
                codeStructureMark1 = codeStructure.markForUndo();
            }

            // [these changes modify also the code structure - what if undo/redo is just running on it ??]
            metacomp.removeCodeExpression();
            metacomp.setInModel(false);
            if (metacomp instanceof ComponentContainer)
                releaseSubcomponentsRecursively(metacomp);

            // turn off undo/redo recording on code structure (if turned on)
            if (undoRedoRecording && !codeStructureUndoRedo) {
                codeStructureMark2 = codeStructure.markForUndo();
                if (codeStructureMark2.equals(codeStructureMark1))
                    codeStructureMark2 = codeStructureMark1 = null;

                codeStructure.setUndoRedoRecording(false);
            }
        }

        FormModelEvent ev = fireComponentRemoved(metacomp, parentContainer, index, fromModel);
        ev.setCodeChange(codeStructureMark1, codeStructureMark2);
    }
    
    private void removeLayoutComponentsRecursively(RADComponent metacomp, boolean fromModel) {
        if (fromModel && (metacomp instanceof ComponentContainer)) {
            RADComponent[] comps = ((ComponentContainer)metacomp).getSubBeans();
            for (int i=0; i<comps.length; i++) {
                removeLayoutComponentsRecursively(comps[i], fromModel);
            }
        }
        layoutModel.removeComponentAndIntervals(metacomp.getId(), fromModel);
    }

    void updateMapping(RADComponent metacomp, boolean register) {
        if (register)
            idToComponents.put(metacomp.getId(), metacomp);
        else
            idToComponents.remove(metacomp.getId());
    }

    // removes all event handlers attached to given component and all
    // its subcomponents
    private void removeEventHandlersRecursively(RADComponent comp) {
        if (comp instanceof ComponentContainer) {
            RADComponent[] subcomps = ((ComponentContainer)comp).getSubBeans();
            for (int i=0; i<subcomps.length; i++)
                removeEventHandlersRecursively(subcomps[i]);
        }

        Event[] events = comp.getKnownEvents();
        for (int i=0; i < events.length; i++)
            if (events[i].hasEventHandlers())
                getFormEvents().detachEvent(events[i]);
    }

    private void releaseSubcomponentsRecursively(RADComponent metacomp) {
        RADComponent[] comps = ((ComponentContainer)metacomp).getSubBeans();
        for (int i=0, n=comps.length; i < n; i++) {
            metacomp = comps[i];
            metacomp.releaseCodeExpression();
            metacomp.setInModel(false);
            if (metacomp instanceof ComponentContainer)
                releaseSubcomponentsRecursively(metacomp);
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

    public CompoundEdit endCompoundEdit(boolean commit) {
        if (compoundEdit != null) {
            t("ending compound edit: "+commit); // NOI18N
            compoundEdit.end();
            if (commit && undoRedoRecording && compoundEdit.isSignificant()) {
                getUndoRedoManager().undoableEditHappened(
                    new UndoableEditEvent(this, compoundEdit));
            }
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
            getUndoRedoManager().undoableEditHappened(
                     new UndoableEditEvent(this, edit));
    }

    UndoRedo.Manager getUndoRedoManager() {
//        if (undoRedoManager == null) {
//            undoRedoManager = new UndoRedoManager();
//            undoRedoManager.setLimit(50);
//        }
        return undoRedoManager;
    }

    // [Undo manager performing undo/redo in AWT event thread should not be
    //  probably implemented here - in FormModel - but seperately.]
    static class UndoRedoManager extends UndoRedo.Manager {
        private Mutex.ExceptionAction runUndo = new Mutex.ExceptionAction() {
            public Object run() throws Exception {
                superUndo();
                return null;
            }
        };
        private Mutex.ExceptionAction runRedo = new Mutex.ExceptionAction() {
            public Object run() throws Exception {
                superRedo();
                return null;
            }
        };

        public void superUndo() throws CannotUndoException {
            super.undo();
        }
        public void superRedo() throws CannotRedoException {
            super.redo();
        }

        public void undo() throws CannotUndoException {
            if (java.awt.EventQueue.isDispatchThread()) {
                superUndo();
            }
            else {
                try {
                    Mutex.EVENT.readAccess(runUndo);
                }
                catch (MutexException ex) {
                    Exception e = ex.getException();
                    if (e instanceof CannotUndoException)
                        throw (CannotUndoException) e;
                    else // should not happen, ignore
                        e.printStackTrace();
                }
            }
        }

        public void redo() throws CannotRedoException {
            if (java.awt.EventQueue.isDispatchThread()) {
                superRedo();
            }
            else {
                try {
                    Mutex.EVENT.readAccess(runRedo);
                }
                catch (MutexException ex) {
                    Exception e = ex.getException();
                    if (e instanceof CannotRedoException)
                        throw (CannotRedoException) e;
                    else // should not happen, ignore
                        e.printStackTrace();
                }
            }
        }
    }

    // ----------
    // listeners registration, firing methods

    public synchronized void addFormModelListener(FormModelListener l) {
        if (listeners == null)
            listeners = new ArrayList();
        listeners.add(l);
    }

    public synchronized void removeFormModelListener(FormModelListener l) {
        if (listeners != null)
            listeners.remove(l);
    }

    /** Fires an event informing about that the form has been just loaded. */
    public void fireFormLoaded() {
        t("firing form loaded"); // NOI18N

//        formLoaded = true;
        eventBroker = new EventBroker();
//        if (undoRedoManager != null)
//            undoRedoManager.discardAllEdits();
        if (!readOnly && !Boolean.getBoolean("netbeans.form.no_undo")) { // NOI18N
            undoRedoManager = new UndoRedoManager();
            undoRedoManager.setLimit(50);
            setUndoRedoRecording(true);
        }
//        initializeCodeGenerator(); // [should go away]

        sendEventLater(new FormModelEvent(this, FormModelEvent.FORM_LOADED));
    }

    /** Fires an event informing about that the form is just about to be saved. */
    public void fireFormToBeSaved() {
        t("firing form to be saved"); // NOI18N

        sendEventImmediately(
            new FormModelEvent(this, FormModelEvent.FORM_TO_BE_SAVED));
    }

    /** Fires an event informing about that the form is just about to be closed. */
    public void fireFormToBeClosed() {
        t("firing form to be closed"); // NOI18N

        if (undoRedoManager != null)
            undoRedoManager.discardAllEdits();

        sendEventImmediately(
            new FormModelEvent(this, FormModelEvent.FORM_TO_BE_CLOSED));
    }

    /** Fires an event informing about changing layout manager of a container.
     * An undoable edit is created and registered automatically. */
    public FormModelEvent fireContainerLayoutExchanged(
                              RADVisualContainer metacont,
                              LayoutSupportDelegate oldLayout,
                              LayoutSupportDelegate newLayout)
    {
        t("firing container layout exchange, container: " // NOI18N
          + (metacont != null ? metacont.getName() : "null")); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.CONTAINER_LAYOUT_EXCHANGED);
        ev.setLayout(metacont, oldLayout, newLayout);
        sendEvent(ev);

        if (undoRedoRecording && metacont != null && oldLayout != newLayout)
            addUndoableEdit(ev.getUndoableEdit());

        return ev;
    }

    /** Fires an event informing about changing a property of container layout.
     * An undoable edit is created and registered automatically. */
    public FormModelEvent fireContainerLayoutChanged(
                              RADVisualContainer metacont,
                              String propName,
                              Object oldValue,
                              Object newValue)
    {
        t("firing container layout change, container: " // NOI18N
          + (metacont != null ? metacont.getName() : "null") // NOI18N
          + ", property: " + propName); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.CONTAINER_LAYOUT_CHANGED);
        ev.setComponentAndContainer(metacont, metacont);
        ev.setProperty(propName, oldValue, newValue);
        sendEvent(ev);

        if (undoRedoRecording
            && metacont != null && (propName == null || oldValue != newValue))
        {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /** Fires an event informing about changing a property of component layout
     * constraints. An undoable edit is created and registered automatically. */
    public FormModelEvent fireComponentLayoutChanged(
                              RADVisualComponent metacomp,
                              String propName,
                              Object oldValue,
                              Object newValue)
    {
        t("firing component layout change: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null")); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.COMPONENT_LAYOUT_CHANGED);
        ev.setComponentAndContainer(metacomp, null);
        ev.setProperty(propName, oldValue, newValue);
        sendEvent(ev);

        if (undoRedoRecording
            && metacomp != null && propName != null && oldValue != newValue)
        {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /** Fires an event informing about adding a component to the form.
     * An undoable edit is created and registered automatically. */
    public FormModelEvent fireComponentAdded(RADComponent metacomp,
                                             boolean addedNew)
    {
        t("firing component added: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null")); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.COMPONENT_ADDED);
        ev.setAddData(metacomp, null, addedNew);
        sendEvent(ev);

        if (undoRedoRecording && metacomp != null)
            addUndoableEdit(ev.getUndoableEdit());

        return ev;
    }

    /** Fires an event informing about removing a component from the form.
     * An undoable edit is created and registered automatically. */
    public FormModelEvent fireComponentRemoved(RADComponent metacomp,
                                               ComponentContainer metacont,
                                               int index,
                                               boolean removedFromModel)
//                                               Object codeStructureMark1,
//                                               Object codeStructureMark2)
    {
        t("firing component removed: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null")); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.COMPONENT_REMOVED);
        ev.setRemoveData(metacomp, metacont, index, removedFromModel);
//                        codeStructureMark1, codeStructureMark2);
        sendEvent(ev);

        if (undoRedoRecording && metacomp != null && metacont != null)
            addUndoableEdit(ev.getUndoableEdit());

        return ev;
    }

    /** Fires an event informing about reordering components in a container.
     * An undoable edit is created and registered automatically. */
    public FormModelEvent fireComponentsReordered(ComponentContainer metacont,
                                                  int[] perm)
    {
        t("firing components reorder in container: " // NOI18N
          + (metacont instanceof RADComponent ?
             ((RADComponent)metacont).getName() : "<top>")); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.COMPONENTS_REORDERED);
        ev.setComponentAndContainer(null, metacont);
        ev.setReordering(perm);
        sendEvent(ev);

        if (undoRedoRecording && metacont != null)
            addUndoableEdit(ev.getUndoableEdit());

        return ev;
    }

    /** Fires an event informing about changing a property of a component.
     * An undoable edit is created and registered automatically. */
    public FormModelEvent fireComponentPropertyChanged(RADComponent metacomp,
                                                       String propName,
                                                       Object oldValue,
                                                       Object newValue)
    {
        t("firing component property change, component: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "<null component>") // NOI18N
          + ", property: " + propName); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.COMPONENT_PROPERTY_CHANGED);
        ev.setComponentAndContainer(metacomp, null);
        ev.setProperty(propName, oldValue, newValue);
        sendEvent(ev);

        if (undoRedoRecording
            && metacomp != null && propName != null && oldValue != newValue)
        {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /** Fires an event informing about changing a synthetic property of
     * a component. An undoable edit is created and registered automatically. */
    public FormModelEvent fireSyntheticPropertyChanged(RADComponent metacomp,
                                                       String propName,
                                                       Object oldValue,
                                                       Object newValue)
    {
        t("firing synthetic property change, component: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null") // NOI18N
          + ", property: " + propName); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.SYNTHETIC_PROPERTY_CHANGED);
        ev.setComponentAndContainer(metacomp, null);
        ev.setProperty(propName, oldValue, newValue);
        sendEvent(ev);

        if (undoRedoRecording && propName != null && oldValue != newValue)
        {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /** Fires an event informing about attaching a new event to an event handler
     * (createdNew parameter indicates whether the event handler was created
     * first). An undoable edit is created and registered automatically. */
    public FormModelEvent fireEventHandlerAdded(Event event,
                                                String handler,
                                                String bodyText,
                                                boolean createdNew)
    {
        t("event handler added: "+handler); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.EVENT_HANDLER_ADDED);
        ev.setEvent(event, handler, bodyText, createdNew);
        sendEvent(ev);

        if (undoRedoRecording && event != null && handler != null)
            addUndoableEdit(ev.getUndoableEdit());

        return ev;
    }

    /** Fires an event informing about detaching an event from event handler
     * (handlerDeleted parameter indicates whether the handler was deleted as
     * the last event was detached). An undoable edit is created and registered
     * automatically. */
    public FormModelEvent fireEventHandlerRemoved(Event event,
                                                  String handler,
                                                  boolean handlerDeleted)
    {
        t("firing event handler removed: "+handler); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.EVENT_HANDLER_REMOVED);
        ev.setEvent(event, handler, null, handlerDeleted);
        sendEvent(ev);

        if (undoRedoRecording && event != null && handler != null)
            addUndoableEdit(ev.getUndoableEdit());

        return ev;
    }

    /** Fires an event informing about renaming an event handler. An undoable
     * edit is created and registered automatically. */
    public FormModelEvent fireEventHandlerRenamed(String oldHandlerName,
                                                  String newHandlerName)
    {
        t("event handler renamed: "+oldHandlerName+" to "+newHandlerName); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.EVENT_HANDLER_RENAMED);
        ev.setEvent(oldHandlerName, newHandlerName);
        sendEvent(ev);

        if (undoRedoRecording && oldHandlerName != null && newHandlerName != null)
            addUndoableEdit(ev.getUndoableEdit());

        return ev;
    }

    /** Fires an event informing about general form change. */
    public FormModelEvent fireFormChanged() {
        t("firing form change"); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.OTHER_CHANGE);
        sendEvent(ev);

        return ev;
    }

    public void fireEvents(FormModelEvent[] events) {
        java.util.List targets;
        synchronized(this) {
            if (listeners == null)
                return;
            targets = (ArrayList) listeners.clone();
        }

        for (int i=0; i < targets.size(); i++) {
            FormModelListener l = (FormModelListener) targets.get(i);
            l.formChanged(events);
        }
    }

    // ---------
    // firing methods for batch event processing

    void sendEvent(FormModelEvent ev) {
        EventBroker broker = getEventBroker();
        if (broker != null)
            broker.sendEvent(ev);
        else {
            t("no event broker, firing event directly: "+ev.getChangeType()); // NOI18N
            fireEvents(new FormModelEvent[] { ev });
        }
    }

    void sendEventLater(FormModelEvent ev) {
        EventBroker broker = getEventBroker();
        if (broker != null)
            broker.sendEventLater(ev);
        else {
            t("no event broker, firing event directly: "+ev.getChangeType()); // NOI18N
            fireEvents(new FormModelEvent[] { ev });
        }
    }

    void sendEventImmediately(FormModelEvent ev) {
        fireEvents(new FormModelEvent[] { ev });
    }

    EventBroker getEventBroker() {
        // event broker is created when the form is loaded
//        if (eventBroker == null && isFormLoaded())
//            eventBroker = new EventBroker();
        return eventBroker;
    }
    
    public FormSettings getSettings() {
        return settings;
    }

    /** Class that collects events and fires them on FormModel in one batch
     * later. Collecting the events works only if the events are passed
     * to the broker from AWT event dispatch thread.
     */
    private class EventBroker implements Runnable {
        private List eventList;
        private boolean compoundUndoStarted;

        public void sendEvent(FormModelEvent ev) {
            if (shouldSendLater(ev))
                sendEventLater(ev);
            else
                sendEventImmediately(ev);
        }

        public void sendEventImmediately(FormModelEvent ev) {
            t("firing event directly from event broker: "+ev.getChangeType()); // NOI18N
            FormModel.this.fireEvents(new FormModelEvent[] { ev });
        }

        public void sendEventLater(FormModelEvent ev) {
            // works properly only if called from AWT event dispatch thread
            if (!java.awt.EventQueue.isDispatchThread()) {
                sendEventImmediately(ev);
                return;
            }

            if (eventList == null) {
                eventList = new ArrayList();
                compoundUndoStarted = ev.isModifying()
                                      && FormModel.this.isUndoRedoRecording()
                                      && FormModel.this.startCompoundEdit();
                if (compoundUndoStarted)
                    t("compound undoable edit started from event broker"); // NOI18N
                java.awt.EventQueue.invokeLater(this);
            }

            eventList.add(ev);
            t("event "+ev.getChangeType()+" added to queue in event broker"); // NOI18N
        }

        private boolean shouldSendLater(FormModelEvent ev) {
            return eventList != null || ev.isModifying();
        }

        private List pickUpEvents() {
            List list = eventList;
            eventList = null;
            if (compoundUndoStarted) {
                compoundUndoStarted = false;
                FormModel.this.endCompoundEdit(true);
            }
            return list;
        }

        public void run() {
            List list = pickUpEvents();
            if (list != null && !list.isEmpty()) {
                FormModelEvent[] events = new FormModelEvent[list.size()];
                list.toArray(events);
                t("firing event batch of "+list.size()+" events from event broker"); // NOI18N
                FormModel.this.fireEvents(events);
            }
        }
    }

    // -------------

    CodeStructure getCodeStructure() {
        return codeStructure;
    }

//    CodeGenerator getCodeGenerator() {
////        return FormEditorSupport.getCodeGenerator(this);
//        if (codeGenerator == null)
//            codeGenerator = new JavaCodeGenerator();
//        return codeGenerator;
//    }
//
//    void initializeCodeGenerator() {
//        getCodeGenerator().initialize(this);
//    }

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
