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

import org.openide.*;
import org.openide.nodes.Node;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
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

    /** Save the document in this thread and start reparsing it.
     * @exception IOException on I/O error
     */
    public void saveDocument() throws IOException {
        IOException ioEx = null;
        try {
            if (formEditor != null) {
                formEditor.saveFormData();
            }
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
        if (formEditor != null) {
            formEditor.reportErrors(FormEditor.SAVING);
        }

        if (ioEx != null)
            throw ioEx;
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
        return getFormEditor().getFormModel();
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
        if (formEditor.isFormLoaded() && !formDataObject.isModified())
            super.notifyModified();
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
        return ((CloneableEditorSupport.Pane)multiviewTC).getEditorPane();
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
        if (formEditor != null) {
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
        if (!alreadyModified)
            updateMVTCDisplayName();
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
        multiviewTC.setDisplayName(getMVTCDisplayName(formDataObject));
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
            FormDesigner designer = null;
            FormEditorSupport formEditor = getFormEditor();
            if (formEditor != null) {
                designer = new FormDesigner(formEditor.getFormEditor(true));
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
