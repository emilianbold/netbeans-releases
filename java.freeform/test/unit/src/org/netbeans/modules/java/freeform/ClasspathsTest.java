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

package org.netbeans.modules.java.freeform;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.TestBase;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.w3c.dom.Element;

// XXX testClasspathsOfBuildProducts
// - should have BOOT and EXECUTE
// XXX testBootClasspathChanges
// - check that after e.g. changing plain source level, new JDK picked
// XXX testSourcepathChanges
// - check that after adding/removing a source root to a given compilation unit, SOURCE changes to match
// (but can this really work generally? what if a single compilation unit is split in half??)
// XXX testIgnoredRoot
// - check that after removing a compilation unit, ClassPath.gCP returns null again, and the ClassPath object is invalidated
// XXX testNonexistentEntries
// - check that correct URL (ending in '/') picked for dirs (not JARs) in CP that do not yet exist
//   and that for nonexistent JARs ('.' in simple name) the correct jar: protocol URL is used

/**
 * Test functionality of classpath definitions in FreeformProject.
 * This class just tests the basic functionality found in the "simple" project.
 * @author Jesse Glick
 */
public class ClasspathsTest extends TestBase {

    public ClasspathsTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        assertNotNull("Must have built ant/freeform unit tests first, INCLUDING copying non-*.java resources to the classes build directory",
            ClasspathsTest.class.getResource("/META-INF/services/org.openide.modules.InstalledFileLocator"));
    }
    
    public void testSourcePath() throws Exception {
        ClassPath cp = ClassPath.getClassPath(myAppJava, ClassPath.SOURCE);
        assertNotNull("have some SOURCE classpath for src/", cp);
        FileObject[] roots = cp.getRoots();
        assertEquals("have one entry in " + cp, 1, roots.length);
        assertEquals("that is src/", simple.getProjectDirectory().getFileObject("src"), roots[0]);
        cp = ClassPath.getClassPath(specialTaskJava, ClassPath.SOURCE);
        assertNotNull("have some SOURCE classpath for antsrc/", cp);
        roots = cp.getRoots();
        assertEquals("have one entry", 1, roots.length);
        assertEquals("that is antsrc/", simple.getProjectDirectory().getFileObject("antsrc"), roots[0]);
        cp = ClassPath.getClassPath(buildProperties, ClassPath.SOURCE);
        assertNull("have no SOURCE classpath for build.properties", cp);
    }
    
    public void testCompileClasspath() throws Exception {
        ClassPath cp = ClassPath.getClassPath(myAppJava, ClassPath.COMPILE);
        assertNotNull("have some COMPILE classpath for src/", cp);
        assertEquals("have two entries in " + cp, 2, cp.entries().size());
        assertEquals("have two roots in " + cp, 2, cp.getRoots().length);
        assertNotNull("found WeakSet in " + cp, cp.findResource("org/openide/util/WeakSet.class"));
        assertNotNull("found NullInputStream", cp.findResource("org/openide/util/io/NullInputStream.class"));
        cp = ClassPath.getClassPath(specialTaskJava, ClassPath.COMPILE);
        assertNotNull("have some COMPILE classpath for antsrc/", cp);
        assertEquals("have one entry", 1, cp.getRoots().length);
        assertNotNull("found Task", cp.findResource("org/apache/tools/ant/Task.class"));
        cp = ClassPath.getClassPath(buildProperties, ClassPath.COMPILE);
        assertNull("have no COMPILE classpath for build.properties", cp);
    }
    
    public void testExecuteClasspath() throws Exception {
        ClassPath cp = ClassPath.getClassPath(myAppJava, ClassPath.EXECUTE);
        assertNotNull("have some EXECUTE classpath for src/", cp);
        Set<String> roots = new TreeSet();
        roots.add(urlForJar("lib/lib1.jar"));
        roots.add(urlForJar("lib/lib2.jar"));
        // #49113: includes built-to dirs as well.
        roots.add(urlForJar("build/simple-app.jar"));
        roots.add(urlForFolder("build/classes"));
        assertEquals("right EXECUTE cp for src/", roots.toString(), urlsOfCp(cp).toString());
        cp = ClassPath.getClassPath(specialTaskJava, ClassPath.EXECUTE);
        assertNotNull("have some EXECUTE classpath for antsrc/", cp);
        // Just check number of entries here... could instead find ${ant.home}/lib/ant.jar.
        assertEquals("have two entries (ant.jar + build/antclasses) in " + cp, 2, cp.entries().size());
        assertNotNull("found Task", cp.findResource("org/apache/tools/ant/Task.class"));
        cp = ClassPath.getClassPath(buildProperties, ClassPath.EXECUTE);
        assertNull("have no EXECUTE classpath for build.properties", cp);
    }
    
    public void testBootClasspath() throws Exception {
        ClassPath cp = ClassPath.getClassPath(myAppJava, ClassPath.BOOT);
        assertNotNull("have some BOOT classpath for src/", cp);
        assertEquals("and it is JDK 1.4", "1.4", specOfBootClasspath(cp));
        ClassPath cp2 = ClassPath.getClassPath(specialTaskJava, ClassPath.BOOT);
        assertNotNull("have some BOOT classpath for antsrc/", cp2);
        assertEquals("and it is JDK 1.4", "1.4", specOfBootClasspath(cp2));
        /* Not actually required:
        assertEquals("same BOOT classpath for all files (since use same spec level)", cp, cp2);
         */
        cp = ClassPath.getClassPath(buildProperties, ClassPath.BOOT);
        assertNull("have no BOOT classpath for build.properties", cp);
    }
    
    private static String specOfBootClasspath(ClassPath cp) {
        List<ClassPath.Entry> entries = cp.entries();
        if (entries.size() != 1) {
            return null;
        }
        ClassPath.Entry entry = (ClassPath.Entry)entries.get(0);
        String u = entry.getURL().toExternalForm();
        // Cf. DummyJavaPlatformProvider.
        Pattern p = Pattern.compile("jar:file:/c:/java/([0-9.]+)/jre/lib/rt\\.jar!/");
        Matcher m = p.matcher(u);
        if (m.matches()) {
            return m.group(1);
        } else {
            return null;
        }
    }
    
    public void testGlobalPathRegistryUsage() throws Exception {
        GlobalPathRegistry gpr = GlobalPathRegistry.getDefault();
        assertEquals("no BOOT classpaths yet", Collections.EMPTY_SET, gpr.getPaths(ClassPath.BOOT));
        assertEquals("no COMPILE classpaths yet", Collections.EMPTY_SET, gpr.getPaths(ClassPath.COMPILE));
        assertEquals("no EXECUTE classpaths yet", Collections.EMPTY_SET, gpr.getPaths(ClassPath.EXECUTE));
        assertEquals("no SOURCE classpaths yet", Collections.EMPTY_SET, gpr.getPaths(ClassPath.SOURCE));
        ProjectOpenedHook hook = (ProjectOpenedHook) simple.getLookup().lookup(ProjectOpenedHook.class);
        assertNotNull("have a ProjectOpenedHook", hook);
        Method opened = ProjectOpenedHook.class.getDeclaredMethod("projectOpened", null);
        opened.setAccessible(true);
        opened.invoke(hook, null);
        Set<ClassPath> boot = gpr.getPaths(ClassPath.BOOT);
        Set<ClassPath> compile = gpr.getPaths(ClassPath.COMPILE);
        Set<ClassPath> execute = gpr.getPaths(ClassPath.EXECUTE);
        Set<ClassPath> source = gpr.getPaths(ClassPath.SOURCE);
        Set<ClassPath> expected = new HashSet();
        expected.add(ClassPath.getClassPath(myAppJava, ClassPath.BOOT));
        expected.add(ClassPath.getClassPath(specialTaskJava, ClassPath.BOOT));
        assertEquals("correct set of BOOT classpaths", expected, boot);
        expected = new HashSet();
        expected.add(ClassPath.getClassPath(myAppJava, ClassPath.COMPILE));
        expected.add(ClassPath.getClassPath(specialTaskJava, ClassPath.COMPILE));
        assertEquals("correct set of COMPILE classpaths", expected, compile);
        expected = new HashSet();
        expected.add(ClassPath.getClassPath(myAppJava, ClassPath.EXECUTE));
        expected.add(ClassPath.getClassPath(specialTaskJava, ClassPath.EXECUTE));
        assertEquals("correct set of EXECUTE classpaths", expected, execute);
        expected = new HashSet();
        expected.add(ClassPath.getClassPath(myAppJava, ClassPath.SOURCE));
        expected.add(ClassPath.getClassPath(specialTaskJava, ClassPath.SOURCE));
        assertEquals("correct set of SOURCE classpaths", expected, source);
        Method closed = ProjectOpenedHook.class.getDeclaredMethod("projectClosed", null);
        closed.setAccessible(true);
        closed.invoke(hook, null);
        assertEquals("again no BOOT classpaths", Collections.EMPTY_SET, gpr.getPaths(ClassPath.BOOT));
        assertEquals("again no COMPILE classpaths", Collections.EMPTY_SET, gpr.getPaths(ClassPath.COMPILE));
        assertEquals("again no EXECUTE classpaths", Collections.EMPTY_SET, gpr.getPaths(ClassPath.EXECUTE));
        assertEquals("again no SOURCE classpaths", Collections.EMPTY_SET, gpr.getPaths(ClassPath.SOURCE));
    }
    
    public void testCompileClasspathChanges() throws Exception {
        clearWorkDir();
        FreeformProject simple2 = copyProject(simple);
        FileObject myAppJava2 = simple2.getProjectDirectory().getFileObject("src/org/foo/myapp/MyApp.java");
        assertNotNull("found MyApp.java", myAppJava2);
        ClassPath cp = ClassPath.getClassPath(myAppJava2, ClassPath.COMPILE);
        assertNotNull("have some COMPILE classpath for src/", cp);
        assertEquals("have two entries in " + cp, 2, cp.entries().size());
        assertEquals("have two roots in " + cp, 2, cp.getRoots().length);
        assertNotNull("found WeakSet in " + cp, cp.findResource("org/openide/util/WeakSet.class"));
        assertNotNull("found NullInputStream", cp.findResource("org/openide/util/io/NullInputStream.class"));
        TestPCL l = new TestPCL();
        cp.addPropertyChangeListener(l);
        EditableProperties props = new EditableProperties();
        FileObject buildProperties = simple2.getProjectDirectory().getFileObject("build.properties");
        assertNotNull("have build.properties", buildProperties);
        InputStream is = buildProperties.getInputStream();
        try {
            props.load(is);
        } finally {
            is.close();
        }
        assertEquals("right original src.cp", "${lib.dir}/lib1.jar:${lib.dir}/lib2.jar", props.getProperty("src.cp"));
        props.setProperty("src.cp", "${lib.dir}/lib1.jar");
        FileLock lock = buildProperties.lock();
        try {
            final OutputStream os = buildProperties.getOutputStream(lock);
            try {
                props.store(os);
            } finally {
                // close file under ProjectManager.readAccess so that events are fired synchronously
                ProjectManager.mutex().readAccess(new Mutex.ExceptionAction() {
                    public Object run() throws Exception {
                        os.close();
                        return null;
                    }
                });
            }
        } finally {
            lock.releaseLock();
        }
        assertEquals("ROOTS fired", new HashSet(Arrays.asList(new String[] {ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS})), l.changed);
        assertEquals("have one entry in " + cp, 1, cp.entries().size());
        assertEquals("have one root in " + cp, 1, cp.getRoots().length);
        assertNotNull("found WeakSet in " + cp, cp.findResource("org/openide/util/WeakSet.class"));
        assertNull("did not find NullInputStream", cp.findResource("org/openide/util/io/NullInputStream.class"));
    }

    public void testCompilationUnitChanges() throws Exception {
        clearWorkDir();
        FreeformProject simple2 = copyProject(simple);
        AntProjectHelper helper2 = simple2.helper();
        FileObject myAppJava = simple2.getProjectDirectory().getFileObject("src/org/foo/myapp/MyApp.java");
        assertNotNull("found MyApp.java", myAppJava);
        ClassPath cpSrc = ClassPath.getClassPath(myAppJava, ClassPath.COMPILE);
        assertNotNull("have some COMPILE classpath for src/", cpSrc);
        assertEquals("have two entries in " + cpSrc, 2, cpSrc.entries().size());
        FileObject mySpecialTask = simple2.getProjectDirectory().getFileObject("antsrc/org/foo/ant/SpecialTask.java");
        assertNotNull("found SpecialTask.java", mySpecialTask);
        ClassPath cpAnt = ClassPath.getClassPath(mySpecialTask, ClassPath.COMPILE);
        assertNotNull("have some COMPILE classpath for antsrc/", cpAnt);
        assertEquals("have one entry in " + cpAnt, 1, cpAnt.entries().size());
        TestPCL srcL = new TestPCL();
        cpSrc.addPropertyChangeListener(srcL);
        TestPCL antL = new TestPCL();
        cpAnt.addPropertyChangeListener(antL);
        
        ProjectOpenedHook hook = (ProjectOpenedHook) simple2.getLookup().lookup(ProjectOpenedHook.class);
        assertNotNull("have a ProjectOpenedHook", hook);
        GlobalPathRegistry gpr = GlobalPathRegistry.getDefault();
        assertEquals("no COMPILE classpaths yet", Collections.EMPTY_SET, gpr.getPaths(ClassPath.COMPILE));
        Method opened = ProjectOpenedHook.class.getDeclaredMethod("projectOpened", null);
        opened.setAccessible(true);
        opened.invoke(hook, null);
        Set<ClassPath> compile = gpr.getPaths(ClassPath.COMPILE);
        Set<ClassPath> expected = new HashSet();
        expected.add(cpSrc);
        expected.add(cpAnt);
        assertEquals("correct set of COMPILE classpaths", expected, compile);
        
        AuxiliaryConfiguration aux = Util.getAuxiliaryConfiguration(helper2);
        List units = JavaProjectGenerator.getJavaCompilationUnits(helper2, aux);
        assertEquals("two compilation units", 2, units.size());
        JavaProjectGenerator.JavaCompilationUnit cu = new JavaProjectGenerator.JavaCompilationUnit();
        cu.packageRoots = new ArrayList();
        cu.packageRoots.add("${src.dir}");
        cu.packageRoots.add("${ant.src.dir}");
        cu.classpath = new ArrayList();
        JavaProjectGenerator.JavaCompilationUnit.CP cucp = new JavaProjectGenerator.JavaCompilationUnit.CP();
        cucp.mode = "compile";
        cucp.classpath = "${src.cp}:${ant.src.cp}";
        cu.classpath.add(cucp);
        List newUnits = new ArrayList();
        newUnits.add(cu);
        JavaProjectGenerator.putJavaCompilationUnits(helper2, aux, newUnits);

        ClassPath cpSrc2 = ClassPath.getClassPath(myAppJava, ClassPath.COMPILE);
        assertNotNull("have some COMPILE classpath for src/", cpSrc2);
        assertEquals("have three entries in " + cpSrc2, 3, cpSrc2.entries().size());
        assertEquals("original classpath is empty now", 0, cpSrc.entries().size());
        ClassPath cpAnt2 = ClassPath.getClassPath(mySpecialTask, ClassPath.COMPILE);
        assertNotNull("have some COMPILE classpath for antsrc/", cpAnt2);
        assertEquals("have three entries in " + cpAnt2, 3, cpAnt2.entries().size());
        assertEquals("original classpath is empty now", 0, cpAnt.entries().size());
        assertEquals("classpath for src/ and antsrc/ are the same. " + cpAnt2 + " "+cpSrc2, cpAnt2, cpSrc2);
        
        assertEquals("ROOTS fired", new HashSet(Arrays.asList(new String[] {ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS})), srcL.changed);
        srcL.reset();
        assertEquals("ROOTS fired", new HashSet(Arrays.asList(new String[] {ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS})), antL.changed);
        antL.reset();
        
        compile = gpr.getPaths(ClassPath.COMPILE);
        expected = new HashSet();
        expected.add(cpSrc);
        expected.add(cpAnt);
        expected.add(cpAnt2);
        assertEquals("correct set of COMPILE classpaths", expected, compile);
        
        TestPCL cpL = new TestPCL();
        cpAnt2.addPropertyChangeListener(cpL);

        JavaProjectGenerator.putJavaCompilationUnits(helper2, aux, units);
        ClassPath cpSrc3 = ClassPath.getClassPath(myAppJava, ClassPath.COMPILE);
        assertNotNull("have some COMPILE classpath for src/", cpSrc3);
        assertEquals("have two entries in " + cpSrc3, 2, cpSrc3.entries().size());
        ClassPath cpAnt3 = ClassPath.getClassPath(mySpecialTask, ClassPath.COMPILE);
        assertNotNull("have some COMPILE classpath for antsrc/", cpAnt3);
        assertEquals("have one entry in " + cpAnt3, 1, cpAnt3.entries().size());
        assertEquals("original classpath instance was reused", cpSrc, cpSrc3);
        assertEquals("original classpath instance was reused", cpAnt, cpAnt3);
        assertEquals("classpath of single compilation unit is empty now", 0, cpAnt2.entries().size());
        
        assertEquals("ROOTS fired", new HashSet(Arrays.asList(new String[] {ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS})), cpL.changed);
        cpL.reset();
        assertEquals("ROOTS fired", new HashSet(Arrays.asList(new String[] {ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS})), srcL.changed);
        srcL.reset();
        assertEquals("ROOTS fired", new HashSet(Arrays.asList(new String[] {ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS})), antL.changed);
        antL.reset();
        
        compile = gpr.getPaths(ClassPath.COMPILE);
        assertEquals("all three classpaths still there", expected, compile);
        Method closed = ProjectOpenedHook.class.getDeclaredMethod("projectClosed", null);
        closed.setAccessible(true);
        closed.invoke(hook, null);
        compile = gpr.getPaths(ClassPath.COMPILE);
        expected = new HashSet();
        assertEquals("correct set of COMPILE classpaths", expected, compile);
    }

    public void testDeadlock77015() throws Exception {
        final CountDownLatch l = new CountDownLatch(2);

        Classpaths.TESTING_LATCH = l;

        new Thread() {
            public void run() {
                ClassPath.getClassPath(myAppJava, ClassPath.SOURCE);
            }
        }.start();

        Thread t = new Thread() {
            public void run() {
                ProjectManager.mutex().writeAccess(new Mutex.Action() {
                    public Object run() {
                        AntProjectHelper helper = simple.helper();
                        Element el = helper.getPrimaryConfigurationData(true);
                        AuxiliaryConfiguration c = (AuxiliaryConfiguration) simple.getLookup().lookup(AuxiliaryConfiguration.class);
                        
                        l.countDown();
                        try {
                            l.await(2, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        
                        c.putConfigurationFragment(el.getOwnerDocument().createElementNS("http://something/X", "A"), false);
                        
                        return null;
                    }
                });
            }
        };

        t.start();
        t.join();
    }
    
    // Copied from org.netbeans.modules.apisupport.project.ClassPathProviderImplTest:
    private String urlForJar(String path) throws Exception {
        return FileUtil.getArchiveRoot(simple.helper().resolveFile(path).toURI().toURL()).toExternalForm();
    }
    private String urlForFolder(String path) throws Exception {
        String s = simple.helper().resolveFile(path).toURI().toURL().toExternalForm();
        if (s.endsWith("/")) {
            return s;
        } else {
            return s + "/";
        }
    }
    private Set<String> urlsOfCp(ClassPath cp) {
        Set<String> s = new TreeSet();
        Iterator it = cp.entries().iterator();
        while (it.hasNext()) {
            s.add(((ClassPath.Entry) it.next()).getURL().toExternalForm());
        }
        return s;
    }
    
}
