/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.metadata.model.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.test.api.MetadataTestBase;

/**
 *
 * @author Andrei Badea
 */
public class JDBCMetadataDerbyTest extends MetadataTestBase {

    private Connection conn;
    private JDBCMetadata metadata;

    public JDBCMetadataDerbyTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        clearWorkDir();
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        conn = DriverManager.getConnection("jdbc:derby:" + getWorkDirPath() + "/test;create=true");
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE TABLE FOO (" +
                "ID INT NOT NULL PRIMARY KEY, " +
                "FOO_NAME VARCHAR(16))");
        stmt.executeUpdate("CREATE TABLE BAR (" +
                "\"i+d\" INT NOT NULL PRIMARY KEY, " +
                "FOO_ID INT NOT NULL, " +
                "BAR_NAME VARCHAR(16), " +
                "FOREIGN KEY (FOO_ID) REFERENCES FOO(ID))");
        stmt.close();
        metadata = new JDBCMetadata(conn, "APP");
    }

    public void testRunReadAction() throws Exception {
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        assertEquals(1, metadata.getCatalogs().size());
        assertTrue(metadata.getCatalogs().contains(defaultCatalog));
        assertNull(defaultCatalog.getName());
        assertNotNull(defaultCatalog.getSchema("NULLID"));
        assertNotNull(defaultCatalog.getSchema("SYSCAT"));

        Schema appSchema = defaultCatalog.getSchema("APP");
        assertSame(appSchema, metadata.getDefaultSchema());
        assertEquals("APP", appSchema.getName());
        assertFalse(appSchema.isSynthetic());
        assertTrue(appSchema.isDefault());

        Collection<Table> tables = appSchema.getTables();
        assertEquals(2, tables.size());
        Table fooTable = appSchema.getTable("FOO");
        Table barTable = appSchema.getTable("BAR");
        assertTrue(tables.contains(fooTable));
        assertTrue(tables.contains(barTable));
        assertEquals("FOO", fooTable.getName());
        assertEquals("BAR", barTable.getName());

        Collection<Column> columns = barTable.getColumns();
        assertEquals(3, columns.size());
        Iterator<Column> columnIterator = columns.iterator();
        assertEquals("i+d", columnIterator.next().getName());
        assertEquals("FOO_ID", columnIterator.next().getName());
        assertEquals("BAR_NAME", columnIterator.next().getName());
    }

    public void testRefresh() throws Exception {
        assertNull(metadata.getDefaultCatalog().getSchema("FOOBAR"));
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE SCHEMA FOOBAR");
        stmt.close();
        metadata.refresh();
        assertNotNull(metadata.getDefaultCatalog().getSchema("FOOBAR"));
    }

    public void testRefreshTable() throws Exception {
        Table fooTable = metadata.getDefaultSchema().getTable("FOO");
        assertNames(Arrays.asList("ID", "FOO_NAME"), fooTable.getColumns());
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("ALTER TABLE FOO ADD NEW_COLUMN VARCHAR(16)");
        stmt.close();
        metadata.refreshTable("BAR");
        assertNames(Arrays.asList("ID", "FOO_NAME"), fooTable.getColumns());
        metadata.refreshTable("FOO");
        assertNames(Arrays.asList("ID", "FOO_NAME", "NEW_COLUMN"), fooTable.getColumns());
    }
}
