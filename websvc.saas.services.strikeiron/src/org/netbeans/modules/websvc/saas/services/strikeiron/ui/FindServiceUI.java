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
import javax.swing.SwingUtilities;
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
        // Make some kind of attempt to size the table columns sensibly.
        int selectWidth = SwingUtilities.computeStringWidth(
                serviceSelectionTable.getFontMetrics(serviceSelectionTable.getFont()),
                serviceSelectionTable.getColumnName(ServiceTableModel.COLUMN_SELECT));
        // Compensate for font size variations, avoids ellipsis.
        selectWidth += (selectWidth / 2);
        serviceSelectionTable.getColumnModel().getColumn(
                ServiceTableModel.COLUMN_SELECT).setPreferredWidth(selectWidth);
        int nameWidth = spTab.getDividerLocation() - selectWidth;
        serviceSelectionTable.getColumnModel().getColumn(
                ServiceTableModel.COLUMN_WS_NAME).setPreferredWidth(nameWidth);
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
                if (getModel().getSelectedCount() > 0) {
                    addButton.setEnabled(true);
                } else {
                    addButton.setEnabled(false);
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
        selectionScrollPane = new javax.swing.JScrollPane();
        serviceSelectionTable = new javax.swing.JTable();
        detailScrollPane = new javax.swing.JScrollPane();
        serviceInfoPanel = new ServiceDetailPanel();
        tfSearch = new javax.swing.JTextField();
        bSearch = new javax.swing.JButton();
        progressContainerPanel = new javax.swing.JPanel();
        tableLabel = new javax.swing.JLabel();
        searchLabel = new javax.swing.JLabel();
        settingsPanel = new javax.swing.JPanel();
        jlAuthenticationMode = new javax.swing.JLabel();
        jlSortBy = new javax.swing.JLabel();
        cbAuthenticationMode = new javax.swing.JComboBox();
        cbSortBy = new javax.swing.JComboBox();
        statusMessage = new javax.swing.JLabel();
        cancelButton = bCancel;
        addButton = bAdd;

        labelDescription.setDisplayedMnemonic('W');
        labelDescription.setLabelFor(tpTabs);
        labelDescription.setText(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.labelDescription.text")); // NOI18N

        tpTabs.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
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
        selectionScrollPane.setViewportView(serviceSelectionTable);
        serviceSelectionTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.serviceSelectionTable.AccessibleContext.accessibleName")); // NOI18N
        serviceSelectionTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.serviceSelectionTable.AccessibleContext.accessibleDescription")); // NOI18N

        spTab.setLeftComponent(selectionScrollPane);
        selectionScrollPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.jScrollPane1.AccessibleContext.accessibleName")); // NOI18N
        selectionScrollPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.jScrollPane1.AccessibleContext.accessibleDescription")); // NOI18N

        detailScrollPane.setViewportView(serviceInfoPanel);
        serviceInfoPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.serviceInfoPanel.AccessibleContext.accessibleName")); // NOI18N

        spTab.setRightComponent(detailScrollPane);
        detailScrollPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.jScrollPane2.AccessibleContext.accessibleName")); // NOI18N
        detailScrollPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.jScrollPane2.AccessibleContext.accessibleDescription")); // NOI18N

        tfSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfSearchonKeyTyped(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfSearchKeyReleased(evt);
            }
        });

        bSearch.setMnemonic('S');
        bSearch.setText(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.bSearch.text")); // NOI18N
        bSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSearchperformSearch(evt);
            }
        });

        progressContainerPanel.setMinimumSize(new java.awt.Dimension(20, 20));
        progressContainerPanel.setPreferredSize(new java.awt.Dimension(20, 20));
        progressContainerPanel.setLayout(new java.awt.BorderLayout());

        tableLabel.setDisplayedMnemonic('r');
        tableLabel.setLabelFor(serviceSelectionTable);
        tableLabel.setText(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.tableLabel.text")); // NOI18N

        searchLabel.setDisplayedMnemonic('Q');
        searchLabel.setLabelFor(tfSearch);
        searchLabel.setText(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.searchLabel.text")); // NOI18N

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(searchLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(progressContainerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE))
                    .addComponent(spTab, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 776, Short.MAX_VALUE)
                    .addComponent(tableLabel))
                .addContainerGap())
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(tfSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bSearch)
                    .addComponent(progressContainerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spTab, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                .addContainerGap())
        );

        spTab.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.spTab.AccessibleContext.accessibleName")); // NOI18N
        spTab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.spTab.AccessibleContext.accessibleDescription")); // NOI18N
        tfSearch.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.tfSearch.AccessibleContext.accessibleName")); // NOI18N
        tfSearch.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.tfSearch.AccessibleContext.accessibleDescription")); // NOI18N
        bSearch.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.bSearch.AccessibleContext.accessibleDescription")); // NOI18N
        progressContainerPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.progressContainerPanel.AccessibleContext.accessibleName")); // NOI18N
        progressContainerPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.progressContainerPanel.AccessibleContext.accessibleDescription")); // NOI18N
        tableLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.tableLabel.AccessibleContext.accessibleDescription")); // NOI18N
        searchLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.searchLabel.AccessibleContext.accessibleDescription")); // NOI18N

        tpTabs.addTab(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.searchPanel.TabConstraints.tabTitle"), searchPanel); // NOI18N
        searchPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.searchPanel.AccessibleContext.accessibleName")); // NOI18N
        searchPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.searchPanel.AccessibleContext.accessibleDescription")); // NOI18N

        jlAuthenticationMode.setDisplayedMnemonic('M');
        jlAuthenticationMode.setLabelFor(cbAuthenticationMode);
        jlAuthenticationMode.setText(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.jlAuthenticationMode.text")); // NOI18N

        jlSortBy.setDisplayedMnemonic('B');
        jlSortBy.setLabelFor(cbSortBy);
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

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jlAuthenticationMode)
                    .addComponent(jlSortBy))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbSortBy, 0, 608, Short.MAX_VALUE)
                    .addComponent(cbAuthenticationMode, 0, 608, Short.MAX_VALUE))
                .addContainerGap())
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlAuthenticationMode)
                    .addComponent(cbAuthenticationMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlSortBy)
                    .addComponent(cbSortBy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(285, Short.MAX_VALUE))
        );

        jlAuthenticationMode.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.jlAuthenticationMode.AccessibleContext.accessibleDescription")); // NOI18N
        jlSortBy.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.jlSortBy.AccessibleContext.accessibleDescription")); // NOI18N
        cbAuthenticationMode.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.cbAuthenticationMode.AccessibleContext.accessibleName")); // NOI18N
        cbAuthenticationMode.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.cbAuthenticationMode.AccessibleContext.accessibleDescription")); // NOI18N
        cbSortBy.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.cbSortBy.AccessibleContext.accessibleName")); // NOI18N
        cbSortBy.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.cbSortBy.AccessibleContext.accessibleDescription")); // NOI18N

        tpTabs.addTab(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.settingsPanel.TabConstraints.tabTitle"), settingsPanel); // NOI18N
        settingsPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.settingsPanel.AccessibleContext.accessibleName")); // NOI18N
        settingsPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.settingsPanel.AccessibleContext.accessibleDescription")); // NOI18N

        statusMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/websvc/saas/services/strikeiron/resources/warning.png"))); // NOI18N
        statusMessage.setLabelFor(serviceInfoPanel);
        statusMessage.setText("");

        cancelButton.setMnemonic('C');
        cancelButton.setText(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.cancelButton.text")); // NOI18N
        cancelButton.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        addButton.setMnemonic('A');
        addButton.setText(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.addButton.text")); // NOI18N
        addButton.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 807, Short.MAX_VALUE)
                    .addComponent(tpTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 807, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(statusMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
                        .addGap(8, 8, 8)
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelDescription)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tpTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                    .addComponent(cancelButton)
                    .addComponent(addButton))
                .addContainerGap())
        );

        labelDescription.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.labelDescription.AccessibleContext.accessibleDescription")); // NOI18N
        tpTabs.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.tpTabs.AccessibleContext.accessibleName")); // NOI18N
        tpTabs.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.tpTabs.AccessibleContext.accessibleDescription")); // NOI18N
        statusMessage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.statusMessage.AccessibleContext.accessibleDescription")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.cancelButton.AccessibleContext.accessibleDescription")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.addButton.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindServiceUI.class, "FindServiceUI.AccessibleContext.accessibleDescription")); // NOI18N
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
            clearMessage();
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
    private javax.swing.JScrollPane detailScrollPane;
    private javax.swing.JLabel jlAuthenticationMode;
    private javax.swing.JLabel jlSortBy;
    private javax.swing.JLabel labelDescription;
    private javax.swing.JPanel progressContainerPanel;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JScrollPane selectionScrollPane;
    private javax.swing.JTextPane serviceInfoPanel;
    private javax.swing.JTable serviceSelectionTable;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JSplitPane spTab;
    private javax.swing.JLabel statusMessage;
    private javax.swing.JLabel tableLabel;
    private javax.swing.JTextField tfSearch;
    private javax.swing.JTabbedPane tpTabs;
    // End of variables declaration//GEN-END:variables
    
}
