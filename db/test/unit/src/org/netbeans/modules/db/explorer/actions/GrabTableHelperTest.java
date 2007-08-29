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

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.sql.Types;
import java.util.Iterator;
import java.util.Vector;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.TableColumn;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.util.DBTestBase;
import org.netbeans.modules.db.util.InfoHelper;

/**
 * @author David Van Couvering
 */
public class GrabTableHelperTest extends DBTestBase {

    public GrabTableHelperTest(String name) {
        super(name);
    }
    
    public void testGrabTable() throws Exception {
        File file = null;
        InfoHelper infoHelper = new InfoHelper(spec, drvSpec, conn);
        
        try {
            String tablename = "grabtable";
            String pkName = "id";
            String col1 = "col1";
            String col2 = "col2";
            String filename = "grabtable.grab";

            if ( dblocation != null  &&  dblocation.length() > 0 ) {
                filename = dblocation + "/" + filename;
            }

            file = new File(filename);
            if ( file.exists() ) {
                file.delete();
            }            

            createBasicTable(tablename, pkName);
            addBasicColumn(tablename, col1, Types.VARCHAR, 255);
            addBasicColumn(tablename, col2, Types.INTEGER, 0);

            // Initialize the table information in the format required
            // by the helper.  This is done by creating a DatabaseNodeInfo
            // for the table
            DatabaseNodeInfo tableInfo = infoHelper.getTableInfo(tablename);

            new GrabTableHelper().execute(spec, tablename, 
                    tableInfo.getChildren().elements(), file);
            
            assertTrue(file.exists());
            
            // Now recreate the table info and make sure it's accurate 
            FileInputStream fstream = new FileInputStream(file);
            ObjectInputStream istream = new ObjectInputStream(fstream);
            CreateTable cmd = (CreateTable)istream.readObject();
            istream.close();
            cmd.setSpecification(spec);
            cmd.setObjectOwner(SCHEMA);
            
            assertEquals(tablename, cmd.getObjectName());
            
            Vector cols = cmd.getColumns();
            assertTrue(cols.size() == 3);
            
            Iterator it = cols.iterator();
            
            while ( it.hasNext() ) {
                TableColumn col = (TableColumn)it.next();
                
                if ( col.getColumnName().equals(pkName)) {
                    assertEquals(col.getColumnType(), Types.INTEGER);  
                    assertEquals(col.getObjectType(), TableColumn.PRIMARY_KEY);
                } else if ( col.getColumnName().equals(col1) ) {
                    assertEquals(col.getColumnType(), Types.VARCHAR);
                    assertEquals(col.getColumnSize(), 255);
                } else if ( col.getColumnName().equals(col2) ) {
                    assertEquals(col.getColumnType(), Types.INTEGER);
                } else {
                    fail("Unexpected column with name " + col.getColumnName());
                }
            }
            
            // OK, now see if we can actually create this guy
            dropTable(tablename);
            cmd.execute();
            
            assertFalse(cmd.wasException());
            
            dropTable(tablename);
        } finally {        
            if ( file != null && file.exists()) {
                file.delete();
            }
        }
    }
}
