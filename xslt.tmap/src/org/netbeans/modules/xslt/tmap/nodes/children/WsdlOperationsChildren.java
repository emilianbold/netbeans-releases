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
package org.netbeans.modules.xslt.tmap.nodes.children;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xslt.tmap.nodes.NodeType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * 
 * @author Vitaly Bychkov
 */
public class WsdlOperationsChildren extends Children.Keys {
    
    private Lookup myLookup;
    private static final Logger LOGGER = Logger.getLogger(WsdlOperationsChildren.class.getName());
    
    public WsdlOperationsChildren(PortType ptObj, Lookup lookup) {
        myLookup = lookup;
        //

        setKeys(new Object[] {ptObj});
    }
    
    protected Node[] createNodes(Object key) {
        if (!(key instanceof PortType)) {
            return new Node[0];
        }
        PortType pt = (PortType)key;
        NodeFactory nodeFactory =
                (NodeFactory)myLookup.lookup(NodeFactory.class);
        ArrayList<Node> nodesList = new ArrayList<Node>();
        
        //
        Collection<Operation> ops = pt.getOperations();
        if (ops != null) {
            Node opNode = null;
            for (Operation op : ops) {
                opNode = nodeFactory.createNode(
                        NodeType.WSDL_OPERATION, op, myLookup);
                if (opNode != null) {
                    nodes.add(opNode);
                }
            }
        }
        //
        Node[] nodesArr = nodesList.toArray(new Node[nodesList.size()]);
        return nodesArr;
    }
}
