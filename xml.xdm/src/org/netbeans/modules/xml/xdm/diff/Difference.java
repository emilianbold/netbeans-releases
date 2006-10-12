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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xdm.diff;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Node;

/**
 * This class represents diff between 2 elements of 2 XML documents
 *
 * @author Ayub Khan
 */
public abstract class Difference {
    
    /** Creates a new instance of DiffEvent */
    public Difference(NodeInfo.NodeType nodeType,
            List<Node> ancestors1, List<Node> ancestors2,
            Node n1, Node n2, int n1Pos, int n2Pos) {
        this.nodeType = nodeType;
        if (! (n1 instanceof Document)) {
            assert ancestors1 != null && ! ancestors1.isEmpty() : "diff of non-root should have ancestors";
        }
        this.oldNodeInfo = new NodeInfo( n1, n1Pos, ancestors1, ancestors2);
        this.newNodeInfo = new NodeInfo( n2, n2Pos, new ArrayList(ancestors1), new ArrayList(ancestors2));
        if (newNodeInfo.getNode() != null && newNodeInfo.getNewAncestors().size() > 0) {
            assert newNodeInfo.getNewAncestors().get(0).getId() != newNodeInfo.getNode().getId();
        }
    }
    
    public NodeInfo.NodeType getNodeType() {
        return nodeType;
    }
    
    /**
     * @returns info on removed node.
     */
    public NodeInfo getOldNodeInfo() {
        return oldNodeInfo;
    }
    
    /**
     * @return info on added node.
     */
    public NodeInfo getNewNodeInfo() {
        return newNodeInfo;
    }
    
    /**
     * @return new path from parent to root.
     */
    public abstract List<Node> getNewAncestors();
    
    public abstract void setNewParent(Node n);
    
    public abstract Node getNewParent();
    
    ////////////////////////////////////////////////////////////////////////////////
    // Member variables
    ////////////////////////////////////////////////////////////////////////////////
    
    private NodeInfo.NodeType nodeType;
    
    private NodeInfo oldNodeInfo;
    
    private NodeInfo newNodeInfo;
    
}
