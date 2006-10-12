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
import org.netbeans.modules.xml.spi.dom.NodeListImpl;
import org.netbeans.modules.xml.xam.dom.ElementIdentity;
import org.netbeans.modules.xml.xdm.diff.DiffFinder.SiblingInfo;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.w3c.dom.NodeList;

/**
 *
 * @author Nam Nguyen
 */
public class NodeIdDiffFinder extends DiffFinder {
    
    /** Creates a new instance of NodeIdDiffFinder */
    public NodeIdDiffFinder() {
    }
    
    protected Node findMatch(Element child, List<Node> childNodes, org.w3c.dom.Node parent1) {
        return findMatchedNode(child, childNodes);
    }
    
    protected Node findMatch(Text child, List<Node> childNodes) {
        return findMatchedNode(child, childNodes);
    }
    
    private Node findMatchedNode(Node child, List<Node> childNodes) {
        if (childNodes != null) {
            for (int i=0; i<childNodes.size(); i++) {
                Node otherChild = (Node) childNodes.get(i);
                if (otherChild.getId() == child.getId()) {
                    return otherChild;
                }
            }
        }
        return null;
    }
    
}

