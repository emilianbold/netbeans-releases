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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.db.sql.support;

import org.netbeans.api.db.sql.support.SQLIdentifiers;
import org.netbeans.modules.db.util.DBTestBase;

/**
 * @author <a href="mailto:david@vancouvering.com">David Van Couvering</a>
 * 
 * This class is a set of tests to make sure we're quoting identifiers
 * correctly
 */
public class QuoterTest extends DBTestBase {
    
    private SQLIdentifiers.Quoter quoter;
    
    public QuoterTest(String testName) {
        super(testName);
    }
    
    public void setUp() throws Exception {
        super.setUp();
        quoter = SQLIdentifiers.createQuoter(conn.getMetaData());        
    }
        
    public void testNoQuoting() {
        String identifier = "YOUDONTNEEDTOQUOTEME2334252__1451";
        String expResult = identifier;
        String result = quoter.quoteIfNeeded(identifier);
        assertEquals(expResult, result);
    } 
    
    public void testSpaces() throws Exception {
        String identifier = "YesYou Need to quote me";
        String expResult = quote(identifier);
        
        String result = quoter.quoteIfNeeded(identifier);
        
        assertEquals(expResult, result);        
    }
    
    public void testCasing() throws Exception {
        String identifier;
        
        // First, find out what kind of casing is done with unquoted
        // identifiers for this connection
        int caseRule = getUnquotedCaseRule();
        
        switch (caseRule) {
        case LC_RULE:
            identifier = "ABCDEFG";
            break;
        case UC_RULE:
            identifier = "abcdefg";
            break;
        default:
            // Nothing to test here
            return;
        }
        
        String expResult = quote(identifier);
        
        String result = quoter.quoteIfNeeded(identifier);
        
        assertEquals(expResult, result);
    }
    
    public void testNonAscii() throws Exception {
        // borrowed translated message from Derby message file :)
        String identifier = "abcdABCD0934" +
                "\u4f8b\u5916\u306e\u305f\u3081\u3001\u59cb\u52d5" +
                "\u306b\u5931\u6557\u3057\u307e\u3057\u305f\u3002 \u8a73\u7d30" +
                "\u306b\u3064\u3044\u3066\u306f\u3001\u6b21\u306e\u4f8b\u5916" +
                "\u3092\u53c2\u7167\u3057\u3066\u304f\u3060\u3055\u3044\u3002" +
                "09298719871";
        
        String expResult = quote(identifier);
        
        String result = quoter.quoteIfNeeded(identifier);
        
        assertEquals(expResult, result);
    }
    
    public void testDontQuoteQuoted() throws Exception {
        String identifier = quote("I am already quoted");
        
        String expResult = identifier;

        String result = quoter.quoteIfNeeded(identifier);
        
        assertEquals(expResult, result);
    }
    
    public void testNullIdentifier() throws Exception {
        try {
            quoter.quoteIfNeeded(null);
            fail("Expected a NullPointerException");
        } catch ( NullPointerException npe ) {
            // expected
        }
    }
    
    public void testFirstCharIsUnderbar() throws Exception {
        String identifier = "_NO_UNDERBAR_AS_FIRST_CHAR";
        
        String expResult = quote(identifier);

        String result = quoter.quoteIfNeeded(identifier);
        
        assertEquals(expResult, result);
    }

    public void testFirstCharIsNumber() throws Exception {
        String identifier = "1NO_NUMBER123_AS_FIRST_CHAR";
        
        String expResult = quote(identifier);

        String result = quoter.quoteIfNeeded(identifier);
        
        assertEquals(expResult, result);
    }
}
