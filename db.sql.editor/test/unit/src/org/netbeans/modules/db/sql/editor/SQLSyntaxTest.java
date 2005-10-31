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

package org.netbeans.modules.db.sql.editor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Andrei Badea
 */
public class SQLSyntaxTest extends NbTestCase {
    
    public SQLSyntaxTest(String testName) {
        super(testName);
    }

    public void testNumberLiteralsEndWithFirstNonDigitCharIssue67379() {
        assertTokens("10-20.3-3", new TokenID[] {
            SQLTokenContext.INT_LITERAL,
            SQLTokenContext.OPERATOR,
            SQLTokenContext.DOUBLE_LITERAL,
            SQLTokenContext.OPERATOR,
            SQLTokenContext.INT_LITERAL,
        });
        
        assertTokens("10foo", new TokenID[] {
            SQLTokenContext.INT_LITERAL,
            SQLTokenContext.IDENTIFIER,
        });
    }
    
    private void assertTokens(String m, TokenID[] tokens) {
        Syntax s = new SQLSyntax();
        s.load(null, m.toCharArray(), 0, m.length(), true, m.length());
        
        TokenID token = null;
        Iterator i = Arrays.asList(tokens).iterator();
        do {
            token = s.nextToken();
            if (token != null) {
                if (!i.hasNext()) {
                    fail("More tokens returned than expected.");
                } else {
                    assertSame("Tokens differ", i.next(), token);
                }
            } else {
                assertFalse("More tokens expected than returned.", i.hasNext());
            }
            if (token != null) {
                log(token.getName());
            }
        } while (token != null);
    }
}
