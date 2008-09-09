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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.spi.CatalogImplementation;
import org.netbeans.modules.db.metadata.model.spi.ColumnImplementation;
import org.netbeans.modules.db.metadata.model.spi.MetadataImplementation;
import org.netbeans.modules.db.metadata.model.spi.SchemaImplementation;
import org.netbeans.modules.db.metadata.model.spi.TableImplementation;
import org.openide.util.Utilities;

/**
 *
 * @author Andrei Badea
 */
public class TestMetadata extends MetadataImplementation {

    private final Map<String, Catalog> catalogs = new TreeMap<String, Catalog>(new NullStringComparator());
    private Catalog defaultCatalog;

    public static Metadata create(List<String> spec) {
        return new TestMetadata(spec).getMetadata();
    }

    public TestMetadata(String[] spec) {
        this(Arrays.asList(spec));
    }

    private TestMetadata(List<String> spec) {
        parse(spec);
        if (defaultCatalog == null) {
            throw new IllegalArgumentException();
        }
        Schema defaultSchema = defaultCatalog.getDefaultSchema();
        if (defaultSchema != null) {
            if (!defaultCatalog.getSchemas().contains(defaultSchema)) {
                Schema syntheticSchema = defaultCatalog.getSyntheticSchema();
                if (syntheticSchema != null && syntheticSchema != defaultSchema) {
                    throw new IllegalArgumentException();
                }
            }
        }
    }

    private void parse(List<String> spec) {
        TestCatalog catalogImpl = null;
        TestSchema schemaImpl = null;
        TestTable tableImpl = null;
        for (String line : spec) {
            int count = 0;
            while (count < line.length() && line.charAt(count) == ' ') {
                count++;
            }
            if (count == line.length()) {
                continue;
            }
            String trimmed = line.trim();
            boolean _default = false;
            if (trimmed.endsWith("*")) {
                trimmed = trimmed.replace("*", "");
                _default = true;
            }
            switch (count) {
                case 0:
                    if (trimmed.equals("<unknown>")) {
                        _default = true;
                        trimmed = null;
                    }
                    catalogImpl = new TestCatalog(trimmed, _default);
                    Catalog catalog = catalogImpl.getCatalog();
                    if (catalogs.get(trimmed) != null) {
                        throw new IllegalArgumentException(line);
                    }
                    catalogs.put(trimmed, catalog);
                    if (_default) {
                        if (defaultCatalog != null) {
                            throw new IllegalArgumentException(line);
                        }
                        defaultCatalog = catalog;
                    }
                    break;
                case 2:
                    boolean synthetic = false;
                    if (trimmed.equals("<no-schema>")) {
                        trimmed = null;
                        _default = true;
                        synthetic = true;
                    }
                    schemaImpl = new TestSchema(catalogImpl, trimmed, _default, synthetic);
                    Schema schema = schemaImpl.getSchema();
                    if (synthetic) {
                        if (catalogImpl.syntheticSchema != null) {
                            throw new IllegalArgumentException(line);
                        }
                        catalogImpl.syntheticSchema = schema;
                    } else {
                        if (catalogImpl.schemas.get(trimmed) != null) {
                            throw new IllegalArgumentException(line);
                        }
                        catalogImpl.schemas.put(trimmed, schema);
                    }
                    if (_default) {
                        if (defaultCatalog != catalogImpl.getCatalog() || catalogImpl.defaultSchema != null) {
                            throw new IllegalArgumentException(line);
                        }
                        catalogImpl.defaultSchema = schema;
                    }
                    break;
                case 4:
                    if (schemaImpl == null) {
                        throw new IllegalArgumentException(line);
                    }
                    tableImpl = new TestTable(schemaImpl, trimmed);
                    schemaImpl.tables.put(trimmed, tableImpl.getTable());
                    break;
                case 6:
                    if (schemaImpl == null || tableImpl == null) {
                        throw new IllegalArgumentException(line);
                    }
                    tableImpl.columns.put(trimmed, new TestColumn(tableImpl, trimmed).getColumn());
                    break;
                default:
                    throw new IllegalArgumentException(line);
            }
        }
    }

    public Catalog getDefaultCatalog() {
        return defaultCatalog;
    }

    public Collection<Catalog> getCatalogs() {
        return catalogs.values();
    }

    public Catalog getCatalog(String name) {
        return catalogs.get(name);
    }

    public void refresh() {
    }

    static final class TestCatalog extends CatalogImplementation {

        private final String name;
        private final boolean _default;
        Map<String, Schema> schemas = new TreeMap<String, Schema>();
        Schema defaultSchema;
        Schema syntheticSchema;

        public TestCatalog(String name, boolean _default) {
            this.name = name;
            this._default = _default;
        }

        public String getName() {
            return name;
        }

        public boolean isDefault() {
            return _default;
        }

        public Schema getDefaultSchema() {
            return defaultSchema;
        }

        public Schema getSyntheticSchema() {
            return syntheticSchema;
        }

        public Collection<Schema> getSchemas() {
            return schemas.values();
        }

        public Schema getSchema(String name) {
            return schemas.get(name);
        }
    }

    static final class TestSchema extends SchemaImplementation {

        private final TestCatalog catalogImpl;
        private final String name;
        private final boolean _default;
        private final boolean synthetic;
        private final Map<String, Table> tables = new TreeMap<String, Table>();

        public TestSchema(TestCatalog catalogImpl, String name, boolean _default, boolean synthetic) {
            this.catalogImpl = catalogImpl;
            this.name = name;
            this._default = _default;
            this.synthetic = synthetic;
        }

        public Catalog getParent() {
            return catalogImpl.getCatalog();
        }

        public String getName() {
            return name;
        }

        public boolean isDefault() {
            return _default;
        }

        public boolean isSynthetic() {
            return synthetic;
        }

        public Collection<Table> getTables() {
            return tables.values();
        }

        public Table getTable(String name) {
            return tables.get(name);
        }
    }

    static final class TestTable extends TableImplementation {

        private final TestSchema schemaImpl;
        private final String name;
        private final Map<String, Column> columns = new LinkedHashMap<String, Column>();

        public TestTable(TestSchema schemaImpl, String name) {
            this.schemaImpl = schemaImpl;
            this.name = name;
        }

        public Schema getParent() {
            return schemaImpl.getSchema();
        }

        public String getName() {
            return name;
        }

        public Collection<Column> getColumns() {
            return columns.values();
        }

        public Column getColumn(String name) {
            return columns.get(name);
        }
    }

    static final class TestColumn extends ColumnImplementation {

        private final TestTable tableImpl;
        private final String name;

        private TestColumn(TestTable tableImpl, String name) {
            this.tableImpl = tableImpl;
            this.name = name;
        }

        public Table getParent() {
            return tableImpl.getTable();
        }

        public String getName() {
            return name;
        }
    }

    private static final class NullStringComparator implements Comparator<String> {

        public int compare(String string1, String string2) {
            if (string1 == null) {
                if (string2 == null) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                if (string2 == null) {
                    return 1;
                } else {
                    return string1.compareTo(string2);
                }
            }
        }
    }
}
