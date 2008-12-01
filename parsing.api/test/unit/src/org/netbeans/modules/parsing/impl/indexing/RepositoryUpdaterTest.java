/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class RepositoryUpdaterTest extends NbTestCase {

    private static final String SOURCES = "FOO_SOURCES";
    private static final String PLATFORM = "FOO_PLATFORM";
    private static final String LIBS = "FOO_LIBS";

    private FileObject srcRoot1;
    private FileObject srcRoot2;
    private FileObject srcRoot3;
    private FileObject compRoot1;
    private FileObject compRoot2;
    private FileObject bootRoot1;
    private FileObject bootRoot2;
    private FileObject compSrc1;
    private FileObject compSrc2;
    private FileObject bootSrc1;
    private FileObject unknown1;
    private FileObject unknown2;
    private FileObject unknownSrc2;

    public RepositoryUpdaterTest (String name) {
        super (name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        final File _wd = this.getWorkDir();
        final FileObject wd = FileUtil.toFileObject(_wd);

        MockServices.setServices(FooPathRecognizer.class, SFBQImpl.class, OpenProject.class);

        assertNotNull("No masterfs",wd);
        srcRoot1 = wd.createFolder("src1");
        assertNotNull(srcRoot1);
        srcRoot2 = wd.createFolder("src2");
        assertNotNull(srcRoot2);
        srcRoot3 = wd.createFolder("src3");
        assertNotNull (srcRoot3);
        compRoot1 = wd.createFolder("comp1");
        assertNotNull (compRoot1);
        compRoot2 = wd.createFolder("comp2");
        assertNotNull (compRoot2);
        bootRoot1 = wd.createFolder("boot1");
        assertNotNull (bootRoot1);
        bootRoot2 = wd.createFolder("boot2");
        assertNotNull (bootRoot2);
        compSrc1 = wd.createFolder("cs1");
        assertNotNull (compSrc1);
        compSrc2 = wd.createFolder("cs2");
        assertNotNull (compSrc2);
        bootSrc1 = wd.createFolder("bs1");
        assertNotNull (bootSrc1);
        unknown1 = wd.createFolder("uknw1");
        assertNotNull (unknown1);
        unknown2 = wd.createFolder("uknw2");
        assertNotNull (unknown2);
        unknownSrc2 = wd.createFolder("uknwSrc2");
        assertNotNull(unknownSrc2);
        SFBQImpl.register (bootRoot1,bootSrc1);
        SFBQImpl.register (compRoot1,compSrc1);
        SFBQImpl.register (compRoot2,compSrc2);
        SFBQImpl.register (unknown2,unknownSrc2);
    }

    public void testPathAddedRemovedChanged () throws Exception {
        //Empty regs test
        RepositoryUpdater ru = RepositoryUpdater.getDefault();
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());

        final TestHandler handler = new TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        //Testing classpath registration
        MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(this.srcRoot1);
        ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        GlobalPathRegistry.getDefault().register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRoot1.getURL(), handler.getSources().get(0));

        //Nothing should be scanned if the same cp is registered again
        handler.reset();
        ClassPath cp1clone = ClassPathFactory.createClassPath(mcpi1);
        GlobalPathRegistry.getDefault().register(SOURCES,new ClassPath[]{cp1clone});        
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());

        //Nothing should be scanned if the cp is unregistered
        handler.reset();        
        GlobalPathRegistry.getDefault().unregister(SOURCES,new ClassPath[]{cp1clone});        
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());

        //Nothing should be scanned after classpath remove
        handler.reset();
        GlobalPathRegistry.getDefault().unregister(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(0, handler.getSources().size());        
        

        //Testing changes in registered classpath - add cp root
        handler.reset();
        GlobalPathRegistry.getDefault().register(SOURCES,new ClassPath[]{cp1});
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(this.srcRoot1.getURL(), handler.getSources().get(0));

        

//        l = new PRListener();
//        mcpi1.addResource(srcRoot2);
//        assertTrue(l.await());
//        result = collectResults ();
//        assertEquals(2,result.size());
//        assertEquals(new FileObject[] {srcRoot1, srcRoot2},result);
//
//        //Testing changes in registered classpath - remove cp root
//        l = new PRListener();
//        mcpi1.removeResource(srcRoot1);
//        assertTrue(l.await());
//        result = collectResults ();
//        assertEquals(1,result.size());
//        assertEquals(srcRoot2.getURL(),result.iterator().next());
//
//        //Testing adding new ClassPath
//        l = new PRListener();
//        MutableClassPathImplementation mcpi2 = new MutableClassPathImplementation ();
//        mcpi2.addResource(srcRoot1);
//        ClassPath cp2 = ClassPathFactory.createClassPath(mcpi2);
//        regs.register (SOURCES, new ClassPath[] {cp2});
//        assertTrue(l.await());
//        result = collectResults ();
//        assertEquals(2,result.size());
//        assertEquals(new FileObject[] {srcRoot2, srcRoot1},result);
//
//        //Testing changes in newly registered classpath - add cp root
//        l = new PRListener();
//        mcpi2.addResource(srcRoot3);
//        assertTrue(l.await());
//        result = collectResults ();
//        assertEquals(3,result.size());
//        assertEquals(new FileObject[] {srcRoot2, srcRoot1, srcRoot3},result);
//
//        //Testing removing ClassPath
//        l = new PRListener();
//        regs.unregister(SOURCES,new ClassPath[] {cp2});
//        assertTrue(l.await());
//        result = collectResults ();
//        assertEquals(1,result.size());
//        assertEquals(new FileObject[] {srcRoot2},result);
//
//        //Testing registering classpath with SFBQ - register PLATFROM
//        l = new PRListener();
//        ClassPath cp3 = ClassPathSupport.createClassPath(new FileObject[] {bootRoot1,bootRoot2});
//        regs.register(PLATFORM,new ClassPath[] {cp3});
//        assertTrue(l.await());
//        result = PathRegistry.getDefault().getSources();
//        assertNotNull (result);
//        assertEquals(2,result.size());
//        assertEquals(new FileObject[] {srcRoot2, bootSrc1},result);
//
//        //Testing registering classpath with SFBQ - register LIBS
//        l = new PRListener();
//        MutableClassPathImplementation mcpi4 = new MutableClassPathImplementation ();
//        mcpi4.addResource (compRoot1);
//        ClassPath cp4 = ClassPathFactory.createClassPath(mcpi4);
//        regs.register(LIBS,new ClassPath[] {cp4});
//        assertTrue(l.await());
//        result = PathRegistry.getDefault().getSources();
//        assertNotNull (result);
//        assertEquals(3,result.size());
//        assertEquals(new FileObject[] {srcRoot2, bootSrc1, compSrc1},result);
//
//        //Testing registering classpath with SFBQ - add into LIBS
//        l = new PRListener();
//        mcpi4.addResource(compRoot2);
//        assertTrue(l.await());
//        result = PathRegistry.getDefault().getSources();
//        assertNotNull (result);
//        assertEquals(4,result.size());
//        assertEquals(new FileObject[] {srcRoot2, bootSrc1, compSrc1, compSrc2},result);
//
//        //Testing registering classpath with SFBQ - remove from LIBS
//        l = new PRListener();
//        mcpi4.removeResource(compRoot1);
//        assertTrue(l.await());
//        result = PathRegistry.getDefault().getSources();
//        assertNotNull (result);
//        assertEquals(3,result.size());
//        assertEquals(new FileObject[] {srcRoot2, bootSrc1, compSrc2},result);
//
//        //Testing registering classpath with SFBQ - unregister PLATFORM
//        l = new PRListener();
//        regs.unregister(PLATFORM,new ClassPath[] {cp3});
//        result = PathRegistry.getDefault().getSources();
//        assertNotNull (result);
//        assertEquals(2,result.size());
//        assertEquals(new FileObject[] {srcRoot2, compSrc2},result);
//
//        //Testing listening on SFBQ.Results - bind source
//        l = new PRListener();
//        SFBQImpl.register(compRoot2,compSrc1);
//        assertTrue(l.await());
//        result = PathRegistry.getDefault().getSources();
//        assertNotNull (result);
//        assertEquals(2,result.size());
//        assertEquals(new FileObject[] {srcRoot2, compSrc1},result);
//
//        //Testing listening on SFBQ.Results - rebind (change) source
//        l = new PRListener();
//        SFBQImpl.register(compRoot2,compSrc2);
//        assertTrue(l.await());
//        result = PathRegistry.getDefault().getSources();
//        assertNotNull (result);
//        assertEquals(2,result.size());
//        assertEquals(new FileObject[] {srcRoot2, compSrc2},result);
    }

    public static class TestHandler extends Handler {

            private CountDownLatch latch;
            private List<URL> sources;
            private Set<URL> binaries;

            public TestHandler () {
                reset();
            }

            public void reset () {
                sources = null;
                binaries = null;
                latch = new CountDownLatch(2);
            }

            public boolean await () throws InterruptedException {
                return latch.await(500000, TimeUnit.MILLISECONDS);
            }

            public Set<URL> getBinaries () {
                return this.binaries;
            }

            public List<URL> getSources() {
                return this.sources;
            }

            @Override
            public void publish(LogRecord record) {
                String msg = record.getMessage();
                if ("scanBinary".equals(msg)) {
                    binaries = (Set<URL>) record.getParameters()[0];
                    latch.countDown();
                }
                else if ("scanSources".equals(msg)) {
                    sources = (List<URL>) record.getParameters()[0];
                    latch.countDown();
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        }
    
    
    
    private static Collection<URL> collectResults () {
        final PathRegistry pr = PathRegistry.getDefault();
        final List<URL> result = new LinkedList<URL>();
        result.addAll(pr.getSources());
        result.addAll(pr.getBinaries());
        result.addAll(pr.getUnknownRoots());
        return result;
    }

    public static class PRI implements FilteringPathResourceImplementation {


        private final URL root;
        private final PropertyChangeSupport support;


        public PRI (URL root) {
            this.root = root;
            this.support = new PropertyChangeSupport (this);
        }


        public boolean includes(URL root, String resource) {
            return true;
        }

        public URL[] getRoots() {
            return new URL[] {root};
        }

        public ClassPathImplementation getContent() {
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            this.support.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            this.support.removePropertyChangeListener(listener);
        }

        public void firePropertyChange (final Object propId) {
            PropertyChangeEvent event = new PropertyChangeEvent (this,FilteringPathResourceImplementation.PROP_INCLUDES,null,null);
            event.setPropagationId(propId);
            this.support.firePropertyChange(event);
        }
    }
    

    private void assertEquals (FileObject[] expected, Collection<? extends URL> result) throws IOException {
        assertEquals(expected.length, result.size());
        Set<URL> expectedUrls = new HashSet<URL> ();
        for (FileObject fo : expected) {
            expectedUrls.add(fo.getURL());
        }
        for (URL url : result) {
            if (!expectedUrls.remove (url)) {
                assertTrue (String.format("Unknown URL: %s in: %s", url, expectedUrls),false);
            }
        }
        assertEquals(expectedUrls.toString(),0,expectedUrls.size());
    }
    

    private static class MutableClassPathImplementation implements ClassPathImplementation {

        private final List<PathResourceImplementation> res;
        private final PropertyChangeSupport support;

        public MutableClassPathImplementation () {
            res = new ArrayList<PathResourceImplementation> ();
            support = new PropertyChangeSupport (this);
        }

        public void addResource (FileObject fo) throws IOException {
            res.add(ClassPathSupport.createResource(fo.getURL()));
            this.support.firePropertyChange(PROP_RESOURCES,null,null);
        }

        public void removeResource (FileObject fo) throws IOException {
            URL url = fo.getURL();
            for (Iterator<PathResourceImplementation> it = res.iterator(); it.hasNext(); ) {
                PathResourceImplementation r = it.next();
                if (url.equals(r.getRoots()[0])) {
                    it.remove();
                    this.support.firePropertyChange(PROP_RESOURCES,null,null);
                }
            }
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        public List<PathResourceImplementation> getResources() {
            return res;
        }

    }

    public static class SFBQImpl implements SourceForBinaryQueryImplementation {

        final static Map<URL,FileObject> map = new HashMap<URL,FileObject> ();
        final static Map<URL,Result> results = new HashMap<URL,Result> ();

        public SFBQImpl () {

        }

        public static void register (FileObject binRoot, FileObject sourceRoot) throws IOException {
            URL url = binRoot.getURL();
            map.put (url,sourceRoot);
            Result r = results.get (url);
            if (r != null) {
                r.update (sourceRoot);
            }
        }

        public static void unregister (FileObject binRoot) throws IOException {
            URL url = binRoot.getURL();
            map.remove(url);
            Result r = results.get (url);
            if (r != null) {
                r.update (null);
            }
        }

        public static void clean () {
            map.clear();
            results.clear();
        }

        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            FileObject srcRoot = this.map.get(binaryRoot);
            if (srcRoot == null) {
                return null;
            }
            Result r = results.get (binaryRoot);
            if (r == null) {
                r = new Result (srcRoot);
                results.put(binaryRoot, r);
            }
            return r;
        }
        
        public static class Result implements SourceForBinaryQuery.Result {

            private FileObject root;
            private final List<ChangeListener> listeners;

            public Result (FileObject root) {
                this.root = root;
                this.listeners = new LinkedList<ChangeListener> ();
            }

            public void update (FileObject root) {
                this.root = root;
                fireChange ();
            }

            public synchronized void addChangeListener(ChangeListener l) {
                this.listeners.add(l);
            }

            public synchronized void removeChangeListener(ChangeListener l) {
                this.listeners.remove(l);
            }

            public FileObject[] getRoots() {
                if (this.root == null) {
                    return new FileObject[0];
                }
                else {
                    return new FileObject[] {this.root};
                }
            }

            private void fireChange () {
                ChangeListener[] _listeners;
                synchronized (this) {
                    _listeners = this.listeners.toArray(new ChangeListener[this.listeners.size()]);
                }
                ChangeEvent event = new ChangeEvent (this);
                for (ChangeListener l : _listeners) {
                    l.stateChanged (event);
                }
            }
        }

    }

    
    public static class FooPathRecognizer extends PathRecognizer {
        
        @Override
        public Set<String> getSourcePathIds() {
            return Collections.singleton(SOURCES);
        }

        @Override
        public Set<String> getBinaryPathIds() {
            final Set<String> res = new HashSet<String>();
            res.add(PLATFORM);
            res.add(LIBS);
            return res;
        }

        @Override
        public Set<String> getMimeType() {
            return Collections.singleton("text/foo");
        }        

    }    

    public static class OpenProject implements  OpenProjectsTrampoline {

        public Project[] getOpenProjectsAPI() {
            return new Project[0];
        }

        public void openAPI(Project[] projects, boolean openRequiredProjects) {

        }

        public void closeAPI(Project[] projects) {

        }

        public void addPropertyChangeListenerAPI(PropertyChangeListener listener, Object source) {
            
        }

        public Future<Project[]> openProjectsAPI() {
            return new Future<Project[]>() {

                public boolean cancel(boolean mayInterruptIfRunning) {
                    return true;
                }

                public boolean isCancelled() {
                    return false;
                }

                public boolean isDone() {
                    return true;
                }

                public Project[] get() throws InterruptedException, ExecutionException {
                    return new Project[0];
                }

                public Project[] get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    return new Project[0];
                }
            };
        }

        public void removePropertyChangeListenerAPI(PropertyChangeListener listener) {
            
        }

        public Project getMainProject() {
            return null;
        }

        public void setMainProject(Project project) {
            
        }

    }

}
