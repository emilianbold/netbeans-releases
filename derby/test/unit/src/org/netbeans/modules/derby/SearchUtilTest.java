/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.derby;

import junit.framework.*;

/**
 *
 * @author pj97932
 */
public class SearchUtilTest extends TestCase {
    
    public SearchUtilTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SearchUtilTest.class);
        
        return suite;
    }

    /**
     * Test of checkForString method, of class org.netbeans.modules.derby.SearchUtil.
     */
    public void testCheckForString() {
        System.out.println("checkForString");
        String searchedFor;
        int searchStart;
        char[] buf;
        int bufLen;
        
        searchedFor = "12345";
        searchStart = 0;
        buf = new char[] {'a', 'b', '1', '2', '3', 'x', 'x'};
        bufLen = 5;
        assertEquals(3, SearchUtil.checkForString(searchedFor, searchStart, buf, bufLen));
        
        searchedFor = "12345";
        searchStart = 2;
        buf = new char[] {'3', '4', '5', 'a', 'b', 'x'};
        bufLen = 5;
        assertEquals(SearchUtil.FOUND, SearchUtil.checkForString(searchedFor, searchStart, buf, bufLen));
        
        searchedFor = "12345";
        searchStart = 0;
        buf = new char[] {'3', '4', '5', 'a', 'b', 'x'};
        bufLen = 5;
        assertEquals(0, SearchUtil.checkForString(searchedFor, searchStart, buf, bufLen));
        
        searchedFor = "12345";
        searchStart = 0;
        buf = new char[] {'a', 'b', 'c', '1', '2', 'x'};
        bufLen = 5;
        assertEquals(2, SearchUtil.checkForString(searchedFor, searchStart, buf, bufLen));
        
    }

    /**
     * Test of checkPosition method, of class org.netbeans.modules.derby.SearchUtil.
     */
    public void testCheckPosition() {
        System.out.println("checkPosition");
        
        String searchedFor = "12345";
        int searchStart = 0;
        char[] buf = new char[] {'a', 'b', '1', '2', '3', 'x', 'x'};
        int bufLen = 5;
        int bufFrom = 2;
        assertEquals(3, SearchUtil.checkPosition(searchedFor, searchStart, buf, bufLen, bufFrom));
        
    }
    
}
