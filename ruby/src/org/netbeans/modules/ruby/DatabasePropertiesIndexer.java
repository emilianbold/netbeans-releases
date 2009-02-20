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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.gsf.api.Index.SearchResult;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;

/**
 * Indexes Rails specific database related properties.
 *
 * @author Tor Norbye, Erno Mononen
 */
final class DatabasePropertiesIndexer {

    private final RubyIndex index;

    private DatabasePropertiesIndexer(RubyIndex index) {
        this.index = index;
    }

    static void indexDatabaseProperties(RubyIndex index, String prefix, NameKind kind, 
            String classFqn, Set<IndexedMethod> methods) {
        DatabasePropertiesIndexer indexer = new DatabasePropertiesIndexer(index);
        indexer.addDatabaseProperties(prefix, kind, classFqn, methods);
    }
    
    private void addDatabaseProperties(String prefix, NameKind kind, String classFqn,
            Set<IndexedMethod> methods) {
        // Query index for database related properties
        if (classFqn.indexOf("::") != -1) {
            // Don't know how to handle this scenario
            return;
        }

        String tableName = RubyUtils.tableize(classFqn);

        String searchField = RubyIndexer.FIELD_DB_TABLE;
        Set<SearchResult> result = new HashSet<SearchResult>();
        index.search(searchField, tableName, NameKind.EXACT_NAME, result);

        List<TableDefinition> tableDefs = new ArrayList<TableDefinition>();
        TableDefinition schema = null;

        for (SearchResult map : result) {
            assert map != null;

            String version = map.getValue(RubyIndexer.FIELD_DB_VERSION);
            assert tableName.equals(map.getValue(RubyIndexer.FIELD_DB_TABLE));
            String fileUrl = map.getPersistentUrl();

            TableDefinition def = new TableDefinition(tableName, version, fileUrl);
            tableDefs.add(def);
            String[] columns = map.getValues(RubyIndexer.FIELD_DB_COLUMN);

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
            Map<String, String> fileUrls = new HashMap<String, String>();
            Set<String> currentCols = new HashSet<String>();
            if (schema != null) {
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
            } else {
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

            // Finally, we've "applied" the migrations - just walk
            // through the datastructure and create completion matches
            // as appropriate
            for (String column : currentCols) {
                if (column.startsWith(prefix)) {
                    if (kind == NameKind.EXACT_NAME) {
                        // Ensure that the method is not longer than the prefix
                        if ((column.length() > prefix.length())) {
                            continue;
                        }
                    } else {
                        // REGEXP, CAMELCASE filtering etc. not supported here
                        assert (kind == NameKind.PREFIX) ||
                                (kind == NameKind.CASE_INSENSITIVE_PREFIX);
                    }

                    String c = columnDefs.get(column);
                    String type = tableName;
                    int semicolonIndex = c.indexOf(';');
                    if (semicolonIndex != -1) {
                        type = c.substring(semicolonIndex + 1);
                    }
                    String fileUrl = fileUrls.get(column);

                    String signature = column;
                    String fqn = tableName + "#" + column;
                    String clz = type;
                    String require = null;
                    String attributes = "";
                    int flags = 0;

                    IndexedMethod method =
                            IndexedMethod.create(index, signature, fqn, clz, fileUrl, require, attributes, flags, index.getContext());
                    method.setMethodType(IndexedMethod.MethodType.DBCOLUMN);
                    method.setSmart(true);
                    methods.add(method);
                }
            }

            if ("find_by_".startsWith(prefix) ||
                    "find_all_by".startsWith(prefix)) {
                // Generate dynamic finders
                for (String column : currentCols) {
                    String methodOneName = "find_by_" + column;
                    String methodAllName = "find_all_by_" + column;
                    if (methodOneName.startsWith(prefix) || methodAllName.startsWith(prefix)) {
                        if (kind == NameKind.EXACT_NAME) {
// XXX methodOneName || methodAllName?
                            // Ensure that the method is not longer than the prefix
                            if ((column.length() > prefix.length())) {
                                continue;
                            }
                        } else {
                            // REGEXP, CAMELCASE filtering etc. not supported here
                            assert (kind == NameKind.PREFIX) ||
                                    (kind == NameKind.CASE_INSENSITIVE_PREFIX);
                        }

                        String type = columnDefs.get(column);
                        type = type.substring(type.indexOf(';') + 1);
                        String fileUrl = fileUrls.get(column);

                        String clz = classFqn;
                        String require = null;
                        int flags = IndexedElement.STATIC;
                        String attributes = IndexedElement.flagToString(flags) + ";;;" + "options(:first|:all),args(=>conditions|order|group|limit|offset|joins|readonly:bool|include|select|from|readonly:bool|lock:bool)";

                        if (methodOneName.startsWith(prefix)) {
                            String signature = methodOneName + "(" + column + ",*options)";
                            String fqn = tableName + "#" + signature;
                            IndexedMethod method =
                                    IndexedMethod.create(index, signature, fqn, clz, fileUrl, require, attributes, flags, index.getContext());
                            method.setInherited(false);
                            method.setSmart(true);
                            methods.add(method);
                        }
                        if (methodAllName.startsWith(prefix)) {
                            String signature = methodAllName + "(" + column + ",*options)";
                            String fqn = tableName + "#" + signature;
                            IndexedMethod method =
                                    IndexedMethod.create(index, signature, fqn, clz, fileUrl, require, attributes, flags, index.getContext());
                            method.setInherited(false);
                            method.setSmart(true);
                            methods.add(method);
                        }
                    }
                }

            }
        }
    }

    private static class TableDefinition implements Comparable<TableDefinition> {

        private String version;
        /** table is redundant, I only search by exact tablenames anyway */
        private String table;
        private String fileUrl;
        private List<String> cols;

        TableDefinition(String table, String version, String fileUrl) {
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

        String getFileUrl() {
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
