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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import junit.framework.*;
import org.netbeans.modules.db.test.jdbcstub.ConnectionImpl;
import org.netbeans.test.stub.api.Stub;

/**
 *
 * @author Andrei Badea
 */
public class CatalogTest extends TestCase {
    
    private Connection conn;
    private DatabaseMetaData metaData;
    
    public CatalogTest(String testName) {
        super(testName);
    }
    
    private void createConnection(String[] catalogNames, String[] schemaNames, String[][] tableNamesBySchema) {
        metaData = (DatabaseMetaData)Stub.create(DatabaseMetaData.class, new SimpleDatabaseMetaDataImpl(catalogNames, schemaNames, tableNamesBySchema));
        conn = (Connection)Stub.create(Connection.class, new ConnectionImpl(metaData));
    }
    
    public void testGetSchemasWhenDefaultCatalog() throws Exception {
        createConnection(new String[0], new String[] { "schema2", "schema1" }, new String[0][0]);
        
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        Catalog catalog = provider.getCatalog(null);
        
        Schema[] schemas = catalog.getSchemas();
        assertEquals("schema1", schemas[0].getName());
        assertEquals("schema2", schemas[1].getName());
        
        assertSame(schemas[0], catalog.getSchema("schema1"));
        assertSame(schemas[1], catalog.getSchema("schema2"));
    }
    
    public void testGetSchemasWhenMultipleCatalogs() throws Exception {
        createConnection(new String[] { "cat2", "cat1" }, new String[0], new String[][] { new String[] { "schema2", "s2table2", "s2table1" }, new String[] { "schema1", "s1table1" } });
        
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        Catalog catalog = provider.getCatalog("cat1");
        
        Schema[] schemas = catalog.getSchemas();
        assertEquals("schema1", schemas[0].getName());
        assertEquals("schema2", schemas[1].getName());
        
        assertSame(schemas[0], catalog.getSchema("schema1"));
        assertSame(schemas[1], catalog.getSchema("schema2"));
    }
    
    public void testGetSchemasCache() throws Exception {
        createConnection(new String[] { "cat2", "cat1" }, new String[0], new String[][] { new String[] { "schema2", "s2table2", "s2table1" }, new String[] { "schema1", "s1table1" } });
        
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        Catalog catalog = provider.getCatalog("cat1");
        
        Schema[] schemas1 = catalog.getSchemas();
        assertEquals("schema1", schemas1[0].getName());
        assertEquals("schema2", schemas1[1].getName());
        
        ((SimpleDatabaseMetaDataImpl)Stub.getDelegate(metaData)).setTables(new String[][] { new String[] { "newschema2", "s2table2", "s2table1" }, new String[] { "newschema1", "s1table1" } });

        Schema[] schemas2 = catalog.getSchemas();
        assertEquals("schema1", schemas2[0].getName());
        assertEquals("schema2", schemas2[1].getName());
    }
}
