/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.commit;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.versioning.system.cvss.CvsFileTableModel;
import org.netbeans.modules.versioning.system.cvss.CvsFileNode;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;
import org.netbeans.lib.cvsclient.command.KeywordSubstitutionOptions;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.io.File;

/**
 * @author Maros Sandor
 */
class CommitTableModel extends AbstractTableModel {

    private static final ResourceBundle loc = NbBundle.getBundle(CommitTableModel.class);
    private static final String [] columns = {
        loc.getString("CTL_CommitTable_Column_File"),
        loc.getString("CTL_CommitTable_Column_Status"),
        loc.getString("CTL_CommitTable_Column_Action"),
        loc.getString("CTL_CommitTable_Column_Folder")
    };
    
    private CvsFileTableModel   fileTableModel;
    private boolean             fetchingNodes;
    private CommitOptions []    commitOptions;
    private CvsFileNode []      nodes;

    public CommitTableModel(CvsFileTableModel tableModel) {
        fileTableModel = tableModel;
    }

    public CommitSettings.CommitFile[] getCommitFiles() {
        if (nodes == null) return new CommitSettings.CommitFile[0];
        CommitSettings.CommitFile [] files = new CommitSettings.CommitFile[nodes.length]; 
        for (int i = 0; i < nodes.length; i++) {
            files[i] = new CommitSettings.CommitFile(nodes[i], commitOptions[i]);
        }
        return files;
    }
    
    public String getColumnName(int column) {
        return columns[column];
    }

    public int getColumnCount() {
        return columns.length;
    }

    public int getRowCount() {
        initNodes();
        return nodes == null ? 0 : nodes.length;
    }

    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0:
        case 1:
        case 3:
            return String.class;
        case 2:
            return CommitOptions.class;
        default: 
            throw new IllegalArgumentException("Column index out of range: " + columnIndex);
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 2;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        initNodes();
        if (nodes == null) return "";
        switch (columnIndex) {
        case 0:
            return nodes[rowIndex].getName();
        case 1:
            return nodes[rowIndex].getInformation().getStatusText();
        case 2:
            return commitOptions[rowIndex];
        case 3:
            return Utils.getRelativePath(nodes[rowIndex].getFile().getParent());
        default:
            throw new IllegalArgumentException("Column index out of range: " + columnIndex);
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 2:
            CommitOptions now = commitOptions[rowIndex]; 
            commitOptions[rowIndex] = (CommitOptions) aValue;
            if (now == CommitOptions.EXCLUDE && aValue != CommitOptions.EXCLUDE) {
                CvsModuleConfig.getDefault().removeExclusionPath(nodes[rowIndex].getFile().getAbsolutePath());
            } else if (aValue == CommitOptions.EXCLUDE && now != CommitOptions.EXCLUDE) {
                CvsModuleConfig.getDefault().addExclusionPath(nodes[rowIndex].getFile().getAbsolutePath());
            }
            break;
        default:
            throw new IllegalArgumentException("Column index out of range: " + columnIndex);
        }
    }

    private synchronized void initNodes() {
        if (fetchingNodes || nodes != null) return;
        fetchingNodes = true;
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                CvsFileNode [] newNodes = fileTableModel.getNodes();
                createCommitOptions(newNodes);
                fetchingNodes = false;
                nodes = newNodes;
                fireTableDataChanged();
            }
        });        
    }

    private void createCommitOptions(CvsFileNode[] newNodes) {
        commitOptions = new CommitOptions[newNodes.length];
        for (int i = 0; i < newNodes.length; i++) {
            CvsFileNode newNode = newNodes[i];
            if (CvsModuleConfig.getDefault().isExcludedFromCommit(newNode.getFile().getAbsolutePath())) {
                commitOptions[i] = CommitOptions.EXCLUDE;
            } else {
                switch (newNode.getInformation().getStatus()) {
                case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY:
                    commitOptions[i] = getDefaultCommitOptions(newNode.getFile());
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

    CvsFileNode getNodeAt(int row) {
        return nodes[row];
    }
    
    private CommitOptions getDefaultCommitOptions(File file) {
        KeywordSubstitutionOptions options = CvsVersioningSystem.getInstance().getDefaultKeywordSubstitution(file);
        return options == KeywordSubstitutionOptions.BINARY ? CommitOptions.ADD_BINARY : CommitOptions.ADD_TEXT;  
    }
}
