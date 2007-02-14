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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.jbuilder.parsing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.projectimport.j2seimport.AbstractProject;

/**
 *
 * @author Radek Matous
 */
public class UserLibrarySupportTest extends NbTestCase {
    private static final String[] reachable_libs = {"MainTest.library","RegularDepsTest.library"};
    private static final String[] un_reachable_libs = {"RegularDepsTest2.library", "CyclicDepsTest.library"};
    File libDir;

    
    static {
        System.setProperty("projectimport.logging.level", "WARNING");
    }
    
    public UserLibrarySupportTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        UserLibrarySupport.setInstallDirLib(null);
        UserLibrarySupport.setUserHomeLib(null);
        libDir = new File(getWorkDir(), getName());
        FileUtil.createFolder(libDir);
        assertTrue(libDir.exists());

        for (int i = 0; i < reachable_libs.length; i++) {
            File f = new File(getWorkDir(), reachable_libs[i]);
            InputStream is = getClass().getResourceAsStream(reachable_libs[i]);
            OutputStream os = new FileOutputStream(f);
            FileUtil.copy(is, os);
            is.close();os.close();
            assertTrue(f.exists());
        }
        
        for (int i = 0; i < un_reachable_libs.length; i++) {
            File f = new File(libDir, un_reachable_libs[i]);
            InputStream is = getClass().getResourceAsStream(un_reachable_libs[i]);
            OutputStream os = new FileOutputStream(f);
            FileUtil.copy(is, os);
            is.close();os.close();
            assertTrue(f.exists());
        }                
    }
    
    public void testInstallLibs() throws Exception {
        libDirs(true);
    }

    public void testHomeLibs() throws Exception {
        libDirs(false);
    }
    
    public void libDirs(boolean install) throws Exception {
        AbstractProject.UserLibrary ul = UserLibrarySupport.getInstance("MainTest", getWorkDir());
        assertNotNull(ul);
        assertEquals(1, ul.getDependencies().size());
        if (install) {
            UserLibrarySupport.setInstallDirLib(libDir);
        } else {
            UserLibrarySupport.setUserHomeLib(libDir);
        }
        ul = UserLibrarySupport.getInstance("MainTest", getWorkDir());
        assertNotNull(ul);
        assertEquals(3, ul.getDependencies().size());
    }    
}
