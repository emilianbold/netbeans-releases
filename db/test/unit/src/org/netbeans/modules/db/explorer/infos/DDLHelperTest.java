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
package org.netbeans.modules.db.explorer.infos;

import java.sql.Types;
import org.netbeans.modules.db.util.DBTestBase;
import org.netbeans.modules.db.util.InfoHelper;

/**
 * @author <href="mailto:david@vancouvering.com">David Van Couvering</href>
 */
public class DDLHelperTest extends DBTestBase {
    InfoHelper helper;

    public DDLHelperTest(String name) {
        super(name);
    }

    public void testDeleteIndex() throws Exception {
        String tablename = "testIndexDelete";
        String colname = "indexcol";
        String indexname = "indexcol_idx";
        
        createBasicTable(tablename, "id");
        addBasicColumn(tablename, colname, Types.INTEGER, 0);
        
        // Create an index
        createSimpleIndex(tablename, indexname, colname);
        
        DDLHelper.deleteIndex(spec, SCHEMA, tablename, indexname);
        
        assertFalse(indexExists(tablename, indexname));
    }
    
    public void testDeleteTable() throws Exception {
        String tablename = "testDeleteTable";
        
        createBasicTable(tablename, "id");
        assertTrue(tableExists(tablename));
        
        DDLHelper.deleteTable(spec, SCHEMA, tablename);
        
        assertFalse(tableExists(tablename));
    }
    
    public void testDeleteView() throws Exception {
        String tablename = "testDeleteViewTable";
        String viewname = "testDeleteView";
        
        createBasicTable(tablename, "id");
        
        createView(viewname, "SELECT * FROM " + quote(tablename));
        assertTrue(viewExists(viewname));
        
        DDLHelper.deleteView(spec, SCHEMA, viewname);
        
        assertFalse(viewExists(viewname));
    }
    

}
