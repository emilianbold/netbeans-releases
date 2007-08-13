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

import java.util.Vector;
import org.netbeans.modules.db.util.DDLTestBase;

public class CreateTableDDLTest extends DDLTestBase {

    public CreateTableDDLTest(String name) {
        super(name);
    }
    
    public void testCreateBasicTable() throws Exception {
        String tablename = "basicTable";
        Vector cols = new Vector();
        Vector pkcols = new Vector();
        
        dropTable(tablename);
        
        ColumnItem col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "id");
        TypeElement type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.PRIMARY_KEY, new Boolean(true));
        cols.add(col);

        col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "firstname");
        type = new TypeElement("java.sql.Types.VARCHAR", "VARCHAR");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.SIZE, "255");
        cols.add(col);
        
        col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "socsec");
        type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        cols.add(col);
        
        CreateTableDDL ddl = new CreateTableDDL(spec, SCHEMA, tablename);
        
        boolean wasException = ddl.execute(cols, pkcols);
        
        assertFalse(wasException);
        
        assertTrue(tableExists(tablename));
        assertTrue(columnInPrimaryKey(tablename, "id"));
    }
    
    public void testCreateTableWithPrimaryKeys() throws Exception {
        String tablename = "basicTable";
        Vector cols = new Vector();
        Vector pkcols = new Vector();
        
        dropTable(tablename);
        
        ColumnItem col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "id");
        TypeElement type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        cols.add(col);
        pkcols.add(col);

        col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "id2");
        type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        cols.add(col);
        pkcols.add(col);
        
        CreateTableDDL ddl = new CreateTableDDL(spec, SCHEMA, tablename);
        
        boolean wasException = ddl.execute(cols, pkcols);
        
        assertFalse(wasException);
        
        assertTrue(tableExists(tablename));
        assertTrue(columnInPrimaryKey(tablename, "id"));
        assertTrue(columnInPrimaryKey(tablename, "id2"));
        
    }
    
    public void testCreateTableWithSecondaryIndex() throws Exception {
        String tablename = "basicTable";
        Vector cols = new Vector();
        Vector pkcols = new Vector();
        
        dropTable(tablename);
        
        ColumnItem col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "id");
        TypeElement type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.PRIMARY_KEY, new Boolean(true));
        cols.add(col);

        col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "firstname");
        type = new TypeElement("java.sql.Types.VARCHAR", "VARCHAR");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.SIZE, "255");
        cols.add(col);
        
        col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "socsec");
        type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.INDEX, new Boolean(true));
        cols.add(col);
        
        CreateTableDDL ddl = new CreateTableDDL(spec, SCHEMA, tablename);
        
        boolean wasException = ddl.execute(cols, pkcols);
        
        assertFalse(wasException);
        
        assertTrue(tableExists(tablename));
        assertTrue(columnInPrimaryKey(tablename, "id"));
        assertTrue(columnInAnyIndex(tablename, "socsec"));
        
    }
}
