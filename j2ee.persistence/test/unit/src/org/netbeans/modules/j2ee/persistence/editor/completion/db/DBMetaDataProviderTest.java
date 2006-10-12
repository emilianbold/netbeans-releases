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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.test.jdbcstub.ConnectionImpl;
import org.netbeans.test.stub.api.Stub;
import org.netbeans.test.stub.api.StubDelegate;

/**
 *
 * @author Andrei Badea
 */
public class DBMetaDataProviderTest extends NbTestCase {
    
    private Connection conn;
    private DatabaseMetaData metaData;
    
    public DBMetaDataProviderTest(String testName) {
        super(testName);
    }
    
    private void createConnection(String[] catalogNames) {
        metaData = (DatabaseMetaData)Stub.create(DatabaseMetaData.class, new SimpleDatabaseMetaDataImpl(catalogNames));
        conn = (Connection)Stub.create(Connection.class, new ConnectionImpl(metaData));
    }
    
    public void testSameProviderForSameConnection() {
        createConnection(new String[0]);
        
        DBMetaDataProvider provider1 = DBMetaDataProvider.get(conn, "");
        DBMetaDataProvider provider2 = DBMetaDataProvider.get(conn, "");
        assertSame("Same provider for the same connection", provider1, provider2);
    }
    
    public void testConnectionAndProviderLeak() {
        createConnection(new String[0]);
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        
        Reference ref = new WeakReference(conn);
        conn = null;
        assertGC("The connection can be GCd", ref);
        
        // causes the stale entries (those, whose keys have been GCd) to be removed from the weak map
        DBMetaDataProvider.get((Connection)Stub.create(Connection.class), "");
        
        ref = new WeakReference(provider);
        provider = null;
        assertGC("The provider can be GCd", ref);
    }
    
    public void testGetCatalogs() throws Exception {
        createConnection(new String[] { "cat2", "cat1" });
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        
        Catalog[] catalogs = provider.getCatalogs();
        assertEquals("cat1", catalogs[0].getName());
        assertEquals("cat2", catalogs[1].getName());
        
        assertSame(catalogs[0], provider.getCatalog("cat1"));
        assertSame(catalogs[1], provider.getCatalog("cat2"));
    }
    
    public void testGetCatalogsCache() throws Exception {
        createConnection(new String[] { "cat1", "cat2"  });
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        
        Catalog[] catalogs1 = provider.getCatalogs();
        assertEquals("cat1", catalogs1[0].getName());
        assertEquals("cat2", catalogs1[1].getName());
        
        ((SimpleDatabaseMetaDataImpl)Stub.getDelegate(metaData)).setCatalogs(new String[] { "newcat1", "newcat2" });
        
        Catalog[] catalogs2 = provider.getCatalogs();
        assertEquals("cat1", catalogs2[0].getName());
        assertEquals("cat2", catalogs2[1].getName());
    }
    
    public void testGetCatalogsWhenNoCatalogs() throws Exception {
        createConnection(new String[0]);
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        
        Catalog[] catalogs = provider.getCatalogs();
        assertNull(catalogs[0].getName());
        assertSame(catalogs[0], provider.getCatalog(null));
    }
}
