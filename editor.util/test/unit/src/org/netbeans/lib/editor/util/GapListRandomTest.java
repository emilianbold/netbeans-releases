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

package org.netbeans.lib.editor.util;

import java.util.ArrayList;
import java.util.Random;
import junit.framework.*;


/**
 * Random test of GapList correctness.
 *
 * @author mmetelka
 */
public class GapListRandomTest extends TestCase {
    
    private static final boolean debug = false;
    
    private static final int OP_COUNT_1 = 10000;
    private static final int ADD_RATIO_1 = 100;
    private static final int ADD_ALL_RATIO_1 = 10;
    private static final int ADD_ALL_MAX_COUNT_1 = 10;
    private static final int REMOVE_RATIO_1 = 100;
    private static final int REMOVE_RANGE_RATIO_1 = 10;
    private static final int CLEAR_RATIO_1 = 5;
    private static final int SET_RATIO_1 = 50;
    
    private static final int OP_COUNT_2 = 10000;
    private static final int ADD_RATIO_2 = 50;
    private static final int ADD_ALL_RATIO_2 = 20;
    private static final int ADD_ALL_MAX_COUNT_2 = 5;
    private static final int REMOVE_RATIO_2 = 100;
    private static final int REMOVE_RANGE_RATIO_2 = 10;
    private static final int CLEAR_RATIO_2 = 3;
    private static final int SET_RATIO_2 = 50;
    
    private ArrayList al;
    
    private GapList gl;
    
    public GapListRandomTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(GapListRandomTest.class);
        return suite;
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void test() {
        testFresh(0);
    }

    public void testFresh(long seed) {
        Random random = new Random();
        if (seed != 0) {
            System.err.println("TESTING with SEED=" + seed);
            random.setSeed(seed);
        }
        
        gl = new GapList();
        al = new ArrayList();
        
        
        testRound(random, OP_COUNT_1, ADD_RATIO_1, ADD_ALL_RATIO_1, ADD_ALL_MAX_COUNT_1,
            REMOVE_RATIO_1, REMOVE_RANGE_RATIO_1, CLEAR_RATIO_1, SET_RATIO_1);
        testRound(random, OP_COUNT_2, ADD_RATIO_2, ADD_ALL_RATIO_2, ADD_ALL_MAX_COUNT_2,
            REMOVE_RATIO_2, REMOVE_RANGE_RATIO_2, CLEAR_RATIO_2, SET_RATIO_2);
    }
    
    private void testRound(Random random, int opCount,
    int addRatio, int addAllRatio, int addAllMaxCount,
    int removeRatio, int removeRangeRatio, int clearRatio, int setRatio) {
        
        int ratioSum = addRatio + addAllRatio + removeRatio + removeRangeRatio
            + clearRatio + setRatio;
        
        for (int op = 0; op < opCount; op++) {
            double r = random.nextDouble() * ratioSum;

            if ((r -= addRatio) < 0) {
                Object o = new Object();
                int index = (int)(al.size() * random.nextDouble());
                al.add(index, o);
                if (debug) {
                    debugOp(op, "add() at index=" + index); // NOI18N
                }
                gl.add(index, o);

            } else if ((r -= addAllRatio) < 0) {
                int count = (int)(random.nextDouble() * addAllMaxCount);
                ArrayList l = new ArrayList();
                for (int i = count; i > 0; i--) {
                    l.add(new Object());
                }

                Object o = new Object();
                int index = (int)(al.size() * random.nextDouble());
                al.addAll(index, l);
                if (debug) {
                    debugOp(op, "addAll() at index=" + index); // NOI18N
                }
                gl.addAll(index, l);

            } else if ((r -= removeRatio) < 0) {
                if (al.size() > 0) { // is anything to remove
                    int index = (int)(al.size() * random.nextDouble());
                    al.remove(index);
                    if (debug) {
                        debugOp(op, "remove() at index=" + index); // NOI18N
                    }
                    gl.remove(index);
                }

            } else if ((r -= removeRangeRatio) < 0) {
                if (al.size() > 0) { // is anything to remove
                    int index = (int)(al.size() * random.nextDouble());
                    int length = (int)((al.size() - index + 1) * random.nextDouble());
                    for (int count = length; count > 0; count--) {
                        al.remove(index);
                    }
                    if (debug) {
                        debugOp(op, "remove() at index=" + index + ", length=" + length); // NOI18N
                    }
                    gl.remove(index, length);
                }
                
            } else if ((r -= clearRatio) < 0) {
                al.clear();
                if (debug) {
                    debugOp(op, "clear()"); // NOI18N
                }
                gl.clear();
                
            } else if ((r -= setRatio) < 0) {
                if (al.size() > 0) { // is anything to remove
                    int index = (int)(al.size() * random.nextDouble());
                    Object o = new Object();
                    al.set(index, o);
                    if (debug) {
                        debugOp(op, "set() at index=" + index); // NOI18N
                    }
                    gl.set(index, o);
                }
            }

            checkConsistency();
        }
        
    }
        
    private void debugOp(int op, String s) {
        System.err.println("op: " + op + ", " + s + ", " + gl.toStringInternals());
    }
    
    private void checkConsistency() {
        gl.consistencyCheck();

        assertEquals(gl.size(), al.size());
        
        int size = al.size();
        for (int i = 0; i < size; i++) {
            assertTrue("Contents differ at index " + i + ", gl: " + gl.get(i) // NOI18N
                + ", al:" + al.get(i), // NOI18N
                (gl.get(i) == al.get(i)));
        }
    }
    
    
}
