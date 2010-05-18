/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.dataobject;

import org.netbeans.modules.edm.editor.multiview.MashupGraphMultiViewDesc;
import org.netbeans.modules.edm.editor.multiview.MashupMultiViewFactory;
import java.awt.EventQueue;
import java.io.Serializable;
import java.util.Enumeration;
import javax.swing.text.Document;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.UndoRedo;
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
import java.util.List;
import org.netbeans.modules.edm.model.MashupCollaborationModel;
import org.netbeans.modules.edm.model.ValidationInfo;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

public class MashupDataEditorSupport extends DataEditorSupport
        implements OpenCookie, EditCookie, EditorCookie.Observable,
        LineCookie, CloseCookie, PrintCookie {

    private TopComponent multiviewTC;
    public static String PRJ_PATH = "";
    public static String PRJ_NAME = "";

    public MashupDataEditorSupport(MashupDataObject sobj) {
        super(sobj, new MashupEditorEnv(sobj));
        setMIMEType(MashupDataLoader.REQUIRED_MIME);
        PRJ_PATH = sobj.getFolder().getPrimaryFile().getParent().getPath();
        PRJ_PATH = PRJ_PATH.replace('/', '\\');
        PRJ_NAME = sobj.getFolder().getPrimaryFile().getParent().getName();
    }

    public MashupEditorEnv getEnv() {
        return (MashupEditorEnv) env;
    }

    @Override
    protected Pane createPane() {
        multiviewTC = MashupMultiViewFactory.createMultiView(
                (MashupDataObject) getDataObject());

        multiviewTC.setName(getDataObject().getPrimaryFile().getNameExt());

        Mode editorMode = WindowManager.getDefault().findMode(
                MashupDataEditorSupport.EDITOR_MODE);
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
        if (!getEnv().getMashupDataObject().isModified()) {
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
        Enumeration en =
                ((CloneableTopComponent) tc).getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements()) {
                oneOrLess = false;
            }
        }

        return oneOrLess;
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
                            if (mvp.preferredID().contains(
                                    MashupGraphMultiViewDesc.PREFERRED_ID)) {
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

    @Override
    protected void initializeCloneableEditor(CloneableEditor editor) {
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

    public MashupCollaborationModel getModel() throws IOException {
        return getEnv().getMashupDataObject().getModel();
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

    @Override
    public void saveDocument() throws IOException {
        synchDocument();
        syncModel();
        super.saveDocument();
        getDataObject().setModified(false);
        MashupDataObject mObj = (MashupDataObject) getDataObject();
        mObj.getModel().setDirty(false);
    }

    /**
     * Have the WSDL model sync with the document.
     */
    public void syncModel() {
        // Only sync the document if the change relates to loss of focus,
        // which indicates that we are switching from the source view.
        // Update the tree with the modified text.
        try {
            if (getModel() != null) {
                getModel().sync();
                openDocument();
                String defnContent = getDocument().getText(0, getDocument().getLength());
                getModel().reLoad(defnContent);
                // is below required? 
                getModel().setReloaded(true);
                updateBadge((MashupDataObject) getDataObject());
            }
        } catch (Exception ioe) {
            // The document cannot be parsed
            //NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(MashupDataEditorSupport.class, "LBL_WSDL_is_not_well-formed"), NotifyDescriptor.ERROR_MESSAGE);
            //DialogDisplayer.getDefault().notify(nd);
            ErrorManager.getDefault().notify(ioe);
            StatusDisplayer.getDefault().setStatusText("syncModel: " + ioe.getMessage());
        }

    }

    public void synchDocument() {
        try {
            MashupDataObject dataObject = (MashupDataObject) getDataObject();
            if (dataObject.getModel() != null) {
                String content = dataObject.getModel().getEDMDefinition().toXMLString("");
                Document doc = getDocument();
                if (doc != null) {
                    doc.remove(0, getDocument().getLength());
                    doc.insertString(0, content, null);
                }
                dataObject.getModel().setDirty(true);
                dataObject.setModified(true);
                updateBadge(dataObject);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    private void updateBadge(MashupDataObject dataObject) {
        try {
            List<ValidationInfo> list = dataObject.getModel().getEDMDefinition().badgeValidate();
            int errCount = 0;
            for (ValidationInfo vInfo : list) {
                if (vInfo.getValidationType() == ValidationInfo.VALIDATION_ERROR) {
                    errCount++;
                }
            }
            if (dataObject.isValid()) {
                MashupDataNode node = (MashupDataNode) dataObject.getNodeDelegate();

                if (errCount > 0) {
                    node.setCollabState(MashupDataNode.ERROR);
                } else if (!list.isEmpty()) {
                    node.setCollabState(MashupDataNode.WARNING);
                } else {
                    node.setCollabState(MashupDataNode.VALID);
                }
                if (multiviewTC != null) {
                    multiviewTC.setIcon(node.getIcon(0));
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    /**
     * Env class extends DataEditorSupport.Env.
     */
    protected static class MashupEditorEnv extends DataEditorSupport.Env {

        static final long serialVersionUID = 1099957785497677206L;

        public MashupEditorEnv(MashupDataObject obj) {
            super(obj);
        }

        public CloneableEditorSupport findTextEditorSupport() {
            return getMashupDataObject().getMashupDataEditorSupport();
        }

        public MashupDataObject getMashupDataObject() {
            return (MashupDataObject) getDataObject();
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

    /**
     * Implementation of CloseOperationHandler for multiview. Ensures both
     * column view and xml editor are correctly closed, data saved, etc. Holds
     * a reference to DataObject only - to be serializable with the
     * multiview TopComponent without problems.
     */
    /* */
    public static class CloseHandler implements CloseOperationHandler, Serializable {

        private static final long serialVersionUID = -3838395157610633251L;
        private DataObject dataObject;

        private CloseHandler() {
            super();
        }

        public CloseHandler(DataObject dobj) {
            dataObject = dobj;
        }

        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            MashupDataEditorSupport mashupEditor = getMashupEditorSupport();
            if (mashupEditor != null) {
                // This handles saving the document.
                boolean close = mashupEditor.canClose();
                if (close) {
                    if (dataObject.isValid()) {
                        // In case user discarded edits, need to reload.
                        if (dataObject.isModified()) {
                            // In case user discarded edits, need to reload.
                            mashupEditor.reloadDocument().waitFinished();
                        }

                        mashupEditor.syncModel();
                        // Need to properly close the support, too.
                        mashupEditor.notifyClosed();
                    }
                    dataObject.setModified(false);
                }
                return close;
            }
            return true;
        }

        private MashupDataEditorSupport getMashupEditorSupport() {
            return dataObject != null &&
                    dataObject instanceof MashupDataObject ? ((MashupDataObject) dataObject).getMashupDataEditorSupport() : null;

        }
    }
}

