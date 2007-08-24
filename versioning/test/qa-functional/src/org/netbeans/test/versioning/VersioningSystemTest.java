/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.versioning;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.textui.TestRunner;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.versioning.VersioningManager;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test Compatibility Kit xtest class.
 * 
 * @author Maros Sandor
 */
public class VersioningSystemTest extends JellyTestCase {
    
    private File    propertiesFile;
    private String  versioningSystemClassName;
    private File    rootDir;
    private VersioningSystem testedSystem;

    public VersioningSystemTest(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new VersioningSystemTest("testOwnership"));
        suite.addTest(new VersioningSystemTest("testInterceptor"));
        return suite;
    }

    protected void setUp() throws Exception {
        super.setUp();
        propertiesFile = new File(getDataDir(), "tck.properties");
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(propertiesFile);
        props.load(fis);
        versioningSystemClassName = props.getProperty("test.vcs");
        rootDir = new File(props.getProperty("test.root"));

        testedSystem = VersioningManager.getInstance().getOwner(rootDir);
        assertNotNull(testedSystem);
        assertEquals(testedSystem.getClass().getName(), versioningSystemClassName);
    }

    public void testInterceptor() throws IOException {
        File newFile = new File(rootDir, "vcs-tck-created.txt");
        assertFalse(newFile.exists());
        FileObject fo = FileUtil.toFileObject(rootDir);

        // test creation
        FileObject newfo = fo.createData("vcs-tck-created.txt");
        
        sleep(1000);

        // test delete
        newfo.delete();
    }
    
    public void testOwnership() throws IOException {
        VersioningSystem vs;

        vs = VersioningManager.getInstance().getOwner(rootDir.getParentFile());
        assertNull(vs);

        testOwnershipRecursively(rootDir);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(VersioningSystemTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void testOwnershipRecursively(File dir) {
        VersioningSystem vs = VersioningManager.getInstance().getOwner(dir);
        assertEquals(testedSystem, vs);
        File [] children = dir.listFiles();
        if (children == null) return;
        for (File child : children) {
            testOwnershipRecursively(child);
        }
    }
}
