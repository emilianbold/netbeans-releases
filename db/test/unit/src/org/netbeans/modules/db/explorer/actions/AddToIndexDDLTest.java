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
package org.netbeans.modules.db.explorer.actions;

import java.sql.Types;
import java.util.HashSet;
import org.netbeans.modules.db.util.DBTestBase;

/**
 *
 * @author David
 */
public class AddToIndexDDLTest extends DBTestBase {

    public AddToIndexDDLTest(String name) {
        super(name);
    }
    
    public void testAddToIndex() throws Exception {
        String tablename = "addToIndex";
        String indexname = "index";
        String col1name = "col1";
        String col2name = "col2";
        
        createBasicTable(tablename, "id");
        addBasicColumn(tablename, col1name, Types.VARCHAR, 255);
        createSimpleIndex(tablename, indexname, col1name);

        addBasicColumn(tablename, col2name, Types.VARCHAR, 255);

        AddToIndexDDL ddl = new AddToIndexDDL(spec, SCHEMA, fixIdentifier(tablename));
        
        HashSet cols = new HashSet();
        cols.add(fixIdentifier(col2name));
        
        boolean wasException = ddl.execute(fixIdentifier(indexname), false, cols);
        assertFalse(wasException);
        assertTrue(columnInIndex(tablename, col2name, indexname));        
    }

}
