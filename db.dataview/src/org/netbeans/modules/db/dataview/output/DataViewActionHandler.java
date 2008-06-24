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

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Handles all the DataView Panel actions.
 * 
 * @author Ahimanikya Satapathy
 */
class DataViewActionHandler {

    private final DataViewPageContext dataPage;
    private final SQLExecutionHelper execHelper;
    private final DataViewUI dataViewUI;
    private final DataView dataView;

    DataViewActionHandler(DataViewUI dataViewUI, DataView dataView) {
        this.dataView = dataView;
        this.dataViewUI = dataViewUI;

        this.dataPage = dataView.getDataViewPageContext();
        this.execHelper = dataView.getSQLExecutionHelper();
    }

    private boolean rejectModifications() {
        boolean doCalculation = true;
        if (dataViewUI.isCommitEnabled()) {
            String msg = "You have uncommited Changes in this page. If you continue, you changes will be lost. Do you still want to continue?";
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, "Confirm navigation", NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.NO_OPTION) {
                doCalculation = false;
            }
        }
        return doCalculation;
    }
    
    void cancelEditPerformed(){
        dataView.getUpdatedRowContext().resetUpdateState();
        dataView.setRowsInTableModel();
        dataViewUI.setCancelEnabled(false);
        dataViewUI.setCommitEnabled(false);
    }

    void setMaxActionPerformed() {
        if (rejectModifications()) {
            int pageSize = dataViewUI.getPageSize(dataPage.getTotalRows());
            dataPage.setPageSize(pageSize);
            dataPage.first();
            execHelper.executeQuery();
        }
    }

    void firstActionPerformed() {
        if (rejectModifications()) {
            dataPage.first();
            execHelper.executeQuery();
        }
    }

    void previousActionPerformed() {
        if (rejectModifications()) {
            dataPage.previous();
            execHelper.executeQuery();
        }
    }

    void nextActionPerformed() {
        if (rejectModifications()) {
            dataPage.next();
            execHelper.executeQuery();
        }
    }

    void lastActionPerformed() {
        if (rejectModifications()) {
            dataPage.last();
            execHelper.executeQuery();
        }
    }

    void commitActionPerformed() {
        if (dataViewUI.isDirty()) {
            execHelper.executeUpdate();
        }
    }

    void insertActionPerformed() {
        InsertRecordDialog dialog = new InsertRecordDialog(dataView);
        dialog.setLocationRelativeTo(dataView);
        dialog.setVisible(true);
    }

    void truncateActionPerformed() {
        String confirmMsg = "Truncate contents of table " + dataView.getDataViewDBTable().geTable(0).getDisplayName();
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(confirmMsg, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE);

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.CANCEL_OPTION) {
            return;
        }
        execHelper.executeTruncate();
    }

    void deleteRecordActionPerformed() {
        DataViewTableUI rsTable = dataViewUI.getResulSetTable();
        if (rsTable.getSelectedRowCount() == 0) {
            String msg = "Please select a row to delete.";
            dataView.setErrorStatusText(msg);
        } else {
            String msg = "Permanently delete record(s) from the database?";
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, "Confirm delete", NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
                execHelper.executeDeleteRow(rsTable);
            }
        }
    }

    void refreshActionPerformed() {
        int intVal = dataPage.getTotalRows();
        if (intVal < 0) {
            return;
        }
        dataPage.setRecordToRefresh(intVal);
        execHelper.executeQuery();
    }
}
