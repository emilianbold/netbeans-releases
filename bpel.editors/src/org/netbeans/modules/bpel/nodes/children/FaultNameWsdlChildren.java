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
import java.util.List;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Supports the list of Message Exchanges and nested Scopes of a Process or a Scope
 *
 * @author nk160297
 */
public class FaultNameWsdlChildren extends BaseScopeChildren {
    
    public FaultNameWsdlChildren(BaseScope bScope, Lookup lookup) {
        super(bScope, lookup);
    }
    
    protected Node[] createNodes(Object key) {
        if (!(key instanceof BaseScope)) {
            return null;
        }
        BaseScope bScope = (BaseScope)key;
        //
        NodeFactory nodeFactory = (NodeFactory)myLookup.lookup(NodeFactory.class);
        //
        List<Node> nodesList = new ArrayList<Node>();
        //
        addScopeNodes(bScope, nodesList);
        //
        // Add Message Exchanges
        MessageExchangeContainer meContainer =
                bScope.getMessageExchangeContainer();
        if (meContainer != null) {
            MessageExchange[] mExchangeArr = meContainer.getMessageExchanges();
            for (MessageExchange mExch : mExchangeArr) {
                Node newNode = nodeFactory.createNode(
                        NodeType.MESSAGE_EXCHANGE, mExch, myLookup);
                nodesList.add(newNode);
            }
        }
        //
        Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
        return nodes;
    }
    
}
