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
        
}
