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

import java.beans.*;
import java.io.*;
import java.util.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditCookie;
import org.openide.loaders.*;
import org.openide.text.*;
import org.openide.util.Utilities;
import org.openide.windows.*;

import org.netbeans.modules.java.JavaEditor;

/**
 *
 * @author Ian Formanek
 */
public class FormEditorSupport extends JavaEditor implements FormCookie, EditCookie
{
    /** The reference to FormDataObject */
    private FormDataObject formObject;

    /** True, if the design form has been loaded from the form file */
    transient private boolean formLoaded = false;

    /** True, if the form has been opened on non-Editing workspace and shoul dbe
     * opened when the user switches back to Editing
     * */
    transient private boolean openOnEditing = false;

    // listeners
    private PropertyChangeListener workspacesListener;
    private static PropertyChangeListener settingsListener;
    private static PropertyChangeListener topcompsListener;

    private UndoRedo.Manager undoManager;
    private RADComponentNode formRootNode;
    private FormModel formModel;
    private PersistenceManager saveManager;

    /** Table of FormModel instances of open forms */
    private static Hashtable openForms = new Hashtable();

    public FormEditorSupport(MultiDataObject.Entry javaEntry,
                             FormDataObject formObject) {
        super(javaEntry);
        this.formObject = formObject;
        formLoaded = false;
    }

    // ----------
    // opening & saving (interface methods)

    /** OpenCookie implementation - opens the form (loads it first if needed).
     * @see OpenCookie#open
     */
    public void open() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // set status text "Opening Form..."
                TopManager.getDefault().setStatusText(
                    java.text.MessageFormat.format(
                        FormEditor.getFormBundle().getString("FMT_OpeningForm"), // NOI18N
                        new Object[] { formObject.getName() }));

                if (loadForm())
                    openGUI();
                FormEditorSupport.super.open();

                // clear status text
                TopManager.getDefault().setStatusText(""); // NOI18N
            }
        });
    }

    /** Main loading method - loads form data from file.
     */
    public synchronized boolean loadForm() {
        if (formLoaded) return true; // form already loaded

        // first find PersistenceManager for loading the form
        PersistenceManager[] pms = recognizeForm(formObject);
        if (pms == null) return false;
        PersistenceManager loadManager = pms[0];
        saveManager = pms[1];

        // load the form data (FormModel) and report errors
        try {
            formModel = loadManager.loadForm(formObject);

            reportErrors(formModel==null, null);
        }
        catch (Throwable t) {
            if (t instanceof ThreadDeath)
                throw (ThreadDeath)t;

            reportErrors(true, t);
        }
        if (formModel == null) return false;

        // create form nodes hierarchy and add it to SourceChildren
        formRootNode = new RADComponentNode(formModel.getTopRADComponent());
//        enforceNodesCreation(formRootNode);
        formRootNode.getChildren().getNodes();
        formObject.getNodeDelegate().getChildren()
                .add(new RADComponentNode [] { formRootNode });

        // form is successfully loaded...
        formLoaded = true;
        openForms.put(this, formModel);

        attachSettingsListener();
        attachTopComponentsListener();

        return true;
    }

    /** EditCookie implementation - opens only java source file.
     * Form is ignored (not loaded).
     * @see EditCookie#edit
     */
    public void edit() {
        super.open();
    }

    /** @return true if the form is opened, false otherwise */
    public boolean isOpened() {
        return formLoaded;
    }

    /** Save the document in this thread and start reparsing it.
     * @exception IOException on I/O error
     */
    public void saveDocument() throws IOException {
        super.saveDocument();
        saveForm();
    }

    /** Save the document in this thread.
     * @param parse true if the parser should be started, otherwise false
     * @exception IOException on I/O error
     */
    protected void saveDocumentIfNecessary(boolean parse) throws IOException {
        super.saveDocumentIfNecessary(parse);
        saveForm();
    }

    // ------------
    // other interface methods

    public FormDataObject getFormObject() {
        return formObject;
    }

    public Node getFormRootNode() {
        return formRootNode;
    }

    /** @return the FormModel of this form */
    public FormModel getFormModel() {
        return formModel;
    }

    /** @return the FormDesigner window */
    FormDesigner getFormDesigner() {
        return formLoaded ? formModel.getFormDesigner() : null;
    }

    boolean supportsAdvancedFeatures() {
        return saveManager.supportsAdvancedFeatures();
    }

    /** Marks the form as modified if it's not yet. Used if changes made
     * in form data don't affect the java source file (generated code). */
    void markFormModified() {
        if (formLoaded && !formObject.isModified())
            super.notifyModified();
    }

    /** Creates an undo/redo manager.
     */
    protected UndoRedo.Manager createUndoRedoManager() {
        undoManager = super.createUndoRedoManager();
        return undoManager;
    }

    UndoRedo.Manager getUndoManager() {
        return undoManager;
    }

    // ------------
    // loading

    private void openForm(final boolean openGui) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                boolean loaded = loadForm();
                if (loaded) {
                    if (openGui)
                        openGUI();
                    FormEditorSupport.super.open();
                }
            }
        });
    }

    /** Finds PersistenceManager that can load and save the form.
     * Returns array of two managers - the first for loading, second for saving.
     */
    private PersistenceManager[] recognizeForm(FormDataObject formObject) {
        PersistenceManager loadManager = null;
        PersistenceManager saveManager = null;

        for (Iterator it = PersistenceManager.getManagers(); it.hasNext(); ) {
            PersistenceManager man = (PersistenceManager)it.next();
            try {
                if (man.canLoadForm(formObject)) {
                    loadManager = man;
                    break;
                }
            }
            catch (IOException e) {} // ignore error and try the next manager
        }

        if (loadManager == null) { // no PersistenceManager is able to load form
            TopManager.getDefault().notify(
                new NotifyDescriptor.Message(
                    FormEditor.getFormBundle().getString("MSG_ERR_NotRecognizedForm"),
                    NotifyDescriptor.ERROR_MESSAGE));
            return null;
        }

        if (!loadManager.supportsAdvancedFeatures()) {
            Object result = TopManager.getDefault().notify(
                new NotifyDescriptor.Confirmation(
                    FormEditor.getFormBundle().getString("MSG_ConvertForm"),
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE));

            if (NotifyDescriptor.YES_OPTION.equals(result))
                saveManager = new GandalfPersistenceManager();
        }

        if (saveManager == null) saveManager = loadManager;

        return new PersistenceManager[] { loadManager, saveManager };
    }

    /** Used for reporting errors occured when loading form.
     */
    private void reportErrors(boolean fatalError, Throwable ex) {
        if (fatalError) {
            if (ex == null) {
                TopManager.getDefault().notify(
                    new NotifyDescriptor.Message(
                        java.text.MessageFormat.format(
                            FormEditor.getFormBundle().getString("FMT_ERR_LoadingForm"),
                            new Object[] { formObject.getName() }),
                        NotifyDescriptor.ERROR_MESSAGE));
            }
            else {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace();

                TopManager.getDefault().notify(
                    new NotifyDescriptor.Message(
                        java.text.MessageFormat.format(
                            FormEditor.getFormBundle().getString("FMT_ERR_LoadingFormDetails"),
                            new Object[] { formObject.getName(),
                                           Utilities.getShortClassName(ex.getClass()),
                                           ex.getMessage() }),
                        NotifyDescriptor.ERROR_MESSAGE));
            }
        }
        else FormEditor.displayErrorLog();
    }

    /** Opens FormDesigner and ComponentInspector.
     */
    private void openGUI() {
        if (isCurrentWorkspaceEditing()) {
            String formWorkspace = FormEditor.getFormSettings().getWorkspace();
            if (!formWorkspace.equalsIgnoreCase(FormEditor.getFormBundle()
                                        .getString("VALUE_WORKSPACE_NONE"))) {
                Workspace visualWorkspace =
                    TopManager.getDefault().getWindowManager()
                        .findWorkspace(formWorkspace);
                if (visualWorkspace != null)
                    visualWorkspace.activate();
            }

            FormDesigner designer = formModel.getFormDesigner();
            designer.initialize();
            designer.open();
            designer.requestFocus();

            ComponentInspector.getInstance().focusForm(formModel, true);
        }
        // if this is not "editing" workspace, attach a listener on
        // workspace switching and wait for the editing one
        else attachWorkspacesListener();
    }

    private boolean isCurrentWorkspaceEditing() {
        String name = TopManager.getDefault().getWindowManager()
                                         .getCurrentWorkspace().getName();
        return !"Browsing".equals(name) && !"Running".equals(name)
                && !"Debugging".equals(name);
    }

    // -----------
    // saving

    private void saveForm() {
        if (formLoaded && !formObject.formFileReadOnly()) {
            formModel.fireFormToBeSaved();
            try {
                saveManager.saveForm(formObject, formModel);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // -----------
    // closing/reloading

    protected void notifyClose() {
        super.notifyClose();
        if (formLoaded)
            closeForm();
    }

    protected org.openide.util.Task reloadDocumentTask() {
        boolean reloadForm = formLoaded;
        if (formLoaded)
            closeForm();

        org.openide.util.Task docLoadTask = super.reloadDocumentTask();

        if (reloadForm)
            openForm(true);

        return docLoadTask;
    }

    /** Closes the form. Used when closing or reloading the document.
     */
    private void closeForm() {
        openForms.remove(this);

        // remove nodes hierarchy
        formObject.getNodeDelegate().getChildren()
            .remove(new RADComponentNode [] { formRootNode });

        // remove listeners
        detachWorkspacesListener();
        if (openForms.isEmpty()) {
            ComponentInspector.getInstance().focusForm(null, false);
            detachSettingsListener();
            detachTopComponentsListener();
        }
        else { // still any opened forms - focus some
            FormModel model = (FormModel) openForms.values().iterator().next();
            ComponentInspector.getInstance().focusForm(model);
        }

        // close the designer
        TopComponent formWin = getFormDesigner();
        if (formWin != null) {
            formWin.setCloseOperation(TopComponent.CLOSE_EACH);
            formWin.close();
        }

        RADMenuItemComponent.freeDesignTimeMenus(formModel);

        // reset references
        formRootNode = null;
        formModel = null;
        formLoaded = false;
    }

    // -----------
    // listeners

    private void attachWorkspacesListener() {
        openOnEditing = true;
        if (workspacesListener != null) return;

        workspacesListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (WindowManager.PROP_CURRENT_WORKSPACE.equals(evt.getPropertyName())) {
                    if (openOnEditing) {
                        if (isCurrentWorkspaceEditing()) {
                            openOnEditing = false;
                            openGUI();
                            detachWorkspacesListener();
                        }
                    }
                }
            }
        };

        TopManager.getDefault().getWindowManager()
            .addPropertyChangeListener(workspacesListener);
    }

    private void detachWorkspacesListener() {
        if (workspacesListener != null) {
            TopManager.getDefault().getWindowManager()
                    .removePropertyChangeListener(workspacesListener);
            workspacesListener = null;
        }
    }

    private static void attachSettingsListener() {
        if (settingsListener != null) return;

        settingsListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Enumeration enum = openForms.keys();
                while (enum.hasMoreElements()) {
                    FormEditorSupport fes = (FormEditorSupport)enum.nextElement();
                    if (!fes.isOpened()) continue;

                    FormModel formModel = fes.getFormModel();
                    String propName = evt.getPropertyName();

                    if (FormLoaderSettings.PROP_USE_INDENT_ENGINE.equals(propName)) {
                        formModel.fireFormChanged();
                    }
                    else if (FormLoaderSettings.PROP_VARIABLES_MODIFIER.equals(propName)) {
                        formModel.fireFormChanged();
                    }
                    else if (FormLoaderSettings.PROP_SELECTION_BORDER_SIZE.equals(propName)
                          || FormLoaderSettings.PROP_SELECTION_BORDER_COLOR.equals(propName)
                          || FormLoaderSettings.PROP_CONNECTION_BORDER_COLOR.equals(propName)) {
                        formModel.getFormDesigner().repaint();
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
        if (topcompsListener != null) return;

        topcompsListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                // we are interested in changing active TopComponent
                if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
                    TopComponent tc = TopComponent.getRegistry().getActivated();
//                    if (tc == null) return;
                    if (!(tc instanceof JavaEditor.JavaEditorComponent)) return;
                    Node[] nodes = tc.getActivatedNodes();
                    if (nodes == null || nodes.length != 1) return;

                    String componentName = tc.getName();
                    if (componentName.endsWith("*"))
                        componentName = componentName.substring(0,componentName.length()-2);
                    componentName.trim();
                    boolean ext = componentName.endsWith(".java"); // NOI18N

                    Enumeration enum = openForms.keys();
                    while (enum.hasMoreElements()) {
                        FormEditorSupport fes = (FormEditorSupport)enum.nextElement();
                        String formName = fes.getFormObject().getName();
                        if (ext) formName = formName + ".java"; // NOI18N
                        if (formName.equals(componentName)) {
//                            if (nodes[0].getCookie(DataObject.class) == fes.getFormObject()) {
                            ComponentInspector.getInstance().focusForm(fes.getFormModel());
//                            }
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

    // ----------
    // FormCookie implementation

    public void gotoEditor() {
        super.open();
    }

    public void gotoForm() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                if (!formLoaded)
                    openForm(true);
                else {
                    formModel.getFormDesigner().open();
                    formModel.getFormDesigner().requestFocus();
                }
            }
        });
    }
}
