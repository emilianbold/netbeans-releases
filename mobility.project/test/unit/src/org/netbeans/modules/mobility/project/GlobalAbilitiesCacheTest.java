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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * GlobalAbilitiesCacheTest.java
 * JUnit based test
 *
 * Created on 06 February 2006, 17:51
 */
package org.netbeans.modules.mobility.project;

import java.io.File;
import java.io.IOException;
import junit.framework.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lukas
 */
public class GlobalAbilitiesCacheTest extends NbTestCase {
    
    static
    {
        TestUtil.setLookup( new Object[] {
            TestUtil.testProjectFactory(),
            TestUtil.testFileLocator(),
            TestUtil.testProjectChooserFactory(),
        }, GlobalAbilitiesCacheTest.class.getClassLoader());
    }
    
    public GlobalAbilitiesCacheTest(String testName) {
        super(testName);
        TestUtil.setEnv();
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        File workDir = getWorkDir();
        File proj = new File(workDir, "testProject");
        
        J2MEProjectGenerator.createNewProject(proj, "testProject", null, null,null);
        OpenProjects.getDefault().open(new Project[] {ProjectManager.getDefault().findProject(FileUtil.toFileObject(proj))},true);
    }
    
    protected void tearDown() throws Exception {
        
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(GlobalAbilitiesCacheTest.class);
        return suite;
    }
    
    public void testJustForCoverage() throws Exception {
        Class.forName("org.netbeans.modules.mobility.project.GlobalAbilitiesCache$1");
    }
    
    /**
     * Test of getAllAbilities method, of class org.netbeans.modules.mobility.project.GlobalAbilitiesCache.
     */
    public void testAbilities() throws IOException {
        System.out.println("Abilities");
        System.setProperty("netbeans.user","test/tiredTester");
        FileObject root=FileUtil.getConfigRoot();
        FileObject fo;
        FileObject dir=root.getFileObject(UserConfigurationTemplatesProvider.CFG_TEMPLATES_PATH);
        if (dir == null)
        {
            if (root.getFileObject("Templates") == null)
                root.createFolder("Templates");
            if (root.getFileObject(UserConfigurationTemplatesProvider.CFG_TEMPLATES_PATH) == null)
                root.getFileObject("Templates").createFolder("J2MEProjectConfigurations");
            dir=root.getFileObject(UserConfigurationTemplatesProvider.CFG_TEMPLATES_PATH);
        }
        else if ((fo=dir.getFileObject("Template.cfg")) != null)
        {
            fo.delete();
        }
        GlobalAbilitiesCache abilities = GlobalAbilitiesCache.getDefault();
        assertNotNull(abilities);
        
        abilities.addAbility("Abil1");
        abilities.addAbility("Abil2");
        Object ab[]=abilities.getAllAbilities().toArray();
        assertEquals(ab.length,2);
        assertEquals(ab[0],"Abil1");
        assertEquals(ab[1],"Abil2");
        
        
        fo=FileUtil.toFileObject(getGoldenFile("Test_template.cfg"));
        fo.copy(dir,"Template","cfg");
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    
}
