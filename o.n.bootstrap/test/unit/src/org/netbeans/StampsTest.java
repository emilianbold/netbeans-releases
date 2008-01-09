/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans;

import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class StampsTest extends NbTestCase {
    private File userdir;
    private File ide;
    private File platform;
    
    public StampsTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        platform = new File(getWorkDir(), "platform7");
        ide = new File(getWorkDir(), "ide8");
        userdir = new File(getWorkDir(), "tmp");
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath());
        System.setProperty("netbeans.user", userdir.getPath());
        
        createModule("org.openide.awt", platform, 50000L);
        createModule("org.openide.nodes", platform, 60000L);
        createModule("org.netbeans.api.languages", ide, 90000L);
        createModule("org.netbeans.modules.logmanagement", userdir, 10000L);
        
        Stamps.main("reset");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGenerateTimeStamps() {
        long stamp = Stamps.moduleJARs();
        assertEquals("Timestamp is taken from api.languages module", 90000L, stamp);
        
        assertStamp(60000L, platform);
        assertStamp(90000L, ide);
        assertStamp(-1L, userdir);
        
    }

    
    
    
    
    
    
    
    
    
    private static void assertStamp(long expectedValue, File cluster) {
        File stamp = new File(cluster, ".lastModified");
        
        if (expectedValue == -1L) {
            assertFalse("File shall not exist: " + stamp, stamp.exists());
        } else {
            assertTrue("File shall exist: " + stamp, stamp.exists());
            assertEquals("Modification time is good " + stamp, expectedValue, stamp.lastModified());
        }
    }

    private void createModule(String cnb, File cluster, long accesTime) throws IOException {
        String dashes = cnb.replace('.', '-');
        
        File config = new File(new File(new File(cluster, "config"), "Modules"), dashes + ".xml");
        File jar = new File(new File(cluster, "modules"), dashes + ".jar");
        
        config.getParentFile().mkdirs();
        jar.getParentFile().mkdirs();
        
        config.createNewFile();
        jar.createNewFile();
        config.setLastModified(accesTime);
        jar.setLastModified(accesTime);
    }

}
