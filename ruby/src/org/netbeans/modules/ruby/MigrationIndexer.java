/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.StrNode;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.ruby.RubyIndexer.TreeAnalyzer;

/**
 * Indexes Rails migrations.
 *
 * <i>Split from RubyIndexer to keep it somehow managable</i>.
 *
 */
final class MigrationIndexer {

    private final ContextKnowledge knowledge;
    private final RubyIndexer.TreeAnalyzer analyzer;

    public MigrationIndexer(ContextKnowledge knowledge, TreeAnalyzer analyzer) {
        this.knowledge = knowledge;
        this.analyzer = analyzer;
    }

    /** Add an include of ActiveRecord::ConnectionAdapters::SchemaStatements from ActiveRecord::Migration.
     * This is a hack. I'm not sure how the SchemaStatements methods end up in a Migration. I've sent
     * some querying e-mails to find out; if you, the reader, know - please let me know so I can track
     * down why the source modeller isn't finding this relationship. */
    void handleMigrations() {
        int flags = 0;
        analyzer.addClassIncludes("Migration", "ActiveRecord::Migration", "ActiveRecord", flags,
                "ActiveRecord::ConnectionAdapters::SchemaStatements");
    }

    /** Handle a migration file */
    void handleMigration() {
        Node root = knowledge.getRoot();

        // Look for self.up methods and register all column deltas
        // create_table: create new table
        //  t.column: add column to the given table
        // add_column, remove_column: add column to the given table
        // rename_table, rename_column: renaming
        // rename_column, change_column - ditto for columns

        // It's going to be hard to deal with something like mephisto's 044_store_single_filter.rb
        // where it's doing conditional logic on the models... schema.rb is more useful here.
        // In the next release, try to use schema.rb if it's known to exist and be up to date

        // Find self.up
        String fileName = analyzer.getFile().getName();
        Node top = null;
        String version;
        if ("schema".equals(fileName)) { // NOI18N
            top = root;
            // Use a special version greater than all allowed migrations such
            // that I can find this when querying
            version = RubyIndexer.SCHEMA_INDEX_VERSION; // NOI18N
        } else {
            String migrationClass;
            if (fileName.charAt(3) == '_') {
                version = fileName.substring(0, 3);
                migrationClass = RubyUtils.underlinedNameToCamel(fileName.substring(4)); // Strip off version prefix
            } else {
                // Rails 2.1 stores it in UTC format
                version = fileName.substring(0, 14);
                migrationClass = RubyUtils.underlinedNameToCamel(fileName.substring(15)); // Strip off version prefix
            }
            String sig = migrationClass + "#up"; // NOI18N
            Node def = AstUtilities.findBySignature(root, sig);

            if (def == null) {
                return;
            }

            top = def;
        }

        // Iterate through the file and find tables and such
        // Map from table name to column names
        Map<String, List<String>> items = new HashMap<String, List<String>>();
        scanMigration(top, items, null);

        if (items.size() > 0) {
            for (Map.Entry<String, List<String>> entry : items.entrySet()) {
                IndexDocument document = analyzer.getSupport().createDocument(analyzer.getIndexable());
                analyzer.getDocuments().add(document);

                String tableName = entry.getKey();
                document.addPair(RubyIndexer.FIELD_DB_TABLE, tableName, true, true);
                document.addPair(RubyIndexer.FIELD_DB_VERSION, version, false, true);

                List<String> columns = entry.getValue();
                for (String column : columns) {
                    document.addPair(RubyIndexer.FIELD_DB_COLUMN, column, false, true);
                }
            }
        }
    }

    private void scanMigration(Node node, Map<String, List<String>> items, String currentTable) {
        if (node.getNodeType() == NodeType.FCALLNODE) {
            // create_table etc.
            String name = AstUtilities.getCallName(node);
            if ("create_table".equals(name)) { // NOI18N
                // Compute the call name, and any column names
                List childNodes = node.childNodes();
                if (childNodes.size() > 0) {
                    Node child = (Node) childNodes.get(0);
                    if (child.getNodeType() == NodeType.ARRAYNODE) {
                        List grandChildren = child.childNodes();
                        if (grandChildren.size() > 0) {
                            Node grandChild = (Node) grandChildren.get(0);
                            if (grandChild.getNodeType() == NodeType.SYMBOLNODE
                                    || grandChild.getNodeType() == NodeType.STRNODE) {
                                String tableName = getString(grandChild);
                                items.put(tableName, new ArrayList<String>());
                                if (childNodes.size() > 1) {
                                    Node n = (Node) childNodes.get(1);
                                    if (n.getNodeType() == NodeType.ITERNODE) {
                                        scanMigration(n, items, tableName);
                                    }
                                }

                                return;
                            }
                        }
                    }
                }
            } else if ("add_column".equals(name) || "remove_column".equals(name)) { // NOI18N
                List<Node> symbols = new ArrayList<Node>();
                // Ugh - this won't work right for complicated migrations which
                // are for example iterating over table like Mephisto's store_single_filter
                // migration
                AstUtilities.addNodesByType(node, new NodeType[]{NodeType.SYMBOLNODE, NodeType.STRNODE}, symbols);
                if (symbols.size() >= 2) {
                    String tableName = getString(symbols.get(0));
                    String columnName = getString(symbols.get(1));
                    String columnType = null;
                    boolean isAdd = "add_column".equals(name); // NOI18N
                    if (isAdd && symbols.size() >= 3) {
                        columnType = getString(symbols.get(2));
                    }

                    List<String> list = items.get(tableName);
                    if (list == null) {
                        list = new ArrayList<String>();
                        items.put(tableName, list);
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(columnName);
                    sb.append(';');
                    if (isAdd) {
                        if (columnType != null) {
                            sb.append(columnType);
                        }
                    } else {
                        sb.append("-");
                    }
                    list.add(sb.toString());
                }

                return;
            } else if ("rename_column".equals(name)) { // NOI18N
                // Simulate as a delete old, add new
                List<Node> symbols = new ArrayList<Node>();
                // Ugh - this won't work right for complicated migrations which
                // are for example iterating over table like Mephisto's store_single_filter
                // migration
                AstUtilities.addNodesByType(node, new NodeType[]{NodeType.SYMBOLNODE, NodeType.STRNODE}, symbols);
                if (symbols.size() >= 3) {
                    String tableName = getString(symbols.get(0));
                    String oldCol = getString(symbols.get(1));
                    String newCol = getString(symbols.get(2));

                    List<String> list = items.get(tableName);
                    if (list == null) {
                        list = new ArrayList<String>();
                        items.put(tableName, list);
                    }
                    list.add(oldCol + ";-"); // NOI18N
                    list.add(newCol);
                }

                return;
            }
        } else if (node.getNodeType() == NodeType.CALLNODE && currentTable != null) {
            // t.column, applying to an outer table
            String name = AstUtilities.getCallName(node);
            if ("column".equals(name)) {  // NOI18N
                List childNodes = node.childNodes();
                if (childNodes.size() >= 2) {
                    Node child = (Node) childNodes.get(0);
                    if (child.getNodeType() != NodeType.DVARNODE) {
                        // Not a call on the block var corresponding to the table
                        // Later, validate more closely that we're making a call
                        // on the actual block variable passed in from the create_table call!
                        return;
                    }

                    child = (Node) childNodes.get(1);
                    List<Node> symbols = new ArrayList<Node>();
                    AstUtilities.addNodesByType(child, new NodeType[]{NodeType.SYMBOLNODE, NodeType.STRNODE}, symbols);
                    if (symbols.size() >= 2) {
                        String columnName = getString(symbols.get(0));
                        String columnType = getString(symbols.get(1));

                        List<String> list = items.get(currentTable);
                        if (list == null) {
                            list = new ArrayList<String>();
                            items.put(currentTable, list);
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append(columnName);
                        sb.append(';');
                        sb.append(columnType);
                        list.add(sb.toString());
                    }

                }

                return;
            } else if ("timestamps".equals(name)) { // NOI18N
                List childNodes = node.childNodes();
                if (childNodes.size() >= 1) {
                    Node child = (Node) childNodes.get(0);
                    if (child.getNodeType() != NodeType.DVARNODE) {
                        // Not a call on the block var corresponding to the table
                        // Later, validate more closely that we're making a call
                        // on the actual block variable passed in from the create_table call!
                        return;
                    }

                    // Insert timestamp codes
                    //    column(:created_at, :datetime)
                    //    column(:updated_at, :datetime)
                    List<String> list = items.get(currentTable);
                    if (list == null) {
                        list = new ArrayList<String>();
                        items.put(currentTable, list);
                    }
                    list.add("created_at;datetime"); // NOI18N
                    list.add("updated_at;datetime"); // NOI18N
                }
            } else {
                // Rails 2.0 shorthand migrations; see http://dev.rubyonrails.org/changeset/6667?new_path=trunk
                int columnTypeIndex = Arrays.binarySearch(RubyIndexer.FIXED_COLUMN_TYPES, name);
                if (columnTypeIndex >= 0 && columnTypeIndex < RubyIndexer.FIXED_COLUMN_TYPES.length) {
                    String columnType = RubyIndexer.FIXED_COLUMN_TYPES[columnTypeIndex];
                    List childNodes = node.childNodes();
                    if (childNodes.size() >= 2) {
                        Node child = (Node) childNodes.get(0);
                        if (child.getNodeType() != NodeType.DVARNODE) {
                            // Not a call on the block var corresponding to the table
                            // Later, validate more closely that we're making a call
                            // on the actual block variable passed in from the create_table call!
                            return;
                        }

                        child = (Node) childNodes.get(1);
                        List<Node> args = child.childNodes();
                        for (Node n : args) {
                            if (n.getNodeType() == NodeType.SYMBOLNODE || n.getNodeType() == NodeType.STRNODE) {
                                String columnName = getString(n);

                                List<String> list = items.get(currentTable);
                                if (list == null) {
                                    list = new ArrayList<String>();
                                    items.put(currentTable, list);
                                }
                                StringBuilder sb = new StringBuilder();
                                sb.append(columnName);
                                sb.append(';');
                                sb.append(columnType);
                                list.add(sb.toString());
                            }
                        }
                    }

                    return;

                }
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            scanMigration(child, items, currentTable);
        }
    }

    private String getString(Node node) {
        if (node.getNodeType() == NodeType.STRNODE) {
            return ((StrNode) node).getValue().toString();
        } else {
            return ((INameNode) node).getName();
        }
    }
}
