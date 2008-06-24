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

import org.netbeans.modules.db.dataview.meta.DBException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Ahimanikya Satapathy
 */
class DataViewActionHandler {

    private final DataViewPageContext dataPage;
    private final SQLExecutionHelper execHelper;
    private final DataViewUI dataViewUI;
    private final DataView parent;

    DataViewActionHandler(DataViewUI dataViewUI, DataView parent) {
        this.parent = parent;
        this.dataViewUI = dataViewUI;

        this.dataPage = parent.getDataViewPageContext();
        this.execHelper = parent.getSQLExecutionHelper();
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

    void setMaxActionPerformed() {
        if (rejectModifications()) {
            int pageSize = dataViewUI.getPageSize(dataPage.getTotalRows());
            dataPage.setPageSize(pageSize);
            dataPage.first();
            parent.executeQuery();
        }
    }

    void firstActionPerformed() {
        if (rejectModifications()) {
            dataPage.first();
            parent.executeQuery();
        }
    }

    void previousActionPerformed() {
        if (rejectModifications()) {
            dataPage.previous();
            parent.executeQuery();
        }
    }

    void nextActionPerformed() {
        if (rejectModifications()) {
            dataPage.next();
            parent.executeQuery();
        }
    }

    void lastActionPerformed() {
        if (rejectModifications()) {
            dataPage.last();
            parent.executeQuery();
        }
    }

    void commitActionPerformed() {
        if (dataViewUI.isDirty()) {
            try {
                UpdatedRowContext tblContext = dataViewUI.getResultSetRowContext();
                for (String key : tblContext.getUpdateKeys()) {
                    execHelper.executeUpdate(key);
                }
            } catch (Exception ex) {
                String errorMsg = DBException.getMessage(ex);
                parent.setErrorStatusText(errorMsg);
            } finally {
                parent.executeQuery();
            }
        }
    }

    void insertActionPerformed() {
        InsertRecordDialog dialog = new InsertRecordDialog(parent);
        dialog.setVisible(true);
    }

    void deleteRecordActionPerformed() {
        DataViewTableUI rsTable = dataViewUI.getResulSetTable();
        if (rsTable.getSelectedRowCount() == 0) {
            String msg = "Please select a row to delete.";
            parent.setErrorStatusText(msg);
        } else {
            try {
                String msg = "Permanently delete record(s) from the database?";
                NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, "Confirm delete", NotifyDescriptor.OK_CANCEL_OPTION);
                if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
                    int[] rows = rsTable.getSelectedRows();
                    for (int j = 0; j < rows.length; j++) {
                        execHelper.executeDeleteRow(rows[j], dataViewUI.getResulSetTable().getModel());
                    }
                    parent.executeQuery();
                }
            } catch (Exception ex) {
                String msg = "Error Deleting Row(s): " + ex.getMessage();
                parent.setErrorStatusText(msg);
            }
        }
    }

    void refreshActionPerformed() {
        int intVal = dataPage.getTotalRows();
        if (intVal < 0) {
            return;
        }
        dataPage.setRecordToRefresh(intVal);
        parent.executeQuery();
    }
}
