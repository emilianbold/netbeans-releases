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

package org.netbeans.modules.projectimport.eclipse.core.wizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.Level;
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
import org.netbeans.modules.projectimport.eclipse.core.EclipseProject;
import org.netbeans.modules.projectimport.eclipse.core.ProjectImporterException;
import org.netbeans.modules.projectimport.eclipse.core.Workspace;
import org.netbeans.modules.projectimport.eclipse.core.WorkspaceFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Represent "Project to import" step(panel) in the Eclipse importer wizard.
 *
 * @author mkrauskopf
 */
final class ProjectSelectionPanel extends JPanel {
    
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(ProjectSelectionPanel.class.getName());

    /** Renderer for projects */
    private class ProjectCellRenderer extends JCheckBox
            implements TableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            EclipseProject project = projects[row];
            setColors(this, isSelected);
            setText(project.getName());
            setSelected(selectedProjects.contains(project) ||
                    requiredProjects.contains(project));
            setToolTipText(null);
            if (project.isImportSupported() && !requiredProjects.contains(project)) {
                setEnabled(true);
            } else {
                // required and non-java project are disabled
                setEnabled(false);
                if (!project.isImportSupported()) {
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
    private Set<EclipseProject> selectedProjects;
    
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
            return (projects[rowIndex].isImportSupported() &&
                    !requiredProjects.contains(projects[rowIndex]));
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            EclipseProject project = projects[rowIndex];
            assert projects != null;
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
        for (EclipseProject prj : allProjects()) {
            String destDir = parent + "/" + prj.getName(); // NOI18N
            if (new File(destDir).exists()) {
                setErrorMessage(ProjectImporterWizard.getMessage(
                        "MSG_ProjectExist", prj.getName())); // NOI18N
                return;
            }
        }
        setErrorMessage(null);
    }
    
    /** Returns both selected and required projects */
    private Collection<EclipseProject> allProjects() {
        Collection<EclipseProject> all = new HashSet<EclipseProject>(selectedProjects);
        all.addAll(requiredProjects);
        return all;
    }
    
    // Helper for recursion check
    private final Stack/*<EclipseProject>*/ solved = new Stack();
    private EclipseProject currentRoot;
    
    /**
     * Solves project dependencies. Fills up <code>requiredProjects</code> as
     * needed.
     */
    private void solveDependencies() {
        requiredProjects.clear();
        if (selectedProjects == null || selectedProjects.isEmpty()) {
            return;
        }
        for (EclipseProject selProject : selectedProjects) {
            assert selProject != null;
            solved.push(selProject);
            currentRoot = selProject;
            fillUpRequiredProjects(selProject);
            EclipseProject poped = (EclipseProject) solved.pop();
            assert poped.equals(currentRoot);
            assert solved.isEmpty();
            currentRoot = null;
        }
    }
    
    private void fillUpRequiredProjects(EclipseProject project) {
        Set children = project.getProjects();
        if (children == null || children.isEmpty()) {
            return;
        }
        for (Iterator it = children.iterator(); it.hasNext(); ) {
            EclipseProject child = (EclipseProject) it.next();
            assert child != null;
            if (solved.contains(child)) {
                recursionDetected(child);
                return;
            }
            requiredProjects.add(child);
            solved.push(child);
            fillUpRequiredProjects(child);
            EclipseProject poped = (EclipseProject) solved.pop();
            assert poped.equals(child);
        }
    }
    
    private void recursionDetected(EclipseProject start) {
        int where = solved.search(start);
        assert where != -1 : "Cannot find start of the cycle."; // NOI18N
        EclipseProject rootOfCycle =
                (EclipseProject) solved.get(solved.size() - where);
        StringBuffer cycle = new StringBuffer();
        for (Iterator it = solved.iterator(); it.hasNext(); ) {
            cycle.append(((EclipseProject)it.next()).getName()).append(" --> "); // NOI18N
        }
        cycle.append(rootOfCycle.getName()).append(" --> ..."); // NOI18N
        logger.warning("Cycle dependencies was detected. Detected cycle: " + cycle); // NOI18N
        NotifyDescriptor d = new DialogDescriptor.Message(
                ProjectImporterWizard.getMessage("MSG_CycleDependencies", cycle.toString()), // NOI18N
                NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
    }
    
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
        projectTable.setModel(new ProjectTableModel());
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
            logger.log(Level.FINE, "ProjectImporterException catched", e); // NOI18N
            return;
        }
        Set wsPrjs = new TreeSet(workspace.getProjects());
        projects = new EclipseProject[wsPrjs.size()];
        int i = 0;
        for (Iterator it = wsPrjs.iterator(); it.hasNext(); ) {
            projects[i++] = (EclipseProject) it.next();
        }
        selectedProjects = new HashSet<EclipseProject>();
        requiredProjects = new HashSet();
        if (projects.length == 0) {
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_WorkspaceIsEmpty", workspaceDir)); // NOI18N
        } else {
            updateValidity();
        }
    }
    
    /** Returns projects selected by selection panel and ordered so that required 
     *  projects are created first.
     */
    List<EclipseProject> getProjects() {
        List<EclipseProject> list = new ArrayList<EclipseProject>();
        addProjects(selectedProjects, list);
        return list;
    }
    
    private void addProjects(Set<EclipseProject> projects, List<EclipseProject> list) {
        for (EclipseProject p : projects) {
            if (list.contains(p)) {
                continue;
            }
            Set<EclipseProject> requiredProjs = p.getProjects();
            if (requiredProjs.size() == 0) {
                list.add(p);
            } else {
                addProjects(requiredProjs, list);
                list.add(p);
            }
        }
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

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        choosePanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        choosePanel.add(destination, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(chooseDestButton, org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "CTL_BrowseButton_B"));
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

        prjLocationLBL.setLabelFor(destination);
        org.openide.awt.Mnemonics.setLocalizedText(prjLocationLBL, org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "LBL_LocationOfNBProjects"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        choosePanel.add(prjLocationLBL, gridBagConstraints);

        add(choosePanel, java.awt.BorderLayout.SOUTH);

        projectPanel.setLayout(new java.awt.GridBagLayout());

        projectListLabel.setLabelFor(projectTable);
        org.openide.awt.Mnemonics.setLocalizedText(projectListLabel, org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "LBL_ProjectsToImport"));
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

    }// </editor-fold>//GEN-END:initComponents

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
