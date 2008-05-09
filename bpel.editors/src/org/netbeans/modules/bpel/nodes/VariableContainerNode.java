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
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
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
    
    public String getDisplayName() {
        return getNodeType().getDisplayName();
    }

    protected String getImplHtmlDisplayName() {
        return getDisplayName();
    }
    
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
