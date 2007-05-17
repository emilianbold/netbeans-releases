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
package org.netbeans.modules.java.hints.infrastructure;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class LazyHintComputationTest extends NbTestCase {
    
    /** Creates a new instance of LazyHintComputationTest */
    public LazyHintComputationTest(String name) {
        super(name);
    }
    
    private FileObject data;
    
    @Override
    public void setUp() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        data = fs.getRoot().createData("test.java");
    }
    
    public void testCancel() throws Exception {
        final LazyHintComputation c = new LazyHintComputation(data);
        boolean[] first = new boolean[1];
        boolean[] second = new boolean[1];
        boolean[] third = new boolean[1];
        final boolean[] callback = new boolean[1];
        final boolean[] doCancel = new boolean[] {true};
        final boolean[] firstCancelled = new boolean[1];
        final boolean[] secondCancelled = new boolean[1];
        final boolean[] thirdCancelled = new boolean[1];
        
        LazyHintComputationFactory.addToCompute(data, new CreatorBasedLazyFixListImpl(first, null, new Runnable() {
            public void run() {
                firstCancelled[0] = true;
            }
        }));
        
        LazyHintComputationFactory.addToCompute(data, new CreatorBasedLazyFixListImpl(second, new Runnable() {
            public void run() {
                if (doCancel[0]) {
                    c.cancel();
                    callback[0] = true;
                }
            }
        }, new Runnable() {
            public void run() {
                secondCancelled[0] = true;
            }
        }));
        
        LazyHintComputationFactory.addToCompute(data, new CreatorBasedLazyFixListImpl(third, null, new Runnable() {
            public void run() {
                thirdCancelled[0] = true;
            }
        }));
        
        c.run(null);
        
        assertTrue(first[0]);
        assertTrue(second[0]);
        assertFalse(third[0]);
        assertTrue(callback[0]);
        assertFalse(firstCancelled[0]);
        assertTrue(secondCancelled[0]);
        assertFalse(thirdCancelled[0]);
        
        first[0] = second[0] = callback[0] = secondCancelled[0] = false;
        
        doCancel[0] = false;
        
        c.run(null);
        
        assertFalse(first[0]);
        assertTrue(second[0]);
        assertTrue(third[0]);
        assertFalse(callback[0]);
        assertFalse(firstCancelled[0]);
        assertFalse(secondCancelled[0]);
        assertFalse(thirdCancelled[0]);
    }
    
    public void test88996() throws Exception {
        boolean[] computed = new boolean[1];
        
        CreatorBasedLazyFixListImpl l = new CreatorBasedLazyFixListImpl(data, computed, null, null);
        
        l.getFixes();
        
        Reference r = new WeakReference(l);
        
        l = null;
        
        assertGC("Not holding the CreatorBasedLazyFixList hard", r);
    }
    
    private static final class CreatorBasedLazyFixListImpl extends CreatorBasedLazyFixList {
        
        private final boolean[] marker;
        private final Runnable callback;
        private final Runnable cancelCallback;
        
        public CreatorBasedLazyFixListImpl(FileObject file, boolean[] marker, Runnable callback, Runnable cancelCallback) {
            super(file, null, -1, null, null);
            this.marker = marker;
            this.callback = callback;
            this.cancelCallback = cancelCallback;
        }
        
        public CreatorBasedLazyFixListImpl(boolean[] marker, Runnable callback, Runnable cancelCallback) {
            this(null, marker, callback, cancelCallback);
        }
        
        @Override
        public void compute(CompilationInfo info) {
            marker[0] = true;
            
            if (callback != null)
                callback.run();
        }
        
        @Override
        public void cancel() {
            if (cancelCallback != null) {
                cancelCallback.run();
            }
        }
    }
    
}
