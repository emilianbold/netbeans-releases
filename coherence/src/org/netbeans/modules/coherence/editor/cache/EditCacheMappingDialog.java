/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.editor.cache;

import org.netbeans.modules.coherence.xml.cache.CacheMapping;
import org.netbeans.modules.coherence.xml.cache.InitParam;
import org.netbeans.modules.coherence.xml.cache.InitParams;
import org.netbeans.modules.coherence.xml.cache.ParamName;
import org.netbeans.modules.coherence.xml.cache.ParamType;
import org.netbeans.modules.coherence.xml.cache.SchemeName;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class EditCacheMappingDialog extends javax.swing.JDialog implements ListSelectionListener {

    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;

    /** Creates new form EditCacheMappingDialog */
    public EditCacheMappingDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initialise();
    }
    /*
     * =========================================================================
     * START: Custom Code
     * =========================================================================
     */
    /*
     * Constructor
     */

    public EditCacheMappingDialog(java.awt.Frame parent, boolean modal, CacheMapping cacheMapping, List<String> schemeList) {
        this(parent, modal);
        this.cacheMapping = cacheMapping;
        this.schemeList = schemeList;
        initialise(cacheMapping);
    }

    /*
     * Inner Classes
     */
    public class InitParamsTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Name", "Value"};
        private List<Object[]> data = new ArrayList<Object[]>();
        private boolean[] edittable = {false, false, false};

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Object value = null;

            if (rowIndex < getRowCount() && columnIndex < getColumnCount()) {
                value = data.get(rowIndex)[columnIndex];
            }
            return value;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex < getRowCount() && columnIndex < getColumnCount()) {
                data.get(rowIndex)[columnIndex] = aValue;
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (getEdittable() == null || columnIndex > getEdittable().length) {
                return false;
            } else {
                return getEdittable()[columnIndex];
            }
        }

        public boolean[] getEdittable() {
            return edittable;
        }

        public void setEdittable(boolean[] edittable) {
            this.edittable = edittable;
        }

        public void addRow(String cacheName, String schemeName, Object node) {
            Object[] row = {cacheName, schemeName, node};
            data.add(row);
            fireTableDataChanged();
        }

        public void clear() {
            data.clear();
            fireTableDataChanged();
        }

        public void updateRow(int rowNum, String cacheName, String schemeName, InitParam initParam) {
            data.get(rowNum)[0] = cacheName;
            data.get(rowNum)[1] = schemeName;
            data.get(rowNum)[2] = initParam;
            fireTableDataChanged();
        }

        public Object getInitParam(int rowIndex) {
            Object value = null;

            if (rowIndex < getRowCount()) {
                value = data.get(rowIndex)[2];
            }
            return value;
        }

        public void removeRow(int rowIndex) {
            if (rowIndex < getRowCount()) {
                data.remove(rowIndex);
            }
            fireTableDataChanged();
        }

        public List<InitParam> getInitParams() {
            List<InitParam> initParamList = new ArrayList<InitParam>();
            for (Object[] o : data) {
                initParamList.add((InitParam) o[2]);
            }
            return initParamList;
        }
    }
    /*
     * Properties
     */
    private CacheMapping cacheMapping = null;
    private InitParamsTableModel initParamsTableModel = new InitParamsTableModel();
    private List<String> schemeList = null;
    /*
     * Methods
     */

    private void initialise() {
        tblInitParams.getSelectionModel().addListSelectionListener(this);
    }

    private void initialise(CacheMapping cacheMapping) {
        cbSchemeRef.setModel(getSchemaRefModel());
        if (cacheMapping != null) {
            tfCacheName.setText(cacheMapping.getCacheName());
            String schemeName = cacheMapping.getSchemeName().getvalue();
            cbSchemeRef.setSelectedItem(schemeName);
            InitParams ip = cacheMapping.getInitParams();
            if (ip != null) {
                for (InitParam param : ip.getInitParam()) {
                    List nameOrValueList = param.getParamNameOrParamType();
                    if (nameOrValueList.size() > 0) {
                        if (nameOrValueList.get(0) instanceof ParamName) {
                            initParamsTableModel.addRow(((ParamName) nameOrValueList.get(0)).getvalue(), param.getParamValue().getvalue(), param);
                        } else if (nameOrValueList.get(0) instanceof ParamType) {
                            initParamsTableModel.addRow(((ParamType) nameOrValueList.get(0)).getvalue(), param.getParamValue().getvalue(), param);
                        }
                    }
                }
            }
        }
    }

    public InitParamsTableModel getInitParamsTableModel() {
        return initParamsTableModel;
    }

    private ComboBoxModel getSchemaRefModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        if (getSchemeList() != null) {
            model = new DefaultComboBoxModel(getSchemeList().toArray());
        }
        return model;
    }

    public void setSchemeList(List<String> schemeList) {
        this.schemeList = schemeList;
        initialise(cacheMapping);
    }

    public List<String> getSchemeList() {
        return schemeList;
    }

    public CacheMapping getCacheMapping() {
        if (cacheMapping == null) {
            cacheMapping = new CacheMapping();
        }
        SchemeName schemeName = cacheMapping.getSchemeName();
        if (schemeName == null) {
            schemeName = new SchemeName();
        }
        cacheMapping.setCacheName(tfCacheName.getText());
        schemeName.setvalue(cbSchemeRef.getSelectedItem().toString());
        cacheMapping.setSchemeName(schemeName);
        InitParams initParams = cacheMapping.getInitParams();
        if (initParams == null) {
            initParams = new InitParams();
        }
        initParams.getInitParam().clear();
        initParams.getInitParam().addAll(initParamsTableModel.getInitParams());
        cacheMapping.setInitParams(initParams);
        return cacheMapping;
    }

    public void setCacheMapping(CacheMapping cacheMapping) {
        this.cacheMapping = cacheMapping;
        initialise(cacheMapping);
    }

    /*
     * Overrides
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        btnEdit.setEnabled(true);
        btnRemove.setEnabled(true);
    }
    /*
     * =========================================================================
     * END: Custom Code
     * =========================================================================
     */

    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        tfCacheName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        cbSchemeRef = new javax.swing.JComboBox();
        tablePanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblInitParams = new javax.swing.JTable();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okButton.setText(org.openide.util.NbBundle.getMessage(EditCacheMappingDialog.class, "EditCacheMappingDialog.okButton.text")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(org.openide.util.NbBundle.getMessage(EditCacheMappingDialog.class, "EditCacheMappingDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getMessage(EditCacheMappingDialog.class, "EditCacheMappingDialog.jLabel1.text")); // NOI18N

        tfCacheName.setText(org.openide.util.NbBundle.getMessage(EditCacheMappingDialog.class, "EditCacheMappingDialog.tfCacheName.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(EditCacheMappingDialog.class, "EditCacheMappingDialog.jLabel2.text")); // NOI18N

        cbSchemeRef.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        tablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(EditCacheMappingDialog.class, "EditCacheMappingDialog.tablePanel.border.title"))); // NOI18N

        btnAdd.setText(org.openide.util.NbBundle.getMessage(EditCacheMappingDialog.class, "EditCacheMappingDialog.btnAdd.text")); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnEdit.setText(org.openide.util.NbBundle.getMessage(EditCacheMappingDialog.class, "EditCacheMappingDialog.btnEdit.text")); // NOI18N
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnRemove.setText(org.openide.util.NbBundle.getMessage(EditCacheMappingDialog.class, "EditCacheMappingDialog.btnRemove.text")); // NOI18N
        btnRemove.setEnabled(false);
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(btnAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEdit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemove)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnEdit)
                    .addComponent(btnRemove))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tblInitParams.setModel(getInitParamsTableModel());
        jScrollPane1.setViewportView(tblInitParams);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout tablePanelLayout = new javax.swing.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(183, Short.MAX_VALUE))
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tablePanelLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tablePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbSchemeRef, 0, 229, Short.MAX_VALUE)
                                    .addComponent(tfCacheName, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfCacheName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cbSchemeRef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        doClose(RET_OK);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                EditInitParamDialog dialog = new EditInitParamDialog(null, true, null);
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
                if (dialog.getReturnStatus() == dialog.RET_OK) {
                    InitParam ip = dialog.getInitParam();
                    CacheMapping cacheMapping = getCacheMapping();
                    cacheMapping.getInitParams().getInitParam().add(ip);
                    List nameOrTypeList = ip.getParamNameOrParamType();
                    if (nameOrTypeList.size() > 0) {
                        if (nameOrTypeList.get(0) instanceof ParamName) {
                            initParamsTableModel.addRow(((ParamName) nameOrTypeList.get(0)).getvalue(), ip.getParamValue().getvalue(), ip);
                        } else if (nameOrTypeList.get(0) instanceof ParamType) {
                            initParamsTableModel.addRow(((ParamType) nameOrTypeList.get(0)).getvalue(), ip.getParamValue().getvalue(), ip);
                        }
                    }
                 }
            }
        });
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                int rowNum = tblInitParams.getSelectedRow();
                InitParam ip = (InitParam) initParamsTableModel.getInitParam(rowNum);
                EditInitParamDialog dialog = new EditInitParamDialog(null, true, ip);
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
                if (dialog.getReturnStatus() == dialog.RET_OK) {
                    ip = dialog.getInitParam();
                    CacheMapping cacheMapping = getCacheMapping();
//                    cacheMapping.getInitParams().getInitParam().add(ip);
                    List nameOrTypeList = ip.getParamNameOrParamType();
                    if (nameOrTypeList.size() > 0) {
                        if (nameOrTypeList.get(0) instanceof ParamName) {
                            initParamsTableModel.updateRow(rowNum, ((ParamName) nameOrTypeList.get(0)).getvalue(), ip.getParamValue().getvalue(), ip);
                        } else if (nameOrTypeList.get(0) instanceof ParamType) {
                            initParamsTableModel.updateRow(rowNum, ((ParamType) nameOrTypeList.get(0)).getvalue(), ip.getParamValue().getvalue(), ip);
                        }
                    }
                }
            }
        });
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                int option = JOptionPane.showConfirmDialog(tablePanel, "Please Confirm Initialisation Parameter Removal", "", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    int rowNum = tblInitParams.getSelectedRow();
                    InitParam cm = (InitParam) initParamsTableModel.getInitParam(rowNum);
                    initParamsTableModel.removeRow(rowNum);

                    CacheMapping cacheMapping = getCacheMapping();
                    cacheMapping.getInitParams().getInitParam().remove(cm);
                }
            }
        });
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                EditCacheMappingDialog dialog = new EditCacheMappingDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox cbSchemeRef;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTable tblInitParams;
    private javax.swing.JTextField tfCacheName;
    // End of variables declaration//GEN-END:variables
    private int returnStatus = RET_CANCEL;
}
