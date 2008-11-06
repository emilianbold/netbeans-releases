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
 * J2MEProjectClassPathExtenderTest.java
 * JUnit based test
 *
 * Created on 10 February 2006, 16:37
 */
package org.netbeans.modules.mobility.project.classpath;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URI;
import junit.framework.*;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.project.libraries.DefaultLibraryImplementation;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.mobility.project.TestUtil;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lukas
 */
public class J2MEProjectClassPathExtenderTest extends NbTestCase {
    static AntProjectHelper aph=null;
    static J2MEProjectClassPathExtender instance = null;
    static FileObject projDir = null;
    
    static
    {
        TestUtil.setLookup( new Object[] {            
        }, J2MEProjectClassPathExtenderTest.class.getClassLoader());
    }
    
    public J2MEProjectClassPathExtenderTest(String testName) {
        super(testName);
        
        TestUtil.setEnv();
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        File workDir = getWorkDir();
        File proj = new File(workDir, "testProject");
        
        System.setProperty("netbeans.user","test/tiredTester");
        
        aph = J2MEProjectGenerator.createNewProject(proj, "testProject", null, null,null);
        projDir=FileUtil.toFileObject(proj);
        Project p=ProjectManager.getDefault().findProject(projDir);
        assertNotNull(p);
        ReferenceHelper refs = p.getLookup().lookup(ReferenceHelper.class);
        ProjectConfigurationsHelper pcfgs = p.getLookup().lookup(ProjectConfigurationsHelper.class);
        assertNotNull(refs);
        instance=new J2MEProjectClassPathExtender(p,aph,refs,pcfgs);
        assertNotNull(instance);
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(J2MEProjectClassPathExtenderTest.class);
        
        return suite;
    }
    
    /**
     * Test of addLibrary method, of class org.netbeans.modules.mobility.project.classpath.J2MEProjectClassPathExtender.
     */
    /*
    public void testAddLibrary() throws Exception {
        System.out.println("addLibrary");
        
        //Create a Library object
        Class cl=Class.forName("org.netbeans.api.project.libraries.Library");
        Class par[]=new Class[] {org.netbeans.spi.project.libraries.LibraryImplementation.class };
        Constructor c=cl.getDeclaredConstructor(par);
        DefaultLibraryImplementation dli=new DefaultLibraryImplementation("Test1", new String[] {"type1","type2"});
        dli.setName("libtest");
        Object p[]=new Object[] {dli};
        c.setAccessible(true);
        Library lib=(Library)c.newInstance(p);
        
        
        assertNotNull(lib);
        boolean result = instance.addLibrary(lib);
        assertTrue(result);
        EditableProperties ep=aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String s=ep.getProperty("libs.classpath");
        assertEquals(s,"${libs.libtest.classpath}");
    }
     */
    
    /**
     * Test of addArchiveFile method, of class org.netbeans.modules.mobility.project.classpath.J2MEProjectClassPathExtender.
     */
    public void testAddArchiveFile() throws Exception {
        System.out.println("addArchiveFile");
        
        File jar=getGoldenFile("MIDletSuite.jar");
        FileObject archiveFile =FileUtil.toFileObject(jar);
        
        boolean result = instance.addArchiveFile(archiveFile);
        assertTrue(result);
        EditableProperties ep=aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String s=ep.getProperty("libs.classpath");
//        assertEquals(s,"${file.reference.MIDletSuite.jar}");
        // add again to extend coverage
//        assertFalse(instance.addArchiveFile(archiveFile));
    }
    
    /**
     * Test of addAntArtifact method, of class org.netbeans.modules.mobility.project.classpath.J2MEProjectClassPathExtender.
     */
    public void testAddAntArtifact() throws Exception {
        System.out.println("addAntArtifact");
        Project p=ProjectManager.getDefault().findProject(projDir);
        assertNotNull(p);
        AntArtifactProvider refs = p.getLookup().lookup(AntArtifactProvider.class);
        
        
        AntArtifact art[]=refs.getBuildArtifacts();
        assertNotNull(art);
        assertTrue(art.length==1);
        URI locs[]=art[0].getArtifactLocations();
        assertNotNull(locs);
        assertTrue(art.length>0);
//        for (int i=0;i<locs.length;i++) {
//            boolean result = instance.addAntArtifact(art[0], locs[i]);
//            assertTrue(result);
//            // add again to extend coverage
//            assertFalse(instance.addAntArtifact(art[0], locs[i]));
//        }
//        EditableProperties ep=aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//        String s=ep.getProperty("libs.classpath");
//        assertTrue(s.indexOf("${reference.testProject.jar}")!=-1);
        
    }
}
