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
package org.netbeans.modules.bpel.properties.editors.nodes.factory;

import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.nodes.children.MessageExchangeChildren;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * This factory create the nodes tree for the Message Exchange chooser.
 * It contains the following structure:
 *
 * Process
 *  |-Process
 *     |-Scope
 *        |- Message Exchange 1
 *        |- Message Exchange 2
 *     |- Message Exchange 1
 *     |- Message Exchange 2
 *
 * @author nk160297
 */
public class MessageExchangeChooserNodeFactory implements NodeFactory<NodeType> {
    
    private NodeFactory myDelegate;
    
    /** Creates a new instance of PropertyChooserNodeFactory */
    public MessageExchangeChooserNodeFactory(NodeFactory delegate) {
        myDelegate = delegate;
    }
    
    public Node createNode(NodeType nodeType, Object ref, 
            Children children, Lookup lookup) {
        return myDelegate.createNode(nodeType, ref, children, lookup);
    }
    
    public Node createNode(NodeType nodeType, Object ref, Lookup lookup) {
        //
        Children children;
        Node newNode;
        //
        NodeFactory nodeFactory =
                (NodeFactory)lookup.lookup(NodeFactory.class);
        if (!(nodeFactory instanceof MessageExchangeChooserNodeFactory)) {
            lookup = new ExtendedLookup(lookup, this);
        }
        //
        switch (nodeType) {
            case PROCESS:
            case SCOPE:
                assert ref instanceof BaseScope;
                children = new MessageExchangeChildren((BaseScope)ref, lookup);
                newNode = myDelegate.createNode(nodeType, ref, children, lookup);
                return newNode;
            case MESSAGE_EXCHANGE:
                assert ref instanceof MessageExchange;
                newNode = myDelegate.createNode(nodeType, ref, Children.LEAF, lookup);
                return newNode;
            default:
                newNode = myDelegate.createNode(nodeType, ref, lookup);
                return newNode;
        }
    }
}
