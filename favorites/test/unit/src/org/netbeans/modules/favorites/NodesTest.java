/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.favorites;

import java.lang.ref.*;
import java.util.*;

//import junit.framework.*;
import org.netbeans.junit.*;

import java.util.List;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.beans.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.*;
import java.util.jar.Manifest;
import java.util.regex.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

//import org.openide.ErrorManager;



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
        if (depth == 0) return;
        
        Node[] arr = Favorites.getNode ().getChildren ().getNodes (true);
        javax.swing.Action add = Actions.add ();
        javax.swing.Action remove = Actions.remove ();
        
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].getDisplayName().indexOf ("->") >= 0) {
                fail ("Node " + arr[i] + " contains shadow indicator");
            }
            
            File f = Favorites.fileForNode(arr[i]);
            // everything else than roots need to have actions 
            Collection set = Arrays.asList (arr[i].getActions (false));
            if (!set.contains(add) || !set.contains (remove)) {
                fail ("Node " + arr[i] + " does not contain actions add and remove, but:\n" + set);
            }
            
            doCheckDepth (arr[i], depth - 1);
        }
    }
}
