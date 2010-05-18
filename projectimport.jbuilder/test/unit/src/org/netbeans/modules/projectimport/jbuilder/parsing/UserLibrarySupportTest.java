/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
