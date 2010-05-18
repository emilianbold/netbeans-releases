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
package org.netbeans.modules.soa.ui.axinodes;

import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Element;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Constructs an AXIOM tree for entire Schema model or
 * for a Global Element or Type.
 *
 * @author nk160297
 */
public class AxiomTreeNodeFactory implements NodeFactory<NodeType> {
    
    public AxiomTreeNodeFactory() {
    }
    
    public Node createNode(NodeType nodeType, Object ref,
            Children children, Lookup lookup) {
        switch (nodeType) {
            case ELEMENT:
                assert ref instanceof Element;
                return new ElementNode((Element)ref, children, lookup);
            case ATTRIBUTE:
                assert ref instanceof Attribute;
                return new AttributeNode((Attribute)ref, children, lookup);
            default: 
                return null;
        }
    }
    
    public Node createNode(NodeType nodeType, Object ref, Lookup lookup) {
        //
        Children children = null;
        Node newNode = null;
        //
        NodeFactory nodeFactory =
                (NodeFactory)lookup.lookup(NodeFactory.class);
        if (!(nodeFactory instanceof AxiomTreeNodeFactory)) {
            lookup = new ExtendedLookup(lookup, this);
        }
        //
        switch (nodeType) {
            case ELEMENT:
                assert ref instanceof Element;
                children = new ElementChildren((Element)ref, lookup);
                newNode = createNode(nodeType, ref, children, lookup);
                return newNode;
            default:
                newNode = createNode(nodeType, ref, Children.LEAF, lookup);
                return newNode;
        }
    }
    
}
