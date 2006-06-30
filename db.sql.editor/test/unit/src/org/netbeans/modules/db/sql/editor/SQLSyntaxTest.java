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
