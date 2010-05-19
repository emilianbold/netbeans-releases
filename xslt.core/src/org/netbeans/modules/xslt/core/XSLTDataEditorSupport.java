/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.xslt.core;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.text.AbstractDocument;
import javax.swing.text.StyledDocument;

import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.modules.xml.validation.core.Controller;
import org.netbeans.modules.xml.misc.Xml;
import org.netbeans.api.project.Project;
import org.netbeans.api.xml.cookies.CookieObserver;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.validation.ui.ShowCookie;
import org.netbeans.modules.xml.xam.AbstractModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
import org.netbeans.modules.xslt.core.multiview.source.XSLTSourceMultiViewElementDesc;
import org.netbeans.modules.xslt.core.multiview.XsltMultiViewSupport;
import org.netbeans.modules.xslt.core.context.MapperContextFactory;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.model.spi.XslModelFactory;
import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.text.Line;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.netbeans.modules.soa.ui.UndoRedoManagerProvider;
import org.openide.cookies.SaveCookie;
import org.openide.util.UserCancelException;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.xslt.core.context.MapperContext;

/**
 * @author Vitaly Bychkov
 */
public class XSLTDataEditorSupport extends DataEditorSupport implements
    OpenCookie, EditCookie, EditorCookie.Observable, ShowCookie, ValidateXMLCookie, UndoRedoManagerProvider {
    
    public XSLTDataEditorSupport(XSLTDataObject dObj) {
        super(dObj, new XSLTEnv(dObj));
        setMIMEType(XSLTDataLoader.MIME_TYPE);
    }

    public UndoRedo.Manager getUndoRedoManager() {
      return getUndoManager();
    }

    @Override
    public void saveDocument() throws IOException {
        super.saveDocument();
        syncModel();
        getDataObject().setModified(false);
    }

    public void syncModel() {
        try {
            XslModel model = getXslModel();
            if (model != null) {
                model.sync();
            }
        }
        catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }

    public QuietUndoManager getUndoManager() {
        return (QuietUndoManager) getUndoRedo();
    }

    public XslModel getXslModel() {
        XSLTDataObject dataObject = getEnv().getXsltDataObject();
        ModelSource modelSource = Utilities.getModelSource(dataObject.getPrimaryFile(), true);
        return getModelFactory().getModel(modelSource);
    }

    /**
     * Implements ShowCookie interface used to open editor for the object containing xam Component
     * 
     * Opens the editor for the file pointed by this resultItem.
     * @param resultItem Contains the error/warning source, message.
     */
    public void show(final ResultItem resultItem) {
        if (!(resultItem.getModel() instanceof AbstractModel)) {
            return;
        }
        
        final Component componentEntity = resultItem.getComponents();
        
        // Get the edit and line cookies.
        DataObject d = getDataObject();
        final LineCookie lc = (LineCookie) d.getCookie(LineCookie.class);
        final EditCookie ec = (EditCookie) d.getCookie(EditCookie.class);
        if (lc == null || ec == null) {
            return;
        }
        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Opens the editor or brings it into focus
                // and makes it the activated topcomponent.
                ec.edit();
                
                TopComponent tc = WindowManager.getDefault().getRegistry()
                    .getActivated();
                MultiViewHandler mvh = MultiViews.findMultiViewHandler(tc);
                
                if (mvh == null) {
                    return;
                }
                
                // If model is broken
                // OR if the resultItem.getComponents() is null which
                // means the resultItem was generated when the model was broken.
                // In the above cases switch to the source multiview.
                if(resultItem.getModel().getState().equals(State.NOT_WELL_FORMED) ||
                        resultItem.getComponents() == null) {
                    for(int index1=0 ; index1<mvh.getPerspectives().length ; index1++) {
                        if(mvh.getPerspectives()[index1].preferredID().equals(
                                XSLTSourceMultiViewElementDesc.PREFERED_ID))
                            mvh.requestActive(mvh.getPerspectives()[index1]);
                    }
                }

                // Set annotation or select element in the multiview.
                MultiViewPerspective mvp = mvh.getSelectedPerspective();
//TODO a                
//                if (mvp.preferredID().equals("xslt-designer")) {
//                    List<TopComponent> list = getAssociatedTopComponents();
//                    for (TopComponent topComponent : list) {
//                        // Make sure this is a multiview window, and not just
//                        // some
//                        // window that has our DataObject (e.g. Projects,Files).
//                        MultiViewHandler handler = MultiViews
//                                .findMultiViewHandler(topComponent);
//                        if (handler != null && topComponent != null) {
//                            SelectXsltElement selectElement =
//                                    (SelectXsltElement) topComponent.getLookup()
//                                    .lookup(SelectXsltElement.class);
//                            if (selectElement == null)
//                                return;
//                            selectElement.select(XSLTComponent);
//                        }
//                    }
//                } else if (mvp.preferredID().equals("xslt-mapper")) {
//                    List<TopComponent> list = getAssociatedTopComponents();
//                    for (TopComponent topComponent : list) {
//                        // Make sure this is a multiview window, and not just
//                        // some
//                        // window that has our DataObject (e.g. Projects,Files).
//                        MultiViewHandler handler = MultiViews
//                                .findMultiViewHandler(topComponent);
//                        if (handler != null && topComponent != null) {
//                            SelectXsltElement selectElement =
//                                    (SelectXsltElement) topComponent.getLookup()
//                                    .lookup(SelectXsltElement.class);
//                            if (selectElement == null)
//                                return;
//                            selectElement.select(XSLTComponent);
//                        }
//                    }
//                } else 
                if (mvp.preferredID().equals(
                        XSLTSourceMultiViewElementDesc.PREFERED_ID)) 
                {
                    Line line = Xml.getLine(resultItem);

                    if (line != null) {
                      line.show(Line.SHOW_GOTO);
                    }
                }
            }
        });
    }
    
    /**
     * Supports one use case for one xsl file
     * If xslt file is used more than one time than first use case will be returned
     *
     * @return MapperContext wrapped first founded use case
     */
    public MapperContext getMapperContext() {
        return getEnv().getMapperContext();
    }
    
    private List<TopComponent> getAssociatedTopComponents() {
        // Create a list of TopComponents associated with the
        // editor's schema data object, starting with the
        // active TopComponent. Add all open TopComponents in
        // any mode that are associated with the DataObject.
        // [Note that EDITOR_MODE does not contain editors in
        // split mode.]
        List<TopComponent> associatedTCs = new ArrayList<TopComponent>();
        DataObject targetDO = getDataObject();
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        if (activeTC != null
                && targetDO == (DataObject) activeTC.getLookup().lookup(
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
        return associatedTCs;
    }

    @Override
    protected CloneableEditorSupport.Pane createPane() {
        TopComponent multiview = XsltMultiViewSupport
                .createMultiView((XSLTDataObject) getDataObject());
        
        Mode editorMode = WindowManager.getDefault().findMode(EDITOR_MODE);
        if (editorMode != null) {
            editorMode.dockInto(multiview);
        }
        
        return (Pane) multiview;
    }
    
    @Override
    public void initializeCloneableEditor(CloneableEditor editor) {
        super.initializeCloneableEditor(editor);

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                updateTitles();
            }
        });
        getValidationController().attach();
    }

    @Override
    protected void notifyClosed() {
        QuietUndoManager undo = getUndoManager();
        StyledDocument doc = getDocument();
        synchronized (undo) {
            // May be null when closing the editor.
            if (doc != null) {
                doc.removeUndoableEditListener(undo);
                undo.endCompound();
                undo.setDocument(null);
            }
            XslModel model = getXslModel();

            if (model != null) {
                model.removeUndoableEditListener(undo);
            }
            // Must unset the model when no longer listening to it.
            undo.setModel(null);

        }
        super.notifyClosed();
        getUndoManager().discardAllEdits();
        prepareTask = null;
        getValidationController().detach();
    }
    
    public boolean validateXML(CookieObserver cookieObserver) {
      getValidationController().runValidation();
      return true;
    }

    private Controller getValidationController() {
      return (Controller) getEnv().getXsltDataObject().getLookup().lookup(Controller.class);
    }

    @Override
    protected boolean notifyModified() {
        boolean notify = super.notifyModified();
        if (!notify) {
            return false;
        }
        
        XSLTDataObject dObj = getEnv().getXsltDataObject();
        if (dObj.getCookie(SaveCookie.class) == null) {
            dObj.addSaveCookie(new SaveCookie() {
                public void save() throws java.io.IOException {
                    try {
                        saveDocument();
                    } catch(UserCancelException e) {
                        //just ignore
                    }
                }
            });
        }
        return true;
    }
    
    /*
     * This method is redefined for marking big TopCompenent as modified (
     * asterik (*) needs to be appended to name of bpel file ). Without this
     * overriding file will be marked as modified only when source multiview is
     * edited. Modification in design view will not lead to marking TopComponent
     * as modified. see bug description for #6421669. (non-Javadoc)
     * 
     * @see org.openide.text.CloneableEditorSupport#updateTitles()
     */
    @Override
    protected void updateTitles()
    {
        /* This method is invoked by DataEditorSupport.DataNodeListener
         * whenever the DataNode displayName property is changed. It is
         * also called when the CloneableEditorSupport is (un)modified.
         */ 

        // Let the superclass handle the CloneableEditor instances.
        super.updateTitles();
        // We need to get the title updated on the MultiViewTopComponent.
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                List<TopComponent> list = getAssociatedTopComponents();
                for (TopComponent topComponent : list) {
                    // Make sure this is a multiview window, and not just some
                    // window that has our DataObject (e.g. Projects, Files).
                    MultiViewHandler handler = MultiViews
                            .findMultiViewHandler(topComponent);
                    if (handler != null && topComponent != null) {
                        topComponent.setHtmlDisplayName(messageHtmlName());
                        String name = messageName();
                        topComponent.setDisplayName(name);
                        topComponent.setName(name);
                        topComponent.setToolTipText(messageToolTip());
                    }
                }
            }
        });
    }
    
    protected XSLTEnv getEnv() {
        return (XSLTEnv) env;
    }
    
    @Override
    protected UndoRedo.Manager createUndoRedoManager()
    {
        // Override so the superclass will use our proxy undo manager
        // instead of the default, then we can intercept edits.
        return new QuietUndoManager(super.createUndoRedoManager());
        // Note we cannot set the document on the undo manager right
        // now, as CES is probably trying to open the document.
    }

    /**
     * Environment that connects the dataobject and ClonneableEditorSupport
     */
    protected static class XSLTEnv extends DataEditorSupport.Env {

        private static final long serialVersionUID = 835762240381934851L;
        
        public XSLTEnv(XSLTDataObject dObj) {
            super(dObj);
        }
        
        public XSLTDataObject getXsltDataObject() {
            return (XSLTDataObject) getDataObject();
        }
        
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        protected FileLock takeLock() throws IOException {
            return ((MultiDataObject) getDataObject()).getPrimaryEntry()
                .takeLock();
        }

        public MapperContext getMapperContext() {
            FileObject xsltFo = getFile();
            Project project = SoaUtil.getProject(xsltFo);

            MapperContext context = null;
            
            context = MapperContextFactory.getInstance().
                                    createMapperContext(xsltFo, project);
            
// TODO m | r            
////            FileObject xsltFo = getFile();
////            Project project = Util.getProject(xsltFo);
////            FileObject tMapFo = Util.getTMapFo(project);
////            FileObject projectRoot = Util.getProjectRoot(xsltFo);
////            XslModel xslModel = Util.getXslModel(xsltFo);
////            FileObject projectSource = Util.getProjectSource(project);
////            
////            if (tMapFo == null || projectRoot == null) {
////                // TODO m 
////                try {
////                    if (tMapFo == null) {
////                        Xml.createNewXmlFo(
////                                projectSource.getPath(),
////                                "transformmap", 
////                                TMapComponent.TRANSFORM_MAP_NS_URI);
////                    }
////                    return new MapperContextImpl(xslModel, Util.getTMapModel(tMapFo));
////                } catch (IOException ex) {
////                    ErrorManager.getDefault().notify(ex);
////                    return null;
////                }
////            }
////            
////            TMapModel tMapModel = Util.getTMapModel(tMapFo);
////            if (tMapModel == null) {
////                // TODO m
////                return new MapperContextImpl(xslModel, Util.getTMapModel(tMapFo));
////            }
////            
////            MapperContext mapperContext = null;
////            TransformMap root = tMapModel.getTransformMap();
////            List<Service> services = root.getServices();
////            Operation operation = null;
////            if (services != null) {
////                for (Service service : services) {
////                    List<Operation> operations = service.getOperations();
////                    for (Operation elem : operations) {
////                        String file = elem.getFile();
////                        if (file != null && file.equals(xsltFo.getPath().substring(projectSource.getPath().length())) ) {
////                            operation = elem;
////                            break;
////                        } 
////                    }
////                    if (operation != null) {
////                        break;
////                    }
////                }
////            }
////            
////            if (operation == null) {
////                // TODO m
////                return new MapperContextImpl(xslModel, Util.getTMapModel(tMapFo));
////            }
////            
////
////            // TODO m
////            AXIComponent sourceComponent = null;//tDesc.getSourceAXIType(projectRoot);
////            AXIComponent targetComponent = null;//tDesc.getTargetAXIType(projectRoot);
////            // TODO m
////            mapperContext = new MapperContextImpl( operation, xslModel, sourceComponent, targetComponent);
            
            return context;
        }
    }

    @Override
    public Task prepareDocument()
    {
        Task task = super.prepareDocument();
        // Avoid listening to the same task more than once.
        if (task == prepareTask) {
            return task;
        }
        task.addTaskListener(new TaskListener() {

            public void taskFinished( Task task ) {
                /* The superclass prepareDocument() adds the undo/redo
                 * manager as a listener -- we need to remove it since
                 *  the views will add and remove it as needed.
                 */
                QuietUndoManager undo = (QuietUndoManager) getUndoRedo();
                StyledDocument doc = getDocument();
                synchronized (undo) {
                    // Now that the document is ready, pass it to the manager.
                    undo.setDocument((AbstractDocument) doc);
                    if (!undo.isCompound()) {
                        /* The superclass prepareDocument() adds the undo/redo
                         * manager as a listener -- we need to remove it since
                         * we will initially listen to the model instead.
                         */
                        doc.removeUndoableEditListener(undo);
                        // If not listening to document, then listen to model.
                        addUndoManagerToModel(undo);
                    }
                }
            }
        });
        return task;
    }

    @Override
    public Task reloadDocument()
    {
        Task task = super.reloadDocument();
        task.addTaskListener(new TaskListener() {

            public void taskFinished( Task task ) {
                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        QuietUndoManager undo = getUndoManager();
                        StyledDocument doc = getDocument();
                        /* The superclass reloadDocument() adds the undo
                         * manager as an undoable edit listener.
                         */
                        synchronized (undo) {
                            if (!undo.isCompound() && doc != null) {
                                doc.removeUndoableEditListener(undo);
                            }
                        }
                    }
                });
            }
        });
        return task;
    }

    /**
     * Adds the undo/redo manager to the document as an undoable edit listener,
     * so it receives the edits onto the queue. The manager will be removed from
     * the model as an undoable edit listener.
     * <p>
     * This method may be called repeatedly.
     * </p>
     */
    public void addUndoManagerToDocument() {
        /* 
         * This method may be called repeatedly.
         * Stop the undo manager from listening to the model, as it will
         * be listening to the document now.
         */
        QuietUndoManager undo = getUndoManager();
        StyledDocument doc = getDocument();
        synchronized (undo) {

            removeUndoManagerFromModel();

            /*
             *  Document may be null if the cloned views are not behaving
             *  correctly.
             */
            if (doc != null) {
                // Ensure the listener is not added twice.
                doc.removeUndoableEditListener(undo);
                doc.addUndoableEditListener(undo);
                /*
                 *  Start the compound mode of the undo manager, such that when
                 * we are hidden, we will treat all of the edits as a single
                 * compound edit. This avoids having the user invoke undo
                 * numerous times when in the model view.
                 */
                undo.beginCompound();
            }
        }
    }

    /**
     * Add the undo/redo manager undoable edit listener to the model.
     * <p>
     * Caller should synchronize on the undo manager prior to calling this
     * method, to avoid thread concurrency issues.
     * </p>
     * 
     * @param undo
     *            the undo manager.
     */
    public void addUndoManagerToModel( QuietUndoManager undo ) {
        XslModel model = getXslModel();
        if (model != null) {
            removeUndoManagerFromModel();
            model.addUndoableEditListener(undo);
            undo.setModel(model);

        }
    }

    /**
     * Removes the undo/redo manager undoable edit listener from the document,
     * to stop receiving undoable edits. The manager will be added to the model
     * as an undoable edit listener.
     * <p>
     * This method may be called repeatedly.
     * </p>
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
     * Removes the undo/redo manager undoable edit listener from the bpel model,
     * to stop receiving undoable edits.
     */
    private void removeUndoManagerFromModel() {
        XslModel model = getXslModel();
        if (model != null) {
            QuietUndoManager undo = getUndoManager();
            model.removeUndoableEditListener(undo);
            // Must unset the model when leaving model view.
            undo.setModel(null);
        }
    }

    /**
     * Remove the undo manager from both the model and document, such that
     * any changes made to either will not be added to the undo queue. The
     * caller should invoke <code>resumeUndoRedo()</code> once the changes
     * are completed.
     *
     * @return  a value that must be passed to <code>resumeUndoRedo()</code>.
     */
    public boolean suspendUndoRedo() {
        QuietUndoManager undo = getUndoManager();
        boolean compound;
        synchronized (undo) {
            compound = undo.isCompound();
            if (compound) {
                removeUndoManagerFromDocument();
            }
            removeUndoManagerFromModel();
        }
        return compound;
    }

    /**
     * Add the undo manager as an undoable edit listener to either the
     * Swing document or the XAM model, and set up the compound mode if
     * that was in place previously.
     * 
     * @param  value  value returned from <code>suspendUndoRedo()</code>
     */
    public void resumeUndoRedo(boolean value) {
        if (value) {
            addUndoManagerToDocument();
        } else {
            QuietUndoManager undo = getUndoManager();
            synchronized (undo) {
                addUndoManagerToModel(undo);
            }
        }
    }
    
    private XslModelFactory getModelFactory() {
        XslModelFactory factory = (XslModelFactory) Lookup.getDefault()
            .lookup(XslModelFactory.class);
        return factory;
    }

    /**
     * This method allows the close behavior of CloneableEditorSupport to be
     * invoked from the SourceMultiViewElement. The close method of
     * CloneableEditorSupport at least clears the undo queue and releases the
     * swing document.
     */
    public boolean silentClose() {
        return super.close(false);
    }
    
    /** 
     * Handles closing of the MultiView component globally. Each opened {@link org.netbeans.core.spi.multiview.MultiViewElement}
     * creates a {@link org.netbeans.core.spi.multiview.CloseOperationState} instance to notify the environment of it's internal state.
     *
     */
    public static class CloseHandler implements CloseOperationHandler, Serializable {
        private static final long serialVersionUID = -4621077799099893176L;
        
        private CloseHandler() {
            // CTOR for deser
        }
        
        public CloseHandler( XSLTDataObject obj ) {
            myDataObject = obj;
        }
        
        public boolean resolveCloseOperation( CloseOperationState[] elements ) {
            XSLTDataEditorSupport support = myDataObject == null ? null
                    : (XSLTDataEditorSupport) myDataObject
                    .getCookie(XSLTDataEditorSupport.class);
            if (support == null) {
                return true;
            }
            boolean close = support.canClose();
            if (close) {
                if (myDataObject.isValid()) {
//                  In odrer to clear the undo queue of orphaned edits, let's always
//                  reload the document, which discards the edits on the undo queue.
//                  The critical part is that BeforeSaveEdit gets added to the queue.
//                  // In case user discarded edits, need to reload.
//                  if (dataObject.isModified()) {
                        support.reloadDocument().waitFinished();
//                    }
                }
                myDataObject.setModified(false); // Issue 85629
            }
            return close;
        }
        
        private XSLTDataObject myDataObject;
    }

    private transient Task prepareTask;
}
