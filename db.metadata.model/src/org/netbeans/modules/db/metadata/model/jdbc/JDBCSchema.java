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
import org.netbeans.modules.db.metadata.model.MetadataUtilities;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.spi.MetadataFactory;
import org.netbeans.modules.db.metadata.model.spi.SchemaImplementation;

/**
 *
 * @author Andrei Badea
 */
public class JDBCSchema implements SchemaImplementation {

    private final JDBCCatalog catalog;
    private final String name;
    private final boolean _default;
    private final boolean synthetic;

    private Map<String, Table> tables;

    public JDBCSchema(JDBCCatalog catalog, String name) {
        this(catalog, name, false, false);
    }

    public JDBCSchema(JDBCCatalog catalog, String name, boolean synthetic) {
        this(catalog, name, true, synthetic);
    }

    private JDBCSchema(JDBCCatalog catalog, String name, boolean _default, boolean synthetic) {
        this.catalog = catalog;
        this.name = name;
        this._default = _default;
        this.synthetic = synthetic;
    }

    public boolean isDefault() {
        return _default;
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public String getName() {
        return name;
    }

    public Collection<Table> getTables() {
        return initTables().values();
    }

    public Table getTable(String name) {
        return MetadataUtilities.find(name, initTables());
    }

    @Override
    public String toString() {
        return "Schema[name='" + name + "']"; // NOI18N
    }

    private Map<String, Table> initTables() {
        if (tables != null) {
            return tables;
        }
        Map<String, Table> newTables = new LinkedHashMap<String, Table>();
        try {
            ResultSet rs = catalog.getMetadata().getDmd().getTables(catalog.getName(), name, "%", new String[] { "TABLE" }); // NOI18N
            try {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME"); // NOI18N
                    newTables.put(tableName, MetadataFactory.createTable(new JDBCTable(this, tableName)));
                }
            } finally {
                rs.close();
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        tables = Collections.unmodifiableMap(newTables);
        return tables;
    }

    public JDBCCatalog getCatalog() {
        return catalog;
    }
}
