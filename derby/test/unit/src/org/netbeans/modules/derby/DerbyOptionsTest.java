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

package org.netbeans.modules.derby;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.derby.test.TestBase;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author abadea
 */
public class DerbyOptionsTest extends TestBase {

    File userdir;
    File externalDerby;
    
    public DerbyOptionsTest(String testName) {
        super(testName);
    }

    public void setUp() throws Exception {
        clearWorkDir();

        userdir = new File(getWorkDir(), ".netbeans");
        userdir.mkdirs();
        
        // create a fake installation of an external derby database
        externalDerby = new File(userdir, "derby");
        createFakeDerbyInstallation(externalDerby);
    }

    public void testDerbyLocationIsNullWhenBundledDerbyNotInstalled() {
        // assert the bundled derby is not installed
        assertNull(DerbyOptions.getDefaultInstallLocation());
        
        DerbyOptions.getDefault().setLocation(externalDerby.getAbsolutePath());
        assertFalse(DerbyOptions.getDefault().isLocationNull());
        
        DerbyOptions.getDefault().setLocation("");
        assertTrue(DerbyOptions.getDefault().isLocationNull());
    }
    
    public void testDerbyLocationIsNotNullWhenBundledDerbyInstalled() throws Exception {
        // create a fake bundled derby database installation
        File bundledDerby = new File(userdir, DerbyOptions.INST_DIR);
        createFakeDerbyInstallation(bundledDerby);
        
        // create a IFL which will find the bundled derby
        setLookup(new Object[] { new InstalledFileLocatorImpl(userdir) });
        
        // assert the bundled derby is installed
        String derbyLocation = DerbyOptions.getDefaultInstallLocation();
        assertNotNull(derbyLocation);
        
        DerbyOptions.getDefault().setLocation(externalDerby.getAbsolutePath());
        assertFalse(DerbyOptions.getDefault().isLocationNull());
        
        DerbyOptions.getDefault().setLocation(""); // this should set the location to the one of the bundled derby
        assertFalse(DerbyOptions.getDefault().isLocationNull());
        assertEquals(DerbyOptions.getDefault().getLocation(), derbyLocation);
    }
    
    public void testLocationWhenNDSHPropertySetIssue76908() throws IOException {
        assertEquals("", DerbyOptions.getDefault().getSystemHome());
        
        File ndshSystemHome = new File(getWorkDir(), ".netbeans-derby-ndsh");
        if (!ndshSystemHome.mkdirs()) {
            throw new IOException("Could not create " + ndshSystemHome);
        }
        File systemHome = new File(getWorkDir(), ".netbeans-derby");
        if (!systemHome.mkdirs()) {
            throw new IOException("Could not create " + systemHome);
        }
        
        // returning the value of the netbeans.derby.system.home property when systemHome is not set...
        System.setProperty(DerbyOptions.NETBEANS_DERBY_SYSTEM_HOME, ndshSystemHome.getAbsolutePath());
        assertEquals(ndshSystemHome.getAbsolutePath(), DerbyOptions.getDefault().getSystemHome());
        
        // ... but returning systemHome when it is set
        DerbyOptions.getDefault().setSystemHome(systemHome.getAbsolutePath());
        assertEquals(systemHome.getAbsolutePath(), DerbyOptions.getDefault().getSystemHome());
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
