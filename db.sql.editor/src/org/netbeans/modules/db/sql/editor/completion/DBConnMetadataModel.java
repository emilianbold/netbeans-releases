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

package org.netbeans.modules.db.sql.editor.completion;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.Parameters;

/**
 *
 * @author Andrei Badea
 */
public class DBConnMetadataModel implements MetadataModel {

    private final DatabaseConnection dbconn;
    private Connection conn;
    private DatabaseMetaData dmd;
    private boolean initialized;

    public DBConnMetadataModel(DatabaseConnection dbconn) {
        this.dbconn = dbconn;
    }

    public String getDefaultSchemaName() {
        String schema = dbconn.getSchema();
        if (schema == null) {
            schema = NO_SCHEMA_NAME;
        }
        return schema;
    }

    private DatabaseMetaData getMetaData() {
        if (initialized) {
            return dmd;
        }
        initialized = true;
        conn = dbconn.getJDBCConnection();
        if (conn == null) {
            conn = Mutex.EVENT.readAccess(new Mutex.Action<Connection>() {
                public Connection run() {
                    ConnectionManager.getDefault().showConnectionDialog(dbconn);
                    return dbconn.getJDBCConnection();
                }
            });
        }
        if (conn == null) {
            return null;
        }
        try {
            dmd = conn.getMetaData();
        } catch (SQLException e) {
            Exceptions.printStackTrace(e);
        }
        return dmd;
    }

    public List<String> getSchemaNames() {
        DatabaseMetaData dmd = getMetaData();
        if (dmd == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        try {
            String defaultCatalog = conn.getCatalog();
            ResultSet rs = dmd.getSchemas();
            try {
                while (rs.next()) {
                    String dbCatalog = rs.getString("TABLE_CATALOG"); // NOI18N
                    if (!compareStringsIgnoreCase(dbCatalog, defaultCatalog)) {
                        continue;
                    }
                    String dbSchema = rs.getString("TABLE_SCHEM"); // NOI18N
                    if (dbSchema == null) {
                        continue;
                    }
                    result.add(dbSchema);
                }
            } finally {
                rs.close();
            }
        } catch (SQLException e) {
            Exceptions.printStackTrace(e);
        }
        return result;
    }

    public List<String> getTableNames(String schema) {
        Parameters.notNull("schema", schema);
        DatabaseMetaData dmd = getMetaData();
        if (dmd == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        try {
            String defaultCatalog = conn.getCatalog();
            ResultSet rs = dmd.getTables(defaultCatalog, null, null, new String[] { "TABLE", "VIEW" }); // NOI18N
            try {
                while (rs.next()) {
                    String dbSchema = rs.getString("TABLE_SCHEM"); // NOI18N
                    if (!compareStringsIgnoreCase(dbSchema, schema)) {
                        continue;
                    }
                    String dbTable = rs.getString("TABLE_NAME"); // NOI18N
                    if (dbTable == null) {
                        continue;
                    }
                    result.add(dbTable);
                }
            } finally {
                rs.close();
            }
        } catch (SQLException e) {
            Exceptions.printStackTrace(e);
        }
        return result;
    }

    public List<String> getColumnNames(String schema, String table) {
        Parameters.notNull("schema", schema);
        Parameters.notNull("table", table);
        DatabaseMetaData dmd = getMetaData();
        if (dmd == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        try {
            String defaultCatalog = conn.getCatalog();
            ResultSet rs = dmd.getColumns(defaultCatalog, null, null, null); // NOI18N
            try {
                while (rs.next()) {
                    String dbSchema = rs.getString("TABLE_SCHEM"); // NOI18N
                    if (!compareStringsIgnoreCase(dbSchema, schema)) {
                        continue;
                    }
                    String dbTable = rs.getString("TABLE_NAME"); // NOI18N
                    if (!compareStringsIgnoreCase(dbTable, table)) {
                        continue;
                    }
                    String dbColumn = rs.getString("COLUMN_NAME"); // NOI18N
                    if (dbColumn == null) {
                        continue;
                    }
                    result.add(dbColumn);
                }
            } finally {
                rs.close();
            }
        } catch (SQLException e) {
            Exceptions.printStackTrace(e);
        }
        return result;
    }

    private static boolean compareStringsIgnoreCase(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        } else {
            return str1.equalsIgnoreCase(str2);
        }
    }

    public String getIdentifierQuoteString() {
        DatabaseMetaData dmd = getMetaData();
        if (dmd == null) {
            return null;
        }
        String result = null;
        try {
            result = dmd.getIdentifierQuoteString();
        } catch (SQLException e) {
            Exceptions.printStackTrace(e);
        }
        return result;
    }
}
