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
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class StampsIdeLessThanPlatformTest extends NbTestCase {
    private File userdir;
    private File ide;
    private File platform;
    private File install;
    
    
    public static Test suite() {
        //return new StampsTest("testStampsInvalidatedWhenClustersChange");
        return new NbTestSuite(StampsIdeLessThanPlatformTest.class);
    }
    
    public StampsIdeLessThanPlatformTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform7");
        ide = new File(install, "ide8");
        userdir = new File(getWorkDir(), "tmp");
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath());
        System.setProperty("netbeans.user", userdir.getPath());
        
        StampsTest.createModule("org.openide.awt", platform, 50000L);
        StampsTest.createModule("org.openide.nodes", platform, 60000L);
        StampsTest.createModule("org.netbeans.api.languages", ide, 50000L);
        StampsTest.createModule("org.netbeans.modules.logmanagement", userdir, 10000L);
        
        Stamps.main("reset");
        
        Thread.sleep(100);

        Logger l = Logger.getLogger("org");
        l.setLevel(Level.OFF);
        l.setUseParentHandlers(false);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGenerateTimeStamps() {
        long stamp = Stamps.moduleJARs();
        assertEquals("Timestamp is taken from nodes module", 60000L, stamp);
        
        StampsTest.assertStamp(60000L, platform, false, true);
        StampsTest.assertStamp(50000L, ide, false, true);
        StampsTest.assertStamp(-1L, userdir, false, false);
    }        
    

}
