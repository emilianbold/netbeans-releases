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

package org.netbeans.modules.db.metadata.model.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCMetadata;
import org.netbeans.modules.db.metadata.model.test.api.MetadataTestBase;

/**
 *
 * @author Andrei Badea
 */
public class MetadataElementHandleTest extends MetadataTestBase {

    private Connection conn;
    private Metadata metadata;

    public MetadataElementHandleTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        clearWorkDir();
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        conn = DriverManager.getConnection("jdbc:derby:" + getWorkDirPath() + "/test;create=true");
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE TABLE FOO (" +
                "ID INT NOT NULL PRIMARY KEY, " +
                "FOO VARCHAR(16))");
        stmt.close();
        metadata = new JDBCMetadata(conn, "APP").getMetadata();
    }

    public void testResolve() {
        Catalog catalog = metadata.getDefaultCatalog();
        MetadataElementHandle<Catalog> catalogHandle = MetadataElementHandle.create(catalog);
        Catalog resolvedCatalog = catalogHandle.resolve(metadata);
        assertSame(catalog, resolvedCatalog);

        Schema schema = metadata.getDefaultSchema();
        MetadataElementHandle<Schema> schemaHandle = MetadataElementHandle.create(schema);
        Schema resolvedSchema = schemaHandle.resolve(metadata);
        assertSame(schema, resolvedSchema);

        Table table = schema.getTable("FOO");
        MetadataElementHandle<Table> tableHandle = MetadataElementHandle.create(table);
        Table resolvedTable = tableHandle.resolve(metadata);
        assertSame(table, resolvedTable);

        Column column = table.getColumn("FOO");
        MetadataElementHandle<Column> columnHandle = MetadataElementHandle.create(column);
        Column resolvedColumn = columnHandle.resolve(metadata);
        assertSame(column, resolvedColumn);
    }

    public void testEquals() {
        MetadataElementHandle<Table> handle1 = MetadataElementHandle.create(Table.class, "CATALOG", null, "TABLE");
        MetadataElementHandle<Table> handle2 = MetadataElementHandle.create(Table.class, "CATALOG", null, "TABLE");
        MetadataElementHandle<Table> handle3 = MetadataElementHandle.create(Table.class, "CATALOG", null, "TABLE");

        MetadataElementHandle<Schema> fakeTableHandle = MetadataElementHandle.create(Schema.class, "CATALOG", null, "TABLE");

        // Reflexivity.
        assertTrue(handle1.equals(handle1));
        assertTrue(handle1.hashCode() == handle2.hashCode());
        // Symmetry.
        assertTrue(handle1.equals(handle2));
        assertTrue(handle2.equals(handle1));
        // Transitivity.
        assertTrue(handle2.equals(handle3));
        assertTrue(handle1.equals(handle3));

        // Not of the same kind, so not equal.
        assertFalse(handle1.equals(fakeTableHandle));
    }
}
