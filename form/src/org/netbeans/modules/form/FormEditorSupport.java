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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import java.awt.Cursor;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.core.spi.multiview.SourceViewMarker;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.UndoRedo;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.text.PositionRef;
import org.openide.util.ImageUtilities;
import org.openide.util.UserQuestionException;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Ian Formanek, Tomas Pavek
 */

public class FormEditorSupport extends DataEditorSupport implements EditorCookie.Observable, CloseCookie, PrintCookie {
    
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
    private static Set<FormEditorSupport> opened = Collections.synchronizedSet(new HashSet<FormEditorSupport>());
    
    private static Map<FileSystem,FileStatusListener> fsToStatusListener = new HashMap<FileSystem,FileStatusListener>();
    
    // --------------
    // constructor
    
    public FormEditorSupport(MultiDataObject.Entry javaEntry,
            FormDataObject formDataObject,
            CookieSet cookies) {
        super(formDataObject, new Environment(formDataObject));
        setMIMEType("text/x-java"); // NOI18N
        this.formDataObject = formDataObject;
        this.cookies = cookies;
    }
    
    // ----------
    // opening & saving interface methods
    
    /** Main entry method. Called by OpenCookie implementation - opens the form.
     * 
     * @param forceFormElement determines whether we should force switch to form element.
     * @see OpenCookie#open
     */
    public void openFormEditor(boolean forceFormElement) {
        boolean alreadyOpened = opened.contains(this);
        boolean switchToForm = forceFormElement || !alreadyOpened;
        if (switchToForm) {
            elementToOpen = FORM_ELEMENT_INDEX;
        }
        try {
            showOpeningStatus("FMT_PreparingForm"); // NOI18N

            multiviewTC = openCloneableTopComponent();
            multiviewTC.requestActive();

            if (switchToForm) {
                MultiViewHandler handler = MultiViews.findMultiViewHandler(multiviewTC);
                handler.requestActive(handler.getPerspectives()[FORM_ELEMENT_INDEX]);
            }
        } finally {
            hideOpeningStatus();
        }
    }

    void showOpeningStatus(String fmtMessage) {
        JFrame mainWin = (JFrame) WindowManager.getDefault().getMainWindow();

        // set status text like "Opening form: ..."
        StatusDisplayer.getDefault().setStatusText(
            FormUtils.getFormattedBundleString(
                fmtMessage, // NOI18N
                new Object[] { formDataObject.getFormFile().getName() }));
        javax.swing.RepaintManager.currentManager(mainWin).paintDirtyRegions();

        // set wait cursor [is not very reliable, but...]
        mainWin.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        mainWin.getGlassPane().setVisible(true);
    }

    void hideOpeningStatus() {
        // clear wait cursor
        JFrame mainWin = (JFrame) WindowManager.getDefault().getMainWindow();
        mainWin.getGlassPane().setVisible(false);
        mainWin.getGlassPane().setCursor(null);

        StatusDisplayer.getDefault().setStatusText(""); // NOI18N
    }

    private void addStatusListener(FileSystem fs) {
        FileStatusListener fsl = fsToStatusListener.get(fs);
        if (fsl == null) {
            fsl = new FileStatusListener() {
                @Override
                public void annotationChanged(FileStatusEvent ev) {
                    Iterator<FormEditorSupport> iter = opened.iterator();
                    while (iter.hasNext()) {
                        FormEditorSupport fes = iter.next();
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
        if (handler != null) {
            handler.requestActive(handler.getPerspectives()[JAVA_ELEMENT_INDEX]);
        }
    }
    
    /** Overriden from JavaEditor - opens editor and ensures it is selected
     * in the multiview.
     */
    @Override
    public void open() {
        if (EventQueue.isDispatchThread()) {
            openInAWT();
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    openInAWT();
                }
            });
        }
    }
    
    private void openInAWT() {
        if (!formDataObject.isValid()) {
            return;
        }
        if (Boolean.TRUE.equals(formDataObject.getPrimaryFile().getAttribute("nonEditableTemplate"))) { // NOI18N
            String pattern = FormUtils.getBundleString("MSG_NonEditableTemplate"); // NOI18N
            String message = MessageFormat.format(pattern, new Object[] {formDataObject.getNodeDelegate().getName()});
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
            return;
        }
        elementToOpen = JAVA_ELEMENT_INDEX;
        super.open();
        
        // This method must be executed in AWT thread because
        // otherwise multiview is opened in AWT using invokeLater
        // and we don't have multiviewTC correctly set
        MultiViewHandler handler = MultiViews.findMultiViewHandler(multiviewTC);
        if (handler != null) {
            handler.requestActive(handler.getPerspectives()[JAVA_ELEMENT_INDEX]);
        }
    }
    
    /** Overriden from JavaEditor - opens editor at given position and ensures
     * it is selected in the multiview.
     * 
     * @param pos position
     */
    public void openAt(PositionRef pos) {
        elementToOpen = JAVA_ELEMENT_INDEX;
        openCloneableTopComponent();
        
        MultiViewHandler handler = MultiViews.findMultiViewHandler(multiviewTC);
        handler.requestActive(handler.getPerspectives()[JAVA_ELEMENT_INDEX]);
        
        openAt(pos, -1).getComponent().requestActive();
    }
    
    public void openAt(Position pos) {
        openAt(createPositionRef(pos.getOffset(), Position.Bias.Forward));
    }
    
    /** Public method for loading form data from file. Does not open the
     * source editor and designer, does not report errors and does not throw
     * any exceptions. Runs in AWT event dispatch thread, returns after the
     * form is loaded (even if not called from AWT thread).
     * @return whether the form is loaded (true also if it already was)
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
    @Override
    public void saveDocument() throws IOException {
        IOException ioEx = null;
        try {
            if (formEditor != null) {
                formEditor.saveFormData();
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
        if (formEditor != null && formEditor.isFormLoaded() && !formDataObject.isModified()) {
            notifyModified();
        }
    }
    
    @Override
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
    
    @Override
    protected org.openide.util.Task reloadDocument() {
        if (multiviewTC == null)
            return super.reloadDocument();
        
        org.openide.util.Task docLoadTask = super.reloadDocument();
        
        if (saving) // workaround for bug 75225
            return docLoadTask;
        
        // after reloading is done, open the form editor again
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                FormDesigner formDesigner = getFormEditor(true).getFormDesigner();
                if (formDesigner == null) {
                    if (multiviewTC == null) {
                        return; // Issue 150534
                    } else {
                        formDesigner = (FormDesigner)multiviewTC.getClientProperty("formDesigner"); // NOI18N
                    }
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

    public FormEditor reloadFormEditor() {
        if (formEditor == null || !formEditor.isFormLoaded()) {
            return null;
        }
        FormDesigner formDesigner = formEditor.getFormDesigner();
        if (formDesigner == null) {
            formDesigner = (FormDesigner)multiviewTC.getClientProperty("formDesigner"); // NOI18N
        }
        formEditor.closeForm();
        formEditor = null;
        formEditor = getFormEditor(true); // new FormEditor instance
        if (formDesigner != null) {
            formDesigner.reset(formEditor);
            formEditor.setFormDesigner(formDesigner);
        }
        loadForm();
        formEditor.reportErrors(FormEditor.LOADING);
        if (formEditor.isFormLoaded() && formDesigner != null && formDesigner.isShowing()) {
            formDesigner.initialize();
        }
        return formEditor;
    }

    public void closeFormEditor() {
        if (isOpened()) {
            final FormDesigner formDesigner = formEditor.getFormDesigner();
            formEditor.closeForm();
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    if (formDesigner != null) {
                        formDesigner.reset(formEditor); // might be reused
                    }
                    selectJavaEditor();
                }
            };
            if (EventQueue.isDispatchThread()) {
                run.run();
            } else {
                try             {
                    java.awt.EventQueue.invokeAndWait(run);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }

    @Override
    protected void notifyClosed() {
        opened.remove(this);
        if (opened.isEmpty()) {
            detachTopComponentsListener();
            detachStatusListeners();
        }
        
        super.notifyClosed(); // close java editor
        if (formEditor != null) {
            formEditor.closeForm();
            formEditor = null;
        }
        multiviewTC = null;
        guardedProvider = null;
        guardedEditor = null;
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
    
    @Override
    protected boolean notifyModified () {
        boolean alreadyModified = isModified();
        boolean retVal = super.notifyModified();
        
        if (retVal) { // java source modification
            addSaveCookie();
        }
        
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
                                @Override
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
    
    @Override
    protected void notifyUnmodified () {
        super.notifyUnmodified();
         // java source modification
        removeSaveCookie();
        updateMVTCDisplayName();
    }
    
    private static void attachTopComponentsListener() {
        if (topcompsListener != null)
            return;
        
        topcompsListener = new PropertyChangeListener() {
            @Override
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
                    Set oldSet = (Set) ev.getOldValue();
                    Set newSet = (Set) ev.getNewValue();
                    if (newSet.size() < oldSet.size()) {
                        Iterator it = oldSet.iterator();
                        while (it.hasNext()) {
                            Object o = it.next();
                            if (!newSet.contains(o)) {
                                if (o instanceof CloneableTopComponent) {
                                    CloneableTopComponent closedTC = (CloneableTopComponent) o;
                                    if (getSelectedElementType(closedTC) != -1) { // it is our multiview
                                        FormEditorSupport fes = getFormEditor(closedTC);
                                        if (fes != null) {
                                            fes.multiViewClosed(closedTC);
                                        }
                                    }
                                }
                            }
                        }
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
    
    @Override
    protected CloneableEditorSupport.Pane createPane() {
        if (!formDataObject.isValid()) {
            return super.createPane(); // Issue 110249
        } 
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

    private static boolean readOnly(FormDataObject formDataObject) {
        if (!formDataObject.getPrimaryFile().canWrite()) {
            return true;
        }
        TopComponent active = TopComponent.getRegistry().getActivated();
        if (active != null && getSelectedElementType(active) == FORM_ELEMENT_INDEX) {
            FormEditorSupport fes = formDataObject.getFormEditor();
            if (fes != null) {
                FormModel fm = fes.getFormModel();
                if (fm != null) {
                    return fm.isReadOnly();
                }
            }
        }
        return false;
    }
    
    private static String getMVTCToolTipText(FormDataObject formDataObject) {
        return DataEditorSupport.toolTip(formDataObject.getPrimaryFile(), formDataObject.isModified(), readOnly(formDataObject));
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
        Node node = formDataObject.getNodeDelegate();
        String title = node.getDisplayName();
        String htmlTitle = node.getHtmlDisplayName();
        if (htmlTitle == null) {
            try {
                htmlTitle = XMLUtil.toElementContent(title);
            } catch (CharConversionException x) {
                htmlTitle = "???";
            }
        }
        FormEditorSupport fes = formDataObject.getFormEditor();
        if (fes != null) {
            FormModel fm = fes.getFormModel();
            if (fm != null) {
                FormDesigner fd = FormEditor.getFormDesigner(formDataObject.getFormEditor().getFormModel());
                if (fd != null && fd.getFormModel() != null) {
                    if (fd.isShowing() && !fd.isTopRADComponent() && fd.getTopDesignComponent() != null) {
                        title = FormUtils.getFormattedBundleString(
                                "FMT_FormTitleWithContainerName", // NOI18N
                                new Object[] {title, fd.getTopDesignComponent().getName()});
                        htmlTitle = FormUtils.getFormattedBundleString(
                                "FMT_FormTitleWithContainerName", // NOI18N
                                new Object[] {htmlTitle, fd.getTopDesignComponent().getName()});
                    }
                }
            }
        }
        boolean modified = formDataObject.isModified();
        boolean readOnly = readOnly(formDataObject);
        return new String[] {
            DataEditorSupport.annotateName(title, false, modified, readOnly),
            DataEditorSupport.annotateName(htmlTitle, true, modified, readOnly)
        };
    }
    
    /** Updates title (display name) of all multiviews for given form. Replans
     * to event queue thread if necessary. */
    void updateMVTCDisplayName() {
        if (java.awt.EventQueue.isDispatchThread()) {
            updateMVTCDisplayNameInAWT();
        } else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
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
                @Override
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
            // Bug 116008: calling group.open() first time may cause hiding the
            // FormDesigner (some winsys multiview initialization mess), calling
            // this method again and hiding the group. By setting the groupVisible
            // to false we make the re-entrant call effectively do nothing.
            groupVisible = Boolean.FALSE;
            group.open();
            groupVisible = Boolean.TRUE;
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
            groupVisible = Boolean.FALSE;
        }
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
    
    public SimpleSection getVariablesSection() {
        return getGuardedSectionManager().findSimpleSection(SECTION_VARIABLES);
    }
    
    public SimpleSection getInitComponentSection() {
        return getGuardedSectionManager().findSimpleSection(SECTION_INIT_COMPONENTS);
    }
    
    public GuardedSectionManager getGuardedSectionManager() {
        try {
            StyledDocument doc = null;
            try {
                doc = openDocument();
            } catch (UserQuestionException uqex) { // Issue 143655
                Object retVal = DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Confirmation(uqex.getLocalizedMessage(),
                            NotifyDescriptor.YES_NO_OPTION));
                if (NotifyDescriptor.YES_OPTION == retVal) {
                    uqex.confirmed();
                    doc = openDocument();
                }
            }
            return (doc == null) ? null : GuardedSectionManager.getInstance(doc);
        } catch (IOException ex) {
            throw (IllegalStateException) new IllegalStateException("cannot open document").initCause(ex); // NOI18N
        }
    }

    private final class FormGEditor implements GuardedEditorSupport {
        
        StyledDocument doc = null;
        
        @Override
        public StyledDocument getDocument() {
            return FormGEditor.this.doc;
        }
    }
    
    private FormGEditor guardedEditor;
    private GuardedSectionsProvider guardedProvider;
    
    @Override
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        if (guardedEditor == null) {
            guardedEditor = new FormGEditor();
            GuardedSectionsFactory gFactory = GuardedSectionsFactory.find(((DataEditorSupport.Env) env).getMimeType());
            if (gFactory != null) {
                guardedProvider = gFactory.create(guardedEditor);
            }
        }
        
        if (guardedProvider != null) {
            guardedEditor.doc = doc;
            Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
            Reader reader = guardedProvider.createGuardedReader(stream, c);
            try {
                kit.read(reader, doc, 0);
            } finally {
                reader.close();
            }
        } else {
            super.loadFromStreamToKit(doc, stream, kit);
        }
    }

    @Override
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        if (guardedProvider != null) {
            Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
            Writer writer = guardedProvider.createGuardedWriter(stream, c);
            try {
                kit.write(writer, doc, 0, doc.getLength());
            } finally {
                writer.close();
            }
        } else {
            super.saveFromKitToStream(doc, kit, stream);
        }
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
        
        @Override
        public MultiViewElement createElement() {
            FormEditorSupport formEditor = getFormEditor();
            return new FormDesigner((formEditor == null) ? null : formEditor.getFormEditor(true));
        }
        
        @Override
        public String getDisplayName() {
            return FormUtils.getBundleString("CTL_DesignTabCaption"); // NOI18N
        }
        
        @Override
        public org.openide.util.HelpCtx getHelpCtx() {
            return org.openide.util.HelpCtx.DEFAULT_HELP;
        }
        
        @Override
        public java.awt.Image getIcon() {
            return ImageUtilities.loadImage(iconURL);
        }
        
        @Override
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_NEVER;
        }
        
        @Override
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
    private static class JavaDesc implements MultiViewDescription, SourceViewMarker, Serializable {
        
        private static final long serialVersionUID =-3126744316624172415L;
        
        private DataObject dataObject;
        
        private JavaDesc() {
        }
        
        public JavaDesc(DataObject formDO) {
            dataObject = formDO;
        }
        
        private FormEditorSupport getJavaEditor() {
            return dataObject != null && dataObject instanceof FormDataObject ?
                ((FormDataObject)dataObject).getFormEditorSupport() : null;
        }
        
        @Override
        public MultiViewElement createElement() {
            FormEditorSupport javaEditor = getJavaEditor();
            if (javaEditor != null) {
                javaEditor.prepareDocument();
                JavaEditorTopComponent editor = new JavaEditorTopComponent(dataObject.getCookie(FormEditorSupport.class));
                Node[] nodes = editor.getActivatedNodes();
                if ((nodes == null) || (nodes.length == 0)) {
                    editor.setActivatedNodes(new Node[] {dataObject.getNodeDelegate()});
                }
                return (MultiViewElement) editor;
            }
            return MultiViewFactory.BLANK_ELEMENT;
        }
        
        @Override
        public String getDisplayName() {
            return FormUtils.getBundleString("CTL_SourceTabCaption"); // NOI18N
        }
        
        @Override
        public org.openide.util.HelpCtx getHelpCtx() {
            return org.openide.util.HelpCtx.DEFAULT_HELP;
        }
        
        @Override
        public java.awt.Image getIcon() {
            return ImageUtilities.loadImage(iconURL);
        }
        
        @Override
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ONLY_OPENED;
        }
        
        @Override
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
                     extends CloneableEditor
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
        
        @Override
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
        
        @Override
        public JComponent getVisualRepresentation() {
            return this;
        }
        
        @Override
        public void componentDeactivated() {
            super.componentDeactivated();
        }
        
        @Override
        public void componentActivated() {
            super.componentActivated();
        }
        
        @Override
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
        
        @Override
        public void requestVisible() {
            if (multiViewObserver != null)
                multiViewObserver.requestVisible();
            else
                super.requestVisible();
        }
        
        @Override
        public void requestActive() {
            if (multiViewObserver != null)
                multiViewObserver.requestActive();
            else
                super.requestActive();
        }
        
        @Override
        public void componentClosed() {
            // Issue 52286 & 55818
            super.canClose(null, true);
            super.componentClosed();
        }
        
        @Override
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
                        if (codeGen != null) {
                            codeGen.regenerateCode();
                        }
                    }
                }
            }
            
        }
        
        @Override
        public void componentHidden() {
            super.componentHidden();
        }
        
        @Override
        public void componentOpened() {
            super.componentOpened();
            DataObject dob = ((DataEditorSupport)cloneableEditorSupport()).getDataObject();
            if ((multiViewObserver != null) && !(dob instanceof FormDataObject)) {
                multiViewObserver.getTopComponent().close(); // Issue 67879
                EditorCookie ec = dob.getCookie(EditorCookie.class);
                ec.open();
            }
        }
        
        @Override
        public void updateName() {
            super.updateName();
            if (multiViewObserver != null) {
                FormDataObject formDataObject = (FormDataObject)((DataEditorSupport) cloneableEditorSupport ()).getDataObject();
                String[] titles = getMVTCDisplayName(formDataObject);
                setDisplayName(titles[0]);
                setHtmlDisplayName(titles[1]);
            }
        }
        
        @Override
        protected boolean closeLast() {
            return true;
        }
        
        @Override
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
        
        @Override
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            FormEditorSupport formEditor = getFormEditor();
            return formEditor != null ? formEditor.canClose() : true;
        }
    }
    
    // ------
    
    private static final class Environment extends DataEditorSupport.Env {
        
        private static final long serialVersionUID = -1;
        
        public Environment(DataObject obj) {
            super(obj);
        }
        
        @Override
        protected FileObject getFile() {
            return this.getDataObject().getPrimaryFile();
        }
        
        @Override
        protected FileLock takeLock() throws java.io.IOException {            
            return ((FormDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }
        
        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return this.getDataObject().getCookie(FormEditorSupport.class);
        }
        
    }
        
    private final SaveCookie saveCookie = new SaveCookie() {
        @Override
        public void save() throws java.io.IOException {
            if (formEditor == null) { // not saving form, only java
                doSave(false); // don't need to be in event dispatch thread (#102986)
            } else if (EventQueue.isDispatchThread()) {
                doSave(true);
            } else {
                try {
                    EventQueue.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            doSave(true);
                        }
                    });
                } catch (InterruptedException ex) {
                    Logger.getLogger(FormEditorSupport.class.getName()).log(Level.INFO, "", ex); // NOI18N
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(FormEditorSupport.class.getName()).log(Level.INFO, "", ex); // NOI18N
                }
            }
        }

        private void doSave(boolean bothJavaAndForm) {
            try {
                if (bothJavaAndForm) {
                    saveDocument();
                } else {
                    saveSourceOnly();
                }
            } catch (IOException ex) {
                Logger.getLogger(FormEditorSupport.class.getName()).log(Level.INFO, "", ex); // NOI18N
            }
        }
    };

    private final CookieSet cookies;

    public void addSaveCookie() {
        DataObject javaData = this.getDataObject();
        if (javaData.getCookie(SaveCookie.class) == null) {
            cookies.add(saveCookie);
            javaData.setModified(true);
        }
    }

    public void removeSaveCookie() {
        DataObject javaData = this.getDataObject();
        if (javaData.getCookie(SaveCookie.class) != null) {
            cookies.remove(saveCookie);
            javaData.setModified(false);
        }
    }
}
