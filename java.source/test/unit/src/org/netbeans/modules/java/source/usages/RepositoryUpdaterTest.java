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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.SourceUtilsTestUtil.TestSourceLevelQueryImplementation;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.tasklist.FirstParseCatcher;
import org.netbeans.modules.java.source.tasklist.TaskCache;
import org.netbeans.modules.java.source.tasklist.TasklistSettings;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class RepositoryUpdaterTest extends NbTestCase {
    
    static {
        Logger.getLogger(RepositoryUpdater.class.getName()).setLevel(Level.ALL);
    }
    
    public RepositoryUpdaterTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        RepositoryUpdater.DELAY = 0;
        handler = new Handler() {
            public void publish(LogRecord rec) {
                if (RepositoryUpdater.GOING_TO_RECOMPILE.equals(rec.getMessage())) {
                    recompiled = (Map<URL, Collection<File>>) rec.getParameters()[0];
                }
            }
            
            public void flush() {
            }
            
            public void close() throws SecurityException {
            }
        };
        Logger.getLogger(RepositoryUpdater.class.getName()).addHandler(handler);
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        Logger.getLogger(RepositoryUpdater.class.getName()).removeHandler(handler);
        if (sourceCP != null) {
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, new ClassPath[] {sourceCP});
        }
        super.tearDown();
    }
    
    private File pack;
    private File a;
    private File b;
    private FileObject fileA;
    private FileObject fileB;
    private URL rootURL;
    private Map<URL, Collection<File>> recompiled;
    private Handler handler;
    private ClassPath sourceCP;
    
    private void prepareTest(String aFileContent, String bFileContent) throws Exception {
        prepareTest(aFileContent, bFileContent, null);
    }
    
    private void prepareTest(String aFileContent, String bFileContent, String sourceLevel) throws Exception {
        clearWorkDir();
        
        File workDir = getWorkDir();
        File src     = new File(workDir, "src");
        File cache   = new File(workDir, "cache");
        File userdir = new File(workDir, "userdir");
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        pack    = new File(src, "pack");
        
        cache.mkdirs();
        pack.mkdirs();
        
        rootURL = src.toURL();
        
        Index.setCacheFolder(cache);
        
        a       = new File(pack, "A.java");
        b       = new File(pack, "B.java");
        
        TestUtilities.copyStringToFile(a, aFileContent);
        TestUtilities.copyStringToFile(b, bFileContent);
        
        List<URL> bootClassPath = SourceUtilsTestUtil.getBootClassPath();
        URL[] bootClassPathRoots = bootClassPath.toArray(new URL[0]);
        
        fileA = FileUtil.toFileObject(a);
        fileB = FileUtil.toFileObject(b);
        
        assertNotNull(fileA);
        assertNotNull(fileB);
        
        if (sourceLevel != null) {
            SourceUtilsTestUtil.setSourceLevel(fileA, sourceLevel);
            SourceUtilsTestUtil.setSourceLevel(fileB, sourceLevel);
        }
        
        ClassPath bootCP = ClassPathSupport.createClassPath(bootClassPathRoots);
        
        sourceCP = ClassPathSupport.createClassPath(new FileObject[] {FileUtil.toFileObject(src)});
        
        CPPImpl impl = new CPPImpl(bootCP, sourceCP);
        
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/tasklist/ui/layer.xml"},new Object[] {impl, new VisibilityQueryImplementation() {
            public boolean isVisible(FileObject file) {
                try {
                    return !file.getURL().toString().contains(".jar");
                } catch (IOException e) {
                    e.printStackTrace();
                    return true;
                }
            }
            public void addChangeListener(ChangeListener l) {}
            public void removeChangeListener(ChangeListener l) {}
        }, new TestSourceLevelQueryImplementation()});
        
        RepositoryUpdater.getDefault();
        
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {sourceCP});
        
        waitScanFinished();
    }
    
    public void testSimpleReparse() throws Exception {
        prepareTest(aFileContent, bFileContent);
        TestUtilities.copyStringToFile(fileB, bFileContentNewContent);
        
        waitScanFinished();
        
        assertFalse(TaskCache.getDefault().getErrors(fileA).isEmpty());
        
        TestUtilities.copyStringToFile(fileB, bFileContent);
        
        waitScanFinished();
        
        assertTrue(TaskCache.getDefault().getErrors(fileA).isEmpty());
    }
    
//    public void testWarnings() throws Exception {
//        System.setProperty("org.netbeans.api.java.source.JavaSource.USE_COMPILER_LINT", "true");
//        prepareTest("package pack; public class A {java.util.List<String> l = new java.util.ArrayList();}", "package pack; public class B{}");
//
//        assertFalse(TaskCache.getDefault().isInError(fileA, true));
//
//        List<Task> tasks = TaskCache.getDefault().getErrors(fileA);
//
//        assertEquals(1, tasks.size());
//
//        Task t = tasks.get(0);
//
//        assertEquals("nb-tasklist-warning", Accessor.DEFAULT.getGroup(t).getName());
//    }
    
    public void testReparseDeprecated() throws Exception {
        System.setProperty("org.netbeans.api.java.source.JavaSource.USE_COMPILER_LINT", "true");
        prepareTest("package pack; public class A {private int x = B.get();}", "package pack; public class B {public static int get() {return 0;}}");
        
        assertTrue(TaskCache.getDefault().getErrors(fileA).toString(), TaskCache.getDefault().getErrors(fileA).isEmpty());
        
        TestUtilities.copyStringToFile(fileB, "package pack; public class B {@Deprecated public static int get() {return 0;}}");
        
        waitScanFinished();
        
        assertEquals(TaskCache.getDefault().getErrors(fileA).toString(), 1, TaskCache.getDefault().getErrors(fileA).size());
    }
    
    public void testDeleteUpdate1() throws Exception {
        prepareTest("package pack; public class A { error }", "package pack; public class B {}");
        
        FileObject parent = fileA.getParent();
        
        assertFalse(TaskCache.getDefault().getErrors(fileA).toString(), TaskCache.getDefault().getErrors(fileA).isEmpty());
        assertTrue(TaskCache.getDefault().isInError(parent, true));
        assertTrue(TaskCache.getDefault().isInError(parent, false));
        
        fileA.delete();
        
        waitScanFinished();
        
        assertFalse(TaskCache.getDefault().isInError(parent, true));
        assertFalse(TaskCache.getDefault().isInError(parent, false));
    }
    
    public void testDeleteUpdate2() throws Exception {
        prepareTest("package pack; public class A { error }", "package pack; public class B {A a;}");
        
        FileObject parent = fileA.getParent();
        
        assertFalse(TaskCache.getDefault().getErrors(fileA).toString(), TaskCache.getDefault().getErrors(fileA).isEmpty());
        assertFalse(TaskCache.getDefault().isInError(fileB, false));
        
        fileA.delete();
        
        waitScanFinished();
        
        assertTrue(TaskCache.getDefault().isInError(fileB, false));
    }
    
    public void testCreateUpdate1() throws Exception {
        prepareTest("package pack; public class A { C c;}", "package pack; public class B {}");
        
        assertTrue(TaskCache.getDefault().isInError(fileA, false));
        assertFalse(TaskCache.getDefault().isInError(fileB, false));
        
        final FileObject[] fileC = new FileObject[1];
        
        fileA.getFileSystem().runAtomicAction(new AtomicAction() {
            public void run() throws IOException {
                try {
                    fileC[0] = FileUtil.createData(FileUtil.toFileObject(pack), "C.java");
                    TestUtilities.copyStringToFile(fileC[0], "package pack; public class C {}");
                } catch (Exception e) {
                    throw (IOException) new IOException().initCause(e);
                }
            }
        });
        
        waitScanFinished();
        
        assertFalse(TaskCache.getDefault().isInError(fileA, false));
        assertFalse(TaskCache.getDefault().isInError(fileB, false));
    }
    
    public void testFileUpdate1() throws Exception {
        prepareTest("package pack; public class A { B.Inner x; }", "package pack; public class B {public static class Inner {}}");
        assertFalse(TaskCache.getDefault().getErrors(fileA).toString(), TaskCache.getDefault().isInError(fileA, false));
        assertFalse(TaskCache.getDefault().getErrors(fileB).toString(), TaskCache.getDefault().isInError(fileB, false));
        
        TestUtilities.copyStringToFile(fileB, "package pack; public class B {}");
        
        waitScanFinished();
        
        assertNotNull(recompiled);
        assertTrue(TaskCache.getDefault().getErrors(fileA).toString(), TaskCache.getDefault().isInError(fileA, false));
        assertFalse(TaskCache.getDefault().getErrors(fileB).toString(), TaskCache.getDefault().isInError(fileB, false));
    }
    
    public void testFileUpdate2() throws Exception {
        prepareTest("package pack; public class A { B.Inner x; }", "package pack; public class B {public static class Inner {}}");
        assertFalse(TaskCache.getDefault().getErrors(fileA).toString(), TaskCache.getDefault().isInError(fileA, false));
        assertFalse(TaskCache.getDefault().getErrors(fileB).toString(), TaskCache.getDefault().isInError(fileB, false));
        
        new FirstParseCatcher().createTask(fileB).run(SourceUtilsTestUtil.getCompilationInfo(JavaSource.forFileObject(fileB), Phase.RESOLVED));
        
        TestUtilities.copyStringToFile(fileB, "package pack; public class B {public static class Inner {} /**/}");
        
        waitScanFinished();
        
        assertNull(String.valueOf(recompiled), recompiled);
        assertFalse(TaskCache.getDefault().getErrors(fileA).toString(), TaskCache.getDefault().isInError(fileA, false));
        assertFalse(TaskCache.getDefault().getErrors(fileB).toString(), TaskCache.getDefault().isInError(fileB, false));
    }
    
    public void testSourceLevelChanged() throws Exception {
        prepareTest("package pack; public enum A { }", "package pack; public class B {}", "1.4");
        assertTrue(TaskCache.getDefault().getErrors(fileA).toString(), TaskCache.getDefault().isInError(fileA, false));
        assertFalse(TaskCache.getDefault().getErrors(fileB).toString(), TaskCache.getDefault().isInError(fileB, false));
        
        SourceUtilsTestUtil.setSourceLevel(fileA, "1.5");
        SourceUtilsTestUtil.setSourceLevel(fileB, "1.5");
        
        //currently, it is not possible to listen on changes in source level query, so a file needs to be reparsed
        //from the source root to detect the change:
        SourceUtilsTestUtil.getCompilationInfo(JavaSource.forFileObject(fileB), Phase.PARSED);
        
        waitScanFinished();
        
        assertFalse(TaskCache.getDefault().getErrors(fileA).toString(), TaskCache.getDefault().isInError(fileA, false));
        assertFalse(TaskCache.getDefault().getErrors(fileB).toString(), TaskCache.getDefault().isInError(fileB, false));
    }
    
    public void testTasklistEnableDisable() throws Exception {
        prepareTest("package pack; public enum A { B;}", "package pack; public class B {A a = A.B;}");
        assertFalse(TaskCache.getDefault().getErrors(fileA).toString(), TaskCache.getDefault().isInError(fileA, false));
        assertFalse(TaskCache.getDefault().getErrors(fileB).toString(), TaskCache.getDefault().isInError(fileB, false));
        
        assertNull(RepositoryUpdater.getDefault().getAttribute(rootURL, RepositoryUpdater.DIRTY_ROOT, null));
        TasklistSettings.setTasklistsEnabled(false);
        
        TestUtilities.copyStringToFile(fileA, "package pack; public enum A { C;}");
        
        waitScanFinished();
        
        assertNull(RepositoryUpdater.getDefault().getAttribute(rootURL, RepositoryUpdater.DIRTY_ROOT, null));
        
        assertFalse(TaskCache.getDefault().getErrors(fileA).toString(), TaskCache.getDefault().isInError(fileA, false));
        assertFalse(TaskCache.getDefault().getErrors(fileB).toString(), TaskCache.getDefault().isInError(fileB, false));
        
        TasklistSettings.setTasklistsEnabled(true);
        
        waitScanFinished();
        
        assertNull(RepositoryUpdater.getDefault().getAttribute(rootURL, RepositoryUpdater.DIRTY_ROOT, null));
        
        assertFalse(TaskCache.getDefault().getErrors(fileA).toString(), TaskCache.getDefault().isInError(fileA, false));
        assertTrue(TaskCache.getDefault().getErrors(fileB).toString(), TaskCache.getDefault().isInError(fileB, false));
        
        //XXX: test isTasklistEnabled and isDependencyTrackingEnabled separately
    }
    
    private void waitScanFinished() throws Exception {
        //XXX:
        Thread.sleep(2000);
        while (RepositoryUpdater.getDefault().waitWorkStarted())
            SourceUtils.waitScanFinished();
        
        SourceUtils.waitScanFinished();
    }
    
    private static final class CPPImpl implements ClassPathProvider {
        
        private static final ClassPath EMPTY = ClassPathSupport.createClassPath(new URL[0]);
        
        private ClassPath  bootCP;
        private ClassPath  sourceCP;
        
        public CPPImpl(ClassPath bootCP,
                ClassPath sourceCP) {
            this.bootCP = bootCP;
            this.sourceCP = sourceCP;
        }
        
        public ClassPath findClassPath(FileObject file, String type) {
            //            if (file != fileA && file != fileB) {
            //                return null;
            //            }
            
            if (ClassPath.BOOT.contains(type)) {
                return bootCP;
            }
            
            if (ClassPath.COMPILE.contains(type)) {
                return EMPTY;
            }
            
            if (ClassPath.SOURCE.contains(type)) {
                return sourceCP;
            }
            
            fail("Unhandled");
            
            return null;
        }
        
    }
    
    private static final String aFileContent = "package pack;\n" +
            "public class A {\n" +
            "public static void main(String [] args) {\n" +
            "B.test();\n" +
            "}\n" +
            "}\n";
    
    private static final String bFileContent = "package pack;\n" +
            "public class B {\n" +
            "public static void test() {}\n" +
            "}\n";
    private static final String bFileContentNewContent = "package pack;\n" +
            "public class B {\n" +
            "public static void tests() {}\n" +
            "}\n";
}
