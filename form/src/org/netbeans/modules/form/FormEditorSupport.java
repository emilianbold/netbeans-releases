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
import java.io.IOException;
import java.util.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditCookie;
import org.openide.loaders.*;
import org.openide.util.Utilities;
import org.openide.windows.*;

import org.netbeans.modules.java.JavaEditor;
import org.netbeans.modules.form.palette.PaletteTopComponent;

/**
 *
 * @author Ian Formanek
 */
public class FormEditorSupport extends JavaEditor implements FormCookie, EditCookie
{
    /** The FormModel instance holding the form itself */
    private FormModel formModel;

    /** The DataObject of the form */
    private FormDataObject formDataObject;

    /** The root node of form hierarchy presented in Component Inspector */
    private FormRootNode formRootNode;

    /** The designer of the form */
    private FormDesigner formDesigner;

    /** The code generator for the form */
//    private CodeGenerator codeGenerator;

    /** Persistence manager responsible for saving the form */
    private PersistenceManager saveManager;

    /** An indicator whether form has been loaded from the .form file */
    private boolean formLoaded = false; 

    /** An indicator whether the form should be opened additionally when
     * switching back from a non-editing workspace to an editing one */
    private boolean openOnEditing = false;

    // listeners
    private FormModelListener formListener;
    private PropertyChangeListener workspacesListener;
    private static PropertyChangeListener settingsListener;
    private static PropertyChangeListener topcompsListener;

    private UndoRedo.Manager undoManager;

    /** Table of FormModel instances (FormModel to FormEditorSupport map) */
    private static Hashtable openForms = new Hashtable();

    // --------------
    // constructor

    public FormEditorSupport(MultiDataObject.Entry javaEntry,
                             FormDataObject formDataObject) {
        super(javaEntry);
        this.formDataObject = formDataObject;
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
                        new Object[] { formDataObject.getName() }));

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
        if (formLoaded)
            return true; // form already loaded

        // first find PersistenceManager for loading the form
        PersistenceManager[] pms = recognizeForm(formDataObject);
        if (pms == null)
            return false;
        PersistenceManager loadManager = pms[0];
        saveManager = pms[1];

        // create and register new FormModel instance
        formModel = new FormModel();
        formModel.setName(formDataObject.getName());
        formModel.setReadOnly(formDataObject.isReadOnly());
        openForms.put(formModel, this);

        // load the form data (FormModel) and report errors
        try {
            loadManager.loadForm(formDataObject, formModel);

            reportErrors(false, null); // non fatal errors
        }
        catch (Throwable t) {
            if (t instanceof ThreadDeath)
                throw (ThreadDeath)t;

            saveManager = null;
            openForms.remove(formModel);
            formModel = null;

            reportErrors(true, t); // fatal errors

            return false;
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

    public FormDataObject getFormDataObject() {
        return formDataObject;
    }

    public Node getFormRootNode() {
        return formRootNode;
    }

    /** @return the FormModel of this form */
    public final FormModel getFormModel() {
        return formModel;
    }

    /** @return the FormDesigner for this form */
    FormDesigner getFormDesigner() {
        if (!formLoaded)
            return null;
        if (formDesigner == null)
            formDesigner = new FormDesigner(formModel);
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

    boolean supportsAdvancedFeatures() {
        return saveManager.supportsAdvancedFeatures();
    }

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
        undoManager = super.createUndoRedoManager();
        return undoManager;
    }

    UndoRedo.Manager getUndoManager() {
        return undoManager;
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
    private PersistenceManager[] recognizeForm(FormDataObject formDO) {
        PersistenceManager loadManager = null;
        PersistenceManager saveManager = null;

        for (Iterator it = PersistenceManager.getManagers(); it.hasNext(); ) {
            PersistenceManager man = (PersistenceManager)it.next();
            try {
                if (man.canLoadForm(formDO)) {
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
                            new Object[] { formDataObject.getName() }),
                        NotifyDescriptor.ERROR_MESSAGE));
            }
            else {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace();

                TopManager.getDefault().notify(
                    new NotifyDescriptor.Message(
                        java.text.MessageFormat.format(
                            FormEditor.getFormBundle().getString("FMT_ERR_LoadingFormDetails"),
                            new Object[] { formDataObject.getName(),
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

            ComponentInspector.getInstance().focusForm(this, true);
            PaletteTopComponent.getInstance().open();
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
        if (formLoaded && !formDataObject.formFileReadOnly()) {
            formModel.fireFormToBeSaved();
            try {
                saveManager.saveForm(formDataObject, formModel);
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
//        formModel.fireFormToBeClosed();
        openForms.remove(formModel);

        // remove nodes hierarchy
        formDataObject.getNodeDelegate().getChildren()
                                          .remove(new Node[] { formRootNode });

        // remove listeners
        detachFormListener();
        detachWorkspacesListener();
        if (openForms.isEmpty()) {
            ComponentInspector.getInstance().focusForm(null, false);
            detachSettingsListener();
            detachTopComponentsListener();

            PaletteTopComponent.getInstance().close();
        }
        else { // still any opened forms - focus some
            FormEditorSupport next = (FormEditorSupport)
                                     openForms.values().iterator().next();
            ComponentInspector.getInstance().focusForm(next);
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
        saveManager = null;
        formLoaded = false;
        formModel = null;
    }

    // -----------
    // listeners

    private void attachFormListener() {
        if (formListener != null || formDataObject.isReadOnly())
            return;

        // this listener ensures necessary updates according to changes in form
        formListener = new FormModelAdapter() {
            public void formChanged(FormModelEvent e) {
                // Mark form document modified explicitly when something changes
                // (so it must have been saved then). This is normally
                // done automatically by code regeneration, but not always.
                markFormModified();
            }

            // the following methods perform node updates
            public void containerLayoutChanged(FormModelEvent e) {
                updateNodeChildren(e.getContainer());
            }
            public void componentLayoutChanged(FormModelEvent e) {
                updateNodeChildren(e.getContainer());
            }
            public void componentAdded(FormModelEvent e) {
                updateNodeChildren(e.getContainer());
            }
            public void componentRemoved(FormModelEvent e) {
                updateNodeChildren(e.getContainer());
            }
            public void componentsReordered(FormModelEvent e) {
                updateNodeChildren(e.getContainer());
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

    private void attachWorkspacesListener() {
        openOnEditing = true;
        if (workspacesListener != null)
            return;

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
        if (settingsListener != null)
            return;

        settingsListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Enumeration enum = openForms.keys();
                while (enum.hasMoreElements()) {
                    FormModel formModel = (FormModel)enum.nextElement();
                    String propName = evt.getPropertyName();

                    if (FormLoaderSettings.PROP_USE_INDENT_ENGINE.equals(propName)
                        || FormLoaderSettings.PROP_VARIABLES_MODIFIER.equals(propName))
                    {
                        formModel.fireSyntheticPropertyChanged(null, propName,
                                        evt.getOldValue(), evt.getNewValue());
                    }
                    else if (FormLoaderSettings.PROP_SELECTION_BORDER_SIZE.equals(propName)
                          || FormLoaderSettings.PROP_SELECTION_BORDER_COLOR.equals(propName)
                          || FormLoaderSettings.PROP_CONNECTION_BORDER_COLOR.equals(propName))
                    {
                        formModel.getFormDesigner().repaint();
                    } else if (FormLoaderSettings.PROP_FORMDESIGNER_BACKGROUND_COLOR.equals(propName)) {
                        formModel.getFormDesigner().getFormDesignerPanel().updateBackgroundColor();
                    } else if (FormLoaderSettings.PROP_FORMDESIGNER_BORDER_COLOR.equals(propName)) {
                        formModel.getFormDesigner().getFormDesignerPanel().updateBorderColor();
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

                    String componentName = tc.getName();
                    if (componentName.endsWith("*"))
                        componentName = componentName.substring(0,componentName.length()-2);
                    componentName.trim();
                    boolean ext = componentName.endsWith(".java"); // NOI18N

                    Iterator it = openForms.values().iterator();
                    while (it.hasNext()) {
                        FormEditorSupport fes = (FormEditorSupport) it.next();
                        String formName = fes.getFormDataObject().getName();
                        if (ext)
                            formName += ".java"; // NOI18N
                        if (formName.equals(componentName)) {
                            ComponentInspector.getInstance().focusForm(fes);
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
