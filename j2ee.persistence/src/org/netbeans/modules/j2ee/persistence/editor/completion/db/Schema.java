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
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.dbschema.DBException;
import org.netbeans.modules.dbschema.DBIdentifier;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.TableElement;
import org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider;
import org.netbeans.modules.dbschema.jdbcimpl.SchemaElementImpl;
import org.openide.ErrorManager;

/**
 * 
 * @author Andrei Badea
 */
public class Schema {
    
    private DBMetaDataProvider provider;
    private Catalog catalog;
    private String name;
    private Set/*<String>*/ tableNames;
    
    private ConnectionProvider cp;
    private SchemaElementImpl schemaElementImpl;
    private SchemaElement schemaElement;
    
    // XXX views
    
    Schema(DBMetaDataProvider provider, Catalog catalog, String name) {
        this.provider = provider;
        this.catalog = catalog;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public synchronized String[] getTableNames() throws SQLException {
        if (tableNames == null) {
            tableNames = getTableNamesByType("TABLE"); // NOI18N
        }
        
        return (String[])tableNames.toArray(new String[tableNames.size()]);
    }
    
    public TableElement getTable(String tableName) throws SQLException {
        SchemaElement schemaElement;
        synchronized (this) {
            schemaElement = this.schemaElement;
        }
        if (schemaElement == null) {
            cp = new ConnectionProvider(provider.getConnection(), provider.getDriverClass());
            cp.setSchema(name);
            schemaElementImpl = new SchemaElementImpl(cp);
            try {
                schemaElementImpl.setName(DBIdentifier.create("foo")); // XXX set a proper name
            } catch (DBException e) {
                ErrorManager.getDefault().notify(e);
            }
            schemaElement = new SchemaElement(schemaElementImpl);
            synchronized (this) {
                this.schemaElement = schemaElement;
            }
        }
        
        DBIdentifier tableId = DBIdentifier.create(tableName);
        TableElement tableElement = schemaElement.getTable(tableId);
        if (tableElement == null) {
            LinkedList tableList = new LinkedList();
            tableList.add(tableName);
            LinkedList viewList = new LinkedList();
            schemaElementImpl.initTables(cp, tableList, viewList, false);
            
            tableElement = schemaElement.getTable(tableId);
        }
        
        return tableElement;
    }
    
    public synchronized void refresh() {
        schemaElement = null;
        tableNames = null;
    }
    
    private Set/*<String>*/ getTableNamesByType(String type) throws SQLException {
        Set/*<String>*/ result = new TreeSet();

        ResultSet rs = provider.getMetaData().getTables(catalog.getName(), name, "%", new String[] { type }); // NOI18N
        try {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME"); // NOI18N
                result.add(tableName);
            }
        } finally {
            rs.close();
        }

        return result;
    }
    
    public String toString() {
        return "Schema[catalog=" + catalog + ",name='" + name + "']"; // NOI18N
    }
}
