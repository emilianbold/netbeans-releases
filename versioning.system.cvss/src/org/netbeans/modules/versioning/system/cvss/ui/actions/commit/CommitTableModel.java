/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.commit;

import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.system.cvss.CvsFileNode;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.lib.cvsclient.command.KeywordSubstitutionOptions;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.io.File;

/**
 * Table model for the Commit dialog table.
 *
 * @author Maros Sandor
 */
class CommitTableModel extends AbstractTableModel {

    /**
     * Defines labels for Versioning view table columns.
     */ 
    private static final Map columnLabels = new HashMap(5);
    {
        ResourceBundle loc = NbBundle.getBundle(CommitTableModel.class);
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
        if (col.equals(CommitSettings.COLUMN_NAME_ACTION)) {
            return CommitOptions.class;
        }
        return String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        String col = columns[columnIndex];
        return col.equals(CommitSettings.COLUMN_NAME_ACTION);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        String col = columns[columnIndex];
        if (col.equals(CommitSettings.COLUMN_NAME_NAME)) {
            return nodes[rowIndex].getName();
        } else if (col.equals(CommitSettings.COLUMN_NAME_STICKY)) {
            String sticky = Utils.getSticky(nodes[rowIndex].getFile());
            return sticky == null ? "" : sticky.substring(1); // NOI18N
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
            fireTableCellUpdated(rowIndex, columnIndex);
        } else {
            throw new IllegalArgumentException("Column index out of range: " + columnIndex); // NOI18N
        }
    }

    private void createCommitOptions() {
        boolean excludeNew = System.getProperty("netbeans.javacvs.excludeNewFiles") != null; // NOI18N
        commitOptions = new CommitOptions[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            CvsFileNode node = nodes[i];
            if (CvsModuleConfig.getDefault().isExcludedFromCommit(node.getFile())) {
                commitOptions[i] = CommitOptions.EXCLUDE;
            } else {
                switch (node.getInformation().getStatus()) {
                case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY:
                    commitOptions[i] = excludeNew ? CommitOptions.EXCLUDE : getDefaultCommitOptions(node.getFile());
                    break;
                case FileInformation.STATUS_VERSIONED_DELETEDLOCALLY:
                case FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY:
                    commitOptions[i] = CommitOptions.COMMIT_REMOVE;
                    break;
                default:
                    commitOptions[i] = CommitOptions.COMMIT;
                }
            }
        }
    }

    CommitSettings.CommitFile getCommitFile(int row) {
        return new CommitSettings.CommitFile(nodes[row], commitOptions[row]);
    }
    
    private CommitOptions getDefaultCommitOptions(File file) {
        KeywordSubstitutionOptions options = CvsVersioningSystem.getInstance().getDefaultKeywordSubstitution(file);
        return options == KeywordSubstitutionOptions.BINARY ? CommitOptions.ADD_BINARY : CommitOptions.ADD_TEXT;  
    }
}
