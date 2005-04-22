/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util;

import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
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
}
