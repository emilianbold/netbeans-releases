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


package org.netbeans.modules.soa.ui.nodes;

import java.awt.Image;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public interface NodeFactory<KeyType> {

    /**
     * Create a new instance of Node by a key.
     * Current implementation implies different Pattern classes as keys.
     * <p>
     * This method implies that the Node's children are defined by it own.
     * If the Node doesn't define any Children then it considered as a LEAF by default.
     * <p>
     * @param key specifies the node type. 
     * @param ref specifies the object the new node should represent. 
     * @param lookup parameter contains all other context.
     * @return the new Node
     */
    Node createNode(KeyType nodeType, Object ref, Lookup lookup);
    
    /**
     * Creates a new instance of Node by a key. 
     * Current implementation implies different Pattern classes as keys.
     * <p>
     * @param key specifies the node type. 
     * @param ref specifies the object the new node should represent. 
     * @param children provides way to load node's children. 
     * @param lookup parameter contains all other context.
     * @return the new Node
     */
    Node createNode(KeyType nodeType, Object ref, 
            Children children, Lookup lookup);
    
    /**
     * This node can be used as a simple node with text. 
     * It's especially helpfull to create a hidden root node. 
     */
    static class TextNode extends AbstractNode {

        public TextNode(Children children, String name) {
            super(children);
            setName(name);
        }

        public String getHtmlDisplayName() {
            return getName();
        }
        
        public Image getIcon(int type) {
            return null;
        }
        
        public Image getOpenedIcon(int type) {
            return null;
        }
    }
 
}
