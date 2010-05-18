/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */
package org.netbeans.modules.etl.ui;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.xml.cookies.CookieObserver;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.modules.etl.ui.model.impl.ETLCollaborationModel;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.awt.UndoRedo.Manager;
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
import org.openide.text.DataEditorSupport;
import org.openide.util.Task;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import com.sun.etl.exception.BaseException;

import java.util.List;
import javax.swing.text.StyledDocument;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.modules.sql.framework.model.ValidationInfo;
import org.openide.awt.StatusDisplayer;

import org.openide.text.Line;
import org.netbeans.modules.soa.ui.UndoRedoManagerProvider;
import org.netbeans.modules.xml.validation.ui.ShowCookie;
import org.netbeans.modules.xml.validation.ui.Output;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;

/**
 *
 * @author Ahimanikya Satapathy
 */
public class ETLEditorSupport extends DataEditorSupport implements OpenCookie, ShowCookie, EditCookie, EditorCookie.Observable, LineCookie,
        CloseCookie, ValidateXMLCookie, PrintCookie, UndoRedoManagerProvider {

    private static ETLDataObject obj;
    private boolean updatedDuringLoad = false;

    public ETLEditorSupport(ETLDataObject sobj) {
        super(sobj, new ETLEditorEnv(sobj));
        obj = sobj;
        setMIMEType(ETLDataLoader.MIME_TYPE);
        PRJ_PATH = sobj.getFolder().getPrimaryFile().getParent().getPath();
        PRJ_PATH = PRJ_PATH.replace('/', '\\');
        PRJ_NAME = sobj.getFolder().getPrimaryFile().getParent().getName();
    //Project prj = FileOwnerQuery.getOwner(obj.getPrimaryFile());
    //java.util.logging.Logger.getLogger(ETLEditorSupport.class.getName()).info("ETLEditorSupport project " + prj);
    }

    /*public static void getPath() {
    Utilities.actionsGlobalContext().lookup(Project.class);
    Utilities.actionsGlobalContext().lookup(DataObject.class);
    Project prj = FileOwnerQuery.getOwner(obj.getPrimaryFile());
    java.util.logging.Logger.getLogger(ETLEditorSupport.class.getName()).info("");
    }*/
    public ETLEditorEnv getEnv() {
        return (ETLEditorEnv) env;
    }

    @Override
    protected Pane createPane() {
        multiviewTC = ETLMultiViewFactory.createMultiView((ETLDataObject) getDataObject());
        multiviewTC.setName(getDataObject().getPrimaryFile().getNameExt());

        Mode editorMode = WindowManager.getDefault().findMode(ETLEditorSupport.EDITOR_MODE);
        if (editorMode != null) {
            editorMode.dockInto(multiviewTC);
        }
        return (Pane) multiviewTC;
    }

    /**
     * This is called by the multiview elements whenever they are created
     * (and given a observer knowing their multiview TopComponent). It is
     * important during deserialization and clonig the multiview - i.e. during
     * the operations we have no control over. But anytime a multiview is
     * created, this method gets called.
     *
     * @param  topComp  TopComponent to which we are associated.
     */
    public void setTopComponent(TopComponent mvtc) {
        this.multiviewTC = mvtc;

        // Force the title to update so the * left over from when the
        // modified data object was discarded is removed from the title.
        // It is okay for this to be invoked multiple times.
        if (!getEnv().getETLDataObject().isModified()) {
            // Update later to avoid a loop.
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    updateTitles();
                }
            });
        }
    }

    public static boolean isLastView(TopComponent tc) {

        if (!(tc instanceof CloneableTopComponent)) {
            return false;
        }
        boolean oneOrLess = true;
        Enumeration en = ((CloneableTopComponent) tc).getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements()) {
                oneOrLess = false;
            }
        }

        return oneOrLess;
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
                if (multiviewTC != null) {
                    multiviewTC.setHtmlDisplayName(messageHtmlName());
                    String name = messageName();
                    multiviewTC.setDisplayName(name);
                    multiviewTC.setName(name);
                    multiviewTC.setToolTipText(messageToolTip());
                }
            }
        });
    }

    /**
     * Returns the UndoRedo.Manager instance managed by this editor support.
     *
     * @return UndoRedo.Manager instance.
     */
    /*public UndoRedo.Manager getUndoManager() {
    return super.getUndoRedo();
    }*/
    public QuietUndoManager getUndoManager() {
        return (QuietUndoManager) getUndoRedo();
    }

    @Override
    protected Task reloadDocument() {
        Task task = super.reloadDocument();
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                // Remove the undo listener only if columns view is showing.
                if (multiviewTC != null) {
                    MultiViewHandler mvh = MultiViews.findMultiViewHandler(multiviewTC);
                    if (mvh != null) {
                        MultiViewPerspective mvp = mvh.getSelectedPerspective();
                        if (mvp != null) {
                            if (mvp.preferredID().contains(ETLEditorViewMultiViewDesc.PREFERRED_ID)) {
                                Document doc = getDocument();
                                UndoRedo.Manager urm = getUndoRedo();
                                if (doc != null && urm != null) {
                                    doc.removeUndoableEditListener(urm);
                                }
                            }
                        }
                    }
                }
            }
        });
        return task;
    }

    /**
     * Have the schema model sync with the document.
     */
    public void syncModel() {
        // Only sync the document if the change relates to loss of focus,
        // which indicates that we are switching from the source view.
        // Update the tree with the modified text.
        try {
            ETLDataObject etlDataObject = (ETLDataObject) getDataObject();
            ETLCollaborationModel collabModel = etlDataObject.getModel();
            collabModel.sync();
            collabModel.getUndoManager().discardAllEdits();

            openDocument();
            String defnContent = getDocument().getText(0, getDocument().getLength());
            //java.util.logging.Logger.getLogger(ETLEditorSupport.class.getName()).info("***************** defnContent in syncModel *************** " + defnContent);
            collabModel.reLoad(defnContent);
            // is below required? 
            collabModel.setReloaded(true);
            populateCanvas(etlDataObject, collabModel);
            isFirstTime = false;

            // Validate the collabModel and update badge
            updateBadge(etlDataObject);
            if (updatedDuringLoad) {
                updatedDuringLoad = false;
                synchDocument();
                super.saveDocument();
            }
            getDataObject().setModified(false);
        } catch (Throwable ioe) {
            // The document cannot be parsed             
            ErrorManager.getDefault().notify(ioe);
            StatusDisplayer.getDefault().setStatusText("syncModel: " + ioe.getMessage());
        }
    }

    /**
     * Populates eTL editor canvas using information from given ETLCollaborationModel.
     *
     * @param collabModel
     * @param disableMods
     * @throws BaseException
     */
    private void populateCanvas(final ETLDataObject etlDataObject, final ETLCollaborationModel collabModel) throws BaseException {
        Runnable run = new Runnable() {

            public void run() {
                try {
                    etlDataObject.getETLEditorTopPanel().getGraphView().clearAll();
                    collabModel.restoreUIState();
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        };

        SwingUtilities.invokeLater(run);
    }

    public void synchDocument() {
        //syn source view
        //TODO: rit we should create etl defininition model based on swing document
        // and there add document listeber to keeep model in synch with text editing.
        //all update model incrementally
        try {
            ETLDataObject etlDataObject = (ETLDataObject) getDataObject();
            String content = etlDataObject.getETLDefinition().toXMLString("");
            Document doc = getDocument();
            //String testing = doc.getText(0, getDocument().getLength());
            //java.util.logging.Logger.getLogger(ETLEditorSupport.class.getName()).info("***************** testing *************** " + testing);
            //java.util.logging.Logger.getLogger(ETLEditorSupport.class.getName()).info("********************* content in synchDocument *********** " + content);
            if (doc != null) {
                doc.remove(0, getDocument().getLength());
                doc.insertString(0, content, null);
            }
            etlDataObject.getModel().setDirty(true);
            etlDataObject.setModified(true);
            updateBadge(etlDataObject);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    private void updateBadge(ETLDataObject etlDataObject) {
        try {
            List<ValidationInfo> list = etlDataObject.getETLDefinition().badgeValidate();
            int errCount = 0;
            for (ValidationInfo vInfo : list) {
                if (vInfo.getValidationType() == ValidationInfo.VALIDATION_ERROR) {
                    errCount++;
                }
            }
            if (etlDataObject.isValid()) {
                ETLNode node = (ETLNode) etlDataObject.getNodeDelegate();

                if (errCount > 0) {
                    node.setCollabState(ETLNode.ERROR);
                } else if (!list.isEmpty()) {
                    node.setCollabState(ETLNode.WARNING);
                } else {
                    node.setCollabState(ETLNode.VALID);
                }
                multiviewTC.setIcon(node.getIcon(0));
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    /**
     * Env class extends SchemaEditorSupport.Env.
     * overrides findSchemaEditorSupport
     *
     */
    protected static class ETLEditorEnv extends DataEditorSupport.Env {

        static final long serialVersionUID = 1099957785497677206L;

        public ETLEditorEnv(ETLDataObject obj) {
            super(obj);
        }

        public CloneableEditorSupport findTextEditorSupport() {
            return getETLDataObject().getETLEditorSupport();
        }

        public ETLDataObject getETLDataObject() {
            return (ETLDataObject) getDataObject();
        }

        @Override
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        @Override
        protected FileLock takeLock() throws IOException {
            return null;//getDataObject().getPrimaryFile().lock();

        }
    }

    public boolean silentClose() {
        return super.close(false);
    }

    /**
     * Implementation of CloseOperationHandler for multiview. Ensures both
     * column view and xml editor are correctly closed, data saved, etc. Holds
     * a reference to Schema DataObject only - to be serializable with the
     * multiview TopComponent without problems.
     */
    public static class CloseHandler implements CloseOperationHandler, Serializable {

        private CloseHandler() {
            super();
        }

        public CloseHandler(ETLDataObject schemaDO) {
            dataObject = schemaDO;
        }

        private ETLEditorSupport getETLEditorSupport() {
            return dataObject != null && dataObject instanceof ETLDataObject ? ((ETLDataObject) dataObject).getETLEditorSupport() : null;
        }

        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            ETLEditorSupport etlEditor = dataObject == null ? null
                    : (ETLEditorSupport) dataObject.getCookie(ETLEditorSupport.class);
            if (etlEditor == null) {
                return true;
            }
            //  if (etlEditor != null) {
            // This handles saving the document.
            boolean close = etlEditor.canClose();
            if (close) {
                if (dataObject.isValid()) {
                    // In case user discarded edits, need to reload.
                    if (dataObject.isModified()) {
                        // In case user discarded edits, need to reload.
                        etlEditor.reloadDocument().waitFinished();
                    }

                    etlEditor.syncModel();
                    // Need to properly close the support, too.
                    etlEditor.notifyClosed();
                }
                dataObject.setModified(false);
            }
            return close;
        // }
        //  return true;
        }
        private static final long serialVersionUID = -3838395157610633251L;
        private ETLDataObject dataObject;
    }

    /*public static String getPath() {
    String path = null;
    if (obj != null) {
    path = obj.getPath();
    PRJ_PATH = path;
    }
    return path;
    }*/
    private TopComponent multiviewTC;
    public boolean isFirstTime = true;
    public static String PRJ_PATH = "";
    public static String PRJ_NAME = "";

    public boolean validateXML(CookieObserver observer) {
        List validationResults;

        Output validationController =
                new Output();
        validationResults = validationController.validate((Model) ((ETLDataObject) this.getDataObject()).getLookup().lookup(Model.class));

        /* Send the complete/slow validation results to the validation
         * controller
         * so that clients can be notified.
         */
        /* BPELValidationController controller =
        (BPELValidationController) ((ETLDataObject) getDataObject()).getLookup().lookup(BPELValidationController.class);
        if (controller != null) {
        controller.notifyCompleteValidationResults(validationResults);
        }*/

        return true;
    }

    public void show(final ResultItem resultItem) {
        DataObject d = getDataObject();
        final LineCookie lc = (LineCookie) d.getCookie(LineCookie.class);
        final EditCookie ec = (EditCookie) d.getCookie(EditCookie.class);
        if (lc == null || ec == null) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                // Opens the editor or brings it into focus
                // and makes it the activated topcomponent.
                ec.edit();

                TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
                MultiViewHandler mvh = MultiViews.findMultiViewHandler(tc);

                if (mvh == null) {
                    return;
                }

                /* If model is broken
                 * OR if the resultItem.getComponents() is null which
                 * means the resultItem was generated when the model was broken.
                 *  In the above cases switch to the source multiview.
                 */
                if (resultItem.getModel().getState().equals(
                        State.NOT_WELL_FORMED) || resultItem.getComponents() == null) {
                    for (int index1 = 0; index1 < mvh.getPerspectives().length; index1++) {
                        if (mvh.getPerspectives()[index1].preferredID().equals(
                                ETLEditorViewMultiViewDesc.PREFERRED_ID)) {
                            mvh.requestActive(mvh.getPerspectives()[index1]);
                        }
                    }
                }

                // Set annotation or select element in the multiview.
                MultiViewPerspective mvp = mvh.getSelectedPerspective();
                /*if (mvp.preferredID().equals("orch-designer")) {
                List<TopComponent> list = getAssociatedTopComponents();
                for (TopComponent topComponent : list) {
                // Make sure this is a multiview window, and not just
                // some
                // window that has our DataObject (e.g. Projects,Files).
                MultiViewHandler handler = MultiViews
                .findMultiViewHandler(topComponent);
                if (handler != null && topComponent != null) {
                SelectBpelElement selectElement = 
                (SelectBpelElement) topComponent
                .getLookup()
                .lookup(SelectBpelElement.class);
                if (selectElement == null)
                return;
                selectElement.select(bpelEntity);
                }
                }
                }
                else */
                if (mvp.preferredID().equals(
                        ETLSourceMultiviewDesc.PREFERRED_ID)) {
                    Line line = ValidationUtil.getLine(resultItem);

                    if (line != null) {
                        line.show(Line.SHOW_GOTO);
                    }
                }

            }
        });
    }

    public Manager getUndoRedoManager() {
        return getUndoManager();
    }

    @Override
    public void saveDocument() throws IOException {
        super.saveDocument();
        syncModel();
        getDataObject().setModified(false);
    }

    @Override
    protected UndoRedo.Manager createUndoRedoManager() {
        // Override so the superclass will use our proxy undo manager
        // instead of the default, then we can intercept edits.
        return new QuietUndoManager(super.createUndoRedoManager());
    // Note we cannot set the document on the undo manager right
    // now, as CES is probably trying to open the document.
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
            undo.setModel(null);

        }
        super.notifyClosed();
        getUndoManager().discardAllEdits();
    //prepareTask = null;
    }

    /* @Override
    public Task prepareDocument() {
    QuietUndoManager undo = (QuietUndoManager) getUndoRedo();
    Task task = super.prepareDocument();
    // Avoid listening to the same task more than once.
    if (task == prepareTask) {
    return task;
    }
    synchronized (undo) {
    task.addTaskListener(new TaskListener() {
    
    public void taskFinished(Task task) {            
    QuietUndoManager undo = (QuietUndoManager) getUndoRedo();
    StyledDocument doc = getDocument();
    synchronized (undo) {
    // Now that the document is ready, pass it to the manager.
    undo.setDocument((AbstractDocument) doc);
    if (!undo.isCompound()) {
    doc.removeUndoableEditListener(undo);
    // If not listening to document, then listen to model.
    // addUndoManagerToModel(undo);
    }
    }
    }
    });
    prepareTask = task;
    }
    return task;
    }
    private transient Task prepareTask;*/
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

    public void addUndoManagerToModel(QuietUndoManager undo) {
        ETLDataObject etlDataObject = (ETLDataObject) getDataObject();
        ETLCollaborationModel model = etlDataObject.getModel();
        //BpelModel model = getBpelModel();
        if (model != null) {
            // Ensure the listener is not added twice.
            removeUndoManagerFromModel();
            model.addUndoableEditListener(undo);
        /* Ensure the model is sync'd when undo/redo is invoked,
         * otherwise the edits are added to the queue and eventually
         * cause exceptions.
         */
        //undo.setModel(model);

        }
    }

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

    private void removeUndoManagerFromModel() {
        ETLDataObject etlDataObject = (ETLDataObject) getDataObject();
        ETLCollaborationModel model = etlDataObject.getModel();
        if (model != null) {
            QuietUndoManager undo = getUndoManager();
            model.removeUndoableEditListener(undo);
            // Must unset the model when leaving model view.
            undo.setModel(null);
        }
    }

    @Override
    protected void initializeCloneableEditor(CloneableEditor editor) {
        super.initializeCloneableEditor(editor);
        // Update later to avoid an infinite loop.
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                updateTitles();
            }
        });
    }

    public void setUpdatedDuringLoad(boolean val) {
        updatedDuringLoad = val;
    }

    public static void setProjectInfo(String name, String prjInfo, boolean value) {
        IS_PROJECT_CALL = value;
        ETLEditorSupport.PRJ_NAME = name;
        ETLEditorSupport.PRJ_PATH = prjInfo;
    }
    public static boolean IS_PROJECT_CALL = false;
}
