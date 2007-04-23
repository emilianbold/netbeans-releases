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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows;

import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import org.openide.windows.*;


/** 
 * 
 * @author Dafe Simonek
 */
public class Bug82319Test extends NbTestCase {

    public Bug82319Test (String name) {
        super (name);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(Bug82319Test.class);
        
        return suite;
    }

    protected boolean runInEQ () {
        return true;
    }
     
    public void test82319ActivatedNodesUpdate () throws Exception {
        Node node1 = new AbstractNode(Children.LEAF);
        Node node2 = new AbstractNode(Children.LEAF);
        
        Mode mode = WindowManagerImpl.getInstance().createMode("test82319Mode",
                Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        
        TopComponent tc1 = new TopComponent();
        tc1.setActivatedNodes(new Node[] { node1 });
        mode.dockInto(tc1);
        
        TopComponent tc2 = new TopComponent();
        tc2.setActivatedNodes(null);
        mode.dockInto(tc2);
        
        tc1.open();
        tc2.open();
        
        tc1.requestActive();
        
        System.out.println("Checking bugfix 82319...");
        Node[] actNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        assertTrue("Expected 1 activated node, but got " + actNodes.length, actNodes.length == 1);
        assertSame("Wrong activated node", actNodes[0], node1);

        tc2.requestActive();
        
        // activated nodes should stay the same, tc2 doesn't have any
        actNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        assertTrue("Expected 1 activated node, but got " + actNodes.length, actNodes.length == 1);
        assertSame("Wrong activated node", actNodes[0], node1);
        
        tc1.setActivatedNodes(new Node[] { node2 });
        
        System.out.println("Checking update of activated nodes...");
        // activated nodes should change, as still nodes should be grabbed from tc1 
        actNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        assertTrue("Expected 1 activated node, but got " + actNodes.length, actNodes.length == 1);
        assertSame("Wrong activated node", actNodes[0], node2);
    }
    
}
