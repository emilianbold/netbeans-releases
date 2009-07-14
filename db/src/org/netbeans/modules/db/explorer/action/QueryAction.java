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

    protected String getDefaultQuery(Node[] activatedNodes) {

        DatabaseConnection connection = activatedNodes[0].getLookup().lookup(DatabaseConnection.class);

        //org.openide.nodes.Node node = activatedNodes[0];
        //DatabaseNodeInfo info = (DatabaseNodeInfo) node.getCookie(DatabaseNodeInfo.class);
        SQLIdentifiers.Quoter quoter;

        try {
            DatabaseMetaData dmd = connection.getConnection().getMetaData();
            quoter = SQLIdentifiers.createQuoter(dmd);
        } catch (SQLException ex) {
            String message = NbBundle.getMessage (QueryAction.class, "ShowDataError", ex.getMessage()); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            return "";
        }

        StringBuffer cols = new StringBuffer();

        SchemaNameProvider provider = activatedNodes[0].getLookup().lookup(SchemaNameProvider.class);

        String schemaName = provider.getSchemaName();
        String catName = provider.getCatalogName();
        if (schemaName == null) {
            schemaName = catName == null ? "" : catName; // NOI18N
        }

        boolean isColumn = activatedNodes[0].getLookup().lookup(ColumnNode.class) != null;

        java.lang.String onome;
        if (!isColumn) {
            onome = quoter.quoteIfNeeded(activatedNodes[0].getName());
            if (!schemaName.equals("")) {
                onome = quoter.quoteIfNeeded(schemaName) + "." + onome;
            }

            return "select * from " + onome;
        } else {
            String parentName = activatedNodes[0].getLookup().lookup(ColumnNameProvider.class).getParentName();
            onome = quoter.quoteIfNeeded(parentName);

            if (!schemaName.equals("")) {
                onome = quoter.quoteIfNeeded(schemaName) + "." + onome;
            }

            for (Node node : activatedNodes) {
                if (cols.length() > 0) {
                    cols.append(", ");
                }

                cols.append(quoter.quoteIfNeeded(node.getName()));
            }

            return "select " + cols.toString() + " from " + onome;
        }
    }
}
