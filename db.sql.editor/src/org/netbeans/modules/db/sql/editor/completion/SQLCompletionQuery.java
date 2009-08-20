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

package org.netbeans.modules.db.sql.editor.completion;

import org.netbeans.modules.db.sql.analyzer.SQLStatementAnalyzer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.sql.support.SQLIdentifiers;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.api.metadata.DBConnMetadataModelManager;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.sql.analyzer.DeleteStatement;
import org.netbeans.modules.db.sql.analyzer.TablesClause;
import org.netbeans.modules.db.sql.analyzer.InsertStatement;
import org.netbeans.modules.db.sql.analyzer.QualIdent;
import org.netbeans.modules.db.sql.analyzer.SQLStatement;
import org.netbeans.modules.db.sql.analyzer.SQLStatement.Context;
import org.netbeans.modules.db.sql.analyzer.SelectStatement;
import org.netbeans.modules.db.sql.analyzer.SQLStatementKind;
import org.netbeans.modules.db.sql.analyzer.UpdateStatement;
import org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionResultSet;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class SQLCompletionQuery extends AsyncCompletionQuery {

    private static final Logger LOGGER = Logger.getLogger(SQLCompletionQuery.class.getName());

    // XXX quoted identifiers.

    private final DatabaseConnection dbconn;

    private Metadata metadata;
    private SQLCompletionEnv env;
    private Quoter quoter;
    private SQLStatement statement;
    /** All tables available for completion in current offset. */
    private TablesClause tablesClause;
    private int anchorOffset = -1; // Relative to statement offset.
    private int substitutionOffset = 0; // Relative to statement offset.
    private SQLCompletionItems items;
    /** Context in SQL statement. */
    private Context context;
    /** Recognized identifier (also incomplete) in SQL statement. */
    private Identifier ident;

    public SQLCompletionQuery(DatabaseConnection dbconn) {
        this.dbconn = dbconn;
    }

    @Override
    protected void query(CompletionResultSet resultSet, final Document doc, final int caretOffset) {
        doQuery(SQLCompletionEnv.forDocument(doc, caretOffset));
        if (items != null) {
            items.fill(resultSet);
        }
        if (anchorOffset != -1) {
            resultSet.setAnchorOffset(env.getStatementOffset() + anchorOffset);
        }
        resultSet.finish();
    }

    public void query(SQLCompletionResultSet resultSet, SQLCompletionEnv newEnv) {
        doQuery(newEnv);
        if (items != null) {
            items.fill(resultSet);
        }
        if (anchorOffset != -1) {
            resultSet.setAnchorOffset(newEnv.getStatementOffset() + anchorOffset);
        }
    }

    private void doQuery(final SQLCompletionEnv newEnv) {
        try {
            DBConnMetadataModelManager.get(dbconn).runReadAction(new Action<Metadata>() {
                public void run(Metadata metadata) {
                    Connection conn = dbconn.getJDBCConnection();
                    if (conn == null) {
                        return;
                    }
                    Quoter quoter = null;
                    try {
                        DatabaseMetaData dmd = conn.getMetaData();
                        quoter = SQLIdentifiers.createQuoter(dmd);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    doQuery(newEnv, metadata, quoter);
                }
            });
        } catch (MetadataModelException e) {
            reportError(e);
        }
    }

    // Called by unit tests.
    SQLCompletionItems doQuery(SQLCompletionEnv env, Metadata metadata, Quoter quoter) {
        this.env = env;
        this.metadata = metadata;
        this.quoter = quoter;
        anchorOffset = -1;
        substitutionOffset = 0;
        items = new SQLCompletionItems(quoter, env.getSubstitutionHandler());
        if (env.getTokenSequence().isEmpty()) {
            completeKeyword("SELECT", "INSERT", "DELETE", "DROP", "UPDATE");  //NOI18N
            return items;
        }
        statement = SQLStatementAnalyzer.analyze(env.getTokenSequence(), quoter);
        if (statement == null) {
            completeKeyword("SELECT", "INSERT", "DELETE", "DROP", "UPDATE");  //NOI18N
            return items;
        }
        context = statement.getContextAtOffset(env.getCaretOffset());
        if (context == null) {
            completeKeyword("SELECT", "INSERT", "DELETE", "DROP", "UPDATE");  //NOI18N
            return items;
        }
        ident = findIdentifier();
        if (ident == null) {
            completeKeyword(context);
            return items;
        }
        anchorOffset = ident.anchorOffset;
        substitutionOffset = ident.substitutionOffset;
        SQLStatementKind kind = statement.getKind();
        switch (kind) {
            case SELECT:
                completeSelect();
                break;
            case INSERT:
                completeInsert();
                break;
            case DROP:
                completeDrop();
                break;
            case UPDATE:
                completeUpdate();
                break;
            case DELETE:
                completeDelete();
                break;
        }
        return items;
    }

    private void completeSelect() {
        SelectStatement selectStatement = (SelectStatement) statement;
        tablesClause = selectStatement.getTablesInEffect(env.getCaretOffset());
        switch (context) {
            case SELECT:
                completeColumn(ident);
                break;
            case FROM:
                completeTable(ident);
                break;
            case JOIN_CONDITION:
                completeColumnWithDefinedTable(ident);
                break;
            case WHERE:
                if (tablesClause != null) {
                    completeColumnWithDefinedTable(ident);
                } else {
                    completeColumn(ident);
                }
                break;
            case ORDER:
            case GROUP:
                completeKeyword(context);
                break;
            default:
                if (tablesClause != null) {
                    completeColumnWithDefinedTable(ident);
                }
        }
    }

    private void completeInsert () {
        InsertStatement insertStatement = (InsertStatement) statement;
        switch (context) {
            case INSERT:
                completeKeyword(context);
                break;
            case INSERT_INTO:
                completeTable(ident);
                break;
            case COLUMNS:
                insideColumns (ident, resolveTable(insertStatement.getTable ()));
                break;
            case VALUES:
                break;
        }
    }

    private void completeDrop() {
        switch (context) {
            case DROP:
                completeKeyword(context);
                break;
            case DROP_TABLE:
                completeTable(ident);
                break;
            default:
        }
    }

    private void completeUpdate() {
        UpdateStatement updateStatement = (UpdateStatement) statement;
        tablesClause = updateStatement.getTablesInEffect(env.getCaretOffset());
        switch (context) {
            case UPDATE:
                completeTable(ident);
                break;
            case JOIN_CONDITION:
                completeColumnWithDefinedTable(ident);
                break;
            case SET:
                completeColumn(ident);
                break;
            default:
                if (tablesClause != null) {
                    completeColumnWithDefinedTable(ident);
                }
        }
    }

    private void completeDelete() {
        DeleteStatement deleteStatement = (DeleteStatement) statement;
        tablesClause = deleteStatement.getTablesInEffect(env.getCaretOffset());
        switch (context) {
            case DELETE:
                completeKeyword(context);
                completeTable(ident);
                break;
            case FROM:
                completeTable(ident);
                break;
            case JOIN_CONDITION:
                completeColumnWithDefinedTable(ident);
                break;
            case WHERE:
                if (tablesClause != null) {
                    completeColumnWithDefinedTable(ident);
                } else {
                    completeColumn(ident);
                }
                break;
            default:
                if (tablesClause != null) {
                    completeColumnWithDefinedTable(ident);
                }
        }
    }

    /** Adds keyword/s according to typed prefix and given context. */
    private void completeKeyword(Context context) {
        switch (context) {
            case SELECT:
                completeKeyword("FROM");  //NOI18N
                break;
            case DELETE:
                completeKeyword("FROM");  //NOI18N
                break;
            case INSERT:
                completeKeyword("INTO");  //NOI18N
                break;
            case INSERT_INTO:
            case COLUMNS:
                completeKeyword("VALUES");  //NOI18N
                break;
            case FROM:
                completeKeyword("WHERE");  //NOI18N
                // with join keywors
                //completeKeyword("WHERE", "INNER", "OUTER", "LEFT", "JOIN", "ON");  //NOI18N
                break;
            case UPDATE:
                completeKeyword("SET");  //NOI18N
                // with join keywors
                //completeKeyword("WHERE", "INNER", "OUTER", "LEFT", "JOIN", "ON");  //NOI18N
                break;
            case JOIN_CONDITION:
                completeKeyword("WHERE");  //NOI18N
                break;
            case SET:
                completeKeyword("WHERE");  //NOI18N
                break;
            case WHERE:
                completeKeyword("GROUP", "ORDER");  //NOI18N
                break;
            case ORDER:
            case GROUP:
                completeKeyword("BY");  //NOI18N
                break;
            case GROUP_BY:
                completeKeyword("HAVING");  //NOI18N
                break;
            case DROP:
                completeKeyword("TABLE");  //NOI18N
                break;
            case DROP_TABLE:
            case HAVING:
            case ORDER_BY:
            case VALUES:
                // nothing to complete
                break;
        }
    }

    /** Adds listed keyword/s according to typed prefix. */
    private void completeKeyword(String... keywords) {
        Arrays.sort(keywords);
        Symbol prefix = findPrefix();
        substitutionOffset = prefix.substitutionOffset;
        anchorOffset = substitutionOffset;
        items.addKeywords(prefix.lastPrefix, substitutionOffset, keywords);
    }

    /** Adds columns, tables, schemas and catalogs according to given identifier. */
    private void completeColumn(Identifier ident) {
        if (ident.fullyTypedIdent.isEmpty()) {
            completeColumnSimpleIdent(ident.lastPrefix, ident.quoted);
        } else {
            completeColumnQualIdent(ident.fullyTypedIdent, ident.lastPrefix, ident.quoted);
        }
    }

    private void insideColumns (Identifier ident, Table table) {
        if (ident.fullyTypedIdent.isEmpty()) {
            if (table == null) {
                completeColumnWithTableIfSimpleIdent (ident.lastPrefix, ident.quoted);
            } else {
                items.addColumns (table, ident.lastPrefix, ident.quoted, substitutionOffset);
            }
        } else {
            if (table == null) {
                completeColumnWithTableIfQualIdent (ident.fullyTypedIdent, ident.lastPrefix, ident.quoted);
            } else {
                items.addColumns (table, ident.lastPrefix, ident.quoted, substitutionOffset);
            }
        }
    }

    /** Adds tables, schemas and catalogs according to given identifier. */
    private void completeTable(Identifier ident) {
        if (ident.fullyTypedIdent.isEmpty()) {
            completeTableSimpleIdent(ident.lastPrefix, ident.quoted);
        } else if (ident.fullyTypedIdent.isSimple()) {
            completeTableQualIdent(ident.fullyTypedIdent, ident.lastPrefix, ident.quoted);
        }
    }

    /** Adds columns, tables, schemas and catalogs according to given identifier
     * but only for tables already defined in statement. */
    private void completeColumnWithDefinedTable(Identifier ident) {
        if (ident.fullyTypedIdent.isEmpty()) {
            completeSimpleIdentBasedOnFromClause(ident.lastPrefix, ident.quoted);
        } else {
            completeQualIdentBasedOnFromClause(ident.fullyTypedIdent, ident.lastPrefix, ident.quoted);
        }
    }

    /** Adds columns, tables, schemas and catalogs according to given identifier. */
    private void completeColumnSimpleIdent(String typedPrefix, boolean quoted) {
        if (tablesClause != null) {
            completeSimpleIdentBasedOnFromClause(typedPrefix, quoted);
        } else {
            Schema defaultSchema = metadata.getDefaultSchema();
            if (defaultSchema != null) {
                // All columns in default schema, but only if a prefix has been typed, otherwise there
                // would be too many columns.
                if (typedPrefix != null) {
                    for (Table table : defaultSchema.getTables()) {
                        items.addColumns(table, typedPrefix, quoted, substitutionOffset);
                    }
                }
                // All tables in default schema.
                items.addTables(defaultSchema, null, typedPrefix, quoted, substitutionOffset);
            }
            // All schemas.
            Catalog defaultCatalog = metadata.getDefaultCatalog();
            items.addSchemas(defaultCatalog, null, typedPrefix, quoted, substitutionOffset);
            // All catalogs.
            items.addCatalogs(metadata, null, typedPrefix, quoted, substitutionOffset);
        }
    }

    private void completeColumnWithTableIfSimpleIdent(String typedPrefix, boolean quoted) {
        Schema defaultSchema = metadata.getDefaultSchema();
        if (defaultSchema != null) {
            // All columns in default schema, but only if a prefix has been typed, otherwise there
            // would be too many columns.
            if (typedPrefix != null) {
                for (Table table : defaultSchema.getTables()) {
                    items.addColumnsWithTableName (table, null, typedPrefix, quoted, substitutionOffset - 1);
                }
            } else {
                // All tables in default schema.
                items.addTablesAtInsertInto (defaultSchema, null, null, typedPrefix, quoted, substitutionOffset - 1);
            }
        }
        // All schemas.
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        items.addSchemas(defaultCatalog, null, typedPrefix, quoted, substitutionOffset);
        // All catalogs.
        items.addCatalogs(metadata, null, typedPrefix, quoted, substitutionOffset);
    }

    private void completeColumnWithTableIfQualIdent(QualIdent fullyTypedIdent, String lastPrefix, boolean quoted) {
            // Assume fullyTypedIdent is a table.
            Table table = resolveTable(fullyTypedIdent);
            if (table != null) {
                items.addColumnsWithTableName (table, fullyTypedIdent, lastPrefix, quoted,
                        substitutionOffset - 1);
            }
            // Assume fullyTypedIdent is a schema.
            Schema schema = resolveSchema(fullyTypedIdent);
            if (schema != null) {
                items.addTablesAtInsertInto (schema, fullyTypedIdent, null, lastPrefix, quoted,
                        substitutionOffset - 1);
            }
            // Assume fullyTypedIdent is a catalog.
            Catalog catalog = resolveCatalog(fullyTypedIdent);
            if (catalog != null) {
                completeCatalog(catalog, lastPrefix, quoted);
            }
    }

    /** Adds columns, tables, schemas and catalogs according to given identifier. */
    private void completeColumnQualIdent(QualIdent fullyTypedIdent, String lastPrefix, boolean quoted) {
        if (tablesClause != null) {
            completeQualIdentBasedOnFromClause(fullyTypedIdent, lastPrefix, quoted);
        } else {
            // Assume fullyTypedIdent is a table.
            Table table = resolveTable(fullyTypedIdent);
            if (table != null) {
                items.addColumns(table, lastPrefix, quoted, substitutionOffset);
            }
            // Assume fullyTypedIdent is a schema.
            Schema schema = resolveSchema(fullyTypedIdent);
            if (schema != null) {
                items.addTables(schema, null, lastPrefix, quoted, substitutionOffset);
            }
            // Assume fullyTypedIdent is a catalog.
            Catalog catalog = resolveCatalog(fullyTypedIdent);
            if (catalog != null) {
                completeCatalog(catalog, lastPrefix, quoted);
            }
        }
    }

    /** Adds all tables from default schema, all schemas from defaultcatalog
     * and all catalogs. */
    private void completeTableSimpleIdent(String typedPrefix, boolean quoted) {
        Schema defaultSchema = metadata.getDefaultSchema();
        if (defaultSchema != null) {
            // All tables in default schema.
            items.addTables(defaultSchema, null, typedPrefix, quoted, substitutionOffset);
        }
        // All schemas.
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        items.addSchemas(defaultCatalog, null, typedPrefix, quoted, substitutionOffset);
        // All catalogs.
        items.addCatalogs(metadata, null, typedPrefix, quoted, substitutionOffset);
    }

    /** Adds all tables in schema get from fully qualified identifier or all
     * schemas from catalog. */
    private void completeTableQualIdent(QualIdent fullyTypedIdent, String lastPrefix, boolean quoted) {
        Schema schema = resolveSchema(fullyTypedIdent);
        if (schema != null) {
            // Tables in the typed schema.
            items.addTables(schema, null, lastPrefix, quoted, substitutionOffset);
        }
        Catalog catalog = resolveCatalog(fullyTypedIdent);
        if (catalog != null) {
            // Items in the typed catalog.
            completeCatalog(catalog, lastPrefix, quoted);
        }
    }

    private void completeSimpleIdentBasedOnFromClause(String typedPrefix, boolean quoted) {
        assert tablesClause != null;
        Set<QualIdent> tableNames = tablesClause.getUnaliasedTableNames();
        Set<Table> tables = resolveTables(tableNames);
        Set<QualIdent> allTableNames = new TreeSet<QualIdent>(tableNames);
        Set<Table> allTables = new LinkedHashSet<Table>(tables);
        Map<String, QualIdent> aliases = tablesClause.getAliasedTableNames();
        for (Entry<String, QualIdent> entry : aliases.entrySet()) {
            QualIdent tableName = entry.getValue();
            allTableNames.add(tableName);
            Table table = resolveTable(tableName);
            if (table != null) {
                allTables.add(table);
            }
        }
        // Aliases.
        Map<String, QualIdent> sortedAliases = new TreeMap<String, QualIdent>(aliases);
        items.addAliases(sortedAliases, typedPrefix, quoted, substitutionOffset);
        // Columns from aliased and non-aliased tables in the FROM clause.
        for (Table table : allTables) {
            items.addColumns(table, typedPrefix, quoted, substitutionOffset);
        }
        // Tables from default schema, restricted to non-aliased table names in the FROM clause.
        Schema defaultSchema = metadata.getDefaultSchema();
        if (defaultSchema != null) {
            Set<String> simpleTableNames = new HashSet<String>();
            for (Table table : tables) {
                if (table.getParent().isDefault()) {
                    simpleTableNames.add(table.getName());
                }
            }
            items.addTables(defaultSchema, simpleTableNames, typedPrefix, quoted, substitutionOffset);
        }
        // Schemas from default catalog other than the default schema, based on non-aliased table names in the FROM clause.
        // Catalogs based on non-aliased tables names in the FROM clause.
        Set<String> schemaNames = new HashSet<String>();
        Set<String> catalogNames = new HashSet<String>();
        for (Table table : tables) {
            Schema schema = table.getParent();
            Catalog catalog = schema.getParent();
            if (!schema.isDefault() && !schema.isSynthetic() && catalog.isDefault()) {
                schemaNames.add(schema.getName());
            }
            if (!catalog.isDefault()) {
                catalogNames.add(catalog.getName());
            }

        }
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        items.addSchemas(defaultCatalog, schemaNames, typedPrefix, quoted, substitutionOffset);
        items.addCatalogs(metadata, catalogNames, typedPrefix, quoted, substitutionOffset);
    }

    private void completeQualIdentBasedOnFromClause(QualIdent fullyTypedIdent, String lastPrefix, boolean quoted) {
        assert tablesClause != null;
        Set<Table> tables = resolveTables(tablesClause.getUnaliasedTableNames());
        // Assume fullyTypedIdent is the name of a table in the default schema.
        Table foundTable = resolveTable(fullyTypedIdent);
        if (foundTable == null || !tables.contains(foundTable)) {
            // Table not found, or it is not in the FROM clause.
            foundTable = null;
            // Then assume fullyTypedIdent is an alias.
            if (fullyTypedIdent.isSimple()) {
                QualIdent aliasedTableName = tablesClause.getTableNameByAlias(fullyTypedIdent.getSimpleName());
                if (aliasedTableName != null) {
                    foundTable = resolveTable(aliasedTableName);
                }
            }
        }
        if (foundTable != null) {
            items.addColumns(foundTable, lastPrefix, quoted, substitutionOffset);
        }
        // Now assume fullyTypedIdent is the name of a schema in the default catalog.
        Schema schema = resolveSchema(fullyTypedIdent);
        if (schema != null) {
            Set<String> tableNames = new HashSet<String>();
            for (Table table : tables) {
                if (table.getParent().equals(schema)) {
                    tableNames.add(table.getName());
                }
            }
            items.addTables(schema, tableNames, lastPrefix, quoted, substitutionOffset);
        }
        // Now assume fullyTypedIdent is the name of a catalog.
        Catalog catalog = resolveCatalog(fullyTypedIdent);
        if (catalog != null) {
            Set<String> syntheticSchemaTableNames = new HashSet<String>();
            Set<String> schemaNames = new HashSet<String>();
            for (Table table : tables) {
                schema = table.getParent();
                if (schema.getParent().equals(catalog)) {
                    if (!schema.isSynthetic()) {
                        schemaNames.add(schema.getName());
                    } else {
                        syntheticSchemaTableNames.add(table.getName());
                    }
                }
            }
            items.addSchemas(catalog, schemaNames, lastPrefix, quoted, substitutionOffset);
            items.addTables(catalog.getSyntheticSchema(), syntheticSchemaTableNames, lastPrefix, quoted, substitutionOffset);
        }
    }

    private void completeCatalog(Catalog catalog, String prefix, boolean quoted) {
        items.addSchemas(catalog, null, prefix, quoted, substitutionOffset);
        Schema syntheticSchema = catalog.getSyntheticSchema();
        if (syntheticSchema != null) {
            items.addTables(syntheticSchema, null, prefix, quoted, substitutionOffset);
        }
    }

    private Catalog resolveCatalog(QualIdent catalogName) {
        if (catalogName.isSimple()) {
            return metadata.getCatalog(catalogName.getSimpleName());
        }
        return null;
    }

    private Schema resolveSchema(QualIdent schemaName) {
        Schema schema = null;
        switch (schemaName.size()) {
            case 1:
                Catalog catalog = metadata.getDefaultCatalog();
                schema = catalog.getSchema(schemaName.getSimpleName());
                break;
            case 2:
                catalog = metadata.getCatalog(schemaName.getFirstQualifier());
                if (catalog != null) {
                    schema = catalog.getSchema(schemaName.getSimpleName());
                }
                break;
        }
        return schema;
    }

    private Table resolveTable(QualIdent tableName) {
        Table table = null;
        if (tableName == null) {
            return table;
        }
        switch (tableName.size()) {
            case 1:
                Schema schema = metadata.getDefaultSchema();
                if (schema != null) {
                    return schema.getTable(tableName.getSimpleName());
                }
                break;
            case 2:
                Catalog catalog = metadata.getDefaultCatalog();
                schema = catalog.getSchema(tableName.getFirstQualifier());
                if (schema != null) {
                    table = schema.getTable(tableName.getSimpleName());
                }
                if (table == null) {
                    catalog = metadata.getCatalog(tableName.getFirstQualifier());
                    if (catalog != null) {
                        schema = catalog.getSyntheticSchema();
                        if (schema != null) {
                            table = schema.getTable(tableName.getSimpleName());
                        }
                    }
                }
                break;
            case 3:
                catalog = metadata.getCatalog(tableName.getFirstQualifier());
                if (catalog != null) {
                    schema = catalog.getSchema(tableName.getSecondQualifier());
                    if (schema != null) {
                        table = schema.getTable(tableName.getSimpleName());
                    }
                }
                break;
        }
        return table;
    }

    private Set<Table> resolveTables(Set<QualIdent> tableNames) {
        Set<Table> result = new LinkedHashSet<Table>(tableNames.size());
        for (QualIdent tableName : tableNames) {
            Table table = resolveTable(tableName);
            if (table != null) {
                result.add(table);
            }
        }
        return result;
    }

    /** Returns part of token before cursor or entire token if at the end of it.
     * Returns null prefix if token is comma or whitespace. Returned offset is
     * caret offset if prefix is null, otherwise it is token offset. */
    private Symbol findPrefix() {
        TokenSequence<SQLTokenId> seq = env.getTokenSequence();
        int caretOffset = env.getCaretOffset();
        String prefix = null;
        if (seq.move(caretOffset) > 0) {
            // Not on token boundary.
            if (!seq.moveNext() && !seq.movePrevious()) {
                return new Symbol(null, caretOffset, caretOffset);
            }
        } else {
            if (!seq.movePrevious()) {
                return new Symbol(null, caretOffset, caretOffset);
            }
        }
        switch (seq.token().id()) {
            case WHITESPACE:
            case COMMA:
                return new Symbol(null, caretOffset, caretOffset);
            default:
                int offset = caretOffset - seq.offset();
                if (offset > 0 && offset < seq.token().length()) {
                    prefix = seq.token().text().subSequence(0, offset).toString();
                } else {
                    prefix = seq.token().text().toString();
                }
                return new Symbol(prefix, seq.offset(), seq.offset());
        }
    }

    /** Finds valid identifier within SQL statement at cursor position.
     * It handles fully qualified and quoted identifiers. Returns null if no
     * valid identifier found. */
    private Identifier findIdentifier() {
        TokenSequence<SQLTokenId> seq = env.getTokenSequence();
        int caretOffset = env.getCaretOffset();
        final List<String> parts = new ArrayList<String>();
        if (seq.move(caretOffset) > 0) {
            // Not on token boundary.
            if (!seq.moveNext() && !seq.movePrevious()) {
                return null;
            }
        } else {
            if (!seq.movePrevious()) {
                return null;
            }
        }
        switch (seq.token().id()) {
            case LINE_COMMENT:
            case BLOCK_COMMENT:
            case INT_LITERAL:
            case DOUBLE_LITERAL:
            case STRING:
            case INCOMPLETE_STRING:
                return null;
        }
        boolean incomplete = false; // Whether incomplete, like '"foo.bar."|'.
        boolean wasDot = false; // Whether the previous token was a dot.
        int lastPrefixOffset = -1;
        main: do {
            switch (seq.token().id()) {
                case DOT:
                    if (parts.isEmpty()) {
                        lastPrefixOffset = caretOffset; // Not the dot offset,
                        // since the user may have typed whitespace after the dot.
                        incomplete = true;
                    }
                    wasDot = true;
                    break;
                case IDENTIFIER:
                case KEYWORD:
                    if (wasDot || parts.isEmpty()) {
                        if (parts.isEmpty() && lastPrefixOffset == -1) {
                            lastPrefixOffset = seq.offset();
                        }
                        wasDot = false;
                        String part;
                        int offset = caretOffset - seq.offset();
                        if (offset > 0 && offset < seq.token().length()) {
                            part = seq.token().text().subSequence(0, offset).toString();
                        } else {
                            part = seq.token().text().toString();
                        }
                        parts.add(part);
                    } else {
                        // Two following identifiers.
                        return null;
                    }
                    break;
                case WHITESPACE:
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                    if (seq.movePrevious()) {
                        switch (seq.token().id()) {
                            case IDENTIFIER:  // Cannot complete 'SELECT foo |'.
                            case INT_LITERAL:  // Cannot complete 'WHERE a = 1 |'.
                            case DOUBLE_LITERAL:
                            case STRING:
                            case INCOMPLETE_STRING:
                            case RPAREN:  // foo is not valid identifier in 'WHERE (a+b > c) foo'
                                return null;
                            case OPERATOR:  // foo is not valid identifier in 'SELECT * foo'
                                if (seq.token().text().toString().equals("*")) {  //NOI18N
                                    if (seq.movePrevious()) {
                                        if (seq.movePrevious()) {
                                            if (seq.token().text().toString().equalsIgnoreCase("SELECT")) {  //NOI18N
                                                return null;
                                            }
                                            seq.moveNext();
                                        }
                                        seq.moveNext();
                                    }
                                }
                                break;
                            case DOT:
                                // Process the dot in the main loop.
                                seq.moveNext();
                                continue main;
                        }
                    }
                    break main;
                default:
                    break main;
            }
        } while (seq.movePrevious());
        Collections.reverse(parts);
        return createIdentifier(parts, incomplete, lastPrefixOffset >= 0 ? lastPrefixOffset : caretOffset);
    }

    /**
     * @param lastPrefixOffset the offset of the last prefix in the identifier, or
     *        if no such prefix, the caret offset.
     * @return
     */
    private Identifier createIdentifier(List<String> parts, boolean incomplete, int lastPrefixOffset) {
        String lastPrefix = null;
        boolean quoted = false;
        int substOffset = lastPrefixOffset;
        if (parts.isEmpty()) {
            if (incomplete) {
                // Just a dot was typed.
                return null;
            }
            // Fine, nothing was typed.
        } else {
            if (!incomplete) {
                lastPrefix = parts.remove(parts.size() - 1);
                String quoteString = quoter.getQuoteString();
                if (lastPrefix.startsWith(quoteString)) {
                    if (lastPrefix.endsWith(quoteString) && lastPrefix.length() > quoteString.length()) {
                        // User typed '"foo"."bar"|', can't complete that.
                        return null;
                    }
                    int lastPrefixLength = lastPrefix.length();
                    lastPrefix = quoter.unquote(lastPrefix);
                    lastPrefixOffset = lastPrefixOffset + (lastPrefixLength - lastPrefix.length());
                    quoted = true;
                } else if (lastPrefix.endsWith(quoteString)) {
                    // User typed '"foo".bar"|', can't complete.
                    return null;
                }
            }
            for (int i = 0; i < parts.size(); i++) {
                String unquoted = quoter.unquote(parts.get(i));
                if (unquoted.length() == 0) {
                    // User typed something like '"foo".""."bar|'.
                    return null;
                }
                parts.set(i, unquoted);
            }
        }
        return new Identifier(new QualIdent(parts), lastPrefix, quoted, lastPrefixOffset, substOffset);
    }

    private static void reportError(MetadataModelException e) {
        LOGGER.log(Level.INFO, null, e);
        String error = e.getMessage();
        String message;
        if (error != null) {
            message = NbBundle.getMessage(SQLCompletionQuery.class, "MSG_Error", error);
        } else {
            message = NbBundle.getMessage(SQLCompletionQuery.class, "MSG_ErrorNoMessage");
        }
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
    }

    private static class Symbol {

        final String lastPrefix;
        final int anchorOffset;
        final int substitutionOffset;

        private Symbol(String lastPrefix, int anchorOffset, int substitutionOffset) {
            this.lastPrefix = lastPrefix;
            this.anchorOffset = anchorOffset;
            this.substitutionOffset = substitutionOffset;
        }
    }

    private static final class Identifier extends Symbol {

        final QualIdent fullyTypedIdent;
        final boolean quoted;

        private Identifier(QualIdent fullyTypedIdent, String lastPrefix, boolean quoted, int anchorOffset, int substitutionOffset) {
            super(lastPrefix, anchorOffset, substitutionOffset);
            this.fullyTypedIdent = fullyTypedIdent;
            this.quoted = quoted;
        }
    }
}
