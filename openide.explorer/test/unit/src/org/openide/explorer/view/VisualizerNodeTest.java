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

import java.awt.Image;
import javax.swing.Icon;
import javax.swing.tree.TreeNode;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/** VisualizerNode tests, mostly based on reported bugs.
 */
public class VisualizerNodeTest extends NbTestCase {

    public VisualizerNodeTest(String name) {
        super(name);
    }

    protected boolean runInEQ() {
        return true;
    }

    public void testIconIsProvidedEvenTheNodeIsBrokenIssue46727() {
        final boolean[] arr = new boolean[1];
        
        AbstractNode a = new AbstractNode(Children.LEAF) {
            public Image getIcon(int type) {
                arr[0] = true;
                return null;
            }
        };
        
        VisualizerNode v = VisualizerNode.getVisualizer(null, a);
        assertNotNull("Visualizer node", v);
        
        Icon icon = v.getIcon(false, false);
        assertNotNull("Cannot be null even the node's icon is null", icon);
        assertTrue("getIcon called", arr[0]);
    }
    
    public void testIndexOfProvidesResultsEvenIfTheVisualizerIsComputedViaDifferentMeans() throws Exception {
        AbstractNode a = new AbstractNode(new Children.Array());
        AbstractNode m = new AbstractNode(Children.LEAF);
        a.getChildren().add(new Node[] { Node.EMPTY.cloneNode(), m, Node.EMPTY.cloneNode() });
        
        TreeNode ta = Visualizer.findVisualizer(a);
        TreeNode tm = Visualizer.findVisualizer(m);
        
        assertEquals("Index is 1", 1, ta.getIndex(tm));
    }
}
