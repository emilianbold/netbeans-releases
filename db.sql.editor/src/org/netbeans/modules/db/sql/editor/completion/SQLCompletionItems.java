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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataObject;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.sql.analyzer.QualIdent;
import org.netbeans.spi.editor.completion.CompletionResultSet;

/**
 *
 * @author Andrei Badea
 */
public class SQLCompletionItems implements Iterable<SQLCompletionItem> {

    private final List<SQLCompletionItem> items = new ArrayList<SQLCompletionItem>();
    private final Quoter quoter;
    private final int itemOffset;

    public SQLCompletionItems(Quoter quoter, int itemOffset) {
        this.quoter = quoter;
        this.itemOffset = itemOffset;
    }

    public Set<String> addCatalogs(Metadata metadata, Set<String> restrict, String prefix, final boolean quote, final int substitutionOffset) {
        Set<String> result = new TreeSet<String>();
        filterMetadata(metadata.getCatalogs(), restrict, prefix, new Handler<Catalog>() {
            public void handle(Catalog catalog) {
                String catalogName = catalog.getName();
                items.add(SQLCompletionItem.catalog(catalogName, doQuote(catalogName, quote), itemOffset + substitutionOffset));
            }
        });
        return result;
    }

    public Set<String> addSchemas(Catalog catalog, Set<String> restrict, String prefix, final boolean quote, final int substitutionOffset) {
        Set<String> result = new TreeSet<String>();
        filterMetadata(catalog.getSchemas(), restrict, prefix, new Handler<Schema>() {
            public void handle(Schema schema) {
                if (!schema.isSynthetic()) {
                    String schemaName = schema.getName();
                    items.add(SQLCompletionItem.schema(schemaName, doQuote(schemaName, quote), itemOffset + substitutionOffset));
                }
            }
        });
        return result;
    }

    public void addTables(Schema schema, Set<String> restrict, String prefix, final boolean quote, final int substitutionOffset) {
        filterMetadata(schema.getTables(), restrict, prefix, new Handler<Table>() {
            public void handle(Table table) {
                String tableName = table.getName();
                items.add(SQLCompletionItem.table(tableName, doQuote(tableName, quote), itemOffset + substitutionOffset));
            }
        });
    }

    public void addAliases(Map<String, QualIdent> aliases, String prefix, final boolean quote, final int substitutionOffset) {
        filterMap(aliases, null, prefix, new ParamHandler<String, QualIdent>() {
            public void handle(String alias, QualIdent tableName) {
                // Issue 145173: do not quote aliases.
                items.add(SQLCompletionItem.alias(alias, tableName, alias, itemOffset + substitutionOffset));
            }
        });
    }

    public void addColumns(Table table, String prefix, final boolean quote, final int substitutionOffset) {
        Schema schema = table.getParent();
        Catalog catalog = schema.getParent();
        List<String> parts = new ArrayList<String>(3);
        if (!catalog.isDefault()) {
            parts.add(catalog.getName());
        }
        if (!schema.isSynthetic() && !schema.isDefault()) {
            parts.add(schema.getName());
        }
        parts.add(table.getName());
        final QualIdent qualTableName = new QualIdent(parts);
        filterMetadata(table.getColumns(), null, prefix, new Handler<Column>() {
            public void handle(Column column) {
                String columnName = column.getName();
                items.add(SQLCompletionItem.column(qualTableName, columnName, doQuote(columnName, quote), itemOffset + substitutionOffset));
            }
        });
    }

    public void fill(CompletionResultSet resultSet) {
        resultSet.addAllItems(items);
    }

    public Iterator<SQLCompletionItem> iterator() {
        return items.iterator();
    }

    private String doQuote(String identifier, boolean always) {
        if (always) {
            return quoter.quoteAlways(identifier);
        } else {
            return quoter.quoteIfNeeded(identifier);
        }
    }

    private static boolean startsWithIgnoreCase(String text, String prefix) {
        return text.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private static boolean filter(String string, String prefix) {
        return prefix == null || startsWithIgnoreCase(string, prefix);
    }

    private static <P> void filterMap(Map<String, P> strings, Set<String> restrict, String prefix, ParamHandler<String, P> handler) {
        for (Entry<String, P> entry : strings.entrySet()) {
            String string = entry.getKey();
            if ((restrict == null || restrict.contains(string)) && filter(string, prefix)) {
                handler.handle(string, entry.getValue());
            }
        }
    }

    private static <T extends MetadataObject> void filterMetadata(Collection<T> objects, Set<String> restrict, String prefix, Handler<T> handler) {
        for (T object : objects) {
            String name = object.getName();
            // The name can be null if the object is, for example, a synthetic schema.
            if (name != null && (restrict == null || restrict.contains(name)) && filter(name, prefix)) {
                handler.handle(object);
            }
        }
    }

    private interface Handler<T> {

        void handle(T object);
    }

    private interface ParamHandler<T, P> {

        void handle(T object, P param);
    }
}
