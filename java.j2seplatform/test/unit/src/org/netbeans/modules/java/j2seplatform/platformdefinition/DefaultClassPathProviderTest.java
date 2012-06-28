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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import org.netbeans.modules.java.j2seplatform.queries.DefaultClassPathProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.net.URL;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.event.ChangeListener;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;



import org.netbeans.core.startup.layers.ArchiveURLMapper;
import org.netbeans.junit.MockServices;



import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.MasterURLMapper;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;

import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;


import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;


/**
 *
 * @author  tom
 */
public class DefaultClassPathProviderTest extends NbTestCase {
    
    private static final int FILE_IN_PACKAGE = 0;
    private static final int FILE_IN_BAD_PACKAGE = 1;
    private static final int FILE_IN_DEFAULT_PACKAGE = 2;
    private static final int DELAY = 2500;

    private static final byte[] CLASS_FILE_DATA = {
        (byte)0xca, (byte)0xfe, (byte)0xba, (byte)0xbe, 0x00, 0x00, 0x00, 0x2e, 0x00, 0x0d, 0x0a, 0x00, 0x03, 0x00, 0x0a, 0x07, 0x00, 0x0b, 0x07, 0x00,
        0x0c, 0x01, 0x00, 0x06, 0x3c, 0x69, 0x6e, 0x69, 0x74, 0x3e, 0x01, 0x00, 0x03, 0x28, 0x29, 0x56, 0x01, 0x00, 0x04, 0x43,
        0x6f, 0x64, 0x65, 0x01, 0x00, 0x0f, 0x4c, 0x69, 0x6e, 0x65, 0x4e, 0x75, 0x6d, 0x62, 0x65, 0x72, 0x54, 0x61, 0x62, 0x6c,
        0x65, 0x01, 0x00, 0x0a, 0x53, 0x6f, 0x75, 0x72, 0x63, 0x65, 0x46, 0x69, 0x6c, 0x65, 0x01, 0x00, 0x09, 0x54, 0x65, 0x73,
        0x74, 0x2e, 0x6a, 0x61, 0x76, 0x61, 0x0c, 0x00, 0x04, 0x00, 0x05, 0x01, 0x00, 0x09, 0x74, 0x65, 0x73, 0x74, 0x2f, 0x54,
        0x65, 0x73, 0x74, 0x01, 0x00, 0x10, 0x6a, 0x61, 0x76, 0x61, 0x2f, 0x6c, 0x61, 0x6e, 0x67, 0x2f, 0x4f, 0x62, 0x6a, 0x65,
        0x63, 0x74, 0x00, 0x21, 0x00, 0x02, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x04, 0x00, 0x05,
        0x00, 0x01, 0x00, 0x06, 0x00, 0x00, 0x00, 0x1d, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x2a, (byte)0xb7, 0x00, 0x01,
        (byte)0xb1, 0x00, 0x00, 0x00, 0x01, 0x00, 0x07, 0x00, 0x00, 0x00, 0x06, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x00, 0x01, 0x00,
        0x08, 0x00, 0x00, 0x00, 0x02, 0x00, 0x09
    };

    private FileObject srcRoot;
    private FileObject[] srcFile = new FileObject[3];
    private FileObject[] compileRoots;
    private static FileObject[] execRoots;
    private static FileObject[] libSourceRoots;
    private FileObject execTestDir;
    
    /** Creates a new instance of DefaultClassPathProviderTest */
    public DefaultClassPathProviderTest (String testName) {
        super (testName);
        MockServices.setServices(
                ArchiveURLMapper.class,
                MasterURLMapper.class,
                JavaPlatformProviderImpl.class,
                SFBQI.class,
                OpenProject.class);
    }
    
    
    @Override
    protected void tearDown () throws Exception {
        this.srcRoot = null;
        this.compileRoots = null;
        super.tearDown();
    }
    
    
    @Override
    protected void setUp() throws Exception {
        this.clearWorkDir();        
        super.setUp();
        final RunnableFuture<Project[]> dummyFuture = new FutureTask<Project[]>(new Callable<Project[]>() {
            @Override
            public Project[] call() throws Exception {
                return new Project[0];
            }
        });
        dummyFuture.run();
        assertTrue(dummyFuture.isDone());
        OpenProject.future = dummyFuture;
        FileObject workDir = FileUtil.toFileObject(this.getWorkDir());
        assertNotNull("MasterFS is not configured.", workDir);
        this.srcRoot = workDir.createFolder("src");
        this.compileRoots = new FileObject[3];
        for (int i=0; i< this.compileRoots.length; i++) {
            this.compileRoots[i] = workDir.createFolder("lib_"+Integer.toString(i));
        }
        ClassPath cp = ClassPathSupport.createClassPath(this.compileRoots);
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] {cp});
       this.execRoots = new FileObject[2];
        this.execRoots[0] = this.compileRoots[2];
        this.execRoots[1] = workDir.createFolder("lib_OnlyExec");
        cp = ClassPathSupport.createClassPath(this.execRoots);
        GlobalPathRegistry.getDefault().register (ClassPath.EXECUTE, new ClassPath[]{cp});
        this.libSourceRoots = new FileObject[2];
        for (int i=0; i< libSourceRoots.length; i++) {
            this.libSourceRoots[i] = workDir.createFolder ("libSrc_"+Integer.toString(i));
        }
        cp = ClassPathSupport.createClassPath (this.libSourceRoots);
        GlobalPathRegistry.getDefault().register (ClassPath.SOURCE, new ClassPath[]{cp});
        execTestDir = workDir.createFolder("exec");
    }
    
    
    
    public void testFindClassPath () throws IOException {
        FileObject artefact = getSourceFile (FILE_IN_PACKAGE);
        ClassPathProvider cpp = new DefaultClassPathProvider ();
        ClassPath cp = cpp.findClassPath(artefact, ClassPath.SOURCE);
        assertNull ("DefaultClassPathProvider returned not null for SOURCES",cp);
//        assertNotNull ("DefaultClassPathProvider returned null for SOURCES",cp);
//        assertEquals("Invalid length of classpath for SOURCE",1,cp.getRoots().length);
//        assertRootsEquals ("Invalid classpath roots for SOURCE", cp, new FileObject[] {this.srcRoot});
        cp = cpp.findClassPath(artefact, ClassPath.COMPILE);        
        assertNotNull ("DefaultClassPathProvider returned null for COMPILE",cp);
        assertEquals("Invalid length of classpath for COMPILE",this.compileRoots.length + 1, cp.getRoots().length);
        FileObject[] resRoots = new FileObject[this.compileRoots.length + 1];
        System.arraycopy(this.compileRoots,0,resRoots,0,this.compileRoots.length);
        resRoots[this.compileRoots.length] = this.execRoots[1];
        assertRootsEquals ("Invalid classpath roots for COMPILE", cp, resRoots);
        cp = cpp.findClassPath(artefact, ClassPath.BOOT);
        assertNotNull ("DefaultClassPathProvider returned null for BOOT",cp);
        JavaPlatform dp = JavaPlatformManager.getDefault().getDefaultPlatform();
        assertEquals("Invalid length of classpath for BOOT",dp.getBootstrapLibraries().getRoots().length, cp.getRoots().length);
        assertRootsEquals ("Invalid classpath roots for BOOT", cp, dp.getBootstrapLibraries().getRoots());

        artefact = getSourceFile (FILE_IN_DEFAULT_PACKAGE);
        cp = cpp.findClassPath(artefact, ClassPath.SOURCE);
        assertNull ("DefaultClassPathProvider returned not null for SOURCES",cp);
//        assertNotNull ("DefaultClassPathProvider returned null for SOURCES",cp);
//        assertEquals("Invalid length of classpath for SOURCE",1,cp.getRoots().length);
//        assertRootsEquals ("Invalid classpath roots for SOURCE", cp, new FileObject[] {this.srcRoot});
        
        artefact = getSourceFile (FILE_IN_BAD_PACKAGE);
        cp = cpp.findClassPath(artefact, ClassPath.SOURCE);
        assertNull ("DefaultClassPathProvider returned not null for SOURCES",cp);
//        assertNotNull ("DefaultClassPathProvider returned null for SOURCES",cp);
//        assertEquals("Invalid length of classpath for SOURCE",1,cp.getRoots().length);
//        FileObject badRoot = this.srcRoot.getFileObject ("test");
//        assertRootsEquals ("Invalid classpath roots for SOURCE", cp, new FileObject[] {badRoot});      //ERROR
        FileObject classFile = getClassFile();
        cp = cpp.findClassPath(classFile, ClassPath.EXECUTE);
        assertNotNull ("DefaultClassPathProvider returned null for EXECUTE",cp);
        assertEquals("Invalid length of classpath for EXECUTE",1,cp.getRoots().length);
        assertEquals("Illegal classpath for EXECUTE: ",cp.getRoots()[0],this.execTestDir);
    }
    
    public void testCycle () throws Exception {
        GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
        Set<ClassPath> toCleanUp = regs.getPaths(ClassPath.COMPILE);        
        regs.unregister(ClassPath.COMPILE, toCleanUp.toArray(new ClassPath[toCleanUp.size()]));
        toCleanUp = regs.getPaths(ClassPath.EXECUTE);        
        regs.unregister(ClassPath.EXECUTE, toCleanUp.toArray(new ClassPath[toCleanUp.size()]));
        File wdf = getWorkDir();
        FileObject wd = FileUtil.toFileObject(wdf);
        FileObject root1 = wd.createFolder("root1");
        FileObject root2 = wd.createFolder("root2");
        ClassPathProvider cpp = new DefaultClassPathProvider ();
        ClassPath dcp = cpp.findClassPath(root2, ClassPath.COMPILE);
        ClassPath cp = ClassPathSupport.createClassPath(new FileObject[] {root1});
        regs.register(ClassPath.COMPILE, new ClassPath[] {cp});        
        assertNotNull(dcp);
        FileObject[] roots = dcp.getRoots();
        assertEquals(1, roots.length);
        assertEquals(root1, roots[0]);
        
        regs.register(ClassPath.COMPILE, new ClassPath[] {dcp});
        roots = dcp.getRoots();
        assertEquals(1, roots.length);        
    }

    /**
     * Checks that single event is fired during opening (closing) of projects
     */
    public void testEvents() throws Exception {
        final File wd = getWorkDir();
        final File root1 = FileUtil.normalizeFile(new File (wd,"src1"));    //NOI18N
        final File root2 = FileUtil.normalizeFile(new File (wd,"src2"));    //NOI18N
        root1.mkdir();
        root2.mkdir();
        final ClassPathProvider cpp = new DefaultClassPathProvider ();
        final ClassPath defaultCompile = cpp.findClassPath(FileUtil.toFileObject(wd), ClassPath.COMPILE);
        assertNotNull(defaultCompile);
        assertFalse(contains(defaultCompile, Utilities.toURI(root1).toURL()));
        assertFalse(contains(defaultCompile, Utilities.toURI(root2).toURL()));
        final Listener listener = new Listener();
        defaultCompile.addPropertyChangeListener(listener);
        final GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
        final ClassPath compileCpProject1 = ClassPathSupport.createClassPath(Utilities.toURI(root1).toURL());
        final ClassPath compileCpProject2 = ClassPathSupport.createClassPath(Utilities.toURI(root2).toURL());
        
        //Simulate projects open
        RunnableFuture<Project[]> barrier = new FutureTask<Project[]>(new Callable<Project[]>() {
            @Override
            public Project[] call() throws Exception {
                return new Project[0];
            }
        });
        OpenProject.future = barrier;

        regs.register(ClassPath.COMPILE, new ClassPath[]{compileCpProject1});
        assertFalse(contains(defaultCompile, Utilities.toURI(root1).toURL()));
        assertFalse(listener.awaitEvent());
        assertEquals(0, listener.get());
        regs.register(ClassPath.COMPILE, new ClassPath[]{compileCpProject2});
        assertFalse(contains(defaultCompile, Utilities.toURI(root2).toURL()));
        assertFalse(listener.awaitEvent());
        assertEquals(0, listener.get());
        barrier.run();
        assertTrue(listener.awaitEvent());
        assertEquals(1, listener.get());
        assertTrue(contains(defaultCompile, Utilities.toURI(root1).toURL()));
        assertTrue(contains(defaultCompile, Utilities.toURI(root2).toURL()));

        //Simulate projects close
        barrier = new FutureTask<Project[]>(new Callable<Project[]>() {
            @Override
            public Project[] call() throws Exception {
                return new Project[0];
            }
        });
        OpenProject.future = barrier;
        listener.reset();
        regs.unregister(ClassPath.COMPILE, new ClassPath[]{compileCpProject1});
        assertTrue(contains(defaultCompile, Utilities.toURI(root1).toURL()));
        assertFalse(listener.awaitEvent());
        assertEquals(0, listener.get());
        regs.unregister(ClassPath.COMPILE, new ClassPath[]{compileCpProject2});
        assertTrue(contains(defaultCompile, Utilities.toURI(root2).toURL()));
        assertFalse(listener.awaitEvent());
        assertEquals(0, listener.get());
        barrier.run();
        assertTrue(listener.awaitEvent());
        assertEquals(1, listener.get());
        assertFalse(contains(defaultCompile, Utilities.toURI(root1).toURL()));
        assertFalse(contains(defaultCompile, Utilities.toURI(root2).toURL()));
    }

    private static boolean contains (final ClassPath cp, final URL url) {
        for (ClassPath.Entry entry : cp.entries()) {
            if (entry.getURL().equals(url)) {
                return true;
            }
        }
        return false;
    }
    
    
    private static void assertRootsEquals (String message, ClassPath cp, FileObject[] roots) {
        Set<FileObject> cpRoots = new HashSet<FileObject>(Arrays.asList(cp.getRoots()));
        assertEquals(message, cpRoots.size(), roots.length);
        for (FileObject root : roots) {
            if (!cpRoots.contains(root)) {
                assertTrue(message, false);
            }
        }
    }
    
    private synchronized FileObject getSourceFile (int type) throws IOException {
        if (this.srcFile[type]==null) {
            assertNotNull (this.srcRoot);
            switch (type) {
                case FILE_IN_PACKAGE:
                    this.srcFile[type] = createFile (this.srcRoot,"test","Test","package test;\npublic class Test {}");                    
                    break;
                case FILE_IN_DEFAULT_PACKAGE:
                    this.srcFile[type] = createFile (this.srcRoot,null,"DefaultTest","public class DefaultTest {}");                    
                    break;
                case FILE_IN_BAD_PACKAGE:
                    this.srcFile[type] = createFile (this.srcRoot,"test","BadTest","package bad;\npublic class BadTest {}");                    
                    break;
                default:
                    throw new IllegalArgumentException ();
            }
        }
        return this.srcFile[type];
    }

    private synchronized FileObject getClassFile () throws IOException {
        FileObject fo = this.execTestDir.getFileObject("test/Test.class");
        if (fo == null) {
            fo = execTestDir.createFolder("test");
            fo = fo.createData("Test","class");
            FileLock lock = fo.lock();
            try {
                DataOutputStream out = new DataOutputStream (fo.getOutputStream(lock));
                try {
                    out.write(CLASS_FILE_DATA);
                    out.flush();
                } finally {
                    out.close();
                }
            } finally {
                lock.releaseLock();
            }
        }
        return fo;
    }

    private static FileObject createFile (FileObject root, String folderName, String name, String body) throws IOException {
        if (folderName != null) {
            FileObject tmp = root.getFileObject(folderName,null);
            if (tmp == null) {
                tmp = root.createFolder (folderName);
            }
            root = tmp;
        }
        FileObject file = root.createData (name,"java");
        FileLock lock = file.lock();
        try {
            PrintWriter out = new PrintWriter ( new OutputStreamWriter (file.getOutputStream(lock)));
            try {
                out.println (body);
            } finally {
                out.close ();
            }
        } finally {
            lock.releaseLock();
        }
        return file;
    }
    
    
    
    public static class SFBQI implements SourceForBinaryQueryImplementation {
        
        
        public SFBQI () {
        }
        
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            for (int i = 0; i < execRoots.length; i++) {
                try {
                    URL url = execRoots[i].getURL ();
                    if (url.equals (binaryRoot)) {
                        return new SourceForBinaryQuery.Result () {
                    
                            public FileObject[] getRoots () {                        
                                return libSourceRoots;
                            }
                    
                            public void addChangeListener (ChangeListener l) {
                            }
                    
                            public void removeChangeListener (ChangeListener l) {
                            }
                        };
                    }
                } catch (Exception e) {}                
            }
            return null;
        }
    }

    public static class OpenProject implements  OpenProjectsTrampoline {

        static Future<Project[]> future;

        @Override
        public Project[] getOpenProjectsAPI() {
            return new Project[0];
        }

        @Override
        public void openAPI(Project[] projects, boolean openRequiredProjects, boolean showProgress) {
        }

        @Override
        public void closeAPI(Project[] projects) {
        }

        @Override
        public Project getMainProject() {
            return null;
        }

        @Override
        public void setMainProject(Project project) {
        }

        @Override
        public void addPropertyChangeListenerAPI(PropertyChangeListener listener, Object source) {
        }

        @Override
        public void removePropertyChangeListenerAPI(PropertyChangeListener listener) {
        }

        @Override
        public Future<Project[]> openProjectsAPI() {
            return future;
        }
    }

    private static class Listener implements PropertyChangeListener {

        private volatile int count;
        private final Lock lock = new ReentrantLock();
        private final Condition cnd = lock.newCondition();

        public void reset() {
            lock.lock();
            try {
                count = 0;
            } finally {
                lock.unlock();
            }
        }

        public int get() {
            return count;
        }

        public boolean awaitEvent() throws InterruptedException {
            lock.lock();
            try {
                while (count == 0) {
                    if (!cnd.await(DELAY, TimeUnit.MILLISECONDS)) {
                        return false;
                    }
                }
            } finally {
                lock.unlock();
            }
            return true;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ClassPath.PROP_ENTRIES.equals(evt.getPropertyName())) {
                lock.lock();
                try {
                    count = count + 1;
                    cnd.signal();
                } finally {
                    lock.unlock();
                }
            }
        }

    }
}
