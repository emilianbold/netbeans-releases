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



public class RootsTest extends NbTestCase {
    private File userDir, platformDir, clusterDir;
    
    public RootsTest(String name) {
        super (name);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(RootsTest.class));
    }    
    
    
    protected void setUp () throws Exception {
        Favorites.ensureShadowsWork (null);
        
        super.setUp ();
        
        // initialize module system with all modules
        org.openide.util.Lookup.getDefault().lookup (
            org.openide.modules.ModuleInfo.class
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
    
    public void testRootNodeContainsAllFileSystemRoots () throws Exception {
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
    }
    
    public void testLinkToHomeDirIsThere () throws Exception {
        Node[] arr = Favorites.getNode ().getChildren ().getNodes (true);
        
        File home = new File (System.getProperty("user.home")).getCanonicalFile();

        HashSet folders = new HashSet ();
        for (int i = 0; i < arr.length; i++) {
            DataFolder f = (DataFolder)arr[i].getCookie(DataFolder.class);
            if (f == null) continue;
            
            folders.add (f);
            
            File file = org.openide.filesystems.FileUtil.toFile (
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

        if (roots.size () == File.listRoots().length) {
            fail ("Roots shall be present" + roots);
        }
    }
}
