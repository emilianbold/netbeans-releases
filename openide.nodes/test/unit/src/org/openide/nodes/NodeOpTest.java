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

package org.openide.nodes;

import java.awt.event.ActionEvent;
import java.beans.*;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;

/** Checking some of the behaviour of Node.
 * @author Jaroslav Tulach
 */
public class NodeOpTest extends NbTestCase {

    public NodeOpTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(NodeOpTest.class));
    }


    private static final class A extends AbstractAction {
        public void actionPerformed(ActionEvent ev) {
        }
    }
    
    public void testFindActions () throws Exception {
        class N extends AbstractNode {
            private Action[] arr;
            
            N (Action[] arr) {
                super (org.openide.nodes.Children.LEAF);
                this.arr = arr;
            }
            
            public Action[] getActions (boolean f) {
                return arr;
            }
        }
        
        Action[] arr = { new A(), new A(), new A(), new A() };
        
        assertArray (
            "Finding actions for one node is simple",
            arr, 
            NodeOp.findActions(new Node[] { new N (arr) })
        );

        assertArray (
            "Finding actions for two nodes with same actions",
            arr, 
            NodeOp.findActions(new Node[] { new N (arr), new N (arr) })
        );
            
        assertArray (
            "Finding actions for three nodes with same actions",
            arr, 
            NodeOp.findActions(new Node[] { new N (arr), new N (arr), new N (arr) })
        );
          
            
        assertArray (
            "Otherwise only common actions are taken",
            new Action[] { arr[3] }, 
            NodeOp.findActions(new Node[] { new N (arr), new N (new Action[] { arr[3], null }) })
        );
            
    }
    
    /**
     * Test that it is OK to return a different list each time.
     * There was a bug in NodeOp preventing this.
     */
    public void testFindActions2() throws Exception {
        class N extends AbstractNode {
            private Action a1 = new A();
            private Action a3 = new A();
            N() {
                super (org.openide.nodes.Children.LEAF);
            }
            public Action[] getActions(boolean f) {
                return new Action[] {
                    a1,
                    new A(),
                    a3,
                };
            }
        }
        Action[] actions = NodeOp.findActions(new Node[] {new N()});
        assertEquals("NodeOp.findActions does not gratuitously remove nonconstant actions", 3, actions.length);
    }
    
    private static void assertArray (String msg, Object[] a1, Object[] a2) {
        assertEquals(msg, Arrays.asList(a1), Arrays.asList(a2));
    }
}
