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

import java.beans.*;
import java.io.IOException;
import java.util.*;
import java.awt.Cursor;
import javax.swing.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.awt.*;
import org.openide.cookies.*;
import org.openide.loaders.*;
import org.openide.util.*;
import org.openide.windows.*;

import org.netbeans.modules.java.JavaEditor;
import org.netbeans.modules.form.palette.*;

/**
 *
 * @author Ian Formanek, Tomas Pavek
 */

public class FormEditorSupport extends JavaEditor
{
    static final String NO_WORKSPACE = "None"; // NOI18N

    private static final int LOADING = 1;
    private static final int SAVING = 2;

    /** The FormModel instance holding the form itself */
    private FormModel formModel;

    /** The DataObject of the form */
    private FormDataObject formDataObject;

    /** The root node of form hierarchy presented in Component Inspector */
    private FormRootNode formRootNode;

    /** The designer of the form */
    private FormDesigner formDesigner;

    /** List of floating windows - must be closed when the form is closed. */
    private ArrayList floatingWindows;

    /** The code generator for the form */
//    private CodeGenerator codeGenerator;

    /** Persistence manager responsible for saving the form */
    private PersistenceManager persistenceManager;

    /** List of exceptions occurred during the last persistence operation */
    private ArrayList persistenceErrors;

    /** An indicator whether the form has been loaded (from the .form file) */
    private boolean formLoaded = false; 

    /** An indicator whether the form should be opened additionally when
     * switching back from a non-editing workspace to editing one */
    private boolean openOnEditing = false;

    // listeners
    private FormModelListener formListener;
    private static PropertyChangeListener workspacesListener;
    private static PropertyChangeListener settingsListener;
    private static PropertyChangeListener topcompsListener;
    private static PropertyChangeListener paletteListener;

    private UndoRedo.Manager editorUndoManager;

    /** Table of FormModel instances (FormModel to FormEditorSupport map) */
    private static Hashtable openForms = new Hashtable();

    static ClassLoader userClassLoader;

    // --------------
    // constructor

    public FormEditorSupport(MultiDataObject.Entry javaEntry,
                             FormDataObject formDataObject) {
        super(javaEntry);
        this.formDataObject = formDataObject;
    }

    // ----------
    // opening & saving interface methods

    /** Opens the form (loads it first if not loaded yet).
     * @see OpenCookie#open
     */
    public void openForm() {
        // set status text "Opening Form: ..."
        StatusDisplayer.getDefault().setStatusText(
            FormUtils.getFormattedBundleString(
                "FMT_OpeningForm", // NOI18N
                new Object[] { formDataObject.getPrimaryFile().getNameExt() }));

        // switch to GUI workspace
        final boolean openGui = activateWorkspace();

        // open java editor
        open();

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame mainWin = (JFrame) WindowManager.getDefault().getMainWindow();

                // set status text "Opening Form: ..."
                StatusDisplayer.getDefault().setStatusText(
                    FormUtils.getFormattedBundleString(
                        "FMT_OpeningForm", // NOI18N
                        new Object[] { formDataObject.getFormFile().getNameExt() }));
                RepaintManager.currentManager(mainWin).paintDirtyRegions();

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

                // open form designer (if loading successful and workspace active)
                if (formLoaded)
                    if (openGui)
                        openGUI();
                    else
                        ComponentInspector.getInstance().focusForm(FormEditorSupport.this);

                // clear status text
                StatusDisplayer.getDefault().setStatusText(""); // NOI18N

                // clear wait cursor
                mainWin.getGlassPane().setVisible(false);
                mainWin.getGlassPane().setCursor(null);

                // report errors during loading
                reportErrors(LOADING);
            }
        });
    }

    /** Public method for loading form data from file. Does not open the
     * source editor and designer, does not report errors and does not throw
     * any exceptions. Runs in AWT event dispatch thread, returns after the
     * form is loaded (even if not called from AWT thread). 
     & @return whether the form is loaded (true also if it already was)
     */
    public boolean loadForm() {
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

    /** @return true if the form is opened, false otherwise */
    public boolean isOpened() {
        return formLoaded;
    }

    /** Save the document in this thread and start reparsing it.
     * @exception IOException on I/O error
     */
    public void saveDocument() throws IOException {
        IOException ioEx = null;
        try {
            saveFormData();
            super.saveDocument();
        }
        catch (PersistenceException ex) {
            Throwable t = ex.getOriginalException();
            if (t instanceof IOException)
                ioEx = (IOException) t;
            else {
                ioEx = new IOException("Cannot save the form"); // NOI18N
                ErrorManager.getDefault().annotate(ioEx, t != null ? t : ex);
            }
        }
        reportErrors(SAVING);

        if (ioEx != null)
            throw ioEx;
    }

    /** Save the document in this thread.
     * @param parse true if the parser should be started, otherwise false
     * @exception IOException on I/O error
     */
    protected void saveDocumentIfNecessary(boolean parse) throws IOException {
        IOException ioEx = null;
        try {
            saveFormData();
            super.saveDocumentIfNecessary(parse);
        }
        catch (PersistenceException ex) {
            Throwable t = ex.getOriginalException();
            if (t instanceof IOException)
                ioEx = (IOException) t;
            else {
                ioEx = new IOException("Cannot save the form"); // NOI18N
                ErrorManager.getDefault().annotate(ioEx, t != null ? t : ex);
            }
        }
        reportErrors(SAVING);

        if (ioEx != null)
            throw ioEx;
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

    // ------------
    // other interface methods

    /** @return data object representing the form */
    public final FormDataObject getFormDataObject() {
        return formDataObject;
    }

    /** @return root node representing the form (in pair with the class node) */
    public final Node getFormRootNode() {
        return formRootNode;
    }

    /** @return the FormModel of this form, null if the form is not loaded */
    public final FormModel getFormModel() {
        return formModel;
    }

    /** @return errors occurred during last form loading or saving operation */
    public Throwable[] getPersistenceErrors() {
        if (!anyPersistenceError())
            return new Throwable[0];

        Throwable[] errors = new Throwable[persistenceErrors.size()];
        persistenceErrors.toArray(errors);
        return errors;
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

    /** @return the FormDesigner for this form */
    FormDesigner getFormDesigner() {
        if (!formLoaded)
            return null;

        if (formDesigner == null) {
            formDesigner = new FormDesigner(formModel);
            // not very nice hack - it's better FormEditorSupport has its
            // listener registered after FormDesigner
            if (formListener != null) {
                formModel.removeFormModelListener(formListener);
                formModel.addFormModelListener(formListener);
            }
        }
        else if (formDesigner.getModel() == null)
            formDesigner.setModel(formModel);

        return formDesigner;
    }

    void setFormDesigner(FormDesigner formDesigner) {
        this.formDesigner = formDesigner;
    }

//    CodeGenerator getCodeGenerator() {
//        if (!formLoaded)
//            return null;
//        if (codeGenerator == null)
//            codeGenerator = new JavaCodeGenerator();
//        return codeGenerator;
//    }

    /** Marks the form as modified if it's not yet. Used if changes made 
     * in form data don't affect the java source file (generated code). */
    void markFormModified() {
        if (formLoaded && !formDataObject.isModified())
            super.notifyModified();
    }

    void updateFormName(String name) {
        if (!formLoaded)
            return;

        formModel.setName(name);
        formModel.getFormDesigner().updateName(name);
        formRootNode.updateName(name);
        formModel.fireFormChanged();
    }

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

    protected UndoRedo.Manager createUndoRedoManager() {
        editorUndoManager = super.createUndoRedoManager();
        return editorUndoManager;
    }

    void discardEditorUndoableEdits() {
        if (editorUndoManager != null)
            editorUndoManager.discardAllEdits();
    }

    UndoRedo.Manager getFormUndoRedoManager() {
        return formModel != null ? formModel.getUndoRedoManager() : null;
    }

    // ------------
    // static getters

    /** @return an array of all opened forms */
    public static FormModel[] getOpenedForms() {
        synchronized(openForms) {
            Collection forms = openForms.values();
            ArrayList list = new ArrayList(forms.size());
            for (Iterator it=forms.iterator(); it.hasNext(); ) {
                FormEditorSupport fes = (FormEditorSupport) it.next();
                if (fes.formLoaded)
                    list.add(fes.getFormModel());
            }

            FormModel[] formsArray = new FormModel[list.size()];
            list.toArray(formsArray);
            return formsArray;
        }
    }

    /** @return FormDesigner for given form */
    public static FormDesigner getFormDesigner(FormModel formModel) {
        FormEditorSupport fes = (FormEditorSupport) openForms.get(formModel);
        return fes != null ? fes.getFormDesigner() : null;
    }

    /** @return CodeGenerator for given form */
//    public static CodeGenerator getCodeGenerator(FormModel formModel) {
//        FormEditorSupport fes = (FormEditorSupport) openForms.get(formModel);
//        return fes != null ? fes.getCodeGenerator() : null;
//    }

    /** @return FormDataObject of given form */
    public static FormDataObject getFormDataObject(FormModel formModel) {
        FormEditorSupport fes = (FormEditorSupport) openForms.get(formModel);
        return fes != null ? fes.getFormDataObject() : null;
    }

    /** @return FormEditorSupport instance for given form */
    public static FormEditorSupport getSupport(FormModel formModel) {
        return (FormEditorSupport) openForms.get(formModel);
    }

    // ------------
    // loading

    /** Works around a Jikes bug which results into bad bytecode if
     * OuterClass.super.open() is called from inside an inner class.
     * Instead of super.open() Jikes generates bytecode for open() which
     * leads to an infinite recursion
     */
    private void superOpen() {
        super.open();
    }

    private void openForm(boolean dontSwitchWS,
                          final boolean openGui)
    {
        if (!dontSwitchWS)
            activateWorkspace();

        open(); // open java editor

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    loadFormData();
                }
                catch (PersistenceException ex) {
                    logPersistenceError(ex, 0);
                }

                if (formLoaded)
                    if (openGui)
                        openGUI();
                    else
                        ComponentInspector.getInstance().focusForm(FormEditorSupport.this);

                reportErrors(LOADING);
            }
        });
    }

    /** This method performs the form data loading.
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
//        getCodeGenerator().initialize(formModel);
        formModel.fireFormLoaded();

        // create form nodes hierarchy and add it to SourceChildren
        formRootNode = new FormRootNode(formModel);
        formRootNode.getChildren().getNodes();
        formDataObject.getNodeDelegate().getChildren()
                                          .add(new Node[] { formRootNode });

        attachFormListener();
        attachSettingsListener();
        attachTopComponentsListener();
        attachPaletteListener();
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

    private void logPersistenceError(Throwable t, int index) {
        if (persistenceErrors == null)
            persistenceErrors = new ArrayList();

        if (index < 0)
            persistenceErrors.add(t);
        else
            persistenceErrors.add(index, t);
    }

    private void resetPersistenceErrorLog() {
        if (persistenceErrors != null)
            persistenceErrors.clear();
        else
            persistenceErrors = new ArrayList();
    }

    private boolean anyPersistenceError() {
        return persistenceErrors != null && !persistenceErrors.isEmpty();
    }

    /** Activates GUI Editing workspace (on certain conditions). Returns
     * whether the workspace was activated.
     */
    private boolean activateWorkspace() {
        attachWorkspacesListener();

        String currentWSName = WindowManager.getDefault()
                                              .getCurrentWorkspace().getName();
        String formWSName = FormEditor.getFormSettings().getWorkspace();

        if ("Editing".equals(currentWSName) // NOI18N
            || "Visual".equals(currentWSName) // NOI18N
            || formWSName.equals(NO_WORKSPACE) // no extra workspace needed
            || formWSName.equals(currentWSName))
        {
            openOnEditing = false;
            if (!formWSName.equals(currentWSName)
                && !formWSName.equals(NO_WORKSPACE))
            {   // switch to the form main workspace
                Workspace formWorkspace =
                    WindowManager.getDefault().findWorkspace(formWSName);
                if (formWorkspace != null)
                    formWorkspace.activate();
            }
            return true;
        }
        else {
            // if this is not an editing workspace, do not open the designer
            openOnEditing = true;
            return false;
        }
    }

    /** Opens FormDesigner and ComponentInspector.
     */
    synchronized private void openGUI() {
        // open FormDesigner
        FormDesigner designer = getFormDesigner();
        if (designer == null)
            return;
        designer.initialize();
        designer.open();

        // open ComponentInspector
        ComponentInspector.getInstance().open();
        ComponentInspector.getInstance().focusForm(this, true);

        // open ComponentPalette
        PaletteTopComponent.getInstance().open();

        // bring the FormDesigner to front and give it the focus
        designer.requestFocus();
    }

    // -----------
    // saving

    private void saveFormData() throws PersistenceException {
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

    // -----------
    // closing/reloading

    protected void notifyClosed() {
        super.notifyClosed();
        if (formLoaded)
            closeForm();
    }

    public void reloadForm() {
        if (canClose())
            reloadDocument();
    }

    protected org.openide.util.Task reloadDocumentTask() {
        boolean reloadForm = formLoaded;
        boolean guiWasOpened = formDesigner != null
            && formDesigner.isOpened(WindowManager.getDefault().getCurrentWorkspace());

        if (formLoaded)
            closeForm();

        org.openide.util.Task docLoadTask = super.reloadDocumentTask();

        if (reloadForm)
            openForm(true, guiWasOpened);

        return docLoadTask;
    }

    /** Closes the form. Used when closing or reloading the document.
     */
    synchronized private void closeForm() {
        formModel.fireFormToBeClosed();

        openForms.remove(formModel);
        formLoaded = false;

        // remove nodes hierarchy
        if (formDataObject.isValid())
            formDataObject.getNodeDelegate().getChildren()
                                        .remove(new Node[] { formRootNode });

        // remove listeners
        detachFormListener();
        if (openForms.isEmpty()) {
            if (formDesigner != null)
                formDesigner.setModel(null);
            ComponentInspector.getInstance().focusForm(null, false);
            detachWorkspacesListener();
            detachSettingsListener();
            detachTopComponentsListener();
            detachPaletteListener();

            TopComponent palette = PaletteTopComponent.getInstance();
            palette.setCloseOperation(TopComponent.CLOSE_EACH);
            palette.close();

            userClassLoader = null;
        }
        else { // still any opened forms - focus some
            FormEditorSupport next = (FormEditorSupport)
                                     openForms.values().iterator().next();
            ComponentInspector.getInstance().focusForm(next);
        }

        // close the floating windows
        if (floatingWindows != null) {
            if (floatingWindows.size() > 0) {
                Iterator it = ((List)floatingWindows.clone()).iterator();
                while (it.hasNext()) {
                    java.awt.Window window = (java.awt.Window) it.next();
                    if (window.isVisible())
                        window.setVisible(false);
                }
            }
            floatingWindows = null;
        }

        // close the designer
        if (formDesigner != null) {
            formDesigner.setCloseOperation(TopComponent.CLOSE_EACH);
            formDesigner.close();
            formDesigner = null;
        }

        // reset references
        formRootNode = null;
        formDesigner = null;
        persistenceManager = null;
        persistenceErrors = null;
        formModel = null;
    }

    // called by FormDesigner when it is about to close
    void designerToBeClosed(Workspace ws) {
        if (formDesigner == null)
            return;

        if (ws == null)
            ws = WindowManager.getDefault().getCurrentWorkspace();
        Mode mode = ws.findMode(formDesigner);
        if (mode != null && FormDesigner.FORM_MODE_NAME.equals(mode.getName())) {
            TopComponent inspector = null;
            TopComponent palette = null;
            TopComponent[] modeComponents = mode.getTopComponents();
            for (int i=0; i < modeComponents.length; i++) {
                TopComponent tc = modeComponents[i];
                if (tc.isOpened(ws))
                    if (tc instanceof FormDesigner) {
                        if (tc != formDesigner) { // other designers still opened
                            palette = null;
                            inspector = null;
                            break;
                        }
                    }
                    else if (tc instanceof ComponentInspector)
                        inspector = tc;
                    else if (tc instanceof PaletteTopComponent)
                        palette = tc;
            }

            if (inspector != null) {
                inspector.setCloseOperation(TopComponent.CLOSE_LAST);
                inspector.close(ws);
            }
            if (palette != null) {
                palette.setCloseOperation(TopComponent.CLOSE_LAST);
                palette.close(ws);
            }
        }
    }

    // -----------
    // listeners

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

                if (formDesigner != null)
                    if (compsToSelect != null) {
                        RADComponent[] comps =
                            new RADComponent[compsToSelect.size()];
                        compsToSelect.toArray(comps);
                        formDesigner.setSelectedComponents(comps);
                    }
                    else if (nodeToSelect != null)
                        formDesigner.setSelectedNode(nodeToSelect);

                if (modifying) // mark the form document modified explicitly
                    markFormModified();
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

    private static void attachWorkspacesListener() {
        if (workspacesListener != null)
            return;

        workspacesListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (!WindowManager.PROP_CURRENT_WORKSPACE.equals(
                                           evt.getPropertyName()))
                    return;

                Workspace ws = WindowManager.getDefault().getCurrentWorkspace();
                if (ws == null)
                    return; // [can it even be null?]

                String currentWSName = ws.getName();
                String formWSName = FormEditor.getFormSettings()
                                                   .getWorkspace();

                if ("Editing".equals(currentWSName) // NOI18N
                    || "Visual".equals(currentWSName) // NOI18N
                    || formWSName.equals(NO_WORKSPACE) // no extra workspace for forms
                    || formWSName.equals(currentWSName))
                {   // if switched to a workspace usable for form editor then
                    // look for forms waiting for opening their designers
                    boolean anyWaitingForm = false;
                    Collection forms = openForms.values();
                    for (Iterator it=forms.iterator(); it.hasNext(); ) {
                        FormEditorSupport fes = (FormEditorSupport) it.next();
                        if (fes.openOnEditing) {
                            fes.openOnEditing = false;
                            fes.openGUI();
                            anyWaitingForm = true;
                        }
                    }
                    if (anyWaitingForm)
                        return;
                }

                // do nothing if switched to the main form workspace
                if (formWSName.equals(currentWSName)
                        && !formWSName.equals(NO_WORKSPACE))
                    return;
                
                // refresh opened designers on this workspace
                Mode formMode = ws.findMode("Form"); // NOI18N
                if (formMode == null)
                    return; // no form mode on this workspace

                boolean modeOpened = false;
                TopComponent[] comps = formMode.getTopComponents();
                for (int i=0; i < comps.length; i++)
                    if (comps[i].isOpened(ws)) {
                        modeOpened = true;
                        break;
                    }

                if (modeOpened) { // form editor is opened on this workspace
                    Collection forms = openForms.values();
                    for (Iterator it=forms.iterator(); it.hasNext(); ) {
                        FormEditorSupport fes = (FormEditorSupport) it.next();
                        fes.getFormDesigner().open(ws);
                    }
                }
            }
        };

        WindowManager.getDefault().addPropertyChangeListener(workspacesListener);
    }

    private static void detachWorkspacesListener() {
        if (workspacesListener != null) {
            WindowManager.getDefault().removePropertyChangeListener(workspacesListener);
            workspacesListener = null;
        }
    }

    private static void attachSettingsListener() {
        if (settingsListener != null)
            return;

        settingsListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Enumeration enum = openForms.keys();
                while (enum.hasMoreElements()) {
                    FormModel formModel = (FormModel)enum.nextElement();
                    String propName = evt.getPropertyName();

                    if (FormLoaderSettings.PROP_USE_INDENT_ENGINE.equals(propName)
                        || FormLoaderSettings.PROP_GENERATE_ON_SAVE.equals(propName)
                        || FormLoaderSettings.PROP_VARIABLES_MODIFIER.equals(propName)
                        || FormLoaderSettings.PROP_VARIABLES_LOCAL.equals(propName)
                        || FormLoaderSettings.PROP_LISTENER_GENERATION_STYLE.equals(propName))
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
                        formModel.getFormDesigner().updateVisualSettings();
                    }
                }
            }
        };

        FormEditor.getFormSettings().addPropertyChangeListener(settingsListener);
    }

    private static void detachSettingsListener() {
        if (settingsListener != null) {
            FormEditor.getFormSettings()
                    .removePropertyChangeListener(settingsListener);
            settingsListener = null;
        }
    }

    private static void attachTopComponentsListener() {
        if (topcompsListener != null)
            return;

        topcompsListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                // we are interested in changing active TopComponent
                if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
                    TopComponent tc = TopComponent.getRegistry().getActivated();
                    if (!(tc instanceof JavaEditor.JavaEditorComponent))
                        return;

                    Node[] nodes = tc.getActivatedNodes();
                    if (nodes == null || nodes.length != 1)
                        return;

                    SourceCookie srcCookie =
                        (SourceCookie) nodes[0].getCookie(SourceCookie.class);
                    if (srcCookie == null)
                        return;

                    DataObject dobj = (DataObject)
                        srcCookie.getSource().getCookie(DataObject.class);
                    if (dobj == null)
                        return;

                    FormEditorSupport selectedForm =
                        ComponentInspector.getInstance().getFocusedForm();

                    Iterator it = openForms.values().iterator();
                    while (it.hasNext()) {
                        FormEditorSupport fes = (FormEditorSupport) it.next();
                        if (fes.getFormDataObject() == dobj) {
                            if (fes != selectedForm) {
                                fes.gotoForm();
                                ComponentInspector.getInstance().focusForm(fes);
                            }
                            break;
                        }
                    }
                }
            }
        };

        TopComponent.getRegistry().addPropertyChangeListener(topcompsListener);
    }

    private static void detachTopComponentsListener() {
        if (topcompsListener != null) {
            TopComponent.getRegistry()
                    .removePropertyChangeListener(topcompsListener);
            topcompsListener = null;
        }
    }

    private static void attachPaletteListener() {
        if (paletteListener != null)
            return;

        paletteListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (CPManager.PROP_MODE.equals(evt.getPropertyName())
                    && new Integer(PaletteAction.MODE_ADD).equals(
                                                           evt.getNewValue()))
                {
                    FormDesigner designer =
                        ComponentInspector.getInstance()
                            .getFocusedForm().getFormDesigner();
                    designer.requestFocus();
                    designer.componentActivated();
                }
            }
        };

        CPManager.getDefault().addPropertyChangeListener(paletteListener);
    }

    private static void detachPaletteListener() {
        if (paletteListener != null) {
            CPManager.getDefault().removePropertyChangeListener(paletteListener);
            paletteListener = null;
        }
    }

    // ----------

    public void gotoEditor() {
        super.open();
    }

    public void gotoForm() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (formModel != null && formModel.getFormDesigner().isOpened())
                    formModel.getFormDesigner().requestVisible();
            }
        });
    }
}
