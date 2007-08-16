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

import java.util.Iterator;
import java.util.Set;
import org.netbeans.lib.ddl.impl.CreateIndex;
import org.netbeans.lib.ddl.impl.DropIndex;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.dlg.ColumnItem;

/**
 * Factors out logic to work with DDL package so we can unit test it
 */
public class AddToIndexDDL {
    Specification spec;
    String schema;
    String tablename;
    
    public AddToIndexDDL(Specification spec, String schema, 
            String tablename) {
        this.spec = spec;
        this.schema = schema;
        this.tablename = tablename;
    }
    
    public boolean execute(String indexName, boolean unique, Set columns) 
            throws Exception {
        CreateIndex icmd = spec.createCommandCreateIndex(tablename);
        icmd.setIndexName(indexName);
        icmd.setObjectOwner(schema);
        icmd.setIndexType(unique ? ColumnItem.UNIQUE : "");

        Iterator enu = columns.iterator();
        while (enu.hasNext()) {
            icmd.specifyColumn((String)enu.next());
        }

        DropIndex dicmd = spec.createCommandDropIndex(indexName);
        dicmd.setObjectOwner(schema);
        dicmd.setTableName(tablename);
        dicmd.execute();
        icmd.execute();
        
        return ( icmd.wasException() || dicmd.wasException());
    }

}
