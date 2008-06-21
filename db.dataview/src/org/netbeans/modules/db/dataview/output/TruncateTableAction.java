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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.db.dataview.output;

import java.awt.event.ActionEvent;
import java.sql.SQLException;
import javax.swing.AbstractAction;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;

/**
 * @author Ahimanikya Satapathy
 */
class TruncateTableAction extends AbstractAction {

    DataView dataView;
    // the RequestProcessor used for executing statements.
    private final RequestProcessor rp = new RequestProcessor("SQLStatementExecution", 1, true); // NOI18N
    private final static String title = "Truncating Table";
    private final static String msg = "Truncating Table from database, please wait...";

    protected TruncateTableAction(DataView dataView) {
        super();
        this.dataView = dataView;
    }

    public void actionPerformed(ActionEvent e) {
        String confirmMsg = "Truncate contents of table " + dataView.getDataViewDBTable().geTable(0).getDisplayName();
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(confirmMsg, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE);

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.CANCEL_OPTION) {
            return;
        }

        dataView.disableButtons();
        SQLStatementExecutor executor = new SQLStatementExecutor(dataView, title, msg) {

            @Override
            public void finished() {
            DBTable tTable = parent.getDataViewDBTable().geTable(0);
            if (this.ex != null) {
                String errorMsg = "Failed to execute" + tTable.getDisplayName() + "\n";
                errorMsg += new DBException(ex).getMessage();
                parent.setErrorStatusText(errorMsg);
                NotifyDescriptor nd = new NotifyDescriptor.Message(errorMsg);
                DialogDisplayer.getDefault().notify(nd);
            } else {
                String informMsg = "Table " + tTable.getDisplayName() + " Truncated Successfully.";
                parent.setErrorStatusText(informMsg);
            }
            dataView.clearDataViewPanel();
            }

            @Override
            public void execute() throws SQLException, DBException {
             parent.getSQLExecutionHelper().truncateDBTable();
            }
        };
        
        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }
}
