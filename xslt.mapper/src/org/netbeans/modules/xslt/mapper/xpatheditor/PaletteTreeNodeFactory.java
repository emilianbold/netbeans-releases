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
package org.netbeans.modules.xslt.mapper.xpatheditor;

import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.soa.ui.nodes.NodeFactory.TextNode;
import org.netbeans.modules.xslt.mapper.xpatheditor.PaletteTreeNodeFactory.NodeType;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Constructs a palette tree for the expression editor.
 *
 * @author nk160297
 */
public class PaletteTreeNodeFactory implements NodeFactory<NodeType> {
    
    public static enum NodeType {
        ROOT, CATEGORY, ITEM;
    }
    
    public PaletteTreeNodeFactory() {
    }
    
    public Node createNode(NodeType nodeType, Object ref,
            Children children, Lookup lookup) {
        switch (nodeType) {
            case ROOT:
                assert ref instanceof String;
                return new TextNode(children, (String)ref);
            case CATEGORY:
                assert ref instanceof DataFolder;
                return new CategoryNode((DataFolder)ref, children, lookup);
            case ITEM:
                assert ref instanceof XMLDataObject;
                if (ItemNode.isLiteral((XMLDataObject)ref)) {
                    // Skip all literals
                    return null;
                } else {
                    return new ItemNode((XMLDataObject)ref, children, lookup);
                }
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
        if (!(nodeFactory instanceof PaletteTreeNodeFactory)) {
            lookup = new ExtendedLookup(lookup, this);
        }
        //
        switch (nodeType) {
            case ROOT:
                assert ref instanceof FileObject; // Root palette folder 
                DataFolder paletteFolder = DataFolder.findFolder((FileObject)ref);
                children = new CategoryChildren(paletteFolder, lookup);
                newNode = createNode(nodeType, "Root", children, lookup); // NOI18N
                return newNode;
            case CATEGORY:
                assert ref instanceof DataFolder;
                children = new CategoryChildren((DataFolder)ref, lookup);
                newNode = createNode(nodeType, ref, children, lookup); // NOI18N
                return newNode;
            default:
                newNode = createNode(nodeType, ref, Children.LEAF, lookup);
                return newNode;
        }
    }
    
}
