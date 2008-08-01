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

package org.netbeans.modules.bpel.mapper.predicates.editor;

import java.util.List;
import javax.swing.Action;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.cast.CastManager;
import org.netbeans.modules.bpel.mapper.cast.PseudoCompManager;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.predicates.AbstractPredicate;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.EmptyTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.VariableTreeInfoProvider;
import org.netbeans.modules.bpel.mapper.tree.models.VariableTreeModel;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.utils.GraphLayout;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;

/**
 * Implementaiton of the MapperModelFactory for the BPEL mapper.
 * 
 * @author nk160297
 */
public class PredicateMapperModelFactory {

    public BpelMapperModel constructEmptyModel(MapperTcContext mapperTcContext, 
            CastManager castManager, PseudoCompManager pseudoCompManager) {
        //
        BpelDesignContext dContext = 
                mapperTcContext.getDesignContextController().getContext();
        //
        EmptyTreeModel sourceModel = new EmptyTreeModel();
        VariableTreeModel variableModel = new VariableTreeModel(
                dContext, null, null, castManager, pseudoCompManager, 
                new MyTreeInfoProvider());
        sourceModel.addExtensionModel(variableModel);
        //
        PredicateExprTreeModel targetModel = new PredicateExprTreeModel(1);
        //
        BpelMapperModel newMapperModel = new BpelMapperModel(
                mapperTcContext, null, sourceModel, targetModel);
        //
        return newMapperModel;
    }

    public BpelMapperModel constructModel(MapperTcContext mapperTcContext, 
            XPathSchemaContext sContext, AbstractPredicate pred, 
            CastManager castManager, PseudoCompManager pseudoCompManager) {
        //
        BpelDesignContext dContext = 
                mapperTcContext.getDesignContextController().getContext();
        //
        EmptyTreeModel sourceModel = new EmptyTreeModel();
        VariableTreeModel variableModel = new VariableTreeModel(
                dContext, null, null, castManager, pseudoCompManager, 
                new MyTreeInfoProvider());
        sourceModel.addExtensionModel(variableModel);
        //
        XPathPredicateExpression[] predicateArr = pred.getPredicates();
        PredicateExprTreeModel targetModel = 
                new PredicateExprTreeModel(predicateArr.length);
        //
        BpelMapperModel newMapperModel = new BpelMapperModel(
                mapperTcContext, null, sourceModel, targetModel);
        //
        MapperSwingTreeModel rightTreeModel = newMapperModel.getRightTreeModel();
        TreeFinderProcessor findProc = new TreeFinderProcessor(rightTreeModel);
        List<TreePath> targetTreePathList = 
                findProc.findChildren(
                new TreePath(rightTreeModel.getRoot()), 
                new PredicateFinder());
        assert targetTreePathList.size() == predicateArr.length;
        //
        for (int index = 0; index < predicateArr.length; index++) {
            XPathPredicateExpression predicate = predicateArr[index];
            TreePath predicateGraphPath = targetTreePathList.get(index);
            addPredicateGraph(sContext, predicateGraphPath, 
                    predicate, newMapperModel);
        }
        //
        return newMapperModel;
    }

    private void addPredicateGraph(XPathSchemaContext sContext, 
            TreePath predicateGraphPath, 
            XPathPredicateExpression predicate, 
            BpelMapperModel newMapperModel) {
        //
        Graph newGraph = new Graph(newMapperModel, predicate);
        //
        // Populate the graph
        MapperSwingTreeModel leftTreeModel = newMapperModel.getLeftTreeModel();
        PredicateGraphBuilderVisitor graphBuilderVisitor = 
                new PredicateGraphBuilderVisitor(
                sContext, newGraph, leftTreeModel, true, null);
        predicate.accept(graphBuilderVisitor);
        //        
        // Attach the graph to the mapper
        newMapperModel.addGraph(newGraph, predicateGraphPath);
        GraphLayout.layout(newGraph);
    }
    
    private static class MyTreeInfoProvider extends VariableTreeInfoProvider {
        
        @Override
        public List<Action> getMenuActions(TreeItem treeItem, Object context, TreePath treePath) {
            return null;
        }
        
    }
    
}
