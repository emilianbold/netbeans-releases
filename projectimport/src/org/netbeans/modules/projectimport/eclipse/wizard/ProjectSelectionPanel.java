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

package org.netbeans.modules.projectimport.eclipse.wizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.projectimport.LoggerFactory;
import org.netbeans.modules.projectimport.ProjectImporterException;
import org.netbeans.modules.projectimport.eclipse.EclipseProject;
import org.netbeans.modules.projectimport.eclipse.Workspace;
import org.netbeans.modules.projectimport.eclipse.WorkspaceFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Represent "Project to import" step(panel) in the Eclipse importer wizard.
 *
 * @author  mkrauskopf
 */
final class ProjectSelectionPanel extends JPanel {
    
    /**
     * Logger for this class
     */
    private static final Logger logger =
            LoggerFactory.getDefault().createLogger(ProjectSelectionPanel.class);
    
    /** Rendererer for projects */
    private class ProjectCellRenderer extends JCheckBox
            implements TableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            EclipseProject project = projects[row];
            setColors(this, isSelected);
            setText(project.getName());
            setSelected(selectedProjects.contains(project) ||
                    requiredProjects.contains(project));
            setToolTipText(""); // NOI18N
            if (project.hasJavaNature() && !requiredProjects.contains(project)) {
                setEnabled(true);
            } else {
                // required and non-java project are disabled
                setEnabled(false);
                if (!project.hasJavaNature()) {
                    setToolTipText(ProjectImporterWizard.getMessage(
                            "MSG_NonJavaProject", project.getName())); // NOI18N
                }
            }
            return this;
        }
    }
    
    private void setColors(Component c, boolean isSelected) {
        c.setBackground(UIManager.getColor(isSelected ?
            "Table.selectionBackground" : "Table.background")); // NOI18N
        c.setForeground(UIManager.getColor(isSelected ?
            "Table.selectionForeground" : "Table.foreground")); // NOI18N
    }
    
    private class ProjectCellEditor extends AbstractCellEditor
            implements TableCellEditor {
        
        private JCheckBox checkBox;
        
        public Object getCellEditorValue() {
            return Boolean.valueOf(checkBox.isSelected());
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            EclipseProject project = projects[row];
            checkBox = new JCheckBox(project.getName(),
                    ((Boolean) value).booleanValue());
            setColors(checkBox, isSelected);
            checkBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    fireEditingStopped();
                }
            });
            return checkBox;
        }
        
        public boolean shouldSelectCell(java.util.EventObject anEvent) {
            return true;
        }
    }
    
    /** All projects in a workspace. */
    private EclipseProject[] projects;
    
    /**
     * Projects selected by user. So it counts the projects which were selected
     * by user and then became required (so became disabled). But project which
     * weren't checked but are required are not members of this set.
     * This all servers for remembering checked project when working with
     * project dependencies.
     */
    private Set selectedProjects;
    
    /**
     * All projects we need to import (involving projects which selected
     * projects depend on.
     */
    private Set requiredProjects;
    
    /** Error message displayed by wizard. */
    private String errorMessage;
    
    private class ProjectTableModel extends AbstractTableModel {
        public Object getValueAt(int rowIndex, int columnIndex) {
            EclipseProject project = projects[rowIndex];
            return Boolean.valueOf(selectedProjects.contains(project) ||
                    requiredProjects.contains(project));
        }
        
        public int getRowCount() {
            return projects != null ? projects.length : 0;
        }
        
        public int getColumnCount() {
            return 1;
        }
        
        public Class getColumnClass(int columnIndex) {
            return Boolean.class;
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (projects[rowIndex].hasJavaNature() &&
                    !requiredProjects.contains(projects[rowIndex]));
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            EclipseProject project = projects[rowIndex];
            if (((Boolean) aValue).booleanValue()) {
                selectedProjects.add(project);
            } else {
                selectedProjects.remove(project);
            }
            solveDependencies();
            fireTableDataChanged();
            projectTable.getSelectionModel().setLeadSelectionIndex(rowIndex);
            updateValidity();
        }
    }
    
    /** Updates panel validity. */
    public void updateValidity() {
        if (selectedProjects == null || selectedProjects.isEmpty()) {
            // user has to select at least one project
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_ProjectIsNotChosed")); // NOI18N
            return;
        }
        String parent = destination.getText();
        for (Iterator it = allProjects().iterator(); it.hasNext(); ) {
            EclipseProject prj = (EclipseProject) it.next();
            String destDir = parent + "/" + prj.getName();
            if (new File(destDir).exists()) {
                setErrorMessage(ProjectImporterWizard.getMessage(
                        "MSG_ProjectExist", prj.getName())); // NOI18N
                return;
            }
        }
        setErrorMessage(null);
    }
    
    /** Returns both selected and required projects */
    private Collection allProjects() {
        Collection all = new HashSet(selectedProjects);
        all.addAll(requiredProjects);
        return all;
    }
    
    // Helper for recursion check
    private Set solved;
    
    /**
     * Solves project dependencies. Fills up <code>requiredProjects</code> as
     * needed.
     */
    private void solveDependencies() {
        requiredProjects.clear();
        solved = new HashSet();
        fillUpRequiredProjects(selectedProjects);
        solved = null;
    }
    
    private void fillUpRequiredProjects(Set parentProjects) {
        if (parentProjects == null || parentProjects.isEmpty()) {
            return;
        }
        for (Iterator it = parentProjects.iterator(); it.hasNext(); ) {
            EclipseProject project = (EclipseProject) it.next();
            Set reqProjects = project.getProjects();
            requiredProjects.addAll(reqProjects);
            if (solved.contains(project)) {
                NotifyDescriptor d = new DialogDescriptor.Message(
                        ProjectImporterWizard.getMessage("MSG_CycleDependencies"), // NOI18N
                        NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                logger.warning("Cycle dependencies was detected. Project: " + project); // NOI18N
                return;
            }
            solved.add(project); // this one is as solved
            fillUpRequiredProjects(reqProjects);
        }
    }
    
    private ProjectTableModel model = new ProjectTableModel();
    private String workspaceDir;
    
    /** Creates new form ProjectSelectionPanel */
    public ProjectSelectionPanel() {
        initComponents();
        init();
        destination.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateValidity(); }
            public void removeUpdate(DocumentEvent e) { updateValidity(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        updateValidity();
    }
    
    private void init() {
        projectTable.setModel(model);
        projectTable.setTableHeader(null);
        projectTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectTable.setDefaultRenderer(Boolean.class, new ProjectCellRenderer());
        projectTable.setDefaultEditor(Boolean.class, new ProjectCellEditor());
        projectTableSP.getViewport().setBackground(projectTable.getBackground());
        destination.setText(System.getProperty("user.home")); // NOI18N
    }
    
    /** Loads project from workspace in the given <code>workspaceDir</code>. */
    void loadProjects(String workspaceDir) {
        Workspace workspace = null;
        try {
            workspace = WorkspaceFactory.getInstance().load(workspaceDir);
        } catch (ProjectImporterException e) {
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_WorkspaceIsInvalid", workspaceDir)); // NOI18N
            return;
        }
        Set wsPrjs = new TreeSet(workspace.getProjects());
        projects = new EclipseProject[wsPrjs.size()];
        int i = 0;
        for (Iterator it = wsPrjs.iterator(); it.hasNext(); ) {
            projects[i++] = (EclipseProject) it.next();
        }
        selectedProjects = new HashSet();
        requiredProjects = new HashSet();
        if (projects == null || projects.length == 0) {
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_WorkspaceIsEmpty", workspaceDir)); // NOI18N
        } else {
            updateValidity();
        }
    }
    
    /** Returns projects selected by selection panel */
    Set getProjects() {
        return selectedProjects;
    }
    
    /**
     * Returns number of projects which will be imported (including both
     * required and selected projects)
     */
    int getNumberOfImportedProject() {
        return allProjects().size();
    }
    
    /**
     * Returns destination directory where new NetBeans projects will be stored.
     */
    String getDestination() {
        return destination.getText();
    }
    
    void setErrorMessage(String newMessage) {
        String oldMessage = this.errorMessage;
        this.errorMessage = newMessage;
        firePropertyChange("errorMessage", oldMessage, newMessage); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        choosePanel = new javax.swing.JPanel();
        destination = new javax.swing.JTextField();
        chooseDestButton = new javax.swing.JButton();
        prjLocationLBL = new javax.swing.JLabel();
        projectPanel = new javax.swing.JPanel();
        projectListLabel = new javax.swing.JLabel();
        projectTableSP = new javax.swing.JScrollPane();
        projectTable = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout(0, 12));

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        choosePanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        choosePanel.add(destination, gridBagConstraints);

        chooseDestButton.setMnemonic(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "CTL_BrowseButton_Mnem").charAt(0));
        chooseDestButton.setText(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "CTL_BrowseButton"));
        chooseDestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseDestButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 0);
        choosePanel.add(chooseDestButton, gridBagConstraints);

        prjLocationLBL.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "LBL_LocationOfNBProjects_Mnem").charAt(0));
        prjLocationLBL.setLabelFor(destination);
        prjLocationLBL.setText(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "LBL_LocationOfNBProjects"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        choosePanel.add(prjLocationLBL, gridBagConstraints);

        add(choosePanel, java.awt.BorderLayout.SOUTH);

        projectPanel.setLayout(new java.awt.GridBagLayout());

        projectListLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "CTL_ProjectSelectionStep_Mnem").charAt(0));
        projectListLabel.setLabelFor(projectTable);
        projectListLabel.setText(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "LBL_ProjectsToImport"));
        projectListLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        projectPanel.add(projectListLabel, gridBagConstraints);

        projectTable.setShowHorizontalLines(false);
        projectTable.setShowVerticalLines(false);
        projectTableSP.setViewportView(projectTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        projectPanel.add(projectTableSP, gridBagConstraints);

        add(projectPanel, java.awt.BorderLayout.CENTER);

    }
    // </editor-fold>//GEN-END:initComponents
    
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
    private javax.swing.JLabel prjLocationLBL;
    private javax.swing.JLabel projectListLabel;
    private javax.swing.JPanel projectPanel;
    private javax.swing.JTable projectTable;
    private javax.swing.JScrollPane projectTableSP;
    // End of variables declaration//GEN-END:variables
    
}
