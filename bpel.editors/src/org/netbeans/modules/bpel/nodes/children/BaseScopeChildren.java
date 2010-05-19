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

import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.ActivityHolder;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.netbeans.modules.bpel.nodes.ReloadableChildren;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * This class is intended to be a base class for load different children with
 * scope visibility.
 *
 * @author nk160297
 */
public abstract class BaseScopeChildren extends Children.Keys 
        implements ReloadableChildren {
    
    protected Lookup myLookup;
    private BaseScope myKey;
    private VisibilityScope visScope;
    
    protected BaseScopeChildren(BaseScope bScope, Lookup lookup) {
        myLookup = lookup;
        myKey = bScope;
        //
        // Try obtaining a visibility scope from the lookup
        visScope = (VisibilityScope)myLookup.lookup(VisibilityScope.class);
        //
        setKeys(new Object[] {bScope});
    }
    
    protected void addScopeNodes(BaseScope bScope, List<Node> nodesList) {
        //
        NodeFactory nodeFactory = (NodeFactory)myLookup.lookup(NodeFactory.class);
        BpelModel bpelModel = (BpelModel)myLookup.lookup(BpelModel.class);
        //
        // Add nested scopes
        if (visScope == null) {
            // The Visibility Scope isn't specified. So show all children.
            if (bScope instanceof ActivityHolder) {
                List<Scope> scopeList = VisibilityScope.Utils.getNestedScopes(
                        ((ActivityHolder)bScope).getActivity());
                for (Scope aScope : scopeList) {
                    Node newNode = nodeFactory.createNode(
                            NodeType.SCOPE, aScope, myLookup);
                    nodesList.add(newNode);
                }
            }
        } else {
            // The Visibility Scope is specified.
            // So take next scope element from the chain.
            List<BaseScope> scopeChain = visScope.getScopeChain();
            BaseScope subsequentScope = null;
            Iterator<BaseScope> itr = scopeChain.iterator();
            while (itr.hasNext()) {
                BaseScope aScope = itr.next();
                if (aScope.equals(bScope)) {
                    // At the point the current Scope element has found in the chain.
                    // The next item in the chain is the sought scope according to
                    // sorting order described at the VisibilityScope class.
                    if (itr.hasNext()) {
                        subsequentScope = itr.next();
                    }
                }
            }
            //
            if (subsequentScope != null) {
                //
                // It's implied that the only root node can be related to Process.
                // But childrens are always related to Scope.
                assert subsequentScope instanceof Scope;
                //
                Node newNode = nodeFactory.createNode(
                        NodeType.SCOPE, subsequentScope, myLookup);
                nodesList.add(newNode);
            }
        }
    }
    
    public void reload() {
        setKeys(new Object[] {new Object()});
        setKeys(new Object[] {myKey});
    }
}
