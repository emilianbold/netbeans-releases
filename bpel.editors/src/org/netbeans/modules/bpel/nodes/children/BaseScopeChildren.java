/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.netbeans.modules.bpel.properties.editors.controls.filter.VisibilityScope;
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
