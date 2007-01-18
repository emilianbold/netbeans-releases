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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import javax.swing.Action;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbNodesFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;


/**
 * Provide a set of children representing the ejb nodes.
 * @author ChrisWebster
 */
public class EjbChildren extends Children.Array {
    private final Node projectNode;

    /** Creates a new instance of EjbChildren */
    public EjbChildren(Node projectNode) {
        this.projectNode = projectNode;
    }

    protected void addNotify() {
        super.addNotify();
        Node ejbsNode = projectNode.getChildren().findChild(EjbNodesFactory.CONTAINER_NODE_NAME);
        // could add code here to only show ejb's which can be referenced
        Node[] ejbNodes = ejbsNode.getChildren().getNodes(true);
        Node[] filteredNodes = new Node[ejbNodes.length];
        for (int i =0; i < ejbNodes.length; i++) {
            filteredNodes[i] = new FilterNode(ejbNodes[i], Children.LEAF) {
                public Action[] getActions(boolean context) {
                    return new Action[0];
                }
            };
        }
        add(filteredNodes);
    }
    
}
