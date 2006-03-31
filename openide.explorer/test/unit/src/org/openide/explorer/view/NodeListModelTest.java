/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.explorer.view;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/*
 * Tests for class NodeListModel
 */
public class NodeListModelTest extends NbTestCase {
    
    private static final int NO_OF_NODES = 20;
    
    public NodeListModelTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    /*
     * Tests whether children of the root node are
     * kept in the memory after the root is passed
     * to the constructor of NodeListModel.
     */
    public void testNodesAreReferenced() {
        
        WeakReference[] tn;
        
        Node c = new AbstractNode(new CNodeChildren());
        NodeListModel model = new NodeListModel(c);
        
        
        
        tn = new WeakReference[model.getSize()];
        for (int i = 0; i < model.getSize(); i++) {
            tn[i] = new WeakReference(model.getElementAt(i));
        }
        
        assertTrue("Need to have more than one child", tn.length > 0);
        
        boolean fail;
        try {
            assertGC("First node should not be gone", tn[0]);
            fail = true;
        } catch (Error err) {
            fail = false;
        }
        if (fail) {
            fail("First node garbage collected!!! " + tn[0].get());
        }
        
        for (int i = 0; i < tn.length; i++) {
            // else fail
            assertNotNull("One of the nodes was gone. Index: " + i, tn[i].get());
        }
    }
    
    /**
     * Tests proper initialization in constructors.
     */
    public void testConstructors() {
        Node c = new AbstractNode(new CNodeChildren());
        NodeListModel model = new NodeListModel(c);
        
        // the following line used to fail if the
        // no parameter costructor does not initialize
        // childrenCount
        model.getSize();
    }
    
    /*
     * Children for testNodesAreReferenced.
     */
    private static class CNodeChildren extends Children.Keys {
        public CNodeChildren() {
            List myKeys = new LinkedList();
            for (int i = 0; i < NO_OF_NODES; i++) {
                myKeys.add(new Integer(i));
            }
            
            setKeys(myKeys);
        }
        
        protected Node[] createNodes(Object key) {
            AbstractNode an = new AbstractNode(Children.LEAF);
            an.setName(key.toString());
            return  new Node[] { an };
        }
    }
}
