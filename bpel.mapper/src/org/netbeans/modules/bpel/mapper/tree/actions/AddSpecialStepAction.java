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
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.predicates.SpecialStepManager;
import org.netbeans.modules.bpel.mapper.predicates.editor.PathConverter;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.VariableTreeModel;
import org.netbeans.modules.bpel.mapper.tree.search.TreeFinderProcessor;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.soa.mappercore.LeftTree;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeTestType;
import org.netbeans.modules.xml.xpath.ext.StepNodeTypeTest;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.openide.util.NbBundle;

/**
 * Shows the Expression editor dialog in order to create a new predicate.
 *
 * @author nk160297
 */
public class AddSpecialStepAction extends MapperAction<Iterable<Object>> {
    
    private static final long serialVersionUID = 1L;
    
    private StepNodeTestType mStepType;
    private boolean mInLeftTree;
    private TreePath mTreePath;
    
    public AddSpecialStepAction(StepNodeTestType stepType, 
            MapperTcContext mapperTcContext,
            boolean inLeftTree, TreePath treePath, 
            Iterable<Object> dataObjectPathItrb) {
        super(mapperTcContext, dataObjectPathItrb);
        mStepType = stepType;
        mTreePath = treePath;
        mInLeftTree = inLeftTree;
        postInit();
        // putValue(DeleteAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
    }
    
    @Override
    public String getDisplayName() {
        switch (mStepType) {
            case NODETYPE_COMMENT:
                return NbBundle.getMessage(MapperAction.class, "ADD_STEP_COMMENT"); // NOI18N
            case NODETYPE_NODE:
                return NbBundle.getMessage(MapperAction.class, "ADD_STEP_NODE"); // NOI18N
            case NODETYPE_PI:
                return NbBundle.getMessage(MapperAction.class, "ADD_STEP_PI"); // NOI18N
            case NODETYPE_TEXT:
                return NbBundle.getMessage(MapperAction.class, "ADD_STEP_TEXT"); // NOI18N
        }
        //
        return null;
    }
    
    public void actionPerformed(ActionEvent e) {
        Iterable<Object> itrb = getActionSubject();
        //
        // Construct a new Schema Context by the current element
        XPathSchemaContext sContext = PathConverter.constructContext(itrb, false);
        if (sContext == null) {
            return;
        }
        //
        // Create a new LocationStep 
        // TODO show modal dialog for processing instruciton
        StepNodeTypeTest newStepType = new StepNodeTypeTest(mStepType, null);
        BpelEntity selectedEntity = getDesignContext().getSelectedEntity();
        XPathModel newModel = BpelXPathModelFactory.create(selectedEntity);
        newModel.setSchemaContext(sContext);
        LocationStep newStep = newModel.getFactory().
                newLocationStep(null, newStepType, null);
        newModel.setRootExpression(newStep);
        //
        // Add the new predicate to the PredicateManager
        MapperModel mm = mMapperTcContext.getMapper().getModel();
        assert mm instanceof BpelMapperModel;
        BpelMapperModel mapperModel = (BpelMapperModel)mm;
        
        MapperSwingTreeModel treeModel = null;
        if (mInLeftTree) {
            treeModel = mapperModel.getLeftTreeModel();
        } else {
            treeModel = mapperModel.getRightTreeModel();
        }
        //
        MapperTreeModel sourceModel = treeModel.getSourceModel();
        VariableTreeModel varTreeModel = MapperTreeModel.Utils.
                findExtensionModel(sourceModel, VariableTreeModel.class);
        if (varTreeModel != null) {
            SpecialStepManager sStepManager = varTreeModel.getSStepManager();
            if (sStepManager != null) {
                sStepManager.addStep(itrb, newStep);
            }
        }
        //
        // Update tree
        treeModel.insertChild(mTreePath, 0, newStep);
        //
        // Set selection to the added predicate item
        TreeFinderProcessor findProc = new TreeFinderProcessor(treeModel);
        TreePath newPredPath = findProc.findChildByDataObj(mTreePath, newStep);
        if (mInLeftTree) {
            LeftTree leftTree = mMapperTcContext.getMapper().getLeftTree();
            leftTree.setSelectionPath(newPredPath);
        } else {
            Mapper mapper = mMapperTcContext.getMapper();
            mapper.setSelected(newPredPath);
        }
        
    }
    
    
}
