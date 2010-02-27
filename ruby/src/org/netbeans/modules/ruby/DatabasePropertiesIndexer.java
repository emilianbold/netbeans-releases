/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.ruby.FindersHelper.FinderMethod;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.openide.filesystems.FileObject;

/**
 * Indexes Rails specific database related properties.
 *
 * @author Tor Norbye, Erno Mononen
 */
final class DatabasePropertiesIndexer {

    private final RubyIndex index;
    private final String prefix;
    private final QuerySupport.Kind kind;
    private final String classFqn;
    private final Set<IndexedMethod> methods;

    private DatabasePropertiesIndexer(RubyIndex index, String prefix, QuerySupport.Kind kind, String classFqn, Set<IndexedMethod> methods) {
        this.index = index;
        this.prefix = prefix;
        this.kind = kind;
        this.classFqn = classFqn;
        this.methods = methods;
    }

    static void indexDatabaseProperties(RubyIndex index, String prefix, QuerySupport.Kind kind,
            String classFqn, Set<IndexedMethod> methods) {
        DatabasePropertiesIndexer indexer = new DatabasePropertiesIndexer(index, prefix, kind, classFqn, methods);
        indexer.addDatabaseProperties();
    }

    private void addDatabaseProperties() {
        // Query index for database related properties
        String tableName = null;
        Collection<? extends IndexResult> classes = index.query(RubyIndexer.FIELD_FQN_NAME, classFqn, QuerySupport.Kind.EXACT);
        for (IndexResult result : classes) {
            tableName = result.getValue(RubyIndexer.FIELD_EXPLICIT_DB_TABLE);
            if (tableName != null) { // just use the first found
                break;
            }
        }
        if (tableName == null) {
            Inflector inflector = Inflector.getDefault();
            tableName = inflector.tableize(inflector.demodulize(classFqn));
        }

        Collection<? extends IndexResult> result = index.query(RubyIndexer.FIELD_DB_TABLE, tableName, QuerySupport.Kind.EXACT);

        List<TableDefinition> tableDefs = new ArrayList<TableDefinition>();
        TableDefinition schema = null;

        for (IndexResult ir : result) {
            assert ir != null;

            String version = ir.getValue(RubyIndexer.FIELD_DB_VERSION);
            assert tableName.equals(ir.getValue(RubyIndexer.FIELD_DB_TABLE));

            TableDefinition def = new TableDefinition(tableName, version, ir.getFile());
            tableDefs.add(def);
            String[] columns = ir.getValues(RubyIndexer.FIELD_DB_COLUMN);

            if (columns != null) {
                for (String column : columns) {
                    // TODO - do this filtering AFTER applying diffs when
                    // I'm doing renaming of columns etc.
                    def.addColumn(column);
                }
            }

            if (RubyIndexer.SCHEMA_INDEX_VERSION.equals(version)) {
                schema = def;
                // With a schema I don't need to look at anything else
                break;
            }
        }

        if (tableDefs.size() > 0) {
            Map<String, String> columnDefs = new HashMap<String, String>();
            Map<String, FileObject> fileUrls = new HashMap<String, FileObject>();
            Set<String> currentCols = new HashSet<String>();
            if (schema != null) {
                addColumnsFromSchema(schema, columnDefs, fileUrls, currentCols);
            } else {
                // Apply migration files
                addColumnsFromMigrations(tableDefs, columnDefs, fileUrls, currentCols);
            }

            // Finally, we've "applied" the migrations - just walk
            // through the datastructure and create completion matches
            // as appropriate
            createMethodsForColumns(tableName, columnDefs, fileUrls, currentCols);

            // dynamic finders
            createDynamicFinders(tableName, columnDefs, fileUrls, currentCols);
        }
    }

    private void addColumnsFromMigrations(List<TableDefinition> tableDefs,
            Map<String, String> columnDefs,
            Map<String, FileObject> fileUrls,
            Set<String> currentCols) {

        // Apply migration files
        Collections.sort(tableDefs);
        for (TableDefinition def : tableDefs) {
            List<String> cols = def.getColumns();
            if (cols == null) {
                continue;
            }

            for (String col : cols) {
                int typeIndex = col.indexOf(';');
                if (typeIndex != -1) {
                    String name = col.substring(0, typeIndex);
                    if (typeIndex < col.length() - 1 && col.charAt(typeIndex + 1) == '-') {
                        // Removing column
                        currentCols.remove(name);
                    } else {
                        currentCols.add(name);
                        fileUrls.put(col, def.getFileUrl());
                        columnDefs.put(name, col);
                    }
                } else {
                    currentCols.add(col);
                    columnDefs.put(col, col);
                    fileUrls.put(col, def.getFileUrl());
                }
            }
        }

    }

    private void addColumnsFromSchema(TableDefinition schema,
            Map<String, String> columnDefs,
            Map<String, FileObject> fileUrls,
            Set<String> currentCols) {

        List<String> cols = schema.getColumns();
        if (cols != null) {
            for (String col : cols) {
                int typeIndex = col.indexOf(';');
                if (typeIndex != -1) {
                    String name = col.substring(0, typeIndex);
                    if (typeIndex < col.length() - 1 && col.charAt(typeIndex + 1) == '-') {
                        // Removing column - this is unlikely in a
                        // schema.rb file!
                        currentCols.remove(col);
                    } else {
                        currentCols.add(name);
                        fileUrls.put(col, schema.getFileUrl());
                        columnDefs.put(name, col);
                    }
                } else {
                    currentCols.add(col);
                    columnDefs.put(col, col);
                    fileUrls.put(col, schema.getFileUrl());
                }
            }
        }

    }

    private void createMethodsForColumns(String tableName,
            Map<String, String> columnDefs,
            Map<String, FileObject> fileUrls,
            Set<String> currentCols) {

        for (String column : currentCols) {
            if (column.startsWith(prefix)) {
                if (kind == QuerySupport.Kind.EXACT) {
                    // Ensure that the method is not longer than the prefix
                    if ((column.length() > prefix.length())) {
                        continue;
                    }
                } else {
                    // REGEXP, CAMELCASE filtering etc. not supported here
                    assert (kind == QuerySupport.Kind.PREFIX) ||
                            (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);
                }

                String c = columnDefs.get(column);
                String type = tableName;
                int semicolonIndex = c.indexOf(';');
                if (semicolonIndex != -1) {
                    type = c.substring(semicolonIndex + 1);
                }
                FileObject fileUrl = fileUrls.get(column);

                String signature = column;
                String fqn = tableName + "#" + column;
                String clz = type;
                String require = null;
                String attributes = "";
                int flags = 0;

                IndexedMethod method =
                        IndexedMethod.create(index, signature, fqn, clz, fileUrl, require, attributes, flags, index.getContext());
                method.setMethodType(IndexedMethod.MethodType.DBCOLUMN);
                method.setType(RailsMigrationTypeMapper.getMappedType(type));
                method.setSmart(true);
                methods.add(method);
            }
        }
    }

    private void createDynamicFinders(String tableName,
            Map<String, String> columnDefs,
            Map<String, FileObject> fileUrls,
            Set<String> currentCols) {

        if (kind == QuerySupport.Kind.EXACT) {
            return;
        }

        List<FinderMethod> finders = new ArrayList<FinderMethod>(FindersHelper.getFinderSignatures(prefix, currentCols));

        for (FindersHelper.FinderMethod finder : finders) {
            String methodName = finder.getName();
            String methodSignature = finder.getSignature();
            if (!methodName.startsWith(prefix) || prefix.length() > methodName.length()) {
                continue;
            }
            // XXX: what's this needed for?
            String column = finder.getColumn();
            FileObject fileUrl = fileUrls.get(column);

            String clz = classFqn;
            String require = null;
            int flags = IndexedElement.STATIC;
            String attributes = IndexedElement.flagToString(flags) + ";;;" + "options(:first|:all),args(=>conditions|order|group|limit|offset|joins|readonly:bool|include|select|from|readonly:bool|lock:bool)";

            String fqn = tableName + "#" + methodSignature;
            IndexedMethod method =
                    IndexedMethod.create(index, methodSignature, fqn, clz, fileUrl, require, attributes, flags, index.getContext());
            method.setMethodType(IndexedMethod.MethodType.DYNAMIC_FINDER);
            method.setInherited(false);
            method.setSmart(true);
            methods.add(method);
        }

    }

    private static class TableDefinition implements Comparable<TableDefinition> {

        private String version;
        /** table is redundant, I only search by exact tablenames anyway */
        private String table;
        private FileObject fileUrl;
        private List<String> cols;

        TableDefinition(String table, String version, FileObject fileUrl) {
            this.table = table;
            this.version = version;
            this.fileUrl = fileUrl;
        }

        public int compareTo(TableDefinition o) {
            // See if we're comparing an old style (3-digit) version number with a new Rails 2.1 UTC version
            if (version.length() != o.version.length()) {
                return version.length() - o.version.length();
            }
            // I can do string comparisons here because the strings
            // are all padded with zeroes on the left (so 100 is going
            // to be greater than 099, which wouldn't be true for "99".)
            return version.compareTo(o.version);
        }

        FileObject getFileUrl() {
            return fileUrl;
        }

        void addColumn(String column) {
            if (cols == null) {
                cols = new ArrayList<String>();
            }

            cols.add(column);
        }

        List<String> getColumns() {
            return cols;
        }
    }
}
