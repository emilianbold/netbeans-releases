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

import java.beans.PropertyVetoException;
import javax.swing.ListSelectionModel;
import javax.swing.tree.TreeSelectionModel;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.view.*;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Children.Array;

/*
 * Note: It's just helper test for investigate issue 37875 (failing RootContextTest)
 * Will be removed when issue 37875 is closed.
 *
 * @author  Jiri Rechtacek
 */
public class SelectNodeInListViewTest extends NbTestCase {
    
    public SelectNodeInListViewTest (java.lang.String testName) {
        super (testName);
    }
    
    public static void main (java.lang.String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
    
    public static Test suite () {
        TestSuite suite = new NbTestSuite (SelectNodeInListViewTest.class);
        return suite;
    }
    
    /** Run all tests in AWT thread */
    protected boolean runInEQ () {
        return true;
    }
    
    public void testSelectedNodeInListView () throws Exception {
    
        // helper variables
        Node[] arr1, arr2;
        Node root1, root2;
    
        arr1 = new Node [3];
        arr1[0] = new AbstractNode (Children.LEAF);
        arr1[0].setName ("One");
        
        arr1[1] = new AbstractNode (Children.LEAF);
        arr1[1].setName ("Two");
        
        arr1[2] = new AbstractNode (Children.LEAF);
        arr1[2].setName ("Three");
        
        Array ch1 = new Array ();
        ch1.add (arr1);
        
        arr2 = new Node [3];
        arr2[0] = new AbstractNode (Children.LEAF);
        arr2[0].setName ("Aaa");
        
        arr2[1] = new AbstractNode (Children.LEAF);
        arr2[1].setName ("Bbb");
        
        arr2[2] = new AbstractNode (Children.LEAF);
        arr2[2].setName ("Ccc");

        Array ch2 = new Array ();
        ch2.add (arr2);
        
        root1 = new AbstractNode (ch1);
        root1.setName ("Root1");
        root2 = new AbstractNode (ch2);
        root2.setName ("Root2");
        
        ListView view = new ListView ();
        view.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        ExplorerPanel panel = new ExplorerPanel ();
        ExplorerManager mgr = panel.getExplorerManager ();
        
        panel.add (view);
        panel.open ();
        
        Node[] selNodes = mgr.getSelectedNodes ();
        log ("Default root is " + mgr.getRootContext ());
        //assertNull ( "Root context is null", mgr.getRootContext ());
        assertEquals ("Count of the selected node is ", 0, selNodes.length);

        mgr.setRootContext (root1);
        log ("New root is " + mgr.getRootContext ());
        mgr.setSelectedNodes (new Node[] {arr1[0], arr1[2]});

        selNodes = mgr.getSelectedNodes ();
        assertEquals ("Root context is ", "Root1", mgr.getRootContext ().getName ());
        assertEquals ("Count of the selected node is ", 2, selNodes.length);
        
    }

}
