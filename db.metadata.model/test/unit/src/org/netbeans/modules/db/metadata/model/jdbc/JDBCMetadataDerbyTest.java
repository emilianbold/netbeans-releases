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
import java.util.HashSet;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.SQLType;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.api.Tuple;
import org.netbeans.modules.db.metadata.model.api.View;
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
                "DEC_COL DECIMAL(12,2), " +
                "FOREIGN KEY (FOO_ID) REFERENCES FOO(ID))");
        stmt.executeUpdate("CREATE VIEW BARVIEW AS SELECT * FROM BAR");
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

        checkColumns(barTable, columns);
    }

    public void testViews() throws Exception {
        Schema schema = metadata.getDefaultSchema();

        Collection<View> views = schema.getViews();
        assertNames(new HashSet<String>(Arrays.asList("BARVIEW")), views);
        View barView = schema.getView("BARVIEW");
        assertTrue(views.contains(barView));
        assertSame(schema, barView.getParent());

        Collection<Column> columns = barView.getColumns();
        checkColumns(barView, columns);
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
        fooTable.refresh();
        assertNames(Arrays.asList("ID", "FOO_NAME", "NEW_COLUMN"), fooTable.getColumns());
    }

    private void checkColumns(Tuple parent, Collection<Column> columns) {
        assertEquals(4, columns.size());
        Column[] colarray = columns.toArray(new Column[4]);

        Column col = colarray[0];
        assertEquals("JDBCColumn[name=i+d, type=INTEGER, length=0, precision=10, radix=10, scale=0, nullable=NOT_NULLABLE, ordinal_position=1]", col.toString());
        assertEquals("i+d", col.getName());
        assertSame(parent, col.getParent());
        assertEquals(SQLType.INTEGER, col.getType());
        assertEquals(0, col.getLength());
        assertEquals(10, col.getRadix());
        assertEquals(10, col.getPrecision());
        assertEquals(0, col.getScale());
        assertEquals(1, col.getOrdinalPosition());

        col = colarray[1];
        assertEquals("JDBCColumn[name=FOO_ID, type=INTEGER, length=0, precision=10, radix=10, scale=0, nullable=NOT_NULLABLE, ordinal_position=2]", col.toString());

        col = colarray[2];
        assertEquals("JDBCColumn[name=BAR_NAME, type=VARCHAR, length=16, precision=0, radix=0, scale=0, nullable=NULLABLE, ordinal_position=3]", col.toString());

        col = colarray[3];
        assertEquals("JDBCColumn[name=DEC_COL, type=DECIMAL, length=0, precision=12, radix=10, scale=2, nullable=NULLABLE, ordinal_position=4]", col.toString());
    }
}
