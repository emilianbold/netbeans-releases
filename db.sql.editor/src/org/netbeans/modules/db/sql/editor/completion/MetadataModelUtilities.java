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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.db.sql.analyzer.QualIdent;

/**
 *
 * @author Andrei Badea
 */
public class MetadataModelUtilities {

    private MetadataModelUtilities() {}

    public static boolean schemaExists(MetadataModel model, String schemaName, String prefix) {
        return model.getSchemaNames().contains(schemaName);
    }

    public static Set<String> addSchemaItems(final List<SQLCompletionItem> items, MetadataModel model, Set<String> restrict, String prefix, final int substitutionOffset) {
        Set<String> result = new TreeSet<String>();
        filter(model.getSchemaNames(), restrict, prefix, new Handler() {
            public void handle(String schemaName) {
                if (!MetadataModel.NO_SCHEMA_NAME.equals(schemaName)) {
                    items.add(SQLCompletionItem.schema(schemaName, substitutionOffset));
                }
            }
        });
        return result;
    }

    public static void addTableItems(final List<SQLCompletionItem> items, MetadataModel model, QualIdent schemaName, Set<String> restrict, String prefix, final int substitutionOffset) {
        if (!schemaName.isSimple()) {
            return;
        }
        filter(model.getTableNames(schemaName.getSimpleName()), restrict, prefix, new Handler() {
            public void handle(String tableName) {
                items.add(SQLCompletionItem.table(tableName, substitutionOffset));
            }
        });
    }

    public static void addAliasItems(final List<SQLCompletionItem> items, List<String> aliases, String prefix, final int substitutionOffset) {
        filter(aliases, null, prefix, new Handler() {
            public void handle(String alias) {
                items.add(SQLCompletionItem.alias(alias, substitutionOffset));
            }
        });
    }

    public static void addColumnItems(final List<SQLCompletionItem> items, MetadataModel model, final QualIdent tableName, String prefix, final int substitutionOffset) {
        final String defaultSchemaName = model.getDefaultSchemaName();
        String schemaName;
        if (tableName.isSimple()) {
            schemaName = defaultSchemaName;
        } else if (tableName.isSingleQualified()) {
            schemaName = tableName.getFirstQualifier();
        } else {
            return;
        }
        final boolean defaultSchema = defaultSchemaName.equals(schemaName);
        final String simpleTableName = tableName.getSimpleName();
        filter(model.getColumnNames(schemaName, tableName.getSimpleName()), null, prefix, new Handler() {
            public void handle(String columnName) {
                if (defaultSchema) {
                    items.add(SQLCompletionItem.column(simpleTableName, columnName, substitutionOffset));
                } else {
                    items.add(SQLCompletionItem.column(tableName, columnName, substitutionOffset));
                }
            }
        });
    }

    private static boolean startsWithIgnoreCase(String text, String prefix) {
        return text.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    public static boolean filter(String string, String prefix) {
        return prefix == null || startsWithIgnoreCase(string, prefix);
    }

    private static void filter(List<String> strings, Set<String> restrict, String prefix, Handler handler) {
        for (String string : strings) {
            if ((restrict == null || restrict.contains(string)) && filter(string, prefix)) {
                handler.handle(string);
            }
        }
    }

    private interface Handler {

        void handle(String string);
    }
}
