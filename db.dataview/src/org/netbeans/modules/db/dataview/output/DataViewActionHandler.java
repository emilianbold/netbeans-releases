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

import java.awt.Component;
import javax.swing.JOptionPane;
import org.netbeans.modules.db.dataview.logger.Localizer;
import org.openide.windows.WindowManager;

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
    private static transient final Localizer mLoc = Localizer.get();

    DataViewActionHandler(DataViewUI dataViewUI, DataView dataView) {
        this.dataView = dataView;
        this.dataViewUI = dataViewUI;

        this.dataPage = dataView.getDataViewPageContext();
        this.execHelper = dataView.getSQLExecutionHelper();
    }

    private boolean rejectModifications() {
        boolean doCalculation = true;
        if (dataViewUI.isCommitEnabled()) {
            String nbBundle5 = mLoc.t("RESC005: You have uncommited Changes in this page. If you continue, you changes will be lost. Do you still want to continue?");
            String msg = nbBundle5.substring(15);
            String nbBundle6 = mLoc.t("RESC006: Confirm Navigation");
            if (showYesAllDialog(msg, nbBundle6.substring(15)) == 1) {
                doCalculation = false;
            }
        }
        return doCalculation;
    }

    void cancelEditPerformed() {
        dataView.getUpdatedRowContext().resetUpdateState();
        dataView.setRowsInTableModel();
        dataViewUI.setCancelEnabled(false);
        dataViewUI.setCommitEnabled(false);
    }

    void setMaxActionPerformed() {
        if (rejectModifications()) {
            int pageSize = dataViewUI.getPageSize();
            dataPage.setPageSize(pageSize);
            dataPage.first();
            dataPage.setTotalRows(-1); // force total row refresh
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
            execHelper.executeUpdateRow();
        }
    }

    void insertActionPerformed() {
        InsertRecordDialog dialog = new InsertRecordDialog(dataView);
        dialog.setLocationRelativeTo(dataViewUI);
        dialog.setVisible(true);
    }

    void truncateActionPerformed() {
        String nbBundle7 = mLoc.t("RESC007: Truncate contents of table");
        String confirmMsg = nbBundle7.substring(15) + dataView.getDataViewDBTable().geTable(0).getDisplayName();
        if (showYesAllDialog(confirmMsg, confirmMsg) == 0) {
            execHelper.executeTruncate();
        } 
    }

    void deleteRecordActionPerformed() {
        DataViewTableUI rsTable = dataViewUI.getDataViewTableUI();
        if (rsTable.getSelectedRowCount() == 0) {
            String nbBundle8 = mLoc.t("RESC008: Please select a row to delete.");
            String msg = nbBundle8.substring(15);
            dataView.setInfoStatusText(msg);
        } else {
            String nbBundle9 = mLoc.t("RESC009: Permanently delete record(s) from the database?");
            String msg = nbBundle9.substring(15);
            String nbBundle10 = mLoc.t("RESC010: Confirm Delete");
            if (showYesAllDialog(msg, nbBundle10.substring(15)) == 0) {
                execHelper.executeDeleteRow(rsTable);
            }
        }
    }

    void refreshActionPerformed() {
        dataPage.setTotalRows(-1); // force total row refresh
        execHelper.executeQuery();
    }

    private static int showYesAllDialog(Object msg, String title) {
       String[] options = new String[] { "Yes", "No",};
       Component parent = WindowManager.getDefault().getMainWindow();
       return JOptionPane.showOptionDialog(parent, msg, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
   }
}
