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

package org.netbeans.modules.db.test.jdbcstub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Andrei Badea
 */
public class ResultSetImplTest extends TestCase {
    
    public ResultSetImplTest(String name) {
        super(name);
    }
    
    public void testEmpty() throws Exception {
        ResultSet rs = JDBCStubUtil.createResultSet(Collections.EMPTY_LIST);
        assertFalse(rs.next());
    }
    
    public void testNextAndGetObject() throws Exception {
        
        List col1 = Arrays.asList(new String[] { "FOO", "foo1", "foo2"});
        List col2 = Arrays.asList(new String[] { "BAR", "bar1", "bar2"});
        
        ResultSet rs = JDBCStubUtil.createResultSet(Arrays.asList(new List[] { col1, col2 }));
        
        try {
            rs.getObject("foo1");
            fail("SQLException thrown if next() not previously called");
        } catch (SQLException e) { }
        
        assertTrue(rs.next());
        
        assertEquals("foo1", rs.getObject("FOO"));
        assertEquals("bar1", rs.getObject("BAR"));
        
        try {
            rs.getObject("inexistent");
            fail("SQLException thrown if unkown column name");
        } catch (SQLException e) { }
        
        assertTrue(rs.next());
        assertEquals("bar2", rs.getObject("BAR"));
        assertEquals("foo2", rs.getObject("FOO"));
        
        assertFalse(rs.next());
        assertFalse(rs.next());
    }
}
