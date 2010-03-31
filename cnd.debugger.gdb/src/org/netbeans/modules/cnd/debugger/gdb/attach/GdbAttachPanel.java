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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.gdb.attach;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.debugger.gdb.DebuggerStartException;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.actions.AttachTableColumn;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author  gordonp
 */
public class GdbAttachPanel extends JPanel implements ProcessListReader {
    
    private ProcessList procList = null;
    private final AttachTableModel processModel = new AttachTableModel();
    private Controller controller = null;
    private static String lastFilterValue = "";
    private static boolean showAll = false;
    private boolean updateColumns = true;
    
    /** Creates new form GdbAttachPanel */
    public GdbAttachPanel() {
        initComponents();
        postComponentsInit();
        updateProcessList(true);
    }

    private void postComponentsInit() {
        for (ServerRecord serverRecord : ServerList.getRecords()) {
            hostComboBox.addItem(serverRecord);
        }
        // Initialize the filter
        filterField.setText(lastFilterValue);
        allCheckBox.setSelected(showAll);

        // Fill the Projects combo box
        fillProjectsCombo(projectCB);
        hostComboBox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                hostComboBoxItemStateChanged(evt);
            }
        });
        filterField.getDocument().addDocumentListener(new AnyChangeDocumentListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
                filterTextChanged();
            }
        });
    }

    private ExecutionEnvironment getCurrentExecutionEnvironment() {
        return ((ServerRecord)hostComboBox.getSelectedItem()).getExecutionEnvironment();
    }

    private synchronized void filterTextChanged() {
        lastFilterValue = filterField.getText();
        updateProcessList(false);
    }
    
    private void setupColumns() {
        processModel.setColumnCount(0);
        for (AttachTableColumn hdr : procList.getColumnHeaders()) {
            processModel.addColumn(hdr);
        }
        // setup CommandColumn
        // last column should be the command column and needs to be enlarged
        //TableColumn tc = processTable.getColumn(procList.getArgsHeader());
        //tc.setPreferredWidth(300);
        //tc.setMinWidth(75);
        int size = processTable.getColumnModel().getColumnCount();
        for (int i = 0; i < size; i++) {
            TableColumn column = processTable.getColumnModel().getColumn(i);
            if (column.getIdentifier() != null && column.getIdentifier().equals(procList.getArgsHeader())) {
                column.setPreferredWidth(300);
                column.setMinWidth(75);
                break;
            }
        }
    }

    public static void fillProjectsCombo(JComboBox comboBox) {
        Project main = OpenProjects.getDefault().getMainProject();
        for (Project proj : OpenProjects.getDefault().getOpenProjects()) {
            // include only cnd projects (see IZ 164690)
            if (proj.getLookup().lookup(ConfigurationDescriptorProvider.class) != null) {
                ProjectInformation pinfo = ProjectUtils.getInformation(proj);
                ProjectCBItem pi = new ProjectCBItem(pinfo);
                comboBox.addItem(pi);
                if (main != null && proj == main) {
                    comboBox.setSelectedItem(pi);
                }
            }
        }
    }
    
    private void updateProcessList(boolean createPS) {
        if (createPS) {
            procList = new ProcessList(getCurrentExecutionEnvironment());
            updateColumns = true;
        }
        // Get the process list
        if (showAll) {
            procList.requestFull(getFilterRE(), this);
        } else {
            procList.requestSimple(getFilterRE(), this);
        }
    }

    Controller getController() {
        if (controller == null) {
            controller = new GdbAttachController();
        }
        return controller;
    }

    /**
     * This callback should be called from a RequestProcessor thread. Once it computes
     * the row vectors, it needs to pass them to the model (Does it need to do this on
     * the AWT event thread?)
     * 
     * @param list A list of lines from a ps command
     */
    @Override
    public void processListCallback(final List<Vector<String>> rows) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (updateColumns) {
                    setupColumns();
                    updateColumns = false;
                }
                processModel.setRowCount(0);
                for (Vector obj : rows) {
                    processModel.addRow(obj);
                }
            }
        });
    }
    
    private Pattern getFilterRE() {
        try {
            return Pattern.compile(lastFilterValue);
        } catch (PatternSyntaxException pse) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(NbBundle.getMessage(GdbAttachPanel.class,
                    "ERR_BadFilterPattern"))); // NOI18N
        }
        return null;
    }
    
    public static class ProjectCBItem {
        
        private ProjectInformation pinfo;
        
        public ProjectCBItem(ProjectInformation pinfo) {
            this.pinfo = pinfo;
        }
        
        @Override
        public String toString() {
            return pinfo.getDisplayName();
        }
        
        public Project getProject() {
            return pinfo.getProject();
        }
        
        public ProjectInformation getProjectInformation() {
            return pinfo;
        }
    }
    
    static class AttachTableModel extends DefaultTableModel {
        
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    private class GdbAttachController implements Controller {
        
        @Override
        public boolean cancel() {
            return true;
        }

        @Override
        public boolean ok() {
            int row = processTable.getSelectedRow();
            if (row >= 0) {
                String pid = processModel.getValueAt(row, 1).toString();
                ProjectCBItem pi = (ProjectCBItem) projectCB.getSelectedItem();
                if (pi != null) {
                    try {
                        GdbDebugger.attach(Long.valueOf(pid), pi.getProjectInformation(), getCurrentExecutionEnvironment());
                    } catch (DebuggerStartException dse) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                NbBundle.getMessage(GdbAttachPanel.class,
                               "ERR_UnexpecedAttachFailure", pid))); // NOI18N
                    }
                }
            }
            return true;
        }

        /**
         * Return <code>true</code> whether value of this customizer
         * is valid (and OK button can be enabled).
         *
         * @return <code>true</code> whether value of this customizer
         * is valid
         */
        @Override
        public boolean isValid() {
            return projectCB.getItemCount() > 0;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        processTable = new javax.swing.JTable();
        filterLabel = new javax.swing.JLabel();
        procLabel = new javax.swing.JLabel();
        projectLabel = new javax.swing.JLabel();
        projectCB = new javax.swing.JComboBox();
        allCheckBox = new javax.swing.JCheckBox();
        filterField = new javax.swing.JTextField();
        hostComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();

        processTable.setModel(processModel);
        processTable.setColumnSelectionAllowed(false);
        processTable.setRowSelectionAllowed(true);
        processTable.setShowVerticalLines(false);
        jScrollPane1.setViewportView(processTable);

        filterLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/attach/Bundle").getString("GdbAttachFilterMNEM").charAt(0));
        filterLabel.setText(org.openide.util.NbBundle.getMessage(GdbAttachPanel.class, "GdbAttachFilterLabel")); // NOI18N

        procLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/attach/Bundle").getString("GdbAttachProcessMNEM").charAt(0));
        procLabel.setLabelFor(jScrollPane1);
        procLabel.setText(org.openide.util.NbBundle.getMessage(GdbAttachPanel.class, "GdbAttachProcessLabel")); // NOI18N

        projectLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/attach/Bundle").getString("GdbAttachProjectMNEM").charAt(0));
        projectLabel.setLabelFor(projectCB);
        projectLabel.setText(org.openide.util.NbBundle.getMessage(GdbAttachPanel.class, "GdbAttachProjectLabel")); // NOI18N

        allCheckBox.setText(org.openide.util.NbBundle.getMessage(GdbAttachPanel.class, "GdbAttachPanel.allCheckBox.text")); // NOI18N
        allCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allCheckBoxActionPerformed(evt);
            }
        });

        hostComboBox.setRenderer(new MyDevHostListCellRenderer());

        jLabel1.setText(org.openide.util.NbBundle.getMessage(GdbAttachPanel.class, "GdbAttachPanel.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(projectLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(projectCB, 0, 532, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(allCheckBox)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hostComboBox, 0, 547, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(filterLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterField, javax.swing.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(procLabel)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterLabel)
                    .addComponent(filterField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(procLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(allCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLabel)
                    .addComponent(projectCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void allCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allCheckBoxActionPerformed
        showAll = allCheckBox.isSelected();
        updateProcessList(false);
    }//GEN-LAST:event_allCheckBoxActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox allCheckBox;
    private javax.swing.JTextField filterField;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JComboBox hostComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel procLabel;
    private javax.swing.JTable processTable;
    private javax.swing.JComboBox projectCB;
    private javax.swing.JLabel projectLabel;
    // End of variables declaration//GEN-END:variables

    private void hostComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {                                              
        updateProcessList(true);
    }             

    public static abstract class AnyChangeDocumentListener implements DocumentListener {
        public abstract void documentChanged(DocumentEvent e);

        @Override
        public void changedUpdate(DocumentEvent e) {
            documentChanged(e);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            documentChanged(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            documentChanged(e);
        }

    }

    static class MyDevHostListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            ServerRecord rec = (ServerRecord) value;
            label.setText(rec.getServerDisplayName());
            return label;
        }
    }
}
