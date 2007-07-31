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

package org.netbeans.modules.db.sql.execute;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Andrei Badea
 */
public class SQLExecuteHelperTest extends NbTestCase {
    
    public SQLExecuteHelperTest(String testName) {
        super(testName);
    }

    public void testSplit() {
        // removing line comments
        assertSplit("select --line\n from dual", "select  from dual");
        assertSplit("select ----line\n from dual", "select  from dual");
        assertSplit("select --line from dual", "select");
        
        // removing block comments
        assertSplit("select /* block */ from dual", "select  from dual");
        assertSplit("select ///* block */ from dual", "select // from dual");
        assertSplit("select /* block * block ***/ from dual", "select  from dual");
        assertSplit("select /* block from dual", "select");
        assertSplit("select a - b / c from dual", "select a - b / c from dual");
        assertSplit("select 'foo /* bar */ -- baz' from dual", "select 'foo /* bar */ -- baz' from dual");
        
        // ; in comments should not be considered a statement separator
        assertSplit("select --comment; \n foo", "select  foo");
        assertSplit("select /* ; */ foo", "select  foo");

        // splitting
        assertSplit(" ;; ; ", new String[0]);
        assertSplit("/* comment */ select foo; /* comment */ select bar -- comment", new String[] { "select foo", "select bar" });

        // splitting and start/end positions
        String test = "  select foo  ;   select /* comment */bar;\n   select baz -- comment";
        // System.out.println(test.substring(12));
        assertSplit(test, new StatementInfo[] { 
            new StatementInfo("select foo", 0, 2, 0, 2, 12, 14),
            new StatementInfo("select bar", 15, 18, 0, 18, 41, 41),
            new StatementInfo("select baz", 42, 46, 1, 3, 56, 67),
        });
    }
    
    private static void assertSplit(String script, String expected) {
        assertSplit(script, new String[] { expected });
    }
    
    private static void assertSplit(String script, String[] expected) {
        StatementInfo[] stmts = (StatementInfo[])SQLExecuteHelper.split(script).toArray(new StatementInfo[0]);
        assertEquals(expected.length, stmts.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], stmts[i].getSQL());
        }
    }
    
    private static void assertSplit(String script, StatementInfo[] expected) {
        StatementInfo[] stmts = (StatementInfo[])SQLExecuteHelper.split(script).toArray(new StatementInfo[0]);
        assertEquals(expected.length, stmts.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].getSQL(), stmts[i].getSQL());
            assertEquals(expected[i].getRawStartOffset(), stmts[i].getRawStartOffset());
            assertEquals(expected[i].getStartOffset(), stmts[i].getStartOffset());
            assertEquals(expected[i].getStartLine(), stmts[i].getStartLine());
            assertEquals(expected[i].getStartColumn(), stmts[i].getStartColumn());
            assertEquals(expected[i].getEndOffset(), stmts[i].getEndOffset());
            assertEquals(expected[i].getRawEndOffset(), stmts[i].getRawEndOffset());
        }
    }
}
