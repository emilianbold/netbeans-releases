/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * IntListTest.java
 * JUnit based test
 *
 * Created on March 22, 2004, 12:24 AM
 */

package org.netbeans.core.output2;

import java.util.Arrays;
import junit.framework.*;

/**
 *
 * @author tim
 */
public class IntListTest extends TestCase {
    
    public IntListTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(IntListTest.class);
        return suite;
    }
    
    /**
     * Test of add method, of class org.netbeans.core.output2.IntList.
     */
    public void testAdd() {
        System.out.println("testAdd");
        
        IntList il = new IntList (100);
        il.add (0);
        il.add (1);
        il.add (2);
        il.add (23);
        il.add (Integer.MAX_VALUE);
        
        assertTrue (il.get(0) == 0);
        assertTrue (il.get(1) == 1);
        assertTrue (il.get(2) == 2);
        assertTrue (il.get(3) == 23);
        assertTrue (il.get(4) == Integer.MAX_VALUE);
    }
    
    /**
     * Test of get method, of class org.netbeans.core.output2.IntList.
     */
    public void testGet() {
        System.out.println("testGet");
        
    }
    
    /**
     * Test of findNearest method, of class org.netbeans.core.output2.IntList.
     */
    public void testFindNearest() {
        System.out.println("testFindNearest");

        IntList il = new IntList (1000);
        
        for (int i=0; i < 100; i++) {
            il.add (i * 10);
        }
        
        int near475 = il.findNearest (475);
        assertTrue ("Nearest entry to 475 should be 470, not " + near475, 
            near475 == 47);
        
        int near470 = il.findNearest (470);
        assertTrue ("List contains an entry 470 at index 47, but returned " 
            + near470 + " as the index with the value closest to 470",
            near470 == 47);
        
        int near505 = il.findNearest (505);
        assertTrue ("Nearest entry to 505 should be 500, not " + near505, 
            near505 == 50);
        
        int near515 = il.findNearest (515);
        assertTrue ("Nearest entry to 515 should be 510, not " + near515, 
            near515 == 51);
        
        int near5 = il.findNearest (5);
        assertTrue ("Nearest entry to 5 should be 0, not " + near5, 
            near5 == 0);
        
        int near995 = il.findNearest (995);
        assertTrue ("Nearest entry to 995 should be 990, not " + near995, 
            near995 == 99);
        
        int near21000 = il.findNearest (21000);
        assertTrue ("Nearest entry to 21000 should be 990, not " + near21000, 
            near21000 == 99);
        
        int nearNeg475 = il.findNearest (-475);
        assertTrue ("Nearest entry to -475 should be 0 not " + nearNeg475, 
            nearNeg475 == 0);
    
    }
    
    /**
     * Test of indexOf method, of class org.netbeans.core.output2.IntList.
     */
    public void testIndexOf() {
        System.out.println("testIndexOf");
        
        IntList il = new IntList (1000);
        
        int[] vals = new int[] {
            1, 4, 23, 31, 47, 2350, 5727, 32323
        };
        
        for (int i=0; i < vals.length; i++) {
            il.add (vals[i]);
        }
        
        for (int i=0; i < vals.length; i++) {
            assertTrue (vals[i] + " was added at index " + i + " but found it" +
            "(or didn't find it) at index " + il.indexOf(vals[i]), 
            il.indexOf(vals[i]) == i);
        }
        
    }
    
    /**
     * Test of size method, of class org.netbeans.core.output2.IntList.
     */
    public void testSize() {
        System.out.println("testSize");
        IntList il = new IntList (1000);
        
        int[] vals = new int[] {
            1, 4, 23, 31, 47, 2350, 5727, 32323
        };
        
        for (int i=0; i < vals.length; i++) {
            il.add (vals[i]);
        }

        assertTrue (il.size() == vals.length);
    }
    
    /**
     * Test of toString method, of class org.netbeans.core.output2.IntList.
     */
    public void testToString() {
        System.out.println("testToString");
        //Nothing to test here
        
    }
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
