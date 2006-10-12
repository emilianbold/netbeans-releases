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

package org.netbeans.modules.j2ee.persistence.editor.completion.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author abadea
 */
public class Catalog {
    
    private final DBMetaDataProvider provider;
    private final String name;
    
    private Map/*<String, Schema>*/ schemas;
    
    Catalog(DBMetaDataProvider provider, String name) {
        this.provider = provider;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public synchronized Schema[] getSchemas() throws SQLException {
        if (schemas == null) {
            schemas = new TreeMap();
            ResultSet rs = null;
            
            if (name == null) {
                // assuming the current catalog when the catalog name is null
                rs = provider.getMetaData().getSchemas();
            } else {
                // DatabaseMetaData.getSchemas() can not be used to retrieved the
                // list of schemas in a given catalog, since it (e.g. for the JTDS
                // driver) only returns the schemas in the current catalog. The 
                // workaround is to retrieve all tables from all schemas in the given
                // catalog and obtain a schema list from that. This is not perfect, 
                // since it will not return the schemas containig neither tables nor views.
                rs = provider.getMetaData().getTables(name, "%", "%", new String[] { "TABLE", "VIEW" }); // NOI18N
            }

            try {
                while (rs.next()) {
                    String schemaName = rs.getString("TABLE_SCHEM"); // NOI18N
                    Schema schema = new Schema(provider, this, schemaName);
                    schemas.put(schemaName, schema);
                }
            } finally {
                rs.close();
            }
        }
        
        return (Schema[])schemas.values().toArray(new Schema[schemas.size()]);
    }
    
    public synchronized Schema getSchema(String name) throws SQLException {
        if (schemas == null) {
            getSchemas();
        }
        
        return (Schema)schemas.get(name);
    }
    
    public String toString() {
        return "Catalog[name='" + name + "']"; // NOI18N
    }
}
