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

package org.netbeans.modules.db.sql.execute;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Andrei Badea
 */
public class SQLExecuteHelperTest extends NbTestCase {
    
    private static final String[] sqlCommentRemoverTests = { 
        "select --line\n from dual", "select  from dual",
        "select ----line\n from dual", "select  from dual",
        "select --line from dual", "select ",
        "select /* block */ from dual", "select  from dual", 
        "select ///* block */ from dual", "select // from dual",
        "select /* block * block ***/ from dual", "select  from dual",
        "select /* block from dual", "select ",
        "select a - b / c from dual", "select a - b / c from dual",
    };
    
    public SQLExecuteHelperTest(String testName) {
        super(testName);
    }

    public void testCommentRemover() {
        for (int i = 0; i < sqlCommentRemoverTests.length;) {
            String sql = sqlCommentRemoverTests[i];
            i++;
            String expected = sqlCommentRemoverTests[i];
            i++;
            String removed = SQLExecuteHelper.removeComments(sql);
            assertEquals(expected, removed);
        }
    }
}
