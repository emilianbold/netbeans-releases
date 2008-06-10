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
import org.netbeans.modules.bpel.mapper.predicates.PredicateManager;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.predicates.AbstractPredicate;
import org.netbeans.modules.bpel.mapper.predicates.SpecialStepManager;
import org.netbeans.modules.bpel.mapper.cast.AbstractTypeCast;
import org.netbeans.modules.bpel.mapper.cast.CastManager;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeExtensionModel;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemInfoProvider;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.RestartableIterator;
import org.netbeans.modules.bpel.model.api.Process;
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
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext.SchemaCompPair;

/**
 * The implementation of the MapperTreeModel for the variables' tree.
 *
 * @author nk160297
 */
public class VariableTreeModel implements MapperTreeExtensionModel<Object> {

    private FindAllChildrenSchemaVisitor sSchemaSearcher = 
            new FindAllChildrenSchemaVisitor(true, true, true);
    
    private Set<VariableDeclaration> mOverriddenVariables;
    private BpelDesignContext mDesignContext;
    private PredicateManager mPredManager;
    private SpecialStepManager mSStepManager;
    private CastManager mCastManager;
    private TreeItemInfoProvider mTreeInfoProvider;
    
    public VariableTreeModel(BpelDesignContext context, boolean leftTree, 
            Object synchSource) {
        this(context, new PredicateManager(), new SpecialStepManager(), 
                new CastManager(leftTree, synchSource));
    }
    
    public VariableTreeModel(BpelDesignContext context, 
            PredicateManager predManager, SpecialStepManager stepManager, 
            CastManager castManager) {
        this(context, predManager, stepManager, castManager, 
                VariableTreeInfoProvider.getInstance());
    }
    
    public VariableTreeModel(BpelDesignContext context, 
            PredicateManager predManager, SpecialStepManager stepManager, 
            CastManager castManager, TreeItemInfoProvider treeInfoProvider) {
        mDesignContext = context;
        mPredManager = predManager;
        mSStepManager = stepManager;
        mCastManager = castManager;
        mTreeInfoProvider = treeInfoProvider;
        //
        mOverriddenVariables = mDesignContext.getVisibilityScope().
                getVisibleVariables().getAllOverridenVariables();
    }
    
    
    
    public List getChildren(RestartableIterator<Object> dataObjectPathItr) {
        Object parent = dataObjectPathItr.next();
        if (parent == MapperTreeModel.TREE_ROOT) {
            Process process = mDesignContext.getBpelModel().getProcess();
            return Collections.singletonList(process);
        }
        if (parent instanceof VariableDeclarationScope) {
            //
            // Look for the parent in the scope chain.
            List<VariableDeclarationScope> scopeChain = 
                    mDesignContext.getVisibilityScope().getVarScopeChain();
            int parentIndex = -1;
            for (int index = 0; index < scopeChain.size(); index++) {
                VariableDeclarationScope scopeFromChain = scopeChain.get(index);
                if (scopeFromChain == parent) {
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
                    (VariableDeclarationScope)parent);
            varList.removeAll(mOverriddenVariables);
            //
            List<Object> childrenList = new ArrayList<Object>();
            for (VariableDeclaration childVar : varList) {
                AbstractVariableDeclaration var = null;
                if (childVar == parent) {
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
        } else if (parent instanceof AbstractVariableDeclaration) {
            AbstractVariableDeclaration varDecl = (AbstractVariableDeclaration)parent;
            WSDLReference<Message> msgRef = varDecl.getMessageType();
            if (msgRef != null) {
                Message msg = msgRef.get();
                if (msg != null) {
                    Collection<Part> parts = msg.getParts();
                    if (parts != null && !parts.isEmpty()) {
                        if (mCastManager == null) {
                            ArrayList<Part> partList = parts != null ? 
                                new ArrayList<Part>(parts) : new ArrayList<Part>();
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
                        return loadSchemaComponents(dataObjectPathItr, gType);
                    }
                } else {
                    SchemaReference<GlobalElement> gElemRef = varDecl.getElement();
                    if (gElemRef != null) {
                        GlobalElement gElem = gElemRef.get();
                        if (gElem != null) {
                            return loadSchemaComponents(dataObjectPathItr, gElem);
                        }
                    }
                }
            }
        } else if (parent instanceof Part) {
            Part part = (Part)parent;
            NamedComponentReference<GlobalType> gTypeRef = part.getType();
            if (gTypeRef != null) {
                GlobalType gType = gTypeRef.get();
                if (gType != null) {
                    return loadSchemaComponents(dataObjectPathItr, gType);
                }
            } else {
                NamedComponentReference<GlobalElement> gElemRef = part.getElement();
                if (gElemRef != null) {
                    GlobalElement gElem = gElemRef.get();
                    if (gElem != null) {
                        return loadSchemaComponents(dataObjectPathItr, gElem);
                    }
                }
            }
        } else if (parent instanceof SchemaComponent) {
            return loadSchemaComponents(dataObjectPathItr, (SchemaComponent)parent);
        } else if (parent instanceof AbstractPredicate) {
            SchemaComponent sComp = ((AbstractPredicate)parent).getSComponent();
            if (sComp != null) {
                return loadSchemaComponents(dataObjectPathItr, sComp);
            }
        } else if (parent instanceof LocationStep) {
            LocationStep step = (LocationStep)parent;
            XPathSchemaContext context = step.getSchemaContext();
            if (context != null) {
                Set<SchemaCompPair> sCompPairs = context.getSchemaCompPairs();
                ArrayList result = new ArrayList();
                for (SchemaCompPair sCompPair : sCompPairs) {
                    SchemaComponent sComp = sCompPair.getComp();
                    result.addAll(loadSchemaComponents(dataObjectPathItr, sComp));
                }
                return result;
            }
        } else if (parent instanceof AbstractTypeCast) {
            GlobalType gType = ((AbstractTypeCast)parent).getCastTo();
            if (gType != null) {
                return loadSchemaComponents(dataObjectPathItr, gType);
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
    
    private List loadSchemaComponents(
            RestartableIterator<Object> dataObjectPathItr, 
            SchemaComponent parent) {
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
            List<LocationStep> step = 
                    mSStepManager.getSteps(dataObjectPathItr);
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
                        mPredManager.getPredicates(dataObjectPathItr, sComp);
                allChildren.addAll(predicates);
            }
            //
            if (mCastManager != null) {
                // Look for the corresponding cast nodes.
                List<AbstractTypeCast> typeCast = 
                        mCastManager.getTypeCast(dataObjectPathItr, sComp);
                allChildren.addAll(typeCast);
            }
        }
        //
        return allChildren;
    }
    
    public Boolean isLeaf(Object node) {
        return null;
    }

    public Boolean isConnectable(Object node) {
        if (node instanceof Element || 
                node instanceof Attribute ||
                node instanceof Part || 
                node instanceof AbstractPredicate ||
                node instanceof LocationStep || 
                node instanceof AbstractTypeCast) {
            return Boolean.TRUE;
        }
        //
        if (node instanceof AbstractVariableDeclaration && 
                !(node instanceof VariableDeclarationScope)) {
            return Boolean.TRUE;
        }
        //
        return null;
    }

    public TreeItemInfoProvider getTreeItemInfoProvider() {
        return mTreeInfoProvider;
    }

}
