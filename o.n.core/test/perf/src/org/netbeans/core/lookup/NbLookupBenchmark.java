/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.lookup;

import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.*;
import org.netbeans.performance.Benchmark;

import java.lang.ref.WeakReference;
import java.util.*;
import junit.framework.*;

public class NbLookupBenchmark extends Benchmark {
    /** how many times objects in INSTANCES should be added in */
    private static Object[] ARGS = { 
        new Integer (1)
    };
    
    
    public NbLookupBenchmark(java.lang.String testName) {
        super(testName, ARGS);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new TestSuite (NbLookupBenchmark.class));
    }
    
    /** instance of lookup */
    private InstanceLookup lookup;

    /** instances that we register */
    private static Object[] INSTANCES = new Object[] {
        new Integer (10), 
        new Object (),
        "Ahoj",
        new C4 (), new C3 (), new C2 (), new C1 ()
    };

    /** Fills the lookup with instances */
    protected void setUp () {
        Integer integer = (Integer)getArgument ();
        int cnt = integer.intValue ();
        
        boolean reverse = cnt < 0;
        if (reverse) cnt = -cnt;
        
        lookup = new InstanceLookup ();
        
        while (cnt-- > 0) {
            for (int i = 0; i < INSTANCES.length; i++) {
                if (reverse) {
                    lookup.add (INSTANCES[INSTANCES.length - i - 1]);
                } else {
                    lookup.add (INSTANCES[i]);
                }
            }
        }
    }
    
    /** Clears the lookup.
     */
    protected void tearDown () {
        lookup = null;
    }
    
    /** Test to find the first registered object.
     */
    public void testInteger () {
        enum (Integer.class);
    }
    
    /** Test object.
     */
    public void testObject () {
        enum (Object.class);
    }
    
    /** Test string.
     */
    public void testString () {
        enum (String.class);
    }
    
    public void testC1 () {
        enum (C1.class);
    }
    
    public void testC2 () {
        enum (C2.class);
    }
    
    public void testC3 () {
        enum (C3.class);
    }
    
    public void testC4 () {
        enum (C4.class);
    }
    
    public void testI1 () {
        enum (I1.class);
    }
    
    public void testI2 () {
        enum (I2.class);
    }
    
    public void testI3 () {
        enum (I3.class);
    }
    
    public void testI4 () {
        enum (I4.class);
    }
        
        
        
    /** Enumerates over instances of given class.
     * @param clazz the class to find instances of
     */
    private void enum (Class clazz) {
        int cnt = getIterationCount ();
        
        while (cnt-- > 0) {
            Lookup.Result res = lookup.lookup (new Lookup.Template (clazz));

            Collection c = res.allInstances ();
        }
    }
    
    
    private static interface I1 {}
    private static interface I2 extends I1 {}
    private static interface I3 extends I1 {}
    private static interface I4 extends I2, I3 {}
    private static class C1 extends Object implements I2 {}
    private static class C2 extends C1 {}
    private static class C3 extends C2 implements I3 {}
    private static class C4 extends C3 implements I4 {}
}
