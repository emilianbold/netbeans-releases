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

package org.netbeans.modules.bpel.mapper.cast;

import java.util.Collections;
import java.util.List;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.BpelExtManagerHolder;
import org.netbeans.modules.bpel.mapper.model.BpelMapperLsmProcessor;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.model.BpelMapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.model.BpelPathConverter;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.ext.editor.api.LocationStepModifier;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmContainer;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.soa.mappercore.LeftTree;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.lsm.DetachedPseudoComp;
import org.netbeans.modules.soa.xpath.mapper.lsm.ExtRegistrationException;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperPseudoComp;
import org.netbeans.modules.soa.xpath.mapper.lsm.PseudoCompManager;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;

/**
 * The class collects all Pseudo Components, which are used in the edited 
 * XPath expression. Each PseudoComp is declared in the special BPEL extension element.
 * The PseudoComp manager keeps the location of type PseudoComp in term of
 * schema components' path.
 *
 * The main intention of the PseudoComp manager is to provide showing of
 * PseudoComp objects in the mapper source and destination trees.
 * 
 * @author nk160297
 */
public class BpelPseudoCompManager extends PseudoCompManager<VariableDeclaration> {
    
    @Override
    public BpelMapperPseudoComp createMapperPseudoComp(XPathPseudoComp pseudoComp) {
        return new BpelMapperPseudoComp(pseudoComp);
    }

    public LocationStepModifier registerNewPseudoComp(
            MapperTcContext mapperTcContext,
            TreeItem anyTreeItem, BpelMapperPseudoComp newPseudo)
            throws ExtRegistrationException {
        //
        DirectedList<Object> parentCompPath = BpelPathConverter.singleton().
                constructObjectLocationList(anyTreeItem, true, true);
        //
        LocationStepModifier result = null;
        try {
            if (parentCompPath != null) {
                if (addPseudoCompImpl(parentCompPath, newPseudo)) {
                    VariableDeclaration varDecl = newPseudo.getBaseBpelVariable();
                    if (varDecl != null) {
                        result = BpelMapperLsmProcessor.registerLsmToVariable(
                                mapperTcContext, varDecl, parentCompPath,
                                newPseudo, mMapperTreeModel.isLeftMapperTree());
                    }
                }
            }
        } finally {
            if (result == null) {
                // remove newly cached pseudo component if it didn't manage
                // to register it in BPEL
                removePseudoComp(newPseudo);
            }
        }
        //
        return result;
    }
    
    public LocationStepModifier registerPseudoComp(BPELElementsBuilder builder,
            LsmContainer destination, BpelMapperPseudoComp newPseudoComp)
            throws ExtRegistrationException {
        //
        PseudoComp newBpelPseudo = builder.createExtensionEntity(PseudoComp.class);
        destination.addExtension(newBpelPseudo);
        try {
            BpelMapperPseudoComp.populatePseudoComp(newPseudoComp,
                    newBpelPseudo, (BpelEntity)destination, 
                    mMapperTreeModel.isLeftMapperTree());
            return newBpelPseudo;
        } catch (ExtRegistrationException ex) {
            //
            // Delete the last PseudoComponent
            destination.removeExtension(newBpelPseudo);
            // rethrow the exception
            throw ex;
        }
    }

    public boolean addPseudoCompCmd(DetachedPseudoComp newDPC,
            TreePath xsdAnyTreePath, boolean inLeftTree,
            MapperStaticContext sContext) throws ExtRegistrationException {
        //
        // Preparation
        MapperTcContext tcContext = MapperTcContext.class.cast(sContext);
        BpelMapperModel bmm = tcContext.getMapperModel();
        BpelMapperSwingTreeModel treeModel = inLeftTree ?
            bmm.getLeftTreeModel() : bmm.getRightTreeModel();
        TreeItem xsdAnyTreeItem = MapperSwingTreeModel.getTreeItem(xsdAnyTreePath);
        BpelExtManagerHolder bemh = treeModel.getExtManagerHolder();
        BpelPseudoCompManager pseudoCompManager = bemh.getPseudoCompManager();
        //
        try {
            //
            XPathSchemaContext baseSContext = BpelPathConverter.singleton().
                    constructContext(xsdAnyTreeItem, true);
            BpelMapperPseudoComp newPseudoComp =
                    new BpelMapperPseudoComp(baseSContext, newDPC);
            LocationStepModifier newPseudo = pseudoCompManager.
                    registerNewPseudoComp(tcContext, xsdAnyTreeItem, newPseudoComp);
            if (newPseudo == null) {
                return Boolean.FALSE;
            }
            //
            TreePath parentPath = xsdAnyTreePath.getParentPath();
            //
            // Update tree
            int childIndex = treeModel.getIndexOfChild(
                    parentPath.getLastPathComponent(),
                    xsdAnyTreePath.getLastPathComponent());
            treeModel.insertChild(parentPath, childIndex + 1, newPseudoComp);
            //
            // Set selection to the added pseudo component item
            Mapper mapper = tcContext.getMapper();
            if (mapper != null) {
                TreeFinderProcessor findProc = new TreeFinderProcessor(treeModel);
                TreePath newPseudoPath =
                        findProc.findChildByDataObj(parentPath, newPseudoComp);
                //
                if (inLeftTree) {
                    LeftTree leftTree = mapper.getLeftTree();
                    leftTree.setSelectionPath(newPseudoPath);
                } else {
                    mapper.expandGraphs(Collections.singletonList(newPseudoPath));
                    mapper.setSelected(newPseudoPath);
                }
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

    public boolean deletePseudoCompCmd(MapperPseudoComp oldPseudoComp, 
            TreeItem treeItem, TreePath subjectTPath, boolean inLeftTree,
            MapperStaticContext stContext) {
        //
        MapperTcContext tcContext = MapperTcContext.class.cast(stContext);
        BpelMapperPseudoComp oldBpelPseudoComp =
                BpelMapperPseudoComp.class.cast(oldPseudoComp);
        //
        BpelMapperLsmProcessor lsmProcessor = new BpelMapperLsmProcessor(tcContext);
        lsmProcessor.deleteLsm(tcContext, treeItem, oldBpelPseudoComp, inLeftTree);
        //
        BpelMapperModel mModel = tcContext.getMapperModel();
        MapperSwingTreeModel treeModel = null;
        if (inLeftTree) {
            treeModel = mModel.getLeftTreeModel();
        } else {
            treeModel = mModel.getRightTreeModel();
        }
        //
        // Update BPEL model
        if (inLeftTree) {
            treeModel = mModel.getLeftTreeModel();
            List<TreePath> dependentGraphs =
                    mModel.getLeftChangeAffectedGraphs(oldBpelPseudoComp);
            for (TreePath graphPath : dependentGraphs) {
                mModel.removeIngoingLinks(graphPath, subjectTPath);
            }
            //
            // Modify BPEL model for all changed graphs in one transaction.
            mModel.fireGraphsChanged(dependentGraphs);
        } else {
            treeModel = mModel.getRightTreeModel();
            mModel.removeNestedGraphs(subjectTPath);
        }
        //
        // Remove node from the tree
        treeModel.remove(subjectTPath);
        //
        return true;
    }

}
  
