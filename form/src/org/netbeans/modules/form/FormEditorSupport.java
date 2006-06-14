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
import java.io.*;
import javax.swing.*;
import javax.swing.text.Document;
import org.netbeans.modules.java.JavaEditor.SimpleSection;

import org.openide.*;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.util.UserQuestionException;
import org.openide.windows.*;
import org.openide.text.*;
import org.openide.util.Utilities;
import org.netbeans.core.spi.multiview.*;
import org.netbeans.core.api.multiview.*;

import org.netbeans.modules.java.JavaEditor;
import org.netbeans.modules.java.parser.JavaParser;

/**
 *
 * @author Ian Formanek, Tomas Pavek
 */

public class FormEditorSupport extends JavaEditor
{
    /** ID of the form designer (in the multiview) */
    private static final String MV_FORM_ID = "form"; //NOI18N
    /** ID of the java editor (in the multiview) */
    private static final String MV_JAVA_ID = "java"; // NOI18N

    private static final int JAVA_ELEMENT_INDEX = 0;
    private static final int FORM_ELEMENT_INDEX = 1;
    private int elementToOpen; // default element index when multiview TC is created

    private static final String SECTION_INIT_COMPONENTS = "initComponents"; // NOI18N
    private static final String SECTION_VARIABLES = "variables"; // NOI18N
    
    /** Icon for the form editor multiview window */
    private static final String iconURL =
        "org/netbeans/modules/form/resources/form.gif"; // NOI18N

    /** The DataObject of the form */
    private FormDataObject formDataObject;

    /** The embracing multiview TopComponent (holds the form designer and
     * java editor) - we remeber the last active TopComponent (not all clones) */
    private CloneableTopComponent multiviewTC;

    // listeners
    private static PropertyChangeListener topcompsListener;

    private UndoRedo.Manager editorUndoManager;
    
    private FormEditor formEditor;

    /** Set of opened FormEditorSupport instances (java or form opened) */
    private static Set opened = Collections.synchronizedSet(new HashSet());
    
    private static Map fsToStatusListener = new HashMap();

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
    public void openFormEditor(boolean forceFormElement) {
        boolean alreadyOpened = opened.contains(this);
        boolean switchToForm = forceFormElement || !alreadyOpened;
        if (switchToForm) {
            elementToOpen = FORM_ELEMENT_INDEX;
        }
        multiviewTC = openCloneableTopComponent();
        multiviewTC.requestActive();

        if (switchToForm) {
            MultiViewHandler handler = MultiViews.findMultiViewHandler(multiviewTC);
            handler.requestActive(handler.getPerspectives()[FORM_ELEMENT_INDEX]);
        }
    }
    
    private void addStatusListener(FileSystem fs) {
        FileStatusListener fsl = (FileStatusListener)fsToStatusListener.get(fs);
        if (fsl == null) {
            fsl = new FileStatusListener() {
                public void annotationChanged(FileStatusEvent ev) {
                    Iterator iter = opened.iterator();
                    while (iter.hasNext()) {
                        FormEditorSupport fes = (FormEditorSupport)iter.next();
                        if (ev.hasChanged(fes.getFormDataObject().getPrimaryFile())
                            || ev.hasChanged(fes.getFormDataObject().getFormFile())) {
                            fes.updateMVTCDisplayName();
                        }
                    }
                }
            };
            fs.addFileStatusListener(fsl);
            fsToStatusListener.put(fs, fsl);
        } // else do nothing - the listener is already added
    }
    
    private static void detachStatusListeners() {
        Iterator iter = fsToStatusListener.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            FileSystem fs = (FileSystem)entry.getKey();
            FileStatusListener fsl = (FileStatusListener)entry.getValue();
            fs.removeFileStatusListener(fsl);
        }
        fsToStatusListener.clear();
    }
    
    void selectJavaEditor(){
        MultiViewHandler handler = MultiViews.findMultiViewHandler(multiviewTC);
        handler.requestActive(handler.getPerspectives()[JAVA_ELEMENT_INDEX]);        
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
        // Ensure initialization of the formEditor
        getFormEditor(true);
        return formEditor.loadForm();
    }
    
    /** @return true if the form is opened, false otherwise */
    public boolean isOpened() {
        return (formEditor != null) && formEditor.isFormLoaded();
    }

    private boolean saving; // workaround for bug 75225

    /** Save the document in this thread and start reparsing it.
     * @exception IOException on I/O error
     */
    public void saveDocument() throws IOException {
        IOException ioEx = null;
        try {
            if (formEditor != null) {
                formEditor.saveFormData();
                I18nSupport i18nSupport = formEditor.getI18nSupport();
                if (i18nSupport != null)
                    i18nSupport.autoSave();
            }
            saving = true; // workaround for bug 75225
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
        finally {
            saving = false; // workaround for bug 75225
        }
        if (formEditor != null) {
            formEditor.reportErrors(FormEditor.SAVING);
        }

        if (ioEx != null)
            throw ioEx;
    }

    void saveSourceOnly() throws IOException {
        try {
            saving = true; // workaround for bug 75225
            super.saveDocument();
        } finally {
            saving = false; // workaround for bug 75225
        }
    }

    // ------------
    // other interface methods

    /** @return data object representing the form */
    public final FormDataObject getFormDataObject() {
        return formDataObject;
    }
    
    // PENDING remove when form_new_layout is merged to trunk
    public static FormDataObject getFormDataObject(FormModel formModel) {
        return FormEditor.getFormDataObject(formModel);
    }
    public FormModel getFormModel() {
        FormEditor fe = getFormEditor();
        return (fe == null) ? null : fe.getFormModel();
    }
    // END of PENDING

    public FormEditor getFormEditor() {
        return getFormEditor(false);
    }
    
    FormEditor getFormEditor(boolean initialize) {
        if ((formEditor == null) && initialize) {
            formEditor = new FormEditor(formDataObject);
        }
        return formEditor;
    }

    /** Marks the form as modified if it's not yet. Used if changes made 
     * in form data don't affect the java source file (generated code). */
    void markFormModified() {
        if (formEditor.isFormLoaded() && !formDataObject.isModified()) {
            notifyModified();
        }
    }

    protected UndoRedo.Manager createUndoRedoManager() {
        editorUndoManager = super.createUndoRedoManager();
        return editorUndoManager;
    }

    void discardEditorUndoableEdits() {
        if (editorUndoManager != null)
            editorUndoManager.discardAllEdits();
    }

    // ------------
    // static getters

    JEditorPane getEditorPane() {
        return multiviewTC != null ?
            ((CloneableEditorSupport.Pane)multiviewTC).getEditorPane() : null;
    }

    // -----------
    // closing/reloading

    public void reloadForm() {
        if (canClose())
            reloadDocument();
    }

    protected org.openide.util.Task reloadDocument() {
        if (multiviewTC == null)
            return super.reloadDocument();

        org.openide.util.Task docLoadTask = super.reloadDocument();

        if (saving) // workaround for bug 75225
            return docLoadTask;

        // after reloading is done, open the form editor again
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FormDesigner formDesigner = getFormEditor(true).getFormDesigner();
                if (formDesigner == null) {                    
                    formDesigner = (FormDesigner)multiviewTC.getClientProperty("formDesigner"); // NOI18N
                }
		if(formDesigner==null) {
		    // if formDesigner is null then it haven't been activated yet...
		    return;
		}

                // close
                getFormEditor().closeForm();
                formEditor = null;
		
                formDesigner.reset(getFormEditor(true));
                getFormEditor().setFormDesigner(formDesigner);

                if(formDesigner.isShowing()) {
                    // load the form only if its open
                    loadForm();
                    FormEditor formEditor = getFormEditor();
                    formEditor.reportErrors(FormEditor.LOADING);
                    if (!formEditor.isFormLoaded()) { // there was a loading error
                        formDesigner.removeAll();
                    } else {
                        formDesigner.initialize();
                    }
                    ComponentInspector.getInstance().focusForm(formEditor);
                    formDesigner.revalidate();
                    formDesigner.repaint();
                }
            }
        });

        return docLoadTask;
    }

    protected void notifyClosed() {
        opened.remove(this);
        if (opened.isEmpty()) {
            detachTopComponentsListener();
            detachStatusListeners();
        }

        super.notifyClosed(); // close java editor
        if (formEditor != null) {
            I18nSupport i18nSupport = formEditor.getI18nSupport();
            if (i18nSupport != null)
                i18nSupport.close();
            formEditor.closeForm();
            formEditor = null;
            multiviewTC = null;
        }
        elementToOpen = JAVA_ELEMENT_INDEX;
    }

    private void multiViewClosed(CloneableTopComponent mvtc) {
        Enumeration en = mvtc.getReference().getComponents();
        boolean isLast = !en.hasMoreElements();
        if (multiviewTC == mvtc) {
            multiviewTC = null;
            FormDesigner formDesigner = null;
            // Find another multiviewTC, possibly with loaded formDesigner
            while (en.hasMoreElements()) {
                multiviewTC = (CloneableTopComponent)en.nextElement();
                FormDesigner designer = (FormDesigner)multiviewTC.getClientProperty("formDesigner"); // NOI18N
                if (designer != null) {
                    formDesigner = designer;
                    break;
                }
            }
            if (!isLast && (formDesigner == null)) {
                // Only Java elements are opened in the remaining clones
                if (formEditor != null) {
                    formEditor.closeForm();
                    formEditor = null;
                }
            }
        }
        
        if (isLast) // last view of this form closed
            notifyClosed();
    }

    protected boolean notifyModified () {
        boolean alreadyModified = isModified();
        boolean retVal = super.notifyModified();
        if (!alreadyModified) {
            FileObject formFile = formDataObject.getFormFile();
            if (!formFile.canWrite()) { // Issue 74092
                FileLock lock = null;
                try {
                    lock = formFile.lock();
                } catch (UserQuestionException uqex) {
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                        uqex.getLocalizedMessage(),
                        FormUtils.getBundleString("TITLE_UserQuestion"), // NOI18N
                        NotifyDescriptor.YES_NO_OPTION);
                    DialogDisplayer.getDefault().notify(nd);
                    if (NotifyDescriptor.YES_OPTION.equals(nd.getValue())) {
                        try {
                            uqex.confirmed();
                            EventQueue.invokeLater(new Runnable() {
                                public void run()  {
                                    reloadForm();
                                }
                            });         
                        } catch (IOException ioex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioex);
                        }
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                } finally {
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
            updateMVTCDisplayName();
        }
        return retVal;
    }

    protected void notifyUnmodified () {
        super.notifyUnmodified();
        updateMVTCDisplayName();
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
                                fes.getFormEditor().setFormDesigner(designer);
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
                    TopComponent active = TopComponent.getRegistry().getActivated();                    
                    if (active!=null && getSelectedElementType(active) != -1) { // it is our multiview
                        FormEditorSupport fes = getFormEditor(active);
                        if (fes != null) {                                                        
                            fes.updateMVTCDisplayName();
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

            TopComponentGroup group = WindowManager.getDefault()
                                        .findTopComponentGroup("form"); // NOI18N
            if (group != null)
                group.close();
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
        try {
            addStatusListener(formDataObject.getPrimaryFile().getFileSystem());
        } catch (FileStateInvalidException fsiex) {
            fsiex.printStackTrace();
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
    
    /**
     * Returns display name of the multiview top component.
     * The first item of the array is normal display name,
     * the second item of the array is HTML display name.
     *
     * @param formDataObject form data object representing the multiview tc.
     * @return display names of the MVTC. The second item can be <code>null</code>.
     */
    private static String[] getMVTCDisplayName(FormDataObject formDataObject) {  
        
        boolean readonly = !formDataObject.getPrimaryFile().canWrite();
        
        TopComponent active = TopComponent.getRegistry().getActivated();
        FormEditorSupport fes = formDataObject.getFormEditor();
        FormModel fm = null;
        if(fes!=null) {
            fm = fes.getFormModel();            
        }                    
        if( active!=null && getSelectedElementType(active) == FORM_ELEMENT_INDEX ) {            
            if(fm!=null) {
                readonly = readonly || fm.isReadOnly();                                    
            }                
        }        
        
        int version;
        if (formDataObject.isModified()) {
            version = readonly ? 2 : 1;
        } else {
            version = readonly ? 0 : 3;
        }

        Node node = formDataObject.getNodeDelegate();
        String htmlTitle = node.getHtmlDisplayName();
        String title = node.getDisplayName();
        if(fm!=null) {            
            FormDesigner fd = FormEditor.getFormDesigner(formDataObject.getFormEditor().getFormModel());
            if(fd!=null) {                
                if( fd.isShowing() && !fd.isTopRADComponent() && fd.getTopDesignComponent() != null ) {                                
                    title = FormUtils.getFormattedBundleString(
                                "FMT_FormTitleWithContainerName",          // NOI18N
                                new Object[] {title, fd.getTopDesignComponent().getName()}); 
                }                
            }            
        }                        
        if (htmlTitle != null) {
            if (!htmlTitle.trim().startsWith("<html>")) { // NOI18N
                htmlTitle = "<html>" + htmlTitle; // NOI18N
            }
        }
        return new String[] {
            FormUtils.getFormattedBundleString("FMT_FormMVTCTitle", new Object[] {new Integer(version), title}), // NOI18N
            (htmlTitle == null) ?
                null :
                FormUtils.getFormattedBundleString("FMT_FormMVTCTitle", new Object[] {new Integer(version), htmlTitle}) // NOI18N
        };
    }

    /** Updates title (display name) of all multiviews for given form. Replans
     * to event queue thread if necessary. */
    void updateMVTCDisplayName() {
        if (java.awt.EventQueue.isDispatchThread()) {
            updateMVTCDisplayNameInAWT();
        } else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    updateMVTCDisplayNameInAWT();
                }
            });
        }
    }
    
    private void updateMVTCDisplayNameInAWT() {
        if ((multiviewTC == null) || (!formDataObject.isValid())) // Issue 67544
            return;

        String[] titles = getMVTCDisplayName(formDataObject);
        Enumeration en = multiviewTC.getReference().getComponents();
        while (en.hasMoreElements()) {
            TopComponent tc = (TopComponent) en.nextElement();
            tc.setDisplayName(titles[0]);
            tc.setHtmlDisplayName(titles[1]);
        }
    }

    /** Updates tooltip of all multiviews for given form. Replans to even queue
     * thread if necessary. */
    void updateMVTCToolTipText() {
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
        String[] titles = getMVTCDisplayName(formDataObject);
        multiviewTC.setDisplayName(titles[0]);
        multiviewTC.setHtmlDisplayName(titles[1]);
        multiviewTC.setToolTipText(getMVTCToolTipText(formDataObject));
        opened.add(this);
        attachTopComponentsListener();
    }

    public static FormEditorSupport getFormEditor(TopComponent tc) {
        Object dobj = tc.getLookup().lookup(DataObject.class);
        return dobj instanceof FormDataObject ?
               ((FormDataObject)dobj).getFormEditorSupport() : null;
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
            final TopComponentGroup paletteGroup = wm.findTopComponentGroup( "commonpalette" ); // NOI18N
            if( null != paletteGroup ) {
                paletteGroup.open();
            }
            ComponentInspector inspector = ComponentInspector.getInstance();
            if (!Boolean.TRUE.equals(inspector.getClientProperty("isSliding"))) { // NOI18N
                inspector.requestVisible();
            }
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

    public JavaEditor.SimpleSection getVariablesSection() {
	return findSimpleSection(SECTION_VARIABLES);	
    }
    
    public JavaEditor.SimpleSection getInitComponentSection() {
	return findSimpleSection(SECTION_INIT_COMPONENTS);
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
                ((FormDataObject)dataObject).getFormEditorSupport() : null;
        }

        public MultiViewElement createElement() {
            FormEditorSupport formEditor = getFormEditor();
            return new FormDesigner((formEditor == null) ? null : formEditor.getFormEditor(true));
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
                ((FormDataObject)dataObject).getFormEditorSupport() : null;
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
                     implements MultiViewElement
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
                ((FormDataObject)((DataEditorSupport) cloneableEditorSupport ()).getDataObject()).getFormEditorSupport().setTopComponent(
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
            // Issue 52286 & 55818
            super.canClose(null, true);
            super.componentClosed();
        }

        public void componentShowing() {
            super.componentShowing();
            DataObject dob = ((DataEditorSupport)cloneableEditorSupport()).getDataObject();
            FormDataObject formDO = (FormDataObject)dob;
            FormModel model = null;
            if (formDO != null) {
                FormEditorSupport fe = formDO.getFormEditor();
                if (fe != null) {
                    model = fe.getFormModel();
                    if (model != null) {
                        JavaCodeGenerator codeGen = (JavaCodeGenerator)FormEditor.getCodeGenerator(model);
                        codeGen.regenerateCode();
                    }
                }
            }

        }

        public void componentHidden() {
            super.componentHidden();
        }

        public void componentOpened() {
            super.componentOpened();
            DataObject dob = ((DataEditorSupport)cloneableEditorSupport()).getDataObject();
            if ((multiViewObserver != null) && !(dob instanceof FormDataObject)) {
                multiViewObserver.getTopComponent().close(); // Issue 67879
                EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
                ec.open();
            }
        }

        public void updateName() {
            super.updateName();
            if (multiViewObserver != null) {
                FormDataObject formDataObject = (FormDataObject)((DataEditorSupport) cloneableEditorSupport ()).getDataObject();
                String[] titles = getMVTCDisplayName(formDataObject);
                setDisplayName(titles[0]);
                setHtmlDisplayName(titles[1]);
            }
        }

        protected boolean closeLast() {
            return true;
        }

        public CloseOperationState canCloseElement() {
            // if this is not the last cloned java editor component, closing is OK
            if (!FormEditorSupport.isLastView(multiViewObserver.getTopComponent()))
                return CloseOperationState.STATE_OK;

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
                ((FormDataObject)dataObject).getFormEditorSupport() : null;
        }

        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            FormEditorSupport formEditor = getFormEditor();
            return formEditor != null ? formEditor.canClose() : true;
        }
    }
    
}
