/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.versioning.system.cvss.ui.actions.commit;

import java.io.File;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.system.cvss.CvsFileNode;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.lib.cvsclient.command.KeywordSubstitutionOptions;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * Table model for the Commit dialog table.
 *
 * @author Maros Sandor
 */
class CommitTableModel extends AbstractTableModel {

    /**
     * Defines labels for Versioning view table columns.
     */ 
    private final Map<String, String[]> columnLabels = new HashMap<String, String[]>(5);
    {
        ResourceBundle loc = NbBundle.getBundle(CommitTableModel.class);
        columnLabels.put(CommitSettings.COLUMN_NAME_COMMIT, new String [] {
                                          loc.getString("CTL_CommitTable_Column_Commit"),  // NOI18N
                                          loc.getString("CTL_CommitTable_Column_Description")}); // NOI18N
        columnLabels.put(CommitSettings.COLUMN_NAME_NAME, new String [] {
                                          loc.getString("CTL_CommitTable_Column_File"), 
                                          loc.getString("CTL_CommitTable_Column_File")});
        columnLabels.put(CommitSettings.COLUMN_NAME_STICKY, new String [] { 
                                          loc.getString("CTL_CommitTable_Column_Sticky"), 
                                          loc.getString("CTL_CommitTable_Column_Sticky")});
        columnLabels.put(CommitSettings.COLUMN_NAME_STATUS, new String [] { 
                                          loc.getString("CTL_CommitTable_Column_Status"), 
                                          loc.getString("CTL_CommitTable_Column_Status")});
        columnLabels.put(CommitSettings.COLUMN_NAME_ACTION, new String [] { 
                                          loc.getString("CTL_CommitTable_Column_Action"), 
                                          loc.getString("CTL_CommitTable_Column_Action")});
        columnLabels.put(CommitSettings.COLUMN_NAME_PATH, new String [] { 
                                          loc.getString("CTL_CommitTable_Column_Folder"), 
                                          loc.getString("CTL_CommitTable_Column_Folder")});
    }
    
    private CommitOptions []    commitOptions;
    private CvsFileNode []      nodes;
    
    private String [] columns;

    public CommitTableModel() {
        setColumns(new String [0]);
        setNodes(new CvsFileNode[0]);
    }

    void setNodes(CvsFileNode [] nodes) {
        this.nodes = nodes;
        createCommitOptions();
        fireTableDataChanged();
    }
    
    void setColumns(String [] cols) {
        if (Arrays.equals(cols, columns)) return;
        columns = cols;
        fireTableStructureChanged();
    }

    public CommitSettings.CommitFile[] getCommitFiles() {
        CommitSettings.CommitFile [] files = new CommitSettings.CommitFile[nodes.length]; 
        for (int i = 0; i < nodes.length; i++) {
            files[i] = new CommitSettings.CommitFile(nodes[i], commitOptions[i]);
        }
        return files;
    }
    
    public String getColumnName(int column) {
        return ((String []) columnLabels.get(columns[column]))[0];
    }

    public int getColumnCount() {
        return columns.length;
    }

    public int getRowCount() {
        return nodes.length;
    }

    public Class getColumnClass(int columnIndex) {
        String col = columns[columnIndex];
        if (col.equals(CommitSettings.COLUMN_NAME_COMMIT)) {
            return Boolean.class;
        } else if (col.equals(CommitSettings.COLUMN_NAME_ACTION)) {
            return CommitOptions.class;
        } else {
            return String.class;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        String col = columns[columnIndex];
        return col.equals(CommitSettings.COLUMN_NAME_COMMIT);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        String col = columns[columnIndex];
        if (col.equals(CommitSettings.COLUMN_NAME_COMMIT)) {
            return commitOptions[rowIndex] != CommitOptions.EXCLUDE;
        } else if (col.equals(CommitSettings.COLUMN_NAME_NAME)) {
            return nodes[rowIndex].getName();
        } else if (col.equals(CommitSettings.COLUMN_NAME_STICKY)) {
            String sticky = Utils.getSticky(nodes[rowIndex].getFile());
            return sticky == null ? "" : sticky; // NOI18N
        } else if (col.equals(CommitSettings.COLUMN_NAME_STATUS)) {
            return nodes[rowIndex].getInformation().getStatusText();
        } else if (col.equals(CommitSettings.COLUMN_NAME_ACTION)) {
            return commitOptions[rowIndex];
        } else if (col.equals(CommitSettings.COLUMN_NAME_PATH)) {
            return Utils.getRelativePath(nodes[rowIndex].getFile());
        }
        throw new IllegalArgumentException("Column index out of range: " + columnIndex); // NOI18N
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String col = columns[columnIndex];
        if (col.equals(CommitSettings.COLUMN_NAME_ACTION)) {
            commitOptions[rowIndex] = (CommitOptions) aValue;
        } else if (col.equals(CommitSettings.COLUMN_NAME_COMMIT)) {
            commitOptions[rowIndex] = ((Boolean) aValue) ? getCommitOptions(rowIndex) : CommitOptions.EXCLUDE;
        } else {
            throw new IllegalArgumentException("Column index out of range: " + columnIndex); // NOI18N
        }
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    private void createCommitOptions() {
        boolean excludeNew = CvsModuleConfig.getDefault().getPreferences().getBoolean(CvsModuleConfig.PROP_EXCLUDE_NEW_FILES, false);
        commitOptions = new CommitOptions[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            CvsFileNode node = nodes[i];
            if (CvsModuleConfig.getDefault().isExcludedFromCommit(node.getFile())) {
                commitOptions[i] = CommitOptions.EXCLUDE;
            } else {
                commitOptions[i] = getCommitOptions(node, excludeNew);
            }
        }
    }

    CommitSettings.CommitFile getCommitFile(int row) {
        return new CommitSettings.CommitFile(nodes[row], commitOptions[row]);
    }

    CvsFileNode getNode(int row) {
        return nodes[row];
    }

    CommitOptions getOptions(int row) {
        return commitOptions[row];
    }
    
    private CommitOptions getDefaultCommitOptions(File file) {
        KeywordSubstitutionOptions options = CvsVersioningSystem.getInstance().getDefaultKeywordSubstitution(file);
        return options == KeywordSubstitutionOptions.BINARY ? CommitOptions.ADD_BINARY : CommitOptions.ADD_TEXT;  
    }

    void setIncluded (int[] rows, boolean include) {
        for (int rowIndex : rows) {
            if (!include || CommitOptions.EXCLUDE.equals(commitOptions[rowIndex])) {
                commitOptions[rowIndex] = include ? getCommitOptions(rowIndex) : CommitOptions.EXCLUDE;
            }
        }
        fireTableRowsUpdated(0, getRowCount() - 1);
    }

    void setAdded (int[] rows, CommitOptions addOption) {
        for (int rowIndex : rows) {
            commitOptions[rowIndex] = addOption;
        }
        fireTableRowsUpdated(0, getRowCount() - 1);
    }

    private CommitOptions getCommitOptions (int rowIndex) {
        CvsFileNode node = nodes[rowIndex];
        return getCommitOptions(node, false);
    }

    private CommitOptions getCommitOptions (CvsFileNode node, boolean excludeNew) {
        CommitOptions options;
        switch (node.getInformation().getStatus()) {
            case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY:
                options = excludeNew ? CommitOptions.EXCLUDE : getDefaultCommitOptions(node.getFile());
                break;
            case FileInformation.STATUS_VERSIONED_DELETEDLOCALLY:
            case FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY:
                options = CommitOptions.COMMIT_REMOVE;
                break;
            default:
                options = CommitOptions.COMMIT;
        }
        return options;
    }
}
