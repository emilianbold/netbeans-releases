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
package org.netbeans.modules.bpel.nodes;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.nodes.actions.AddVariableAddAction;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.bpel.properties.editors.controls.filter.NodeChildFilter;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Represents the Variable Container object of Process or Scope.
 *
 * @author nk160297
 */
public class VariableContainerNode
        extends ContainerBpelNode<BaseScope, VariableContainer>
        implements ReloadableChildren {

    public VariableContainerNode(VariableContainer varContainer, Children children, Lookup lookup) {
        super((BaseScope)varContainer.getParent(), children, lookup);
    }

    public VariableContainerNode(VariableContainer varContainer, Lookup lookup) {
        this((BaseScope)varContainer.getParent(), lookup);
    }

    public VariableContainerNode(final BaseScope baseScope, Children children, Lookup lookup) {
        super(baseScope, children, lookup);
    }
    
    public VariableContainerNode(final BaseScope baseScope, Lookup lookup) {
        super(baseScope, lookup);
        //
        Children.MUTEX.postWriteRequest(new Runnable() {
            public void run() {
                setChildren(new MyChildren(baseScope));
            }
        });
    }
    
    public NodeType getNodeType() {
        return NodeType.VARIABLE_CONTAINER;
    }
    
    public void reload() {
        Children.MUTEX.postWriteRequest(new Runnable() {
            public void run() {
                BaseScope ref = getReference();
                if (ref != null) {
                    setChildren(new MyChildren(ref));
                }
            }
        });
    }
    
//    public Action getPreferredAction() {
//        Action action = (Action)getLookup().lookup( Action.class );
//        return action ;
//    }
//    
    public String getDisplayName() {
        return getNodeType().getDisplayName();
    }

    protected String getImplHtmlDisplayName() {
        return getDisplayName();
    }
    
//    protected String getImplShortDescription() {
//        return NbBundle.getMessage(VariableContainerNode.class,
//                "LBL_VARIABLE_CONTAINER_NODE_TOOLTIP"); // NOI18N
//    }
    
    public VariableContainer getContainerReference() {
        BaseScope ref = getReference();
        return ref == null ? null : ref.getVariableContainer();
    }
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.ADD_VARIABLE,
            ActionType.SEPARATOR,
            ActionType.GO_TO_VARCONTAINER_SOURCE
        };
    }
    
    public Action createAction(ActionType actionType) {
        if (ActionType.ADD_VARIABLE.equals(actionType)) {
            return SystemAction.get(AddVariableAddAction.class);
        }
        return super.createAction(actionType);
    }
    
    private class MyChildren extends Children.Keys {
        
        private NodeChildFilter filter;
        
        public MyChildren(BaseScope baseScope) {
            super();
            //
            filter = (NodeChildFilter)getLookup().lookup(NodeChildFilter.class);
            //
            setKeys(new Object[] {baseScope});
        }
        
        protected Node[] createNodes(Object key) {
            assert key instanceof BaseScope;
            //
            VariableContainer variableContainer =
                    ((BaseScope)key).getVariableContainer();
            if (variableContainer != null) {
                List<Node> nodesList = new ArrayList<Node>();
                //
                for (Variable var : variableContainer.getVariables()) {
                    //
                    Node newVarNode = PropertyNodeFactory.getInstance().
                            createNode(NodeType.VARIABLE, var, getLookup());
                    // VariableNode newVarNode = new VariableNode(var, getLookup());
                    //
                    if (filter == null || filter.isPairAllowed(getNode(), newVarNode)) {
                        nodesList.add(newVarNode);
                    }
                }
                //
                Node[] nodesArr = nodesList.toArray(new Node[nodesList.size()]);
                //
                return nodesArr;
            }
            return new Node[] {}; // Return empty array
        }
    }
}
