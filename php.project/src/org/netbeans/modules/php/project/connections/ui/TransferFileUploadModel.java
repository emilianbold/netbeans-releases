/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.php.project.connections.ui;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.table.JTableHeader;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
public class TransferFileUploadModel extends TransferFileTableModel {

    private final Logger err = Logger.getLogger("org.netbeans.modules.php.project.connections.ui.FileConfirmationTableModelImpl");

    public TransferFileUploadModel(List<TransferFileUnit> fileUnits) {
        setData(fileUnits);
    }

    @Override
    public String getToolTipText(int row, int col) {
        if (col == 3) {
        } else if (col == 0) {
        }
        return super.getToolTipText(row, col);
    }

    @Override
    public void setValueAt(Object anValue, int row, int col) {
        super.setValueAt(anValue, row, col);

        if (col == 1) {
            // second column handles buttons
            return;
        }
        assert col == 0 : "First column.";
        if (anValue == null) {
            return;
        }
        //assert getCategoryAtRow(row).isExpanded();
        TransferFileUnit u = getUnitAtRow(row);
        assert anValue instanceof Boolean : anValue + " must be instanceof Boolean.";
        boolean beforeMarked = u.isMarked();
        if ((Boolean) anValue != beforeMarked) {
            u.setMarked(!beforeMarked);
            fireUpdataUnitChange();
            if (u.isMarked() != beforeMarked) {
                fireButtonsChange();
            } else {
                //TODO: message should contain spec.version
                String message = getBundle("NotificationAlreadyPreparedToIntsall", u.getDisplayName()); // NOI18N
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
            }
        }

    }

    public Object getValueAt(int row, int col) {
        Object res = null;

        TransferFileUnit u = getUnitAtRow(row);
        switch (col) {
            case 0:
                res = u.isMarked() ? Boolean.TRUE : Boolean.FALSE;
                break;
            case 1:
                res = u.getDisplayName();
                break;
        }

        return res;
    }

    public int getColumnCount() {
        return 2;
    }

    public Class getColumnClass(int c) {
        Class res = null;

        switch (c) {
            case 0:
                res = Boolean.class;
                break;
            case 1:
                res = String.class;
                break;
        }

        return res;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return getBundle("FileConfirmationTableModel_Columns_Upload");
            case 1:
                return getBundle("FileConfirmationTableModel_Columns_RelativePath");
        }

        assert false;
        return super.getColumnName(column);
    }

    @Override
    public int getMinWidth(JTableHeader header, int col) {
        return super.getMinWidth(header, col);
    }

    public int getPreferredWidth(JTableHeader header, int col) {
        switch (col) {
            case 1:
                return super.getMinWidth(header, col) * 4;
        }
        return super.getMinWidth(header, col);
    }

    public boolean isSortAllowed(Object columnIdentifier) {
        boolean isUpload = getColumnName(0).equals(columnIdentifier);
        return isUpload ? false : true;
    }

    protected Comparator<TransferFileUnit> getComparator(final Object columnIdentifier, final boolean sortAscending) {
        return new Comparator<TransferFileUnit>() {

            public int compare(TransferFileUnit o1, TransferFileUnit o2) {
                TransferFileUnit unit1 = sortAscending ? o1 : o2;
                TransferFileUnit unit2 = sortAscending ? o2 : o1;
                if (getColumnName(0).equals(columnIdentifier)) {
                    assert false : columnIdentifier.toString();
                } else if (getColumnName(1).equals(columnIdentifier)) {
                    return TransferFileUnit.compare(unit1, unit2);
                }
                return 0;
            }
        };
    }

    protected String getBundle(String key, Object... params) {
        return NbBundle.getMessage(this.getClass(), key, params);
    }

    public String getTabTitle() {
        return NbBundle.getMessage(TransferFileUploadModel.class,
                "FileConfirmationTableModel_Upload_Title");//NOI18N
    }

    @Override
    public Type getType() {
        return TransferFileTableModel.Type.UPLOAD;
    }
}
