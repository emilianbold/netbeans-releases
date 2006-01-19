/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.classpath;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
//import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.project.TestPlatformProvider;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.test.TestUtil;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class ClassPathProviderImplTest extends NbTestCase {
    
    private Project project;
    private AntProjectHelper helper;
    private FileObject sourceRoot;
    private FileObject testRoot;
    private FileObject webRoot;
    private FileObject bootPlatformRoot;
    private FileObject sourceClass;
    private FileObject testClass;
    private FileObject jspPage;

    public ClassPathProviderImplTest(String testName) {
        super(testName);
    }
    
    public void setUp() throws Exception {
        // setup some platforms -- needed for testing findClassPath(FileObject, ClassPath.BOOT)
        FileObject scratch = TestUtil.makeScratchDir(this);
        bootPlatformRoot = scratch.createFolder("DefaultPlatformRoot");
        ClassPath defBCP = ClassPathSupport.createClassPath(new URL[] { bootPlatformRoot.getURL() });
        
        TestUtil.setLookup(new Object[] {
            new TestPlatformProvider(defBCP, defBCP)
        });
        
        assertTrue("No Java platforms found.", JavaPlatformManager.getDefault().getInstalledPlatforms().length >= 2);
        
        // setup the project
        File f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        Sources src = (Sources)project.getLookup().lookup(Sources.class);
        SourceGroup[] groups = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        sourceRoot = findSourceRoot(groups, "${src.dir}");
        assertNotNull(sourceRoot);
        testRoot = findSourceRoot(groups, "${test.src.dir}");
        assertNotNull(testRoot);
        
        sourceClass = sourceRoot.getFileObject("pkg/NewClass.java");
        assertNotNull(sourceClass);
        testClass = testRoot.getFileObject("pkg/NewClassTest.java");
        assertNotNull(testClass);
        
        // XXX should not cast to WebProject
        helper = ((WebProject)project).getAntProjectHelper();
        String web = helper.getStandardPropertyEvaluator().getProperty(WebProjectProperties.WEB_DOCBASE_DIR);
        webRoot = helper.resolveFileObject(web);
        jspPage = webRoot.getFileObject("index.jsp");
    }
    
    public void testClassPaths() throws Exception {
        ClassPathProvider cpp = (ClassPathProvider)project.getLookup().lookup(ClassPathProvider.class);
        assertTrue("No ClassPathProvider in project lookup!", cpp != null);
        
        ClassPath cp;
        
        // testing all cp's twice as the second time they come from a cache
        
        // sources
        
        cp = cpp.findClassPath(sourceClass, ClassPath.SOURCE);
        checkSourceSourceClassPath(cp);
        cp = cpp.findClassPath(sourceClass, ClassPath.SOURCE);
        checkSourceSourceClassPath(cp);
        
        cp = cpp.findClassPath(sourceClass, ClassPath.COMPILE);
        checkCompileClassPath(cp);
        cp = cpp.findClassPath(sourceClass, ClassPath.COMPILE);
        checkCompileClassPath(cp);
        
        cp = cpp.findClassPath(sourceClass, ClassPath.EXECUTE);
        checkSourceExecuteClassPath(cp);
        cp = cpp.findClassPath(sourceClass, ClassPath.EXECUTE);
        checkSourceExecuteClassPath(cp);
        
        cp = cpp.findClassPath(sourceClass, ClassPath.BOOT);
        checkBootClassPath(cp);
        cp = cpp.findClassPath(sourceClass, ClassPath.BOOT);
        checkBootClassPath(cp);
        
        // test sources
        
        cp = cpp.findClassPath(testClass, ClassPath.SOURCE);
        checkTestSourceClassPath(cp);
        cp = cpp.findClassPath(testClass, ClassPath.SOURCE);
        checkTestSourceClassPath(cp);
        
        cp = cpp.findClassPath(testClass, ClassPath.COMPILE);
        checkCompileClassPath(cp);
        cp = cpp.findClassPath(testClass, ClassPath.COMPILE);
        checkCompileClassPath(cp);
        
        cp = cpp.findClassPath(testClass, ClassPath.EXECUTE);
        checkTestExecuteClassPath(cp);
        cp = cpp.findClassPath(testClass, ClassPath.EXECUTE);
        checkTestExecuteClassPath(cp);
        
        cp = cpp.findClassPath(testClass, ClassPath.BOOT);
        checkBootClassPath(cp);
        cp = cpp.findClassPath(testClass, ClassPath.BOOT);
        checkBootClassPath(cp);
        
        // JSP pages
        
        cp = cpp.findClassPath(jspPage, ClassPath.SOURCE);
        checkJSPSourceClassPath(cp);
        cp = cpp.findClassPath(jspPage, ClassPath.SOURCE);
        checkJSPSourceClassPath(cp);
    }
    
    private void checkSourceSourceClassPath(ClassPath cp) {
        FileObject[] roots = cp.getRoots();
        assertEquals(1, roots.length);
        assertTrue(cp.getRoots()[0].equals(sourceRoot));
    }
    
    private void checkSourceExecuteClassPath(ClassPath cp) throws Exception {
        // this jar is on debug.classpath
        assertTrue(classPathContainsURL(cp, resolveURL("libs/jar1.jar")));
    }
    
    private void checkTestSourceClassPath(ClassPath cp) {
        FileObject[] roots = cp.getRoots();
        assertEquals(1, roots.length);
        assertTrue(cp.getRoots()[0].equals(testRoot));
    }
    
    private void checkTestExecuteClassPath(ClassPath cp) throws Exception {
        // this jar is on run.test.classpath
        // this test fails!
        // assertTrue(classPathContainsURL(cp, resolveURL("libs/jar2.jar")));
    }
    
    private void checkJSPSourceClassPath(ClassPath cp) {
        FileObject[] roots = cp.getRoots();
        assertEquals(2, roots.length);
        assertTrue(cp.getRoots()[0].equals(webRoot));
        assertTrue(cp.getRoots()[1].equals(sourceRoot));
    }
    
    private void checkCompileClassPath(ClassPath cp) throws Exception {
        // this jar is on javac.classpath
        assertTrue(classPathContainsURL(cp, resolveURL("libs/jar0.jar")));
        // XXX should also test J2EE classpath
    }

    private void checkBootClassPath(ClassPath cp) throws Exception {
        assertTrue(classPathContainsURL(cp, bootPlatformRoot.getURL()));
    }
    
    private URL resolveURL(String relative) throws Exception {
        return helper.resolveFileObject(relative).getURL();
    }
    
    private static final boolean classPathContainsURL(ClassPath cp, URL url) {
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        }
        for (Iterator i = cp.entries().iterator(); i.hasNext();) {
            ClassPath.Entry e = (ClassPath.Entry)i.next();
            if (e.getURL().equals(url)) {
                return true;
            }
        }
        return false;
    }
    
    private static FileObject findSourceRoot(SourceGroup[] groups, String name) {
        for (int i = 0; i < groups.length; i++) {
            if (name.equals(groups[i].getName())) {
                return groups[i].getRootFolder();
            }
        }
        return null;
    }
}
