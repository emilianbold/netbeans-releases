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

package org.netbeans.modules.bpel.mapper.predicates;

import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.model.BpelMapperLsmProcessor;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.model.BpelPathConverter;
import org.netbeans.modules.bpel.mapper.predicates.editor.PredicateUpdateController;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.ext.editor.api.LocationStepModifier;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmContainer;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmProcessor;
import org.netbeans.modules.bpel.model.ext.editor.api.Predicate;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.lsm.ExtRegistrationException;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperPredicate;
import org.netbeans.modules.soa.xpath.mapper.lsm.PredicateManager;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.openide.ErrorManager;

/**
 * The class collects all predicate expressions which are in the edited 
 * XPath expression. Each predicate is declared in the XPath location step.
 * The location step is bound to the specific schema element or attribute.
 * The predicate manager keeps the location of predicate in term of
 * schema components' path.
 *
 * The main intention of the predicate manager is to provide showing of
 * predicates in the mapper source and destination trees.
 * 
 * @author nk160297
 */
public class BpelPredicateManager extends PredicateManager<VariableDeclaration> {

    public BpelPredicateManager(Object synchSource) {
        super(synchSource);
    }
    
    @Override
    public BpelMapperPredicate createMapperPredicate(
            PredicatedSchemaContext predSContext) {
        //
        return new BpelMapperPredicate(predSContext);
    }

    public LocationStepModifier registerNewPredicate(
            MapperTcContext mapperTcContext,
            BpelMapperPredicate newPred) throws ExtRegistrationException {
        //
        PredicatedSchemaContext predSContext = newPred.getSchemaContext();
        //
        // skip first because of the path to the parent is required here.
        DirectedList<Object> parentPath = BpelPathConverter.singleton().
                constructObjectLocationList(predSContext, true, true);
        //
        LocationStepModifier result = null;
        try {
            if (parentPath != null && !parentPath.isEmpty()) {
                if (addPredicate(parentPath, newPred)) {
                    VariableDeclaration varDecl = newPred.getBaseBpelVariable();
                    if (varDecl != null) {
                        DirectedList<Object> basePath = 
                                BpelPathConverter.singleton().
                                constructObjectLocationList(
                                predSContext.getBaseContext(), true, false);
                        result = BpelMapperLsmProcessor.registerLsmToVariable(
                                mapperTcContext, varDecl, basePath,
                                newPred, mMapperTreeModel.isLeftMapperTree());
                    }
                }
            }
        } finally {
            if (result == null) {
                // remove newly cached predicate if it didn't manage
                // to register it in BPEL
                removePredicate(newPred);
            }
        }
        //
        return result;
    }
    
    /**
     * Removes the predicate from the cache and unregister it from BPEL Editor extension
     * @param predToDelete
     */
    public boolean deletePredicate(final BpelMapperPredicate predToDelete,
            final BpelMapperModel mapperModel, final TreePath treePath) {
        final VariableDeclaration varDecl = predToDelete.getBaseBpelVariable();
        if (varDecl != null && varDecl instanceof ExtensibleElements) {
            //
            // TODO: check if the deleted predicates is used somewhere and ask 
            // user's confirmation if so. 
            //
            try {
                BpelModel bpelModel = ((BpelEntity)varDecl).getBpelModel();
                bpelModel.invoke(new Callable<Object>() {
                    public Object call() throws Exception {
                        BpelMapperPredicate oldPredicate = predToDelete.clone();
                        //
                        deletePredicateInCaches(varDecl, oldPredicate);
                        LsmProcessor.clearEmptyEditorEntity((ExtensibleElements)varDecl);
                        //
                        // Update BPEL model
                        if (mMapperTreeModel.isLeftMapperTree()) {
                            List<TreePath> dependentGraphs = mapperModel.
                                    getLeftChangeAffectedGraphs(predToDelete);
                            for (TreePath graphPath : dependentGraphs) {
                                mapperModel.removeIngoingLinks(graphPath, treePath);
                            }
                            //
                            // Modify BPEL model for all changed graphs in one transaction.
                            mapperModel.fireGraphsChanged(dependentGraphs);
                        } else {
                            mapperModel.removeNestedGraphs(treePath);
                        }
                        //
                        return null;
                    }
                }, mSynchSource);
                return true;
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        return false;
    }

    public LocationStepModifier registerPredicate(BPELElementsBuilder builder,
            LsmContainer destination, BpelMapperPredicate newPredicate)
            throws ExtRegistrationException {
        //
        Predicate newBpelPred = builder.createExtensionEntity(Predicate.class);
        destination.addExtension(newBpelPred);
        boolean populated = newPredicate.populatePredicate(
                newBpelPred, (BpelEntity)destination, 
                mMapperTreeModel.isLeftMapperTree());
        if (!populated) {
            destination.removeExtension(newBpelPred);
            throw new ExtRegistrationException(null);
        }
        return newBpelPred;
    }

    //==========================================================================

    public TreePath addPredicateCmd(MapperPredicate newPred, TreePath subjectTPath,
            boolean inLeftTree, MapperStaticContext stContext) {
        //
        MapperTcContext tcContext = MapperTcContext.class.cast(stContext);
        BpelMapperPredicate newBpelPred = BpelMapperPredicate.class.cast(newPred);
        //
        PredicateUpdateController controller = new PredicateUpdateController(
                subjectTPath, inLeftTree, tcContext);
        try {
            return controller.addPredicate(newBpelPred);
        } catch (ExtRegistrationException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return null;
    }

    public boolean modifyPredicateCmd(MapperPredicate oldPred,
            XPathPredicateExpression[] newPredArr, TreePath subjectTPath,
            boolean inLeftTree, MapperStaticContext stContext) {
        //
        MapperTcContext tcContext = MapperTcContext.class.cast(stContext);
        BpelMapperPredicate oldBpelPred = BpelMapperPredicate.class.cast(oldPred);
        //
        PredicateUpdateController controller = new PredicateUpdateController(
                subjectTPath, inLeftTree, tcContext);
        try {
            return controller.modifyPredicate(oldBpelPred, newPredArr);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }
    }

    public boolean deletePredicateCmd(MapperPredicate oldPred, TreeItem treeItem, 
            TreePath subjectTPath, boolean inLeftTree, MapperStaticContext stContext) {
        //
        MapperTcContext tcContext = MapperTcContext.class.cast(stContext);
        BpelMapperPredicate oldBpelPred = BpelMapperPredicate.class.cast(oldPred);
        //
        BpelMapperLsmProcessor lsmProcessor = new BpelMapperLsmProcessor(tcContext);
        lsmProcessor.deleteLsm(tcContext, treeItem, oldBpelPred, inLeftTree);
        //
        PredicateUpdateController controller = new PredicateUpdateController(
                subjectTPath, inLeftTree, tcContext);
        return controller.deletePredicate(oldBpelPred);
    }

    //==========================================================================

}
  
