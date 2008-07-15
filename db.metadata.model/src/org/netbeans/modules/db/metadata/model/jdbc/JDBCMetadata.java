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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.db.metadata.model.MetadataUtilities;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.spi.MetadataFactory;
import org.netbeans.modules.db.metadata.model.spi.MetadataImplementation;

/**
 *
 * @author Andrei Badea
 */
public class JDBCMetadata implements MetadataImplementation {

    private final Connection conn;
    private final String defaultSchemaName;
    private final DatabaseMetaData dmd;

    private Catalog defaultCatalog;
    private Map<String, Catalog> catalogs;

    public JDBCMetadata(Connection conn, String defaultSchemaName) throws SQLException {
        this.conn = conn;
        this.defaultSchemaName = defaultSchemaName;
        dmd = conn.getMetaData();
    }

    public Catalog getDefaultCatalog() throws SQLException {
        initCatalogs();
        return defaultCatalog;
    }

    public Collection<Catalog> getCatalogs() throws SQLException {
        return initCatalogs().values();
    }

    public Catalog getCatalog(String name) throws SQLException {
        return MetadataUtilities.find(name, initCatalogs());
    }

    private Map<String, Catalog> initCatalogs() throws SQLException {
        if (catalogs != null) {
            return catalogs;
        }
        Map<String, Catalog> newCatalogs = new LinkedHashMap<String, Catalog>();
        String defaultCatalogName = conn.getCatalog();
        ResultSet rs = dmd.getCatalogs();
        try {
            while (rs.next()) {
                String catalogName = rs.getString("TABLE_CAT"); // NOI18N
                if (MetadataUtilities.equals(catalogName, defaultCatalogName)) {
                    defaultCatalog = MetadataFactory.createCatalog(new JDBCCatalog(this, catalogName, defaultSchemaName));
                    newCatalogs.put(defaultCatalog.getName(), defaultCatalog);
                } else {
                    newCatalogs.put(catalogName, MetadataFactory.createCatalog(new JDBCCatalog(this, catalogName)));
                }
            }
        } finally {
            rs.close();
        }
        if (defaultCatalog == null) {
            defaultCatalog = MetadataFactory.createCatalog(new JDBCCatalog(this, null, defaultSchemaName));
            newCatalogs.put(defaultCatalog.getName(), defaultCatalog);
        }
        catalogs = Collections.unmodifiableMap(newCatalogs);
        return catalogs;
    }

    public Connection getConnection() {
        return conn;
    }

    public DatabaseMetaData getDmd() {
        return dmd;
    }
}
