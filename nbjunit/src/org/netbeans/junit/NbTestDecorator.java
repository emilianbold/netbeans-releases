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

import junit.extensions.TestDecorator;
import java.io.*;
import org.netbeans.junit.diff.*;

/**
 * A Decorator for Tests. Use TestDecorator as the base class
 * for defining new test decorators. Test decorator subclasses
 * can be introduced to add behaviour before or after a test
 * is run.
 *
 */

import junit.framework.*;

/** NetBeans extension to JUnit's TestDecorator class. Tests created 
 * with the help of this class can use method assertFile and can be
 * filtered.
 */
public class NbTestDecorator extends TestDecorator implements NbTest {
	
/**
 */
	public NbTestDecorator(Test test) {
		super(test);
	}

        /**
         * Sets active filter.
         * @param filter Filter to be set as active for current test, null will reset filtering.
         */
        public void setFilter(Filter filter) {
            //System.out.println("NbTestDecorator.setFilter()");
            if (fTest instanceof NbTest) {
                //System.out.println("NbTestDecorator.setFilter(): test="+fTest+" filter="+filter);
                ((NbTest)fTest).setFilter(filter);
            }
        }
        
        /**
         * Returns expected fail message.
         * @return expected fail message if it's expected this test fail, null otherwise.
         */
        public String getExpectedFail() {
            if (fTest instanceof NbTest) {
                return ((NbTest)fTest).getExpectedFail();
            } else {
                return null;
            }
        }
        
        /**
         * Checks if a test isn't filtered out by the active filter.
         */
        public boolean canRun() {
            //System.out.println("NbTestDecorator.canRun()");
            if (fTest instanceof NbTest) {
                //System.out.println("fTest is NbTest");
                return ((NbTest)fTest).canRun();
            } else {
                // standard JUnit tests can always run
                return true;
            }
        }        

        
        
        // additional asserts !!!!
        // these methods only wraps functionality from asserts from NbTestCase,
        // because java does not have multiinheritance :-)
        // please see more documentatino in this file.
        
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(String message, String test, String pass, String diff, Diff externalDiff) {
            NbTestCase.assertFile(message,test,pass,diff,externalDiff);
        }
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(String test, String pass, String diff, Diff externalDiff) {
            NbTestCase.assertFile(test, pass, diff, externalDiff);
        }
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(String message, String test, String pass, String diff) {
            NbTestCase.assertFile(message, test, pass, diff);
        }
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(String test, String pass, String diff) {
            NbTestCase.assertFile(test, pass, diff);
        }
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(String test, String pass) {
            NbTestCase.assertFile(test, pass);
        }
        
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(String message, File test, File pass, File diff, Diff externalDiff) {
            NbTestCase.assertFile(message,test,pass,diff,externalDiff);
        }
        
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(File test, File pass, File diff, Diff externalDiff) {
            NbTestCase.assertFile(test, pass, diff, externalDiff);
        }
        
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(String message, File test, File pass, File diff) {
            NbTestCase.assertFile(message, test, pass, diff);
        }
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(File test, File pass, File diff) {
            NbTestCase.assertFile(test,pass,diff);
        }
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(File test, File pass) {
            NbTestCase.assertFile(test, pass);
        }
        
        
}