/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.eclipse.wizard;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JFileChooser;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.modules.projectimport.ProjectImporterException;
import org.netbeans.modules.projectimport.eclipse.EclipseProject;
import org.netbeans.modules.projectimport.eclipse.Workspace;
import org.netbeans.modules.projectimport.eclipse.WorkspaceFactory;
import org.openide.util.HelpCtx;

/**
 * Represent "Project to import" step(panel) in the Eclipse importer wizard.
 *
 * @author  mkrauskopf
 */
final class ProjectSelectionPanel extends ImporterWizardPanel {
    
    private static final Color NEEDED_PROJECT_COLOR = new Color(220, 220, 220);
    
    /** List of all workspace projects. */
    private EclipseProject[] projects;
    
    /** Projects selected by user. */
    private Set selectedProjects;
    
    /** Error message displayed by wizard. */
    private String errorMessage;
    
    /**
     * All projects we need to import (involving projects which selected
     * projects depend on.
     */
    private Set neededProjects;
    
    private class ProjectTableModel extends AbstractTableModel {
        static final int COLUMN_CHECKBOX = 0;
        static final int COLUMN_PROJECT = 1;
        static final int NUMBER_OF_COLUMNS = 2;
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            EclipseProject project = projects[rowIndex];
            if (columnIndex == COLUMN_CHECKBOX) {
                return Boolean.valueOf(selectedProjects.contains(project));
            } else {
                return (project.hasJavaNature() ?
                    project.getName() :
                    project.getName() + " (non-java project)");
            }
        }
        
        public int getRowCount() {
            return projects != null ? projects.length : 0;
        }
        
        public int getColumnCount() {
            return NUMBER_OF_COLUMNS;
        }
        
        public Class getColumnClass(int columnIndex) {
            return (columnIndex == COLUMN_CHECKBOX) ? Boolean.class : String.class;
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == COLUMN_CHECKBOX
                    && projects[rowIndex].hasJavaNature());
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == COLUMN_CHECKBOX) {
                EclipseProject project = projects[rowIndex];
                if (((Boolean) aValue).booleanValue()) {
                    selectedProjects.add(project);
                } else {
                    selectedProjects.remove(project);
                }
                solveDependencies();
                fireTableDataChanged();
                updateValidity();
            }
        }
    }
    
    /** Updates panel validity. */
    public void updateValidity() {
        if (selectedProjects == null || selectedProjects.isEmpty()) {
            // user has to select at least one project
            setErrorMessage("At least one project has to be chosen");
            return;
        }
        String parent = destination.getText();
        for (Iterator it = neededProjects.iterator(); it.hasNext(); ) {
            EclipseProject prj = (EclipseProject) it.next();
            String destDir = parent + "/" + prj.getName();
            if (new File(destDir).exists()) {
                setErrorMessage(destDir + " directory already exists.");
                return;
            }
        }
        setErrorMessage(null);
    }
    
    private class ProjectCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
                javax.swing.JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            EclipseProject project = projects[row];
            if (project.hasJavaNature()) {
                c.setForeground(Color.BLACK);
            } else {
                c.setForeground(Color.LIGHT_GRAY);
            }
            if (neededProjects.contains(project)) {
                c.setBackground(NEEDED_PROJECT_COLOR);
            } else {
                c.setBackground(Color.WHITE);
            }
            return c;
        }
    }
    
    private void solveDependencies() {
        neededProjects.clear();
        for (Iterator it = selectedProjects.iterator(); it.hasNext(); ) {
            EclipseProject project = (EclipseProject) it.next();
            neededProjects.add(project);
            neededProjects.addAll(project.getProjects());
        }
    }
    
    private static final int COL_CHECKBOX_WIDTH = 26;
    private ProjectTableModel model = new ProjectTableModel();
    private String workspaceDir;
    
    /** Creates new form ProjectSelectionPanel */
    public ProjectSelectionPanel() {
        super(1);
        initComponents();
        init();
        destination.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateValidity(); }
            public void removeUpdate(DocumentEvent e) { updateValidity(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        updateValidity();
        setPreferredSize(new java.awt.Dimension(500, 380));
    }
    
    private void init() {
        projectTable.setModel(model);
        projectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectTable.setTableHeader(null);
        projectTable.setSelectionBackground(projectTable.getSelectionBackground());
        projectTable.setSelectionForeground(projectTable.getSelectionForeground());
        projectTable.setDefaultRenderer(Object.class, new ProjectCellRenderer());
        TableColumn checkboxCol = projectTable.getColumnModel().getColumn(0);
        checkboxCol.setMinWidth(COL_CHECKBOX_WIDTH);
        checkboxCol.setMaxWidth(COL_CHECKBOX_WIDTH);
        
        ListSelectionModel rowSM = projectTable.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) return;
                
                ListSelectionModel lsm =
                        (ListSelectionModel)e.getSource();
                if (!lsm.isSelectionEmpty()) {
                    int selectedRow = lsm.getMinSelectionIndex();
                    Object o = projectTable.getValueAt(
                            projectTable.getSelectedRow(),
                            ProjectTableModel.COLUMN_PROJECT);
                }
            }
        });
        destination.setText(System.getProperty("user.home"));
    }
    
    /** Loads project from workspace in the given <code>workspaceDir</code>. */
    void loadProjects(String workspaceDir) {
        Workspace workspace = null;
        try {
            workspace = WorkspaceFactory.getInstance().load(workspaceDir);
        } catch (ProjectImporterException e) {
            setErrorMessage("The workspace in a " + workspaceDir + " is invalid");
            return;
        }
        Set wsPrjs = new TreeSet();
        wsPrjs.addAll(workspace.getProjects());
        projects = new EclipseProject[wsPrjs.size()];
        int i = 0;
        for (Iterator it = wsPrjs.iterator(); it.hasNext(); ) {
            projects[i++] = (EclipseProject) it.next();
        }
        selectedProjects = new HashSet();
        neededProjects = new HashSet();
        updateValidity();
    }
    
    /** Returns projects selected by selection panel */
    Set getProjects() {
        return selectedProjects;
    }
    
    /**
     * Return number of projects which will be imported (including required
     * projects.
     */
    int getNumberOfNeededProjects() {
        return neededProjects.size();
    }
    
    /**
     * Return destination directory where new NetBeans projects will be stored.
     */
    String getDestination() {
        return destination.getText();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        choosePanel = new javax.swing.JPanel();
        destination = new javax.swing.JTextField();
        chooseDestButton = new javax.swing.JButton();
        projectPanel = new javax.swing.JPanel();
        projectListLabel = new javax.swing.JLabel();
        projectTableSP = new javax.swing.JScrollPane();
        projectTable = new javax.swing.JTable();

        jLabel1.setText("jLabel1");

        setLayout(new java.awt.BorderLayout(5, 5));

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        choosePanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        choosePanel.add(destination, gridBagConstraints);

        chooseDestButton.setText("Choose Destination");
        chooseDestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseDestButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        choosePanel.add(chooseDestButton, gridBagConstraints);

        add(choosePanel, java.awt.BorderLayout.SOUTH);

        projectPanel.setLayout(new java.awt.GridBagLayout());

        projectListLabel.setText("Workspace projects:");
        projectListLabel.setAutoscrolls(true);
        projectListLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        projectPanel.add(projectListLabel, gridBagConstraints);

        projectTableSP.setViewportView(projectTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        projectPanel.add(projectTableSP, gridBagConstraints);

        add(projectPanel, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents
    
    private void chooseDestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseDestButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            destination.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_chooseDestButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton chooseDestButton;
    private javax.swing.JPanel choosePanel;
    private javax.swing.JTextField destination;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel projectListLabel;
    private javax.swing.JPanel projectPanel;
    private javax.swing.JTable projectTable;
    private javax.swing.JScrollPane projectTableSP;
    // End of variables declaration//GEN-END:variables
    
}
