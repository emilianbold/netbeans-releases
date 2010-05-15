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
package org.netbeans.modules.bpel.nodes.children;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.editors.api.Constants.VariableStereotype;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.nodes.ReloadableChildren;
import org.netbeans.modules.bpel.nodes.VariableNode;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VariableTypeFilter;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VariableTypeInfoProvider;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Supports the list of Variables and nested VariableDeclarationScope
 * of another VariableDeclarationScope.
 * It's used by the Variable Chooser.
 *
 * @author nk160297
 */
public class VariableDeclarationScopesChildren extends Children.Keys
        implements ReloadableChildren {
    
    protected Lookup myLookup;
    private VariableDeclarationScope myKey;
    private VisibilityScope varVisScope;
    
    public VariableDeclarationScopesChildren(
            VariableDeclarationScope vdScope, Lookup lookup) {
        myLookup = lookup;
        myKey = vdScope;
        //
        // Try obtaining a variable visibility scope from the lookup
        varVisScope = (VisibilityScope)myLookup.lookup(VisibilityScope.class);
        //
        setKeys(new Object[] {vdScope});
    }
    
    protected void addScopeNodes(
            VariableDeclarationScope parentVdScope, List<Node> nodesList) {
        //
        NodeFactory nodeFactory = (NodeFactory)myLookup.lookup(NodeFactory.class);
        // BpelModel bpelModel = (BpelModel)myLookup.lookup(BpelModel.class);
        //
        // Add nested scopes
        if (varVisScope == null) {
            // The Visibility Scope isn't specified. So show all children.
            List<VariableDeclarationScope> vdScopList =
                    VisibilityScope.Utils.getNestedVarScopes(parentVdScope);
            //
            for (VariableDeclarationScope vdScope : vdScopList) {
                Node newNode = createVdScopeNode(vdScope, nodeFactory);
                nodesList.add(newNode);
            }
        } else {
            // The Visibility Scope is specified.
            // So take next scope element from the chain.
            List<VariableDeclarationScope> scopeChain = varVisScope.getVarScopeChain();
            VariableDeclarationScope subsequentScope = null;
            Iterator<VariableDeclarationScope> itr = scopeChain.iterator();
            while (itr.hasNext()) {
                VariableDeclarationScope aScope = itr.next();
                if (aScope.equals(parentVdScope)) {
                    // At the point the current VariableDeclarationScope element
                    // has found in the chain.
                    // The next item in the chain is the sought scope according to
                    // sorting order described at the VariableVisibilityScope class.
                    if (itr.hasNext()) {
                        subsequentScope = itr.next();
                    }
                }
            }
            //
            if (subsequentScope != null) {
                Node newNode = createVdScopeNode(subsequentScope, nodeFactory);
                nodesList.add(newNode);
            }
        }
    }
    
    protected Node[] createNodes(Object key) {
        if (!(key instanceof VariableDeclarationScope)) {
            return null;
        }
        VariableDeclarationScope vdScope = (VariableDeclarationScope)key;
        //
        NodeFactory nodeFactory = (NodeFactory)myLookup.lookup(NodeFactory.class);
        //
        List<Node> nodesList = new ArrayList<Node>();
        //
        // Add Variables
        VariableDeclaration[] varDeclArr = getDeclaredVariables(vdScope);
        for (VariableDeclaration varDecl : varDeclArr) {
            Node newNode = nodeFactory.createNode(
                    NodeType.VARIABLE, varDecl, myLookup);
            nodesList.add(newNode);
        }
        //
        // Add Variable Declaration Scopes
        addScopeNodes(vdScope, nodesList);
        //
        Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
        return nodes;
    }
    
    public void reload() {
        setKeys(new Object[] {new Object()});
        setKeys(new Object[] {myKey});
    }
    
    private Node createVdScopeNode(
            VariableDeclarationScope vdScope, NodeFactory nodeFactory) {
        //
        NodeType bpelNodeType = EditorUtil.getBasicNodeType(vdScope);
        Node newNode = nodeFactory.createNode(bpelNodeType, vdScope, myLookup);
        //
        return newNode;
    }
    
    private VariableDeclaration[] getDeclaredVariables(VariableDeclarationScope vdScope) {
        //
        // Check if it necessary to show only variables with appropriate type
        boolean showAppropriateVarOnly = false;
        VariableTypeFilter typeFilter = (VariableTypeFilter)myLookup.
                lookup(VariableTypeFilter.class);
        if (typeFilter != null) {
            showAppropriateVarOnly = typeFilter.isShowAppropriateVarOnly();
        }
        //
        if (vdScope instanceof BaseScope) {
            VariableContainer vc = ((BaseScope)vdScope).getVariableContainer();
            if (vc != null) {
                VariableDeclaration[] varArr =
                        (VariableDeclaration[])vc.getVariables();
                //
                if (showAppropriateVarOnly) {
                    ArrayList<VariableDeclaration> varList =
                            new ArrayList<VariableDeclaration>();
                    //
                    MyVariableTypeInfoProvider typeInfoProvider =
                            new MyVariableTypeInfoProvider();
                    for (VariableDeclaration varDecl : varArr) {
                        typeInfoProvider.setVariableDecl(varDecl);
                        if (typeFilter.isTypeAllowed(typeInfoProvider)) {
                            varList.add(varDecl);
                        }
                    }
                    //
                    VariableDeclaration[] filteredVarArr = varList.toArray(
                            new VariableDeclaration[varList.size()]);
                    return filteredVarArr;
                } else {
                    return varArr;
                }
            }
        } else if (vdScope instanceof VariableDeclaration) {
            if (showAppropriateVarOnly) {
                VariableDeclaration var = (VariableDeclaration)vdScope;
                MyVariableTypeInfoProvider typeInfoProvider =
                        new MyVariableTypeInfoProvider();
                typeInfoProvider.setVariableDecl(var);
                if (typeFilter.isTypeAllowed(typeInfoProvider)) {
                    return new VariableDeclaration[] {var};
                }
            } else {
                return new VariableDeclaration[] {(VariableDeclaration)vdScope};
            }
        }
        //
        return new VariableDeclaration[0];
    }
    
    private class MyVariableTypeInfoProvider implements VariableTypeInfoProvider {
        private VariableDeclaration varDecl;
        
        public void setVariableDecl(VariableDeclaration newValue) {
            varDecl = newValue;
        }
        
        public VariableStereotype getVariableStereotype() {
            return EditorUtil.getVariableStereotype(varDecl);
        }
        
        public Object getVariableType() {
            return EditorUtil.getVariableType(varDecl);
        }
        
        public QName getVariableQNameType() {
            return VariableNode.getVariableQNameType(varDecl);
        }
    }
    
}
