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

package org.openide.explorer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.LinkedList;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Jaroslav Tulach
 */
public class ExplorerManagerTest extends NbTestCase
        implements PropertyChangeListener {
    private ExplorerManager em;
    private Keys keys;
    private Node root;
    private LinkedList events;
    
    public ExplorerManagerTest(String testName) {
        super(testName);
    }
    
    /** This code is supposed to run in AWT test.
     */
    protected boolean runInEQ() {
        return true;
    }
    
    protected void setUp() throws Exception {
        em = new ExplorerManager();
        keys = new Keys();
        root = new AbstractNode(keys);
        Node[] justAsk = root.getChildren().getNodes(true);
        em.setRootContext(root);
        events = new LinkedList();
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        assertFalse("No read lock held", Children.MUTEX.isReadAccess());
        assertFalse("No write lock held", Children.MUTEX.isWriteAccess());
        
        events.add(ev);
    }
    
    public void testNormalSelectionChange() throws Exception {
        final Node a = keys.key("a key");
        
        em.addPropertyChangeListener(this);
        
        em.setSelectedNodes(new Node[] { a });
        Node[] arr = em.getSelectedNodes();
        assertEquals("One selected", 1, arr.length);
        assertEquals("A is there", a, arr[0]);
        
        
        assertEquals("One event", 1, events.size());
        PropertyChangeEvent ev = (PropertyChangeEvent)events.removeFirst();
        assertEquals("Name is good", ExplorerManager.PROP_SELECTED_NODES, ev.getPropertyName());

        events.clear();
        em.setSelectedNodes(new Node[] { a });
        assertEquals("No change: " + events, 0, events.size());
    }
    
    public void testCannotSetNodesNotUnderTheRoot() throws Exception {
        final Node a = new AbstractNode(Children.LEAF);
        
        try {
            em.setSelectedNodes(new Node[] { a });
            fail("Should throw IllegalArgumentException as the node is not under root");
        } catch (IllegalArgumentException ex) {
            // ok, a is not under root
        }
    }
    
    
    public void testSetNodesSurviveChangeOfNodes() throws Exception {
        final Node a = keys.key("toRemove");
        
        class ChangeTheSelectionInMiddleOfMethod implements VetoableChangeListener {
            public int cnt;
            
            public void vetoableChange(PropertyChangeEvent evt) {
                cnt++;
                keys.keys(new String[0]);
            }
        }
        
        ChangeTheSelectionInMiddleOfMethod list = new ChangeTheSelectionInMiddleOfMethod();
        em.addVetoableChangeListener(list);
        
        em.setSelectedNodes(new Node[] { a });
        
        assertEquals("Vetoable listener called", 1, list.cnt);
        assertEquals("Node is dead", null, a.getParentNode());
        
        // handling of removed nodes is done asynchronously
        em.waitFinished();
        
        Node[] arr = em.getSelectedNodes();
        assertEquals("No nodes can be selected", 0, arr.length);
    }
    
    public void testCannotVetoSetToEmptySelection() throws Exception {
        final Node a = keys.key("toRemove");
        
        em.setSelectedNodes(new Node[] { a });
        
        class NeverCalledVeto implements VetoableChangeListener {
            public int cnt;
            
            public void vetoableChange(PropertyChangeEvent evt) {
                cnt++;
                keys.keys(new String[0]);
            }
        }
        
        NeverCalledVeto list = new NeverCalledVeto();
        em.addVetoableChangeListener(list);
        
        em.setSelectedNodes(new Node[0]);
        
        assertEquals("Veto not called", 0, list.cnt);
        Node[] arr = em.getSelectedNodes();
        assertEquals("No nodes can be selected", 0, arr.length);
    }
    
    private static final class Keys extends Children.Keys {
        public Node key(String k) {
            keys(new String[] { k });
            return getNodes()[0];
        }
        public void keys(String[] keys) {
            super.setKeys(keys);
        }
        protected Node[] createNodes(Object o) {
            AbstractNode an = new AbstractNode(Children.LEAF);
            an.setName((String)o);
            return new Node[] { an };
        }
    }
}
