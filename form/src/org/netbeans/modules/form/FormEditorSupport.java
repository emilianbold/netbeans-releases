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
import java.util.*;
import java.awt.EventQueue;
import java.awt.Cursor;
import java.io.*;
import javax.swing.*;
import javax.swing.text.Document;

import org.openide.*;
import org.openide.nodes.Node;
import org.openide.awt.UndoRedo;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.util.Mutex;
import org.openide.windows.*;
import org.openide.text.*;
import org.openide.util.Utilities;
import org.netbeans.core.spi.multiview.*;
import org.netbeans.core.api.multiview.*;

import org.netbeans.modules.java.JavaEditor;
import org.netbeans.modules.java.parser.JavaParser;
import org.netbeans.modules.form.palette.CPManager;

/**
 *
 * @author Ian Formanek, Tomas Pavek
 */

public class FormEditorSupport extends JavaEditor
{
    private static final int LOADING = 1;
    private static final int SAVING = 2;

    /** ID of the form designer (in the multiview) */
    private static final String MV_FORM_ID = "form"; //NOI18N
    /** ID of the java editor (in the multiview) */
    private static final String MV_JAVA_ID = "java"; // NOI18N

    private static final int JAVA_ELEMENT_INDEX = 0;
    private static final int FORM_ELEMENT_INDEX = 1;
    private int elementToOpen; // default element index when multiview TC is created

    /** Icon for the form editor multiview window */
    private static final String iconURL =
        "org/netbeans/modules/form/resources/form.gif"; // NOI18N

    /** The FormModel instance holding the form itself */
    private FormModel formModel;

    /** The DataObject of the form */
    private FormDataObject formDataObject;

    /** The root node of form hierarchy presented in Component Inspector */
    private FormRootNode formRootNode;

    /** The designer component - the last active designer of the form
     * (there can be more clones). May happen to be null if the active designer
     * was closed and no other designer of given form was activated since then. */
    private FormDesigner formDesigner;

    /** The embracing multiview TopComponent (holds the form designer and
     * java editor) - we remeber the last active TopComponent (not all clones) */
    private CloneableTopComponent multiviewTC;

    /** List of floating windows - must be closed when the form is closed. */
    private ArrayList floatingWindows;

    /** The code generator for the form */
//    private CodeGenerator codeGenerator;

    /** Persistence manager responsible for saving the form */
    private PersistenceManager persistenceManager;

    /** List of exceptions occurred during the last persistence operation */
    private ArrayList persistenceErrors;

    /** An indicator whether the form has been loaded (from the .form file) */
    boolean formLoaded = false; 

    // listeners
    private FormModelListener formListener;
    private PropertyChangeListener dataObjectListener;
    private static PropertyChangeListener settingsListener;
    private static PropertyChangeListener topcompsListener;
    private static PropertyChangeListener paletteListener;

    private UndoRedo.Manager editorUndoManager;

    /** Table of opened FormModel instances (FormModel to FormEditorSupport map) */
    private static Hashtable openForms = new Hashtable();

    /** Set of opened FormEditorSupport instances (java or form opened) */
    private static Set opened = Collections.synchronizedSet(new HashSet());

    // --------------
    // constructor

    public FormEditorSupport(MultiDataObject.Entry javaEntry,
                             FormDataObject formDataObject) {
        super(formDataObject);
        this.formDataObject = formDataObject;
    }

    // ----------
    // opening & saving interface methods

    /** Main entry method. Called by OpenCookie implementation - opens the form.
     * @see OpenCookie#open
     */
    public void openFormEditor() {
        boolean alreadyOpened = opened.contains(this);
        if (!alreadyOpened) {
            elementToOpen = FORM_ELEMENT_INDEX;
        }
        multiviewTC = openCloneableTopComponent();
        multiviewTC.requestActive();

        if (!alreadyOpened) {
            MultiViewHandler handler = MultiViews.findMultiViewHandler(multiviewTC);
            handler.requestActive(handler.getPerspectives()[FORM_ELEMENT_INDEX]);
        }
    }

    /** Overriden from JavaEditor - opens editor and ensures it is selected
     * in the multiview.
     */
    public void open() {
        if (EventQueue.isDispatchThread()) {
            openInAWT();
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    openInAWT();
                }
            });
        }
    }
    
    private void openInAWT() {
        elementToOpen = JAVA_ELEMENT_INDEX;
        super.open();

        // This method must be executed in AWT thread because
        // otherwise multiview is opened in AWT using invokeLater
        // and we don't have multiviewTC correctly set
        MultiViewHandler handler = MultiViews.findMultiViewHandler(multiviewTC);
        handler.requestActive(handler.getPerspectives()[JAVA_ELEMENT_INDEX]);
    }

    /** Overriden from JavaEditor - opens editor at given position and ensures
     * it is selected in the multiview.
     */
    public void openAt(PositionRef pos) {
        elementToOpen = JAVA_ELEMENT_INDEX;
        openCloneableTopComponent();

        MultiViewHandler handler = MultiViews.findMultiViewHandler(multiviewTC);
        handler.requestActive(handler.getPerspectives()[JAVA_ELEMENT_INDEX]);

        super.openAt(pos);
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
    public static FormEditorSupport getFormEditor(FormModel formModel) {
        return (FormEditorSupport) openForms.get(formModel);
    }
    
    JEditorPane getEditorPane() {
        return ((CloneableEditorSupport.Pane)multiviewTC).getEditorPane();
    }

    // ------------
    // loading

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
    
    void reinstallListener() {
        if (formListener != null) {
            formModel.removeFormModelListener(formListener);
            formModel.addFormModelListener(formListener);
        }
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
//        getCodeGenerator().initialize(formModel);
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

    public void reloadForm() {
        if (canClose())
            reloadDocument();
    }

    protected org.openide.util.Task reloadDocument() {
        MultiViewHandler handler = MultiViews.findMultiViewHandler(multiviewTC);
        MultiViewPerspective[] mvps = handler.getPerspectives();
        int openedElement = 0;
        for (int i=0; i < mvps.length; i++) {
            if (mvps[i] == handler.getSelectedPerspective()) {
                openedElement = i; // remember selected element
                break;
            }
        }

        // close all views - should close also the form editor
        Enumeration en = multiviewTC.getReference().getComponents();
        while (en.hasMoreElements()) {
            TopComponent tc = (TopComponent) en.nextElement();
            tc.close();
        }
        
        // Must be done after tc.close(), it sets elementToOpen to 0
        elementToOpen = openedElement;
        
        // TODO would be better not to close the form, but just reload
        // FormModel and update form designer(s) with the new model

        org.openide.util.Task docLoadTask = super.reloadDocument();

        // after reloading is done, open the form editor again
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                openCloneableTopComponent();
                multiviewTC.requestActive();
                MultiViewHandler handler = MultiViews.findMultiViewHandler(multiviewTC);
                handler.requestActive(handler.getPerspectives()[elementToOpen]);
            }
        });

        return docLoadTask;
    }

    protected void notifyClosed() {
        opened.remove(this);
        if (opened.isEmpty())
            detachTopComponentsListener();

        super.notifyClosed(); // close java editor
        if (formLoaded)
            closeForm();
        elementToOpen = JAVA_ELEMENT_INDEX;
    }

    private void multiViewClosed(CloneableTopComponent mvtc) {
        Enumeration en = mvtc.getReference().getComponents();
        boolean isLast = !en.hasMoreElements();
        if (multiviewTC == mvtc) {
            multiviewTC = null;
            formDesigner = null;
            // Find another multiviewTC, possibly with loaded formDesigner
            while (en.hasMoreElements()) {
                multiviewTC = (CloneableTopComponent)en.nextElement();
                FormDesigner designer = (FormDesigner)multiviewTC.getClientProperty("formDesigner"); // NOI18N
                if (designer != null) {
                    formDesigner = designer;
                    break;
                }
            }
            if (!isLast && (formDesigner == null) && formLoaded) {
                // Only Java elements are opened in the remaining clones
                closeForm();
            }
        }
        
        if (isLast) // last view of this form closed
            notifyClosed();
    }

    protected boolean notifyModified () {
        boolean alreadyModified = isModified();
        boolean retVal = super.notifyModified();
        if (!alreadyModified)
            updateMVTCDisplayName();
        return retVal;
    }

    protected void notifyUnmodified () {
        super.notifyUnmodified();
        updateMVTCDisplayName();
    }

    /** Closes the form. Used when closing the form editor or reloading
     * the form. */
    private void closeForm() {
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

        // reset references
        formDesigner = null;
        multiviewTC = null;
        persistenceManager = null;
        persistenceErrors = null;
        formModel = null;
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

                FormDesigner designer = getFormDesigner();
                if (designer != null) {
                    if (compsToSelect != null) {
                        RADComponent[] comps =
                            new RADComponent[compsToSelect.size()];
                        compsToSelect.toArray(comps);
                        designer.setSelectedComponents(comps);
                    }
                    else if (nodeToSelect != null)
                        designer.setSelectedNode(nodeToSelect);
                }

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
                    updateMVTCToolTipText();
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
                Enumeration en = openForms.keys();
                while (en.hasMoreElements()) {
                    FormModel formModel = (FormModel) en.nextElement();
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

    private static void attachTopComponentsListener() {
        if (topcompsListener != null)
            return;

        topcompsListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                if (TopComponent.Registry.PROP_ACTIVATED.equals(
                                                ev.getPropertyName()))
                {   // activated TopComponent has changed
                    TopComponent active = TopComponent.getRegistry().getActivated();
                    if (getSelectedElementType(active) != -1) { // it is our multiview
                        FormEditorSupport fes = getFormEditor(active);
                        if (fes != null) {
                            fes.multiviewTC = (CloneableTopComponent) active;
                            FormDesigner designer = (FormDesigner)active.getClientProperty("formDesigner"); // NOI18N
                            if (designer != null)
                                fes.setFormDesigner(designer);
                        }
                    }
                    checkFormGroupVisibility();
                }
                else if (TopComponent.Registry.PROP_OPENED.equals(
                                                ev.getPropertyName()))
                {   // set of opened TopComponents has changed - hasn't some
                    // of our views been closed?
                    CloneableTopComponent closedTC = null;
                    Set oldSet = (Set) ev.getOldValue();
                    Set newSet = (Set) ev.getNewValue();
                    if (newSet.size() < oldSet.size()) {
                        Iterator it = oldSet.iterator();
                        while (it.hasNext()) {
                            Object o = it.next();
                            if (!newSet.contains(o)) {
                                if (o instanceof CloneableTopComponent)
                                    closedTC = (CloneableTopComponent) o;
                                break;
                            }
                        }
                    }
                    if (getSelectedElementType(closedTC) != -1) { // it is our multiview
                        FormEditorSupport fes = getFormEditor(closedTC);
                        if (fes != null)
                            fes.multiViewClosed(closedTC);
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

            TopComponentGroup group = WindowManager.getDefault()
                                        .findTopComponentGroup("form"); // NOI18N
            if (group != null)
                group.close();
        }
    }

    private static void attachPaletteListener() {
        if (paletteListener != null)
            return;

        paletteListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (CPManager.PROP_SELECTEDITEM.equals(evt.getPropertyName())) {
                    Enumeration en = openForms.keys();
                    while (en.hasMoreElements()) {
                        FormModel formModel = (FormModel) en.nextElement();
                        FormDesigner designer = getFormDesigner(formModel);
                        if (designer != null) {
                            // PENDING should be done for all cloned designers
                            if (evt.getNewValue() == null) {
                                if (designer.getDesignerMode() == FormDesigner.MODE_ADD)
                                    designer.setDesignerMode(FormDesigner.MODE_SELECT);
                            }
                            else designer.setDesignerMode(FormDesigner.MODE_ADD);
                            // TODO activate current designer?
                        }
                    }
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

    // -------
    // window system & multiview
    
    protected CloneableEditorSupport.Pane createPane() {
        MultiViewDescription[] descs = new MultiViewDescription[] {
            new JavaDesc(formDataObject), new FormDesc(formDataObject) };

        CloneableTopComponent mvtc =  
              MultiViewFactory.createCloneableMultiView(
                                    descs,
                                    descs[elementToOpen],
                                    new CloseHandler(formDataObject));
        
        // #45665 - dock into editor mode if possible..
        Mode editorMode = WindowManager.getDefault().findMode(CloneableEditorSupport.EDITOR_MODE);
        if (editorMode != null) {
            editorMode.dockInto(mvtc);
        }
        return (CloneableEditorSupport.Pane)mvtc;
    }
    
    private static String getMVTCToolTipText(FormDataObject formDataObject) {
        String name = FileUtil.getFileDisplayName(formDataObject.getFormFile());
        if (name.endsWith(".form")) { // NOI18N
            name = name.substring(0, name.length()-5);
        }
        return name;
    }
    
    private static String getMVTCDisplayName(FormDataObject formDataObject) {
        boolean readonly = !formDataObject.getPrimaryFile().canWrite();
        int version;
        if (formDataObject.isModified()) {
            version = readonly ? 2 : 1;
        } else {
            version = readonly ? 0 : 3;
        }

        return FormUtils.getFormattedBundleString("FMT_FormMVTCTitle", // NOI18N
            new Object[] {new Integer(version), formDataObject.getName()});
    }

    /** Updates title (display name) of all multiviews for given form. Replans
     * to event queue thread if necessary. */
    private void updateMVTCDisplayName() {
        if (java.awt.EventQueue.isDispatchThread()) {
            if (multiviewTC == null)
                return;

            String title = getMVTCDisplayName(formDataObject);
            Enumeration en = multiviewTC.getReference().getComponents();
            while (en.hasMoreElements()) {
                TopComponent tc = (TopComponent) en.nextElement();
                tc.setDisplayName(title);
            }
        }
        else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    if (multiviewTC == null)
                        return;

                    String title = getMVTCDisplayName(formDataObject);
                    Enumeration en = multiviewTC.getReference().getComponents();
                    while (en.hasMoreElements()) {
                        TopComponent tc = (TopComponent) en.nextElement();
                        tc.setDisplayName(title);
                    }
                }
            });
        }
    }

    /** Updates tooltip of all multiviews for given form. Replans to even queue
     * thread if necessary. */
    private void updateMVTCToolTipText() {
        if (java.awt.EventQueue.isDispatchThread()) {
            if (multiviewTC == null)
                return;

            String tooltip = getMVTCToolTipText(formDataObject);
            Enumeration en = multiviewTC.getReference().getComponents();
            while (en.hasMoreElements()) {
                TopComponent tc = (TopComponent) en.nextElement();
                tc.setToolTipText(tooltip);
            }
        }
        else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    if (multiviewTC == null)
                        return;

                    String tooltip = getMVTCToolTipText(formDataObject);
                    Enumeration en = multiviewTC.getReference().getComponents();
                    while (en.hasMoreElements()) {
                        TopComponent tc = (TopComponent) en.nextElement();
                        tc.setToolTipText(tooltip);
                    }
                }
            });
        }
    }

    static boolean isLastView(TopComponent tc) {
        if (!(tc instanceof CloneableTopComponent))
            return false;

        boolean oneOrLess = true;
        Enumeration en = ((CloneableTopComponent)tc).getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements())
                oneOrLess = false;
        }
        return oneOrLess;
    }

    /** This is called by the multiview elements whenever they are created
     * (and given a observer knowing their multiview TopComponent). It is
     * important during deserialization and clonig the multiview - i.e. during
     * the operations we have no control over. But anytime a multiview is
     * created, this method gets called.
     */
    void setTopComponent(TopComponent topComp) {
        multiviewTC = (CloneableTopComponent)topComp;
        multiviewTC.setDisplayName(getMVTCDisplayName(formDataObject));
        multiviewTC.setToolTipText(getMVTCToolTipText(formDataObject));
        opened.add(this);
        attachTopComponentsListener();
    }

    public static FormEditorSupport getFormEditor(TopComponent tc) {
        Object dobj = tc.getLookup().lookup(DataObject.class);
        return dobj instanceof FormDataObject ?
               ((FormDataObject)dobj).getFormEditor() : null;
    }

    private static Boolean groupVisible = null;

    static void checkFormGroupVisibility() {
        // when active TopComponent changes, check if we should open or close
        // the form editor group of windows (Inspector, Palette, Properties)
        WindowManager wm = WindowManager.getDefault();
        final TopComponentGroup group = wm.findTopComponentGroup("form"); // NOI18N
        if (group == null)
            return; // group not found (should not happen)

        boolean designerSelected = false;
        Iterator it = wm.getModes().iterator();
        while (it.hasNext()) {
            Mode mode = (Mode) it.next();
            TopComponent selected = mode.getSelectedTopComponent();
            if (getSelectedElementType(selected) == FORM_ELEMENT_INDEX) {
                designerSelected = true;
                break;
            }
        }

        if (designerSelected && !Boolean.TRUE.equals(groupVisible)) {
            group.open();
            ComponentInspector.getInstance().requestActive();
        }
        else if (!designerSelected && !Boolean.FALSE.equals(groupVisible)) {
            group.close();
        }

        groupVisible = designerSelected ? Boolean.TRUE : Boolean.FALSE;
    }

    /** @return 0 if java editor in form editor multiview is selected
     *          1 if form designer in form editor multiview is selected
     *         -1 if the given TopComponent is not form editor multiview
     */
    static int getSelectedElementType(TopComponent tc) {
        if (tc != null) {
            MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
            if (handler != null) {
                String prefId = handler.getSelectedPerspective().preferredID();
                if (MV_JAVA_ID.equals(prefId))
                    return JAVA_ELEMENT_INDEX; // 0
                if (MV_FORM_ID.equals(prefId))
                    return FORM_ELEMENT_INDEX; // 1
            }
        }
        return -1;
    }

    // --------

    /** A descriptor for the FormDesigner element of multiview. Allows lazy
     * creation of the FormDesigner (and thus form loading). */
    private static class FormDesc implements MultiViewDescription, Serializable {

        private static final long serialVersionUID =-3126744316624172415L;        

        private DataObject dataObject;

        private FormDesc() {
        }

        public FormDesc(DataObject formDO) {
            dataObject = formDO;
        }

        private FormEditorSupport getFormEditor() {
            return dataObject != null && dataObject instanceof FormDataObject ?
                ((FormDataObject)dataObject).getFormEditor() : null;
        }

        public MultiViewElement createElement() {
            FormDesigner designer = null;
            FormEditorSupport formEditor = getFormEditor();
            if (formEditor != null) {
                designer = new FormDesigner(formEditor);
            }

            return designer == null ? MultiViewFactory.BLANK_ELEMENT : designer;
        }

        public String getDisplayName() {
            return FormUtils.getBundleString("CTL_DesignTabCaption"); // NOI18N
        }

        public org.openide.util.HelpCtx getHelpCtx() {
            return org.openide.util.HelpCtx.DEFAULT_HELP;
        }

        public java.awt.Image getIcon() {
            return Utilities.loadImage(iconURL);
        }

        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_NEVER;
        }

        public String preferredID() {
            return MV_FORM_ID;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(dataObject);
        }

        public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException
        {
            Object firstObject = in.readObject();
            if (firstObject instanceof FormDataObject)
                dataObject = (DataObject) firstObject;
        }
    }

    // -------

    /** A descriptor for the java editor as an element in multiview. */
    private static class JavaDesc implements MultiViewDescription, Serializable {

        private static final long serialVersionUID =-3126744316624172415L;        

        private DataObject dataObject;

        private JavaDesc() {
        }

        public JavaDesc(DataObject formDO) {
            dataObject = formDO;
        }

        private JavaEditor getJavaEditor() {
            return dataObject != null && dataObject instanceof FormDataObject ?
                ((FormDataObject)dataObject).getFormEditor() : null;
        }

        public MultiViewElement createElement() {
            JavaEditor javaEditor = getJavaEditor();
            if (javaEditor != null) {
                javaEditor.prepareDocument();
                JavaEditorTopComponent editor = new JavaEditorTopComponent((JavaEditor) dataObject.getCookie(JavaEditor.class));
                Node[] nodes = editor.getActivatedNodes();
                if ((nodes == null) || (nodes.length == 0)) {
                    editor.setActivatedNodes(new Node[] {dataObject.getNodeDelegate()});
                }
                dataObject.getCookie(JavaParser.class);
                return (MultiViewElement) editor;
            }
            return MultiViewFactory.BLANK_ELEMENT;
        }

        public String getDisplayName() {
            return FormUtils.getBundleString("CTL_SourceTabCaption"); // NOI18N
        }

        public org.openide.util.HelpCtx getHelpCtx() {
            return org.openide.util.HelpCtx.DEFAULT_HELP;
        }

        public java.awt.Image getIcon() {
            return Utilities.loadImage(iconURL);
        }

        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ONLY_OPENED;
        }

        public String preferredID() {
            return MV_JAVA_ID;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(dataObject);
        }

        public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException
        {
            Object firstObject = in.readObject();
            if (firstObject instanceof FormDataObject)
                dataObject = (DataObject) firstObject;
        }
    }

    // --------

    private static class JavaEditorTopComponent
                     extends JavaEditor.JavaEditorComponent
                     implements MultiViewElement, CloneableEditorSupport.Pane
    {
        private static final long serialVersionUID =-3126744316624172415L;        

        private transient JComponent toolbar;

        private transient MultiViewElementCallback multiViewObserver;

        JavaEditorTopComponent() {
            super();
        }

        JavaEditorTopComponent(DataEditorSupport s) {
            super(s);
        }

        public JComponent getToolbarRepresentation() {
            if (toolbar == null) {
                JEditorPane pane = getEditorPane();
                if (pane != null) {
                    Document doc = pane.getDocument();
                    if (doc instanceof NbDocument.CustomToolbar) {
                        toolbar = ((NbDocument.CustomToolbar)doc).createToolbar(pane);
                    }
                }
                if (toolbar == null) {
                    // attempt to create own toolbar??
                    toolbar = new JPanel();
                }
            }
            return toolbar;
        }

        public JComponent getVisualRepresentation() {
            return this; 
        }

        public void componentDeactivated() {
            super.componentDeactivated();
        }

        public void componentActivated() {
            super.componentActivated();
        }

        public void setMultiViewCallback(MultiViewElementCallback callback) {
            multiViewObserver = callback;

            // needed for deserialization...
            if (((DataEditorSupport) cloneableEditorSupport ()).getDataObject() instanceof FormDataObject) { // [obj is from EditorSupport.Editor]
                // this is used (or misused?) to obtain the deserialized
                // multiview topcomponent and set it to FormEditorSupport
                ((FormDataObject)((DataEditorSupport) cloneableEditorSupport ()).getDataObject()).getFormEditor().setTopComponent(
                                                   callback.getTopComponent());
            }
        }

        public void requestVisible() {
            if (multiViewObserver != null)
                multiViewObserver.requestVisible();
            else
                super.requestVisible();
        }

        public void requestActive() {
            if (multiViewObserver != null)
                multiViewObserver.requestActive();
            else
                super.requestActive();
        }

        public void componentClosed() {
            super.componentClosed();
        }

        public void componentShowing() {
            super.componentShowing();
        }

        public void componentHidden() {
            super.componentHidden();
        }

        public void componentOpened() {
            super.componentOpened();
        }

        public void updateName() {
            super.updateName();
            if (multiViewObserver != null) {
                FormDataObject formDataObject = (FormDataObject)((DataEditorSupport) cloneableEditorSupport ()).getDataObject();
                setDisplayName(getMVTCDisplayName(formDataObject));
            }
        }

        protected boolean closeLast() {
            return true;
        }

        public CloseOperationState canCloseElement() {
            // if this is not the last cloned java editor component, closing is OK
            if (!FormEditorSupport.isLastView(multiViewObserver.getTopComponent()))
                return CloseOperationState.STATE_OK;

            // Issue 52286
            super.canClose(null, true);
            
            // return a placeholder state - to be sure our CloseHandler is called
            return MultiViewFactory.createUnsafeCloseState(
                "ID_JAVA_CLOSING", // dummy ID // NOI18N
                MultiViewFactory.NOOP_CLOSE_ACTION,
                MultiViewFactory.NOOP_CLOSE_ACTION);
        }

        protected boolean isActiveTC() {
            TopComponent selected = getRegistry().getActivated();

            if (selected == null)
                return false;
            if (selected == this)
                return true;

            MultiViewHandler handler = MultiViews.findMultiViewHandler(selected);
            if (handler != null
                    && MV_JAVA_ID.equals(handler.getSelectedPerspective()
                                                          .preferredID()))
                return true;

            return false;
        }
    }

    // ------

    /** Implementation of CloseOperationHandler for multiview. Ensures both form
     * and java editor are correctly closed, data saved, etc. Holds a reference
     * to form DataObject only - to be serializable with the multiview
     * TopComponent without problems.
     */
    private static class CloseHandler implements CloseOperationHandler,
                                                 Serializable
    {
        private static final long serialVersionUID =-3126744315424172415L;        

        private DataObject dataObject;

        private CloseHandler() {
        }

        public CloseHandler(DataObject formDO) {
            dataObject = formDO;
        }

        private FormEditorSupport getFormEditor() {
            return dataObject != null && dataObject instanceof FormDataObject ?
                ((FormDataObject)dataObject).getFormEditor() : null;
        }

        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            FormEditorSupport formEditor = getFormEditor();
            return formEditor != null ? formEditor.canClose() : true;
        }
    }
}
