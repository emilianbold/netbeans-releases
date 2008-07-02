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

package org.netbeans.modules.bpel.mapper.tree.actions;

import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.cast.PseudoCompEditor;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.cast.DetachedPseudoComp;
import org.netbeans.modules.bpel.mapper.cast.ExtRegistrationException;
import org.netbeans.modules.bpel.mapper.cast.PseudoCompManager;
import org.netbeans.modules.bpel.mapper.cast.SyntheticPseudoComp;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.soa.mappercore.LeftTree;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.openide.util.NbBundle;

/**
 * Shows the Type Chooser dialog to create a new Pseudo schema component based on 
 * Any or AnyAttribute. 
 *
 * @author nk160297
 */
public class AddPseudoCompAction extends MapperAction<TreeItem> {
    
    private static final long serialVersionUID = 1L;
    
    private boolean mInLeftTree;
    private TreePath mTreePath;
    
    private AnyElement mAnyElement;
    private AnyAttribute mAnyAttr;
    private boolean mIsAttribute;
    
    public AddPseudoCompAction(AnyElement anyElement, 
            MapperTcContext mapperTcContext,
            boolean inLeftTree, TreePath treePath, 
            TreeItem treeItem) {
        super(mapperTcContext, treeItem);
        mTreePath = treePath;
        mInLeftTree = inLeftTree;
        mAnyElement = anyElement;
        mIsAttribute = false;
        postInit();
    }
    
    public AddPseudoCompAction(AnyAttribute anyAttr, 
            MapperTcContext mapperTcContext,
            boolean inLeftTree, TreePath treePath, 
            TreeItem treeItem) {
        super(mapperTcContext, treeItem);
        mTreePath = treePath;
        mInLeftTree = inLeftTree;
        mAnyAttr = anyAttr;
        mIsAttribute = true;
        postInit();
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(MapperAction.class, "ADD_CAST"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent e) {
        //
        // The  iterator points to an xsd:any or xsd:anyAttribute component
        final TreeItem treeItem = getActionSubject();
        //
        // Add the new type PseudoComp to the PseudoCompManager
        MapperModel mm = mMapperTcContext.getMapper().getModel();
        assert mm instanceof BpelMapperModel;
        BpelMapperModel mapperModel = (BpelMapperModel)mm;
        //
        MapperSwingTreeModel treeModel = null;
        if (mInLeftTree) {
            treeModel = mapperModel.getLeftTreeModel();
        } else {
            treeModel = mapperModel.getRightTreeModel();
        }
        //
        SoaTreeModel sourceModel = treeModel.getSourceModel();
        final PseudoCompManager pseudoCompManager = 
                PseudoCompManager.getPseudoCompManager(sourceModel);
        if (pseudoCompManager == null) {
            return ;
        }
        //
        BpelDesignContext bdContext = 
                mMapperTcContext.getDesignContextController().getContext();
        assert bdContext != null;
        //
        PseudoCompEditor editor = null;
        if (mIsAttribute) {
            editor = new PseudoCompEditor(treeItem, mAnyAttr, 
                    bdContext.getBpelModel(), mapperModel, mInLeftTree);
        } else {
            editor = new PseudoCompEditor(treeItem, mAnyElement, 
                    bdContext.getBpelModel(), mapperModel, mInLeftTree);
        }
        //
        final MapperSwingTreeModel finalTreeModel = treeModel;
        final PseudoCompEditor finalEditor = editor;
        //
        // Prepare Ok processor for the PseudoComp editor
        Callable<Boolean> okProcessor = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                try {
                    DetachedPseudoComp newDPC = finalEditor.getSelectedValue();
                    //
                    SyntheticPseudoComp newPseudoComp = new SyntheticPseudoComp(treeItem, newDPC);
                    boolean pseudoAdded = pseudoCompManager.addPseudoComp(treeItem, newPseudoComp);
                    //
                    TreePath parentPath = mTreePath.getParentPath();
                    if (pseudoAdded) {
                        //
                        // Update tree
                        int childIndex = finalTreeModel.getIndexOfChild(
                                parentPath.getLastPathComponent(), 
                                mTreePath.getLastPathComponent());
                        finalTreeModel.insertChild(parentPath, childIndex + 1, newPseudoComp);
                    }
                    //
                    // Set selection to the added pseudo component item
                    TreeFinderProcessor findProc = new TreeFinderProcessor(finalTreeModel);
                    TreePath newPredPath = findProc.findChildByDataObj(parentPath, newPseudoComp);
                    if (mInLeftTree) {
                        LeftTree leftTree = mMapperTcContext.getMapper().getLeftTree();
                        leftTree.setSelectionPath(newPredPath);
                    } else {
                        Mapper mapper = mMapperTcContext.getMapper();
                        mapper.setSelected(newPredPath);
                    }
                } catch (ExtRegistrationException regEx) {
                    Throwable cause = regEx.getCause();
                    if (cause instanceof VetoException) {
                        VetoException vetoEx = (VetoException)cause;
                        String errMsg = vetoEx.getMessage();
                        UserNotification.showMessage(errMsg);
                        return Boolean.FALSE;       
                    } else if (cause instanceof InvalidNamespaceException) {
                        InvalidNamespaceException nsEx = (InvalidNamespaceException)cause;
                        String errMsg = nsEx.getMessage();
                        UserNotification.showMessage(errMsg);
                        return Boolean.FALSE;       
                    } else {
                        throw regEx;
                    }
                }
                //
                return Boolean.TRUE;
            }
        };
        //
        PseudoCompEditor.showDlg(editor, okProcessor);
    }
    
}
