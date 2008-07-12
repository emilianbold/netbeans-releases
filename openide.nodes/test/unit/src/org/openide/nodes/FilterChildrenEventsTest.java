/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
            ChildFactoryTest.assertNodeAndEvent(ev);
            events.add( ev );
        }
        
        /** Fired when a set of children is removed.
         * @param ev event describing the action
         *
         */
        public void childrenRemoved(NodeMemberEvent ev) {
            ChildFactoryTest.assertNodeAndEvent(ev);
            events.add( ev );
        }
        
        /** Fired when the order of children is changed.
         * @param ev event describing the change
         *
         */
        public void childrenReordered(NodeReorderEvent ev) {
            ChildFactoryTest.assertNodeAndEvent(ev);
            events.add( ev );
        }
        
        /** Fired when the node is deleted.
         * @param ev event describing the node
         *
         */
        public void nodeDestroyed(NodeEvent ev) {
            ChildFactoryTest.assertNodeAndEvent(ev);
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
