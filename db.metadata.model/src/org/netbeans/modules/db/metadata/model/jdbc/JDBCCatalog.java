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
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.spi.CatalogImplementation;
import org.netbeans.modules.db.metadata.model.spi.MetadataFactory;

/**
 *
 * @author Andrei Badea
 */
public class JDBCCatalog implements CatalogImplementation {

    private final JDBCMetadata metadata;
    private final String name;
    private final boolean _default;
    private final String defaultSchemaName;

    private Schema defaultSchema;
    private Map<String, Schema> schemas;

    public JDBCCatalog(JDBCMetadata metadata, String name) {
        this(metadata, name, false, null);
    }

    public JDBCCatalog(JDBCMetadata metadata, String name, String defaultSchemaName) {
        this(metadata, name, true, defaultSchemaName);
    }

    private JDBCCatalog(JDBCMetadata metadata, String name, boolean _default, String defaultSchemaName) {
        this.metadata = metadata;
        this.name = name;
        this._default = _default;
        this.defaultSchemaName = defaultSchemaName;
    }

    public String getName() {
        return name;
    }

    public boolean isDefault() {
        return _default;
    }

    public Schema getDefaultSchema() {
        initSchemas();
        return defaultSchema;
    }

    public Collection<Schema> getSchemas() {
        return initSchemas().values();
    }

    public Schema getSchema(String name) {
        return MetadataUtilities.find(name, initSchemas());
    }

    @Override
    public String toString() {
        return "Catalog[name='" + name + "']"; // NOI18N
    }

    private Map<String, Schema> initSchemas() {
        if (schemas != null) {
            return schemas;
        }
        Map<String, Schema> newSchemas = new LinkedHashMap<String, Schema>();
        try {
            ResultSet rs = metadata.getDmd().getSchemas();
            try {
                while (rs.next()) {
                    String catalogName = rs.getString("TABLE_CATALOG"); // NOI18N
                    String schemaName = rs.getString("TABLE_SCHEM"); // NOI18N
                    if (MetadataUtilities.equals(catalogName, name)) {
                        if (defaultSchemaName != null && MetadataUtilities.equals(schemaName, defaultSchemaName)) {
                            defaultSchema = MetadataFactory.createSchema(new JDBCSchema(this, defaultSchemaName, false));
                            newSchemas.put(defaultSchema.getName(), defaultSchema);
                        } else {
                            newSchemas.put(schemaName, MetadataFactory.createSchema(new JDBCSchema(this, schemaName)));
                        }
                    }
                }
            } finally {
                rs.close();
            }
            if (newSchemas.isEmpty() && !metadata.getDmd().supportsSchemasInTableDefinitions()) {
                defaultSchema = MetadataFactory.createSchema(new JDBCSchema(this, null, true));
                newSchemas.put(defaultSchema.getName(), defaultSchema);
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        schemas = Collections.unmodifiableMap(newSchemas);
        return schemas;
    }

    public JDBCMetadata getMetadata() {
        return metadata;
    }
}
