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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.saas.services.strikeiron.ui;

import com.strikeiron.search.AUTHENTICATIONSTYLE;
import com.strikeiron.search.SORTBY;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceData;
import org.netbeans.modules.websvc.saas.util.WsdlUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

/**
 *
 * @author  nam
 */
public class FindServiceUI extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;
    private ProgressHandle progressHandle;
    private JComponent progressComponent;
    private boolean jaxrpcWarned = false;
    private JButton bAdd;
    private JButton bCancel;
    
    /** Creates new form FindServiceUI */
    public FindServiceUI(JButton bAdd, JButton bCancel) {
        this.bAdd = bAdd;
        this.bCancel = bCancel;
        initComponents();
        cbAuthenticationMode.setSelectedItem(getModel().getAuthenticationStyle());
        cbSortBy.setSelectedItem(getModel().getSortBy());
        serviceSelectionTable.getColumnModel().getColumn(ServiceTableModel.COLUMN_WS_NAME).setMinWidth(220);
        serviceSelectionTable.getColumnModel().getColumn(ServiceTableModel.COLUMN_SELECT).setMinWidth(50);
        serviceSelectionTable.getColumnModel().getColumn(ServiceTableModel.COLUMN_SELECT).setResizable(false);
        bSearch.setEnabled(false);
        addButton.setEnabled(false);
        clearMessage();
        warnJaxRpc();
        addSearchListener();
        serviceSelectionTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int i = serviceSelectionTable.getSelectedRow();
                WsdlServiceData currentService = i < 0 ? null : getModel().getService(i);  
                getDetailPanel().setCurrentService(currentService);
                if (currentService != null && currentService.isInRepository()) {
                    showMessageByKey("MSG_AlreadyExists", true);
                } else {
                    clearMessage();
                }
            }
        });
    }
    
    public JButton getCancelButton() {
        return cancelButton;
    }
    
    public JButton getAddButton() {
        return addButton;
    }
    
    public Set<? extends WsdlServiceData> getSelectedServices() {
        return getModel().getSelectedServices();
    }
    
    ServiceTableModel getModel() {
        return (ServiceTableModel) serviceSelectionTable.getModel();
    }

    private void addSearchListener() {
        getModel().addEventListener(new ServiceTableModel.SearchListener() {

            public void searchCompleted(ChangeEvent e) {
                stopProgress();
                setCursor(null);
                tfSearch.setEnabled(true);
            }
            
            public void serviceSelectionChanged(ChangeEvent e) {
                if (getSelectedServices().size() > 0) {
                    addButton.setEnabled(true);
                }
            }
        });
    };

    public void startProgress() {
        progressHandle = ProgressHandleFactory.createHandle(null, new Cancellable() {
            public boolean cancel() {
                return getModel().cancelSearch();
            }
        });
        progressComponent = ProgressHandleFactory.createProgressComponent(progressHandle);
        progressContainerPanel.add(progressComponent, BorderLayout.CENTER);
        progressContainerPanel.setVisible(true);
        progressHandle.start();
        progressContainerPanel.revalidate();
        clearMessage();
    }

    public void stopProgress() {
        progressHandle.finish();
        progressContainerPanel.remove(progressComponent);
        progressContainerPanel.revalidate();
        // without this, the removed progress component remains painted on its parent... why?
        progressContainerPanel.repaint();
        showMessage(getModel().getStatusMessage(), getModel().hasWarnsOrErrors());

    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelDescription = new javax.swing.JLabel();
        tpTabs = new javax.swing.JTabbedPane();
        searchPanel = new javax.swing.JPanel();
        spTab = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        serviceSelectionTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        serviceInfoPanel = new ServiceDetailPanel();
        tfSearch = new javax.swing.JTextField();
        bSearch = new javax.swing.JButton();
        progressContainerPanel = new javax.swing.JPanel();
        settingsPanel = new javax.swing.JPanel();
        jlAuthenticationMode = new javax.swing.JLabel();
        jlSortBy = new javax.swing.JLabel();
        cbAuthenticationMode = new javax.swing.JComboBox();
        cbSortBy = new javax.swing.JComboBox();
        statusMessage = new javax.swing.JLabel();
        cancelButton = bCancel;
        addButton = bAdd;

        labelDescription.setText(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.labelDescription.text")); // NOI18N

        tpTabs.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tpTabs.setOpaque(true);
        tpTabs.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tpTabsStateChanged(evt);
            }
        });

        spTab.setBorder(null);
        spTab.setDividerLocation(275);
        spTab.setResizeWeight(0.5);
        spTab.setOneTouchExpandable(true);

        serviceSelectionTable.setModel(new ServiceTableModel());
        jScrollPane1.setViewportView(serviceSelectionTable);

        spTab.setLeftComponent(jScrollPane1);

        jScrollPane2.setViewportView(serviceInfoPanel);

        spTab.setRightComponent(jScrollPane2);

        tfSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfSearchKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfSearchonKeyTyped(evt);
            }
        });

        bSearch.setText(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.bSearch.text")); // NOI18N
        bSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSearchperformSearch(evt);
            }
        });

        progressContainerPanel.setMinimumSize(new java.awt.Dimension(20, 20));
        progressContainerPanel.setPreferredSize(new java.awt.Dimension(20, 20));
        progressContainerPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout searchPanelLayout = new org.jdesktop.layout.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(searchPanelLayout.createSequentialGroup()
                        .add(tfSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 216, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bSearch)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(progressContainerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, spTab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 744, Short.MAX_VALUE))
                .addContainerGap())
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(tfSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(progressContainerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spTab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                .addContainerGap())
        );

        tpTabs.addTab(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.searchPanel.TabConstraints.tabTitle"), searchPanel); // NOI18N

        jlAuthenticationMode.setText(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.jlAuthenticationMode.text")); // NOI18N

        jlSortBy.setText(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.jlSortBy.text")); // NOI18N

        cbAuthenticationMode.setModel(new javax.swing.DefaultComboBoxModel(AUTHENTICATIONSTYLE.values()));
        cbAuthenticationMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAuthenticationModeActionPerformed(evt);
            }
        });

        cbSortBy.setModel(new javax.swing.DefaultComboBoxModel(SORTBY.values()));
        cbSortBy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSortByActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout settingsPanelLayout = new org.jdesktop.layout.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jlAuthenticationMode)
                    .add(jlSortBy))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cbSortBy, 0, 607, Short.MAX_VALUE)
                    .add(cbAuthenticationMode, 0, 607, Short.MAX_VALUE))
                .addContainerGap())
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(settingsPanelLayout.createSequentialGroup()
                .add(16, 16, 16)
                .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlAuthenticationMode)
                    .add(cbAuthenticationMode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlSortBy)
                    .add(cbSortBy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(226, Short.MAX_VALUE))
        );

        tpTabs.addTab(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.settingsPanel.TabConstraints.tabTitle"), settingsPanel); // NOI18N

        statusMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/websvc/saas/services/strikeiron/resources/warning.png"))); // NOI18N
        statusMessage.setText("");

        cancelButton.setText(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.cancelButton.text")); // NOI18N
        cancelButton.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        addButton.setText(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.addButton.text")); // NOI18N
        addButton.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(labelDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 807, Short.MAX_VALUE)
                    .add(tpTabs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 807, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(statusMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE)
                        .add(8, 8, 8)
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(cancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(labelDescription)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tpTabs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(statusMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                    .add(cancelButton)
                    .add(addButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tfSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfSearchKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            startSearch();
        } else {
            bSearch.setEnabled(tfSearch.getText().length() > 0);
        }
    }//GEN-LAST:event_tfSearchKeyReleased

    private void tfSearchonKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfSearchonKeyTyped
        bSearch.setEnabled(tfSearch.getText().length() > 0);
    }//GEN-LAST:event_tfSearchonKeyTyped

    private void bSearchperformSearch(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSearchperformSearch
        startSearch();
    }//GEN-LAST:event_bSearchperformSearch

    private void cbAuthenticationModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAuthenticationModeActionPerformed
        getModel().setAuthenticationStyle((AUTHENTICATIONSTYLE)cbAuthenticationMode.getSelectedItem());
    }//GEN-LAST:event_cbAuthenticationModeActionPerformed

    private void cbSortByActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSortByActionPerformed
        getModel().setSortBy((SORTBY)cbSortBy.getSelectedItem());
    }//GEN-LAST:event_cbSortByActionPerformed

    private void tpTabsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tpTabsStateChanged
        if (settingsPanel.isShowing()) {
            addButton.setEnabled(false);
            clearMessage();
        } else {
            addButton.setEnabled(true);
        }
    }//GEN-LAST:event_tpTabsStateChanged
    
    private ServiceDetailPanel getDetailPanel() {
        return (ServiceDetailPanel) serviceInfoPanel;
    }
    
    private void startSearch() {
        getDetailPanel().clear();
        startProgress();
        setCursor(Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
        tfSearch.setEnabled(false);
        getModel().doSearch(tfSearch.getText());
    }

    private void clearMessage() {
        showMessage(null, false);
    }
    
    private void showMessageByKey(String key, boolean warnOrError) {
        String message = key == null ? "" : NbBundle.getMessage(FindServiceUI.class, key);
        showMessage(message, warnOrError);
    }

    private void showMessage(String message, boolean warnOrError) {
        if (warnOrError) {
            statusMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/org/netbeans/modules/websvc/saas/services/strikeiron/resources/warning.png"))); // NOI18N        
        } else {
            statusMessage.setIcon(null);
        }
        if (message != null) {
            statusMessage.setText("<html>"+message+"</html>"); //NOI18N
        } else {
            statusMessage.setText(""); //NOI18N
        }
    }

    private void warnJaxRpc() {
        if (! jaxrpcWarned) {
            jaxrpcWarned = true;
            if (! WsdlUtil.isJAXRPCAvailable())
            {
                showMessageByKey("WARNING_JAXRPC_UNAVAILABLE", true);
            }
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton bSearch;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox cbAuthenticationMode;
    private javax.swing.JComboBox cbSortBy;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel jlAuthenticationMode;
    private javax.swing.JLabel jlSortBy;
    private javax.swing.JLabel labelDescription;
    private javax.swing.JPanel progressContainerPanel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JTextPane serviceInfoPanel;
    private javax.swing.JTable serviceSelectionTable;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JSplitPane spTab;
    private javax.swing.JLabel statusMessage;
    private javax.swing.JTextField tfSearch;
    private javax.swing.JTabbedPane tpTabs;
    // End of variables declaration//GEN-END:variables
    
}
