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

package org.openide.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import junit.framework.*;
import org.netbeans.junit.*;

public class WeakSetTest extends NbTestCase {

    public WeakSetTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(WeakSetTest.class));
    }

    public void testToArrayMayContainNullsIssue42271 () {
        class R implements Runnable {
            Object[] arr;
            Object last;
            
            public R () {
                int cnt = 10;
                arr = new Object[cnt];
                for (int i = 0; i < cnt; i++) {
                    arr[i] = new Integer (i);
                }
            }
            
            
            public void run () {
                
                WeakReference r = new WeakReference (last);
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = null;
                }
                arr = null;
                last = null;
                assertGC ("Last item has to disappear", r);
            }
            
            public void putToSet (NotifyWhenIteratedSet s) {
                for (int i = 0; i < arr.length; i++) {
                    s.add (arr[i]);
                }
                assertEquals (arr.length, s.size ());
                Iterator it = s.superIterator ();
                Object prev = it.next ();
                while (it.hasNext ()) {
                    prev = it.next ();
                }
                last = prev;
            }
        }
        R r = new R ();
        
        
        
        NotifyWhenIteratedSet ws = new NotifyWhenIteratedSet (r, 1);
        
        r.putToSet (ws);
        
        Object[] arr = ws.toArray ();
        for (int i = 0; i < arr.length; i++) {
            assertNotNull (i + "th index should not be null", arr[i]);
        }
    }
    
    private static final class NotifyWhenIteratedSet extends WeakSet {
        private Runnable run;
        private int cnt;
        
        public NotifyWhenIteratedSet (Runnable run, int cnt) {
            this.run = run;
            this.cnt = cnt;
        }
        
        public Iterator superIterator () {
            return super.iterator ();
        }
        
        public Iterator iterator () {
            final Iterator it = super.iterator ();
            class I implements Iterator {
                public boolean hasNext() {
                    return it.hasNext ();
                }

                public Object next() {
                    if (--cnt == 0) {
                        run.run ();
                    }
                    return it.next ();
                }

                public void remove() {
                    it.remove();
                }
            }
            return new I ();
        }
    }
    
    private final static class TestObj {
        static final Set<TestObj> testObjs = new WeakSet<TestObj>();
        private final String name;
        TestObj(String name) {
            this.name = name;
            synchronized (testObjs) {
                testObjs.add(this);
            }
        }

        @Override
        public String toString() {
            return name;
        }
        
    }
    
    private final static class GC implements Runnable {

        public void run() {
            try {
                for (int i = 0; i < 5; i++) {
                    gc();
                }
            } catch (InterruptedException ex) {
                // ignore
            }
        }

        static void gc() throws InterruptedException {
            List<byte[]> alloc = new ArrayList<byte[]>();
            int size = 100000;
            for (int i = 0; i < 50; i++) {
                System.gc();
                System.runFinalization();
                try {
                    alloc.add(new byte[size]);
                    size = (int) (((double) size) * 1.3);
                } catch (OutOfMemoryError error) {
                    size = size / 2;
                }
                if (i % 3 == 0) {
                    Thread.sleep(100);
                }
            }
        }
    }

    /**
     * test for issue #106218
     * @throws java.lang.Exception
     */
    public void testWeakSetIntegrity() throws Exception {
        //CharSequence log = Log.enable(WeakSet.class.getName(), Level.FINE);
        ArrayList<WeakReference<TestObj>> awr = new ArrayList<WeakReference<TestObj>>();
        ExecutorService exec = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 1000; i++) {
            TestObj to = new TestObj("T" + i);
            awr.add(new WeakReference<TestObj>(to));
            if (i < 2) {
                exec.execute(new GC());
            }
            for (WeakReference<TestObj> wro : awr) {
                TestObj wroo = wro.get();
                if (wroo != null) {
                    synchronized (TestObj.testObjs) {
                        boolean found = false;
                        for (TestObj o : TestObj.testObjs) {
                            if (o == wroo) {
                                found = true;
                            }
                        }
                        if (found != TestObj.testObjs.contains(wroo)) {
                            //System.out.println(log.toString());
                            fail("Inconsistency of iterator chain and hash map");
                        }
                    }
                }
            }
        }
        exec.shutdownNow();
    }    
}
