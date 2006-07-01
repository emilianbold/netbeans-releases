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

import junit.framework.*;
import junit.textui.TestRunner;
import java.util.*;
import org.openide.nodes.*;

import org.netbeans.junit.*;

/** Tests whether notification to NodeListener is fired under Mutex.writeAccess
 *
 * @author Jaroslav Tulach
 */
public class NodeListenerTest extends NbTestCase {
    public NodeListenerTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(NodeListenerTest.class));
    }

    /** Creates a node with children, attaches a listener and tests whether
     * notifications are delivered under correct lock.
     */
    public void testCorrectMutexUsage () throws Exception {
        Children.Array ch = new Children.Array ();
        AbstractNode n = new AbstractNode (ch);
        
        class L extends Object implements NodeListener, Runnable {
            private boolean run;
            
            public void childrenAdded (NodeMemberEvent ev) {
                runNows ();
            }
            public void childrenRemoved (NodeMemberEvent ev) {
                runNows ();
            }
            public void childrenReordered(NodeReorderEvent ev) {
            }
            public void nodeDestroyed (NodeEvent ev) {
            }
            
            public void propertyChange (java.beans.PropertyChangeEvent ev) {
            }
            
            public void run () {
                run = true;
            }
            
            private void runNows () {
                L read = new L ();
                Children.MUTEX.postReadRequest (read);
                if (read.run) {
                    fail ("It is possible to run read access request");
                }
                
                L write = new L ();
                Children.MUTEX.postWriteRequest (write);
                if (!write.run) {
                    fail ("It is not possible to run write access request");
                }
            }
        }
        
        
        L l = new L ();
        
        n.addNodeListener (l);
        Node t = new AbstractNode (Children.LEAF);
        ch.add (new Node[] { t });
        
        ch.remove (new Node[] { t });
    }
}
