/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.nodes;

import junit.framework.*;
import junit.textui.TestRunner;
import java.beans.*;
import java.beans.beancontext.*;
import java.util.*;
import org.openide.util.Mutex;

import org.netbeans.junit.*;

/** Test updating of bean children in proper circumstances, e.g.
 * deleting nodes or beans.
 * @author Jesse Glick
 */
public class FilterChildrenEventsTest extends NbTestCase {
    
    public FilterChildrenEventsTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        System.out.println("Running");
        TestRunner.run(new NbTestSuite(BeanChildrenTest.class));
    }
    
    
    public void testNodesNodeDestroyed() throws Exception {
        
        Node[] chNodes = createTestNodes();
        Children ch = new Children.Array();
        ch.add( chNodes );
        
        Node n = new AbstractNode( ch );
        FilterNode fn = new FilterNode( n );
        n.setName( "X" );
        MyListener ml = new MyListener();
        
        fn.addNodeListener( ml );
        
        n.setName( "Y" );
        
        List events = ml.getEvents();
        
        System.out.println( "Size " + events.size() );
        

        assertTrue("correct events", events.size() == 2 );
    }
    
    public void testPropertyChange() throws Exception {
        
        /*
        assertEquals("correct subnodes",
            new HashSet(Arrays.asList(new String[] {"one", "three"})),
            new HashSet(Arrays.asList(nodes2Names(nodes))));
         */
    }
    
    public void testChildrenAdded() throws Exception {
        Node[] chNodes = createTestNodes();
        Children ch = new Children.Array();
        ch.add( chNodes );
        
        Node n = new AbstractNode( ch );
        FilterNode fn = new FilterNode( n );
        n.setName( "X" );
        MyListener ml = new MyListener();
        
        fn.addNodeListener( ml );
        Node[] hold = fn.getChildren().getNodes();
        
        ch.add( new Node[] { new AbstractNode( Children.LEAF) } );
        
        List events = ml.getEvents();
        
        assertEquals("correct events", 1, events.size() );
    }
    
    
    private static Node[] createTestNodes() {
        
        Node[] tNodes = new Node[] {
            new AbstractNode( Children.LEAF ),
            new AbstractNode( Children.LEAF ),
            new AbstractNode( Children.LEAF )
        };
        
        tNodes[0].setName( "A" );
        tNodes[1].setName( "B" );
        tNodes[2].setName( "C" );
        
        return tNodes;
    }
    
    private static class MyListener implements NodeListener {
        
        ArrayList events = new ArrayList();
        
        
        /** Fired when a set of new children is added.
         * @param ev event describing the action
         *
         */
        public void childrenAdded(NodeMemberEvent ev) {
            events.add( ev );
        }
        
        /** Fired when a set of children is removed.
         * @param ev event describing the action
         *
         */
        public void childrenRemoved(NodeMemberEvent ev) {
            events.add( ev );
        }
        
        /** Fired when the order of children is changed.
         * @param ev event describing the change
         *
         */
        public void childrenReordered(NodeReorderEvent ev) {
            events.add( ev );
        }
        
        /** Fired when the node is deleted.
         * @param ev event describing the node
         *
         */
        public void nodeDestroyed(NodeEvent ev) {
            events.add( ev );
        }
        
        /** This method gets called when a bound property is changed.
         * @param evt A PropertyChangeEvent object describing the event source
         *   	and the property that has changed.
         *
         */
        public void propertyChange(PropertyChangeEvent ev) {
            events.add( ev );
        }
        
        public List getEvents() {
            return events;
        }
        
    }
}
