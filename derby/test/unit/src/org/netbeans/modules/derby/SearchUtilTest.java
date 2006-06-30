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
