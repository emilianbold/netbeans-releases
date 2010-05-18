/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.mapper.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.mapper.cast.BpelCastManager;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperTypeCast;
import org.netbeans.modules.bpel.mapper.predicates.BpelPredicateManager;
import org.netbeans.modules.soa.xpath.mapper.lsm.CastManager;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperLsm;
import org.netbeans.modules.bpel.mapper.cast.BpelPseudoCompManager;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperPseudoComp;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.predicates.BpelMapperPredicate;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.LocationStepModifier;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmContainer;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmConverter;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmProcessor;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmProcessor.CastWrapperResolver;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmProcessor.PseudoWrapperResolver;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmProcessor.XPathLsmCollector;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmProcessor.XPathLsmContainer;
import org.netbeans.modules.bpel.model.ext.editor.api.Predicate;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.lsm.ExtRegistrationException;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperLsmProcessor;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperLsmTree;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperLsmTree.TreeNode;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;
import org.netbeans.modules.soa.xpath.mapper.utils.XPathMapperUtils;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.CastSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCast;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;
import org.openide.ErrorManager;

/**
 * Process special BPEL extensions like <editor>, <cast>, <pseudoComp>
 *
 * TODO: Generalize the algorithm and move it to BPEL Model in order
 * the model and mapper can share a common algorithm!!!
 * TODO: It's necessary to introduce an XPathPredicate as a LocationStepModifier
 * at the level of XPath model to unify the algorithm.
 *
 * @author nk160297
 */
public class BpelMapperLsmProcessor implements MapperLsmProcessor {
    
    private BpelMapperModel mModel;
    private BpelDesignContext mDContext;
    
    public BpelMapperLsmProcessor(MapperTcContext mapperTcContext) {
        mModel = mapperTcContext.getMapperModel();
        mDContext = mapperTcContext.getDesignContextController().getContext();
        //
        assert mModel != null;
        assert mDContext != null;
    }

    public BpelMapperLsmProcessor(BpelMapperModel mm, BpelDesignContext context) {
        mModel = mm;
        mDContext = context;
        //
        assert mModel != null;
        assert mDContext != null;
    }

    /**
     * Registers all LocationStepModifier extensions declared in variables. 
     */
    public void processVariables() {
        //
        Set<VariableDeclaration> visVars = mDContext.getVisibilityScope().
                getVisibleVariables().getAllVisibleVariables();
        //
        MapperLsmContainer extContainer = new MapperLsmContainer();
        BpelEntity varContextEntity = mDContext.getSelectedEntity();
        for (VariableDeclaration varDecl : visVars) {
            collectsLsm(extContainer, varDecl, varContextEntity, true, true);
        }
        registerAll(extContainer);
    }

    /**
     * Special algorithm for @xsi:type generation
     * Can return null.
     * @param entity
     * @return
     */
    public List<XPathCast> collectsLsmXsiType(BpelEntity entity) {
        //
        assert entity instanceof ExtensibleElements;
        //
        Editor editorExt = LsmProcessor.getEditorEntity(
                (ExtensibleElements)entity, null, false);
        //
        if (editorExt == null) {
            return null;
        }
        //
        XPathLsmXsiTypeCollector collector = new XPathLsmXsiTypeCollector(entity);
        editorExt.accept(collector);
        //
        return collector.getCasts();
    }

    // This method has the similar code in comparison with 
    // constructor of the XPathCastResolverImpl class
    public MapperLsmContainer collectsLsm(MapperLsmContainer extContainer,
            BpelEntity entity, BpelEntity varContextEntity,
            boolean registerFrom, boolean registerTo) {
        //
        if (extContainer == null) {
            extContainer = new MapperLsmContainer();
        }
        //
        assert entity instanceof ExtensibleElements;
        //
        Editor editorExt = LsmProcessor.getEditorEntity(
                (ExtensibleElements)entity, null, false);
        //
        if (editorExt != null) {
            collectsLsm(extContainer, editorExt,
                    varContextEntity, registerFrom, registerTo);
        }
        //
        return extContainer;
    }

    /**
     *
     * @param extContainer
     * @param editor
     * @param varContextEntity see BpelXPathModelFactory#create
     * @param registerFrom
     * @param registerTo
     * @return
     */
    public MapperLsmContainer collectsLsm(MapperLsmContainer extContainer,
            Editor editor, BpelEntity varContextEntity,
            boolean registerFrom, boolean registerTo) {
        //
        if (extContainer == null) {
            extContainer = new MapperLsmContainer();
        }
        //
        XPathLsmCollector collector = new XPathLsmCollector(
                new MapperLsmConvertor(extContainer), varContextEntity, 
                registerFrom, registerTo);
        editor.accept(collector);
        //
        return extContainer;
    }

    //------------------------------------------------------------

    /**
     * Register all LSMs in corresponding managers.
     * @param collector
     *
     * TODO: rename to be more informative. For example,
     */
    public void registerAll(MapperLsmContainer collector) {
        if (collector == null) {
            return;
        }
        //
        List<XPathCast> fromCasts = collector.getFromCasts();
        if (!fromCasts.isEmpty()) {
            CastManager fromCastManager = getCastManager(true);
            if (fromCastManager != null) {
                for (XPathCast cast : fromCasts) {
                    fromCastManager.addTypeCast(cast);
                }
            }
        }
        //
        List<XPathCast> toCasts = collector.getToCasts();
        if (!toCasts.isEmpty()) {
            CastManager toCastManager = getCastManager(false);
            if (toCastManager != null) {
                for (XPathCast cast : toCasts) {
                    toCastManager.addTypeCast(cast);
                }
            }
        }
        //
        List<XPathPseudoComp> fromPseudos = collector.getFromPseudoComps();
        if (!fromPseudos.isEmpty()) {
            BpelPseudoCompManager fromPseudoManager = getPseudoCompManager(true);
            if (fromPseudoManager != null) {
                for (XPathPseudoComp pseudo : fromPseudos) {
                    fromPseudoManager.addPseudoComp(pseudo);
                }
            }
        }
        //
        List<XPathPseudoComp> toPseudos = collector.getToPseudoComps();
        if (!toPseudos.isEmpty()) {
            BpelPseudoCompManager toPseudoManager = getPseudoCompManager(false);
            if (toPseudoManager != null) {
                for (XPathPseudoComp pseudo : toPseudos) {
                    toPseudoManager.addPseudoComp(pseudo);
                }
            }
        }
        //
        List<BpelMapperPredicate> fromPredicates = collector.getFromPredicates();
        if (!fromPredicates.isEmpty()) {
            BpelPredicateManager fromPredicateManager = getPredicateManager(true);
            if (fromPredicateManager != null) {
                for (BpelMapperPredicate predicate : fromPredicates) {
                    fromPredicateManager.addPredicate(predicate);
                }
            }
        }
        //
        List<BpelMapperPredicate> toPredicates = collector.getToPredicates();
        if (!toPredicates.isEmpty()) {
            BpelPredicateManager toPredicateManager = getPredicateManager(false);
            if (toPredicateManager != null) {
                for (BpelMapperPredicate predicate : toPredicates) {
                    toPredicateManager.addPredicate(predicate);
                }
            }
        }
    }

    private BpelCastManager getCastManager(boolean forLeftTree) {
        BpelExtManagerHolder bemh = getBemh(forLeftTree);
        if (bemh != null) {
            return bemh.getCastManager();
        }
        return null;
    }
    
    private BpelPseudoCompManager getPseudoCompManager(boolean forLeftTree) {
        BpelExtManagerHolder bemh = getBemh(forLeftTree);
        if (bemh != null) {
            return bemh.getPseudoCompManager();
        }
        return null;
    }
    
    private BpelPredicateManager getPredicateManager(boolean forLeftTree) {
        BpelExtManagerHolder bemh = getBemh(forLeftTree);
        if (bemh != null) {
            return bemh.getPredicateManager();
        }
        return null;
    }
    
    private BpelExtManagerHolder getBemh(boolean forLeftTree) {
        if (forLeftTree) {
            BpelMapperSwingTreeModel treeModel = mModel.getLeftTreeModel();
            return treeModel.getExtManagerHolder();
        } else {
            BpelMapperSwingTreeModel treeModel = mModel.getRightTreeModel();
            return treeModel.getExtManagerHolder();
        }
    }

    //------------------------------------------------------------

    public static class MapperLsmConvertor implements LsmConverter {

        private MapperLsmContainer mContainer;

        public MapperLsmConvertor() {
        }

        public MapperLsmConvertor(MapperLsmContainer container) {
            mContainer = container;
        }

        public XPathCast processCast(Cast cast, XPathCastResolver parentResolver) {
            BpelMapperTypeCast mapperCast = BpelMapperTypeCast.convert(cast, parentResolver);
            //
            if (mapperCast == null) {
                return null;
            }
            //
            if (mContainer != null) {
                boolean useLeftTree = (cast.getSource() != Source.TO);
                if (useLeftTree) {
                    mContainer.getFromCasts().add(mapperCast);
                } else {
                    mContainer.getToCasts().add(mapperCast);
                }
            }
            //
            return mapperCast;
        }

        public XPathPseudoComp processPseudo(PseudoComp pseudoComp,
                XPathCastResolver parentResolver) {
            //
            BpelMapperPseudoComp mapperPseudo =
                    BpelMapperPseudoComp.convert(pseudoComp, parentResolver);
            //
            if (mapperPseudo == null) {
                return null;
            }
            //
            if (mContainer != null) {
                boolean useLeftTree = (pseudoComp.getSource() != Source.TO);
                if (useLeftTree) {
                    mContainer.getFromPseudoComps().add(mapperPseudo);
                } else {
                    mContainer.getToPseudoComps().add(mapperPseudo);
                }
            }
            //
            return mapperPseudo;
        }

        public BpelMapperPredicate processPredicate(Predicate predicate,
                XPathCastResolver parentResolver, BpelEntity varContextEntity) {
            BpelMapperPredicate xPathPredicate = BpelMapperPredicate.convert(
                    predicate, parentResolver, varContextEntity);
            //
            if (xPathPredicate == null) {
                return null;
            }
            //
            if (mContainer != null) {
                boolean useLeftTree = (predicate.getSource() != Source.TO);
                if (useLeftTree) {
                    mContainer.getFromPredicates().add(xPathPredicate);
                } else {
                    mContainer.getToPredicates().add(xPathPredicate);
                }
            }
            //
            return xPathPredicate;
        }

    }

    /**
     * It is an auxiliary container for building complex XPathCastResolver.
     * It collects all LocationStepModifiers in a form which acceptable
     * by XPath model.
     */
    public static class MapperLsmContainer extends XPathLsmContainer {

        private ArrayList<BpelMapperPredicate> mFromPredicates;
        private ArrayList<BpelMapperPredicate> mToPredicates;

        public MapperLsmContainer() {
            super();
            mFromPredicates = new ArrayList<BpelMapperPredicate>();
            mToPredicates = new ArrayList<BpelMapperPredicate>();
        }

        public List<BpelMapperPredicate> getFromPredicates() {
            return mFromPredicates;
        }

        public List<BpelMapperPredicate> getToPredicates() {
            return mToPredicates;
        }

        @Override
        public String toString() {
            return super.toString() +
                    " FromPredicate: " + mFromPredicates +
                    " ToPredicates: " + mToPredicates;
        }

    }

    /**
     * Special implementation of XPathLsmCollector.
     * It is intended to be used for @xsi:type generation.
     * It collects only type casts and only attached to To expressions.
     *
     * There is very important and specific behavior of the collector.
     * It looks for cases when a predicate is nested to a type cast. 
     * In such case the type cast is replaced because of it has to
     * be applied to the predicate.
     *
     */
    public static class XPathLsmXsiTypeCollector extends XPathLsmCollector {


        private LinkedList<XPathCast> mCastList;
        private boolean mCastIsLastProcessed;

        public XPathLsmXsiTypeCollector(BpelEntity varContextEntity) {
            super(new MapperLsmConvertor(), varContextEntity, false, true);
            mCastList = new LinkedList<XPathCast>();
        }

        public List<XPathCast> getCasts() {
            return mCastList;
        }

        @Override
        public void visit(Cast cast) {
            //
            Source source = cast.getSource();
            if (source != Source.TO) {
                return;
            }
            //
            XPathCastResolver parentResolver = mResolverStack.isEmpty() ?
                null : mResolverStack.peek();
            XPathCast xPathCast = mConverter.processCast(cast, parentResolver);
            if (xPathCast == null) {
                return;
            }
            //
            mCastIsLastProcessed = true;
            mCastList.addLast(xPathCast);
            //
            List<LocationStepModifier> childrenLsm = cast.getChildrenLsm(null);
            if (childrenLsm != null && !childrenLsm.isEmpty()) {
                CastWrapperResolver newResolver =
                        new CastWrapperResolver(xPathCast, parentResolver);
                mResolverStack.push(newResolver);
                try {
                    for (LocationStepModifier childLsm : childrenLsm) {
                        childLsm.accept(this);
                    }
                } finally {
                    mResolverStack.pop();
                }
            }
        }

        @Override
        public void visit(PseudoComp pseudoComp) {
            //
            Source source = pseudoComp.getSource();
            if (source != Source.TO) {
                return;
            }
            //
            mCastIsLastProcessed = false;
            //
            XPathCastResolver parentResolver = mResolverStack.isEmpty() ?
                null : mResolverStack.peek();
            XPathPseudoComp xPathPseudo =
                    mConverter.processPseudo(pseudoComp, parentResolver);
            if (xPathPseudo == null) {
                return;
            }
            //
            List<LocationStepModifier> childrenLsm = pseudoComp.getChildrenLsm(null);
            if (childrenLsm != null && !childrenLsm.isEmpty()) {
                PseudoWrapperResolver newResolver =
                        new PseudoWrapperResolver(xPathPseudo, parentResolver);
                mResolverStack.push(newResolver);
                try {
                    for (LocationStepModifier childLsm : childrenLsm) {
                        childLsm.accept(this);
                    }
                } finally {
                    mResolverStack.pop();
                }
            }
        }

        @Override
        public void visit(Predicate predicate) {
            Source source = predicate.getSource();
            if (source != Source.TO) {
                return;
            }
            //
            boolean castJustProcessed = mCastIsLastProcessed;
            //
            mCastIsLastProcessed = false;
            //
            XPathCastResolver parentResolver = mResolverStack.isEmpty() ?
                null : mResolverStack.peek();
            BpelMapperPredicate mapperPred =
                    ((MapperLsmConvertor)mConverter).processPredicate(
                    predicate, parentResolver, mVarContextEntity);
            //
            if (mapperPred != null && castJustProcessed) {
                XPathCast lastCast = mCastList.getLast();
                CastSchemaContext lastCastSContext = lastCast.getSchemaContext();
                PredicatedSchemaContext predSContext = mapperPred.getSchemaContext();
                //
                if (XPathSchemaContext.Utilities.equalsChain(
                        lastCastSContext, predSContext.getBaseContext())) {
                    //
                    // Replace last cast with a new one.
                    // After replacement it should look like the cast and
                    // the predicate swapped places.
                    mCastList.removeLast();
                    PredicatedSchemaContext newPredSC = new PredicatedSchemaContext(
                            lastCastSContext.getBaseContext(),
                            predSContext.getPredicateExpressions());
                    BpelMapperTypeCast newLastCast = new BpelMapperTypeCast(newPredSC, lastCast.getType());
                    mCastList.addLast(newLastCast);
                }
            }
            //
            List<LocationStepModifier> childrenLsm = predicate.getChildrenLsm(null);
            if (childrenLsm != null && !childrenLsm.isEmpty()) {
                for (LocationStepModifier childLsm : childrenLsm) {
                    childLsm.accept(this);
                }
            }
        }

    };

    //------------------------------------------------------------

    /**
     * Looks for a BPEL LSM by the Mapper's LSM inside the specified ExtensibleElements.
     * @param treeItem
     * @param extElement
     * @return
     */
    public static LocationStepModifier findBpelLsm(
            TreeItem treeItem, ExtensibleElements extElement, boolean inLeftTree) {
        //
        Editor editorExt = LsmProcessor.getEditorEntity(extElement, null, false);
        if (editorExt == null) {
            return null;
        }
        //
        DirectedList<Object> compPath = BpelPathConverter.singleton().
                constructObjectLocationList(treeItem, true, false);
        return findBpelLsm(editorExt, compPath, extElement, inLeftTree);
    }

    /**
     * Looks for a BPEL LSM by the Mapper's LSM inside the specified ExtensibleElements.
     * @param compPath
     * @param extElement
     * @return
     */
    public static LocationStepModifier findBpelLsm(Editor editorExt,
            DirectedList<Object> lsmCompPath, ExtensibleElements extElement,
            boolean inLeftTree) {
        //
        assert editorExt != null;
        //
        DirectedList<BpelMapperLsm> lsmList =
                XPathMapperUtils.extractLsms(lsmCompPath, BpelMapperLsm.class);
        //
        LsmContainer parent = editorExt;
        LocationStepModifier bpelLsm = null;
        Iterator<BpelMapperLsm> itr = lsmList.backwardIterator();
        while (itr.hasNext()) {
            BpelMapperLsm mapperLsm = itr.next();
            bpelLsm = findChildBpelLsm(parent, mapperLsm, inLeftTree);
            if (bpelLsm == null) {
                return null; // Not found
            }
            parent = bpelLsm;
        }
        //
        return bpelLsm;
    }

    /**
     * Looks for a LocationStepModifier under the specified parent container,
     * which corresponds to the spacified mapper LSM.
     * 
     * @param parent LSM Container
     * @param forLeftTree indicates if the LSM has to be from the left or right mapper's tree.
     * @param mapperLsm Mapper LSM
     * @return
     */
    public static LocationStepModifier findChildBpelLsm(LsmContainer parent,
            BpelMapperLsm mapperLsm, boolean inLeftTree) {
        //
        List<LocationStepModifier> lsmList = parent.getChildrenLsm(null);
        for (LocationStepModifier lsm : lsmList) {
            Source source = lsm.getSource();
            switch (source) {
                case FROM:
                    if (!inLeftTree) {
                        continue;
                    }
                    break;
                case TO:
                    if (inLeftTree) {
                        continue;
                    }
                    break;
                case INVALID:
                    continue;
            }
            if (mapperLsm.equalsIgnoreLocation(lsm)) {
                return lsm;
            }
        }
        //
        return null;
    }

    //------------------------------------------------------------

    /**
     * Registers an LSM in a BPEL variable area. It makes real changes to BPEL. 
     *
     * @param mapperTcContext
     * @param var
     * @param castedCompPath
     * @param newMapperLsm
     * @param forLeftTree
     * @return the newly created BPEL LSM
     */
    public static LocationStepModifier registerLsmToVariable(
            MapperTcContext mapperTcContext,
            final VariableDeclaration var, DirectedList<Object> castedCompPath,
            final BpelMapperLsm newMapperLsm,
            final boolean forLeftTree) throws ExtRegistrationException {
        //
        if (!(var instanceof ExtensibleElements)) {
            return null;
        }
        //
        final DirectedList<BpelMapperLsm> parentLsmList =
                XPathMapperUtils.extractLsms(castedCompPath, BpelMapperLsm.class);
        final BpelMapperLsmProcessor lsmProcessor =
                new BpelMapperLsmProcessor(mapperTcContext);
        //
        try {
            BpelModel bpelModel = var.getBpelModel();
            return bpelModel.invoke(new Callable<LocationStepModifier>() {
                public LocationStepModifier call() throws Exception {
                    try {
                        BpelModel bpelModel = var.getBpelModel();
                        BPELElementsBuilder builder = bpelModel.getBuilder();
                        //
                        Editor editor = LsmProcessor.getEditorEntity(
                                (ExtensibleElements)var, builder, true);
                        //
                        return lsmProcessor.registerMapperLsm(editor,
                                forLeftTree, parentLsmList, newMapperLsm);
                    } finally {
                        LsmProcessor.clearEmptyEditorEntity((ExtensibleElements)var);
                    }
                }
            }, mapperTcContext);
        } catch (ExtRegistrationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExtRegistrationException(ex);
        }
    }

    /**
     * Registers the new Mapper LSM under the specified Editor.
     * If some parent LSM is absent then it will be created in BPEL.
     *
     * It is implied the method is called inside of BPEL transaction
     *
     * @param editor
     * @param forLeftTree 
     * @param parentLsmList
     * @param newMapperLsm
     * @return .
     */
    public LocationStepModifier registerMapperLsm(Editor editor, boolean forLeftTree,
            DirectedList<BpelMapperLsm> parentLsmList,
            BpelMapperLsm newMapperLsm) throws ExtRegistrationException {
        //
        BpelModel bpelModel = editor.getBpelModel();
        BPELElementsBuilder builder = bpelModel.getBuilder();
        LsmContainer destination = editor;
        //
        // register parent LSM list if necessary
        Iterator<BpelMapperLsm> itr = parentLsmList.backwardIterator();
        while (itr.hasNext()) {
            BpelMapperLsm mapperLsm = itr.next();
            destination = registerMapperLsmImpl(builder, forLeftTree,
                    destination, mapperLsm);
        }
        //
        // register new LSM itself
        return registerMapperLsmImpl(builder, forLeftTree, destination, newMapperLsm);
    }

    private LocationStepModifier registerMapperLsmImpl(BPELElementsBuilder builder,
            boolean forLeftTree, LsmContainer destination,
            BpelMapperLsm newMapperLsm)
            throws ExtRegistrationException {
        //
        LocationStepModifier bpelLsm =
                findChildBpelLsm(destination, newMapperLsm, forLeftTree);
        if (bpelLsm != null) {
            return bpelLsm; // Nothing to register because it already exists
        }
        //
        LocationStepModifier result = null;
        //
        if (newMapperLsm instanceof BpelMapperTypeCast) {
            BpelCastManager castManager = getCastManager(forLeftTree);
            result = castManager.registerTypeCast(builder, destination,
                    (BpelMapperTypeCast)newMapperLsm);
        } else if (newMapperLsm instanceof BpelMapperPseudoComp) {
            BpelPseudoCompManager pseudoManager = getPseudoCompManager(forLeftTree);
            result = pseudoManager.registerPseudoComp(builder, destination,
                    (BpelMapperPseudoComp)newMapperLsm);
        } else if (newMapperLsm instanceof BpelMapperPredicate) {
            BpelPredicateManager predManager = getPredicateManager(forLeftTree);
            result = predManager.registerPredicate(builder, destination,
                    (BpelMapperPredicate)newMapperLsm);
        }
        //
        return result;
    }

    /**
     * Write to the BPEL a tree of LSMs.
     * It is implied the method is executed in a BPEL transaction.
     *
     * @param owner
     * @param lsmTree
     * @throws org.netbeans.modules.bpel.mapper.cast.ExtRegistrationException
     */
    public void registerAll(ExtensibleElements owner, MapperLsmTree lsmTree,
            boolean inLeftTree) throws ExtRegistrationException {
        //
        if (lsmTree.getRootNodes().isEmpty()) {
            return;
        }
        //
        BPELElementsBuilder builder = owner.getBpelModel().getBuilder();
        //
        Editor editorExt = LsmProcessor.getEditorEntity(owner, builder, true);
        //
        for (TreeNode rootNode : lsmTree.getRootNodes()) {
            BpelMapperLsm newMapperLsm = BpelMapperLsm.class.cast(rootNode.getLsm());
            LocationStepModifier rootBpelLsm = registerMapperLsmImpl(
                    builder, inLeftTree, editorExt, newMapperLsm);
            //
            registerChildren(rootNode, rootBpelLsm, inLeftTree, builder);
        }
    }

    /**
     * Recursive!
     * @param parentNode
     * @param parentBpelLsm
     * @param inLeftTree
     */
    private void registerChildren(TreeNode parentNode,
            LocationStepModifier parentBpelLsm, boolean inLeftTree,
            BPELElementsBuilder builder) throws ExtRegistrationException {
        //
        if (parentNode == null || parentBpelLsm == null) {
            return;
        }
        //
        List<TreeNode> children = parentNode.getChildNodes();
        for (TreeNode childNode : children) {
            BpelMapperLsm chldLsm = BpelMapperLsm.class.cast(childNode.getLsm());
            LocationStepModifier newBpelLsm = registerMapperLsmImpl(
                    builder, inLeftTree, parentBpelLsm, chldLsm);
            //
            registerChildren(childNode, newBpelLsm, inLeftTree, builder);
        }
    }

    //--------------------------------------------------------------

    public void deleteLsm(MapperTcContext mapperTcContext,
            final TreeItem treeItem,
            final BpelMapperLsm lsmToDelete,
            final boolean inLeftTree) {
        final VariableDeclaration var = lsmToDelete.getBaseBpelVariable();
        if (var != null) {
            //
            // TODO: check if the deleted cast is used somewhere and ask
            // user's confirmation if so.
            //
            try {
                BpelModel bpelModel = ((BpelEntity)var).getBpelModel();
                bpelModel.invoke(new Callable<Object>() {
                    public Object call() throws Exception {
                        deleteLsmImpl((ExtensibleElements)var, 
                                treeItem, lsmToDelete, inLeftTree);
                        return null;
                    }
                }, mapperTcContext);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }

    /**
     * Deletes the LSM entity from the BPEL sources at the specified
     * destination.
     *
     * @param destination
     * @param castToUnreg
     */
    private boolean deleteLsmImpl(ExtensibleElements destination,
            TreeItem treeItem, BpelMapperLsm lsmToDelete,
            boolean inLeftTree) {
        //
        LocationStepModifier bpelLsm = findBpelLsm(treeItem, destination, inLeftTree);
        if (bpelLsm == null) {
            return false; // Nothing found so nothing to unregister
        }
        //
        BpelContainer parent = bpelLsm.getParent();
        parent.remove(bpelLsm);
        //
        if (lsmToDelete instanceof BpelMapperTypeCast) {
            CastManager castManager = getCastManager(inLeftTree);
            castManager.removeTypeCast((BpelMapperTypeCast)lsmToDelete);
        } else if (lsmToDelete instanceof BpelMapperPseudoComp) {
            BpelPseudoCompManager pseudoManager = getPseudoCompManager(inLeftTree);
            pseudoManager.removePseudoComp((BpelMapperPseudoComp)lsmToDelete);
        } else if (lsmToDelete instanceof BpelMapperPredicate) {
            BpelPredicateManager predManager = getPredicateManager(inLeftTree);
            predManager.removePredicate((BpelMapperPredicate)lsmToDelete);
        }
        //
        return true;
    }


}
