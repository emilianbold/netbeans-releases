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
package org.netbeans.modules.db.explorer.action;

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
        // Quote the index name in this case because we're recreating an
        // existing index name
        icmd.setIndexName(icmd.quote(indexName));
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
