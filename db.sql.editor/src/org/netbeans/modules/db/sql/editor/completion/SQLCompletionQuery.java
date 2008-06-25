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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.sql.analyzer.FromTables;
import org.netbeans.modules.db.sql.analyzer.QualIdent;
import org.netbeans.modules.db.sql.analyzer.StatementAnalyzer;
import org.netbeans.modules.db.sql.editor.completion.SQLCompletionEnv.Context;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;

/**
 *
 * @author Andrei Badea
 */
public class SQLCompletionQuery extends AsyncCompletionQuery {

    // XXX quoted identifiers.

    private final MetadataModel model;

    private SQLCompletionEnv env;
    private StatementAnalyzer analyzer;
    private int anchorOffset = -1;
    List<SQLCompletionItem> items;

    public SQLCompletionQuery(MetadataModel model) {
        this.model = model;
    }

    @Override
    protected void query(CompletionResultSet resultSet, final Document doc, final int caretOffset) {
        doQuery(SQLCompletionEnv.create(doc, caretOffset));
        resultSet.addAllItems(items);
        if (anchorOffset != -1) {
            resultSet.setAnchorOffset(anchorOffset);
        }
        resultSet.finish();
    }

    // Called by unit tests.
    void doQuery(SQLCompletionEnv env) {
        this.env = env;
        anchorOffset = -1;
        items = new ArrayList<SQLCompletionItem>();
        if (env != null && env.isSelect()) {
            completeSelect();
        }
    }

    private void completeSelect() {
        Context context = env.getContext();
        if (context == null) {
            return;
        }
        analyzer = new StatementAnalyzer(env.getTokenSequence());
        switch (context) {
            case SELECT:
                insideSelect();
                break;
            case FROM:
                insideFrom();
                break;
        }
    }

    private void insideSelect() {
        Identifier ident = findIdentifier();
        if (ident == null) {
            return;
        }
        anchorOffset = ident.anchorOffset;
        if (ident.fullyTypedIdent.isEmpty()) {
            completeSelectSimpleIdent(ident.lastPrefix);
        } else if (ident.fullyTypedIdent.isSimple()) {
            completeSelectSingleQualIdent(ident.fullyTypedIdent, ident.lastPrefix);
        } else if (ident.fullyTypedIdent.isSingleQualified()) {
            completeSelectDoubleQualIdent(ident.fullyTypedIdent, ident.lastPrefix);
        }
    }

    private void insideFrom() {
        Identifier ident = findIdentifier();
        if (ident == null) {
            return;
        }
        anchorOffset = ident.anchorOffset;
        if (ident.fullyTypedIdent.isEmpty()) {
            completeFromSimpleIdent(ident.lastPrefix);
        } else if (ident.fullyTypedIdent.isSimple()) {
            completeFromSingleQualIdent(ident.fullyTypedIdent, ident.lastPrefix);
        }
    }

    private void completeSelectSimpleIdent(String typedPrefix) {
        FromTables fromTables = analyzer.getFromTables();
        if (fromTables != null) {
            // Columns from tables and aliases.
            Set<QualIdent> tableNames = fromTables.getUnaliasedTableNames();
            Set<QualIdent> allTableNames = new TreeSet<QualIdent>(tableNames);
            Map<String, QualIdent> aliases = fromTables.getAliases();
            for (Entry<String, QualIdent> entry : aliases.entrySet()) {
                allTableNames.add(entry.getValue());
            }
            for (QualIdent tableName : allTableNames) {
                MetadataModelUtilities.addColumnItems(items, model, tableName, typedPrefix, anchorOffset);
            }
            // Tables from default schema.
            String defaultSchemaName = model.getDefaultSchemaName();
            Set<String> simpleTableNames = new HashSet<String>();
            for (QualIdent tableName : tableNames) {
                String simpleTableName = tableName.getSimpleName();
                if (tableName.isSimple()) {
                    simpleTableNames.add(simpleTableName);
                } else if (tableName.isSingleQualified()) {
                    if (defaultSchemaName.equals(tableName.getFirstQualifier())) {
                        simpleTableNames.add(simpleTableName);
                    }
                }
            }
            MetadataModelUtilities.addTableItems(items, model, new QualIdent(defaultSchemaName), simpleTableNames, typedPrefix, anchorOffset);
            // Aliases.
            List<String> sortedAliases = new ArrayList<String>(aliases.keySet());
            Collections.sort(sortedAliases);
            MetadataModelUtilities.addAliasItems(items, sortedAliases, typedPrefix, anchorOffset);
            // Schemas based on qualified tables.
            Set<String> schemaNames = new HashSet<String>();
            for (QualIdent tableName : tableNames) {
                if (!tableName.isSimple()) {
                    schemaNames.add(tableName.getFirstQualifier());
                }
            }
            MetadataModelUtilities.addSchemaItems(items, model, schemaNames, typedPrefix, anchorOffset);
        } else {
            String defaultSchemaName = model.getDefaultSchemaName();
            List<String> defaultSchemaTableNames = model.getTableNames(defaultSchemaName);
            // All columns in default schema, but only if a prefix has been typed, otherwise there
            // would be too many columns.
            if (typedPrefix != null) {
                for (String defaultSchemaTableName : defaultSchemaTableNames) {
                    MetadataModelUtilities.addColumnItems(items, model, new QualIdent(defaultSchemaName, defaultSchemaTableName), typedPrefix, anchorOffset);
                }
            }
            // All tables in default schema.
            MetadataModelUtilities.addTableItems(items, model, new QualIdent(defaultSchemaName), null, typedPrefix, anchorOffset);
            // All schemas.
            MetadataModelUtilities.addSchemaItems(items, model, null, typedPrefix, anchorOffset);
        }
    }

    private void completeSelectSingleQualIdent(QualIdent fullyTypedIdent, String lastPrefix) {
        FromTables fromTables = analyzer.getFromTables();
        // Assume table name.
        QualIdent tableName = fullyTypedIdent;
        if (fromTables != null) {
            // The table name must be a simple name in or qualifed by the default schema.
            String defaultSchemaName = model.getDefaultSchemaName();
            boolean found = false;
            for (QualIdent unaliasedTableName : fromTables.getUnaliasedTableNames()) {
                if (unaliasedTableName.equals(tableName) || unaliasedTableName.equals(new QualIdent(defaultSchemaName, tableName))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                String alias = fullyTypedIdent.getFirstQualifier();
                tableName = fromTables.getTableNameByAlias(alias);
            }
        }
        if (tableName != null) {
            MetadataModelUtilities.addColumnItems(items, model, tableName, lastPrefix, anchorOffset);
        }
        // Now assume schema name.
        QualIdent schemaName = fullyTypedIdent;
        Set<String> tableNames = null;
        if (fromTables != null) {
            tableNames = new HashSet<String>();
            for (QualIdent unaliasedTableName : fromTables.getUnaliasedTableNames()) {
                if (unaliasedTableName.isSingleQualified() && unaliasedTableName.getPrefix().equals(schemaName)) {
                    tableNames.add(unaliasedTableName.getSimpleName());
                }
            }
        }
        MetadataModelUtilities.addTableItems(items, model, schemaName, tableNames, lastPrefix, anchorOffset);
    }

    private void completeSelectDoubleQualIdent(QualIdent fullyTypedIdent, String lastPrefix) {
        FromTables fromTables = analyzer.getFromTables();
        QualIdent tableName = fullyTypedIdent;
        if (fromTables != null) {
            if (!fromTables.unaliasedTableNameExists(tableName)) {
                tableName = null;
            }
        }
        if (tableName != null) {
            MetadataModelUtilities.addColumnItems(items, model, tableName, lastPrefix, anchorOffset);
        }
    }

    private void completeFromSimpleIdent(String typedPrefix) {
        String defaultSchemaName = model.getDefaultSchemaName();
        // All tables in default schema.
        MetadataModelUtilities.addTableItems(items, model, new QualIdent(defaultSchemaName), null, typedPrefix, anchorOffset);
        // All schemas.
        MetadataModelUtilities.addSchemaItems(items, model, null, typedPrefix, anchorOffset);
    }

    private void completeFromSingleQualIdent(QualIdent fullyTypedIdent, String lastPrefix) {
        MetadataModelUtilities.addTableItems(items, model, fullyTypedIdent, null, lastPrefix, anchorOffset);
    }

    private Identifier findIdentifier() {
        TokenSequence<SQLTokenId> seq = env.getTokenSequence();
        int caretOffset = env.getCaretOffset();
        final List<String> parts = new ArrayList<String>();
        int offset = seq.move(caretOffset);
        if (offset > 0) {
            if (seq.moveNext()) {
                switch (seq.token().id()) {
                    case WHITESPACE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                        return Identifier.create(parts, false, caretOffset);
                    case IDENTIFIER:
                        parts.add(seq.token().text().subSequence(0, offset).toString());
                        break;
                }
            } else {
                return Identifier.create(parts, false, caretOffset);
            }
        }
        boolean incomplete = false; // Whether incomplete, like "foo.bar."
        boolean wasDot = false; // Whether the previous token was a dot.
        int identAnchorOffset = -1;
        main: for (;;) {
            if (!seq.movePrevious()) {
                break;
            }
            switch (seq.token().id()) {
                case DOT:
                    if (parts.isEmpty()) {
                        identAnchorOffset = caretOffset; // Not the dot offset,
                        // since the user may have typed whitespace after the dot.
                        incomplete = true;
                    }
                    wasDot = true;
                    break;
                case IDENTIFIER:
                case KEYWORD:
                    if (wasDot || parts.isEmpty()) {
                        if (parts.isEmpty() && identAnchorOffset == -1) {
                            identAnchorOffset = seq.offset();
                        }
                        wasDot = false;
                        parts.add(seq.token().text().toString());
                    } else {
                        break main;
                    }
                    break;
                default:
                    break main;
            }
        }
        Collections.reverse(parts);
        return Identifier.create(parts, incomplete, identAnchorOffset >= 0 ? identAnchorOffset : caretOffset);
    }

    private static final class Identifier {

        final QualIdent fullyTypedIdent;
        final String lastPrefix;
        final int anchorOffset;

        public static Identifier create(List<String> parts, boolean incomplete, int anchorOffset) {
            String lastPrefix = null;
            if (parts.isEmpty()) {
                if (incomplete) {
                    // Just a dot was typed.
                    return null;
                }
                // Fine, nothing was typed.
            } else {
                if (!incomplete) {
                    lastPrefix = parts.remove(parts.size() - 1);
                }
            }
            return new Identifier(new QualIdent(parts), lastPrefix, anchorOffset);
        }

        private Identifier(QualIdent fullyTypedIdent, String lastPrefix, int anchorOffset) {
            this.fullyTypedIdent = fullyTypedIdent;
            this.lastPrefix = lastPrefix;
            this.anchorOffset = anchorOffset;
        }
    }
}
