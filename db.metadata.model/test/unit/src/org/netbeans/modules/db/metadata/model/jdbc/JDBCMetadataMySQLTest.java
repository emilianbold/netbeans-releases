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
import org.netbeans.modules.db.metadata.model.api.Parameter;
import org.netbeans.modules.db.metadata.model.api.Parameter.Direction;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.api.View;
import org.netbeans.modules.db.metadata.model.api.Procedure;
import org.netbeans.modules.db.metadata.model.api.SQLType;
import org.netbeans.modules.db.metadata.model.api.Tuple;
import org.netbeans.modules.db.metadata.model.jdbc.mysql.MySQLMetadata;
import org.netbeans.modules.db.metadata.model.test.api.MetadataTestBase;

/**
 *
 * @author Andrei Badea
 */
public class JDBCMetadataMySQLTest extends MetadataTestBase {

    private JDBCMetadata metadata;
    private String defaultCatalogName;

    public JDBCMetadataMySQLTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        String mysqlHost = System.getProperty("mysql.host", "localhost");
        int mysqlPort = Integer.getInteger("mysql.port", 3306);
        String mysqlDatabase = System.getProperty("mysql.database", "test");
        String mysqlUser = System.getProperty("mysql.user", "test");
        String mysqlPassword = System.getProperty("mysql.password", "test");
        clearWorkDir();
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase, mysqlUser, mysqlPassword);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DROP DATABASE test");
        stmt.executeUpdate("CREATE DATABASE test");
        stmt.executeUpdate("USE test");
        stmt.executeUpdate("CREATE TABLE foo (" +
                "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                "FOO_NAME VARCHAR(16))");
        stmt.executeUpdate("CREATE TABLE bar (" +
                "`i+d` INT NOT NULL PRIMARY KEY, " +
                "foo_id INT NOT NULL, " +
                "bar_name  VARCHAR(16), " +
                "bar_digit DECIMAL(12,2) NOT NULL, " +
                "FOREIGN KEY (foo_id) REFERENCES foo(id))");
        stmt.executeUpdate("CREATE VIEW barview AS SELECT * FROM bar");
        // TODO - test precision, scale, radix
        stmt.executeUpdate("CREATE PROCEDURE barproc(IN param1 INT, OUT result VARCHAR(255), INOUT param2 DECIMAL(5,2)) " +
                "BEGIN SELECT * from bar; END");
        stmt.executeUpdate("CREATE PROCEDURE fooproc(IN param1 INT) " +
                "BEGIN SELECT * from foo; END");
        metadata = new MySQLMetadata(conn, null);
        defaultCatalogName = mysqlDatabase;
    }

    public void testRunReadAction() throws Exception {
        Collection<Catalog> catalogs = metadata.getCatalogs();
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        assertEquals(defaultCatalogName, defaultCatalog.getName());
        assertTrue(catalogs.contains(defaultCatalog));

        Catalog informationSchema = metadata.getCatalog("information_schema");
        assertFalse(informationSchema.isDefault());
        Schema syntheticSchema = informationSchema.getSyntheticSchema();
        assertNotNull(syntheticSchema);
        assertFalse("Only the default catalog should have a default schema", syntheticSchema.isDefault());

        Schema schema = metadata.getDefaultSchema();
        assertTrue(schema.isSynthetic());
        assertTrue(schema.isDefault());
        assertSame(schema, defaultCatalog.getSyntheticSchema());
        assertSame(defaultCatalog, schema.getParent());

        Collection<Table> tables = schema.getTables();
        assertNames(new HashSet<String>(Arrays.asList("foo", "bar")), tables);
        Table barTable = schema.getTable("bar");
        assertTrue(tables.contains(barTable));
        assertSame(schema, barTable.getParent());

        checkColumns(barTable);
    }

    public void testViews() throws Exception {
        Schema schema = metadata.getDefaultSchema();

        Collection<View> views = schema.getViews();
        assertNames(new HashSet<String>(Arrays.asList("barview")), views);
        View barView = schema.getView("barview");
        assertTrue(views.contains(barView));
        assertSame(schema, barView.getParent());

        checkColumns(barView);
    }

    public void testProcedures() throws Exception {
        Schema schema = metadata.getDefaultSchema();

        Collection<Procedure> procs = schema.getProcedures();
        assertNames(Arrays.asList("barproc", "fooproc"), procs);

        Procedure barProc = schema.getProcedure("barproc");
        Collection<Parameter> barParams = barProc.getParameters();
        assertNames(Arrays.asList("param1", "result", "param2"), barParams);

        // MySQL does not tell you what the result columns are - bummer
        assertEquals(0, barProc.getColumns().size());
        
        Procedure fooProc = schema.getProcedure("fooproc");
        Collection<Parameter> fooParams = fooProc.getParameters();
        assertNames(Arrays.asList("param1"), fooParams);

        // MySQL does not tell you what the result columns are - bummer
        assertEquals(0, barProc.getColumns().size());

        Parameter param = barProc.getParameter("param1");
        assertTrue(barParams.contains(param));
        assertSame(barProc, param.getParent());
        assertEquals("JDBCParameter[name=param1, type=INTEGER, length=0, precision=10, radix=10, scale=0, nullable=NOT_NULLABLE, direction=IN, position=1]", param.toString());
        assertEquals(SQLType.INTEGER, param.getType());
        assertEquals(0, param.getLength());
        assertEquals(Direction.IN, param.getDirection());
        assertEquals(10, param.getPrecision());
        assertEquals(0, param.getScale());
        assertEquals(10, param.getRadix());

        param = barProc.getParameter("result");
        assertTrue(barParams.contains(param));
        assertSame(barProc, param.getParent());
        assertEquals("JDBCParameter[name=result, type=VARCHAR, length=255, precision=0, radix=10, scale=0, nullable=NOT_NULLABLE, direction=OUT, position=2]", param.toString());

        param = barProc.getParameter("param2");
        assertTrue(barParams.contains(param));
        assertSame(barProc, param.getParent());
        assertEquals("JDBCParameter[name=param2, type=DECIMAL, length=0, precision=5, radix=10, scale=2, nullable=NOT_NULLABLE, direction=INOUT, position=3]", param.toString());
    }

    private void checkColumns(Tuple parent) {
        Collection<Column> columns = parent.getColumns();
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
        assertEquals("JDBCColumn[name=foo_id, type=INTEGER, length=0, precision=10, radix=10, scale=0, nullable=NOT_NULLABLE, ordinal_position=2]", col.toString());

        col = colarray[2];
        assertEquals("JDBCColumn[name=bar_name, type=VARCHAR, length=16, precision=0, radix=10, scale=0, nullable=NULLABLE, ordinal_position=3]", col.toString());

        col = colarray[3];
        assertEquals(12, col.getPrecision());
        assertEquals(2, col.getScale());
        assertEquals("JDBCColumn[name=bar_digit, type=DECIMAL, length=0, precision=12, radix=10, scale=2, nullable=NOT_NULLABLE, ordinal_position=4]", col.toString());
    }
}
