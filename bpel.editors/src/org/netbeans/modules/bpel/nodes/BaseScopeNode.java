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

import org.netbeans.modules.bpel.nodes.BpelNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.bpel.model.api.ActivityHolder;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.properties.editors.controls.filter.NodeChildFilter;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.nodes.actions.AddCompensationHandlerAction;
import org.netbeans.modules.bpel.nodes.actions.AddEventHandlersAction;
import org.netbeans.modules.bpel.nodes.actions.AddFaultHandlersAction;
import org.netbeans.modules.bpel.nodes.dnd.BpelEntityPasteType;
import org.netbeans.modules.bpel.nodes.dnd.SequenceEntityPasteType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 * Represents Process or Scope at a tree in Variable chooser.
 * This node can look different depend on the presence of the
 * INodeChildFilter or VisibilityScope at the lookup.
 *
 * @author nk160297
 */
//public class BaseScopeNode extends BpelNode<BaseScope> {
public abstract class BaseScopeNode<BS extends BaseScope> extends BpelNode<BS> 
{
    private NodeType myType;
    
    /**
     * Create Node for a Process.
     */
    public BaseScopeNode(BS baseScope, Lookup lookup) {
        super(baseScope, new MyChildren(baseScope, lookup), lookup);
    }
    
    /**
     * Create Node for a Process.
     */
    public BaseScopeNode(BS baseScope, Children children, Lookup lookup) {
        super(baseScope, children, lookup);
    }
    
    protected boolean isEventRequreUpdate(ChangeEvent event) {
        assert event != null;
        
        boolean isUpdate = false;
        isUpdate = super.isEventRequreUpdate(event);
        if (isUpdate) {
            return isUpdate;
        }
        
        BpelEntity entity = event.getParent();
        if (entity == null) {
            return false;
        }
        BaseScope ref = getReference();
        return  
                ref != null && ref == entity.getParent() && (
                entity.getElementType() == PartnerLinkContainer.class
// TODO r issue 84631                || entity.getElementType() == FaultHandlers.class
                || entity.getElementType() == VariableContainer.class
                || entity.getElementType() == CorrelationSetContainer.class
                || entity.getElementType() == MessageExchangeContainer.class
                );
    }

    protected boolean isDropNodeInstanceSupported(BpelNode childNode) {
        return isDropNodeSupported(childNode);
    }
    
    public List<BpelEntityPasteType> createSupportedPasteTypes(BpelNode childNode) {
        if (!isDropNodeInstanceSupported(childNode)) {
            return null;
        }
        
        if (childNode.getNodeType().equals(NodeType.SEQUENCE)) {
            BpelEntity parentRefObj = (BpelEntity) getReference();
            BpelEntity childRefObj = (BpelEntity) childNode.getReference();
            List<BpelEntityPasteType> resultPTs
                    = new ArrayList<BpelEntityPasteType>();
            resultPTs.add(new SequenceEntityPasteType(parentRefObj,childRefObj));
            return resultPTs;
        }
        
        return null;
    }
    
    protected ActionType[] getActionsArray() {
        if (isModelReadOnly()) {
            return new ActionType[] {
//                ActionType.GO_TO_SOURCE,
//                ActionType.GO_TO_DIAGRAMM,
                ActionType.GO_TO,
                ActionType.SEPARATOR,
                ActionType.SHOW_POPERTY_EDITOR,
                ActionType.SEPARATOR,
                ActionType.TOGGLE_BREAKPOINT,
                ActionType.SEPARATOR,
                ActionType.REMOVE
            };
        }
        
        return new ActionType[] {
            ActionType.ADD_NEWTYPES,
            ActionType.SEPARATOR,
//            ActionType.GO_TO_SOURCE,
//            ActionType.GO_TO_DIAGRAMM,
            ActionType.GO_TO,
            ActionType.SEPARATOR,
            ActionType.SHOW_POPERTY_EDITOR,
            ActionType.SEPARATOR,
            ActionType.MOVE_UP,
            ActionType.MOVE_DOWN,
            ActionType.SEPARATOR,
            ActionType.TOGGLE_BREAKPOINT,
            ActionType.SEPARATOR,
            ActionType.REMOVE
        };
    }
    
    public ActionType[] getAddActionArray() {
        return new ActionType[] {
            ActionType.ADD_VARIABLE,
            ActionType.ADD_CORRELATION_SET,
            ActionType.ADD_COMPENSATION_HANDLER,
            ActionType.ADD_EVENT_HANDLERS,
            ActionType.ADD_FAULT_HANDLERS
        };
    }
    
    public Action createAction(ActionType actionType) {
        Action action = null;
        switch (actionType) {
            case ADD_COMPENSATION_HANDLER
                    : action = SystemAction.get(AddCompensationHandlerAction.class);
                    break;
            case ADD_EVENT_HANDLERS
                    : action = SystemAction.get(AddEventHandlersAction.class);
                    break;
            case ADD_FAULT_HANDLERS
                    : action = SystemAction.get(AddFaultHandlersAction.class);
                    break;
                    
            default
                            : action = super.createAction(actionType);
        }
        return action;
    }
}

class MyChildren extends Children.Keys {
    
    private Lookup myLookup;
    
    public MyChildren(BaseScope scope, Lookup lookup) {
        super();
        myLookup = lookup;
        //
        setKeys(new Object[] {scope});
    }
    
    protected Node[] createNodes(Object key) {
        BaseScope bScope = (BaseScope)key;
        List<Node> nodesList = new ArrayList<Node>();
        Node currentNode = getNode();
        //
        NodeChildFilter filter = (NodeChildFilter)myLookup.
                lookup(NodeChildFilter.class);
        //
        // Add the "Variables" node
        VariableContainerNode varContainerNode =
                new VariableContainerNode(bScope, myLookup);
        if (filter == null || filter.isPairAllowed(currentNode, varContainerNode)) {
            nodesList.add(varContainerNode);
        }
        //
        // Add the "Correlation Sets" node
        CorrelationSetContainerNode csContainerNode =
                new CorrelationSetContainerNode(bScope, myLookup);
        if (filter == null || filter.isPairAllowed(currentNode, csContainerNode)) {
            nodesList.add(csContainerNode);
        }
        //
        VisibilityScope visScope = (VisibilityScope)myLookup.
                lookup(VisibilityScope.class);
        if (visScope == null) {
            // The Visibility Scope isn't specified. So show all children.
            if (bScope instanceof ActivityHolder) {
                List<Scope> scopeList = VisibilityScope.Utils.getNestedScopes(
                        ((ActivityHolder)bScope).getActivity());
                for (Scope aScope : scopeList) {
                    ScopeNode newNode =
                            new ScopeNode(aScope, myLookup);
                    if (filter == null || filter.isPairAllowed(currentNode, newNode)) {
                        nodesList.add(newNode);
                    }
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
                ScopeNode newNode =
                        new ScopeNode((Scope)subsequentScope, myLookup);
                if (filter == null || filter.isPairAllowed(currentNode, newNode)) {
                    nodesList.add(newNode);
                }
            }
        }
        //
        Node[] nodesArr = nodesList.toArray(new Node[nodesList.size()]);
        return nodesArr;
    }
    
}
