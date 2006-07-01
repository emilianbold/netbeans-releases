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

package org.netbeans.core.output2;

import junit.framework.TestCase;

/**
 * Test for org.netbeans.core.output2.SparseIntList.
 *
 * @author Tim Boudreau
 */
public class SparseIntListTest extends TestCase {

    public SparseIntListTest(String testName) {
        super(testName);
    }

    private SparseIntList l = null;
    protected void setUp() throws Exception {
        l = new SparseIntList(20);
    }

    public void testGetLessThanZeroReturnsZero() {
        System.out.println("testGetLessThanZeroReturnsZero");
        assertTrue (l.get(Integer.MIN_VALUE) == 0);
    }
    
    public void testGetFromEmptyListReturnsRequestedIndex() {
        System.out.println("testGetFromEmptyListReturnsRequestedIndex");
        assertTrue (l.get(20) == 20);
        assertTrue (l.get(Integer.MAX_VALUE) == Integer.MAX_VALUE);
        assertTrue (l.get(1) == 1);
        assertTrue (l.get(0) == 0);
    }
    
    public void testGetBelowFirstEntryReturnsIndex() {
        System.out.println("testGetBelowFirstEntryReturnsIndex");
        l.add (20, 11);
        for (int i=0; i < 11; i++) {
            assertTrue (l.get(i) == i);
        }
    }
    
    public void testAdd() {
        System.out.println("testAdd");
        l.add (20, 11);
        int val = l.get(11);
        assertTrue ("After add(20, 11), value at 11 should still be 11, not " + val, val == 11);
        val = l.get(12);
        assertTrue ("After add(20, 11), value at 12 should be 21, not " + val, val == 21);
        
        l.add (30, 12);
        val = l.get(12);
        assertTrue ("After add(30, 12), value at 12 should still be 21, not " + val, val == 21);
        val = l.get(13);
        assertTrue ("After add(30, 12), value at 13 should be 31, not " + val, val == 31);
        
        l.add (80, 30);
        val = l.get(12);
        assertTrue ("After add(80, 30), value at 12 should still be 21, not " + val + " adding an entry above should not change it", val == 21);
        val = l.get(13);
        assertTrue ("After add(80, 30), value at 13 should be 31, not " + val + " adding an entry above should not change it", val == 31);
        val = l.get(31);
        assertTrue ("After add(80, 30), value at 31 should be 81, not " + val, val == 81);
        
        for (int i=0; i < 10; i++) {
            val = l.get(i);
            assertTrue ("In a populated map, get() on an index below the first added entry should return the index, but get(" + i + ") returns " + val, val == i);
        }
        
    }
    
    public void testBadValuesThrowExceptions() {
        System.out.println("testBadValuesThrowExceptions");
        l.add (20, 11);
        Exception e = null;
        try {
            l.add (19, 13);
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
        
        try {
            l.add (21, 10);
        } catch (Exception ex2) {
            e = ex2;
        }
        assertNotNull(e);
        
    }
    
    public void testGetAboveFirstEntryReturnsEntryPlusIndexDiff() {
        System.out.println("testGetAboveFirstEntryReturnsEntryPlusIndexDiff");
        l.add (20, 11);
        int x = 21;
        for (int i=12; i < 40; i++){
            assertTrue ("Entry at " + i + " should be " + x + ", not " + l.get(i), l.get(i) == x);
            x++;
        }
    }
    
    

}
