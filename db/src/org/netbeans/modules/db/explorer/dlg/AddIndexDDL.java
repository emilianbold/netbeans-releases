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

import java.util.Iterator;
import java.util.Set;
import org.netbeans.lib.ddl.impl.CreateIndex;
import org.netbeans.lib.ddl.impl.Specification;

/**
 * DDL for creating an index, refactored out of the AddIndex dialog
 * 
 * @author <a href="mailto:david@vancouvering.com">David Van Couvering</a>
 */
public class AddIndexDDL {
    private Specification       spec;
    private String              schema;
    private String              tablename;

    public AddIndexDDL (
            Specification spec, 
            String schema,
            String tablename) {
        this.spec       = spec;
        this.schema     = schema;
        this.tablename  = tablename;
    }
    
    /**
     * Execute the DDL to create an index.  
     * 
     * @param indexName the name of the index
     * @param isUnique set to true if a unique index
     * @param columns - A Vector of ColumnItem representing the columns
     *      in the index
     */
    public boolean execute(String indexName, 
            boolean isUnique, Set columns) throws Exception {
        CreateIndex icmd = spec.createCommandCreateIndex(tablename);
        
        icmd.setObjectOwner(schema);
        icmd.setIndexName(indexName);
        icmd.setIndexType(isUnique ? ColumnItem.UNIQUE : "");
        
        Iterator enu = columns.iterator();
        while (enu.hasNext()) {
            icmd.specifyColumn((String)enu.next());
        }
        
        icmd.execute();

        return icmd.wasException();
    }    
}
