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
package org.netbeans.modules.db.explorer.dlg;

import java.sql.Types;
import java.util.HashSet;
import org.netbeans.modules.db.util.DBTestBase;

/**
 *
 * @author David
 */
public class AddIndexDDLTest extends DBTestBase {

    public AddIndexDDLTest(String name) {
        super(name);
    }
    
    public void testAddIndex() throws Exception {
        String tablename = "mytable";
        String pkname = "id";
        String colname = "col";
        String ixname = "col_ix";
        
        createBasicTable(tablename, pkname);
        addBasicColumn(tablename, colname, Types.VARCHAR, 255);
        
        AddIndexDDL ddl = new AddIndexDDL(spec, SCHEMA, 
                fixIdentifier(tablename));
        
        HashSet cols = new HashSet();
        cols.add(fixIdentifier(colname));
        
        boolean wasException = ddl.execute(ixname, false, cols);
        
        assertFalse(wasException);
        assertTrue(columnInIndex(tablename, colname, ixname));
        
        colname = "col2";
        ixname = "col2_ix";
        addBasicColumn(tablename, colname, Types.VARCHAR, 255);
        
        cols.clear();
        cols.add(fixIdentifier(colname));
        wasException = ddl.execute(ixname, true, cols);
        assertFalse(wasException);
        assertTrue(columnInIndex(tablename, colname, ixname));
        assertTrue(indexIsUnique(tablename, ixname));
    }

}
