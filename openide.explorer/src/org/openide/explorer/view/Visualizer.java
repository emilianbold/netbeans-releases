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
package org.openide.explorer.view;

import org.openide.nodes.Node;

import javax.swing.tree.*;


/** This class provide access to thread safe layer that
* reflects the hierarchy of Nodes, but is updated only in
* event dispatch thread (in contrast to nodes that can be updated from any thread).
* That is why this class is useful for writers of explorer views,
* because it guarantees that all changes will be done safely.
* <P>
* NodeTreeModel, NodeListModel, etc. use these objects as its
* model values.
*
* @author Jaroslav Tulach
*/
public class Visualizer extends Object {
    /** No constructor. */
    private Visualizer() {
    }

    /** Methods that create a tree node for given node.
    * The tree node reflects the state of the associated node as close
    * as possible, but is updated asynchronously in event dispatch thread.
    * <P>
    * This method can be called only from AWT-Event dispatch thread.
    *
    * @param node node to create safe representant for
    * @return tree node that represents the node
    */
    public static TreeNode findVisualizer(Node node) {
        return VisualizerNode.getVisualizer(null, node);
    }

    /** Converts visualizer object back to its node representant.
    *
    * @param visualizer visualizer create by findVisualizer method
    * @return node associated with the visualizer
    * @exception ClassCastException if the parameter is invalid
    */
    public static Node findNode(Object visualizer) {
        if (visualizer instanceof Node) {
            return (Node) visualizer;
        } else {
            return ((VisualizerNode) visualizer).node;
        }
    }
}
