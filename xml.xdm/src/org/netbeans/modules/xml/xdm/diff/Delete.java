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

import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Node;

/**
 * This class represents element add between 2 DOM tree
 *
 * @author Ayub Khan
 */
public class Delete extends Difference {
    
    /** Creates a new instance of DiffEvent */
    public Delete(NodeInfo.NodeType nodeType,
            List<Node> ancestors1, List<Node> ancestors2, Node n, int pos) {
        super(nodeType, ancestors1, ancestors2, n, null, pos, -1);
    }
    
    public List<Node> getNewAncestors() {
        return getOldNodeInfo().getNewAncestors();
    }
    
    public void setNewParent(Node p) {
        getOldNodeInfo().setNewParent(p);
    }
    
    public Node getNewParent() {
        return getOldNodeInfo().getNewParent();
    }
    
    public String toString() {
        return "DELETE("+ getOldNodeInfo() + ")";
    }
}
