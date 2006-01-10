/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.derby;

import java.io.File;
import org.netbeans.modules.derby.test.TestBase;
import org.openide.filesystems.Repository;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author abadea
 */
public class DerbyOptionsTest extends TestBase {
    
    File userdir;
    File derby;
    
    public DerbyOptionsTest(String testName) {
        super(testName);
    }
    
    public void setUp() throws Exception {
        clearWorkDir();
        
        userdir = new File(getWorkDir(), ".netbeans");
        userdir.mkdirs();
        
        // create a fake installation of an external derby database
        derby = new File(userdir, "derby");
        derby.mkdir();
    }

    public void testDerbyLocationIsNullWhenBundledDerbyNotInstalled() {
        // assert the bundled derby is not installed
        assertNull(DerbyOptions.getDefaultInstallLocation());
        
        DerbyOptions.getDefault().setLocation(derby.getAbsolutePath());
        assertFalse(DerbyOptions.getDefault().isLocationNull());
        
        DerbyOptions.getDefault().setLocation("");
        assertTrue(DerbyOptions.getDefault().isLocationNull());
    }
    
    public void testDerbyLocationIsNotNullWhenBundledDerbyInstalled() throws Exception {
        // create a fake bundled derby database installation
        File bundledDerby = new File(userdir, DerbyOptions.INST_DIR);
        bundledDerby.mkdirs();
        File bundledDerbyLib = new File(bundledDerby, "lib");
        bundledDerbyLib.mkdir();
        new File(bundledDerbyLib, "derby.jar").createNewFile();
        
        // create a IFL which will find the bundled derby
        setLookup(new Object[] { new InstalledFileLocatorImpl(userdir) });
        
        // assert the bundled derby is installed
        String derbyLocation = DerbyOptions.getDefaultInstallLocation();
        assertNotNull(derbyLocation);
        
        DerbyOptions.getDefault().setLocation(derby.getAbsolutePath());
        assertFalse(DerbyOptions.getDefault().isLocationNull());
        
        DerbyOptions.getDefault().setLocation(""); // this should set the location to the one of the bundled derby
        assertFalse(DerbyOptions.getDefault().isLocationNull());
        assertEquals(DerbyOptions.getDefault().getLocation(), derbyLocation);
    }
    
    private static final class InstalledFileLocatorImpl extends InstalledFileLocator {
        
        private File userdir;
        
        public InstalledFileLocatorImpl(File userdir) {
            this.userdir = userdir;
        }
        
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            File f = new File(userdir, relativePath);
            return f.exists() ? f : null;
        }
    }
}
