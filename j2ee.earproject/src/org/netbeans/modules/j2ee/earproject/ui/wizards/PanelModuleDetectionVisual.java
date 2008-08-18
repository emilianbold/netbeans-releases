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

package org.netbeans.modules.j2ee.earproject.ui.wizards;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.earproject.ModuleType;
import org.netbeans.modules.j2ee.earproject.util.EarProjectUtil;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Martin Krauskopf
 */
public class PanelModuleDetectionVisual extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private final Vector<Vector<String>> modules = new Vector<Vector<String>>();
    private static final int REL_PATH_INDEX = 0;
    private static final int TYPE_INDEX = 1;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    // Location of Enterprise Application to be imported, chosen on the previous panel.
    private File eaLocation;
    
    public PanelModuleDetectionVisual() {
        initComponents();
        initModuleTable();
        // Provide a name in the title bar.
        setName(getMessage("LBL_IW_ApplicationModulesStep"));
        putClientProperty("NewProjectWizard_Title", getMessage("TXT_ImportProject"));
        getAccessibleContext().setAccessibleDescription(getMessage("ACS_NWP1_NamePanel_A11YDesc"));
    }
    
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    private void initModuleTable() {
        Vector<String> colNames = new Vector<String>();
        colNames.add(getMessage("LBL_IW_Module"));
        colNames.add(getMessage("LBL_IW_Type"));
        DefaultTableModel moduleTableModel = new DefaultTableModel(modules, colNames);
        moduleTable.setModel(moduleTableModel);
        TableColumnModel tcm = moduleTable.getColumnModel();
        TableColumn tc = tcm.getColumn(1);
        ModuleTypeRenderer renderer = new ModuleTypeRenderer();
        tc.setCellRenderer(renderer);
        tc.setCellEditor(new ModuleTypeEditor());
        moduleTable.setRowHeight((int) renderer.getPreferredSize().getHeight());
        moduleSP.getViewport().setBackground(moduleTable.getBackground());
    }
    
    void read(WizardDescriptor settings) {
        File newEALocation = (File) settings.getProperty(WizardProperties.SOURCE_ROOT);
        assert newEALocation != null : "Location is not available!";
        if (!newEALocation.equals(eaLocation)) {
            // reset all set of modules
            this.modules.removeAllElements();
        }
        eaLocation = newEALocation;
        FileObject eaLocationFO = FileUtil.toFileObject(eaLocation);
        Map<FileObject, ModuleType> modules = ModuleType.detectModules(eaLocationFO);
        for (FileObject moduleDir : modules.keySet()) {
            addModuleToTable(FileUtil.toFile(moduleDir));
        }
        getModuleTableModel().fireTableDataChanged();
    }
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        // #143772 - we need to check whether the directory is not already NB project, but NOT j2ee module
        for (Vector<String> module : modules) {
            String moduleDirectory = module.get(REL_PATH_INDEX);
            if (isForbiddenProject(moduleDirectory)) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(PanelModuleDetectionVisual.class, "MSG_ModuleNotJavaEEModule", moduleDirectory));
                return false;
            }
        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null); // NOI18N
        return true;
    }

    // return true for nb project which is not java ee module
    private boolean isForbiddenProject(String moduleDirectory) {
        File module = FileUtil.normalizeFile(new File(eaLocation, moduleDirectory));
        Project project = null;
        try {
            project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(module));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (project == null) {
            // not nb project at all
            return false;
        }
        return !EarProjectUtil.isJavaEEModule(project);
    }

    void store(WizardDescriptor wd) {
        Map<FileObject, ModuleType> userModules =
                new HashMap<FileObject, ModuleType>();
        for (Vector<String> module : modules) {
            String description = module.get(TYPE_INDEX);
            for (ModuleType type : ModuleType.values()) {
                if (type.getDescription().equals(description)) {
                    File moduleDir = new File(eaLocation, module.get(REL_PATH_INDEX));
                    FileObject moduleDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(moduleDir));
                    assert moduleDirFO != null;
                    userModules.put(moduleDirFO, type);
                    break;
                }
            }
        }
        wd.putProperty(WizardProperties.USER_MODULES, userModules);
    }
    
    private DefaultTableModel getModuleTableModel() {
        return (DefaultTableModel) moduleTable.getModel();
    }
    
    private void addModuleToTable(final File moduleF) {
        String relPath = PropertyUtils.relativizeFile(eaLocation, moduleF);
        for (Vector<String> module : modules) {
            if (relPath.equals(module.get(REL_PATH_INDEX))) {
                // already added
                return;
            }
        }
        Vector<String> row = new Vector<String>();
        row.add(relPath);
        row.add(getModuleType(relPath).getDescription());
        modules.add(row);
        changeSupport.fireChange();
    }
    
    private static final String getMessage(String bundleKey) {
        return NbBundle.getMessage(PanelModuleDetectionVisual.class, bundleKey);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        appModulesLabel = new javax.swing.JLabel();
        moduleSP = new javax.swing.JScrollPane();
        moduleTable = new javax.swing.JTable();
        addModuleButton = new javax.swing.JButton();
        removeModuleButton = new javax.swing.JButton();

        appModulesLabel.setLabelFor(moduleTable);
        org.openide.awt.Mnemonics.setLocalizedText(appModulesLabel, org.openide.util.NbBundle.getMessage(PanelModuleDetectionVisual.class, "LBL_IW_ApplicationModules"));
        appModulesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelModuleDetectionVisual.class, "ACSD_LBL_IW_ApplicationModules"));

        moduleSP.setViewportView(moduleTable);

        moduleSP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelModuleDetectionVisual.class, "ACSN_CTL_AppModules"));
        moduleSP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelModuleDetectionVisual.class, "ACSD_CTL_AppModules"));

        org.openide.awt.Mnemonics.setLocalizedText(addModuleButton, org.openide.util.NbBundle.getMessage(PanelModuleDetectionVisual.class, "LBL_IW_Add"));
        addModuleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addModuleButtonActionPerformed(evt);
            }
        });

        addModuleButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelModuleDetectionVisual.class, "ACSD_LBL_IW_Add"));

        org.openide.awt.Mnemonics.setLocalizedText(removeModuleButton, org.openide.util.NbBundle.getMessage(PanelModuleDetectionVisual.class, "LBL_IW_Remove"));
        removeModuleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeModuleButtonActionPerformed(evt);
            }
        });

        removeModuleButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelModuleDetectionVisual.class, "ACSD_LBL_IW_Remove"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(appModulesLabel)
                    .add(moduleSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(addModuleButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                    .add(removeModuleButton)))
        );

        layout.linkSize(new java.awt.Component[] {addModuleButton, removeModuleButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(appModulesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addModuleButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeModuleButton)
                        .addContainerGap())
                    .add(moduleSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void removeModuleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeModuleButtonActionPerformed
        int row = moduleTable.getSelectedRow();
        if (row != -1) {
            modules.remove(row);
            getModuleTableModel().fireTableRowsDeleted(row, row);
            changeSupport.fireChange();
        }
    }//GEN-LAST:event_removeModuleButtonActionPerformed
    
    private void addModuleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addModuleButtonActionPerformed
        JFileChooser chooser = new JFileChooser(eaLocation);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            if (eaLocation.equals(chooser.getSelectedFile())) {
                // XXX show some dialog to the user that Enterprise Application
                // itself cannot be added
                return;
            }
            addModuleToTable(chooser.getSelectedFile());
            getModuleTableModel().fireTableDataChanged();
        }
    }//GEN-LAST:event_addModuleButtonActionPerformed
    
    private ModuleType getModuleType(final String relPath) {
        ModuleType type = null;
        File dir = FileUtil.normalizeFile(new File(eaLocation, relPath));
        FileObject dirFO = FileUtil.toFileObject(dir);
        if (dirFO != null) {
            type = ModuleType.detectModuleType(dirFO);
        }
        return type == null ? ModuleType.WEB : type; // WEB is default if detection fails;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addModuleButton;
    private javax.swing.JLabel appModulesLabel;
    private javax.swing.JScrollPane moduleSP;
    private javax.swing.JTable moduleTable;
    private javax.swing.JButton removeModuleButton;
    // End of variables declaration//GEN-END:variables
    
    private static final class ModuleTypeRenderer extends JComboBox implements TableCellRenderer {
        private static final long serialVersionUID = 1L;
        
        ModuleTypeRenderer() {
            for (ModuleType type : ModuleType.values()) {
                addItem(type.getDescription());
            }
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            
            String moduleType = (String) value;
            setSelectedItem(moduleType);
            return this;
        }
        
    }
    
    private class ModuleTypeEditor extends JComboBox implements TableCellEditor {
        private static final long serialVersionUID = 1L;
        
        protected EventListenerList listenerList = new EventListenerList();
        protected ChangeEvent changeEvent = new ChangeEvent(this);
        
        ModuleTypeEditor() {
            for (ModuleType type : ModuleType.values()) {
                addItem(type.getDescription());
            }
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    fireEditingStopped();
                }
            });
        }
        
        public void addCellEditorListener(CellEditorListener listener) {
            listenerList.add(CellEditorListener.class, listener);
        }
        
        public void removeCellEditorListener(CellEditorListener listener) {
            listenerList.remove(CellEditorListener.class, listener);
        }
        
        protected void fireEditingStopped() {
            CellEditorListener listener;
            Object[] listeners = listenerList.getListenerList();
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] == CellEditorListener.class) {
                    listener = (CellEditorListener) listeners[i + 1];
                    listener.editingStopped(changeEvent);
                }
            }
        }
        
        protected void fireEditingCanceled() {
            CellEditorListener listener;
            Object[] listeners = listenerList.getListenerList();
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] == CellEditorListener.class) {
                    listener = (CellEditorListener) listeners[i + 1];
                    listener.editingCanceled(changeEvent);
                }
            }
        }
        
        public void cancelCellEditing() {
            fireEditingCanceled();
        }
        
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }
        
        public boolean isCellEditable(EventObject event) {
            return true;
        }
        
        public boolean shouldSelectCell(EventObject event) {
            return true;
        }
        
        public Object getCellEditorValue() {
            return getSelectedItem();
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            String moduleType = (String) value;
            setSelectedItem(moduleType);
            return this;
        }
        
    }
    
}
