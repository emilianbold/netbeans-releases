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

package org.netbeans.modules.web.freeform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 * Tests for FreeformProjectGenerator.
 *
 * @author Pavel Buzek
 */
public class WebProjectGeneratorTest extends NbTestCase {

    private File lib1;
    private File lib2;
    private File src;
    private File web;
    private File doc;
    private File buildClasses;
    
    public WebProjectGeneratorTest(String testName) {
        super(testName);
    }
    
        protected void setUp() throws Exception {
            super.setUp();
            Lookup.getDefault().lookup(ModuleInfo.class);
        }
    
    private AntProjectHelper createEmptyProject(String projectFolder, String projectName) throws Exception {
        File base = new File(getWorkDir(), projectFolder);
        base.mkdir();
        File antScript = new File(base, "build.xml");
        antScript.createNewFile();
        src = new File(base, "src");
        src.mkdir();
        web = new File(base, "web");
        web.mkdir();
        doc = new File(base, "doc");
        doc.mkdir();
        buildClasses = new File(base, "buildClasses");
        buildClasses.mkdir();
        File libs = new File(base, "libs");
        libs.mkdir();
        lib1 = new File(libs, "some.jar");
        createRealJarFile(lib1);
        lib2 = new File(libs, "some2.jar");
        createRealJarFile(lib2);
        
        ArrayList webModules = new ArrayList ();
        WebProjectGenerator.WebModule wm = new WebProjectGenerator.WebModule ();
        wm.docRoot = web.getAbsolutePath();
        wm.contextPath = "/context";
        wm.j2eeSpecLevel = WebModule.J2EE_14_LEVEL;
        wm.classpath = base.getAbsolutePath() + "/buildClasses:" + lib1.getAbsolutePath();
        webModules.add (wm);
        
        AntProjectHelper helper = FreeformProjectGenerator.createProject(base, base, projectName, null);
        WebProjectGenerator.putWebModules(helper, Util.getAuxiliaryConfiguration(helper), webModules);
        return helper;
    }
    
    public void testWebModules () throws Exception {
        clearWorkDir();
        AntProjectHelper helper = createEmptyProject("proj2", "proj-2");
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        WebModule wm = WebModule.getWebModule(FileUtil.toFileObject(web));
        assertNotNull("WebModule not found", wm);
        assertEquals("correct document base", FileUtil.toFileObject(web), wm.getDocumentBase());
        assertEquals("correct j2ee version", WebModule.J2EE_14_LEVEL, wm.getJ2eePlatformVersion());
        assertEquals("correct context path", "/context", wm.getContextPath());
        WebModule wm2 = WebModule.getWebModule(FileUtil.toFileObject (src));
//        assertNotNull("WebModule not found", wm2);
//        assertEquals("the same wm for web and src folder", wm, wm2);
    }
    
    public void test2WebModules () throws Exception {
        clearWorkDir();
        AntProjectHelper helper = createEmptyProject("proj6", "proj-6");
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        File src2 = FileUtil.toFile (base.createFolder("src2"));
        File web2 = FileUtil.toFile (base.createFolder("web2"));

        AuxiliaryConfiguration aux = Util.getAuxiliaryConfiguration(helper);
        List webModules = WebProjectGenerator.getWebmodules(helper, aux);
        WebProjectGenerator.WebModule wm = new WebProjectGenerator.WebModule ();
        wm.docRoot = web2.getAbsolutePath();
        wm.contextPath = "/context2";
        wm.j2eeSpecLevel = WebModule.J2EE_13_LEVEL;
        wm.classpath = FileUtil.toFile (base).getAbsolutePath() + "/buildClasses2:" + lib2.getAbsolutePath();
        webModules.add (wm);
        WebProjectGenerator.putWebModules(helper, aux, webModules);
        ProjectManager.getDefault().saveProject(p);
        
        WebModule webModule = WebModule.getWebModule(base.getFileObject("web2"));
        assertNotNull("WebModule not found", webModule);
        assertEquals("correct document base", base.getFileObject("web2"), webModule.getDocumentBase());
        WebModule webModule2 = WebModule.getWebModule(base.getFileObject("src2"));
//        assertNotNull("WebModule not found", webModule2);
//        assertEquals("correct document base", webModule, webModule2);
    }
    
    // create real Jar otherwise FileUtil.isArchiveFile returns false for it
    public void createRealJarFile(File f) throws Exception {
        OutputStream os = new FileOutputStream(f);
        try {
            JarOutputStream jos = new JarOutputStream(os);
            JarEntry entry = new JarEntry("foo.txt");
            jos.putNextEntry(entry);
            jos.flush();
            jos.close();
        } finally {
            os.close();
        }
    }
    
}
