/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.java.source.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import junit.framework.*;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Tomas Zezula
 */
public class GlobalSourcePathTest extends NbTestCase {
    
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
    
    static {
        GlobalSourcePathTest.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", GlobalSourcePathTest.Lkp.class.getName());
        Assert.assertEquals(GlobalSourcePathTest.Lkp.class, Lookup.getDefault().getClass());
    }   
    
    public static class Lkp extends ProxyLookup {
        
        private static Lkp DEFAULT;
        
        public Lkp () {
            Assert.assertNull(DEFAULT);
            DEFAULT = this;
            ClassLoader l = Lkp.class.getClassLoader();
            this.setLookups(
                 new Lookup [] {
                    Lookups.metaInfServices(l),
                    Lookups.fixed (new Object[] {DeadLockSFBQImpl.getDefault (), SFBQImpl.getDefault (),l}),
            });
        }
        
    }
    
    
    public GlobalSourcePathTest(String testName) {
        super(testName);
    }
    
    
    public @Override void setUp() throws Exception {
        this.clearWorkDir();
        File _wd = this.getWorkDir();
        FileObject wd = FileUtil.toFileObject(_wd);
        File cacheFolder = new File (_wd,"cache");
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
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
        SFBQImpl q = SFBQImpl.getDefault();
        q.register (bootRoot1,bootSrc1);
        q.register (compRoot1,compSrc1);
        q.register (compRoot2,compSrc2);
        q.register (unknown2,unknownSrc2);
    }
    
    @Override 
    public void tearDown() throws Exception {
    }

    public void testGlobalSourcePath () throws Exception {
        GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
        GlobalSourcePath gcp = GlobalSourcePath.getDefault();
        ClassPathImplementation cpi = ClassPathSupport.createProxyClassPathImplementation(new ClassPathImplementation[] {gcp.getSourcePath(), gcp.getUnknownSourcePath()});
        List<? extends PathResourceImplementation> impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(0,impls.size());
        //Testing classpath registration
        MutableClassPathImplementation mcpi1 = new MutableClassPathImplementation ();
        mcpi1.addResource(this.srcRoot1);
        CPListener l = new CPListener(cpi);
        ClassPath cp1 = ClassPathFactory.createClassPath(mcpi1);
        regs.register(ClassPath.SOURCE,new ClassPath[]{cp1});        
        assertTrue(l.await());
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(1,impls.size());
        assertEquals(srcRoot1.getURL(),impls.get(0).getRoots()[0]);
        //Testing changes in registered classpath
        l = new CPListener(cpi);
        mcpi1.addResource(srcRoot2);
        assertTrue(l.await());
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(2,impls.size());
        assertEquals(new FileObject[] {srcRoot1, srcRoot2},impls);
        l = new CPListener(cpi);
        mcpi1.removeResource(srcRoot1);
        assertTrue(l.await());
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(1,impls.size());
        assertEquals(srcRoot2.getURL(),impls.get(0).getRoots()[0]);
        //Testing adding new ClassPath
        l = new CPListener(cpi);
        MutableClassPathImplementation mcpi2 = new MutableClassPathImplementation ();
        mcpi2.addResource(srcRoot1);
        ClassPath cp2 = ClassPathFactory.createClassPath(mcpi2);
        regs.register (ClassPath.SOURCE, new ClassPath[] {cp2});
        assertTrue(l.await());
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2, srcRoot1},impls);
        l = new CPListener(cpi);
        mcpi2.addResource(srcRoot3);
        assertTrue(l.await());
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2, srcRoot1, srcRoot3},impls);
        //Testing removing ClassPath
        l = new CPListener(cpi);
        regs.unregister(ClassPath.SOURCE,new ClassPath[] {cp2});
        assertTrue(l.await());
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2},impls);
        //Testing registering classpath with SFBQ
        l = new CPListener(cpi);
        ClassPath cp3 = ClassPathSupport.createClassPath(new FileObject[] {bootRoot1,bootRoot2});
        regs.register(ClassPath.BOOT,new ClassPath[] {cp3});
        assertTrue(l.await());
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2, bootSrc1},impls);
        l = new CPListener(cpi);
        MutableClassPathImplementation mcpi4 = new MutableClassPathImplementation ();
        mcpi4.addResource (compRoot1);
        ClassPath cp4 = ClassPathFactory.createClassPath(mcpi4);
        regs.register(ClassPath.COMPILE,new ClassPath[] {cp4});
        assertTrue(l.await());
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2, bootSrc1, compSrc1},impls);
        l = new CPListener(cpi);
        mcpi4.addResource(compRoot2);
        assertTrue(l.await());
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2, bootSrc1, compSrc1, compSrc2},impls);
        l = new CPListener(cpi);
        mcpi4.removeResource(compRoot1);
        assertTrue(l.await());
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2, bootSrc1, compSrc2},impls);
        l = new CPListener(cpi);
        regs.unregister(ClassPath.BOOT,new ClassPath[] {cp3});
        assertTrue(l.await());
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2, compSrc2},impls);
        //Testing listening on SFBQ.Results
        l = new CPListener(cpi);
        SFBQImpl.getDefault().register(compRoot2,compSrc1);
        assertTrue(l.await());
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2, compSrc1},impls);
        l = new CPListener(cpi);
        SFBQImpl.getDefault().register(compRoot2,compSrc2);
        assertTrue(l.await());
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2, compSrc2},impls);                                    
        //Test unknown source roots                
        ClassPath ucp = ClassPathSupport.createClassPath(new FileObject[] {unknown1, unknown2});
        gcp.getSourceRootForBinaryRoot(unknown1.getURL(),ucp,true);
        gcp.getSourceRootForBinaryRoot(unknown2.getURL(),ucp,true);
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2, compSrc2, unknownSrc2},impls);
        //Test unknown source root after gc
        ucp = null;
        gc(); gc();
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2, compSrc2},impls);
        //Test reregistration of unknown cp
        ucp = ClassPathSupport.createClassPath(new FileObject[] {unknown1, unknown2});
        gcp.getSourceRootForBinaryRoot(unknown1.getURL(),ucp,true);
        gcp.getSourceRootForBinaryRoot(unknown2.getURL(),ucp,true);
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2, compSrc2, unknownSrc2},impls);
        ClassPath rcp = ClassPathSupport.createClassPath(new FileObject[] {unknown1, unknown2});
        regs.register(ClassPath.COMPILE,new ClassPath[] {rcp});
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2, compSrc2, unknownSrc2},impls);
        ucp = null;
        gc(); gc();
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2, compSrc2, unknownSrc2},impls);
        regs.unregister(ClassPath.COMPILE,new ClassPath[] {rcp});
        impls = cpi.getResources();
        assertNotNull (impls);
        assertEquals(new FileObject[] {srcRoot2, compSrc2},impls);
    }
    
    /**
     * Simulates a project mutex deadlock
     */
    public void testProjectMutexDeadlock () throws Exception {
        ExecutorService es = Executors.newSingleThreadExecutor();
        final CountDownLatch wk_ready = new CountDownLatch (1);
        final CountDownLatch mt_ready = new CountDownLatch (1);
        try {
            es.submit(new Runnable () {
                public void run () {
                    try {
                        Object lock = DeadLockSFBQImpl.getDefault().getLock();
                        ClassPath cp = ClassPathSupport.createClassPath(new FileObject[] {unknown1});
                        synchronized (lock) {
                            wk_ready.countDown();
                            mt_ready.await();
                            Thread.sleep(1000);
                            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] {cp});
                        } 
                    } catch (InterruptedException e) {                        
                    }
                }
            });
            ClassPath cp = ClassPathSupport.createClassPath(new FileObject[] {unknown1});
            wk_ready.await();
            mt_ready.countDown();
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] {cp});
        } finally {
            es.shutdownNow();
        }
    }
    
    
    public void testRaceCondition () throws Exception {
        final GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
        final GlobalSourcePath gcp = GlobalSourcePath.getDefault();        
        final int initialSize = gcp.getSourcePath().getResources().size();
        final CountDownLatch state_1 = new CountDownLatch (1);
        final CountDownLatch state_2 = new CountDownLatch (1);
        final CountDownLatch state_3 = new CountDownLatch (1);        
        final CountDownLatch state_4 = new CountDownLatch (1);        
        final ExecutorService es = Executors.newSingleThreadExecutor();
        gcp.setDebugCallBack(new Runnable (){
            public void run () {
                try {
                    state_2.countDown();
                    state_3.await();
                } catch (InterruptedException ie) {}
            }
        });
        try {
            es.submit(new Runnable() {
                public void run () {
                    try {
                        state_1.await();
                        gcp.getSourcePath().getResources();                        
                        gcp.getSourcePath().getResources();
                        state_4.countDown();
                    } catch (InterruptedException ie) {}
                }
            });
            regs.register(ClassPath.SOURCE, new ClassPath[] {ClassPathSupport.createClassPath(new URL[] {new URL("file:///foo1/")})});
            state_1.countDown();
            state_2.await();
            regs.register(ClassPath.SOURCE, new ClassPath[] {ClassPathSupport.createClassPath(new URL[] {new URL("file:///foo2/")})});            
            state_3.countDown();
            state_4.await();
            assertEquals("Race condition",initialSize+2,gcp.getSourcePath().getResources().size());
        } finally {
            es.shutdownNow();
        }
    }
    
    public void testRaceCondition2 () throws Exception {
        final GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
        final GlobalSourcePath gcp = GlobalSourcePath.getDefault();        
        final ClassPathImplementation cp = gcp.getSourcePath();
        final int initialSize = cp.getResources().size();        
        final CountDownLatch state_1 = new CountDownLatch (1);
        final CountDownLatch state_2 = new CountDownLatch (1);
        final CountDownLatch state_3 = new CountDownLatch (1);        
        final CountDownLatch state_4 = new CountDownLatch (1);        
        final ExecutorService es = Executors.newSingleThreadExecutor();
        gcp.setDebugCallBack(new Runnable (){
            public void run () {
                try {
                    state_2.countDown();
                    state_3.await();
                } catch (InterruptedException ie) {}
            }
        });
        try {
            es.submit(new Runnable() {
                public void run () {
                    try {
                        state_1.await();
                        cp.getResources();
                        cp.getResources();
                        state_4.countDown();
                    } catch (InterruptedException ie) {}
                }
            });
            regs.register(ClassPath.SOURCE, new ClassPath[] {ClassPathSupport.createClassPath(new URL[] {new URL("file:///foo3/")})});
            state_1.countDown();
            state_2.await();
            regs.register(ClassPath.SOURCE, new ClassPath[] {ClassPathSupport.createClassPath(new URL[] {new URL("file:///foo4/")})});            
            state_3.countDown();
            state_4.await();
            assertEquals("Race condition",initialSize+2, cp.getResources().size());
        } finally {
            es.shutdownNow();
        }
    }
    
    public void testBinaryPath () throws Exception {
        Set<ClassPath> cps = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
        GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cps.toArray(new ClassPath[cps.size()]));
        cps = GlobalPathRegistry.getDefault().getPaths(ClassPath.BOOT);
        GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cps.toArray(new ClassPath[cps.size()]));
        cps = GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE);
        GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cps.toArray(new ClassPath[cps.size()]));
        ClassPathImplementation sourcePath = GlobalSourcePath.getDefault().getSourcePath();
        ClassPathImplementation binaryPath = GlobalSourcePath.getDefault().getBinaryPath();
        assertEquals (0,sourcePath.getResources().size());
        assertEquals (0,binaryPath.getResources().size());
        
        
        ClassPath src = ClassPathSupport.createClassPath(new FileObject[] {srcRoot1, srcRoot2, srcRoot3});
        ClassPath compile = ClassPathSupport.createClassPath(new FileObject[] {compRoot1, compRoot2});
        ClassPath boot = ClassPathSupport.createClassPath(new FileObject[] {bootRoot1, bootRoot2});
        
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {src});
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] {compile});
        GlobalPathRegistry.getDefault().register(ClassPath.BOOT, new ClassPath[] {boot});
        
        List<? extends PathResourceImplementation> res = sourcePath.getResources();
        assertEquals(new FileObject[] {srcRoot1, srcRoot2, srcRoot3, compSrc1, compSrc2, bootSrc1}, res);
        res = binaryPath.getResources();
        assertEquals(new FileObject[] {bootRoot2}, res);
        
        ClassPath compile2 = ClassPathSupport.createClassPath(new FileObject[] {unknown1, unknown2});
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] {compile2});
        
        res = sourcePath.getResources();
        assertEquals(new FileObject[] {srcRoot1, srcRoot2, srcRoot3, compSrc1, compSrc2, bootSrc1, unknownSrc2}, res);
        res = binaryPath.getResources();
        assertEquals(new FileObject[] {bootRoot2, unknown1}, res);
    }
    
    
    
    public void testExcludeEvents () throws Exception {
        List<PRI> resources = new ArrayList<PRI>(2);
        resources.add(new PRI (srcRoot1.getURL()));
        resources.add(new PRI (srcRoot2.getURL()));
        ClassPath cp = ClassPathSupport.createClassPath(resources);
        
        class L implements PropertyChangeListener {
            
            Set ids = new HashSet ();
        
            public void propertyChange(PropertyChangeEvent e) {
                if (ClassPath.PROP_INCLUDES.equals(e.getPropertyName())) {
                    ids.add (e.getPropagationId());
                }
            }
        };
        L l = new L ();
        cp.addPropertyChangeListener(l);
        Object propId = "ID0";
        for (PRI pri : resources) {
            pri.firePropertyChange(propId);
        }
        cp.getRoots();
        propId = "ID1";
        for (PRI pri : resources) {
            pri.firePropertyChange(propId);
        }
        assertEquals(1, l.ids.size());
        propId = "ID2";
        for (PRI pri : resources) {
            pri.firePropertyChange(propId);
        }
        assertEquals(2, l.ids.size());
    }
    
    public void testSetUseLibraries () {
        GlobalSourcePathTestUtil.setUseLibraries(true);     //Assertion error shouldn't be thrown
        try {
            GlobalSourcePath.getDefault().setUseLibraries(true);
            assertTrue(false);
        } catch (AssertionError e) {
            //AssertionError has to be thrown
        }
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
    
    private static void gc () {
        System.gc();
        long[][] spaces = new long[100][];
        for (int i=0; i<spaces.length; i++) {
            spaces[i] = new long[1000];
        }
        System.gc();
        spaces = null;
        System.gc();
    }
    
    private void assertEquals (FileObject[] expected, List<? extends PathResourceImplementation> result) throws IOException {
        assertEquals(expected.length, result.size());
        Set<URL> expectedUrls = new HashSet ();
        for (FileObject fo : expected) {
            expectedUrls.add(fo.getURL());
        }
        for (PathResourceImplementation impl : result) {
            URL url = impl.getRoots()[0];
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
            res = new ArrayList ();
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

        public List getResources() {
            return res;
        }
        
    }
    
    private static class SFBQImpl implements SourceForBinaryQueryImplementation {
        
        private static SFBQImpl instance;
        
        final Map<URL,FileObject> map = new HashMap ();
        final Map<URL,Result> results = new HashMap ();

        private SFBQImpl () {
            
        }
        
        public void register (FileObject binRoot, FileObject sourceRoot) throws IOException {
            URL url = binRoot.getURL();
            this.map.put (url,sourceRoot);
            Result r = results.get (url);
            if (r != null) {
                r.update (sourceRoot);
            }
        }
        
        public void unregister (FileObject binRoot) throws IOException {
            URL url = binRoot.getURL();
            this.map.remove(url);
            Result r = results.get (url);
            if (r != null) {
                r.update (null);
            }
        }
        
        public void clean () {
            this.map.clear();
            this.results.clear();
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
        
        public static synchronized SFBQImpl getDefault () {
            if (instance == null) {
                instance = new SFBQImpl ();
            }
            return instance;
        }
        
        public static class Result implements SourceForBinaryQuery.Result {
            
            private FileObject root;
            private final List<ChangeListener> listeners;
            
            public Result (FileObject root) {
                this.root = root;
                this.listeners = new LinkedList ();
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
    
    private static class DeadLockSFBQImpl implements SourceForBinaryQueryImplementation {
        
        private static DeadLockSFBQImpl instance;
        
        private final Object lock;
        
        private DeadLockSFBQImpl () {
            this.lock = new String ("Lock");            
        }
        
        public Object getLock () {
            return this.lock;
        }
        
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            synchronized (lock) {
                lock.toString();
            }
            return null;
        }
        
        public static synchronized DeadLockSFBQImpl getDefault () {
            if (instance == null) {
                instance = new DeadLockSFBQImpl ();
            }
            return instance;
        }
        
    }
    
    private static class CPListener implements PropertyChangeListener {
        
        private final CountDownLatch latch = new CountDownLatch(1);
        private final ClassPathImplementation impl;
        
        public CPListener (final ClassPathImplementation impl) {
            assert impl != null;
            this.impl = impl;
            this.impl.addPropertyChangeListener(WeakListeners.propertyChange(this, this.impl));
        }
        
        public boolean await () throws InterruptedException {
            return this.latch.await(5000, TimeUnit.MILLISECONDS);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (this.impl == evt.getSource() && 
                ClassPathImplementation.PROP_RESOURCES.equals(evt.getPropertyName())) {
                latch.countDown();
            }
        }        
    }
    
}
