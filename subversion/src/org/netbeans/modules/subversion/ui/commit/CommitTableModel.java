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

package org.netbeans.modules.subversion.ui.commit;

import java.io.*;
import org.netbeans.modules.subversion.*;
import org.openide.util.NbBundle;
import org.netbeans.modules.subversion.SvnFileNode;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.settings.SvnModuleConfig;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.io.File;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Table model for the Commit dialog table.
 *
 * @author Maros Sandor
 */
class CommitTableModel extends AbstractTableModel {

    static final String COLUMN_NAME_NAME    = "name"; // NOI18N
    static final String COLUMN_NAME_STATUS  = "status"; // NOI18N
    static final String COLUMN_NAME_ACTION  = "action"; // NOI18N
    static final String COLUMN_NAME_PATH    = "path"; // NOI18N

    private class RootFile {
        String repositoryPath;
        String rootLocalPath;
    }
    private Set<SVNUrl> repositoryRoots;
    private RootFile rootFile;

    /**
     * Defines labels for Versioning view table columns.
     */ 
    private static final Map<String, String[]> columnLabels = new HashMap<String, String[]>(4);   

    {
        ResourceBundle loc = NbBundle.getBundle(CommitTableModel.class);
        columnLabels.put(COLUMN_NAME_NAME, new String [] {
                                          loc.getString("CTL_CommitTable_Column_File"), 
                                          loc.getString("CTL_CommitTable_Column_File")});
        columnLabels.put(COLUMN_NAME_STATUS, new String [] {
                                          loc.getString("CTL_CommitTable_Column_Status"), 
                                          loc.getString("CTL_CommitTable_Column_Status")});
        columnLabels.put(COLUMN_NAME_ACTION, new String [] {
                                          loc.getString("CTL_CommitTable_Column_Action"), 
                                          loc.getString("CTL_CommitTable_Column_Action")});
        columnLabels.put(COLUMN_NAME_PATH, new String [] {
                                          loc.getString("CTL_CommitTable_Column_Folder"), 
                                          loc.getString("CTL_CommitTable_Column_Folder")});
    }
    
    private CommitOptions []    commitOptions;
    private SvnFileNode []      nodes;
    
    private String [] columns;

    /**
     * Create stable with name, status, action and path columns
     * and empty nodes {@link #setNodes model}.
     */
    public CommitTableModel(String[] columns) {
        setColumns(columns);
        setNodes(new SvnFileNode[0]);
    }

    void setNodes(SvnFileNode [] nodes) {
        this.nodes = nodes;
        defaultCommitOptions();
        fireTableDataChanged();
    }
    
    void setColumns(String [] cols) {
        if (Arrays.equals(cols, columns)) return;
        columns = cols;
        fireTableStructureChanged();
    }

    /**
     * @return Map&lt;SvnFileNode, CommitOptions>
     */
    public Map<SvnFileNode, CommitOptions> getCommitFiles() {
        Map<SvnFileNode, CommitOptions> ret = new HashMap<SvnFileNode, CommitOptions>(nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            ret.put(nodes[i], commitOptions[i]);
        }
        return ret;
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
        if (col.equals(COLUMN_NAME_ACTION)) {
            return CommitOptions.class;
        }
        return String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        String col = columns[columnIndex];
        return col.equals(COLUMN_NAME_ACTION);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        SvnFileNode node;
        String col = columns[columnIndex];
        if (col.equals(COLUMN_NAME_NAME)) {
            return nodes[rowIndex].getName();
        } else if (col.equals(COLUMN_NAME_STATUS)) {
            node = nodes[rowIndex];
            FileInformation finfo =  node.getInformation();
            finfo.getEntry(node.getFile());  // HACK returned value is not interesting, point is side effect, it loads ISVNStatus structure
            return finfo.getStatusText();
        } else if (col.equals(COLUMN_NAME_ACTION)) {
            return commitOptions[rowIndex];
        } else if (col.equals(COLUMN_NAME_PATH)) {
            String shortPath = null;
            // XXX this is a mess
            if(rootFile != null) {
                shortPath = rootFile.repositoryPath + nodes[rowIndex].getFile().getAbsolutePath().substring(rootFile.rootLocalPath.length());
            } else {
                Set<SVNUrl> url = getRepositoryRoots();
                for (Iterator<SVNUrl> it = url.iterator(); it.hasNext();) {
                    SVNUrl nextUrl = it.next();
                    shortPath = SvnUtils.getRelativePath(nextUrl, nodes[rowIndex].getFile());
                }
                if (shortPath == null) {
                    SVNUrl newUrl = SvnUtils.getRepositoryRootUrl(nodes[rowIndex].getFile());
                    shortPath = SvnUtils.getRelativePath(newUrl, nodes[rowIndex].getFile());                    
                    url.add(newUrl);
                }
                //shortPath = SvnUtils.getRelativePath(nodes[rowIndex].getFile());
                if (shortPath == null) {
                    shortPath = org.openide.util.NbBundle.getMessage(CommitTableModel.class, "CTL_CommitForm_NotInRepository"); // NOI18N
                }
            }
            return shortPath;
        }
        throw new IllegalArgumentException("Column index out of range: " + columnIndex); // NOI18N
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String col = columns[columnIndex];
        if (col.equals(COLUMN_NAME_ACTION)) {
            commitOptions[rowIndex] = (CommitOptions) aValue;
            fireTableCellUpdated(rowIndex, columnIndex);
        } else {
            throw new IllegalArgumentException("Column index out of range: " + columnIndex); // NOI18N
        }
    }

    private void defaultCommitOptions() {
        boolean excludeNew = System.getProperty("netbeans.subversion.excludeNewFiles") != null; // NOI18N
        commitOptions = new CommitOptions[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            SvnFileNode node = nodes[i];
            File file = node.getFile();
            if (SvnModuleConfig.getDefault().isExcludedFromCommit(file.getAbsolutePath())) {
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

    public SvnFileNode getNode(int row) {
        return nodes[row];
    }

    public CommitOptions getOptions(int row) {
        return commitOptions[row];
    }

    private CommitOptions getDefaultCommitOptions(File file) {
        if (file.isFile()) {
            InputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(file), 1024);
                long size = Math.min(1024, file.length());
                byte[] probe = new byte[(int)size];
                int read = in.read(probe);
                assert read == size;
                if (Subversion.getInstance().isBinary(probe)) {
                    return CommitOptions.ADD_BINARY;
                } else {
                    return CommitOptions.ADD_TEXT;
                }
            } catch (IOException ex) {
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        // closed
                    }
                }
            }
            return CommitOptions.ADD_TEXT;
        } else {
            return CommitOptions.ADD_DIRECTORY;
        }
    }

    private Set<SVNUrl> getRepositoryRoots() {
        if(repositoryRoots == null) {
            repositoryRoots = new HashSet<SVNUrl>();
        }
        return repositoryRoots;
    }

    void setRootFile(String repositoryPath, String rootLocalPath) {
        rootFile = new RootFile();
        rootFile.repositoryPath = repositoryPath;
        rootFile.rootLocalPath = rootLocalPath;
    }

}
