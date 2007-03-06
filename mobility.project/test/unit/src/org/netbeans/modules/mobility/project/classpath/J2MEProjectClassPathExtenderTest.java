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
import org.netbeans.modules.masterfs.MasterFileSystem;
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
        assertNotNull(MasterFileSystem.settingsFactory(null));
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
        assertEquals(s,"${file.reference.MIDletSuite.jar}");
        // add again to extend coverage
        assertFalse(instance.addArchiveFile(archiveFile));
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
        for (int i=0;i<locs.length;i++) {
            boolean result = instance.addAntArtifact(art[0], locs[i]);
            assertTrue(result);
            // add again to extend coverage
            assertFalse(instance.addAntArtifact(art[0], locs[i]));
        }
        EditableProperties ep=aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String s=ep.getProperty("libs.classpath");
        assertTrue(s.indexOf("${reference.testProject.jar}")!=-1);
        
    }
}
