/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.dataloader;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.StyledDocument;

import org.netbeans.api.xml.cookies.CookieObserver;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.modules.xml.validation.core.Controller;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WLMModelFactory;
import org.netbeans.modules.worklist.editor.designview.DesignerMultiViewDescription;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.validation.ui.ShowCookie;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 *
 * @author Jeri Lockhart
 * @author Todd Fast, todd.fast@sun.com
 */
public class WorklistEditorSupport extends DataEditorSupport
        implements WorklistModelCookie, OpenCookie, EditCookie,
        EditorCookie.Observable, LineCookie, ValidateXMLCookie, CloseCookie,
        ShowCookie, ValidationControllerCookie
{
    
    /** Used for managing the prepareTask listener. */
    private transient Task prepareTask2;
    private Controller validationController;
    private final Object sync = new Object();


    public WorklistEditorSupport(WorklistDataObject sobj) {
        super(sobj, new WorklistEditorEnv(sobj));
        setMIMEType(WorklistDataLoader.MIME_TYPE);
        //TODO: we need to use below one eventually
        //setMIMEType(WorklistDataLoader.REQUIRED_MIME);
    }
    
    public WorklistEditorEnv getEnv() {
        return (WorklistEditorEnv) env;
    }

    @Override
    protected Pane createPane() {
        TopComponent tc = WorklistMultiViewFactory.createMultiView(
                (WorklistDataObject) getDataObject());
        // Note that initialization of the editor happens separately,
        // and we only need to handle that during the initial creation
        // of the text editor.
        Mode editorMode = WindowManager.getDefault().findMode(
                WorklistEditorSupport.EDITOR_MODE);
        if (editorMode != null) {
            editorMode.dockInto(tc);
        }
        return (Pane) tc;
    }

    @Override
    public void open() {
        if (getOpenedPanes() != null &&
                getOpenedPanes().length > 0) {
            //if already open, open current view.
            super.open();
            return;
        }
        // Do not use  ViewComponentCookie here, because of recursion.
        WLMModel model = getModel();
        TTask task = null;
        if (model.getState() == Model.State.VALID) {
            task = model.getTask();
        }
        String preferredID = DesignerMultiViewDescription.PREFERRED_ID;
        if (task == null) {
            preferredID = WorklistSourceMultiViewDescription.PREFERRED_ID;
        }
        super.open();
        WorklistMultiViewFactory.requestMultiviewActive(preferredID);
    }
    
    public static boolean isLastView(TopComponent tc) {
        if (!(tc instanceof CloneableTopComponent)) {
            return false;
        }
        
        boolean oneOrLess = true;
        
        Enumeration en = ((CloneableTopComponent) tc).getReference()
                .getComponents();
        
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements()) {
                oneOrLess = false;
            }
        }
        
        return oneOrLess;
    }
    
    // Change method access to public
    @Override
    public void initializeCloneableEditor(CloneableEditor editor) {
        super.initializeCloneableEditor(editor);
        // Force the title to update so the * left over from when the
        // modified data object was discarded is removed from the title.
        getValidationController().attach();
        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Have to do this later to avoid infinite loop.
                updateTitles();
                getValidationController().triggerValidation();
            }
        });
    }    

    @Override
    protected void updateTitles() {
        // This method is invoked by DataEditorSupport.DataNodeListener
        // whenever the DataNode displayName property is changed. It is
        // also called when the CloneableEditorSupport is (un)modified.

        // Let the superclass handle the CloneableEditor instances.
        super.updateTitles();

        // We need to get the title updated on the MultiViewTopComponent.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Create a list of TopComponents associated with the
                // editor's data object, starting with the active
                // TopComponent. Add all open TopComponents in any
                // mode that are associated with the DataObject.
                // [Note that EDITOR_MODE does not contain editors in
                // split mode.]
                List<TopComponent> associatedTCs = new ArrayList<TopComponent>();
                DataObject targetDO = getDataObject();
                TopComponent activeTC = TopComponent.getRegistry().getActivated();
                if (activeTC != null && targetDO == (DataObject) activeTC.getLookup().lookup(
                        DataObject.class)) {
                    associatedTCs.add(activeTC);
                }
                Set openTCs = TopComponent.getRegistry().getOpened();
                for (Object tc : openTCs) {
                    TopComponent tcc = (TopComponent) tc;
                    if (targetDO == (DataObject) tcc.getLookup()
                            .lookup(DataObject.class)) 
                    {
                        associatedTCs.add(tcc);
                    }
                }
                for (TopComponent tc : associatedTCs) {
                    // Make sure this is a multiview window, and not just some
                    // window that has our DataObject (e.g. Projects, Files).
                    MultiViewHandler mvh = MultiViews.findMultiViewHandler(tc);
                    if (mvh != null) {
                        tc.setHtmlDisplayName(messageHtmlName());
                        String name = messageName();
                        tc.setDisplayName(name);
                        tc.setName(name);
                        tc.setToolTipText(messageToolTip());
                    }
                }
            }
        });
    }    
    
    @Override
    protected UndoRedo.Manager createUndoRedoManager() {
        // Override so the superclass will use our proxy undo manager
        // instead of the default, then we can intercept edits.
        return new QuietUndoManager(super.createUndoRedoManager());
        // Note we cannot set the document on the undo manager right
        // now, as CES is probably trying to open the document.
    }    
    
   /**
     * Returns the UndoRedo.Manager instance managed by this editor support.
     *
     * @return UndoRedo.Manager instance.
     */
    public QuietUndoManager getUndoManager() {
        return (QuietUndoManager) getUndoRedo();
    }    
    
    @Override
    public Task prepareDocument() {
        Task task = super.prepareDocument();
        // Avoid listening to the same task more than once.
        if (task != prepareTask2) {
            prepareTask2 = task;
            task.addTaskListener(new TaskListener() {
                public void taskFinished(Task task) {
                    QuietUndoManager undo = getUndoManager();
                    StyledDocument doc = getDocument();
                    synchronized (undo) {
                        // Now that the document is ready, pass it to the manager.
                        undo.setDocument((AbstractDocument) doc);
                        if (!undo.isCompound()) {
                            // The superclass prepareDocument() adds the undo/redo
                            // manager as a listener -- we need to remove it since
                            // we will initially listen to the model instead.
                            doc.removeUndoableEditListener(undo);
                            // If not listening to document, then listen to model.
                            addUndoManagerToModel(undo);
                        }
                    }
                    prepareTask2 = null;
                }
            });
        }
        return task;
    }
    
    @Override
    public Task reloadDocument() {
        Task task = super.reloadDocument();
        task.addTaskListener(new TaskListener() {
            public void taskFinished(Task task) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        QuietUndoManager undo = getUndoManager();
                        StyledDocument doc = getDocument();
                        // The superclass reloadDocument() adds the undo
                        // manager as an undoable edit listener.
                        synchronized (undo) {
                            if (!undo.isCompound()) {
                                doc.removeUndoableEditListener(undo);
                            }
                        }
                    }
                });
            }
        });
        return task;
    }

    @Override
    protected void notifyClosed() {
        // Stop listening to the undoable edit sources when we are closed.
        QuietUndoManager undo = getUndoManager();
        StyledDocument doc = getDocument();
        synchronized (undo) {
            // May be null when closing the editor.
            if (doc != null) {
                doc.removeUndoableEditListener(undo);
                undo.endCompound();
                undo.setDocument(null);
            }
            WLMModel model = getModel();
            if (model != null) {
                model.removeUndoableEditListener(undo);
            }
            // Must unset the model when no longer listening to it.
            undo.setModel(null);
        }
        super.notifyClosed();
        getUndoManager().discardAllEdits();
        prepareTask2 = null;
        getValidationController().detach();
    }

    public WLMModel getModel() {
        WorklistDataObject dobj = getEnv().getWorklistDataObject();
        ModelSource modelSource = Utilities.getModelSource(dobj.getPrimaryFile(), true);
        if(modelSource != null) {
            return WLMModelFactory.getDefault().getModel(modelSource);
        }

        return null;
    }
    
    /**
     * Adds the undo/redo manager to the document as an undoable edit
     * listener, so it receives the edits onto the queue. The manager
     * will be removed from the model as an undoable edit listener.
     *
     * <p>This method may be called repeatedly.</p>
     */
    public void addUndoManagerToDocument() {
        // This method may be called repeatedly.
        // Stop the undo manager from listening to the model, as it will
        // be listening to the document now.
        QuietUndoManager undo = getUndoManager();
        StyledDocument doc = getDocument();
        
        synchronized (undo) {
            WLMModel model = getModel();
            if (model != null) {
                model.removeUndoableEditListener(undo);
            }
            // Must unset the model when no longer listening to it.
            undo.setModel(null);

            // Document may be null if the cloned views are not behaving correctly.
            if (doc != null) {
                // Ensure the listener is not added twice.
                doc.removeUndoableEditListener(undo);
                doc.addUndoableEditListener(undo);
                // Start the compound mode of the undo manager, such that when
                // we are hidden, we will treat all of the edits as a single
                // compound edit. This avoids having the user invoke undo
                // numerous times when in the model view.
                undo.beginCompound();
            }
        }
    }

    /**
     * Removes the undo/redo manager undoable edit listener from the
     * document, to stop receiving undoable edits. The manager will
     * be added to the model as an undoable edit listener.
     *
     * <p>This method may be called repeatedly.</p>
     */
    public void removeUndoManagerFromDocument() {
        // This method may be called repeatedly.
        QuietUndoManager undo = getUndoManager();
        StyledDocument doc = getDocument();
        
        synchronized (undo) {
            // May be null when closing the editor.
            if (doc != null) {
                doc.removeUndoableEditListener(undo);
                undo.endCompound();
            }
            // Have the undo manager listen to the model when it is not
            // listening to the document.
            addUndoManagerToModel(undo);
        }
    }

    /**
     * Add the undo/redo manager undoable edit listener to the model.
     *
     * <p>Caller should synchronize on the undo manager prior to calling
     * this method, to avoid thread concurrency issues.</p>
     *
     * @param  undo  the undo manager.
     */
    private void addUndoManagerToModel(QuietUndoManager undo) {
        // This method may be called repeatedly.
        WLMModel model = getModel();
        if (model != null) {
            // Ensure the listener is not added twice.
            model.removeUndoableEditListener(undo);
            model.addUndoableEditListener(undo);
            // Ensure the model is sync'd when undo/redo is invoked,
            // otherwise the edits are added to the queue and eventually
            // cause exceptions.
            undo.setModel(model);
        }
    }

    /**
     * This method allows the close behavior of CloneableEditorSupport to be 
     * invoked from the SourceMultiViewElement. The close method of 
     * CloneableEditorSupport at least clears the undo queue and releases
     * the swing document. 
     */ 
    public boolean silentClose() {
    return super.close(false);
    }
    
    /**
     * Have the WLM model sync with the document.
     */
    public void syncModel() {
        // Only sync the document if the change relates to loss of focus,
        // which indicates that we are switching from the source view.
        // Update the tree with the modified text.
        try {
            if(getModel() != null) {
                getModel().sync();
            }
        } catch (Throwable ioe) {
            // The document cannot be parsed
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle
                    .getMessage(WorklistEditorSupport.class, 
                    "MSG_NotWellformedWLM"), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    public boolean validateXML(CookieObserver cookieObserver) {
        getValidationController().runValidation();
        return true;
//        List<ResultItem> resultItems = new ValidationOutputWindowController()
//                .validate(getModel(), true);
//
//        StyledDocument document = getModel().getModelSource().getLookup()
//                .lookup(StyledDocument.class);
//
//        if (resultItems != null) {
//            Line.Set lineSet = getLineSet();
//
//            for (ResultItem item : resultItems) {
//                ValidationAnnotation annotation = new ValidationAnnotation();
//
//                Component component = item.getComponents();
//
//                if (component != null) {
//                    DocumentComponent documentComponent = (DocumentComponent) component;
//                    int position = documentComponent.findPosition();
//
//                    int lineNumber = NbDocument.findLineNumber(document, position);
//
//                    Line line = lineSet.getLines().get(lineNumber);
//
//                    annotation.show(line, item.getDescription());
//                }
//            }
//        }
//
//        return true;
    }

    public void show(ResultItem resultItem) {
        if (resultItem == null) {
            return;
        }

        WorklistDataObject dataObject = (WorklistDataObject) getDataObject();
        Model model = resultItem.getModel();
        Component component = (WLMComponent) resultItem.getComponents();

        WLMModel wlmModel = null;
        WLMComponent wlmComponent = null;

        if (!(model instanceof WLMModel)) {
            return;
        }

        wlmModel = (WLMModel) model;

        if (component == null || (component instanceof WLMComponent)) {
            wlmComponent = (WLMComponent) component;
        }

        SwingUtilities.invokeLater(new GoToRunnable(dataObject, wlmModel,
                wlmComponent));
    }

    public Controller getValidationController() {
        synchronized (sync) {
            if (validationController == null) {
                validationController = new Controller(getModel());
            }
            return validationController;
        }
    }

    //////////////////////////////////
    //////////////////////////////////
    //////////////////////////////////
    
    
    /**
     * Env class extends DataEditorSupport.Env.
     */
    protected static class WorklistEditorEnv extends DataEditorSupport.Env {
        
        static final long serialVersionUID =1099957785497677206L;
        
        public WorklistEditorEnv(WorklistDataObject obj) {
            super(obj);
        }
        
        public CloneableEditorSupport findTextEditorSupport() {
            return getWorklistDataObject().getWlmEditorSupport();
        }
        
        public WorklistDataObject getWorklistDataObject(){
            return (WorklistDataObject) getDataObject();
        }
        
        @Override
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }
        
        @Override
        protected FileLock takeLock() throws IOException {
            return getDataObject().getPrimaryFile().lock();
        }
    }
    
    /**
     * Implementation of CloseOperationHandler for multiview. Ensures both
     * column view and xml editor are correctly closed, data saved, etc. Holds
     * a reference to DataObject only - to be serializable with the
     * multiview TopComponent without problems.
     */
    public static class CloseHandler implements CloseOperationHandler, Serializable {
        private static final long serialVersionUID =-3838395157610633251L;
        private DataObject dataObject;

        private CloseHandler() {
            super();
        }

        public CloseHandler(DataObject dobj) {
            dataObject = dobj;
        }

        private WorklistEditorSupport getWLMEditorSupport() {
            return (dataObject instanceof WorklistDataObject) 
                    ? ((WorklistDataObject) dataObject).getWlmEditorSupport() 
                    : null;
        }

        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            WorklistEditorSupport wlmEditor = getWLMEditorSupport();
        boolean canClose = (wlmEditor != null) 
                    ? wlmEditor.canClose() : true;
        // during the shutdown sequence this is called twice. The first time
        // through the multi-view infrastructure. The second time is done through
        // the TopComponent close. If the file is dirty and the user chooses
        // to discard changes, the second time will also ask whether the
        // to save or discard changes. 
        if (canClose) {
        dataObject.setModified(false);
        }
        return canClose;
        }
    }
}
