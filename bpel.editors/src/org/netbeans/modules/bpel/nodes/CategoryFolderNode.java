/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.nodes;

import java.awt.Image;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * This node is intended to present variouse simple folder at a tree view.
 *
 * @author nk160297
 */
public class CategoryFolderNode extends BpelNode<NodeType> {
    
    private Image[] myIcons = new Image[4];
    
    public CategoryFolderNode(NodeType type, Children children, Lookup lookup) {
        super(type, children, lookup);
    }
    
    public NodeType getNodeType() {
        return getReference();
    }
    
    public synchronized Image getIcon(int type) {
        if (myIcons[type] == null) {
            myIcons[type] = super.getIcon(type);
            //
            if (NodeType.UNKNOWN_IMAGE.equals(myIcons[type])) {
                DataObject dobj;
                try {
                    dobj = DataObject.find(Repository.getDefault().
                            getDefaultFileSystem().getRoot());
                    Node n = dobj.getNodeDelegate();
                    myIcons[type] = n.getIcon(type);
                } catch (DataObjectNotFoundException ex) {
                    // do nothing here
                }
            }
        }
        //
        return myIcons[type];
    }
    
}
