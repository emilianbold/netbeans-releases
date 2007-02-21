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
package org.netbeans.modules.bpel.nodes.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CatchAll;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.nodes.children.BpelNodeChildren;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 3 May 2006
 *
 */
public class BaseScopeChildren extends BpelNodeChildren<BaseScope> {
    
    public BaseScopeChildren(BaseScope baseScope, Lookup contextLookup) {
        super(baseScope, contextLookup);
    }

    public Collection getNodeKeys() {
        BaseScope ref = getReference();
        if (ref == null) {
            return Collections.EMPTY_LIST;
        }
        
        List<Object> childs = new ArrayList<Object>();
        // so as special sort order ()
        // is required do next:
        
        // set variable container node
        VariableContainer varCont = ref.getVariableContainer();
        if (varCont != null) {
            childs.add(varCont);
        } else {
            childs.add(VariableContainer.class);
        }
        
        // set correlation set container node
        CorrelationSetContainer corrSetCont = ref.getCorrelationSetContainer();
        if (corrSetCont != null) {
            childs.add(corrSetCont);
        } else {
            childs.add(CorrelationSetContainer.class);
        }

// Issue 85553 start.        
//        // set message exchange container node
//        MessageExchangeContainer mExCont = ref.getMessageExchangeContainer();
//        if (mExCont != null) {
//            childs.add(mExCont);
//        } else {
//            childs.add(MessageExchangeContainer.class);
//        }
// Issue 85553 end.        

        // set activity node
        BpelContainer activity = ref.getActivity();
        if (activity != null) {
            childs.add(activity);
        }
        
        FaultHandlers faultHandlers = ref.getFaultHandlers();
        
        if (faultHandlers != null) {
            childs.add(faultHandlers);
        // TODO r issue 84631
//            // set catch nodes
//            Catch[] catches = faultHandlers.getCatches();
//            if (catches != null && catches.length > 0) {
//                childs.addAll(Arrays.asList(catches));
//            }
//            
//            // set catchAll node
//            CatchAll catchAll = faultHandlers.getCatchAll();
//            if (catchAll != null) {
//                childs.add(catchAll);
//            }
        }
        
        // Set eventHandlers Nodes
        EventHandlers eventHandlers = ref.getEventHandlers();
        if (eventHandlers != null) {
            childs.add(eventHandlers);
        }
        
        // Set CompensationHandlerHandler Node
        if (ref instanceof Scope) {
            CompensationHandler compensationHandler = ((Scope)ref).getCompensationHandler();
            if (compensationHandler != null) {
                childs.add(compensationHandler);
            }
        }

        // Set TerminationHandler Node
        if (ref instanceof Scope) {
            TerminationHandler terminationHandler = ((Scope)ref).getTerminationHandler();
            if (terminationHandler != null) {
                childs.add(terminationHandler);
            }
        }
        
        return childs;
    }
    
    protected Node[] createNodes(Object object) {
        if (object == null) {
            return new Node[0];
        }
        NavigatorNodeFactory factory = NavigatorNodeFactory.getInstance();
        Node childNode = null;
        
        // create variable container node
        if (object instanceof VariableContainer 
                || object == VariableContainer.class) 
        {
            childNode = factory.createNode(
                    NodeType.VARIABLE_CONTAINER
                    ,getReference()
                    ,getLookup());
        } else if (object instanceof CorrelationSetContainer 
                || object == CorrelationSetContainer.class) 
        { // create correlation set container
            childNode = factory.createNode(
                    NodeType.CORRELATION_SET_CONTAINER
                    ,getReference()
                    ,getLookup());
        } else if (object instanceof MessageExchangeContainer 
                || object == MessageExchangeContainer.class) 
        { // create message exchange container
            childNode = factory.createNode(
                    NodeType.MESSAGE_EXCHANGE_CONTAINER
                    ,getReference()
                    ,getLookup());
        } else  if (object instanceof BpelEntity) {
            childNode = factory.createNode((BpelEntity)object,getLookup());
        }
        
        return childNode == null ? new Node[0] : new Node[] {childNode};
    }
}
