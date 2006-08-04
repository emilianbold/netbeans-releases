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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectType;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectTest;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

// XXX test GPR usage

/**
 * Test functionality of {@link ClassPathProviderImpl}.
 * @author Jesse Glick
 */
public class ClassPathProviderImplTest extends TestBase {
    
    public ClassPathProviderImplTest(String name) {
        super(name);
    }
    
    private File copyOfSuite2;
    private FileObject copyOfMiscDir;
    private NbModuleProject copyOfMiscProject;
    private ProjectXMLManager copyOfMiscXMLManager;
    
    protected void setUp() throws Exception {
        super.setUp();
        copyOfSuite2 = copyFolder(file(EEP + "/suite2"));
        File miscF = new File(copyOfSuite2, "misc-project");
        copyOfMiscDir = FileUtil.toFileObject(miscF);
        copyOfMiscProject = (NbModuleProject) ProjectManager.getDefault().findProject(copyOfMiscDir);
        assertNotNull(copyOfMiscProject);
        copyOfMiscXMLManager = new ProjectXMLManager(copyOfMiscProject);
        // make sure its platform-private.properties is correct:
        Project copyOfSuite2P = ProjectManager.getDefault().findProject(FileUtil.toFileObject(copyOfSuite2));
        SuiteProjectTest.openSuite(copyOfSuite2P);
    }
    
    private String urlForJar(String path) {
        return Util.urlForJar(file(path)).toExternalForm();
    }
    
    private String urlForDir(String path) {
        return Util.urlForDir(file(path)).toExternalForm();
    }
    
    private Set/*<String>*/ urlsOfCp(ClassPath cp) {
        Set/*<String>*/ s = new TreeSet();
        Iterator it = cp.entries().iterator();
        while (it.hasNext()) {
            s.add(((ClassPath.Entry) it.next()).getURL().toExternalForm());
        }
        return s;
    }
    
    private static final Set/*<String>*/ TESTLIBS = new HashSet(Arrays.asList(new String[] {
        "junit.jar", "nbjunit.jar", "nbjunit-ide.jar", "insanelib.jar",
        "org-netbeans-modules-nbjunit.jar",
        "org-netbeans-modules-nbjunit-ide.jar",
    }));
    
    private void assertSets(String message,Set set1, Set set2 ) {
        StringBuffer result = new StringBuffer();
        boolean pass = true;
        for (Iterator it = set1.iterator() ; it.hasNext() ; ) {
            Object obj = it.next();
            if (!set2.contains(obj)) {
                pass = false;
                result.append("- " + obj + "\n");
            }
        }
        for (Iterator it = set2.iterator() ; it.hasNext() ; ) {
            Object obj = it.next();
            if (!set1.contains(obj)) {
                pass = false;
                result.append("+ " + obj + "\n");
            }
        }
        assertTrue(message + "\n" + result,pass);
    }
 
    private Set/*<String>*/ urlsOfCp4Tests(ClassPath cp) {
        Set/*<String>*/ s = new TreeSet();
        Iterator it = cp.entries().iterator();
        while (it.hasNext()) {
            String url = ((ClassPath.Entry) it.next()).getURL().toExternalForm();
            if (url.indexOf("$%7B") != -1) {
                // Unevaluated Ant reference (after octet escaping), so skip.
                continue;
            }
            String simplifiedJarName = url.replaceFirst("^.+/([^/]+?)[0-9_.-]*\\.jar!/$", "$1.jar");
            if (TESTLIBS.contains(simplifiedJarName)) {
                s.add(simplifiedJarName);
            } else {
                s.add(url);
            }
        }
        return s;
    }
    
    public void testMainClasspath() throws Exception {
        FileObject src = nbroot.getFileObject("ant/src");
        assertNotNull("have ant/src", src);
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set/*<String>*/ expectedRoots = new TreeSet();
        // Keep up to date w/ changes in ant/nbproject/project.{xml,properties}:
        // ${module.classpath}:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-api-xml.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-modules.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-awt.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-options.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-text.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-actions.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-execution.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-io.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-explorer.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-spi-navigator.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-jdesktop-layout.jar"));
        expectedRoots.add(urlForJar("libs/external/swing-layout-1.0.1.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-options-api.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-api-progress.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-projectapi.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-projectuiapi.jar"));
        assertEquals("right COMPILE classpath for ant/src", expectedRoots.toString(), urlsOfCp(cp).toString());
        cp = ClassPath.getClassPath(src, ClassPath.EXECUTE);
        assertNotNull("have an EXECUTE classpath", cp);
        // #48099: need to include build/classes here too
        expectedRoots.add(urlForDir("ant/build/classes"));
        // And #70206: transitive runtime deps too.
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-queries.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-editor-mimelookup.jar"));
        assertEquals("right EXECUTE classpath (COMPILE plus classes)", expectedRoots.toString(), urlsOfCp(cp).toString());
        cp = ClassPath.getClassPath(src, ClassPath.SOURCE);
        assertNotNull("have a SOURCE classpath", cp);
        assertEquals("right SOURCE classpath", Collections.singleton(src), new HashSet(Arrays.asList(cp.getRoots())));
        // XXX test BOOT
    }

    public void testMainClasspathExternalModules() throws Exception {
        FileObject src = extexamples.getFileObject("suite3/dummy-project/src");
        assertNotNull("have .../dummy-project/src", src);
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set/*<String>*/ expectedRoots = new TreeSet();
        expectedRoots.add(urlForJar(EEP + "/suite3/nbplatform/random/modules/random.jar"));
        expectedRoots.add(urlForJar(EEP + "/suite3/nbplatform/random/modules/ext/stuff.jar"));
        assertEquals("right COMPILE classpath", expectedRoots, urlsOfCp(cp));
    }
    
    /**
     * #52354: interpret <class-path-extension>s both in myself and in dependent modules.
     */
    public void testClasspathExtensions() throws Exception {
        // java/javacore has its own <class-path-extension> and uses others from dependents.
        FileObject src = nbroot.getFileObject("java/javacore/src");
        assertNotNull("have java/javacore/src", src);
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set/*<String>*/ expectedRoots = new TreeSet();
        // Keep up to date w/ changes in java/javacore/nbproject/project.xml:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-api-java.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-classfile.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-jmi-javamodel.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/javax-jmi-reflect.jar"));
        expectedRoots.add(urlForJar("mdr/external/jmi.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/javax-jmi-model.jar"));
        expectedRoots.add(urlForJar("mdr/external/mof.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-api-mdr.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-projectapi.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-api-progress.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-mdr.jar"));
        expectedRoots.add(urlForJar("mdr/dist/mdr.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-jmiutils.jar"));
        expectedRoots.add(urlForJar("mdr/jmiutils/dist/jmiutils.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-modules.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-awt.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-text.jar"));
        expectedRoots.add(urlForJar("java/parser/dist/java-parser.jar"));
        assertEquals("right COMPILE classpath", expectedRoots.toString(), urlsOfCp(cp).toString());
    }
    
    public void testExtraCompilationUnits() throws Exception {
        FileObject srcbridge = nbroot.getFileObject("ant/src-bridge");
        assertNotNull("have ant/src-bridge", srcbridge);
        ClassPath cp = ClassPath.getClassPath(srcbridge, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set/*<String>*/ expectedRoots = new TreeSet();
        // Keep up to date w/ changes in ant/nbproject/project.{xml,properties}:
        expectedRoots.add(urlForDir("ant/build/classes"));
        expectedRoots.add(urlForJar("ant/external/lib/ant.jar"));
        // ${module.classpath}:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-api-xml.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-modules.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-awt.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-options.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-text.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-actions.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-execution.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-io.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-explorer.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-spi-navigator.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-jdesktop-layout.jar"));
        expectedRoots.add(urlForJar("libs/external/swing-layout-1.0.1.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-options-api.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-api-progress.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-projectapi.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-projectuiapi.jar"));
        assertEquals("right COMPILE classpath for ant/src-bridge", expectedRoots.toString(), urlsOfCp(cp).toString());
        cp = ClassPath.getClassPath(srcbridge, ClassPath.EXECUTE);
        assertNotNull("have an EXECUTE classpath", cp);
        expectedRoots.add(urlForDir("ant/build/bridge-classes"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/ant/nblib/bridge.jar"));
        assertEquals("right EXECUTE classpath (COMPILE plus classes plus JAR)", expectedRoots, urlsOfCp(cp));
        cp = ClassPath.getClassPath(srcbridge, ClassPath.SOURCE);
        assertNotNull("have a SOURCE classpath", cp);
        assertEquals("right SOURCE classpath", Collections.singleton(srcbridge), new HashSet(Arrays.asList(cp.getRoots())));
        // XXX test BOOT
    }
    
    public void testUnitTestClasspaths() throws Exception {
        FileObject src = nbroot.getFileObject("autoupdate/test/unit/src");
        assertNotNull("have autoupdate/test/unit/src", src);
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set/*<String>*/ expectedRoots = new TreeSet();
        // Keep up to date w/ changes in autoupdate/nbproject/project.{xml,properties}:
        // module.classpath:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-modules.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/boot.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-explorer.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-awt.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-options.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-actions.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-core.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-api-progress.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/core.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-editor-mimelookup.jar"));
        // cp.extra:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/ext/updater.jar"));
        // module JAR:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-autoupdate.jar"));
        expectedRoots.add("junit.jar");
        expectedRoots.add("nbjunit.jar");
        expectedRoots.add("nbjunit-ide.jar");
        expectedRoots.add("insanelib.jar");
        // recursive dependencies
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-swing-plaf.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-text.jar"));
        
        assertSets("right COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));
        
        cp = ClassPath.getClassPath(src, ClassPath.EXECUTE);
        assertNotNull("have an EXECUTE classpath", cp);
        expectedRoots.add(urlForDir("autoupdate/build/test/unit/classes"));
        // test.unit.run.cp.extra:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/boot.jar"));
        assertSets("right EXECUTE classpath (COMPILE plus classes)", expectedRoots, urlsOfCp4Tests(cp));
        cp = ClassPath.getClassPath(src, ClassPath.SOURCE);
        assertNotNull("have a SOURCE classpath", cp);
        assertEquals("right SOURCE classpath", Collections.singleton(src), new HashSet(Arrays.asList(cp.getRoots())));
        // XXX test BOOT
    }
    
    public void testUnitTestClasspathsExternalModules() throws Exception {
        FileObject src = extexamples.getFileObject("suite1/support/lib-project/test/unit/src");
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set/*<String>*/ expectedRoots = new TreeSet();
        expectedRoots.add(urlForJar(EEP + "/suite1/build/cluster/modules/org-netbeans-examples-modules-lib.jar"));
        expectedRoots.add("junit.jar");
        expectedRoots.add("nbjunit.jar");
        expectedRoots.add("insanelib.jar");
        expectedRoots.add("org-netbeans-modules-nbjunit.jar");
        expectedRoots.add("org-netbeans-modules-nbjunit-ide.jar");
        assertEquals("right COMPILE classpath", expectedRoots.toString(), urlsOfCp4Tests(cp).toString());
        // Now test in suite3, where there is no source...
        src = extexamples.getFileObject("suite3/dummy-project/test/unit/src");
        cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        expectedRoots = new TreeSet();
        expectedRoots.add(urlForJar(EEP + "/suite3/dummy-project/build/cluster/modules/org-netbeans-examples-modules-dummy.jar"));
        expectedRoots.add(urlForJar(EEP + "/suite3/nbplatform/random/modules/random.jar"));
        expectedRoots.add(urlForJar(EEP + "/suite3/nbplatform/random/modules/ext/stuff.jar"));
        expectedRoots.add("junit.jar");
        expectedRoots.add("nbjunit.jar");
        expectedRoots.add("insanelib.jar");
        expectedRoots.add("org-netbeans-modules-nbjunit.jar");
        expectedRoots.add("org-netbeans-modules-nbjunit-ide.jar");
        assertEquals("right COMPILE classpath", expectedRoots.toString(), urlsOfCp4Tests(cp).toString());
    }
    
    public void testQaFunctionalTestClasspath() throws Exception {
        FileObject qaftsrc = nbroot.getFileObject("performance/test/qa-functional/src");
        assertNotNull("have performance/test/qa-functional/src", qaftsrc);
        ClassPath cp = ClassPath.getClassPath(qaftsrc, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set/*<String>*/ expectedRoots = new TreeSet();
        // Keep up to date w/ changes in /space/src/nb_all/performance/nbproject/project.properties
        // & nbbuild/templates/xtest-qa-functional.xml:
        expectedRoots.add("junit.jar");
        expectedRoots.add("nbjunit.jar");
        expectedRoots.add("nbjunit-ide.jar");
        expectedRoots.add("insanelib.jar");
        // jemmy.and.jelly.path:
        expectedRoots.add(urlForJar("jemmy/builds/jemmy.jar"));
        expectedRoots.add(urlForJar("jellytools/builds/jelly2-nb.jar"));
        // test.qa-functional.cp.extra currently empty
        assertEquals("right COMPILE classpath", expectedRoots.toString(), urlsOfCp4Tests(cp).toString());
        cp = ClassPath.getClassPath(qaftsrc, ClassPath.EXECUTE);
        assertNotNull("have an EXECUTE classpath", cp);
        expectedRoots.add(urlForDir("performance/build/test/qa-functional/classes"));
        assertEquals("right EXECUTE classpath (COMPILE plus classes)", expectedRoots, urlsOfCp4Tests(cp));
        cp = ClassPath.getClassPath(qaftsrc, ClassPath.SOURCE);
        assertNotNull("have a SOURCE classpath", cp);
        assertEquals("right SOURCE classpath", Collections.singleton(qaftsrc), new HashSet(Arrays.asList(cp.getRoots())));
        // XXX test BOOT
    }
    
    public void testQaFunctionalTestClasspathExternalModules() throws Exception {
        FileObject qaftsrc = extexamples.getFileObject("suite1/action-project/test/qa-functional/src");
        assertNotNull("have action-project/test/qa-functional/src", qaftsrc);
        ClassPath cp = ClassPath.getClassPath(qaftsrc, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set/*<String>*/ expectedRoots = new TreeSet();
        expectedRoots.add("junit.jar");
        expectedRoots.add("nbjunit.jar");
        expectedRoots.add("nbjunit-ide.jar");
        expectedRoots.add("insanelib.jar");
        expectedRoots.add(urlForJar("jemmy/builds/jemmy.jar"));
        expectedRoots.add(urlForJar("jellytools/builds/jelly2-nb.jar"));
        expectedRoots.add("org-netbeans-modules-nbjunit.jar");
        expectedRoots.add("org-netbeans-modules-nbjunit-ide.jar");
        assertEquals("right COMPILE classpath", expectedRoots.toString(), urlsOfCp4Tests(cp).toString());
        TestBase.TestPCL l = new TestBase.TestPCL();
        cp.addPropertyChangeListener(l);
        NbModuleProject p = (NbModuleProject) FileOwnerQuery.getOwner(qaftsrc);
        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        File added = file("xtest/lib/xtest.jar");
        ep.setProperty("test.qa-functional.cp.extra", added.getAbsolutePath());
        p.getHelper().putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        // do not save! just want to test change, not actually make it...
        assertTrue("got changes", l.changed.contains(ClassPath.PROP_ROOTS));
        expectedRoots.add(urlForJar("xtest/lib/xtest.jar"));
        assertEquals("right COMPILE classpath after added to .extra", expectedRoots.toString(), urlsOfCp4Tests(cp).toString());
    }
    
    public void testBuildClassPath () throws Exception {
        FileObject srcRoot = nbroot.getFileObject("ant/project/src/");
        assertNotNull("have ant/project/src",srcRoot);
        ClassPath ccp = ClassPath.getClassPath(srcRoot, ClassPath.COMPILE);
        assertNotNull("No compile ClassPath for sources",ccp);
        FileObject  buildClasses = nbroot.getFileObject("ant/project/build/classes/");
        assertNotNull("have ant/project/build/classes",buildClasses);
                
        assertNull ("ClassPath.SOURCE for build must be null",ClassPath.getClassPath(buildClasses, ClassPath.SOURCE));
        assertNull ("ClassPath.COMPILE for build must be null",ClassPath.getClassPath(buildClasses, ClassPath.COMPILE));
        ClassPath cp = ClassPath.getClassPath(buildClasses, ClassPath.EXECUTE);
        assertNotNull("ClassPath.EXECUTE for build must NOT be null",cp);
        ClassPath expectedCp = ClassPathSupport.createProxyClassPath(new ClassPath[] {
                ClassPathSupport.createClassPath(new FileObject[] {buildClasses}),
                ccp
        });
        assertClassPathsHaveTheSameResources(cp, expectedCp);
        
        FileObject testSrcRoot = nbroot.getFileObject("ant/project/test/unit/src/");
        assertNotNull("have ant/project/test/unit/src/",testSrcRoot);
        ClassPath tccp = ClassPath.getClassPath(testSrcRoot, ClassPath.COMPILE);
        assertNotNull("No compile ClassPath for tests",tccp);
        Project prj = FileOwnerQuery.getOwner(testSrcRoot);
        assertNotNull("No project found",prj);
        assertTrue("Invalid project type", prj instanceof NbModuleProject);
        FileObject testBuildClasses = nbroot.getFileObject ("ant/project/build/test/unit/classes/");
        if (testBuildClasses == null) {
            // Have to have it, so we can call CP.gCP on it:
            testBuildClasses = FileUtil.createFolder(nbroot, "ant/project/build/test/unit/classes");
        }
        assertNull ("ClassPath.SOURCE for build/test must be null",ClassPath.getClassPath(testBuildClasses, ClassPath.SOURCE));
        assertNull ("ClassPath.COMPILE for build/test must be null",ClassPath.getClassPath(testBuildClasses, ClassPath.COMPILE));
        cp = ClassPath.getClassPath(testBuildClasses, ClassPath.EXECUTE);
        
        String path = ((NbModuleProject)prj).evaluator().getProperty("test.unit.run.cp.extra");     //NOI18N
        List trExtra = new ArrayList ();
        if (path != null) {
            String[] pieces = PropertyUtils.tokenizePath(path);
            for (int i = 0; i < pieces.length; i++) {
                File f = ((NbModuleProject)prj).getHelper().resolveFile(pieces[i]);
                URL url = f.toURI().toURL();
                if (FileUtil.isArchiveFile(url)) {
                    url = FileUtil.getArchiveRoot (url);
                }
                else {
                    String stringifiedURL = url.toString ();
                    if (!stringifiedURL.endsWith("/")) {        //NOI18N
                        url = new URL (stringifiedURL+"/");     //NOI18N
                    }
                }
                trExtra.add(ClassPathSupport.createResource(url));
            }
        }        
        assertNotNull("ClassPath.EXECUTE for build/test must NOT be null", cp);
        expectedCp = ClassPathSupport.createProxyClassPath(new ClassPath[] {
                ClassPathSupport.createClassPath(new FileObject[] {testBuildClasses}),
                tccp,
                ClassPathSupport.createClassPath(trExtra),
                ClassPathSupport.createClassPath(new URL[] {new URL(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-masterfs.jar"))})
        });
        assertClassPathsHaveTheSameResources(cp, expectedCp);

        File jarFile = ((NbModuleProject) prj).getModuleJarLocation();
        FileObject jarFO = FileUtil.toFileObject(jarFile);
        assertNotNull("No module jar", jarFO);
        FileObject jarRoot = FileUtil.getArchiveRoot(jarFO);
//        assertEquals(prj, FileOwnerQuery.getOwner(jarRoot));
        assertNull("ClassPath.SOURCE for module jar must be null", ClassPath.getClassPath(jarRoot, ClassPath.SOURCE));
        assertNull("ClassPath.COMPILE for module jar must be null", ClassPath.getClassPath(jarRoot, ClassPath.COMPILE));
        cp = ClassPath.getClassPath(jarRoot, ClassPath.EXECUTE);
        assertNotNull("ClassPath.EXECUTE for module jar must NOT be null", cp);
        expectedCp = ClassPathSupport.createProxyClassPath(new ClassPath[] {
            ClassPathSupport.createClassPath(new FileObject[] {jarRoot}),
            ccp
        });
        assertClassPathsHaveTheSameResources(cp, expectedCp);
    }
    
    public void testCompileClasspathChanges() throws Exception {
        ClassPath cp = ClassPath.getClassPath(copyOfMiscDir.getFileObject("src"), ClassPath.COMPILE);
        Set/*<String>*/ expectedRoots = new TreeSet();
        assertEquals("right initial COMPILE classpath", expectedRoots, urlsOfCp(cp));
        TestBase.TestPCL l = new TestBase.TestPCL();
        cp.addPropertyChangeListener(l);
        ModuleEntry ioEntry = copyOfMiscProject.getModuleList().getEntry("org.openide.io");
        assertNotNull(ioEntry);
        copyOfMiscXMLManager.addDependencies(Collections.singleton(new ModuleDependency(ioEntry)));
        assertTrue("got changes", l.changed.contains(ClassPath.PROP_ROOTS));
        l.changed.clear();
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-io.jar"));
        assertEquals("right COMPILE classpath after changing project.xml", expectedRoots, urlsOfCp(cp));
        ModuleEntry utilEntry = copyOfMiscProject.getModuleList().getEntry("org.openide.util");
        assertNotNull(utilEntry);
        copyOfMiscXMLManager.addDependencies(Collections.singleton(new ModuleDependency(utilEntry)));
        assertTrue("got changes again", l.changed.contains(ClassPath.PROP_ROOTS));
        l.changed.clear();
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        assertEquals("right COMPILE classpath after changing project.xml again", expectedRoots, urlsOfCp(cp));
    }
    
    public void testExecuteClasspathChanges() throws Exception {
        ClassPath cp = ClassPath.getClassPath(copyOfMiscDir.getFileObject("src"), ClassPath.EXECUTE);
        Set/*<String>*/ expectedRoots = new TreeSet();
        expectedRoots.add(Util.urlForDir(file(FileUtil.toFile(copyOfMiscDir), "build/classes")).toExternalForm());
        assertEquals("right initial EXECUTE classpath", expectedRoots, urlsOfCp(cp));
        TestBase.TestPCL l = new TestBase.TestPCL();
        cp.addPropertyChangeListener(l);
        ModuleEntry ioEntry = copyOfMiscProject.getModuleList().getEntry("org.openide.io");
        assertNotNull(ioEntry);
        copyOfMiscXMLManager.addDependencies(Collections.singleton(new ModuleDependency(ioEntry)));
        assertTrue("got changes", l.changed.contains(ClassPath.PROP_ROOTS));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-io.jar"));
        // #70206: transitive deps added too:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        assertEquals("right EXECUTE classpath after changing project.xml", expectedRoots.toString(), urlsOfCp(cp).toString());
    }
    
    public void testUnitTestCompileClasspathChanges() throws Exception {
        ClassPath cp = ClassPath.getClassPath(copyOfMiscDir.getFileObject("test/unit/src"), ClassPath.COMPILE);
        Set/*<String>*/ expectedRoots = new TreeSet();
        expectedRoots.add(Util.urlForJar(file(copyOfSuite2, "build/cluster/modules/org-netbeans-examples-modules-misc.jar")).toExternalForm());
        expectedRoots.add("junit.jar");
        expectedRoots.add("nbjunit.jar");
        expectedRoots.add("insanelib.jar");
        expectedRoots.add("org-netbeans-modules-nbjunit.jar");
        expectedRoots.add("org-netbeans-modules-nbjunit-ide.jar");
        assertEquals("right initial COMPILE classpath", expectedRoots.toString(), urlsOfCp4Tests(cp).toString());
        TestBase.TestPCL l = new TestBase.TestPCL();
        cp.addPropertyChangeListener(l);
        ModuleEntry ioEntry = copyOfMiscProject.getModuleList().getEntry("org.openide.io");
        assertNotNull(ioEntry);
        copyOfMiscXMLManager.addDependencies(Collections.singleton(new ModuleDependency(ioEntry)));
        assertTrue("got changes", l.changed.contains(ClassPath.PROP_ROOTS));
        l.changed.clear();
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-io.jar"));
        assertEquals("right COMPILE classpath after changing project.xml", expectedRoots, urlsOfCp4Tests(cp));
        EditableProperties props = copyOfMiscProject.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("test.unit.cp.extra", "${netbeans.dest.dir}/lib/fnord.jar");
        copyOfMiscProject.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        assertTrue("got changes again", l.changed.contains(ClassPath.PROP_ROOTS));
        expectedRoots.add(urlForJar("nbbuild/netbeans/lib/fnord.jar"));
        assertEquals("right COMPILE classpath after changing project.properties", expectedRoots, urlsOfCp4Tests(cp));
    }
    
    public void testBinaryOriginAbsolutePath() throws Exception {
        File jmfhome = new File(getWorkDir(), "jmfhome");
        File audioFiles = file("platform/samples/audio-files");
        assertTrue("have 'platform' checked out", audioFiles.isDirectory());
        File audioviewer = copyFolder(audioFiles);
        // Make it a standalone module so we can copy it:
        File pp = new File(audioviewer, "nbproject/private/private.properties".replace('/', File.separatorChar));
        pp.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(pp);
        try {
            Properties p = new Properties();
            p.setProperty("jmf.home", jmfhome.getAbsolutePath());
            p.store(os, null);
        } finally {
            os.close();
        }
        pp = new File(audioviewer, "nbproject/private/platform-private.properties".replace('/', File.separatorChar));
        pp.getParentFile().mkdirs();
        os = new FileOutputStream(pp);
        try {
            Properties p = new Properties();
            p.setProperty("netbeans.dest.dir", file("nbbuild/netbeans").getAbsolutePath());
            p.store(os, null);
        } finally {
            os.close();
        }
        File px = new File(audioviewer, "nbproject/project.xml".replace('/', File.separatorChar));
        Document doc = XMLUtil.parse(new InputSource(px.toURI().toString()), false, true, null, null);
        NodeList nl = doc.getDocumentElement().getElementsByTagNameNS(NbModuleProjectType.NAMESPACE_SHARED, "data");
        assertEquals(1, nl.getLength());
        Element data = (Element) nl.item(0);
        // XXX insert at position 1, between <c-n-b> and <m-d>:
        data.appendChild(doc.createElementNS(NbModuleProjectType.NAMESPACE_SHARED, "standalone"));
        os = new FileOutputStream(px);
        try {
            XMLUtil.write(doc, os, "UTF-8");
        } finally {
            os.close();
        }
        FileObject audioviewerFO = FileUtil.toFileObject(audioviewer);
        Project p = ProjectManager.getDefault().findProject(audioviewerFO);
        assertNotNull(p);
        FileObject src = audioviewerFO.getFileObject("src");
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set/*<String>*/ expectedRoots = new TreeSet();
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-actions.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-text.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
        File lib = new File(jmfhome, "lib");
        expectedRoots.add(Util.urlForJar(new File(lib, "jmf.jar")).toExternalForm());
        expectedRoots.add(Util.urlForJar(new File(lib, "mediaplayer.jar")).toExternalForm());
        assertEquals("right COMPILE classpath incl. absolute locations of JARs",
            expectedRoots.toString(), urlsOfCp(cp).toString());
    }
    
    private void assertClassPathsHaveTheSameResources(ClassPath actual, ClassPath expected) {
        assertSets("cls: ",urlsOfCp(expected), urlsOfCp(actual));
    }
    
    public void testTransitiveExecuteClasspath() throws Exception { // #70206
        NbModuleProject p = TestBase.generateStandaloneModule(getWorkDir(), "prj");
        Util.addDependency(p, "org.openide.windows");
        ProjectManager.getDefault().saveProject(p);
        ClassPath cp = ClassPath.getClassPath(p.getSourceDirectory(), ClassPath.EXECUTE);
        Set/*<String>*/ expectedRoots = new TreeSet();
        // What we just added:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
        // And its transitive deps:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-awt.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        // And the usual:
        expectedRoots.add(Util.urlForDir(new File(FileUtil.toFile(p.getProjectDirectory()), "build/classes")).toExternalForm());
        assertEquals("right EXECUTE classpath incl. transitive deps",
            expectedRoots.toString(), urlsOfCp(cp).toString());
    }
    
}
