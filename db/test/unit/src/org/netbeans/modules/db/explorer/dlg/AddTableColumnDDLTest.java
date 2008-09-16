/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.explorer.dlg;

import org.netbeans.modules.db.test.DDLTestBase;

/**
 *
 * @author David
 */
public class AddTableColumnDDLTest extends DDLTestBase {

    public AddTableColumnDDLTest(String name) {
        super(name);
    }

    /**
     * Basic test, nothing fancy
     */
    public void testAddTableColumn() throws Exception {
        String tablename = "testAddColumn";
        String colname = "testColumn";
        String pkeyName = "id";
        
        createBasicTable(tablename, pkeyName);
        
        addColumn(tablename, colname);
        
        // Now verify the column exists
        assertTrue(columnExists(tablename, colname));
    }

    
    private void addColumn(String tablename, String colname) throws Exception {
        AddTableColumnDDL ddl = new AddTableColumnDDL(
                getSpecification(), getDriverSpecification(), getSchema(), fixIdentifier(tablename));
        
        ColumnItem col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, colname);
        TypeElement type = new TypeElement("java.sql.Types.VARCHAR", "VARCHAR");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.SIZE, "255");
        
        ddl.execute(colname, col, null);
    }

    public void testAddColumnToIndex() throws Exception {
        String tablename = "testAddColumn";
        String firstColname = "firstColumn";
        String secondColname = "secondColumn";
        String pkeyName = "id";
        String indexName = "idx";

        createBasicTable(tablename, pkeyName);     
        addColumn(tablename, firstColname);
        createSimpleIndex(tablename, indexName, firstColname);

        AddTableColumnDDL ddl = new AddTableColumnDDL(
                getSpecification(), getDriverSpecification(), getSchema(), fixIdentifier(tablename));
        
        ColumnItem col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, secondColname);
        TypeElement type = new TypeElement("java.sql.Types.VARCHAR", "VARCHAR");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.SIZE, "20");
        col.setProperty(ColumnItem.INDEX, new Boolean(true));
        
        ddl.execute(secondColname, col, fixIdentifier(indexName));
        
        // Now verify the column exists and is part of the index
        assertTrue(columnInIndex(tablename, secondColname, 
            indexName));        
        
    }    
}
