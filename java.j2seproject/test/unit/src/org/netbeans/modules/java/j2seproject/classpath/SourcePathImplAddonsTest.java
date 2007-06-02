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

package org.netbeans.modules.java.j2seproject.classpath;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 * Test's adding generate source in addons directory to source classpath.
 */
public class SourcePathImplAddonsTest extends NbTestCase{
    private static final String SRC_ROOT_1 = "generated/addons/srcroot1";  // No I18N
    private static final String SRC_ROOT_2 = "generated/addons/srcroot2";  // No I18N    
    private static final String SRC_ROOT_3 = "generated/addons/srcroot3";  // No I18N        
    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private ProjectManager pm;
    private J2SEProject pp;
    private AntProjectHelper helper;    

    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj001");
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.4"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj001",null,null); //NOI18N
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        pm = ProjectManager.getDefault();
        pp = pm.findProject(projdir).getLookup().lookup(J2SEProject.class);
        sources = projdir.getFileObject("src");
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        pm = null;
        super.tearDown();
    }

    public SourcePathImplAddonsTest(String testName) {
        super(testName);
    }
    
    private void assertContainsURL(List<ClassPath.Entry> list, URL url, boolean present){
        ClassPath.Entry cpe = null;        
        Iterator<ClassPath.Entry> itr = itr = list.iterator();
        
        if (present){
            boolean found = false;
            while (itr.hasNext()){
                cpe = itr.next();
                if (url.equals(cpe.getURL())){
                    found = true;
                }
            }   
            assertTrue(found);            
        } else {
            while (itr.hasNext()){
                cpe = itr.next();
                assertFalse(url.equals(cpe.getURL()));
            }            
        }
    }
    
    /**
     * Test's newly added source root appears in source classpath.
     **/
    public void testAddonsCreateSourceRoot () throws Exception {
        ClassPathProviderImpl cpProvider = pp.getLookup().lookup(ClassPathProviderImpl.class);
        ClassPath[] cps = cpProvider.getProjectClassPaths(ClassPath.SOURCE);
        ClassPath cp = cps[0];
        List<ClassPath.Entry> entries = cp.entries();
        assertNotNull ("Entries can not be null", entries);
        assertEquals("There must be src root", entries.get(0).getRoot(), sources);
        String buildDir = (String) J2SEProjectUtil.getEvaluatedProperty(pp,"${build.dir}"); // No I18N
        assertNotNull ("There is no build.dir property", buildDir);
        File addonModuleDir = new File (pp.getAntProjectHelper().resolveFile(buildDir), SRC_ROOT_1); 
        URL url = addonModuleDir.toURI().toURL();
        if (!addonModuleDir.exists()) {
            url = new URL (url.toExternalForm() + "/");
        }

        assertContainsURL(entries, url, false);
        
        addonModuleDir.mkdirs();
        // Simulate folder creation thru NB task.
        pp.getAntProjectHelper().resolveFileObject(buildDir); 
        Thread.sleep(1 * 1000); // Allow for event to propagate for a second.

        assertContainsURL(cp.entries(), url, true);        

    }

    /**
     * Test's deletion of source root also removes that root from the source classpath.
     * Since Deletion is recognized only after new folder creation event.
     **/    
    public void testAddonsRemoveSourceRoot () throws Exception {
        ClassPathProviderImpl cpProvider = pp.getLookup().lookup(ClassPathProviderImpl.class);
        ClassPath[] cps = cpProvider.getProjectClassPaths(ClassPath.SOURCE);
        ClassPath cp = cps[0];
        String buildDir = (String) J2SEProjectUtil.getEvaluatedProperty(pp,"${build.dir}"); // No I18N        
        File addonModuleDir1 = new File (pp.getAntProjectHelper().resolveFile(buildDir), SRC_ROOT_1); 
        URL url1 = addonModuleDir1.toURI().toURL();
        if (!addonModuleDir1.exists()) {
            url1 = new URL (url1.toExternalForm() + "/");
        }
        
        addonModuleDir1.mkdirs();
        // Simulate folder creation thru NB task.
        FileObject buildDirFO = pp.getAntProjectHelper().resolveFileObject(buildDir); 
        Thread.sleep(1 * 1000); // Allow for event to propagate for a second.
        assertContainsURL(cp.entries(), url1, true);        
        
        FileObject src1Fo = buildDirFO.getFileObject(SRC_ROOT_1);
        FileObject addonsFO = src1Fo.getParent();
        TestUtil.deleteRec(FileUtil.toFile(src1Fo));
        //src1Fo.delete();
        
        File addonModuleDir2 = new File (pp.getAntProjectHelper().resolveFile(buildDir), SRC_ROOT_2); 
        URL url2 = addonModuleDir2.toURI().toURL();
        if (!addonModuleDir2.exists()) {
            url2 = new URL (url2.toExternalForm() + "/");
        }
        
        addonsFO.createFolder("srcroot2"); // No I18N
        Thread.sleep(1 * 1000); // Allow for event to propagate for a second.           
        assertContainsURL(cp.entries(), url1, false);        
        assertContainsURL(cp.entries(), url2, true);                
    }
    /**
     * Test's newly added multiple source root appears in source classpath.
     **/
    public void testAddonsMultipleSourceRoot () throws Exception {
        ClassPathProviderImpl cpProvider = pp.getLookup().lookup(ClassPathProviderImpl.class);
        ClassPath[] cps = cpProvider.getProjectClassPaths(ClassPath.SOURCE);
        ClassPath cp = cps[0];
        List<ClassPath.Entry> entries = cp.entries();
        assertNotNull ("Entries can not be null", entries);
        assertEquals("There must be src root", entries.get(0).getRoot(), sources);
        String buildDir = (String) J2SEProjectUtil.getEvaluatedProperty(pp,"${build.dir}"); // No I18N
        assertNotNull ("There is no build.dir property", buildDir);
        File addonModuleDir1 = new File (pp.getAntProjectHelper().resolveFile(buildDir), SRC_ROOT_1); 
        File addonModuleDir2 = new File (pp.getAntProjectHelper().resolveFile(buildDir), SRC_ROOT_2); 
        File addonModuleDir3 = new File (pp.getAntProjectHelper().resolveFile(buildDir), SRC_ROOT_3);         
        
        URL url1 = addonModuleDir1.toURI().toURL();
        URL url2 = addonModuleDir2.toURI().toURL();
        URL url3 = addonModuleDir3.toURI().toURL();        
        
        if (!addonModuleDir1.exists()) {
            url1 = new URL (url1.toExternalForm() + "/");
        }

        if (!addonModuleDir2.exists()) {
            url2 = new URL (url2.toExternalForm() + "/");
        }

        if (!addonModuleDir3.exists()) {
            url3 = new URL (url3.toExternalForm() + "/");
        }

        assertContainsURL(entries, url1, false);
        assertContainsURL(entries, url2, false);
        assertContainsURL(entries, url3, false);        
        
        addonModuleDir1.mkdirs();
        addonModuleDir2.mkdirs();
        addonModuleDir3.mkdirs();
        
        // Simulate folder creation thru NB task.
        pp.getAntProjectHelper().resolveFileObject(buildDir); 
        Thread.sleep(1 * 1000); // Allow for event to propagate for a second.

        assertContainsURL(cp.entries(), url1, true);        
        assertContainsURL(cp.entries(), url2, true);        
        assertContainsURL(cp.entries(), url3, true);       
    }
}
