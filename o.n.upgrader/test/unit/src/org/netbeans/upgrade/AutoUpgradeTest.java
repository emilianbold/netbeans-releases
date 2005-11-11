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

package org.netbeans.upgrade;
import java.io.File;
import java.net.URL;
import java.util.*;

import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;

/** Tests copying of attributes during upgrade when .nbattrs file is stored on the
 * local filesystem while the respective fileobject is stored on the XML filesystem.
 *
 * @author sherold
 */
public final class AutoUpgradeTest extends org.netbeans.junit.NbTestCase {
    public AutoUpgradeTest (String name) {
        super (name);
    }
    
    protected void setUp() throws java.lang.Exception {
        super.setUp();
    }
    
    
    public void testDoUpgrade() throws Exception {
        File wrkDir = getWorkDir();
        clearWorkDir();
        File old = new File(wrkDir, "old");
        old.mkdir();
        File config = new File(old, "config");
        config.mkdir();
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(config);
        // filesystem must not be empty, otherwise .nbattrs file will be deleted :(
        lfs.getRoot().createFolder("test");
        
        String oldVersion = "foo";
        
        URL url = AutoUpgradeTest.class.getResource("layer" + oldVersion + ".xml");
        XMLFileSystem xmlfs = new XMLFileSystem(url);
        
        MultiFileSystem mfs = new MultiFileSystem(
                new FileSystem[] { lfs, xmlfs }
        );
        
        String fooBar = "/foo/bar";
        
        FileObject fooBarFO = mfs.findResource(fooBar);
        String attrName = "color";
        String attrValue = "black";
        fooBarFO.setAttribute(attrName, attrValue);
        
        System.setProperty("netbeans.user", new File(wrkDir, "new").getAbsolutePath());
        
        AutoUpgrade.doUpgrade(old, oldVersion);
        
        FileSystem dfs = Repository.getDefault().getDefaultFileSystem();
        
        MultiFileSystem newmfs = new MultiFileSystem(
                new FileSystem[] { dfs, xmlfs }
        );
        
        FileObject newFooBarFO = newmfs.findResource(fooBar);
        assertNotNull(newFooBarFO);
        assertEquals(attrValue, newFooBarFO.getAttribute(attrName));
    }
 }
