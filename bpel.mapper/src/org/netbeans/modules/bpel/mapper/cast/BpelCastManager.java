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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.BpelExtManagerHolder;
import org.netbeans.modules.bpel.mapper.model.BpelPathConverter;
import org.netbeans.modules.bpel.mapper.model.BpelMapperLsmProcessor;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.model.BpelMapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.tree.models.VariableDeclarationWrapper;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.LocationStepModifier;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmContainer;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmProcessor;
import org.netbeans.modules.bpel.model.ext.editor.api.NestedExtensionsVisitor;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.soa.mappercore.LeftTree;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.lsm.CastManager;
import org.netbeans.modules.soa.xpath.mapper.lsm.ExtRegistrationException;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperTypeCast;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCast;
import org.openide.ErrorManager;

/**
 * The class collects all type casts which are used in the edited 
 * XPath expression. Each type cast is declared in the special BPEL extension element.
 * The cast manager keeps the location of type cast in term of
 * schema components' path.
 *
 * The main intention of the cast manager is to provide showing of
 * type casts in the mapper source and destination trees.
 * 
 * @author nk160297
 */
public class BpelCastManager extends CastManager<VariableDeclaration> {

    @Override
    public BpelMapperTypeCast createMapperTypeCast(XPathCast cast) {
        return new BpelMapperTypeCast(cast);
    }

    public LocationStepModifier registerNewTypeCast(
            MapperTcContext mapperTcContext,
            TreeItem castedTreeItem, BpelMapperTypeCast newCast)
            throws ExtRegistrationException {
        //
        DirectedList<Object> castedCompPath = BpelPathConverter.singleton().
                constructObjectLocationList(castedTreeItem, true, false);
        //
        LocationStepModifier result = null;
        try {
            if (castedCompPath != null) {
                if (addTypeCastImpl(castedCompPath, newCast)) {
                    VariableDeclaration varDecl = newCast.getBaseBpelVariable();
                    if (varDecl != null) {
                        result = BpelMapperLsmProcessor.registerLsmToVariable(
                                mapperTcContext, varDecl, castedCompPath,
                                newCast, mMapperTreeModel.isLeftMapperTree());
                    }
                }
            }
        } finally {
            if (result == null) {
                // remove newly cached type cast if it didn't manage
                // to register it in BPEL
                removeTypeCast(newCast);
            }
        }
        //
        return result;
    }
    
    public LocationStepModifier registerTypeCast(BPELElementsBuilder builder,
            LsmContainer destination, BpelMapperTypeCast newTypeCast)
            throws ExtRegistrationException {
        //
        Cast newCast = builder.createExtensionEntity(Cast.class);
        destination.addExtension(newCast);
        boolean populated = newTypeCast.populateCast(
                newCast, (BpelEntity)destination, mMapperTreeModel.isLeftMapperTree());
        if (!populated) {
            destination.removeExtension(newCast);
            throw new ExtRegistrationException(null);
        }
        return newCast;
    }

    /**
     * Looks for a BPEL Editor extension Cast object under 
     * the specified BPEL entity. The Editor extension is sought at first.
     * @param soughtPred
     * @param bpelEntity
     *
     * TODO: Refactor according the new hierarchy arrangment of extensions
     *
     * @return
     */
    public Cast lookForCast(BpelMapperTypeCast soughtCast,
            ExtensibleElements bpelEntity) {
        //
        Editor editor = LsmProcessor.getEditorEntity(bpelEntity, null, false);
        if (editor == null) {
            return null;
        }
        //
        return lookForCast(editor, soughtCast);
   }
    
    /**
     * Looks for a BPEL Editor extension Cast object under 
     * the specified extension container. 
     * @param container
     * @param soughtPred
     * @return
     */
    public Cast lookForCast(LsmContainer container,
            final BpelMapperTypeCast soughtCast) {
        //
        class CastFinder extends NestedExtensionsVisitor.ForwardTracer {
            
            Cast mFoundCast = null;

            @Override
            public void visit(Cast cast) {
                if (mFoundCast != null) {
                    return;
                }
                //
                boolean inLeftMapperTree = mMapperTreeModel.isLeftMapperTree();
                if (cast.getSource() == Source.TO && inLeftMapperTree ||
                        cast.getSource() != Source.TO && !inLeftMapperTree) {
                    // Skip predicates with oposit source
                    return;
                } 
                //
                BpelMapperTypeCast typeCast = BpelMapperTypeCast.convert(cast);
                if (typeCast != null) {
                    if (typeCast.equals(soughtCast)) {
                        mFoundCast = cast;
                        return;
                    }
                }
                //
                visitNestedExt((LsmContainer)cast);
            }
            
            public Cast getFound() {
                return mFoundCast;
            }
        }
        //
        CastFinder castFinder = new CastFinder();
        container.accept(castFinder);
        //
        return castFinder.getFound();
   }
   
    /**
     * Returns casted variables based on the specified variable.
     * @param parentPath
     * @param var
     * @return
     */
    public List<MapperTypeCast> getCastedVariables(
            AbstractVariableDeclaration var, Part part) {
        //
        ArrayList<MapperTypeCast> result = new ArrayList<MapperTypeCast>();
        if (mCachedCastedVarList != null && !mCachedCastedVarList.isEmpty()) {
            VariableDeclaration varDecl = null;
            if (var instanceof VariableDeclaration) {
                varDecl = (VariableDeclaration)var;
            } else if (var instanceof VariableDeclarationWrapper) {
                varDecl = VariableDeclarationWrapper.class.cast(var).getDelegate();
            }
            XPathBpelVariable xPathVar = new XPathBpelVariable(varDecl, part);
            //
            for (CachedVariableCast cVCast : mCachedCastedVarList) {
                if (cVCast.getCastedVariableDecl().equals(xPathVar)) {
                    result.add(cVCast.getTypeCast());
                }
            }
        }
        //
        return result;
    }

    public TreePath addCastCmd(GlobalType targetGType, TreePath castedTPath,
            boolean inLeftTree, MapperStaticContext stContext) {
        //
        // Preparation
        MapperTcContext tcContext = MapperTcContext.class.cast(stContext);
        BpelMapperModel bmm = tcContext.getMapperModel();
        BpelMapperSwingTreeModel treeModel = inLeftTree ?
            bmm.getLeftTreeModel() : bmm.getRightTreeModel();
        TreeItem castedTreeItem = MapperSwingTreeModel.getTreeItem(castedTPath);
        BpelExtManagerHolder bemh = treeModel.getExtManagerHolder();
        BpelCastManager castManager = bemh.getCastManager();
        //
        // The  iterator points to the casted component
        XPathSchemaContext castedItemSContext = BpelPathConverter.singleton().
                constructContext(castedTreeItem, false);
        BpelMapperTypeCast newTypeCast =
                new BpelMapperTypeCast(castedItemSContext, targetGType);
        try {
            LocationStepModifier newBpelTypeCast = castManager.
                    registerNewTypeCast(tcContext, castedTreeItem, newTypeCast);
            if (newBpelTypeCast == null) {
                return null;
            }
        } catch (ExtRegistrationException ex) {
            ErrorManager.getDefault().notify(ex);
            return null;
        }
        //
        // Update tree
        TreePath parentPath = castedTPath.getParentPath();
        int childIndex = treeModel.getIndexOfChild(
                parentPath.getLastPathComponent(),
                castedTPath.getLastPathComponent());
        treeModel.insertChild(parentPath, childIndex + 1, newTypeCast);
        //
        TreeFinderProcessor findProc = new TreeFinderProcessor(treeModel);
        TreePath newTcPath = findProc.findChildByDataObj(parentPath, newTypeCast);
        //
        // Set selection to the added predicate item
        Mapper mapper = tcContext.getMapper();
        if (mapper != null) {
            if (inLeftTree) {
                LeftTree leftTree = mapper.getLeftTree();
                leftTree.setSelectionPath(newTcPath);
            } else {
                mapper.expandGraphs(Collections.singletonList(newTcPath));
                mapper.setSelected(newTcPath);
            }
        }
        //
        return newTcPath;
    }

    public boolean deleteCastCmd(MapperTypeCast oldCast, TreeItem treeItem, 
            TreePath subjectTPath, boolean inLeftTree,
            MapperStaticContext stContext) {
        //
        MapperTcContext tcContext = MapperTcContext.class.cast(stContext);
        BpelMapperTypeCast bpelOldCast = BpelMapperTypeCast.class.cast(oldCast);

        BpelMapperLsmProcessor lsmProcessor = new BpelMapperLsmProcessor(tcContext);
        lsmProcessor.deleteLsm(tcContext, treeItem, bpelOldCast, inLeftTree);
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
            List<TreePath> dependentGraphs =
                    mModel.getLeftChangeAffectedGraphs(bpelOldCast);
            for (TreePath graphPath : dependentGraphs) {
                mModel.removeIngoingLinks(graphPath, subjectTPath);
            }
            //
            // Modify BPEL model for all changed graphs in one transaction.
            mModel.fireGraphsChanged(dependentGraphs);
        } else {
            mModel.removeNestedGraphs(subjectTPath);
        }
        //
        // Remove node from the tree
        treeModel.remove(subjectTPath);
        //
        return true;
    }

}
  
