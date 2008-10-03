/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.java.source.usages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.ClassPathRootsListener.ClassPathRootsChangedListener;
import org.netbeans.modules.java.source.usages.fcs.FileChangeSupport;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class ClassPathRootsListenerTest extends NbTestCase {
    
    public ClassPathRootsListenerTest(String testName) {
        super(testName);
    }            

    protected void setUp() throws Exception {
        clearWorkDir();
        
        File workDir = getWorkDir();
        
        work = new File(workDir, "work");
        
        work.mkdirs();
        
        File cache = new File(workDir, "cache");
        
        cache.mkdirs();
        
        FileUtil.refreshFor(workDir);
        
        Index.setCacheFolder(cache);
        
        super.setUp();
    }
    
    private File work;
    
    public void testClassPathListener() throws Exception {
        File a = new File(work, "a/"); URL au = new URL("file:" + a.getAbsolutePath() + "/");
        File b = new File(work, "b/"); URL bu = new URL("file:" + b.getAbsolutePath() + "/");
        File c = new File(work, "c/"); URL cu = new URL("file:" + c.getAbsolutePath() + "/");
        
        a.mkdirs();
        
        PathResourceImpl i = new PathResourceImpl();
        
        i.setURLs(au, bu);
        
        ClassPath cp = ClassPathSupport.createClassPath(Collections.singletonList(i));
        
        final AtomicInteger counter = new AtomicInteger();
        
        ClassPathRootsChangedListener l = new ClassPathRootsChangedListener() {
            public void rootsChanged(Collection<ClassPath> forCPs, File f) {
                counter.incrementAndGet();
            }
        };
        
        ClassPathRootsListener.getDefault().addClassPathRootsListener(cp, false, l);
        
        assertNotNull(FileUtil.createFolder(b));
        
        assertEquals(1, counter.get());
        
        FileUtil.toFileObject(a).delete();
        
        assertEquals(2, counter.get());
        
        i.setURLs(au, cu);
        
        FileUtil.toFileObject(b).delete();

        assertEquals(3, counter.get());
        
        assertNotNull(FileUtil.createFolder(c));
        
        assertEquals(4, counter.get());
        
        i = null;
        cp = null;
        l = null;
        
        FileChangeSupport.DEFAULT.purge();
        
        checkWeakMapGCAble(ClassPathRootsListener.getDefault().classPath2Roots);
        checkWeakMapGCAble(ClassPathRootsListener.getDefault().cp2Listeners);
        checkWeakMapGCAble(ClassPathRootsListener.getDefault().entry2File);
        checkWeakMapGCAble(ClassPathRootsListener.getDefault().entry2Listener);
        
        try {
            assertGC("will fail", new WeakReference(this));
        } catch (AssertionFailedError e) {}
        
        FileChangeSupport.DEFAULT.purge();
        checkWeakMapGCAble(ClassPathRootsListener.getDefault().file2ClassPaths);
        checkWeakMapGCAble(ClassPathRootsListener.getDefault().file2Listener);
        checkWeakMapGCAble(ClassPathRootsListener.getDefault().fileNormalizationFacility);
    }

    private void checkWeakMapGCAble(Map<?, ?> map) {
        List<Reference<Object>> l = new  LinkedList<Reference<Object>>();
        
        for (Object o : map.keySet()) {
            l.add(new WeakReference<Object>(o));
        }
        
        for (Reference<Object> r : l) {
            assertGC("", r);
        }
    }
    
    private static final class PathResourceImpl implements PathResourceImplementation {

        private URL[] urls = new URL[0];
        private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        public synchronized URL[] getRoots() {
            return urls;
        }

        public ClassPathImplementation getContent() {
            return null;
        }

        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        
        public synchronized void setURLs(URL... urls) {
            this.urls = urls;
            pcs.firePropertyChange(PROP_ROOTS, null, urls);
        }
        
    }
}
