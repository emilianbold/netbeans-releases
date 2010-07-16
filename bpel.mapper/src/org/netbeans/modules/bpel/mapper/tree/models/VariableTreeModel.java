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
import org.netbeans.modules.bpel.mapper.cast.BpelCastManager;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperPseudoComp;
import org.netbeans.modules.bpel.mapper.predicates.BpelPredicateManager;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.predicates.BpelMapperPredicate;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperTypeCast;
import org.netbeans.modules.bpel.mapper.cast.BpelPseudoCompManager;
import org.netbeans.modules.bpel.mapper.model.BpelExtManagerHolder;
import org.netbeans.modules.bpel.mapper.properties.PropertiesNode;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.ext.editor.api.NMProperty;
import org.netbeans.modules.soa.ui.tree.SoaTreeExtensionModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeStructureProvider;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperPredicate;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperTypeCast;
import org.netbeans.modules.soa.xpath.mapper.specstep.SpecialStepManager;
import org.netbeans.modules.xml.xpath.ext.schema.FindAllChildrenSchemaVisitor;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext.SchemaCompPair;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStep;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.soa.xpath.mapper.tree.models.MapperConnectabilityProvider;

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

    private BpelExtManagerHolder mBxmh;
    private VariableTreeInfoProvider mTreeInfoProvider;
    private boolean leftTreeFlag = true;
    
    public VariableTreeModel(BpelDesignContext context, 
            BpelExtManagerHolder bxmh, boolean leftTree) {
        this(context, bxmh, VariableTreeInfoProvider.getInstance(), leftTree);
    }
    
    public VariableTreeModel(BpelDesignContext context, 
            BpelExtManagerHolder bxmh,
            VariableTreeInfoProvider treeInfoProvider, boolean leftTree) {
        mDesignContext = context;
        mBxmh = bxmh;
        mTreeInfoProvider = treeInfoProvider;
        leftTreeFlag = leftTree;
        //
        BpelPredicateManager predManager = mBxmh.getPredicateManager();
        if (predManager != null) {
            BpelCastManager bcm = mBxmh.getCastManager();
            if (bcm != null) {
                predManager.addListener(bcm);
            }
            BpelPseudoCompManager pcm = mBxmh.getPseudoCompManager();
            if (pcm != null) {
                predManager.addListener(pcm);
            }
        }
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
                    BpelCastManager castManager = mBxmh.getCastManager();
                    if (castManager != null) {
                        // Look for the corresponding cast nodes.
                        List<MapperTypeCast> typeCast =
                                getCastManager().getCastedVariables(var, null);
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
            List<Object> result = null;
            AbstractVariableDeclaration varDecl = (AbstractVariableDeclaration)dataObj;
            WSDLReference<Message> msgRef = varDecl.getMessageType();
            if (msgRef != null) {
                Message msg = msgRef.get();
                if (msg != null) {
                    Collection<Part> parts = msg.getParts();
                    if (parts != null && !parts.isEmpty()) {
                        if (getCastManager() == null) {
                            result = (parts != null) 
                                    ? new ArrayList<Object>(parts) 
                                    : Collections.EMPTY_LIST;
                        } else {
                            result = new ArrayList<Object>();
                            result.addAll(parts);
                            //
                            // Add casted parts
                            // Look for the corresponding cast nodes.
                            for (Part part : parts) {
                                List<MapperTypeCast> typeCast =
                                        getCastManager().getCastedVariables(varDecl, part);
                                result.addAll(typeCast);
                            }
                        }
                    }
                }
            } else {
                SchemaReference<GlobalType> gTypeRef = varDecl.getType();
                if (gTypeRef != null) {
                    GlobalType gType = gTypeRef.get();
                    if (gType != null) {
                        result = loadSchemaComponents(treeItem, gType);
                    }
                } else {
                    SchemaReference<GlobalElement> gElemRef = varDecl.getElement();
                    if (gElemRef != null) {
                        GlobalElement gElem = gElemRef.get();
                        if (gElem != null) {
                            result = loadSchemaComponents(treeItem, gElem);
                        }
                    }
                }
            }
            if (msgRef != null) {
                if (result == null) {
                    result = new ArrayList<Object>();
                }
                result.add(new PropertiesNode(varDecl, leftTreeFlag));
            }
            
            return result;
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
        } else if (dataObj instanceof BpelMapperPredicate) {
            SchemaCompHolder sCompHolder = ((BpelMapperPredicate)dataObj).getSCompHolder(false);
            if (sCompHolder != null) {
                SchemaComponent sComp = sCompHolder.getSchemaComponent();
                return loadSchemaComponents(treeItem, sComp);
            }
        } else if (dataObj instanceof XPathSpecialStep) {
            XPathSpecialStep step = (XPathSpecialStep)dataObj;
            XPathSchemaContext context = step.getSchemaContext();
            if (context != null) {
                Set<SchemaCompPair> sCompPairs = context.getSchemaCompPairs();
                ArrayList<Object> result = new ArrayList<Object>();
                for (SchemaCompPair sCompPair : sCompPairs) {
                    SchemaCompHolder sCompHolder = sCompPair.getCompHolder();
                    SchemaComponent sComp = sCompHolder.getSchemaComponent();
                    result.addAll(loadSchemaComponents(treeItem, sComp));
                }
                return result;
            }
        } else if (dataObj instanceof BpelMapperTypeCast) {
            GlobalType type = ((BpelMapperTypeCast)dataObj).getType();
            if (type != null) {
                return loadSchemaComponents(treeItem, type);
            }
        } else if (dataObj instanceof BpelMapperPseudoComp) {
            GlobalType type = ((BpelMapperPseudoComp)dataObj).getType();
            if (type != null) {
                return loadSchemaComponents(treeItem, type);
            }
        } else if (dataObj instanceof PropertiesNode) {
            return ((PropertiesNode) dataObj).getChildren(mDesignContext);
        } else if (dataObj instanceof FileObject) {
            TreeItem i = treeItem.getParent();
            while (i != null) {
                Object data = i.getDataObject();
                if (data instanceof PropertiesNode) {
                    return ((PropertiesNode) data).getChildren(mDesignContext, 
                            dataObj);
                }
                i = i.getParent();
            }
//            
//            return PropertiesUtils.loadChildren((FileObject) dataObj);
        }
        
        
        //
        return null;
    }

    public BpelPredicateManager getPredicateManager() {
        return mBxmh.getPredicateManager();
    }
    
    public SpecialStepManager getSStepManager() {
        return mBxmh.getSpecialStepManager();
    }
    
    public BpelCastManager getCastManager() {
        return mBxmh.getCastManager();
    }

    public BpelPseudoCompManager getPseudoCompManager() {
        return mBxmh.getPseudoCompManager();
    }

    private List loadSchemaComponents(TreeItem treeItem, SchemaComponent parent) {
        //
        sSchemaSearcher.lookForSubcomponents(parent);
        List<SchemaComponent> childrenComp = sSchemaSearcher.getFound();
        //
        if (mBxmh == null) {
            return childrenComp;
        }
        //
        List<Object> allChildren = new ArrayList<Object>(childrenComp.size() + 5);
        SpecialStepManager sStepManager = mBxmh.getSpecialStepManager();
        if (sStepManager != null) {
            //
            // Look for the corresponding special nodes (text(), node(), comment()).
            List<XPathSpecialStep> step = sStepManager.getSteps(treeItem);
            if (step != null && !step.isEmpty()) {
                allChildren.addAll(step);
            }
        }
        //
        for (SchemaComponent sComp : childrenComp) {
            allChildren.add(sComp);
            //
            BpelPredicateManager predManager = mBxmh.getPredicateManager();
            if (predManager != null) {
                // Look for the corresponding predicated nodes.
                List<MapperPredicate> predicates =
                        predManager.getPredicates(treeItem, sComp);
                allChildren.addAll(predicates);
            }
            //
            BpelCastManager castManager = mBxmh.getCastManager();
            if (castManager != null) {
                // Look for the corresponding cast nodes.
                List<MapperTypeCast> typeCast =
                        castManager.getTypeCast(treeItem, sComp);
                allChildren.addAll(typeCast);
            }
        }
        //
        // Add pseudo components to the end of list.
        // It's not clear how to correlate a pseudo element with 
        // the corresponding xsd:any. So the pseudo components are at the end of 
        // children list.
        BpelPseudoCompManager pseudoCompManager = mBxmh.getPseudoCompManager();
        if (pseudoCompManager != null) {
            // Look for the corresponding cast nodes.
            List<XPathPseudoComp> pseudoCompList = 
                    pseudoCompManager.getPseudoComp(treeItem);
            for (XPathPseudoComp pseudoComp : pseudoCompList) {
                allChildren.add(pseudoComp);
                //
                BpelPredicateManager predManager = mBxmh.getPredicateManager();
                if (predManager != null) {
                    // Look for the corresponding predicated nodes.
                    List<MapperPredicate> predicates = predManager.
                            getPredicates(treeItem, pseudoComp);
                    allChildren.addAll(predicates);
                }
            }
        }
        //
        return allChildren;
    }
    
    public Boolean isLeaf(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (dataObj instanceof PropertiesNode) {
            return Boolean.FALSE;
        }
        
        if (dataObj instanceof FileObject) {
            return Boolean.valueOf(!((FileObject) dataObj).isFolder());
        }
        
        return null;
    }

    public Boolean isConnectable(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (dataObj instanceof Element || 
                dataObj instanceof Attribute ||
                dataObj instanceof Part || 
                dataObj instanceof BpelMapperPredicate ||
                dataObj instanceof XPathSpecialStep ||
                dataObj instanceof BpelMapperTypeCast ||
                dataObj instanceof BpelMapperPseudoComp ||
                dataObj instanceof CorrelationProperty) {
            return Boolean.TRUE;
        }
        
        if (dataObj instanceof FileObject &&
                !((FileObject) dataObj).isFolder())
        {
            return Boolean.TRUE;
        }
        
        if (dataObj instanceof NMProperty) {
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
