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

package org.netbeans.modules.db.api.sql;

import junit.framework.*;

/**
 * Tests the SQLKeyword class, that is, it ensures that the is.*Keyword()
 * methods return true for all keywords. Hopefull this will catch someone
 * making the keyword lists not correctly ordered when adding a
 * possibly forgotten keyword.
 *
 * @author Andrei Badea
 */
public class SQLKeywordsTest extends TestCase {

    public SQLKeywordsTest(String testName) {
        super(testName);
    }

    public void testIsSQL99ReservedKeyword() {
        for (int i = 0; i < SQLKeywords.SQL99_RESERVED.length; i++) {
            String identifier = SQLKeywords.SQL99_RESERVED[i].toUpperCase();
            assertTrue(identifier + " should be a reserved keyword", SQLKeywords.isSQL99ReservedKeyword(identifier));
            identifier = identifier.toLowerCase();
            assertTrue(identifier + " should be a reserved keyword", SQLKeywords.isSQL99ReservedKeyword(identifier));
        }

        // should return null for non-keywords
        assertFalse(SQLKeywords.isSQL99ReservedKeyword("FOOBAR"));

        // null identifier should throw NPE
        try {
            SQLKeywords.isSQL99ReservedKeyword(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) { }
    }

    public void testIsSQL99NonReservedKeyword() {
        for (int i = 0; i < SQLKeywords.SQL99_NON_RESERVED.length; i++) {
            String identifier = SQLKeywords.SQL99_NON_RESERVED[i].toUpperCase();
            assertTrue(identifier + " should be a non-reserved keyword", SQLKeywords.isSQL99NonReservedKeyword(identifier));
            identifier = identifier.toLowerCase();
            assertTrue(identifier + " should be a non-reserved keyword", SQLKeywords.isSQL99NonReservedKeyword(identifier));
        }

        // should return null for non-keywords
        assertFalse(SQLKeywords.isSQL99NonReservedKeyword("FOOBAR"));

        // null identifier should throw NPE
        try {
            SQLKeywords.isSQL99NonReservedKeyword(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) { }
    }

    public void testIsSQL99Keyword() {
        for (int i = 0; i < SQLKeywords.SQL99_RESERVED.length; i++) {
            String identifier = SQLKeywords.SQL99_RESERVED[i].toUpperCase();
            assertTrue(identifier + " should be a keyword", SQLKeywords.isSQL99Keyword(identifier));
            identifier = identifier.toLowerCase();
            assertTrue(identifier + " should be a keyword", SQLKeywords.isSQL99Keyword(identifier));
        }
        for (int i = 0; i < SQLKeywords.SQL99_NON_RESERVED.length; i++) {
            String identifier = SQLKeywords.SQL99_RESERVED[i].toUpperCase();
            assertTrue(identifier + " should be a keyword", SQLKeywords.isSQL99Keyword(identifier));
            identifier = identifier.toLowerCase();
            assertTrue(identifier + " should be a keyword", SQLKeywords.isSQL99Keyword(identifier));
        }

        // should return null for non-keywords
        assertFalse(SQLKeywords.isSQL99Keyword("FOOBAR"));

        // null identifier should throw NPE
        try {
            SQLKeywords.isSQL99Keyword(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) { }
    }
}
