/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.api.java.source;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.TestFileUtils;

/**
 *
 * @author Tomas Zezula
 */
public class ClassIndexTest extends NbTestCase {

    private static FileObject srcRoot;
    private static FileObject srcRoot2;
    private static FileObject binRoot;
    private static FileObject binRoot2;
    private static FileObject libSrc2;
    private static ClassPath sourcePath;
    private static ClassPath compilePath;
    private static ClassPath bootPath;
    private static MutableCp spiCp;
    private static MutableCp spiSrc;

    public ClassIndexTest (String name) {
        super (name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        File cache = new File(getWorkDir(), "cache");       //NOI18N
        cache.mkdirs();
        IndexUtil.setCacheFolder(cache);
        File src = new File(getWorkDir(), "src");           //NOI18N
        src.mkdirs();
        srcRoot = FileUtil.toFileObject(src);
        srcRoot.createFolder("foo");                        //NOI18N
        src = new File(getWorkDir(), "src2");               //NOI18N
        src.mkdirs();
        srcRoot2 = FileUtil.toFileObject(src);
        src = new File(getWorkDir(), "lib");               //NOI18N
        src.mkdirs();
        binRoot = FileUtil.toFileObject(src);
        src = new File(getWorkDir(), "lib2");               //NOI18N
        src.mkdirs();
        binRoot2 = FileUtil.toFileObject(src);
        src = new File(getWorkDir(), "lib2Src");            //NOI18N
        src.mkdirs();
        libSrc2 = FileUtil.toFileObject(src);
        spiSrc = new MutableCp (Collections.singletonList(ClassPathSupport.createResource(srcRoot.getURL())));
        sourcePath = ClassPathFactory.createClassPath(spiSrc);
        spiCp = new MutableCp ();
        compilePath = ClassPathFactory.createClassPath(spiCp);
        bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        MockServices.setServices(ClassPathProviderImpl.class, SFBQ.class);
    }

    @Override
    protected void tearDown() throws Exception {
        MockServices.setServices();
    }

    public void testEvents () throws Exception {
        GlobalPathRegistry.getDefault().register(ClassPath.BOOT, new ClassPath[] {bootPath});
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] {compilePath});
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {sourcePath});
        IndexingManager.getDefault().refreshIndexAndWait(srcRoot.getURL(), null);
        final ClasspathInfo cpi = ClasspathInfo.create(srcRoot);
        final ClassIndex index = cpi.getClassIndex();
        index.getPackageNames("org", true, EnumSet.of(ClassIndex.SearchScope.SOURCE));
        final CIL testListener = new CIL ();
        index.addClassIndexListener(testListener);

        Set<EventType> et = EnumSet.of(EventType.TYPES_ADDED);
        testListener.setExpectedEvents (et);
        createFile ("foo/A.java", "package foo;\n public class A {}");
        assertTrue("TestListener returned false instead of true.", testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());        
        
        
        et = EnumSet.of(EventType.TYPES_CHANGED);
        testListener.setExpectedEvents (et);
        createFile ("foo/A.java", "package foo;\n public class A extends Object {}");
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());
        
        
        et = EnumSet.of(EventType.TYPES_REMOVED);
        testListener.setExpectedEvents (et);
        deleteFile("foo/A.java");
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());
        
        
        et = EnumSet.of (EventType.ROOTS_ADDED);
        testListener.setExpectedEvents(et);
        List<PathResourceImplementation> impls = new ArrayList<PathResourceImplementation>();
        impls.add (ClassPathSupport.createResource(srcRoot.getURL()));
        impls.add (ClassPathSupport.createResource(srcRoot2.getURL()));
        spiSrc.setImpls(impls);
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());

        Thread.sleep(500);

        et = EnumSet.of (EventType.ROOTS_REMOVED);
        testListener.setExpectedEvents(et);
        impls = new ArrayList<PathResourceImplementation>();
        impls.add (ClassPathSupport.createResource(srcRoot.getURL()));
        spiSrc.setImpls(impls);
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());
        
        et = EnumSet.of (EventType.ROOTS_ADDED);
        testListener.setExpectedEvents(et);
        impls = new ArrayList<PathResourceImplementation>();
        impls.add (ClassPathSupport.createResource(binRoot.getURL()));
        spiCp.setImpls(impls);
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());

        et = EnumSet.of (EventType.ROOTS_REMOVED);
        testListener.setExpectedEvents(et);
        spiCp.setImpls(Collections.<PathResourceImplementation>emptyList());
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());
        
        et = EnumSet.of (EventType.ROOTS_ADDED);
        testListener.setExpectedEvents(et);
        impls = new ArrayList<PathResourceImplementation>();
        impls.add (ClassPathSupport.createResource(binRoot2.getURL()));
        spiCp.setImpls(impls);
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());

        et = EnumSet.of (EventType.ROOTS_REMOVED);
        testListener.setExpectedEvents(et);
        spiCp.setImpls(Collections.<PathResourceImplementation>emptyList());
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());


        //Root Added should NOT be fired by registration of new source root
        //outside these ClassPaths, but should be fired by other ClassIndex
        et = EnumSet.noneOf (EventType.class);
        testListener.setExpectedEvents(et);
        ClassPath srcPath2 = ClassPathSupport.createClassPath(new FileObject[]{libSrc2});
        ClasspathInfo cpInfo2 = ClasspathInfo.create(ClassPathSupport.createClassPath(new URL[0]),
                ClassPathSupport.createClassPath(new URL[0]), srcPath2);
        ClassIndex ci2 = cpInfo2.getClassIndex();
        CIL testListener2 = new CIL ();
        ci2.addClassIndexListener(testListener2);
        EnumSet<EventType> et2 = EnumSet.of (EventType.ROOTS_ADDED);
        testListener2.setExpectedEvents(et2);

        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[]{srcPath2});
        assertTrue(testListener2.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et2, testListener2.getEventLog());
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());
        ci2.removeClassIndexListener(testListener2);
        ci2 = null;
        
        //Root Added should be fired by registration of new binary root pointing to already registered source root
        et = EnumSet.of (EventType.ROOTS_ADDED);
        testListener.setExpectedEvents(et);
        impls = new ArrayList<PathResourceImplementation>();
        impls.add (ClassPathSupport.createResource(binRoot2.getURL()));
        spiCp.setImpls(impls);
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());

        //Root Removed should be called on ClassIndex 1
        et = EnumSet.of (EventType.ROOTS_REMOVED);
        testListener.setExpectedEvents(et);
        spiCp.setImpls(Collections.<PathResourceImplementation>emptyList());
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());
    }
    
    public void testholdsWriteLock () throws Exception {
        //Test basics
        final ClassIndexManager m = ClassIndexManager.getDefault();
        m.readLock(new ClassIndexManager.ExceptionAction<Void>() {
            public Void run() throws IOException, InterruptedException {
                assertFalse(m.holdsWriteLock());
                return null;
            }
        });
        m.writeLock(new ClassIndexManager.ExceptionAction<Void>() {
            public Void run() throws IOException, InterruptedException {
                assertTrue(m.holdsWriteLock());
                return null;
            }
        });
        //Test nesting of [write|read] lock in write lock
        //the opposite is forbidden
        m.writeLock(new ClassIndexManager.ExceptionAction<Void>() {
            public Void run() throws IOException, InterruptedException {                           
                assertTrue(m.holdsWriteLock());
                m.writeLock(new ClassIndexManager.ExceptionAction<Void>() {
                    public Void run() throws IOException, InterruptedException {                
                        assertTrue(m.holdsWriteLock());
                        return null;
                    }
                });
                assertTrue(m.holdsWriteLock());
                m.writeLock(new ClassIndexManager.ExceptionAction<Void>() {
                    public Void run() throws IOException, InterruptedException {                
                        assertTrue(m.holdsWriteLock());
                        return null;
                    }
                });                
                assertTrue(m.holdsWriteLock());
                return null;
            }
        });
    }
    
    
    private static void assertExpectedEvents (final Set<EventType> et, final List<? extends EventRecord> eventLog) {
        assert et != null;
        assert eventLog != null;
        for (Iterator<? extends EventRecord> it = eventLog.iterator(); it.hasNext(); ) {
            EventRecord rec = it.next();
            if (et.remove(rec.type)) {
                it.remove();
            }
        }
        assertTrue (et.isEmpty());
        assertTrue (eventLog.isEmpty());
    }
    
    private static void createFile (final String path, final String content) throws IOException {
        assert srcRoot != null && srcRoot.isValid();
        srcRoot.getFileSystem().runAtomicAction(new FileSystem.AtomicAction () {
            public void run () throws IOException {
                final FileObject data = FileUtil.createData(srcRoot, path);                
                assert data != null;
                final FileLock lock = data.lock();
                try {
                    PrintWriter out = new PrintWriter (new OutputStreamWriter (data.getOutputStream(lock)));
                    try {
                        out.print (content);
                    } finally {
                        out.close ();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        });                
    }
    
    private static void deleteFile (final String path) throws IOException {
        assert srcRoot != null && srcRoot.isValid();
        final FileObject data  = srcRoot.getFileObject(path);        
        if (data != null) {
            //Workaround of issue #126367
            final FileObject parent = data.getParent();
            data.delete();
        }
    }
       

    public static class ClassPathProviderImpl implements ClassPathProvider {

        public ClassPath findClassPath(final FileObject file, final String type) {
            final FileObject[] roots = sourcePath.getRoots();
            for (FileObject root : roots) {
                if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                    if (type == ClassPath.SOURCE) {
                        return sourcePath;
                    }
                    if (type == ClassPath.COMPILE) {
                        return compilePath;
                    }
                    if (type == ClassPath.BOOT) {
                        return bootPath;
                    }
                }
            }
            if (libSrc2.equals(file) || FileUtil.isParentOf(libSrc2, file)) {
                if (type == ClassPath.SOURCE) {
                        return ClassPathSupport.createClassPath(new FileObject[]{libSrc2});
                    }
                    if (type == ClassPath.COMPILE) {
                        return ClassPathSupport.createClassPath(new URL[0]);
                    }
                    if (type == ClassPath.BOOT) {
                        return bootPath;
                    }
            }
            return null;
        }        
    }
    
    public static class SFBQ implements SourceForBinaryQueryImplementation {

        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) { 
            try {
                if (binaryRoot.equals(binRoot2.getURL())) {
                    return new SourceForBinaryQuery.Result () {

                        public FileObject[] getRoots() {
                            return new FileObject[] {libSrc2};
                        }

                        public void addChangeListener(ChangeListener l) {
                        }

                        public void removeChangeListener(ChangeListener l) {
                        }

                    };
                }
            } catch (FileStateInvalidException e) {}
            return null;
        }
        
    }
    
    private static enum EventType {
        TYPES_ADDED,
        TYPES_REMOVED,
        TYPES_CHANGED,
        ROOTS_ADDED,
        ROOTS_REMOVED
    }
    
    private static final class EventRecord {
        
        private final EventType type;
        private final TypesEvent typesEvent;
        private final RootsEvent rootsEvent;
        
        public EventRecord (final EventType type, final TypesEvent event) {
            assert type == EventType.TYPES_ADDED || type == EventType.TYPES_REMOVED || type == EventType.TYPES_CHANGED;
            this.type = type;
            this.typesEvent = event;
            this.rootsEvent = null;
        }
        
        public EventRecord (final EventType type, final RootsEvent event) {
            assert type == EventType.ROOTS_ADDED || type == EventType.ROOTS_REMOVED;
            this.type = type;
            this.typesEvent = null;
            this.rootsEvent = event;
        }

        @Override
        public String toString() {
            return "[" + type +"]";
        }
                
    }
    
    private static final class CIL implements ClassIndexListener {
        
        private CountDownLatch latch; 
        private Set<EventType> expectedEvents;
        private final List<EventRecord> eventsLog = new LinkedList<EventRecord> ();

        public void typesAdded(final TypesEvent event) {
            eventsLog.add(new EventRecord (EventType.TYPES_ADDED, event));
            if (expectedEvents.remove(EventType.TYPES_ADDED)) {
                latch.countDown();
            }
        }

        public void typesRemoved(final TypesEvent event) {
            eventsLog.add (new EventRecord (EventType.TYPES_REMOVED, event));
            if (expectedEvents.remove(EventType.TYPES_REMOVED)) {
                latch.countDown();
            }
        }

        public void typesChanged(final TypesEvent event) {
            eventsLog.add (new EventRecord (EventType.TYPES_CHANGED, event));
            if (expectedEvents.remove(EventType.TYPES_CHANGED)) {
                latch.countDown();
            }
        }

        public void rootsAdded(final RootsEvent event) {
            eventsLog.add (new EventRecord (EventType.ROOTS_ADDED, event));
            if (expectedEvents.remove(EventType.ROOTS_ADDED)) {
                latch.countDown();
            }
        }

        public void rootsRemoved(final RootsEvent event) {
            eventsLog.add (new EventRecord (EventType.ROOTS_REMOVED, event));
            if (expectedEvents.remove(EventType.ROOTS_REMOVED)) {
                latch.countDown();
            }
        }
                
        public List<? extends EventRecord> getEventLog () {
            return new LinkedList<EventRecord> (this.eventsLog);
        }
        
        public void setExpectedEvents (final Set<EventType> et) {
            assert et != null;
            assert this.latch == null;
            this.expectedEvents = EnumSet.copyOf(et);            
            this.eventsLog.clear();
            latch = new CountDownLatch (this.expectedEvents.size());            
        }
        
        public boolean awaitEvent (int timeout, TimeUnit tu) throws InterruptedException {            
            assert this.latch != null;
            final boolean res = latch.await(timeout, tu);
            this.latch = null;
            return res;
        }
        
    }
    
    
    private static final class MutableCp implements ClassPathImplementation {
        
        private final PropertyChangeSupport support;
        private List<? extends PathResourceImplementation> impls;
        
        
        public MutableCp () {
             this (Collections.<PathResourceImplementation>emptyList());
        }
        
        public MutableCp (final List<? extends PathResourceImplementation> impls) {
            assert impls != null;
            support = new PropertyChangeSupport (this);
            this.impls =impls;
        }

        public List<? extends PathResourceImplementation> getResources() {
            return impls;
        }
                
        public void addPropertyChangeListener(final PropertyChangeListener listener) {
            assert listener != null;
            this.support.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(final PropertyChangeListener listener) {
            assert listener != null;
            this.support.removePropertyChangeListener(listener);
        }
        
        
        void setImpls (final List<? extends PathResourceImplementation> impls) {
            assert impls != null;
            this.impls = impls;
            this.support.firePropertyChange(PROP_RESOURCES, null, null);
        }
        
    }
}
