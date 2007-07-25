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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.java.source.gen;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.save.ListMatcher;

/**
 * Test ListMatcher.
 * 
 * @author Pavel Flaska
 */
public class ListMatcherTest extends NbTestCase {
    
    /** Creates a new instance of ListMatcherTest */
    public ListMatcherTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ListMatcherTest.class);
        return suite;
    }
    
    public void testAddToEmpty() {
        String[] oldL = { };
        String[] newL = { "A", "B", "C" };
        String golden = 
                "{insert} A\n" +
                "{insert} B\n" +
                "{insert} C\n";
        ListMatcher<String> matcher = ListMatcher.<String>instance(oldL, newL);
        if (matcher.match()) {
            String result = matcher.printResult(false);
            System.err.println("-------------");
            System.err.println("testAddToEmpty");
            System.err.println("-------------");
            System.err.println(result);
            assertEquals(golden, result);
        } else {
            assertTrue("No match!", false);
        }
    }

    public void testRemoveAll() {
        String[] oldL= { "A", "B", "C" };
        String[] newL = { };
        String golden = 
                "{delete} A\n" +
                "{delete} B\n" +
                "{delete} C\n";
        ListMatcher<String> matcher = ListMatcher.<String>instance(oldL, newL);
        if (matcher.match()) {
            String result = matcher.printResult(false);
            System.err.println("-------------");
            System.err.println("testRemoveAll");
            System.err.println("-------------");
            System.err.println(result);
            assertEquals(golden, result);
        } else {
            assertTrue("No match!", false);
        }
    }
    
    public void testAddToIndex0() {
        String[] oldL= { "B" };
        String[] newL = { "A", "B" };
        String golden = 
                "{insert} A\n" +
                "{nochange} B\n";
        ListMatcher<String> matcher = ListMatcher.<String>instance(oldL, newL);
        if (matcher.match()) {
            String result = matcher.printResult(false);
            System.err.println("---------------");
            System.err.println("testAddToIndex0");
            System.err.println("---------------");
            System.err.println(result);
            assertEquals(golden, result);
        } else {
            assertTrue("No match!", false);
        }
    }
    
    public void testRemoveAtIndex0() {
        String[] oldL = { "A", "B" };
        String[] newL = { "B" };
        String golden = 
                "{delete} A\n" +
                "{nochange} B\n";
        ListMatcher<String> matcher = ListMatcher.<String>instance(oldL, newL);
        if (matcher.match()) {
            String result = matcher.printResult(false);
            System.err.println("------------------");
            System.err.println("testRemoveAtIndex0");
            System.err.println("------------------");
            System.err.println(result);
            assertEquals(golden, result);
        } else {
            assertTrue("No match!", false);
        }
    }
    
    public void testComplex() {
        String[] oldL = { "A", "B", "C", "D", "E", "F", "G" };
        String[] newL = { "B", "C", "C1", "D", "E", "G", "H" };
        String golden = 
                "{delete} A\n" +
                "{nochange} B\n" +
                "{nochange} C\n" +
                "{insert} C1\n" +
                "{nochange} D\n" +
                "{nochange} E\n" +
                "{delete} F\n" +
                "{nochange} G\n" +
                "{insert} H\n";
        ListMatcher<String> matcher = ListMatcher.<String>instance(oldL, newL);
        if (matcher.match()) {
            String result = matcher.printResult(false);
            System.err.println("-----------");
            System.err.println("testComplex");
            System.err.println("-----------");
            System.err.println(result);
            assertEquals(golden, result);
        } else {
            assertTrue("No match!", false);
        }
    }
}
