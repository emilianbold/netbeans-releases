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

package org.netbeans.modules.favorites;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.Action;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

public class NodesTest extends NbTestCase {
    private File userDir, platformDir, clusterDir;

    public NodesTest(String name) {
        super (name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(NodesTest.class));
    }    
    
    
    protected void setUp () throws Exception {
        super.setUp ();
        
        // initialize module system with all modules
        Lookup.getDefault().lookup (
            ModuleInfo.class
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
