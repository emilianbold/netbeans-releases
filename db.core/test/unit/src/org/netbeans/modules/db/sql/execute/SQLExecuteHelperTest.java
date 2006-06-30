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
