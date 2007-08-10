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
    private Class<?> formBaseClass;

    // the top metacomponent of the form (null if form is based on Object)
    private RADComponent topRADComponent;

    // other components - out of the main hierarchy under topRADComponent
    private List<RADComponent> otherComponents = new ArrayList<RADComponent>(10);

    // holds both topRADComponent and otherComponents
    private ComponentContainer modelContainer;

    private LayoutModel layoutModel;

    private Map<String,RADComponent> idToComponents = new HashMap<String,RADComponent>();

    private boolean formLoaded = false;

    private UndoRedo.Manager undoRedoManager;
    private boolean undoRedoRecording = false;
    private CompoundEdit compoundEdit;
    private boolean undoCompoundEdit = false;

    private FormEvents formEvents;

    // list of listeners registered on FormModel
    private ArrayList<FormModelListener> listeners;
    private EventBroker eventBroker;
    private boolean firing;

    private MetaComponentCreator metaCreator;

    private CodeStructure codeStructure = new CodeStructure(false);
    
    private FormSettings settings = new FormSettings(this);
    
    private boolean freeDesignDefaultLayout = false;

    // -------------
    // initialization

    FormModel() {
    }

    /** This methods sets the form base class (which is in fact the superclass
     * of the form class in source java file). It is used for initializing
     * the top meta component, and is also presented as the top component
     * in designer and inspector.
     */
    public void setFormBaseClass(Class<?> formClass) throws Exception {
        if (formBaseClass != null)
            throw new IllegalStateException("Form type already initialized."); // NOI18N

        RADComponent topComp;
        if (FormUtils.isVisualizableClass(formClass)) {
            if (FormUtils.isContainer(formClass)) {
                topComp = new RADVisualFormContainer();
            }
            else {
                topComp = new RADVisualComponent() {
                    // top-level component does not have a variable
                    @Override
                    public String getName() {
                        return FormUtils.getBundleString("CTL_FormTopContainerName"); // NOI18N
                    }
                    @Override
                    public void setName(String value) {}
                };
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
        layoutModel = new LayoutModel();
        layoutModel.setChangeRecording(false);
    }

    public Class<?> getFormBaseClass() {
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

    public final boolean wasCorrected() {
        return formLoaded && layoutModel != null && layoutModel.wasCorrected();
    }

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

    public Collection<RADComponent> getOtherComponents() {
        return Collections.unmodifiableCollection(otherComponents);
    }

    public final LayoutModel getLayoutModel() {
        return layoutModel;
    }

    public final RADComponent getMetaComponent(String id) {
        return idToComponents.get(id);
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

    /**
     * Returns list of all components in the model. A new List instance is
     * created. The order of the components is random.
     */
    public java.util.List<RADComponent> getComponentList() {
        return new ArrayList<RADComponent>(idToComponents.values());
    }

    /**
     * Returns list of all components in the model. A new instance of list is
     * created and the components are added to the list in the traversal order
     * (used e.g. by code generator or persistence manager).
     */
    public java.util.List<RADComponent> getOrderedComponentList() {
        java.util.List<RADComponent> list = new ArrayList<RADComponent>(idToComponents.size());
        collectMetaComponents(getModelContainer(), list);
        return list;
    }

    /**
     * Returns an unmodifiable collection of all components in the model
     * in random order.
     */
    public Collection<RADComponent> getAllComponents() {
        return Collections.unmodifiableCollection(idToComponents.values());
    }

    public List<RADComponent> getNonVisualComponents() {
        List<RADComponent> list = new ArrayList<RADComponent>(otherComponents.size());
        for (RADComponent metacomp : otherComponents) {
            if (!(metacomp instanceof RADVisualComponent)) {
                list.add(metacomp);
            }
        }
        return list;
    }

    public List<RADComponent> getVisualComponents() {
        List<RADComponent> list = new ArrayList<RADComponent>(idToComponents.size());
        for (Map.Entry<String,RADComponent> e : idToComponents.entrySet()) {
            RADComponent metacomp = e.getValue();
            if (metacomp instanceof RADVisualComponent) {
                list.add(metacomp);
            }
        }
        return list;
    }

    public FormEvents getFormEvents() {
        if (formEvents == null)
            formEvents = new FormEvents(this);
        return formEvents;
    }

    private static void collectMetaComponents(ComponentContainer cont,
                                              java.util.List<RADComponent> list) {
        RADComponent[] comps = cont.getSubBeans();
        for (int i = 0; i < comps.length; i++) {
            RADComponent comp = comps[i];
            list.add(comp);
            if (comp instanceof ComponentContainer)
                collectMetaComponents((ComponentContainer) comp, list);
        }
    }

    private static void collectVisualMetaComponents(RADVisualContainer cont,
                                                    java.util.List<RADComponent> list) {
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
                             ComponentContainer parentContainer,
                             boolean newlyAdded)
    {
        if (newlyAdded || !metacomp.isInModel()) {
            setInModelRecursively(metacomp, true);
            newlyAdded = true;
        }

        if (parentContainer != null) {
            parentContainer.add(metacomp);
        }
        else {
            metacomp.setParentComponent(null);
            otherComponents.add(metacomp);
        }

        FormModelEvent ev = fireComponentAdded(metacomp, newlyAdded);
    }

    /** Adds a new visual component to given container managed by the old
     * layout support. */
    public void addVisualComponent(RADVisualComponent metacomp,
                                   RADVisualContainer parentContainer,
                                   Object constraints,
                                   boolean newlyAdded)
    {
        LayoutSupportManager layoutSupport = parentContainer.getLayoutSupport();
        if (layoutSupport != null) {
            RADVisualComponent[] compArray = new RADVisualComponent[] { metacomp };
            LayoutConstraints c = constraints instanceof LayoutConstraints ?
                                  (LayoutConstraints) constraints : null;
            LayoutConstraints[] constrArray = new LayoutConstraints[] { c };
            int index = constraints instanceof Integer ? ((Integer)constraints).intValue() : -1;

            // component needs to be "in model" (have code expression) before added to layout
            if (newlyAdded || !metacomp.isInModel()) {
                setInModelRecursively(metacomp, true);
                newlyAdded = true;
            }

            try {
                layoutSupport.acceptNewComponents(compArray, constrArray, index);
            }
            catch (RuntimeException ex) {
                // LayoutSupportDelegate may not accept the component
                if (newlyAdded)
                    setInModelRecursively(metacomp, false);
                throw ex;
            }

            parentContainer.add(metacomp, index);

            layoutSupport.addComponents(compArray, constrArray, index);

            FormModelEvent ev = fireComponentAdded(metacomp, newlyAdded);
        }
        else {
            addComponent(metacomp, parentContainer, newlyAdded);
        }
    }

    public void setContainerLayoutImpl(RADVisualContainer metacont,
                                   LayoutSupportDelegate layoutDelegate,
                                   java.awt.LayoutManager initInstance)
        throws Exception
    {
        LayoutSupportManager currentLS = metacont.getLayoutSupport();
        LayoutSupportDelegate currentDel =
            currentLS != null ? currentLS.getLayoutDelegate() : null;

        if (currentLS == null) { // switching to old layout support
            metacont.setOldLayoutSupport(true);
        }
        metacont.setLayoutSupportDelegate(layoutDelegate, initInstance);

        fireContainerLayoutExchanged(metacont, currentDel, layoutDelegate);
    }

    public void setContainerLayout(RADVisualContainer metacont,
                                   LayoutSupportDelegate layoutDelegate,
                                   java.awt.LayoutManager initInstance)
        throws Exception {
        LayoutSupportManager currentLS = metacont.getLayoutSupport();
        setContainerLayoutImpl(metacont, layoutDelegate, initInstance);
        if (currentLS == null) { // switching to old layout support
            Object layoutStartMark = layoutModel.getChangeMark();
            UndoableEdit ue = layoutModel.getUndoableEdit();
            boolean autoUndo = true;
            try {
                layoutModel.changeContainerToComponent(metacont.getId());
                autoUndo = false;
            } finally {
                if (layoutStartMark != null && !layoutStartMark.equals(layoutModel.getChangeMark())) {
                    addUndoableEdit(ue);
                }
                if (autoUndo) {
                    forceUndoOfCompoundEdit();
                }
            }
        }
    }
    
    void setNaturalContainerLayoutImpl(RADVisualContainer metacont) {
        LayoutSupportManager currentLS = metacont.getLayoutSupport();
        LayoutSupportDelegate currentDel = currentLS.getLayoutDelegate();
        metacont.setOldLayoutSupport(false);
        fireContainerLayoutExchanged(metacont, currentDel, null);
    }

    public void setNaturalContainerLayout(RADVisualContainer metacont) {
        LayoutSupportManager currentLS = metacont.getLayoutSupport();
        if (currentLS == null)
            return; // already set (no old layout support)
        
        setNaturalContainerLayoutImpl(metacont);
        Object layoutStartMark = layoutModel.getChangeMark();
        UndoableEdit ue = layoutModel.getUndoableEdit();
        boolean autoUndo = true;
        try {
            if (!layoutModel.changeComponentToContainer(metacont.getId())) {
                layoutModel.addRootComponent(
                        new LayoutComponent(metacont.getId(), true));
            }
            autoUndo = false;
        } finally {
            if (layoutStartMark != null && !layoutStartMark.equals(layoutModel.getChangeMark())) {
                addUndoableEdit(ue);
            }
            if (autoUndo) {
                forceUndoOfCompoundEdit();
            }
        }
    }

    public void removeComponent(RADComponent metacomp, boolean fromModel) {
        Object layoutStartMark = null;
        UndoableEdit ue = null;
        boolean autoUndo = true;
        try {
            if (fromModel && (layoutModel != null)) {
                layoutStartMark = layoutModel.getChangeMark();
                ue = layoutModel.getUndoableEdit();
                layoutModel.removeComponent(metacomp.getId(), true);
                removeLayoutComponentsRecursively(metacomp);
            }

            // [TODO need effective multi-component remove from LayoutModel (start in ComponentInspector.DeleteActionPerformer)]
            autoUndo = false;
        } finally {
            removeComponentImpl(metacomp, fromModel);
            if (layoutStartMark != null && !layoutStartMark.equals(layoutModel.getChangeMark())) {
                addUndoableEdit(ue); // is added to a compound edit
            }
            if (autoUndo) {
                forceUndoOfCompoundEdit();
            }
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

        if (fromModel) {
            setInModelRecursively(metacomp, false);
        }

        FormModelEvent ev = fireComponentRemoved(metacomp, parentContainer, index, fromModel);
    }

    // needed for the case of mixed hierarchy of new/old layout support
    private void removeLayoutComponentsRecursively(RADComponent metacomp) {
        if (metacomp instanceof ComponentContainer) {
            RADComponent[] comps = ((ComponentContainer)metacomp).getSubBeans();
            for (int i=0; i<comps.length; i++) {
                removeLayoutComponentsRecursively(comps[i]);
            }
        }
        LayoutComponent layoutComp = layoutModel == null ? null : layoutModel.getLayoutComponent(metacomp.getId());
        if (layoutComp != null && layoutComp.getParent() == null) {
            // remove only root components
            layoutModel.removeComponent(layoutComp.getId(), true);
        }
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

    static void setInModelRecursively(RADComponent metacomp, boolean inModel) {
        if (metacomp instanceof ComponentContainer) {
            RADComponent[] comps = ((ComponentContainer)metacomp).getSubBeans();
            for (int i=0; i < comps.length; i++)
                setInModelRecursively(comps[i], inModel);
        }
        metacomp.setInModel(inModel);
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

    private void startCompoundEdit() {
        if (compoundEdit == null) {
            t("starting compound edit"); // NOI18N
            compoundEdit = new CompoundEdit();
        }
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
    
    public void forceUndoOfCompoundEdit() {
        if (compoundEdit != null) {
            undoCompoundEdit = true;
        }
    }

    public boolean isCompoundEditInProgress() {
        return compoundEdit != null; // && compoundEdit.isInProgress();
    }

    public void addUndoableEdit(UndoableEdit edit) {
        t("adding undoable edit"); // NOI18N
        if (!isCompoundEditInProgress()) {
            startCompoundEdit();
        }
        compoundEdit.addEdit(edit);
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
    class UndoRedoManager extends UndoRedo.Manager {
        private Mutex.ExceptionAction<Object> runUndo = new Mutex.ExceptionAction<Object>() {
            public Object run() throws Exception {
                superUndo();
                return null;
            }
        };
        private Mutex.ExceptionAction<Object> runRedo = new Mutex.ExceptionAction<Object>() {
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

        @Override
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

        @Override
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
            listeners = new ArrayList<FormModelListener>();
        listeners.add(l);
    }

    public synchronized void removeFormModelListener(FormModelListener l) {
        if (listeners != null)
            listeners.remove(l);
    }

    /** Fires an event informing about that the form has been just loaded. */
    public void fireFormLoaded() {
        t("firing form loaded"); // NOI18N

        formLoaded = true;
        eventBroker = new EventBroker();
//        if (undoRedoManager != null)
//            undoRedoManager.discardAllEdits();
        if (!readOnly && !Boolean.getBoolean("netbeans.form.no_undo")) { // NOI18N
            undoRedoManager = new UndoRedoManager();
            undoRedoManager.setLimit(50);
            setUndoRedoRecording(true);
            if (layoutModel != null)
                layoutModel.setChangeRecording(true);
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
    {
        t("firing component removed: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null")); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.COMPONENT_REMOVED);
        ev.setRemoveData(metacomp, metacont, index, removedFromModel);
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

    public FormModelEvent fireBindingChanged(RADComponent metacomp,
                                             String path,
                                             MetaBinding oldValue,
                                             MetaBinding newValue)
    {
        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.BINDING_PROPERTY_CHANGED);
        ev.setComponentAndContainer(metacomp, null);
        ev.setProperty(path, oldValue, newValue);
        sendEvent(ev);

        if (undoRedoRecording && oldValue != newValue) {
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
    public FormModelEvent fireFormChanged(boolean immediately) {
        t("firing form change"); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.OTHER_CHANGE);
        if (immediately)
            sendEventImmediately(ev);
        else
            sendEvent(ev);

        return ev;
    }

    /**
     * Fires events collected from all changes done during the last round of AWT
     * event queue. After firing, if there was no error, all the changes are
     * placed as one UndoableEdit into undo/redo queue. When the fired events are
     * processed, some more changes may happen, but these should be fired
     * immeditalely (not later) to be included in the same UndoableEdit. This may
     * cause this method is re-entered while previous firing is not finished yet.
     * For robustness, if some error happens which would leave things in
     * incosistent state, all the changes done so far are undone:
     * If an operation failed before firing, the undoCompoundEdit field is set
     * and then no events are fired at all (the changes were defective), and the
     * changes done before the failure are undone. All the changes are undone
     * also if the failure happens during processing the events (e.g. the layout
     * can't be built).
     */
    private void fireEventBatch(FormModelEvent[] events) {
        if (!firing) {
            boolean firingFailed = false;
            try {
                firing = true;
                if (!undoCompoundEdit) {
                    firingFailed = true;
                    fireEvents(events);
                    firingFailed = false;
                }
            } finally {
                firing = false;
                boolean revert = undoCompoundEdit || firingFailed;
                undoCompoundEdit = false;
                CompoundEdit edit = endCompoundEdit(!revert);
                if (edit != null && revert) {
                    edit.undo();
                }
            }
        } else { // re-entrant call
            fireEvents(events);
        }
    }

    void fireEvents(FormModelEvent[] events) {
        java.util.List targets;
        synchronized(this) {
            if (listeners == null || listeners.size() == 0) {
                return;
            }
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
            broker.sendEvent(ev); // let the broker decide when to fire
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
        EventBroker broker = getEventBroker();
        if (broker != null)
            broker.sendEventImmediately(ev);
        else {
            t("no event broker, firing event directly: "+ev.getChangeType()); // NOI18N
            fireEvents(new FormModelEvent[] { ev });
        }
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
        private List<FormModelEvent> eventList;

        public void sendEvent(FormModelEvent ev) {
            if (shouldSendLater(ev))
                sendEventLater(ev);
            else
                sendEventImmediately(ev);
        }

        public void sendEventImmediately(FormModelEvent ev) {
            t("firing event directly from event broker: "+ev.getChangeType()); // NOI18N
            if (eventList == null)
                eventList = new ArrayList<FormModelEvent>();
            eventList.add(ev);
            run();
        }

        public void sendEventLater(FormModelEvent ev) {
            // works properly only if called from AWT event dispatch thread
            if (!java.awt.EventQueue.isDispatchThread()) {
                sendEventImmediately(ev);
                return;
            }

            if (eventList == null) {
                eventList = new ArrayList<FormModelEvent>();
                java.awt.EventQueue.invokeLater(this);
            }

            eventList.add(ev);
            t("event "+ev.getChangeType()+" added to queue in event broker"); // NOI18N
        }

        private boolean shouldSendLater(FormModelEvent ev) {
            return eventList != null || ev.isModifying();
        }

        private List<FormModelEvent> pickUpEvents() {
            List<FormModelEvent> list = eventList;
            eventList = null;
            return list;
        }

        public void run() {
            List<FormModelEvent> list = pickUpEvents();
            if (list != null && !list.isEmpty()) {
                FormModelEvent[] events = new FormModelEvent[list.size()];
                list.toArray(events);
                t("firing event batch of "+list.size()+" events from event broker"); // NOI18N
                FormModel.this.fireEventBatch(events);
            }
        }
    }

    // -------------

    public CodeStructure getCodeStructure() {
        return codeStructure;
    }
    
    public boolean isFreeDesignDefaultLayout() {
        return freeDesignDefaultLayout;
    }
    
    void setFreeDesignDefaultLayout(boolean freeDesignDefaultLayout) {
        this.freeDesignDefaultLayout = freeDesignDefaultLayout;
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
                components[perm[i]] = otherComponents.get(i);

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
