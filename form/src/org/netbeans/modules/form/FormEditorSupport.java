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

/* $Id$ */

package org.netbeans.modules.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Iterator;
import java.util.Hashtable;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.src.nodes.SourceChildren;
import org.openide.text.*;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.windows.WindowManager;
import org.openide.windows.Workspace;
import org.openide.windows.TopComponent;

import org.netbeans.modules.java.JavaEditor;

/**
 *
 * @author Ian Formanek
 */
public class FormEditorSupport extends JavaEditor implements FormCookie, EditCookie {

    /** The reference to FormDataObject */
    private FormDataObject formObject;

    /** True, if the design form has been loaded from the form file */
    transient private boolean formLoaded = false;

    /** True, if the form has been opened on non-Editing workspace and shoul dbe
     * opened when the user switches back to Editing
     * */
    transient private boolean openOnEditing = false;

    transient private PropertyChangeListener workspacesListener = null;

    private UndoRedo.Manager undoManager;
    private RADComponentNode formRootNode;
    private FormManager2 formManager;
    private PersistenceManager saveManager;
    private PropertyChangeListener settingsListener;

    /** lock for opening form */
    private static final Object OPEN_FORM_LOCK = new Object();

    /** Table of FormManager instances of open forms */
    private static Hashtable openForms = new Hashtable();

    private static boolean listenerRegistered = false;
    private static PropertyChangeListener editorFocusChangeListener =
        new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(TopComponent.Registry.PROP_ACTIVATED)) {
                    TopComponent tc = TopComponent.getRegistry().getActivated();
                    if (tc != null) {
                        String componentName = tc.getName();
                        // is it a form ? => find formManager for it and focus it in ComponentInspector
                        for (java.util.Enumeration enum = openForms.keys(); enum.hasMoreElements();) {
                            FormEditorSupport fes =(FormEditorSupport) enum.nextElement();
                            String formName = fes.getFormObject().getName();
                            if (formName.equals(componentName)) {
                                FormEditor.getComponentInspector().focusForm((FormManager2) openForms.get(fes));
                                break;
                            }
                        }
                    }
                }
            }
        };

    public FormEditorSupport(MultiDataObject.Entry javaEntry, FormDataObject formObject) {
        super(javaEntry);
        this.formObject = formObject;
        formLoaded = false;
        settingsListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (formLoaded) {
                    if (FormLoaderSettings.PROP_INDENT_AWT_HIERARCHY.equals(
                        evt.getPropertyName())) {
                        formManager.fireCodeChange();
                    } else if (FormLoaderSettings.PROP_VARIABLES_MODIFIER.equals(
                        evt.getPropertyName())) {
                        formManager.fireFormChange();
                    } else if (FormLoaderSettings.PROP_NULL_LAYOUT.equals(
                        evt.getPropertyName())) {
                        formManager.fireCodeChange();
                    }
                }
            }
        };
    }

    private boolean openForm() {
        // sets status text

        TopManager.getDefault().setStatusText(
            java.text.MessageFormat.format(
                NbBundle.getBundle(FormEditorSupport.class).getString("FMT_OpeningForm"),
                new Object[] { formObject.getName() }
                )
            );

        // load the form

        synchronized(OPEN_FORM_LOCK) {
            if (!formLoaded) {
                if (!loadForm()) {
                    TopManager.getDefault().setStatusText(""); // NOI18N
                    return false;
                }
            }
        }
        openForms.put(this, getFormManager());

        if (!listenerRegistered) {
            TopComponent.getRegistry().addPropertyChangeListener(
                org.openide.util.WeakListener.propertyChange(
                    editorFocusChangeListener,TopComponent.getRegistry()
                    )
                );
            listenerRegistered = true;
        }

        // swich workspace

        String formWorkspace = FormEditor.getFormSettings().getWorkspace();
        if (!formWorkspace.equalsIgnoreCase(
            FormEditor.getFormBundle().getString("VALUE_WORKSPACE_NONE"))
            && isCurrentWorkspaceEditing()
            ) {
            Workspace visualWorkspace =
                TopManager.getDefault().getWindowManager().findWorkspace(formWorkspace);
            if (visualWorkspace != null)
                visualWorkspace.activate();
        }

        if (! isCurrentWorkspaceEditing()) {
            attachWorkspacesListener();
        }
        else {
            getFormTopComponent().open();
            getFormTopComponent().requestFocus();

            FormEditor.getComponentInspector().focusForm(getFormManager());
            FormEditor.getComponentInspector().open();
        }

        // clear status text
        TopManager.getDefault().setStatusText(null); // NOI18N
        return true;
    }

    /** Focuses existing component to open, or if none exists creates new.
     * @see OpenCookie#open
     */

    public void open() {
        openForm();
        super.open();
    }

    /** EditCookie implementation - opens only java file.
     */
    public void edit() {
        super.open();
    }

    /* Calls superclass.
     * @param pos Where to place the caret.
     * @return always non null editor
     */
    protected EditorSupport.Editor openAt(PositionRef pos) {
        openForm();
        return super.openAt(pos);

    }

    private boolean isCurrentWorkspaceEditing() {
        String name = TopManager.getDefault().getWindowManager().getCurrentWorkspace().getName();
        if (!("Browsing".equals(name) || "Running".equals(name) || "Debugging".equals(name))) { // NOI18N
            return true;
        } else {
            return false;
        }
    }

    private void attachWorkspacesListener() {
        openOnEditing = true;
        if (workspacesListener == null) {
            workspacesListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (WindowManager.PROP_CURRENT_WORKSPACE.equals(evt.getPropertyName())) {
                        if (openOnEditing) {
                            if (isCurrentWorkspaceEditing()) {
                                openOnEditing = false;
                                TopManager.getDefault().getWindowManager().removePropertyChangeListener(workspacesListener);
                                workspacesListener = null;

                                FormEditor.getComponentInspector().open();
                                getFormTopComponent().open();
                            }
                        }
                    }
                }
            };
            TopManager.getDefault().getWindowManager().addPropertyChangeListener(workspacesListener);
        }
    }

    public FormDataObject getFormObject() {
        return formObject;
    }

    public org.openide.nodes.Node getFormRootNode() {
        return formRootNode;
    }

    /** Create an undo/redo manager.
     * This manager is then attached to the document, and listens to
     * all changes made in it.
     * <P>
     * The default implementation simply uses <code>UndoRedo.Manager</code>.
     *
     * @return the undo/redo manager
     */
    protected UndoRedo.Manager createUndoRedoManager() {
        undoManager = super.createUndoRedoManager();
        return undoManager;
    }

    UndoRedo.Manager getUndoManager() {
        return undoManager;
    }

    /** @returns the FormManager2 of this form */
    public FormManager2 getFormManager() {
        return formManager;
    }

    /** @returns the Form Window */
    FormTopComponent getFormTopComponent() {
        if (!formLoaded) return null;
        return formManager.getFormTopComponent();
    }

    /** Marks the form as modified if it's not yet. Used if changes made
     * in form data don't project to the java source file (generated code). */
    void markFormModified() {
        if (formLoaded && !formObject.isModified())
            super.notifyModified();
    }

    // -----------------------------------------------------------------------------
    // Form Loading

    protected void notifyClose() {
        super.notifyClose();
        if (formLoaded) closeForm();
    }

    /** @return true if the form is already loaded, false otherwise */
    boolean isLoaded() {
        return formLoaded;
    }

    boolean supportsAdvancedFeatures() {
        return saveManager.supportsAdvancedFeatures();
    }

    /** @return true if the form is opened, false otherwise */
    public boolean isOpened() {
        return formLoaded;
    }

    protected Task reloadDocumentTask() {
        if (formLoaded) {
            closeForm();
            openForm();
        }

        Task docLoadTask = super.reloadDocumentTask();

        if (formLoaded) {
            FormManager2 fm = getFormManager();
            fm.getCodeGenerator().initialize(fm);
        }

        return docLoadTask;
    }

    // used when closing or reloading the document
    private void closeForm() {
        if (workspacesListener != null) {
            TopManager.getDefault().getWindowManager().removePropertyChangeListener(workspacesListener);
        }
        org.openide.windows.TopComponent formWin = getFormTopComponent();
        if (formWin != null) {
            formWin.setCloseOperation(TopComponent.CLOSE_EACH);
            formWin.close();
        }
        FormEditor.getComponentInspector().focusForm(null);
        openForms.remove(this);
        if (openForms.isEmpty()) {
            FormEditor.getComponentInspector().close();
        }
        SourceChildren sc =(SourceChildren)formObject.getNodeDelegate().getChildren();
        sc.remove(new RADComponentNode [] { formRootNode });
        RADMenuItemComponent.freeDesignTimeMenus(formManager);
        formRootNode = null;
        formManager = null;
        FormEditor.getFormSettings().removePropertyChangeListener(settingsListener);
        formLoaded = false;
    }

    /** Loads the DesignForm from the .form file.
     * @return true if the form was correcly loaded, false if any error occured
     */
    protected boolean loadForm() {
        return loadFormInternal(null);
    }

    /** Loads the DesignForm from the .form file.
     * @param formTopComponent the top component that the formManager should be initialized with - used during deserialization of workspaces
     * @return true if the form was correcly loaded, false if any error occured
     */
    protected boolean loadFormInternal(FormTopComponent formTopComponent) {
        PersistenceManager loadManager = null;
        for (Iterator it = PersistenceManager.getManagers(); it.hasNext();) {
            PersistenceManager man =(PersistenceManager)it.next();
            try {
                if (man.canLoadForm(formObject)) {
                    loadManager = man;
                    break;
                }
            } catch (IOException e) {
                // ignore error and try the next manager
            }
        }

        if (loadManager == null) {
            TopManager.getDefault().notify(
                new NotifyDescriptor.Message(
                    FormEditor.getFormBundle().getString("MSG_ERR_NotRecognizedForm"),
                    NotifyDescriptor.ERROR_MESSAGE));
            return false;
        }

        if (!loadManager.supportsAdvancedFeatures()) {
            Object result = TopManager.getDefault().notify(
                new NotifyDescriptor.Confirmation(FormEditor.getFormBundle().getString("MSG_ConvertForm"),
                                                  NotifyDescriptor.YES_NO_OPTION,
                                                  NotifyDescriptor.QUESTION_MESSAGE)
                );
            if (NotifyDescriptor.YES_OPTION.equals(result)) {
                saveManager = new GandalfPersistenceManager();
            } else {
                saveManager = loadManager;
            }
        } else {
            saveManager = loadManager;
        }

        try {
            formManager = loadManager.loadForm(formObject);
            if (formManager == null) {
                TopManager.getDefault().notify(
                    new NotifyDescriptor.Message(
                        java.text.MessageFormat.format(
                            FormEditor.getFormBundle().getString("FMT_ERR_LoadingForm"),
                            new Object[] { formObject.getName() }),
                        NotifyDescriptor.ERROR_MESSAGE));
                return false;
            }
            if (formTopComponent != null) {
                formManager.initFormTopComponent(formTopComponent);
            }
            formManager.initialize();

            // create form hierarchy node and add it to SourceChildren
            SourceChildren sc =(SourceChildren)formObject.getNodeDelegate().getChildren();
            formRootNode = new RADComponentNode(formManager.getRADForm().getTopLevelComponent());
            enforceNodesCreation(formRootNode);
            sc.add(new RADComponentNode [] { formRootNode });

            formLoaded = true;
            FormEditor.getFormSettings().addPropertyChangeListener(settingsListener);
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw(ThreadDeath)t;
            }
            if (System.getProperty("netbeans.debug.exceptions") != null) {
                t.printStackTrace();
            }
            TopManager.getDefault().notify(
                new NotifyDescriptor.Message(
                    java.text.MessageFormat.format(
                        FormEditor.getFormBundle().getString("FMT_ERR_LoadingFormDetails"),
                        new Object[] { formObject.getName(), org.openide.util.Utilities.getShortClassName(t.getClass()), t.getMessage()}),
                    NotifyDescriptor.ERROR_MESSAGE));
            return false;
        }

        // if there are errors or warnings, display it
        FormEditor.displayErrorLog();

        return true;
    }

    private void enforceNodesCreation(org.openide.nodes.Node node) {
        org.openide.nodes.Children ch = node.getChildren();
        if (ch != org.openide.nodes.Children.LEAF) {
            org.openide.nodes.Node[] nodes = ch.getNodes();
            for (int i = 0; i < nodes.length; i++) {
                enforceNodesCreation(nodes[i]);
            }
        }
    }

    // -----------------------------------------------------------------------------
    // Form Saving

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

    private void saveForm() {
        if (formLoaded) {
            formManager.fireFormToBeSaved();
            try {
                saveManager.saveForm(formObject, formManager);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // -----------------------------------------------------------------------------
    // FormCookie implementation

    /** Method from FormCookie */
    public void gotoEditor() {
        super.open();

    }

    /** Method from FormCookie */
    public void gotoForm() {
        synchronized(OPEN_FORM_LOCK) {
            if (!formLoaded)
                if (!loadForm()) return;
        }
        getFormTopComponent().open();
        getFormTopComponent().requestFocus();
    }

}
