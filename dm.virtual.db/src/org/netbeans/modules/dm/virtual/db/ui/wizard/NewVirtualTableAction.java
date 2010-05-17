/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.dm.virtual.db.ui.wizard;

import java.awt.Dialog;
import java.awt.Dimension;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.dm.virtual.db.model.VirtualDBConnectionFactory;
import org.netbeans.modules.dm.virtual.db.bootstrap.PropertyKeys;
import org.netbeans.modules.dm.virtual.db.model.VirtualDatabaseModel;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.model.DBExplorerUtil;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public final class NewVirtualTableAction extends CallableSystemAction {

    public void performAction() {
        WizardDescriptor.Iterator iterator = new VirtualDBTableWizardIterator();
        WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(NewVirtualTableAction.class, "LBL_title"));

        ((VirtualDBTableWizardIterator) iterator).setDescriptor(wizardDescriptor);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        //dialog.setSize(new Dimension(650, 300));
        dialog.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(NewVirtualTableAction.class, "ACSD_dialog"));
        dialog.setVisible(true);
        dialog.toFront();

        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            String error = null;
            boolean status = false;
            String jdbcUrl = (String) wizardDescriptor.getProperty("url"); // NOI18N

            VirtualDatabaseModel model = (VirtualDatabaseModel) wizardDescriptor.getProperty(VirtualDBTableWizardIterator.PROP_VIRTUALDBMODEL);
            List<String> urls = (List<String>) wizardDescriptor.getProperty(
                    VirtualDBTableWizardIterator.URL_LIST);
            List<String> tables = (List<String>) wizardDescriptor.getProperty(
                    VirtualDBTableWizardIterator.TABLE_LIST);

            Connection conn = null;
            Statement stmt = null;
            String dbDir = (DBExplorerUtil.parseConnUrl(jdbcUrl))[1];
            try {
                conn = VirtualDBConnectionFactory.getInstance().getConnection(jdbcUrl);
                if (conn != null) {
                    conn.setAutoCommit(true);
                    stmt = conn.createStatement();
                } else {
                    throw new SQLException("Unable to get Connection -->" + jdbcUrl);
                }

                if (model != null) {
                    Iterator tablesIt = model.getTables().iterator();
                    while (tablesIt.hasNext()) {
                        VirtualDBTable table = (VirtualDBTable) tablesIt.next();
                        int i = tables.indexOf(table.getName());
                        ((VirtualDBTable) table).setOrPutProperty(PropertyKeys.FILENAME, urls.get(i));
                        String sql = table.getCreateStatementSQL();
                        stmt.execute(sql);
                    }
                }
                status = true;
            } catch (Exception ex) {
                ErrorManager.getDefault().log(ex.getMessage());
                status = false;
            } finally {
                if (conn != null) {
                    try {
                        if (stmt != null) {
                            stmt.execute("shutdown"); // NOI18N
                        }
                        conn.close();
                        File dbExplorerNeedRefresh = new File(dbDir + "/dbExplorerNeedRefresh"); // NOI18N
                        dbExplorerNeedRefresh.createNewFile();
                    } catch (Exception ex) {
                        conn = null;
                    }
                }
            }

            if (status) {
                NotifyDescriptor d = new NotifyDescriptor.Message(
                        NbBundle.getMessage(NewVirtualTableAction.class, "MSG_Success"),
                        NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            } else {
                String msg = NbBundle.getMessage(NewVirtualTableAction.class, "MSG_fail");
                if (error != null) {
                    msg = msg + NbBundle.getMessage(NewVirtualTableAction.class, "MSG_cause") + error;
                }
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }
    }

    public String getName() {
        return NbBundle.getMessage(NewVirtualTableAction.class, "LBL_add_table");
    }

    @Override
    protected void initialize() {
        super.initialize();
        putValue(NbBundle.getMessage(NewVirtualTableAction.class, "MSG_noIconInMenu"), Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}