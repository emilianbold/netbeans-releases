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
package org.netbeans.modules.db.explorer.action;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.netbeans.api.db.sql.support.SQLIdentifiers;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.node.ColumnNode;
import org.netbeans.modules.db.explorer.node.ColumnNameProvider;
import org.netbeans.modules.db.explorer.node.SchemaNameProvider;
import org.netbeans.modules.db.explorer.node.TableNode;
import org.netbeans.modules.db.explorer.node.ViewNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Rob Englander
 */
public abstract class QueryAction extends BaseAction {

    protected boolean enable(Node[] activatedNodes) {
        boolean result = false;

        // either 1 table or view node, or 1 more more column nodes
        if (activatedNodes.length == 1) {
            Lookup lookup = activatedNodes[0].getLookup();
            result = lookup.lookup(TableNode.class) != null ||
                    lookup.lookup(ViewNode.class) != null ||
                    lookup.lookup(ColumnNode.class) != null;
        } else {
            result = true;
            for (Node node : activatedNodes) {
                if (node.getLookup().lookup(ColumnNode.class) == null) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Used by the {@code getQualifiedTableName} method to append a catalog or a
     * schema into a {@code StringBuilder} that will contain the qualified table
     * name.  This method is run for the name of the table's catalog first and
     * then the table's schema.  The builder is appended if the catalog/schema
     * name is different from that which is the default for the database
     * connection.  The name part is always added if the table has already been
     * qualified (as defined by the {@code tableIsQualified} parameter.  This
     * ensures that the schema name is always appended if the catalog is
     * appended (when the schema is not null).  Some databases, notably MySQL,
     * define catalogs and not schemas.  In this case the schema name is null
     * and is never appended onto the table name buffer.
     * @param tableNameBuilder the buffer that will ultimately contain the table
     *                          name that is sufficiently qualified to be accessed
     *                          over a given database connection
     * @param quoter puts SQL identifiers is quotes when needed.
     * @param name of the catalog/schema that the table is defined under
     * @param authenticatedName the corresponding catalog/schema that is the
     *                          default for this database connection
     * @param tableIsQualified true when the table name has already been appended
     *                          and therefore should be appended to by this call
     *                          (assuming that {@code name} is not null).
     * @return true if table name buffer has been appended to or {@code tableIsQualified} is true
     */
    private boolean appendQualifiedName(
            StringBuilder tableNameBuilder,
            SQLIdentifiers.Quoter quoter,
            String name,
            String authenticatedName,
            boolean tableIsQualified) {
        if (name != null && (tableIsQualified || !name.equals(authenticatedName))) {
            tableNameBuilder.append(quoter.quoteIfNeeded(name));
            tableNameBuilder.append('.');
            return true;
        } else {
            return tableIsQualified;
        }
    }

    /**
     * Get the table that is sufficiently specified to enable it to be used with
     * the given database connection.  Database connections are initialised with
     * catalog and a schema.  If the table is for the default schema for this
     * database connection then the simple table name does not need to be
     * qualified and {@code simpleTableName} is returned.  If the table is in a
     * different schema but the same catalog then the table name is qualified
     * with the schema and if the table's schema is in a different catalog from
     * the database connection's default then the table name is qualified with
     * the catalog and the schema names.
     * <p/>
     * The parts of a qualified table name are separated by periods (full-stops).
     * For example, the following selects from the city table of the sakila
     * schema {@code select * from sakila.city}.
     * @param simpleTableName unqualified table name
     * @param connection valid database connection that SQL will be run under using the given table.
     * @param provider gives the catalog and the schema names that the given table name is referenced under.
     * @param quoter puts SQL identifiers is quotes when needed.
     * @return table name that is sufficiently qualified to execute against the given catalog and schema.
     * @throws SQLException failed to identify the default catalog for this database connection
     */
    private String getQualifiedTableName(String simpleTableName, DatabaseConnection connection, SchemaNameProvider provider, SQLIdentifiers.Quoter quoter) throws SQLException {
        final String schemaName = provider.getSchemaName();
        final String catName = provider.getCatalogName();

        StringBuilder fullTableName = new StringBuilder();
        boolean tableIsQualified = false;

        tableIsQualified = appendQualifiedName(fullTableName, quoter, catName, connection.getConnection().getCatalog(), tableIsQualified);
        tableIsQualified = appendQualifiedName(fullTableName, quoter, schemaName, connection.getSchema(), tableIsQualified);
        fullTableName.append(simpleTableName);

        return fullTableName.toString();
    }

    protected String getDefaultQuery(Node[] activatedNodes) {

        DatabaseConnection connection = activatedNodes[0].getLookup().lookup(DatabaseConnection.class);

        SQLIdentifiers.Quoter quoter;

        try {
            DatabaseMetaData dmd = connection.getConnection().getMetaData();
            quoter = SQLIdentifiers.createQuoter(dmd);

            SchemaNameProvider provider = activatedNodes[0].getLookup().lookup(SchemaNameProvider.class);

            boolean isColumn = activatedNodes[0].getLookup().lookup(ColumnNode.class) != null;

            String onome;
            if (!isColumn) {
                onome = getQualifiedTableName(activatedNodes[0].getName(), connection, provider, quoter);

                return "select * from " + onome; // NOI18N
            } else {
                String parentName = activatedNodes[0].getLookup().lookup(ColumnNameProvider.class).getParentName();
                onome = getQualifiedTableName(parentName, connection, provider, quoter);

                StringBuilder cols = new StringBuilder();
                for (Node node : activatedNodes) {
                    if (cols.length() > 0) {
                        cols.append(", ");
                    }

                    cols.append(quoter.quoteIfNeeded(node.getName()));
                }

                return "select " + cols.toString() + " from " + onome; // NOI18N
            }
        } catch (SQLException ex) {
            String message = NbBundle.getMessage(QueryAction.class, "ShowDataError", ex.getMessage()); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            return "";
        }
    }
}
