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
import org.netbeans.modules.mobility.cldcplatform.PlatformConvertor;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

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
        GlobalAbilitiesCache abilities = GlobalAbilitiesCache.getDefault();
        assertNotNull(abilities);
        
        abilities.addAbility("Abil1");
        abilities.addAbility("Abil2");
        Object ab[]=abilities.getAllAbilities().toArray();
        assertEquals(ab.length,2);
        assertEquals(ab[0],"Abil1");
        assertEquals(ab[1],"Abil2");
        
        FileObject dir=Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject(PlatformConvertor.CFG_TEMPLATES_PATH);
        FileObject fo=FileUtil.toFileObject(getGoldenFile("Test_template.cfg"));
        fo.copy(dir,"Template","cfg");
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    
}
