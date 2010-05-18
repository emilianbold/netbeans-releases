/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle.Kind;
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
        stmt.executeUpdate("CREATE TABLE BAR (ID INT NOT NULL PRIMARY KEY, FOO_ID INT NOT NULL, FOREIGN KEY (FOO_ID) REFERENCES FOO)");
        stmt.executeUpdate("CREATE INDEX FOO_INDEX ON FOO(FOO)");
        stmt.executeUpdate("CREATE VIEW FOOVIEW AS SELECT * FROM FOO");
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

        PrimaryKey key = table.getPrimaryKey();
        MetadataElementHandle<PrimaryKey> keyHandle = MetadataElementHandle.create(key);
        PrimaryKey resolvedKey = keyHandle.resolve(metadata);
        assertSame(key, resolvedKey);

        Column column = table.getColumn("FOO");
        MetadataElementHandle<Column> columnHandle = MetadataElementHandle.create(column);
        Column resolvedColumn = columnHandle.resolve(metadata);
        assertSame(column, resolvedColumn);

        Index index = table.getIndex("FOO_INDEX");
        MetadataElementHandle<Index> indexHandle = MetadataElementHandle.create(index);
        Index resolvedIndex = indexHandle.resolve(metadata);
        assertSame(index, resolvedIndex);

        Table barTable = schema.getTable("BAR");
        ForeignKey fkey = barTable.getForeignKeys().toArray(new ForeignKey[0])[0];
        MetadataElementHandle<ForeignKey> fkeyHandle = MetadataElementHandle.create(fkey);
        ForeignKey resolvedForeignKey = fkeyHandle.resolve(metadata);
        assertSame(fkey, resolvedForeignKey);

        View view = schema.getView("FOOVIEW");
        MetadataElementHandle<View> viewHandle = MetadataElementHandle.create(view);
        View resolvedView = viewHandle.resolve(metadata);
        assertSame(view, resolvedView);

        // Negative test - what happens if you create a handle for null
        try {
            MetadataElementHandle<Catalog> bogusHandle = MetadataElementHandle.create((Catalog)null);
            fail("Should have thrown a NullPointerException");
        } catch (NullPointerException npe) {
            //expected
        }
    }

    public void testEquals() {
        String[] names = new String[] {"CATALOG", null, "TABLEORVIEW"};

        Kind[] tableKinds = new Kind[] {Kind.CATALOG, Kind.SCHEMA, Kind.TABLE};
        Kind[] viewKinds = new Kind[] {Kind.CATALOG, Kind.SCHEMA, Kind.VIEW};
        Kind[] schemaKinds = new Kind[] {Kind.CATALOG, Kind.SCHEMA};

        MetadataElementHandle<? extends MetadataElement> handle1 = MetadataElementHandle.create(Table.class, names, tableKinds);
        MetadataElementHandle<? extends MetadataElement> handle2 = MetadataElementHandle.create(Table.class, names, tableKinds);
        MetadataElementHandle<? extends MetadataElement> handle3 = MetadataElementHandle.create(Table.class, names, tableKinds);

        MetadataElementHandle<? extends MetadataElement> schemaHandle = MetadataElementHandle.create(Schema.class, names, schemaKinds);

        checkHandles(handle1, handle2, handle3, schemaHandle);

        handle1 = MetadataElementHandle.create(View.class, names, viewKinds);
        handle2 = MetadataElementHandle.create(View.class, names, viewKinds);
        handle3 = MetadataElementHandle.create(View.class, names, viewKinds);

        checkHandles(handle1, handle2, handle3, schemaHandle);
    }

    private void checkHandles(MetadataElementHandle<? extends MetadataElement> handle1,
            MetadataElementHandle<? extends MetadataElement> handle2,
            MetadataElementHandle<? extends MetadataElement> handle3,
            MetadataElementHandle<? extends MetadataElement> badHandle) {
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
        assertFalse(handle1.equals(badHandle));
    }
}
