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

package org.netbeans.modules.bpel.mapper.tree.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.bpel.mapper.cast.AbstractPseudoComp;
import org.netbeans.modules.bpel.mapper.predicates.PredicateManager;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.predicates.AbstractPredicate;
import org.netbeans.modules.bpel.mapper.predicates.SpecialStepManager;
import org.netbeans.modules.bpel.mapper.cast.AbstractTypeCast;
import org.netbeans.modules.bpel.mapper.cast.CastManager;
import org.netbeans.modules.bpel.mapper.cast.PseudoCompManager;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.soa.ui.tree.SoaTreeExtensionModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeStructureProvider;
import org.netbeans.modules.xml.xpath.ext.schema.FindAllChildrenSchemaVisitor;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext.SchemaCompPair;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;

/**
 * The implementation of the MapperTreeModel for the variables' tree.
 *
 * @author nk160297
 */
public class VariableTreeModel implements SoaTreeExtensionModel, 
        TreeStructureProvider, MapperConnectabilityProvider {

    private FindAllChildrenSchemaVisitor sSchemaSearcher = 
            new FindAllChildrenSchemaVisitor(true, true, true);
    
    private Set<VariableDeclaration> mOverriddenVariables;
    private BpelDesignContext mDesignContext;
    private PredicateManager mPredManager;
    private SpecialStepManager mSStepManager;
    private CastManager mCastManager;
    private PseudoCompManager mPseudoCompManager;
    private VariableTreeInfoProvider mTreeInfoProvider;
    
    public VariableTreeModel(BpelDesignContext context, boolean leftTree, 
            Object synchSource) {
        this(context, new PredicateManager(), new SpecialStepManager(), 
                new CastManager(leftTree, synchSource), 
                new PseudoCompManager(leftTree, synchSource));
    }
    
    public VariableTreeModel(BpelDesignContext context, 
            PredicateManager predManager, SpecialStepManager stepManager, 
            CastManager castManager, PseudoCompManager pseudoCompManager) {
        this(context, predManager, stepManager, castManager, pseudoCompManager, 
                VariableTreeInfoProvider.getInstance());
    }
    
    public VariableTreeModel(BpelDesignContext context, 
            PredicateManager predManager, SpecialStepManager stepManager, 
            CastManager castManager, PseudoCompManager pseudoCompManager, 
            VariableTreeInfoProvider treeInfoProvider) {
        mDesignContext = context;
        mPredManager = predManager;
        mSStepManager = stepManager;
        mCastManager = castManager;
        mPseudoCompManager = pseudoCompManager;
        mTreeInfoProvider = treeInfoProvider;
        //
        mOverriddenVariables = mDesignContext.getVisibilityScope().
                getVisibleVariables().getAllOverridenVariables();
    }
    
    public List<Object> getChildren(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (dataObj == SoaTreeModel.TREE_ROOT) {
            Process process = mDesignContext.getBpelModel().getProcess();
            return Collections.singletonList((Object)process);
        }
        if (dataObj instanceof VariableDeclarationScope) {
            //
            // Look for the parent in the scope chain.
            List<VariableDeclarationScope> scopeChain = 
                    mDesignContext.getVisibilityScope().getVarScopeChain();
            int parentIndex = -1;
            for (int index = 0; index < scopeChain.size(); index++) {
                VariableDeclarationScope scopeFromChain = scopeChain.get(index);
                if (scopeFromChain == dataObj) {
                    parentIndex = index;
                    break;
                }
            }
            //
            if (parentIndex == -1) {
                // The parent is not in any scope at all :-(
                // it should not aver happened
                return null;
            }
            //
            VariableDeclarationScope nextScope = null;
            if (parentIndex + 1 < scopeChain.size()) {
                // Take the next scope if the parent scope is not the last in the chain
                nextScope = scopeChain.get(parentIndex + 1);
            }
            //
            List<VariableDeclaration> varList = 
                    VisibilityScope.Utils.getVarDeclInScope(
                    (VariableDeclarationScope)dataObj);
            varList.removeAll(mOverriddenVariables);
            //
            List<Object> childrenList = new ArrayList<Object>();
            for (VariableDeclaration childVar : varList) {
                AbstractVariableDeclaration var = null;
                if (childVar == dataObj) {
                    VariableDeclarationWrapper wrapper = 
                            new VariableDeclarationWrapper(childVar);
                    var = wrapper;
                } else {
                    var = childVar;
                }
                //
                if (var != null) {
                    childrenList.add(var);
                    //
                    // Add casted variables
                    if (mCastManager != null) {
                        // Look for the corresponding cast nodes.
                        List<AbstractTypeCast> typeCast = 
                                mCastManager.getCastedVariables(var, null);
                        childrenList.addAll(typeCast);
                    }
                }
            }
            //
            if (nextScope != null) {
                childrenList.add(nextScope);
            }
            //
            return childrenList;
        } else if (dataObj instanceof AbstractVariableDeclaration) {
            AbstractVariableDeclaration varDecl = (AbstractVariableDeclaration)dataObj;
            WSDLReference<Message> msgRef = varDecl.getMessageType();
            if (msgRef != null) {
                Message msg = msgRef.get();
                if (msg != null) {
                    Collection<Part> parts = msg.getParts();
                    if (parts != null && !parts.isEmpty()) {
                        if (mCastManager == null) {
                            List<Object> partList = parts != null ? 
                                new ArrayList<Object>(parts) : Collections.EMPTY_LIST;
                            return partList;
                        } else {
                            List<Object> childrenList = new ArrayList<Object>();
                            childrenList.addAll(parts);
                            //
                            // Add casted parts
                            // Look for the corresponding cast nodes.
                            for (Part part : parts) {
                                List<AbstractTypeCast> typeCast = 
                                        mCastManager.getCastedVariables(varDecl, part);
                                childrenList.addAll(typeCast);
                            }
                            //
                            return childrenList;
                        }
                    }
                }
            } else {
                SchemaReference<GlobalType> gTypeRef = varDecl.getType();
                if (gTypeRef != null) {
                    GlobalType gType = gTypeRef.get();
                    if (gType != null) {
                        return loadSchemaComponents(treeItem, gType);
                    }
                } else {
                    SchemaReference<GlobalElement> gElemRef = varDecl.getElement();
                    if (gElemRef != null) {
                        GlobalElement gElem = gElemRef.get();
                        if (gElem != null) {
                            return loadSchemaComponents(treeItem, gElem);
                        }
                    }
                }
            }
        } else if (dataObj instanceof Part) {
            Part part = (Part)dataObj;
            NamedComponentReference<GlobalType> gTypeRef = part.getType();
            if (gTypeRef != null) {
                GlobalType gType = gTypeRef.get();
                if (gType != null) {
                    return loadSchemaComponents(treeItem, gType);
                }
            } else {
                NamedComponentReference<GlobalElement> gElemRef = part.getElement();
                if (gElemRef != null) {
                    GlobalElement gElem = gElemRef.get();
                    if (gElem != null) {
                        return loadSchemaComponents(treeItem, gElem);
                    }
                }
            }
        } else if (dataObj instanceof SchemaComponent) {
            return loadSchemaComponents(treeItem, (SchemaComponent)dataObj);
        } else if (dataObj instanceof AbstractPredicate) {
            SchemaCompHolder sCompHolder = ((AbstractPredicate)dataObj).getSCompHolder();
            if (sCompHolder != null) {
                SchemaComponent sComp = sCompHolder.getSchemaComponent();
                return loadSchemaComponents(treeItem, sComp);
            }
        } else if (dataObj instanceof LocationStep) {
            LocationStep step = (LocationStep)dataObj;
            XPathSchemaContext context = step.getSchemaContext();
            if (context != null) {
                Set<SchemaCompPair> sCompPairs = context.getSchemaCompPairs();
                ArrayList result = new ArrayList();
                for (SchemaCompPair sCompPair : sCompPairs) {
                    SchemaCompHolder sCompHolder = sCompPair.getCompHolder();
                    SchemaComponent sComp = sCompHolder.getSchemaComponent();
                    result.addAll(loadSchemaComponents(treeItem, sComp));
                }
                return result;
            }
        } else if (dataObj instanceof AbstractTypeCast) {
            GlobalType type = ((AbstractTypeCast)dataObj).getType();
            if (type != null) {
                return loadSchemaComponents(treeItem, type);
            }
        } else if (dataObj instanceof AbstractPseudoComp) {
            GlobalType type = ((AbstractPseudoComp)dataObj).getType();
            if (type != null) {
                return loadSchemaComponents(treeItem, type);
            }
        }
        //
        return null;
    }

    public PredicateManager getPredicateManager() {
        return mPredManager;
    }
    
    public SpecialStepManager getSStepManager() {
        return mSStepManager;
    }
    
    public CastManager getCastManager() {
        return mCastManager;
    }

    public PseudoCompManager getPseudoCompManager() {
        return mPseudoCompManager;
    }

    private List loadSchemaComponents(TreeItem treeItem, SchemaComponent parent) {
        //
        sSchemaSearcher.lookForSubcomponents(parent);
        List<SchemaComponent> childrenComp = sSchemaSearcher.getFound();
        //
        if (mPredManager == null && mSStepManager == null && mCastManager == null) {
            return childrenComp;
        }
        //
        List<Object> allChildren = new ArrayList<Object>(childrenComp.size() + 5);
        if (mSStepManager != null) {
            //
            // Look for the corresponding special nodes (text(), node(), comment()).
            List<LocationStep> step = mSStepManager.getSteps(treeItem);
            if (step != null && !step.isEmpty()) {
                allChildren.addAll(step);
            }
        }
        //
        for (SchemaComponent sComp : childrenComp) {
            allChildren.add(sComp);
            //
            if (mPredManager != null) {
                // Look for the corresponding predicated nodes.
                List<AbstractPredicate> predicates = 
                        mPredManager.getPredicates(treeItem, sComp);
                allChildren.addAll(predicates);
            }
            //
            if (mCastManager != null) {
                // Look for the corresponding cast nodes.
                List<AbstractTypeCast> typeCast = 
                        mCastManager.getTypeCast(treeItem, sComp);
                allChildren.addAll(typeCast);
            }
        }
        //
        // Add pseudo components to the end of list.
        // It's not clear how to correlate a pseudo element with 
        // the corresponding xsd:any. So the pseudo components are at the end of 
        // children list.
        if (mPseudoCompManager != null) {
            // Look for the corresponding cast nodes.
            List<XPathPseudoComp> pseudoCompList = 
                    mPseudoCompManager.getPseudoComp(treeItem);
            for (XPathPseudoComp pseudoComp : pseudoCompList) {
                allChildren.add(pseudoComp);
                //
                if (mPredManager != null) {
                    // Look for the corresponding predicated nodes.
                    List<AbstractPredicate> predicates = mPredManager.
                            getPredicates(treeItem, pseudoComp);
                    allChildren.addAll(predicates);
                }
            }
        }
        //
        return allChildren;
    }
    
    public Boolean isLeaf(TreeItem treeItem) {
        return null;
    }

    public Boolean isConnectable(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (dataObj instanceof Element || 
                dataObj instanceof Attribute ||
                dataObj instanceof Part || 
                dataObj instanceof AbstractPredicate ||
                dataObj instanceof LocationStep || 
                dataObj instanceof AbstractTypeCast || 
                dataObj instanceof AbstractPseudoComp) {
            return Boolean.TRUE;
        }
        //
        if (dataObj instanceof AbstractVariableDeclaration && 
                !(dataObj instanceof VariableDeclarationScope)) {
            return Boolean.TRUE;
        }
        //
        return null;
    }

    public TreeItemInfoProvider getTreeItemInfoProvider() {
        return mTreeInfoProvider;
    }

    public TreeStructureProvider getTreeStructureProvider() {
        return this;
    }
    
    public TreeItemActionsProvider getTreeItemActionsProvider() {
        return mTreeInfoProvider;
    }

}
