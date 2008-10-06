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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.MetadataAccessor;
import org.netbeans.modules.db.metadata.model.MetadataUtilities;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.spi.SchemaImplementation;

/**
 *
 * @author Andrei Badea
 */
public class JDBCSchema extends SchemaImplementation {

    private static final Logger LOGGER = Logger.getLogger(JDBCSchema.class.getName());

    protected final JDBCCatalog jdbcCatalog;
    protected final String name;
    protected final boolean _default;
    protected final boolean synthetic;

    protected Map<String, Table> tables;

    public JDBCSchema(JDBCCatalog jdbcCatalog, String name, boolean _default, boolean synthetic) {
        this.jdbcCatalog = jdbcCatalog;
        this.name = name;
        this._default = _default;
        this.synthetic = synthetic;
    }

    public final Catalog getParent() {
        return jdbcCatalog.getCatalog();
    }

    public final String getName() {
        return name;
    }

    public final boolean isDefault() {
        return _default;
    }

    public final boolean isSynthetic() {
        return synthetic;
    }

    public final Collection<Table> getTables() {
        return initTables().values();
    }

    public final Table getTable(String name) {
        return MetadataUtilities.find(name, initTables());
    }

    @Override
    public String toString() {
        return "JDBCSchema[name='" + name + "',default=" + _default + ",synthetic=" + synthetic + "]"; // NOI18N
    }

    protected JDBCTable createJDBCTable(String name) {
        return new JDBCTable(this, name);
    }

    protected void createTables() {
        LOGGER.log(Level.FINE, "Initializing tables in {0}", this);
        Map<String, Table> newTables = new LinkedHashMap<String, Table>();
        try {
            ResultSet rs = jdbcCatalog.getJDBCMetadata().getDmd().getTables(jdbcCatalog.getName(), name, "%", new String[] { "TABLE", "SYSTEM TABLE" }); // NOI18N
            try {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME"); // NOI18N
                    Table table = createJDBCTable(tableName).getTable();
                    newTables.put(tableName, table);
                    LOGGER.log(Level.FINE, "Created table {0}", table);
                }
            } finally {
                rs.close();
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        tables = Collections.unmodifiableMap(newTables);
    }

    private Map<String, Table> initTables() {
        if (tables != null) {
            return tables;
        }
        createTables();
        return tables;
    }

    public final JDBCCatalog getJDBCCatalog() {
        return jdbcCatalog;
    }

    public final void refreshTable(String tableName) {
        if (tables == null) {
            return;
        }
        Table table = MetadataUtilities.find(tableName, tables);
        if (table == null) {
            return;
        }
        ((JDBCTable) MetadataAccessor.getDefault().getTableImpl(table)).refresh();
    }
}
