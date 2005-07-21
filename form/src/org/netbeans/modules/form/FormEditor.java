/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.awt.Cursor;
import java.beans.*;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.netbeans.spi.palette.PaletteController;

import org.openide.*;
import org.openide.awt.UndoRedo;
import org.openide.awt.StatusDisplayer;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.windows.*;

import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.form.project.ClassPathUtils;

/**
 * Form editor.
 *
 * @author Jan Stola
 */
public class FormEditor {
    static final int LOADING = 1;
    static final int SAVING = 2;

    /** The FormModel instance holding the form itself */
    private FormModel formModel;

    /** The root node of form hierarchy presented in Component Inspector */
    private FormRootNode formRootNode;

    /** The designer component - the last active designer of the form
     * (there can be more clones). May happen to be null if the active designer
     * was closed and no other designer of given form was activated since then. */
    private FormDesigner formDesigner;

    /** The code generator for the form */
    private CodeGenerator codeGenerator;

    /** List of exceptions occurred during the last persistence operation */
    private List persistenceErrors;
    
    /** Persistence manager responsible for saving the form */
    private PersistenceManager persistenceManager;
    
    /** An indicator whether the form has been loaded (from the .form file) */
    private boolean formLoaded = false;
    
    /** Table of opened FormModel instances (FormModel to FormEditor map) */
    private static Map openForms = new Hashtable();
    
    /** List of floating windows - must be closed when the form is closed. */
    private List floatingWindows;
    
    /** The DataObject of the form */
    private FormDataObject formDataObject;
    private PropertyChangeListener dataObjectListener;
    private static PropertyChangeListener settingsListener;
    private static PropertyChangeListener paletteListener;
    
    // listeners
    private FormModelListener formListener;
    
    FormEditor(FormDataObject formDataObject) {
        this.formDataObject = formDataObject;
    }

    /** @return root node representing the form (in pair with the class node) */
    public final Node getFormRootNode() {
        return formRootNode;
    }

    /** @return the FormModel of this form, null if the form is not loaded */
    public final FormModel getFormModel() {
        return formModel;
    }
    
    public final FormDataObject getFormDataObject() {
        return formDataObject;
    }
    
    CodeGenerator getCodeGenerator() {
        if (!formLoaded)
            return null;
        if (codeGenerator == null)
            codeGenerator = new JavaCodeGenerator();
        return codeGenerator;
    }
    
    boolean isFormLoaded() {
        return formLoaded;
    }
    
    /** This methods loads the form, reports errors, creates the FormDesigner */
    void loadFormDesigner() {
        JFrame mainWin = (JFrame) WindowManager.getDefault().getMainWindow();

        // set status text "Opening Form: ..."
        StatusDisplayer.getDefault().setStatusText(
            FormUtils.getFormattedBundleString(
                "FMT_OpeningForm", // NOI18N
                new Object[] { formDataObject.getFormFile().getName() }));
        javax.swing.RepaintManager.currentManager(mainWin).paintDirtyRegions();

        // set wait cursor [is not very reliable, but...]
        mainWin.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        mainWin.getGlassPane().setVisible(true);

        // load form data and report errors
        try {
            loadFormData();
        }
        catch (PersistenceException ex) {
            logPersistenceError(ex, 0);
        }

        // clear status text
        StatusDisplayer.getDefault().setStatusText(""); // NOI18N

        // clear wait cursor
        mainWin.getGlassPane().setVisible(false);
        mainWin.getGlassPane().setCursor(null);

        // report errors during loading
        reportErrors(LOADING);
    }
    
    boolean loadForm() {
        if (formLoaded)
            return true;

        if (java.awt.EventQueue.isDispatchThread()) {
            try {
                loadFormData();
            }
            catch (PersistenceException ex) {
                logPersistenceError(ex, 0);
            }
        }
        else { // loading must be done in AWT event dispatch thread
            try {
                java.awt.EventQueue.invokeAndWait(new Runnable() {
                    public void run() {
                        try {
                            loadFormData();
                        }
                        catch (PersistenceException ex) {
                            logPersistenceError(ex, 0);
                        }
                    }
                });
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return formLoaded;
    }
    
    /** This method performs the form data loading. All open/load methods go
     * through this one.
     */
    private void loadFormData() throws PersistenceException {
        if (formLoaded)
            return; // form already loaded

        resetPersistenceErrorLog(); // clear log of errors

        // first find PersistenceManager for loading the form
        persistenceManager = recognizeForm(formDataObject);

        // create and register new FormModel instance
        formModel = new FormModel();
        formModel.setName(formDataObject.getName());
        formModel.setReadOnly(formDataObject.isReadOnly());
        openForms.put(formModel, this);

        // load the form data (FormModel) and report errors
        synchronized(persistenceManager) {
            try {
                FormLAF.executeWithLookAndFeel(new Mutex.ExceptionAction() {
                    public Object run() throws Exception {
                        persistenceManager.loadForm(formDataObject,
                                                    formModel,
                                                    persistenceErrors);
                        return null;
                    }
                });
            }
            catch (PersistenceException ex) { // some fatal error occurred
                persistenceManager = null;
                openForms.remove(formModel);
                formModel = null;
                throw ex;
            }
            catch (Exception ex) { // should not happen, but for sure...
                ex.printStackTrace();
                persistenceManager = null;
                openForms.remove(formModel);
                formModel = null;
                return;
            }
        }

        // form is successfully loaded...
        formLoaded = true;
        getCodeGenerator().initialize(formModel);
        formModel.fireFormLoaded();

        // create form nodes hierarchy and add it to SourceChildren
        formRootNode = new FormRootNode(formModel);
        formRootNode.getChildren().getNodes();
        formDataObject.getNodeDelegate().getChildren()
                                          .add(new Node[] { formRootNode });

        attachFormListener();
        attachDataObjectListener();
        attachSettingsListener();
        attachPaletteListener();
    }
    
    /** Public method for saving form data to file. Does not save the
     * source code (document), does not report errors and does not throw
     * any exceptions.
     * @return whether there was not any fatal error during saving (true means
     *         everything was ok); returns true even if nothing was saved
     *         because form was not loaded or read-only, etc.
     */
    public boolean saveForm() {
        try {
            saveFormData();
            return true;
        }
        catch (PersistenceException ex) {
            logPersistenceError(ex, 0);
            return false;
        }
    }
    
    void saveFormData() throws PersistenceException {
        if (formLoaded && !formDataObject.formFileReadOnly()) {
            formModel.fireFormToBeSaved();

            resetPersistenceErrorLog();

            synchronized(persistenceManager) {
                persistenceManager.saveForm(formDataObject,
                                            formModel,
                                            persistenceErrors);
            }
        }
    }
    
    private void resetPersistenceErrorLog() {
        if (persistenceErrors != null)
            persistenceErrors.clear();
        else
            persistenceErrors = new ArrayList();
    }
    
    private void logPersistenceError(Throwable t, int index) {
        if (persistenceErrors == null)
            persistenceErrors = new ArrayList();

        if (index < 0)
            persistenceErrors.add(t);
        else
            persistenceErrors.add(index, t);
    }
    
    /** Finds PersistenceManager that can load and save the form.
     */
    private PersistenceManager recognizeForm(FormDataObject formDO)
        throws PersistenceException
    {
        Iterator it = PersistenceManager.getManagers();
        if (!it.hasNext()) { // there's no PersistenceManager available
            PersistenceException ex = new PersistenceException(
                                      "No persistence manager registered"); // NOI18N
            ErrorManager.getDefault().annotate(
                ex,
                ErrorManager.ERROR,
                null,
                FormUtils.getBundleString("MSG_ERR_NoPersistenceManager"), // NOI18N
                null,
                null);
            throw ex;
        }

        do {
            PersistenceManager pm = (PersistenceManager)it.next();
            synchronized(pm) {
                try {
                    if (pm.canLoadForm(formDO)) {
                        resetPersistenceErrorLog();
                        return pm;
                    }
                }
                catch (PersistenceException ex) {
                    logPersistenceError(ex);
                    // [continue on exception?]
                }
            }
        }
        while (it.hasNext());

        // no PersistenceManager is able to load the form
        PersistenceException ex;
        if (!anyPersistenceError()) {
            // no error occurred, the format is just unknown
            ex = new PersistenceException("Form file format not recognized"); // NOI18N
            ErrorManager.getDefault().annotate(
                ex,
                ErrorManager.ERROR,
                null,
                FormUtils.getBundleString("MSG_ERR_NotRecognizedForm"), // NOI18N
                null,
                null);
        }
        else { // some errors occurred when recognizing the form file format
            Throwable annotateT = null;
            int n = persistenceErrors.size();
            if (n == 1) { // just one exception occurred
                ex = (PersistenceException) persistenceErrors.get(0);
                Throwable t = ex.getOriginalException();
                annotateT = t != null ? t : ex;
                n = 0;
            }
            else { // there were more exceptions
                ex = new PersistenceException("Form file cannot be loaded"); // NOI18N
                annotateT = ex;
            }
            ErrorManager.getDefault().annotate(
                annotateT,
                FormUtils.getBundleString("MSG_ERR_LoadingErrors") // NOI18N
            );
            for (int i=0; i < n; i++) {
                PersistenceException pe = (PersistenceException)
                                          persistenceErrors.get(i);
                Throwable t = pe.getOriginalException();
                ErrorManager.getDefault().annotate(ex, (t != null ? t : pe));
            }
            // all the exceptions were attached to the main exception to
            // be thrown, so the log can be cleared
            resetPersistenceErrorLog();
        }
        throw ex;
    }

    private void logPersistenceError(Throwable t) {
        logPersistenceError(t, -1);
    }

    private boolean anyPersistenceError() {
        return persistenceErrors != null && !persistenceErrors.isEmpty();
    }
    
    /** Reports errors occurred during loading or saving the form.
     */
    public void reportErrors(int operation) {
        if (!anyPersistenceError())
            return; // no errors or warnings logged

        ErrorManager errorManager = ErrorManager.getDefault();

        boolean checkLoadingErrors = operation == LOADING && formLoaded;
        boolean anyNonFatalLoadingError = false; // was there a real error?

        for (Iterator it=persistenceErrors.iterator(); it.hasNext(); ) {
            Throwable t = (Throwable) it.next();
            if (t instanceof PersistenceException) {
                Throwable th = ((PersistenceException)t).getOriginalException();
                if (th != null)
                    t = th;
            }

            if (checkLoadingErrors && !anyNonFatalLoadingError) {
                // was there a real loading error (not just warnings) causing
                // some data not loaded?
                ErrorManager.Annotation[] annotations =
                                            errorManager.findAnnotations(t);
                int severity = 0;
                if (annotations != null) {
                    for (int i=0; i < annotations.length; i++) {
                        int s = annotations[i].getSeverity();
                        if (s == ErrorManager.UNKNOWN)
                            s = ErrorManager.EXCEPTION;
                        if (s > severity)
                            severity = s;
                    }
                }
                else severity = ErrorManager.EXCEPTION;

                if (severity > ErrorManager.WARNING)
                    anyNonFatalLoadingError = true;
            }

            errorManager.notify(t);
        }

        if (checkLoadingErrors && anyNonFatalLoadingError) {
            // the form was loaded with some non-fatal errors - some data
            // was not loaded - show a warning about possible data loss
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    // for some reason this would be displayed before the
                    // ErrorManager if not invoked later
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                        FormUtils.getBundleString("MSG_FormLoadedWithErrors"), // NOI18N
                        FormUtils.getBundleString("CTL_FormLoadedWithErrors"), // NOI18N
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.WARNING_MESSAGE,
                        new Object[] { NotifyDescriptor.OK_OPTION },
                        null));
                }
            });
        }

        resetPersistenceErrorLog();
    }
    
    /** @return the last activated FormDesigner for this form */
    FormDesigner getFormDesigner() {
        if (!formLoaded)
            return null;

        return formDesigner;
    }

    /** Called by FormDesigner when activated. */
    void setFormDesigner(FormDesigner designer) {
        formDesigner = designer;
    }
    
    /** Closes the form. Used when closing the form editor or reloading
     * the form. */
    void closeForm() {
        if (formLoaded) {
            formModel.fireFormToBeClosed();
            
            openForms.remove(formModel);
            formLoaded = false;
            
            // remove nodes hierarchy
            if (formDataObject.isValid()) {
                // Avoiding deadlock (issue 51796)
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        formDataObject.getNodeDelegate().getChildren()
                            .remove(new Node[] { formRootNode });
                        formRootNode = null;
                    }
                });
            }
            
            // remove listeners
            detachFormListener();
            detachDataObjectListener();
            if (openForms.isEmpty()) {
                ComponentInspector.getInstance().focusForm(null);
                detachSettingsListener();
                detachPaletteListener();
            }
            else { // still any opened forms - focus some
                FormEditor next = (FormEditor)openForms.values().iterator().next();
                ComponentInspector.getInstance().focusForm(next);
            }
            
            // close the floating windows
            if (floatingWindows != null) {
                if (floatingWindows.size() > 0) {
                    List tempList = new LinkedList(floatingWindows);
                    Iterator it = tempList.iterator();
                    while (it.hasNext()) {
                        java.awt.Window window = (java.awt.Window) it.next();
                        if (window.isVisible())
                            window.setVisible(false);
                    }
                }
                floatingWindows = null;
            }
            
            // reset references
            formDesigner = null;
            persistenceManager = null;
            persistenceErrors = null;
            formModel = null;
            codeGenerator = null;
        }
    }
    
    private void attachFormListener() {
        if (formListener != null || formDataObject.isReadOnly())
            return;

        // this listener ensures necessary updates of nodes according to
        // changes in containers in form
        formListener = new FormModelListener() {
            public void formChanged(FormModelEvent[] events) {
                boolean modifying = false;
                Set changedContainers = events.length > 0 ?
                                          new HashSet() : null;
                Set compsToSelect = null;
                FormNode nodeToSelect = null;

                for (int i=0; i < events.length; i++) {
                    FormModelEvent ev = events[i];

                    if (ev.isModifying())
                        modifying = true;

                    int type = ev.getChangeType();
                    if (type == FormModelEvent.CONTAINER_LAYOUT_EXCHANGED
                        || type == FormModelEvent.CONTAINER_LAYOUT_CHANGED
                        || type == FormModelEvent.COMPONENT_ADDED
                        || type == FormModelEvent.COMPONENT_REMOVED
                        || type == FormModelEvent.COMPONENTS_REORDERED)
                    {
                        ComponentContainer cont = ev.getContainer();
                        if (changedContainers == null
                            || !changedContainers.contains(cont))
                        {
                            updateNodeChildren(cont);
                            if (changedContainers != null)
                                changedContainers.add(cont);
                        }

                        if (type == FormModelEvent.COMPONENT_REMOVED) {
                            FormNode select;
                            if (cont instanceof RADComponent)
                                select = ((RADComponent)cont).getNodeReference();
                            else
                                select = formRootNode.getOthersNode();

                            if (!(nodeToSelect instanceof RADComponentNode)) {
                                if (nodeToSelect != formRootNode)
                                    nodeToSelect = select;
                            }
                            else if (nodeToSelect != select)
                                nodeToSelect = formRootNode;
                        }
                        else if (type == FormModelEvent.CONTAINER_LAYOUT_EXCHANGED) {
                            nodeToSelect = ((RADVisualContainer)cont)
                                                .getLayoutNodeReference();
                        }
                        else if (type == FormModelEvent.COMPONENT_ADDED
                                 && ev.getComponent().isInModel())
                        {
                            if (compsToSelect == null)
                                compsToSelect = new HashSet();

                            compsToSelect.add(ev.getComponent());
                            compsToSelect.remove(ev.getContainer());
                        }
                    }
                }

                FormDesigner designer = getFormDesigner();
                if (designer != null) {
                    if (compsToSelect != null) {
                        designer.clearSelectionImpl();
                        for (Iterator it=compsToSelect.iterator(); it.hasNext(); ) {
                            designer.addComponentToSelectionImpl((RADComponent)it.next());
                        }
                        designer.updateComponentInspector();
//                        RADComponent[] comps =
//                            new RADComponent[compsToSelect.size()];
//                        compsToSelect.toArray(comps);
//                        designer.setSelectedComponents(comps);
                    }
                    else if (nodeToSelect != null)
                        designer.setSelectedNode(nodeToSelect);
                }

                if (modifying)  { // mark the form document modified explicitly
                    FormDataObject formDO = getFormDataObject();
                    formDO.getFormEditorSupport().markFormModified();
                }
            }
        };

        formModel.addFormModelListener(formListener);
    }

    private void detachFormListener() {
        if (formListener != null) {
            formModel.removeFormModelListener(formListener);
            formListener = null;
        }
    }
    
    /** Updates (sub)nodes of a container (in Component Inspector) after
     * a change has been made (like component added or removed). */
    void updateNodeChildren(ComponentContainer metacont) {
        FormNode node;

        if (metacont == null || metacont == formModel.getModelContainer())
            node = formRootNode != null ?
                       formRootNode.getOthersNode() : null;

        else if (metacont instanceof RADComponent)
            node = ((RADComponent)metacont).getNodeReference();

        else node = null;

        if (node != null)
            node.updateChildren();
    }
    
    private void attachDataObjectListener() {
        if (dataObjectListener != null)
            return;

        dataObjectListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                if (DataObject.PROP_NAME.equals(ev.getPropertyName())) {
                    // FormDataObject's name has changed
                    String name = formDataObject.getName();
                    formModel.setName(name);
                    formRootNode.updateName(name);
                    FormDataObject formDO = getFormDataObject();
                    formDO.getFormEditorSupport().updateMVTCToolTipText();
                        // display name is set via notifyModified/notifyUnmodified
                    formModel.fireFormChanged(); // regenerate code
                }
                else if (DataObject.PROP_COOKIE.equals(ev.getPropertyName())) {
                    java.awt.EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            Node[] nodes = ComponentInspector.getInstance()
                                     .getExplorerManager().getSelectedNodes();
                            for (int i=0; i < nodes.length; i++)
                                ((FormNode)nodes[i]).updateCookies();
                        }
                    });
                }
            }
        };

        formDataObject.addPropertyChangeListener(dataObjectListener);
    }

    private void detachDataObjectListener() {
        if (dataObjectListener != null) {
            formDataObject.removePropertyChangeListener(dataObjectListener);
            dataObjectListener = null;
        }
    }
    
    private static void attachSettingsListener() {
        if (settingsListener != null)
            return;

        settingsListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Iterator iter = openForms.keySet().iterator();
                while (iter.hasNext()) {
                    FormModel formModel = (FormModel) iter.next();
                    String propName = evt.getPropertyName();

                    if (FormLoaderSettings.PROP_USE_INDENT_ENGINE.equals(propName)
                        || FormLoaderSettings.PROP_GENERATE_ON_SAVE.equals(propName))
                    {
                        formModel.fireSyntheticPropertyChanged(null, propName,
                                        evt.getOldValue(), evt.getNewValue());
                    }
                    else if (FormLoaderSettings.PROP_SELECTION_BORDER_SIZE.equals(propName)
                          || FormLoaderSettings.PROP_SELECTION_BORDER_COLOR.equals(propName)
                          || FormLoaderSettings.PROP_CONNECTION_BORDER_COLOR.equals(propName)
                          || FormLoaderSettings.PROP_FORMDESIGNER_BACKGROUND_COLOR.equals(propName)
                          || FormLoaderSettings.PROP_FORMDESIGNER_BORDER_COLOR.equals(propName))
                    {
                        FormDesigner designer = getFormDesigner(formModel);
                        if (designer != null)
                            designer.updateVisualSettings();
                        // PENDING all cloned designers for given form should be updated
                    }
                    else if (FormLoaderSettings.PROP_PALETTE_IN_TOOLBAR.equals(propName))
                    {
                        FormDesigner designer = getFormDesigner(formModel);
                        if (designer != null)
                            designer.getFormToolBar().showPaletteButton(
                                FormLoaderSettings.getInstance().isPaletteInToolBar());
                        // PENDING all cloned designers for given form should be updated
                    }
                }
            }
        };

        FormLoaderSettings.getInstance().addPropertyChangeListener(settingsListener);
    }

    private static void detachSettingsListener() {
        if (settingsListener != null) {
            FormLoaderSettings.getInstance()
                    .removePropertyChangeListener(settingsListener);
            settingsListener = null;
        }
    }
    
    private static void attachPaletteListener() {
        if (paletteListener != null)
            return;

        paletteListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (PaletteController.PROP_SELECTED_ITEM.equals(evt.getPropertyName())) {
                    Iterator iter = openForms.keySet().iterator();
                    while (iter.hasNext()) {
                        FormModel formModel = (FormModel)iter.next();
                        FormDesigner designer = getFormDesigner(formModel);
                        if (designer != null) {
                            // PENDING should be done for all cloned designers
                            if (evt.getNewValue() == null) {
                                if (designer.getDesignerMode() == FormDesigner.MODE_ADD)
                                    designer.setDesignerMode(FormDesigner.MODE_SELECT);
                            } else {
                                if (designer.getDesignerMode() == FormDesigner.MODE_ADD) {
                                    // Change in the selected palette item means unselection
                                    // of the old item and selection of the new one
                                    designer.setDesignerMode(FormDesigner.MODE_SELECT);
                                }
                                designer.setDesignerMode(FormDesigner.MODE_ADD);
                            }
                            // TODO activate current designer?
                        }
                    }
                }
            }
        };

        PaletteUtils.getPalette().addPropertyChangeListener(paletteListener);
    }

    private static void detachPaletteListener() {
        if (paletteListener != null) {
            PaletteUtils.getPalette().removePropertyChangeListener(paletteListener);
            paletteListener = null;
        }
    }

    void reinstallListener() {
        if (formListener != null) {
            formModel.removeFormModelListener(formListener);
            formModel.addFormModelListener(formListener);
        }
    }
    
    /** @return FormDesigner for given form */
    public static FormDesigner getFormDesigner(FormModel formModel) {
        FormEditor formEditor = (FormEditor) openForms.get(formModel);
        return formEditor != null ? formEditor.getFormDesigner() : null;
    }

    /** @return CodeGenerator for given form */
    public static CodeGenerator getCodeGenerator(FormModel formModel) {
        FormEditor formEditor = (FormEditor) openForms.get(formModel);
        return formEditor != null ? formEditor.getCodeGenerator() : null;
    }

    /** @return FormDataObject of given form */
    public static FormDataObject getFormDataObject(FormModel formModel) {
        FormEditor formEditor = (FormEditor) openForms.get(formModel);
        return formEditor != null ? formEditor.getFormDataObject() : null;
    }
    
    /** @return FormEditor instance for given form */
    public static FormEditor getFormEditor(FormModel formModel) {
        return (FormEditor) openForms.get(formModel);
    }
    
    UndoRedo.Manager getFormUndoRedoManager() {
        return formModel != null ? formModel.getUndoRedoManager() : null;
    }
    
    public void registerFloatingWindow(java.awt.Window window) {
        if (floatingWindows == null)
            floatingWindows = new ArrayList();
        else
            floatingWindows.remove(window);
        floatingWindows.add(window);
    }

    public void unregisterFloatingWindow(java.awt.Window window) {
        if (floatingWindows != null)
            floatingWindows.remove(window);
    }

    /**
     * Updates project classpath with the layout extensions library.
     */
    public boolean updateProjectForNaturalLayout() {
        try {
            // [TODO maybe we should check if the library isn't there already]
            ClassSource cs = new ClassSource("org.jdesktop.layout.*", // class name actually not needed // NOI18N
                                             new String[] { ClassSource.LIBRARY_SOURCE },
                                             new String[] { "layoutext" }); // NOI18N
            return ClassPathUtils.updateProject(formDataObject.getFormFile(), cs);
        }
        catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return false;
    }

    public static boolean isNaturalLayoutEnabled() {
        return Boolean.getBoolean("netbeans.form.new_layout"); // NOI18N
    }

    public static boolean isNonVisualTrayEnabled() {
        return isNaturalLayoutEnabled() || Boolean.getBoolean("netbeans.form.non_visual_tray"); // NOI18N
    }
}
