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

import java.util.ArrayList;
import java.util.Enumeration;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.openide.loaders.DataFolder;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class CategoryChildren extends Children.Keys {
    
    private Lookup myLookup;
    
    public CategoryChildren(DataFolder paletteFolder, Lookup lookup) {
        myLookup = lookup;
        setKeys(new Object[] {paletteFolder});
    }

    protected Node[] createNodes(Object key) {
        assert key instanceof DataFolder;
        //
        NodeFactory factory = (NodeFactory)myLookup.lookup(NodeFactory.class);
        assert factory != null;
        //
        DataFolder df = (DataFolder)key;
        Enumeration childEnum = df.children();
        //
        ArrayList<Node> nodesList = new ArrayList<Node>();
        //
        while (childEnum.hasMoreElements()) {
            Object next = childEnum.nextElement();
            if (next instanceof DataFolder) {
                Node newNode = factory.createNode(
                        PaletteTreeNodeFactory.NodeType.CATEGORY, next, myLookup);
                nodesList.add(newNode);
            } else if (next instanceof XMLDataObject) {
                Node newNode = factory.createNode(
                        PaletteTreeNodeFactory.NodeType.ITEM, next, myLookup);
                if (newNode != null) {
                    nodesList.add(newNode);
                }
            }
        }
        //
        Node[] nodesArr = nodesList.toArray(new Node[nodesList.size()]);
        return nodesArr;
    }
    
}
