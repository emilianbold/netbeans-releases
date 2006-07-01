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
import java.util.HashSet;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

public class RootsTest extends NbTestCase {
    private File userDir, platformDir, clusterDir;

    public RootsTest(String name) {
        super (name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(RootsTest.class));
    }    
    
    
    protected void setUp () throws Exception {
        super.setUp ();
        
        // initialize module system with all modules
        Lookup.getDefault().lookup (
            ModuleInfo.class
        );
        
/*
        // clear directories first
        this.clearWorkDir();
        
        userDir = new File (getWorkDir(), "user");
        assertTrue (userDir.mkdirs ());
        platformDir = new File (getWorkDir(), "platform");
        assertTrue (platformDir.mkdirs ());
        clusterDir = new File (getWorkDir (), "clstr");
        assertTrue (clusterDir.mkdirs ());
        
        System.setProperty("netbeans.home", platformDir.toString ());
        System.setProperty("netbeans.user", userDir.toString ());
        */
    }
    
    /* UI was changed. There are no more FS roots displayed in Favorites tab */
    /*public void testRootNodeContainsAllFileSystemRoots () throws Exception {
        HashSet roots = new HashSet ();
        {
            File[] arr = File.listRoots();
            for (int i = 0; i < arr.length; i++) {
                roots.add (arr[i]);
            }
        }
        
        Node[] arr = Favorites.getNode ().getChildren ().getNodes (true);
        
        for (int i = 0; i < arr.length; i++) {
            File f = Favorites.fileForNode (arr[i]);
            if (f != null) {
                roots.remove (f);
            }
        }

        if (!roots.isEmpty()) {
            fail (
                "All roots should be children, but these were missing:\n" + 
                roots +
                " this is the list of children nodes:\n" +
                Arrays.asList (arr)
            );
        }
    }*/
    
    public void testLinkToHomeDirIsThere () throws Exception {
        Node[] arr = Favorites.getNode ().getChildren ().getNodes (true);
        
        File home = new File (System.getProperty("user.home")).getCanonicalFile();

        HashSet folders = new HashSet ();
        for (int i = 0; i < arr.length; i++) {
            DataFolder f = (DataFolder)arr[i].getCookie(DataFolder.class);
            if (f == null) continue;
            
            folders.add (f);
            
            File file = FileUtil.toFile (
                f.getPrimaryFile()
            );
            assertNotNull ("All folders have files", file);
            
            file = file.getCanonicalFile();
            
            if (file.equals (home)) {
                return;
            }
        }
        
        fail ("none of the folders represent user home: " + home + "\n" + folders);
    }

    
    public void testNodesUnderRootRepresentTheirFiles () throws Exception {
        HashSet roots = new HashSet (Arrays.asList (File.listRoots()));
        
        Node[] arr = Favorites.getNode ().getChildren ().getNodes (true);
        
        for (int i = 0; i < arr.length; i++) {
            File f = Favorites.fileForNode (arr[i]);
            if (roots.remove (f)) {
                Node[] nexts = arr[i].getChildren().getNodes (true);
                for (int j = 0; j < nexts.length; j++) {
                    File file = Favorites.fileForNode (nexts[i]);
                    assertNotNull ("For node: " + nexts[i] + " there has to be file", file);
                    assertEquals ("Correct parent for " + nexts[i], f, file.getParentFile());
                }
            }
        }
    }
}
