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
import java.util.Collection;
import javax.swing.JPanel;
import java.util.List;
import java.util.StringTokenizer;
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
import org.openide.util.RequestProcessor;

/**
 *
 * @author  gordonp
 */
public class GdbAttachPanel extends JPanel implements ProcessListReader {
    
    private ProcessList procList;
    private final AttachTableModel processModel = new AttachTableModel();
    private final Controller controller;
    private static String lastFilterValue = "";
    private static boolean showAll = false;
    
    /** Creates new form GdbAttachPanel */
    public GdbAttachPanel() {
        controller = new GdbAttachController();
        initComponents();
        initHostCombo();
        procList = new ProcessList(getCurrentExecutionEnvironment());
        filterField.getDocument().addDocumentListener(new AnyChangeDocumentListener() {
            public void documentChanged(DocumentEvent e) {
                filterTextChanged();
            }
        });
        postComponentsInit();
    }

    private void initHostCombo() {
        Collection<? extends ServerRecord> records = ServerList.getRecords();
        for (ServerRecord serverRecord : records) {
            hostComboBox.addItem(serverRecord);
        }
    }

    private ExecutionEnvironment getCurrentExecutionEnvironment() {
        return ((ServerRecord)hostComboBox.getSelectedItem()).getExecutionEnvironment();
    }

    private synchronized void filterTextChanged() {
        lastFilterValue = filterField.getText();
        updateProcessList();
    }
    
    private void postComponentsInit() {
        for (AttachTableColumn hdr : procList.getColumnHeaders()) {
            processModel.addColumn(hdr);
        }
        setupCommandColumn();

        // Initialize the filter
        filterField.setText(lastFilterValue);
        allCheckBox.setSelected(showAll);
        // list will not be updated if text is empty
        if (lastFilterValue.length() == 0) {
            updateProcessList();
        }
        
        // Fill the Projects combo box
        fillProjectsCombo(projectCB);
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

    private void setupCommandColumn() {
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
    
    private void updateProcessList() {
        // Get the process list
        if (showAll) {
            procList.requestFull(this);
        } else {
            procList.requestSimple(this);
        }
    }

    Controller getController() {
        return controller;
    }

    /**
     * This callback should be called from a RequestProcessor thread. Once it computes
     * the row vectors, it needs to pass them to the model (Does it need to do this on
     * the AWT event thread?)
     * 
     * @param list A list of lines from a ps command
     */
    public void processListCallback(List<String> list) {
        Pattern re = getFilterRE();
        final Vector<Vector> rows = new Vector<Vector>();
        for (String line : list) {
            Vector<String> row = new Vector<String>();
            StringTokenizer tok = new StringTokenizer(line);
            while (tok.hasMoreTokens()) {
                row.add(tok.nextToken());
            }
            if (re == null || re.matcher(line).find()) {
                Vector<String> rowData = null;
                if (procList.isWindowsPsFound()) {
                    rowData = reorderWindowsProcLine(row);
                } else if (procList.isStd()) {
                    rowData = combineArgs(row);
                } else {
                    // too early, the process list is not yet ready
                }
                if (rowData != null) {
                    rows.add(rowData);
                }
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                processModel.setRowCount(0);
                for (Vector obj : rows) {
                    processModel.addRow(obj);
                }
            }
        });
    }
    
    private Vector<String> reorderWindowsProcLine(Vector<String> oldrow) {
        StringBuilder tmp = new StringBuilder();
        Vector<String> nurow = new Vector<String>(oldrow.size() - 2);
        String status = oldrow.get(0);
        String stime;
        int i;
        
        if (status.length() == 1 && (status.equals("I") ||  status.equals("C") || status.equals("O"))) { // NOI18N
            // The status field is optional...
            nurow.add(0, oldrow.get(6));  // UID
            nurow.add(1, oldrow.get(4));  // WINPID
            nurow.add(2, oldrow.get(1));  // PID
            nurow.add(3, oldrow.get(2));  // PPID
            nurow.add(4, oldrow.get(7));  // STIME
            stime = oldrow.get(7);
            if (Character.isDigit(stime.charAt(0))) {
                i = 8;
            } else {
                stime = stime + ' ' + oldrow.get(8);
                i = 9;
            }
            nurow.add(4, stime);  // STIME
            while (i < oldrow.size()) {
                tmp.append(oldrow.get(i++));
            }
        } else {
            nurow.add(0, oldrow.get(5));  // UID
            nurow.add(1, oldrow.get(3));  // WINPID
            nurow.add(2, oldrow.get(0));  // PID
            nurow.add(3, oldrow.get(1));  // PPID
            stime = oldrow.get(6);
            if (Character.isDigit(stime.charAt(0))) {
                i = 7;
            } else {
                stime = stime + ' ' + oldrow.get(7);
                i = 8;
            }
            nurow.add(4, stime);  // STIME
            while (i < oldrow.size()) {
                tmp.append(oldrow.get(i++));
            }
        }
        nurow.add(5, tmp.toString());  // CMD
        return nurow;
    }
    
    private Vector<String> combineArgs(Vector<String>oldrow) {
        Vector<String> nurow = new Vector<String>(oldrow.size());
        nurow.add(0, oldrow.get(0));
        nurow.add(1, oldrow.get(1));
        nurow.add(2, oldrow.get(2));
        nurow.add(3, oldrow.get(3));
        nurow.add(4, oldrow.get(4));
        StringBuilder args = new StringBuilder();
        for (int i = 5; i < oldrow.size(); i++) {
            args.append(oldrow.get(i) + ' ');
        }
        nurow.add(5, args.toString());
        return nurow;
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
        
        public boolean cancel() {
            return true;
        }

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

        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

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
        hostComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                hostComboBoxItemStateChanged(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getMessage(GdbAttachPanel.class, "GdbAttachPanel.jLabel1.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(projectLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(projectCB, 0, 502, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(allCheckBox)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(hostComboBox, 0, 519, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(filterLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filterField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(procLabel)
                .addContainerGap())
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hostComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(filterLabel)
                    .add(filterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(procLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(allCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectLabel)
                    .add(projectCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void allCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allCheckBoxActionPerformed
        showAll = allCheckBox.isSelected();
        updateProcessList();
    }//GEN-LAST:event_allCheckBoxActionPerformed

    private void hostComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_hostComboBoxItemStateChanged
        final ExecutionEnvironment exEnv = getCurrentExecutionEnvironment();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                procList = new ProcessList(exEnv);
                updateProcessList();
            }
        });
    }//GEN-LAST:event_hostComboBoxItemStateChanged
    
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

    public static abstract class AnyChangeDocumentListener implements DocumentListener {
        public abstract void documentChanged(DocumentEvent e);

        public void changedUpdate(DocumentEvent e) {
            documentChanged(e);
        }

        public void insertUpdate(DocumentEvent e) {
            documentChanged(e);
        }

        public void removeUpdate(DocumentEvent e) {
            documentChanged(e);
        }

    }

    class MyDevHostListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            ServerRecord rec = (ServerRecord) value;
            label.setText(rec.getServerDisplayName());
            return label;
        }
    }
}
