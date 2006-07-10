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
package org.netbeans.modules.java.classpath;


import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import junit.framework.TestCase;
import junit.framework.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.WeakListeners;

/**
 *
 * @author tom
 */
public class ProxyClassPathImplementationTest extends NbTestCase {
    
    public ProxyClassPathImplementationTest(String testName) {
        super(testName);
    }
    
    public void testResources () throws Exception {
        final URL url1 = new URL ("file:///tmp/a/");
        final URL url2 = new URL ("file:///tmp/b/");
        final URL url3 = new URL ("file:///tmp/b/");
        
        final ClassPath cp1 = ClassPathSupport.createClassPath(new URL[] {url1, url2});
        final ClassPath cp2 = ClassPathSupport.createClassPath(new URL[] {url3});
        final ClassPath prxCp = ClassPathSupport.createProxyClassPath(new ClassPath[] {cp1,cp2});
        List<ClassPath.Entry> entries = prxCp.entries();
        assertEquals(3,entries.size());
        assertEquals(url1,entries.get(0).getURL());
        assertEquals(url2,entries.get(1).getURL());
        assertEquals(url3,entries.get(2).getURL());
    }

    public void testDeadLock() throws Exception{
        List<PathResourceImplementation> resources = Collections.<PathResourceImplementation>emptyList();
        final ReentrantLock lock = new ReentrantLock (false);
        final CountDownLatch signal = new CountDownLatch (1);
        final ClassPath cp = ClassPathFactory.createClassPath(ClassPathSupport.createProxyClassPathImplementation(new ClassPathImplementation[] {new LockClassPathImplementation (resources,lock, signal)}));
        lock.lock();
        final ExecutorService es = Executors.newSingleThreadExecutor();        
        try {
            es.submit(new Runnable () {
                public void run () {
                    cp.entries();
                }
            });
            signal.await();
            cp.entries();
        } finally {
            es.shutdownNow();
        }
    }  
    
    
    private class LockClassPathImplementation implements ClassPathImplementation {
        
        private List<? extends PathResourceImplementation> resources;
        private ReentrantLock lock;
        private CountDownLatch signal;
        
        public LockClassPathImplementation (final List<? extends PathResourceImplementation> resources, final ReentrantLock lock, final CountDownLatch signal) {
            this.resources = resources;
            this.lock = lock;
            this.signal = signal;
        }
        
        public List<? extends PathResourceImplementation> getResources() {
            this.signal.countDown();
            this.lock.lock();
            return this.resources;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        
    }
    
    
}
