/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dm.virtual.db.ui.wizard;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import org.openide.util.NbBundle;


public final class FileSelectionVisualPanel extends JPanel {


    private class InPropertyListener implements java.beans.PropertyChangeListener {

        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            owner.fireChangeEvent();
        }
    }
    private static File currDir;
    private boolean canAdvance = false;
    private FileSelectionPanel owner;

    public FileSelectionVisualPanel(FileSelectionPanel panel) {
        owner = panel;
        initComponents();
        addEntry.setMnemonic('A');
        removeButton.setMnemonic('R');
        requestFocus();
//        super.setSize(new Dimension(550,500));
//        super.setMaximumSize(new Dimension(550,500));
//        super.setMinimumSize(new Dimension(550,500));
//        super.setPreferredSize(new Dimension(550,500));
        fileChooser.addPropertyChangeListener(new InPropertyListener());
        disableCancelButton(fileChooser);
        fileChooser.setMaximumSize(new Dimension(500, 450));
        fileChooser.setPreferredSize(new Dimension(500, 450));
        fileChooser.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSelectionVisualPanel.class, "ACSD_FileChooser"));
        fileChooser.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FileSelectionVisualPanel.class, "ACSD_FileChooser"));

        url.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (url.getText().trim().length() == 0) {
                    addEntry.setEnabled(false);
                } else {
                    addEntry.setEnabled(true);
                }
            }
        });

        // set file filter
        FlatfileFilter ffFilter = new FlatfileFilter();
        ffFilter.setDescription(NbBundle.getMessage(FileSelectionVisualPanel.class, "ACSD_FlatfileFilter"));
        ffFilter.addType(".csv");
        ffFilter.addType(".txt");
        ffFilter.addType(".xls");
        ffFilter.addType(".dat");
        fileChooser.addChoosableFileFilter(ffFilter);
        //fileChooser.setAcceptAllFileFilterUsed(true);

        removeButton.setEnabled(false);
        addEntry.setEnabled(false);
        setTableColumnSize();

    }

    @Override
    public String getName() {
        return NbBundle.getMessage(FileSelectionVisualPanel.class, "TITLE_ChooseDataSource");
    }

    public boolean canAdvance() {
        return canAdvance;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileChooser = new javax.swing.JFileChooser();
        panel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        url = new javax.swing.JTextField();
        addEntry = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        removeButton = new javax.swing.JButton();
        error = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(624, 563));
        setMinimumSize(new java.awt.Dimension(624, 563));

        fileChooser.setApproveButtonMnemonic('d');
        fileChooser.setApproveButtonText(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_button_select"));
        fileChooser.setApproveButtonToolTipText(NbBundle.getMessage(FileSelectionVisualPanel.class, "TOOLTIP_FileChooserAddButton"));
        fileChooser.setDialogType(javax.swing.JFileChooser.CUSTOM_DIALOG);
        fileChooser.setToolTipText(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_FileChooserBorder"));
        fileChooser.setAutoscrolls(true);
        fileChooser.setBorder(javax.swing.BorderFactory.createTitledBorder(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_FileChooserBorder")));
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserActionPerformed(evt);
            }
        });

        jLabel3.setDisplayedMnemonic('U');
        jLabel3.setLabelFor(url);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_FileChooserURL"));

        org.openide.awt.Mnemonics.setLocalizedText(addEntry, NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_button_select"));
        addEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEntryActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout panel1Layout = new org.jdesktop.layout.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panel1Layout.createSequentialGroup()
                .add(jLabel3)
                .add(50, 50, 50)
                .add(url, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addEntry)
                .addContainerGap())
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(url, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(addEntry)
                .add(jLabel3))
        );

        jLabel3.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_FileChooserURL"));
        jLabel3.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_FileChooserURL"));
        url.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_FileChooserURL"));
        url.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_FileChooserURL"));
        addEntry.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_button_select"));

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_FileChooser1Border")));
        jScrollPane1.setMaximumSize(null);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "#", "Table Source"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        jScrollPane1.setViewportView(jTable1);
        jTable1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FileSelectionVisualPanel.class, "ACSD_FileChooser1"));
        jTable1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSelectionVisualPanel.class, "ACSD_FileChooser1"));

        removeButton.setMnemonic('r');
        org.openide.awt.Mnemonics.setLocalizedText(removeButton, NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_button_remove"));
        removeButton.setToolTipText(org.openide.util.NbBundle.getMessage(FileSelectionVisualPanel.class, "TOOLTIP_RemoveButton")); // NOI18N
        removeButton.setMaximumSize(null);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        error.setDisplayedMnemonic('E');
        error.setForeground(new java.awt.Color(255, 0, 51));
        error.setLabelFor(jTable1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap(220, Short.MAX_VALUE)
                        .add(error, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 209, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(8, 8, 8)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(fileChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 624, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(panel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileChooser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 326, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 178, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(error, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        fileChooser.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_FileChooserBorder"));
        fileChooser.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_FileChooserBorder"));
        panel1.getAccessibleContext().setAccessibleName("panel1");
        panel1.getAccessibleContext().setAccessibleDescription("panel1");
        jScrollPane1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_FileChooser1Border"));
        jScrollPane1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSelectionVisualPanel.class, "ACSD_FileChooserJScrollPane"));
        removeButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_button_remove"));
        removeButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_button_remove"));
        error.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FileSelectionVisualPanel.class, "ACSD_FileChooserError"));
        error.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSelectionVisualPanel.class, "ACSD_FileChooserError"));

        getAccessibleContext().setAccessibleName("form");
        getAccessibleContext().setAccessibleDescription("form");
    }// </editor-fold>//GEN-END:initComponents
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        removeFromModel();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEntryActionPerformed
        String urlAdd = url.getText().trim();
        addToModel(urlAdd);
    }//GEN-LAST:event_addEntryActionPerformed

    private void fileChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileChooserActionPerformed
        if (evt.getActionCommand().equalsIgnoreCase(JFileChooser.APPROVE_SELECTION)) {
            File files[] = fileChooser.getSelectedFiles();
            if ((files == null) || (files.length <= 0)) {
                error.setText(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_FileSelectionError"));
                return;
            }

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

            for (File file : files) {
                Object[] obj = new Object[2];
                obj[0] = model.getRowCount() + 1;
                obj[1] = file.getAbsolutePath();
                model.addRow(obj);
            }

            setTableModel(model);
            canAdvance = true;
            removeButton.setEnabled(true);
            setErrorText("");
            owner.fireChangeEvent();
        }
    }//GEN-LAST:event_fileChooserActionPerformed

    public void setErrorText(String string) {
        error.setText(string);
    }

    private void disableCancelButton(Component c) {
        if (c instanceof Container) {
            Component[] comps = ((Container) c).getComponents();
            for (int i = 0; i < comps.length; i++) {
                if (comps[i] instanceof JButton) {
                    if ("Cancel".equalsIgnoreCase(((JButton) comps[i]).getText())) {
                        ((Container) c).remove(comps[i]);
                        break;
                    }
                } else {
                    disableCancelButton(comps[i]);
                }
            }
        }
    }

    public void setCurrentDirectory(File newDir) {
        currDir = (newDir == null) ? new File(".") : newDir;
        fileChooser.setCurrentDirectory(currDir);
    }

    public DefaultTableModel getSelectedTables() {
        return (DefaultTableModel) jTable1.getModel();
    }

    private void addToModel(String urlAdd) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        Object[] obj = new Object[2];
        obj[0] = model.getRowCount() + 1;
        obj[1] = urlAdd;
        model.addRow(obj);
        setTableModel(model);
        canAdvance = true;
        removeButton.setEnabled(true);
        owner.fireChangeEvent();
        setErrorText("");
        url.setText("");
    }

    private void removeFromModel() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (jTable1.isRowSelected(i)) {
                model.removeRow(i);
            }
        }

        // adjust the sequence number
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(i + 1, i, 0);
        }

        setTableModel(model);
        if (model.getRowCount() == 0) {
            canAdvance = false;
            removeButton.setEnabled(false);
            setErrorText(NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_NoTableError"));
        }
        owner.fireChangeEvent();
    }

    private void setTableModel(final DefaultTableModel model) {
        Runnable run = new Runnable() {

            public void run() {
                Object[] obj = new Object[2];
                obj[0] = "#";
                obj[1] = NbBundle.getMessage(FileSelectionVisualPanel.class, "LBL_TableSource");
                model.setColumnIdentifiers(obj);
                jTable1.setModel(model);
                setTableColumnSize();
            }
        };
        SwingUtilities.invokeLater(run);
    }

    private void setTableColumnSize() {
        TableColumnModel tableColumnModel = jTable1.getColumnModel();
        tableColumnModel.getColumn(0).setMinWidth(5);
        tableColumnModel.getColumn(0).setMaxWidth(30);
        tableColumnModel.getColumn(0).setPreferredWidth(30);
    }

    public class FlatfileFilter extends javax.swing.filechooser.FileFilter {

        protected String description;
        protected ArrayList<String> exts = new ArrayList<String>();

        public void addType(String s) {
            exts.add(s);
        }

        /** Return true if the given file is accepted by this filter. */
        public boolean accept(File f) {
            // Little trick: if you don't do this, only directory names
            // ending in one of the extentions appear in the window.
            if (f.isDirectory()) {
                return true;
            } else if (f.isFile()) {
                for (String ext : exts) {
                    if (f.getName().endsWith(ext)) {
                        return true;
                    }
                }
            }
            // A file that didn't match, or a weirdo (e.g. UNIX device file?).
            return false;
        }

        /** Set the printable description of this filter. */
        public void setDescription(String s) {
            description = s;
        }

        /** Return the printable description of this filter. */
        public String getDescription() {
            return description;
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addEntry;
    private javax.swing.JLabel error;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel panel1;
    private javax.swing.JButton removeButton;
    private javax.swing.JTextField url;
    // End of variables declaration//GEN-END:variables
}

