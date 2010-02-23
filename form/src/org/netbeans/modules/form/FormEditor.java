/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.form;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import java.awt.EventQueue;
import java.beans.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.*;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.form.actions.EditContainerAction;
import org.netbeans.modules.form.actions.EditFormAction;
import org.netbeans.modules.form.assistant.AssistantModel;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.netbeans.spi.palette.PaletteController;

import org.openide.*;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.actions.SystemAction;

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

    /** The FormJavaSource for the form */
    private FormJavaSource formJavaSource;
    
    /** ResourceSupport instance for the form */
    private ResourceSupport resourceSupport;

    /** Instance of binding support for the form.*/
    private BindingDesignSupport bindingSupport;

    /** List of exceptions occurred during the last persistence operation */
    private List<Throwable> persistenceErrors;
    
    /** Persistence manager responsible for saving the form */
    private PersistenceManager persistenceManager;
    
    /** An indicator whether the form has been loaded (from the .form file) */
    private boolean formLoaded = false;
    
    /** Table of opened FormModel instances (FormModel to FormEditor map) */
    private static Map<FormModel,FormEditor> openForms = new Hashtable<FormModel,FormEditor>();

    /* Maps form model to assistant model. */
    private static Map<FormModel,AssistantModel> formModelToAssistant = new WeakHashMap<FormModel,AssistantModel>();
    
    /** List of floating windows - must be closed when the form is closed. */
    private List<java.awt.Window> floatingWindows;
    
    /** The DataObject of the form */
    private FormDataObject formDataObject;
    private PropertyChangeListener dataObjectListener;
    private static PreferenceChangeListener settingsListener;
    private PropertyChangeListener paletteListener;
    
    // listeners
    private FormModelListener formListener;

    /** List of actions that are tried when a component is double-clicked. */
    private List<Action> defaultActions;

    /** Indicates that a task has been posted to ask the user about format
     * upgrade - not to show the confirmation dialog multiple times.
     */
    private boolean upgradeCheckPosted;

    // -----

    FormEditor(FormDataObject formDataObject) {
        this.formDataObject = formDataObject;
    }

    /** @return root node representing the form (in pair with the class node) */
    public final FormNode getFormRootNode() {
        return formRootNode;
    }

    public final FormNode getOthersContainerNode() {
        FormNode othersNode = formRootNode.getOthersNode();
        return othersNode != null ? othersNode : formRootNode;
    }

    /** @return the FormModel of this form, null if the form is not loaded */
    public final FormModel getFormModel() {
        return formModel;
    }
    
    public final FormDataObject getFormDataObject() {
        return formDataObject;
    }

    private final FormJavaSource getFormJavaSource() {
        return formJavaSource;
    }
    
    CodeGenerator getCodeGenerator() {
        if (!formLoaded)
            return null;
        if (codeGenerator == null)
            codeGenerator = new JavaCodeGenerator();
        return codeGenerator;
    }

    ResourceSupport getResourceSupport() {
        if (resourceSupport == null && formModel != null) {
            resourceSupport = new ResourceSupport(formModel);
            resourceSupport.init();
        }
        return resourceSupport;
    }

    BindingDesignSupport getBindingSupport() {
        if (bindingSupport == null && formModel != null) {
            bindingSupport = new BindingDesignSupport(formModel);
        }
        return bindingSupport;
    }

    /**
     * To be used just before loading a form to set a persistence manager that
     * already has the form recognized and superclass determined
     * (i.e. potentially long java parsing already done).
     */
    void setPersistenceManager(PersistenceManager pm) {
        persistenceManager = pm;
    }

    boolean isFormLoaded() {
        return formLoaded;
    }
    
    /** This methods loads the form, reports errors, creates the FormDesigner */
    void loadFormDesigner() {
        getFormDataObject().getFormEditorSupport().showOpeningStatus("FMT_OpeningForm"); // NOI18N

        preCreationUpdate();

        // load form data and report errors
        try {
            loadFormData();
        }
        catch (PersistenceException ex) { // a fatal loading error happened
            logPersistenceError(ex, 0);
            if (!formLoaded) { // loading failed - don't keep empty designer opened
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        getFormDataObject().getFormEditorSupport().selectJavaEditor();
                    }
                });
            }
        }

        getFormDataObject().getFormEditorSupport().hideOpeningStatus();

        // report errors during loading
        reportErrors(LOADING);

        // may do additional setup for just created form
        postCreationUpdate();
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
                    @Override
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

        // Issue 151166 - hack
        Project p = FileOwnerQuery.getOwner(formDataObject.getFormFile());
        // FileOwnerQuery.getOwner() on form opened using Template editor returns null
        if (p != null) {
            FileObject projectDirectory = p.getProjectDirectory();
            FileObject nbprojectFolder = projectDirectory.getFileObject("nbproject", null); // NOI18N
            if ((nbprojectFolder == null) && (projectDirectory.getFileObject("pom", "xml") != null)) { // NOI18N
                // Maven project
                ClassPathUtils.resetFormClassLoader(p);
            }
        }
 
        resetPersistenceErrorLog(); // clear log of errors

        // first find PersistenceManager for loading the form
        if (persistenceManager == null) {
            persistenceManager = recognizeForm(formDataObject);
        }

        // create and register new FormModel instance
        formModel = new FormModel();
        formModel.setName(formDataObject.getName());        
        formModel.setReadOnly(formDataObject.isReadOnly());		
        formJavaSource = new FormJavaSource(formDataObject);
	formModel.getCodeStructure().setFormJavaSource(formJavaSource);
	
        openForms.put(formModel, this);

        Logger.getLogger("TIMER").log(Level.FINE, "FormModel", new Object[] { formDataObject.getPrimaryFile(), formModel}); // NOI18N

        // Force initialization of Auto Set Component Name.
        // It cannot be initialized in constructor of FormModel,
        // because it may call getResourceSupport() which
        // requires formModel/FormEditor pair to be in openForms.
        formModel.getSettings().getAutoSetComponentName();

        // load the form data (FormModel) and report errors
        try {
            FormLAF.executeWithLookAndFeel(formModel, new Mutex.ExceptionAction() {
                @Override
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
                                
        // form is successfully loaded...
        formLoaded = true;
	
        getCodeGenerator().initialize(formModel);
        ResourceSupport resupport = getResourceSupport(); // make sure ResourceSupport is created and initialized
        if (resupport.getDesignLocale() != null) {
            resupport.updateDesignLocale();
        }

        getBindingSupport();
        formModel.fireFormLoaded();
        if (formModel.wasCorrected()) // model repaired or upgraded
            formModel.fireFormChanged(false);

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
        if (formLoaded && !formDataObject.formFileReadOnly() && !formModel.isReadOnly()) {
            formModel.fireFormToBeSaved();

            resetPersistenceErrorLog();

            persistenceManager.saveForm(formDataObject, formModel, persistenceErrors);
        }
    }
    
    private void resetPersistenceErrorLog() {
        if (persistenceErrors != null)
            persistenceErrors.clear();
        else
            persistenceErrors = new ArrayList<Throwable>();
    }
    
    private void logPersistenceError(Throwable t, int index) {
        if (persistenceErrors == null)
            persistenceErrors = new ArrayList<Throwable>();

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
        Iterator<PersistenceManager> it = PersistenceManager.getManagers();
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
            PersistenceManager pm = it.next();
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

    boolean anyPersistenceError() {
        return persistenceErrors != null && !persistenceErrors.isEmpty();
    }
    
    /** Reports errors occurred during loading or saving the form.
     * 
     * @param operation operation being performed.
     */
    public void reportErrors(int operation) {        
        if (!anyPersistenceError())
            return; // no errors or warnings logged

        final ErrorManager errorManager = ErrorManager.getDefault();
        final GandalfPersistenceManager persistManager = 
                    (GandalfPersistenceManager) persistenceManager;

        boolean checkLoadingErrors = operation == LOADING && formLoaded;
        boolean anyNonFatalLoadingError = false; // was there a real error?

        StringBuilder userErrorMsgs = new StringBuilder();
        
        for (Iterator it=persistenceErrors.iterator(); it.hasNext(); ) {
            Throwable t  = (Throwable) it.next();      
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
                if ((annotations != null) && (annotations.length != 0)) {
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
            errorManager.notify(ErrorManager.INFORMATIONAL, t);

            if (persistManager != null) {
                // form file was recognized, there is instance of persistance managager
                // creating report about problems while loading components, 
                // setting props of components, ...
                userErrorMsgs.append(persistManager.getExceptionAnnotation(t));
                userErrorMsgs.append("\n\n");  // NOI18N
            } else {
                // form file was not recognized
                errorManager.notify(ErrorManager.WARNING, t);
            }
        }
        
        if (checkLoadingErrors && anyNonFatalLoadingError) {
            // the form was loaded with some non-fatal errors - some data
            // was not loaded - show a warning about possible data loss
            final String wholeMsg = userErrorMsgs.append(
                    FormUtils.getBundleString("MSG_FormLoadedWithErrors")).toString();  // NOI18N

            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    // for some reason this would be displayed before the
                    // ErrorManager if not invoked later
                    if (!isFormLoaded()) {
                        return; // issue #164444
                    }
                    
                    JButton viewOnly = new JButton(FormUtils.getBundleString("CTL_ViewOnly"));		// NOI18N
                    JButton allowEditing = new JButton(FormUtils.getBundleString("CTL_AllowEditing"));	// NOI18N                                        
                    
                    Object ret = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                        wholeMsg,
                        FormUtils.getBundleString("CTL_FormLoadedWithErrors"), // NOI18N
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.WARNING_MESSAGE,
                        new Object[] { viewOnly, allowEditing, NotifyDescriptor.CANCEL_OPTION },
                        viewOnly ));
                   
                    if(ret == viewOnly) {
                        setFormReadOnly();
                    } else if(ret == allowEditing) {    
                        destroyInvalidComponents();
                    } else { // close form, switch to source editor
                        getFormDesigner().reset(FormEditor.this); // might be reused
                        closeForm();
                        getFormDataObject().getFormEditorSupport().selectJavaEditor();
                    }                                                      
                }
            });
        }

        resetPersistenceErrorLog();
    }    
    
    /**
     * Destroys all components from {@link #formModel} taged as invalid
     */
    private void destroyInvalidComponents() {
        Collection<RADComponent> allComps = formModel.getAllComponents();
        List<RADComponent> invalidComponents = new ArrayList<RADComponent>(allComps.size());
        // collect all invalid components
        for (RADComponent comp : allComps) {
            if(!comp.isValid()) {
                invalidComponents.add(comp);
            }
        }              
        // destroy all invalid components
        for (RADComponent comp : invalidComponents) {
            try {
                RADComponentNode node = comp.getNodeReference();
                if (node != null) {
                    node.destroy();
                }
            }
            catch (java.io.IOException ex) { // should not happen
                ex.printStackTrace();
            }                    
        }           
    }
    
    /**
     * Sets the FormEditor in Read-Only mode
     */
    private void setFormReadOnly() {
        formModel.setReadOnly(true);
        getFormDesigner().getHandleLayer().setViewOnly(true);                                                
        detachFormListener();
        getFormDataObject().getFormEditorSupport().updateMVTCDisplayName();                                
    }

    boolean needPostCreationUpdate() {
        return Boolean.TRUE.equals(formDataObject.getPrimaryFile().getAttribute("justCreatedByNewWizard")); // NOI18N
        // see o.n.m.f.w.TemplateWizardIterator.instantiate()
    }

    private void preCreationUpdate() {
        FileObject fob = formDataObject.getPrimaryFile();
        Object libName = fob.getAttribute("requiredLibrary"); // NOI18N
        if (libName != null) {
            Object className = fob.getAttribute("requiredClass"); // NOI18N
            if ((className == null) || !ClassPathUtils.isOnClassPath(fob, className.toString())) {
                try {
                    Library lib = LibraryManager.getDefault().getLibrary((String)libName);
                    ClassPathUtils.updateProject(fob, new ClassSource(
                            (className == null) ? null : className.toString(),
                            new ClassSource.LibraryEntry(lib))
                    );
                } catch (IOException ioex) {
                    Logger.getLogger(FormEditor.class.getName()).log(Level.INFO, ioex.getLocalizedMessage(), ioex);
                }
            }
        }
    }

    /**
     * Form just created by the user via the New wizard may need some additional
     * setup that can't be ensured by the static template. For example the type
     * of layout code generation needs to be honored, or properties
     * internationalized or converted to resources.
     */
    private void postCreationUpdate() {
        if (formLoaded && formModel != null && !formModel.isReadOnly()
            && needPostCreationUpdate()) // just created via New wizard
        {   // detect settings, update the form, regenerate code, save
            // make sure no upgrade warning is shown
            formModel.setMaxVersionLevel(FormModel.LATEST_VERSION);
            // switch to resources if needed
            FormLAF.executeWithLookAndFeel(formModel, new Runnable() {
                @Override
                public void run() {
                    getResourceSupport().prepareNewForm();
                }
            });
            // make sure layout code generation type is detected
            formModel.getSettings().getLayoutCodeTarget();
            // hack: regenerate code immediately
            // - needs to be forced since there might be no change fired
            // - don't wait for the next round, we want to save now
            formModel.fireFormChanged(true);
            // remove SuppressWarnings annotation if necessary
            checkSuppressWarningsAnnotation();
            // save the form if changed
            FormEditorSupport fes = formDataObject.getFormEditorSupport();
            try {
                if (fes.isModified()) {
                    saveFormData();
                    fes.saveSourceOnly();
                }
                formDataObject.getPrimaryFile().setAttribute("justCreatedByNewWizard", null); // NOI18N
                formDataObject.getPrimaryFile().setAttribute("nonEditableTemplate", null); // NOI18N
            }
            catch (Exception ex) { // no problem should happen for just created form
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
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
            formModelToAssistant.remove(formModel);
            formLoaded = false;
            
            // remove nodes hierarchy
            if (formDataObject.isValid()) {
                // Avoiding deadlock (issue 51796)
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (formDataObject.isValid()) {
                            formDataObject.getNodeDelegate().getChildren()
                                .remove(new Node[] { formRootNode });
                        }
                        formRootNode = null;
                    }
                });
            }
            
            // remove listeners
            detachFormListener();
            detachDataObjectListener();
            detachPaletteListener();

            // focus the next form in inspector
            FormEditor next;
            if (openForms.isEmpty()) {
                next = null;
                detachSettingsListener();
            }
            else { // still any opened forms - focus some
                next = openForms.values().iterator().next();
            }
            if (ComponentInspector.exists()) {
                ComponentInspector.getInstance().focusForm(next);
            }

            // close the floating windows
            if (floatingWindows != null) {
                if (floatingWindows.size() > 0) {
                    List<java.awt.Window> tempList = new LinkedList<java.awt.Window>(floatingWindows);
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
	    formJavaSource = null;
            resourceSupport = null;
            bindingSupport = null;
        }
    }
    
    private void attachFormListener() {
        if (formListener != null || formDataObject.isReadOnly() || formModel.isReadOnly())
            return;

        // this listener ensures necessary updates of nodes according to
        // changes in containers in form
        formListener = new FormModelListener() {
            @Override
            public void formChanged(FormModelEvent[] events) {
                if (events == null)
                    return;

                boolean modifying = false;
                Set<ComponentContainer> changedContainers = events.length > 0 ?
                                          new HashSet<ComponentContainer>() : null;
                Set<RADComponent> compsToSelect = null;
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
                            if (cont instanceof RADComponent) {
                                select = ((RADComponent)cont).getNodeReference();
                             } else {
                                select = getOthersContainerNode();
                             }

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
                                compsToSelect = new HashSet<RADComponent>();

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
                    getFormDataObject().getFormEditorSupport().markFormModified();
                    checkFormVersionUpgrade();
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
        FormNode node = null;

        if (metacont == null || metacont == formModel.getModelContainer()) {
            node = (formRootNode != null ? getOthersContainerNode() : null);
        } else if (metacont instanceof RADComponent) {
            node = ((RADComponent)metacont).getNodeReference();
        }

        if (node != null) {
            node.updateChildren();
        }
    }

    /**
     * After a round of changes check whether they did not require to upgrade
     * the form version. If the required version is higher than the version of
     * the IDE in which the form was created, ask the user for confirmation - to
     * let them know the form will not open in the older IDE anymore. If the
     * user refuses the upgrade, undo is performed (for that all the fired
     * changes must be already processed).
     */
    private void checkFormVersionUpgrade() {
        FormModel.FormVersion currentVersion = formModel.getCurrentVersionLevel();
        FormModel.FormVersion maxVersion = formModel.getMaxVersionLevel();
        if (currentVersion.ordinal() > maxVersion.ordinal()) {
            if (EventQueue.isDispatchThread()) {
                processVersionUpgrade(true);
            } else { // not a result of a user action, or some forgotten upgrade...
                confirmVersionUpgrade();
            }
        }
    }

    private void processVersionUpgrade(boolean processingEvents) {
        if (!processingEvents && formModel.hasPendingEvents()) {
            processingEvents = true;
        }
        if (processingEvents) { // post a task for later, if not already posted
            if (!upgradeCheckPosted) {
                upgradeCheckPosted = true;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        upgradeCheckPosted = false;
                        if (formModel != null) {
                            processVersionUpgrade(false);
                        }
                    }
                });
            }
        } else { // all events processed
            String upgradeOption = FormUtils.getBundleString("CTL_UpgradeOption"); // NOI18N
            String undoOption = FormUtils.getBundleString("CTL_CancelOption"); // NOI18N
            NotifyDescriptor d = new NotifyDescriptor(
                    FormUtils.getBundleString("MSG_UpgradeQuestion"), // NOI18N
                    FormUtils.getBundleString("TITLE_FormatUpgrade"), // NOI18N
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    new String[] { upgradeOption, undoOption},
                    upgradeOption);
            if (DialogDisplayer.getDefault().notify(d) == upgradeOption) {
                confirmVersionUpgrade();
            } else { // upgrade refused
                revertVersionUpgrade();
            }
        }
    }

    private void confirmVersionUpgrade() {
        if (formModel != null) {
            formModel.setMaxVersionLevel(FormModel.LATEST_VERSION);
        }
    }

    private void revertVersionUpgrade() {
        if (formModel != null) {
            formModel.getUndoRedoManager().undo();
            formModel.revertVersionLevel();
        }
    }

    private void attachDataObjectListener() {
        if (dataObjectListener != null)
            return;

        dataObjectListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent ev) {
                if (DataObject.PROP_NAME.equals(ev.getPropertyName())) {
                    // FormDataObject's name has changed
                    String name = formDataObject.getName();
                    formModel.setName(name);
                    formRootNode.updateName(name);
                    // multiview updated by FormEditorSupport
                    // code regenerated by FormRefactoringUpdate
                }
                else if (DataObject.PROP_COOKIE.equals(ev.getPropertyName())) {
                    if (ComponentInspector.exists()) { // #40224
                        Node[] nodes = ComponentInspector.getInstance()
                                 .getExplorerManager().getSelectedNodes();
                        for (int i=0; i < nodes.length; i++) {
                            ((FormNode)nodes[i]).updateCookies();
                        }
                    }
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

        settingsListener = new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                Iterator iter = openForms.keySet().iterator();
                while (iter.hasNext()) {
                    FormModel formModel = (FormModel) iter.next();
                    String propName = evt.getKey();

                    if (FormLoaderSettings.PROP_USE_INDENT_ENGINE.equals(propName)) {
                        formModel.fireSyntheticPropertyChanged(null, propName,
                                                               null, evt.getNewValue());
                    } else if (FormLoaderSettings.PROP_SELECTION_BORDER_SIZE.equals(propName)                    
                          || FormLoaderSettings.PROP_SELECTION_BORDER_COLOR.equals(propName)
                          || FormLoaderSettings.PROP_CONNECTION_BORDER_COLOR.equals(propName)
                          || FormLoaderSettings.PROP_FORMDESIGNER_BACKGROUND_COLOR.equals(propName)
                          || FormLoaderSettings.PROP_FORMDESIGNER_BORDER_COLOR.equals(propName))
                    {
                        FormDesigner designer = getFormDesigner(formModel);
                        if (designer != null) {
                            designer.updateVisualSettings();
                        }
                    } else if (FormLoaderSettings.PROP_PALETTE_IN_TOOLBAR.equals(propName)) {
                        FormDesigner designer = getFormDesigner(formModel);
                        if (designer != null) {
                            designer.getFormToolBar().showPaletteButton(
                                FormLoaderSettings.getInstance().isPaletteInToolBar());
                        }
                    }
                }
            }
        };

        FormLoaderSettings.getPreferences().addPreferenceChangeListener(settingsListener);
    }

    private static void detachSettingsListener() {
        if (settingsListener != null) {
            FormLoaderSettings.getPreferences().removePreferenceChangeListener(settingsListener);
            settingsListener = null;
        }
    }
    
    private void attachPaletteListener() {
        if (paletteListener != null)
            return;

        paletteListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (PaletteController.PROP_SELECTED_ITEM.equals(evt.getPropertyName())) {
                    FormModel formModel = getFormModel();
                    if (isFormLoaded() && formModel != null && !formModel.isReadOnly()) {
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

        PaletteUtils.addPaletteListener(paletteListener, formDataObject.getPrimaryFile());
    }

    private void detachPaletteListener() {
        if (paletteListener != null) {
            PaletteUtils.removePaletteListener(paletteListener, formDataObject.getPrimaryFile());
            paletteListener = null;
        }
    }

    void reinstallListener() {
        if (formListener != null) {
            formModel.removeFormModelListener(formListener);
            formModel.addFormModelListener(formListener);
        }
    }

    /**
     * Returns code editor pane for the specified form.
     * 
     * @param formModel form model.
     * @return JEditorPane set up with the actuall forms java source*/
    public static JEditorPane createCodeEditorPane(FormModel formModel) {                        
        FormDataObject dobj = getFormDataObject(formModel);
        JavaCodeGenerator codeGen = (JavaCodeGenerator) FormEditor.getCodeGenerator(formModel);
        codeGen.regenerateCode();

        JEditorPane codePane = new JEditorPane();
        SimpleSection sec = dobj.getFormEditorSupport().getInitComponentSection();
        int pos = sec.getText().indexOf('{') + 2 + sec.getStartPosition().getOffset();
        FormUtils.setupEditorPane(codePane, dobj.getPrimaryFile(), pos);
        return codePane;
    }

    public static synchronized AssistantModel getAssistantModel(FormModel formModel) {
        assert (formModel != null);
        AssistantModel assistant = formModelToAssistant.get(formModel);
        if (assistant == null) {
            assistant = new AssistantModel();
            formModelToAssistant.put(formModel, assistant);
        }
        return assistant;
    }

    /**
     * @param formModel form model.
     * @return FormDesigner for given form */
    public static FormDesigner getFormDesigner(FormModel formModel) {
        FormEditor formEditor = openForms.get(formModel);
        return formEditor != null ? formEditor.getFormDesigner() : null;
    }

    /**
     * Returns code generator for the specified form.
     * 
     * @param formModel form model.
     * @return CodeGenerator for given form */
    public static CodeGenerator getCodeGenerator(FormModel formModel) {
        FormEditor formEditor = openForms.get(formModel);
        return formEditor != null ? formEditor.getCodeGenerator() : null;
    }

    /**
     * Returns form data object for the specified form.
     * 
     * @param formModel form model.
     * @return FormDataObject of given form */
    public static FormDataObject getFormDataObject(FormModel formModel) {
        FormEditor formEditor = openForms.get(formModel);
        return formEditor != null ? formEditor.getFormDataObject() : null;
    }

    /**
     * Returns <code>FormJavaSource</code> for the specified form.
     * 
     * @param formModel form model.
     * @return FormJavaSource of given form */
    public static FormJavaSource getFormJavaSource(FormModel formModel) {
        FormEditor formEditor = openForms.get(formModel);
        return formEditor != null ? formEditor.getFormJavaSource() : null;
    }

    /**
     * Returns <code>ResourceSupport</code> for the specified form.
     * 
     * @param formModel form model.
     * @return ResourceSupport of given form */
    static ResourceSupport getResourceSupport(FormModel formModel) {
        FormEditor formEditor = openForms.get(formModel);
        return formEditor != null ? formEditor.getResourceSupport() : null;
    }

    /**
     * Returns <code>BindingDesignSupport</code> for the specified form.
     * 
     * @param formModel form model.
     * @return BindingDesignSupport of given form */
    static BindingDesignSupport getBindingSupport(FormModel formModel) {
        FormEditor formEditor = openForms.get(formModel);
        return formEditor != null ? formEditor.getBindingSupport() : null;
    }

    /**
     * Returns form editor for the specified form.
     * 
     * @param formModel form model.
     * @return FormEditor instance for given form */
    public static FormEditor getFormEditor(FormModel formModel) {
        return openForms.get(formModel);
    }
    
    UndoRedo.Manager getFormUndoRedoManager() {
        return formModel != null ? formModel.getUndoRedoManager() : null;
    }
    
    public void registerFloatingWindow(java.awt.Window window) {
        if (floatingWindows == null)
            floatingWindows = new ArrayList<java.awt.Window>();
        else
            floatingWindows.remove(window);
        floatingWindows.add(window);
    }

    public void unregisterFloatingWindow(java.awt.Window window) {
        if (floatingWindows != null)
            floatingWindows.remove(window);
    }

    public void registerDefaultComponentAction(Action action) {
        if (defaultActions == null) {
            createDefaultComponentActionsList();
        } else {
            defaultActions.remove(action);
        }
        defaultActions.add(0, action);
    }

    public void unregisterDefaultComponentAction(Action action) {
        if (defaultActions != null) {
            defaultActions.remove(action);
        }
    }

    private void createDefaultComponentActionsList() {
        defaultActions = new LinkedList<Action>();
        defaultActions.add(SystemAction.get(EditContainerAction.class));
        defaultActions.add(SystemAction.get(EditFormAction.class));
        defaultActions.add(SystemAction.get(DefaultRADAction.class));
    }

    Collection<Action> getDefaultComponentActions() {
        if (defaultActions == null) {
            createDefaultComponentActionsList();
        }
        return Collections.unmodifiableList(defaultActions);
    }

    /**
     * Updates project classpath with the layout extensions library.
     * 
     * @param formModel form model.
     * @return <code>true</code> if the project was updated.
     */
    public static boolean updateProjectForNaturalLayout(FormModel formModel) {
        FormEditor formEditor = getFormEditor(formModel);
        if (formEditor != null
                && formModel.getSettings().getLayoutCodeTarget() != JavaCodeGenerator.LAYOUT_CODE_JDK6
                && !ClassPathUtils.isOnClassPath(formEditor.getFormDataObject().getFormFile(), org.jdesktop.layout.GroupLayout.class.getName())) {
            try {
                Library lib = LibraryManager.getDefault().getLibrary("swing-layout"); // NOI18N
                if (lib == null) {
                    return false;
                }
                ClassSource cs = new ClassSource("", // class name is not needed // NOI18N
                                                 new ClassSource.LibraryEntry(lib));
                return Boolean.TRUE == ClassPathUtils.updateProject(formEditor.getFormDataObject().getFormFile(), cs);
            }
            catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            catch (RuntimeException ex) { // e.g. UnsupportedOperationException
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return false;
    }

    /**
     * Updates project classpath with the beans binding library.
     * 
     * @param formModel form model.
     * @return <code>true</code> if the project was updated.
     */
    public static boolean updateProjectForBeansBinding(FormModel formModel) {
        FormEditor formEditor = getFormEditor(formModel);
        if (formEditor != null
                && !ClassPathUtils.isOnClassPath(formEditor.getFormDataObject().getFormFile(), org.jdesktop.beansbinding.Binding.class.getName())) {
            try {
                Library lib = LibraryManager.getDefault().getLibrary("beans-binding"); // NOI18N
                if (lib == null) {
                    return false;
                }
                ClassSource cs = new ClassSource("", // class name is not needed // NOI18N
                                                 new ClassSource.LibraryEntry(lib));
                return Boolean.TRUE == ClassPathUtils.updateProject(formEditor.getFormDataObject().getFormFile(), cs);
            }
            catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return false;
    }

    public static boolean isNonVisualTrayEnabled() {
        return Boolean.getBoolean("netbeans.form.non_visual_tray"); // NOI18N
    }

    private void checkSuppressWarningsAnnotation() {
        FileObject fo = getFormDataObject().getPrimaryFile();
        String sourceLevel = SourceLevelQuery.getSourceLevel(fo);
        boolean invalidSL = (sourceLevel != null) && ("1.5".compareTo(sourceLevel) > 0); // NOI18N
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.BOOT);
        if (invalidSL || cp.findResource("java/lang/SuppressWarnings.class") == null) { // NOI18N
            // The project's bootclasspath doesn't contain SuppressWarnings class.
            // So, remove this annotation from initComponents() method.
            final String foName = fo.getName();
            JavaSource js = JavaSource.forFileObject(fo);
            final int[] positions = new int[] {-1,-1};
            try {
                js.runModificationTask(new CancellableTask<WorkingCopy>() {
                    @Override
                    public void cancel() {
                    }
                    @Override
                    public void run(WorkingCopy wcopy) throws Exception {
                        wcopy.toPhase(JavaSource.Phase.RESOLVED);

                        ClassTree clazz = null;
                        CompilationUnitTree cu = wcopy.getCompilationUnit();
                        for (Tree tree : cu.getTypeDecls()) {
                            if (tree.getKind() == Tree.Kind.CLASS) {
                                ClassTree cand = (ClassTree)tree;
                                if (foName.equals(cand.getSimpleName().toString())) {
                                    clazz = cand;
                                }
                            }
                        }
                        if (clazz == null) return;
                        
                        for (Tree tree : clazz.getMembers()) {
                            if (tree.getKind() == Tree.Kind.METHOD) {
                                MethodTree method = (MethodTree)tree;
                                if ("initComponents".equals(method.getName().toString()) // NOI18N
                                        && (method.getParameters().isEmpty())) {
                                    ModifiersTree modifiers = method.getModifiers();
                                    for (AnnotationTree annotation : modifiers.getAnnotations()) {
                                        if (annotation.getAnnotationType().toString().contains("SuppressWarnings")) { // NOI18N
                                            SourcePositions sp = wcopy.getTrees().getSourcePositions();
                                            positions[0] = (int)sp.getStartPosition(cu, annotation);
                                            positions[1] = (int)sp.getEndPosition(cu, annotation);
                                            // We cannot use the following code because
                                            // part of the modifier is in guarded block
                                            //ModifiersTree newModifiers = wcopy.getTreeMaker().removeModifiersAnnotation(method.getModifiers(), annotation);
                                            //wcopy.rewrite(modifiers, newModifiers);
                                        }
                                    }
                                }
                            }
                        }

                    }
                }).commit();
            } catch (IOException ioex) {
                Logger.getLogger(FormEditor.class.getName()).log(Level.INFO, ioex.getLocalizedMessage(), ioex);
            }
            if (positions[0] != -1) {
                try {
                    getFormDataObject().getFormEditorSupport().getDocument().remove(positions[0], positions[1]-positions[0]);
                } catch (BadLocationException blex) {
                    Logger.getLogger(FormEditor.class.getName()).log(Level.INFO, blex.getLocalizedMessage(), blex);
                }
            }
        }
    }

}
