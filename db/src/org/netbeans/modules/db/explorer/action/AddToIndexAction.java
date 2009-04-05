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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.dlg.LabeledComboDialog;
import org.netbeans.modules.db.explorer.node.IndexNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Rob Englander
 */
public class AddToIndexAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(AddToIndexAction.class.getName());

    @Override
    public String getName() {
        return NbBundle.getMessage (AddToIndexAction.class, "AddColumn"); // NOI18N
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;

        if (activatedNodes.length == 1) {
            enabled = null != activatedNodes[0].getLookup().lookup(IndexNode.class);
        }

        return enabled;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(AddToIndexAction.class);
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        final IndexNode node = activatedNodes[0].getLookup().lookup(IndexNode.class);
        RequestProcessor.getDefault().post(
            new Runnable() {
                public void run() {
                    perform(node);
                }
            }
        );
    }

    private void perform(final IndexNode node) {
        DatabaseConnection dbconn = node.getLookup().lookup(DatabaseConnection.class);
        DatabaseConnector connector = dbconn.getConnector();

        try {
            final String tablename = node.getTableName();
            String sname = node.getSchemaName();
            String catalogName = node.getCatalogName();
            if (sname == null) {
                sname = catalogName;
            }

            if (catalogName == null) {
                catalogName = sname;
            }

            final String schemaName = sname;
            final Specification spec = connector.getDatabaseSpecification();

            final DriverSpecification drvSpec = connector.getDriverSpecification(catalogName);

            final String index = node.getName();

            // List columns used in current index (do not show)
            final HashSet<String> ixrm = new HashSet<String>();

            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        doAddToIndex(drvSpec, tablename, schemaName, index, ixrm, spec);

                        // go up as many as 2 nodes to find a parent to refresh
                        Node refreshNode = node.getParentNode();
                        if (refreshNode == null) {
                            refreshNode = node;
                        } else {
                            Node parent = refreshNode.getParentNode();
                            if (parent != null) {
                                refreshNode = parent;
                            }
                        }

                        SystemAction.get(RefreshAction.class).performAction(new Node[] { refreshNode } );
                    } catch (Exception exc) {
                        LOGGER.log(Level.INFO, exc.getMessage(), exc);
                        String message = NbBundle.getMessage(AddToIndexAction.class, "MSG_UnableToAddColumn", exc.getMessage());
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    }
                }
            });

        } catch(Exception exc) {
            LOGGER.log(Level.INFO, exc.getMessage(), exc);
            DbUtilities.reportError(NbBundle.getMessage (AddToIndexAction.class, "ERR_UnableToAddColumn"), exc.getMessage()); // NOI18N
        }
    }
    private void doAddToIndex(DriverSpecification drvSpec, String tablename, String schemaName, String index,
            HashSet<String> ixrm, Specification spec) throws DatabaseException, SQLException, Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("This method can not be called from the event dispatch thread");
        }
        drvSpec.getIndexInfo(tablename, false, true);
        ResultSet rs = drvSpec.getResultSet();
        HashMap rset = new HashMap();
        boolean isUQ = false;
        String ixname;
        while (rs.next()) {
            rset = drvSpec.getRow();
            ixname = (String) rset.get(new Integer(6));
            if (!index.equals(ixname)) {
                continue;
            }
            String colname = (String) rset.get(new Integer(9));
            ixrm.add(colname);
            String val = (String) rset.get(new Integer(4));
            if (val.equals("1")) {
                isUQ = false;
            } else {
                isUQ = !(Boolean.valueOf(val).booleanValue());
            }
            rset.clear();
        }
        rs.close();
        // List columns not present in current index
        Vector<String> cols = new Vector<String> (5);
        getColumns(drvSpec, tablename, rs, rset, ixrm, cols);
        if (cols.size() == 0) {
            throw new Exception(NbBundle.getMessage (AddToIndexAction.class, "EXC_NoUsableColumnInPlace")); // NOI18N
        }

        // Create and execute command
        final LabeledComboDialog dlg = new LabeledComboDialog(
                NbBundle.getMessage (AddToIndexAction.class, "AddToIndexTitle"), // NOI18N
                NbBundle.getMessage (AddToIndexAction.class, "AddToIndexLabel"), cols); // NOI18N
        Boolean success = Mutex.EVENT.readAccess(new ExceptionAction<Boolean>() {
            public Boolean run() throws Exception {
                return new Boolean(dlg.run());
            }
        });

        String selectedCol;
        if (success) {
            AddToIndexDDL ddl = new AddToIndexDDL(spec, schemaName, tablename);
            selectedCol = (String) dlg.getSelectedItem();
            ixrm.add(selectedCol);
            ddl.execute(index, isUQ, ixrm);
        }
    }

    private void getColumns(DriverSpecification drvSpec, String tablename, ResultSet rs, HashMap rset, HashSet ixrm, Vector<String> cols) throws SQLException {
        drvSpec.getColumns(tablename, "%");
        rs = drvSpec.getResultSet();
        while (rs.next()) {
            rset = drvSpec.getRow();
            String colname = (String) rset.get(new Integer(4));
            if (!ixrm.contains(colname)) {
                cols.add(colname);
            }
            rset.clear();
        }
        rs.close();
    }
}
