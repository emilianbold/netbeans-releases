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

import javax.swing.JPanel;
import javax.swing.tree.TreeNode;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Tests for issue #95364.
 */
public class VisualizerSpeed95364Test extends NbTestCase {
    private int size;
    private TreeNode toCheck;
    
    /**
     *
     * @param name
     */
    public VisualizerSpeed95364Test(String name) {
        super(name);
    }
    
    /**
     *
     * @return
     */
    public static NbTestSuite suite() {
        return NbTestSuite.speedSuite(
                VisualizerSpeed95364Test.class, /* what tests to run */
                10 /* ten times slower */,
                3 /* try three times if it fails */
                );
    }
    
    /**
     *
     */
    protected void setUp() {
        size = getTestNumber();
        final MyKeys chK = new MyKeys();
        final AbstractNode root = new AbstractNode(chK);
        root.setName("test root");
        
        final String[] childrenNames = new String[size];
        for (int i = 0; i < size; i++) {
            childrenNames[i] = "test"+i;
        };
        chK.mySetKeys(childrenNames);
        toCheck = Visualizer.findVisualizer(root);
    }
    
    private void doTest() {
        TreeNode tn = null;
        for (int i = 0; i < 100000; i++) {
            tn = toCheck.getChildAt(size/2);
        }
    }
    
    /**
     *
     */
    public void test10() { doTest(); }
    /**
     *
     */
    public void test100() { doTest(); }
    /**
     *
     */
    public void test1000() { doTest(); }
    
    
    private static Node createLeaf(String name) {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(name);
        return n;
    }
    
    private static final class MyKeys extends Children.Keys {
        protected Node[] createNodes(Object key) {
            return new Node[] { createLeaf(key.toString())};
        }
        
        public void mySetKeys(Object[] newKeys) {
            super.setKeys(newKeys);
        }
    }
}
