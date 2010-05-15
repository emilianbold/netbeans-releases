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
import java.util.Collection;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Shows the list of Message Part nodes
 *
 * @author nk160297
 */
public class MessagePartsChildren extends Children.Keys {
    
    private Lookup myLookup;
    
    public MessagePartsChildren(Message message, Lookup lookup) {
        myLookup = lookup;
        //
        setKeys(new Object[] {message});
    }
    
    protected Node[] createNodes(Object key) {
        assert key instanceof Message;
        NodeFactory nodeFactory =
                (NodeFactory)myLookup.lookup(NodeFactory.class);
        ArrayList<Node> nodesList = new ArrayList<Node>();
        //
        Collection<Part> mParts = ((Message)key).getParts();
        for (Part elem : mParts) {
            Node newNode = nodeFactory.createNode(
                NodeType.MESSAGE_PART, elem, myLookup);
            if (newNode != null) {
                nodesList.add(newNode);
            }
            
        }
        //
        Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
        return nodes;
    }
}
