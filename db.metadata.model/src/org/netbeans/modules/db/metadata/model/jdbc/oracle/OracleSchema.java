/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 - 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.metadata.model.jdbc.oracle;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCCatalog;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCSchema;

/**
 *
 * @author Andrei Badea
 */
public class OracleSchema extends JDBCSchema {

    private static final Logger LOGGER = Logger.getLogger(OracleSchema.class.getName());

    public OracleSchema(JDBCCatalog catalog, String name, boolean _default, boolean synthetic) {
        super(catalog, name, _default, synthetic);
    }

    @Override
    public String toString() {
        return "OracleSchema[name='" + name + "',default=" + _default + ",synthetic=" + synthetic + "]"; // NOI18N
    }

    @Override
    protected void createTables() {
        LOGGER.log(Level.FINE, "Initializing tables in {0}", this);
        Map<String, Table> newTables = new LinkedHashMap<String, Table>();
        try {
            DatabaseMetaData dmd = jdbcCatalog.getJDBCMetadata().getDmd();
            Set<String> recycleBinTables = getRecycleBinTables(dmd);
            ResultSet rs = dmd.getTables(jdbcCatalog.getName(), name, "%", new String[] { "TABLE" }); // NOI18N
            try {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME"); // NOI18N
                    if (!recycleBinTables.contains(tableName)) {
                        Table table = createJDBCTable(tableName).getTable();
                        newTables.put(tableName, table);
                        LOGGER.log(Level.FINE, "Created table {0}", table);
                    } else {
                        LOGGER.log(Level.FINE, "Ignoring recycle bin table ''{0}''", tableName);
                    }
                }
            } finally {
                rs.close();
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        tables = Collections.unmodifiableMap(newTables);
    }

    private Set<String> getRecycleBinTables(DatabaseMetaData dmd) {
        String driverName = null;
        String driverVer = null;
        try {
            driverName = dmd.getDriverName();
            driverVer = dmd.getDriverVersion();
            if (dmd.getDatabaseMajorVersion() < 10) {
                return Collections.emptySet();
            }
            Set<String> result = new HashSet<String>();
            Statement stmt = dmd.getConnection().createStatement();
            try {
                ResultSet rs = stmt.executeQuery("SELECT OBJECT_NAME FROM RECYCLEBIN WHERE TYPE = 'TABLE'"); // NOI18N
                try {
                    while (rs.next()) {
                        result.add(rs.getString("OBJECT_NAME")); // NOI18N
                    }
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
            return result;
        } catch (AbstractMethodError ame) {
            LOGGER.log(Level.INFO, "Error while analyzing the recycle bin. JDBC Driver: " + driverName + "(" + driverVer + ")", ame);
        } catch (SQLException e) {
            LOGGER.log(Level.INFO, "Error while analyzing the recycle bin. JDBC Driver: " + driverName + "(" + driverVer + ")", e);
        }
        return Collections.emptySet();
    }
}
