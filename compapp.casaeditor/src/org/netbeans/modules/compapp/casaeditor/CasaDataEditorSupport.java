/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor;

import java.awt.EventQueue;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.modules.compapp.casaeditor.multiview.CasaMultiViewFactory;
import org.openide.ErrorManager;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.text.DataEditorSupport;
import org.openide.util.Task;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.compapp.casaeditor.model.jbi.CasaModelFactory;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
import org.openide.awt.UndoRedo;
import org.openide.util.TaskListener;

/**
 *
 * @author tli
 */
public class CasaDataEditorSupport extends DataEditorSupport
implements 
        /* CasaModelCookie, */ 
        OpenCookie, 
        EditCookie,
        EditorCookie.Observable, 
        LineCookie, 
        CloseCookie, 
        PrintCookie
{
    
    /** Used for managing the prepareTask listener. */
    private transient Task prepareTask2;
    
    /** Needed for casa file deletion. */
    private static Map<DataObject, CasaWrapperModel> modelMap = 
            new HashMap<DataObject, CasaWrapperModel>();
    
    
    public CasaDataEditorSupport(CasaDataObject sobj) {
        super(sobj, new CasaEditorEnv(sobj));
        setMIMEType("text/xml");     // NOI18N
        //TODO: we need to use below one eventually
        //setMIMEType(WSDLDataLoader.MIME_TYPE);
    }
    
    
    public CasaEditorEnv getEnv() {
        return (CasaEditorEnv)env;
    }
    
    @Override
    protected Pane createPane() {
        TopComponent tc = CasaMultiViewFactory.createMultiView(
                (CasaDataObject) getDataObject());
        Mode editorMode = WindowManager.getDefault().findMode(
                CasaDataEditorSupport.EDITOR_MODE);
        if (editorMode != null) {
            editorMode.dockInto(tc);
        }
        return (Pane) tc;
    }
    
    public static boolean isLastView(TopComponent tc) {
        
        if (!(tc instanceof CloneableTopComponent))
            return false;
        
        boolean oneOrLess = true;
        Enumeration en =
                ((CloneableTopComponent)tc).getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements())
                oneOrLess = false;
        }
        
        return oneOrLess;
    }
    
    // Change method access to public
    @Override
    public void initializeCloneableEditor(CloneableEditor editor) {
        super.initializeCloneableEditor(editor);
        // Force the title to update so the * left over from when the
        // modified data object was discarded is removed from the title.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Have to do this later to avoid infinite loop.
                updateTitles();
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
                    if (targetDO == (DataObject) tcc.getLookup().lookup(
                            DataObject.class)) {
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
                        
                        // Make the Source View multiview window un-editable.
                        JEditorPane[] editorPanes = getOpenedPanes();
                        if (editorPanes != null) {
                            for (JEditorPane editorPane : editorPanes) {
                                editorPane.setEditable(false);
                            }
                        }

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
            try {
                CasaWrapperModel model = getModel();
                if (model != null) {
                    model.removeUndoableEditListener(undo);
                }
                // Must unset the model when no longer listening to it.
                undo.setModel(null);
            } catch (Exception ioe) {
                // Model is gone, but just removing the listener is not
                // going to matter anyway.
            }
        }
     
        super.notifyClosed();
    }
    
    public CasaWrapperModel getModel() {
        CasaDataObject dobj = getEnv().getCasaDataObject();
        return getModel(dobj);
    }
    
    private static CasaWrapperModel getModel(DataObject dobj) {
        CasaWrapperModel model = modelMap.get(dobj);
        if (model == null) {
            ModelSource modelSource = Utilities.getModelSource(dobj.getPrimaryFile(), true);
            if (modelSource != null) {
                model = (CasaWrapperModel) CasaModelFactory.getInstance().getModel(modelSource);
                modelMap.put(dobj, model);
            }
        }
        return model;
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
            try {
                CasaWrapperModel model = getModel();
                if (model != null) {
                    model.removeUndoableEditListener(undo);
                }
                // Must unset the model when no longer listening to it.
                undo.setModel(null);
            } catch (Exception ioe) {
                // Model is gone, but just removing the listener is not
                // going to matter anyway.
            }
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
        try {
            CasaWrapperModel model = getModel();
            if (model != null) {
                // Ensure the listener is not added twice.
                model.removeUndoableEditListener(undo);
                model.addUndoableEditListener(undo);
                // Ensure the model is sync'd when undo/redo is invoked,
                // otherwise the edits are added to the queue and eventually
                // cause exceptions.
                undo.setModel(model);
            }
        } catch (Exception ioe) {
            // Model is gone, nothing will work, return immediately.
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
    
    /** {@inheritDoc} */
    public void saveDocument() throws IOException {
        super.saveDocument();
        
        getModel().saveRelatedDataObjects();

        // 01/24/07, perform pending add/del SE project actions...
        getModel().addPendingProjects();
        getModel().deletePendingProjects();
        
        syncModel();
        getDataObject().setModified(false);
    }
    
    /**
     * Have the Casa model sync with the document.
     */
    public void syncModel() {
        // Only sync the document if the change relates to loss of focus,
        // which indicates that we are switching from the source view.
        // Update the tree with the modified text.
        try {
            if (getModel() != null) {
                 getModel().sync();
            }
        } catch (Throwable ioe) {
// initially, for debug purposes, just print out the exception
ioe.printStackTrace();
ErrorManager.getDefault().notify(ioe);

//            NotifyDescriptor nd = new NotifyDescriptor.Message(
//                    NbBundle.getMessage(
//                    CasaDataEditorSupport.class, 
//                    "MSG_NotWellformedXml"), 
//                    NotifyDescriptor.ERROR_MESSAGE);
//            DialogDisplayer.getDefault().notify(nd);
        }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Env class extends DataEditorSupport.Env.
     */
    protected static class CasaEditorEnv extends DataEditorSupport.Env {
        
        static final long serialVersionUID = -4541024350167923282L;
        
        public CasaEditorEnv(CasaDataObject obj) {
            super(obj);
        }
        
        public CloneableEditorSupport findTextEditorSupport() {
            return getCasaDataObject().getEditorSupport();
        }
        
        public CasaDataObject getCasaDataObject(){
            return (CasaDataObject) getDataObject();
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
    

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Implementation of CloseOperationHandler for multiview. Ensures both
     * column view and xml editor are correctly closed, data saved, etc. Holds
     * a reference to DataObject only - to be serializable with the
     * multiview TopComponent without problems.
     */
    public static class CloseHandler implements CloseOperationHandler, Serializable {
        static final long serialVersionUID = -3085696462320564244L;
        private DataObject myDataObject;

        private CloseHandler() {
            super();
        }

        public CloseHandler(DataObject dobj) {
            myDataObject = dobj;
        }

        private CasaDataEditorSupport getCasaDataEditorSupport() {
            return myDataObject instanceof CasaDataObject ?
                ((CasaDataObject) myDataObject).getEditorSupport() : null;
        }
        
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            CasaDataEditorSupport editor = getCasaDataEditorSupport();
	    boolean canClose = editor != null ? editor.canClose() : true;
	    // during the shutdown sequence this is called twice. The first time
	    // through the multi-view infrastructure. The second time is done through
	    // the TopComponent close. If the file is dirty and the user chooses
	    // to discard changes, the second time will also ask whether the
	    // to save or discard changes. 
	    if (canClose) {
		myDataObject.setModified(false);
                
                // discard changes to related models
                CasaWrapperModel model = getModel(myDataObject);
                if (model != null) {
                    // when deleting casa file, the model will be null at
                    // the second time. The related data objects should have
                    // been discarded at the first time.
                    model.discardRelatedDataObjects();
                }
                
                // invalidate the in-memory copy of this data object 
                try {
                    myDataObject.setValid(false);
                } catch (PropertyVetoException ex) {
                    ex.printStackTrace();
                }
                
                modelMap.remove(myDataObject);
	    }
	    return canClose;
        }
    }
}
