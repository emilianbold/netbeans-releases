/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.favorites;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.Action;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.nodes.Node;

public class NodesTest extends NbTestCase {
    private File userDir, platformDir, clusterDir;
    
    public NodesTest(String name) {
        super (name);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(NodesTest.class));
    }    
    
    
    protected void setUp () throws Exception {
        Favorites.ensureShadowsWork (null);
        
        super.setUp ();
        
        // initialize module system with all modules
        org.openide.util.Lookup.getDefault().lookup (
            org.openide.modules.ModuleInfo.class
        );
    }
    
    public void testNoneOfTheNodesHaveShadowLinks () throws Exception {
        doCheckDepth (Favorites.getNode (), 1);
    }
    
    private void doCheckDepth (Node node, int depth) throws Exception {
        //Limit test to 2 levels
        if (depth > 2) {
            return;
        }
        Node[] arr = node.getChildren().getNodes(true);
        Action add = Actions.add();
        Action remove = Actions.remove();
        
        for (int i = 0; i < arr.length; i++) {
            File f = Favorites.fileForNode(arr[i]);
            //First level (link) has action remove
            //Further level has action add
            Collection set = Arrays.asList (arr[i].getActions(false));
            if (depth == 1) {
                if (!set.contains (remove)) {
                    fail ("Node " + arr[i] + " does not contain action remove, but:\n" + set);
                }
                if (set.contains(add)) {
                    fail ("Node " + arr[i] + " contains action add.");
                }
            } else {
                if (!set.contains(add)) {
                    fail ("Node " + arr[i] + " does not contain action, but:\n" + set);
                }
                if (set.contains (remove)) {
                    fail ("Node " + arr[i] + " contains action remove.");
                }
            }
            
            doCheckDepth (arr[i], depth + 1);
        }
    }
}
