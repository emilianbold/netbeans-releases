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

package org.netbeans.junit;

import java.util.*;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * NetBeans extension to JUnit's TestSuite class.
 */
public class NbTestSuite extends TestSuite implements NbTest {


    private Filter fFilter;

    /**
     * Constructs an empty TestSuite.
     */
    public NbTestSuite() {
        super();
    }

    /**
     * Constructs a TestSuite from the given class. Adds all the methods
     * starting with "test" as test cases to the suite.
     *
     */
    public NbTestSuite(Class theClass) {       
        super(theClass);
    }

    /**
     * Constructs an empty TestSuite.
     */
    public NbTestSuite(String name) {
        super(name);
    }
    
    
    /**
     * Adds a test to the suite.
     */
    public void addTest(Test test) {
        if (test instanceof NbTest) {
            //System.out.println("NbTestSuite.addTest(): Adding test with filter, test:"+test);
            ((NbTest)test).setFilter(fFilter);
        } else {
            //System.out.println("NbTestSuite.addTest(): Adding test, test:"+test);
        }
        super.addTest(test);
    }

    
    /**
     * adds a test suite to this test suite
     */
    public void addTestSuite(Class testClass) {
        NbTest t = new NbTestSuite(testClass);
        t.setFilter(fFilter);
        addTest(t);
    }

            /**
         * Sets active filter.
         * @param filter Filter to be set as active for current test, null will reset filtering.
         */
        public void setFilter(Filter filter) {
                Enumeration e;

                this.fFilter = filter;
                e = this.tests();
                while(e.hasMoreElements()) {
                    Object test = e.nextElement();
                    if (test instanceof NbTest) {
                        //System.out.println("NbTestSuite.setFilter(): Setting filter:"+filter);
                        ((NbTest)test).setFilter(filter);
                    }
                      
                }
        }
        /**
         * Checks if a test isn't filtered out by the active filter.
         */
        public boolean canRun() {
                return true; // suite can always run
        }
        
        public String getExpectedFail() {
            return null;
        }
        
        
    /** Factory method to create a special execution suite that not only
     * executes the tests but also measures the times each execution took.
     * It then compares the times and fails if the difference is too big.
     * Test tests can be executed more times to eliminate the effect
     * of GC and hotspot compiler.
     *
     * @param clazz the class to create tests for (from methods starting with test)
     * @param slowness this must be true: slowness * min < max
     * @param repeat number of times to repeat the test
     */
    public static NbTestSuite speedSuite (Class clazz, int slowness, int repeat) {
        return new SpeedSuite (clazz, repeat, slowness, SpeedSuite.CONSTANT);
    }
    
    /** Factory method to create a special execution suite that not only
     * executes the tests but also measures the times each execution took.
     * It then compares the times devided by the size of query 
     * and fails if the difference is too big.
     * Test tests can be executed more times to eliminate the effect
     * of GC and hotspot compiler.
     *
     * @param clazz the class to create tests for (from methods starting with test)
     * @param slowness this must be true: slowness * min < max
     * @param repeat number of times to repeat the test
     */
    public static NbTestSuite linearSpeedSuite (Class clazz, int slowness, int repeat) {
        return new SpeedSuite (clazz, repeat, slowness, SpeedSuite.LINEAR);
    }

    
    /** Allows enhanced execution and comparition of speed of a set of 
     * tests.
     */
    private static final class SpeedSuite extends NbTestSuite {
        public static final int CONSTANT = 0;
        public static final int LINEAR = 1;
        
        /** number of repeats to try, if there is a failure */
        private int repeat;
        /** the maximum difference between the slowest and fastest test */
        private int slowness;
        /** type of query CONSTANT, LINEAR, etc. */
        private int type;
        
        public SpeedSuite (Class clazz, int repeat, int slowness, int type) {
            super (clazz);
            this.repeat = repeat;
            this.slowness = slowness;
            this.type = type;
        }
        
        public void run (junit.framework.TestResult result) {
            StringBuffer error = new StringBuffer ();
            for (int i = 0; i < repeat; i++) {
                super.run(result);
                
                error.setLength (0);
                
                if (!result.wasSuccessful ()) {
                    // if there was a failure, end the test
                    return;
                }
                
                {
                    Enumeration en = tests ();
                    while (en.hasMoreElements ()) {
                        Object t = en.nextElement ();
                        if (t instanceof NbTestCase) {
                            NbTestCase test = (NbTestCase)t;
                            error.append ("Test "); error.append (test.getName ());
                            error.append(" took "); error.append(test.getExecutionTime() / 1000000);
                            error.append (" ms\n");
                        } else {
                            error.append ("Test "); error.append (t); 
                            error.append (" is not NbTestCase");
                        }
                         
                    }
                }

                double min = Long.MAX_VALUE;
                double max = Long.MIN_VALUE;

                {
                    Enumeration en = tests ();
                    while (en.hasMoreElements ()) {
                        Object t = en.nextElement ();
                        if (t instanceof NbTestCase) {
                            double l = ((NbTestCase)t).getExecutionTime ();
                            
                            if (type == LINEAR) {
                                l = l / ((NbTestCase)t).getTestNumber ();
                            }
                            
                            if (l > max) max = l;
                            if (l < min) min = l;
                        }
                    }
                }

                System.err.println(error.toString ());

                if (max <= min * slowness) {
                    // ok
                    return;
                }
            }
            
            result.addFailure (this, new junit.framework.AssertionFailedError (
                "Execution times of tests differ too much;\n" +
                "the results are supposed to be " + typeName () + ":\n"
                + error.toString ()
            ));
        }
        
        private String typeName () {
            switch (type) {
                case CONSTANT: return "constant"; 
                case LINEAR: return "linear";
                default: NbTestCase.fail ("This is not supported type: " + type);
            }
            return null;
        }
    }        
}
