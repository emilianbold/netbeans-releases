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
package org.netbeans.modules.coherence.editor.cache.scheme;

import org.netbeans.modules.coherence.editor.cache.CacheConfigSchemeView;
import org.netbeans.modules.coherence.editor.cache.Scheme;
import org.netbeans.modules.coherence.editor.cache.SchemeModifiedListener;
import org.netbeans.modules.coherence.editor.cache.SchemeRemoveListener;
import org.netbeans.modules.coherence.editor.cache.storemanagers.AsyncStoreManagerPanel;
import org.netbeans.modules.coherence.editor.cache.storemanagers.BDBStoreManagerPanel;
import org.netbeans.modules.coherence.editor.cache.storemanagers.CustomStoreManagerPanel;
import org.netbeans.modules.coherence.editor.cache.storemanagers.LHFileManagerPanel;
import org.netbeans.modules.coherence.editor.cache.storemanagers.NIOFileManagerPanel;
import org.netbeans.modules.coherence.editor.cache.storemanagers.NIOMemoryManagerPanel;
import org.netbeans.modules.coherence.xml.cache.AsyncStoreManager;
import org.netbeans.modules.coherence.xml.cache.BdbStoreManager;
import org.netbeans.modules.coherence.xml.cache.CustomStoreManager;
import org.netbeans.modules.coherence.xml.cache.ExternalScheme;
import org.netbeans.modules.coherence.xml.cache.LhFileManager;
import org.netbeans.modules.coherence.xml.cache.NioFileManager;
import org.netbeans.modules.coherence.xml.cache.NioMemoryManager;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.Binding.SyncFailure;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.BindingListener;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Property;
import org.jdesktop.beansbinding.PropertyStateEvent;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class ExternalSchemePanel extends JPanel implements SchemePanelInterface, BindingListener {

    /** Creates new form DistributedSchemePanel */
    public ExternalSchemePanel() {
        initComponents();
        initialise();
    }
    /*
     * =========================================================================
     * START: Custom Code
     * =========================================================================
     */

    public ExternalSchemePanel(ExternalScheme scheme) {
        this();
        this.scheme = scheme;
        setPanelData(scheme);
    }
    /*
     * Properties
     */
    private static final String BUTTON_TEXT_PREFIX = "External Scheme";
    private ExternalScheme scheme = new ExternalScheme();
    private static final String[] SCHEME_TYPES = {"Async Store Manager", "Custom Store Manager", "LH Store Manager", "BDB Store Manager", "NIO File Manager", "NIO Memory Manager"};
    private BindingGroup bindingGroup = new BindingGroup();
    private AsyncStoreManager asyncStoreManager = null;
    private BdbStoreManager bdbStoreManager = null;
    private CustomStoreManager customStoreManager = null;
    private LhFileManager lhFileManager = null;
    private NioFileManager nioFileManager = null;
    private NioMemoryManager nioMemoryManager = null;
    private AsyncStoreManagerPanel asyncStoreManagerPanel = null;
    private BDBStoreManagerPanel bdbStoreManagerPanel = null;
    private CustomStoreManagerPanel customStoreManagerPanel = null;
    private LHFileManagerPanel lhFileManagerPanel = null;
    private NIOFileManagerPanel nioFileManagerPanel = null;
    private NIOMemoryManagerPanel nioMemoryManagerPanel = null;

    /*
     * Methods
     */
    private void initialise() {
        jPanel1.setVisible(jToggleButton1.isSelected());
    }

    private void setPanelData(ExternalScheme scheme) {
        cbSchemeTypes.setModel(getTypeModel());
        jToggleButton1.setText(BUTTON_TEXT_PREFIX.concat(" : ").concat(CacheConfigSchemeView.getSchemeName(scheme)));
        refreshBindings();
    }

    private ComboBoxModel getTypeModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model = new DefaultComboBoxModel(SCHEME_TYPES);
        return model;
    }

    private void refreshBindings() {
        for (Binding b : bindingGroup.getBindings()) {
            bindingGroup.removeBinding(b);
        }
        setupBindings();
    }

    private void setupBindings() {
        // Set Bindings
        Property propertyTextValue = BeanProperty.create("text");
        Property propertySelected = BeanProperty.create("selected");
        Property propertySelectedItem = BeanProperty.create("selectedItem");
        Property propertySelectedIndex = BeanProperty.create("selectedIndex");
        // SchemeName
        Property propertySchemeName = BeanProperty.create("schemeName");
        Binding bindingSchemeName = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertySchemeName, tfSchemeName, propertyTextValue);
        bindingGroup.addBinding(bindingSchemeName);
        // SchemeName
        Property propertyHighUnits = BeanProperty.create("highUnits");
        Binding bindingHighUnits = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, scheme, propertyHighUnits, tfHighUnits, propertyTextValue);
        bindingGroup.addBinding(bindingHighUnits);
        // Reference Scheme
        Property propertyManagerType = BeanProperty.create("managerType");
        Binding bindingManagerType = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyManagerType, cbSchemeTypes, propertySelectedIndex);
        bindingGroup.addBinding(bindingManagerType);
        // Bind All
        bindingGroup.addBindingListener(this);
        bindingGroup.bind();
    }

    /*
     * Binding Methods
     */
    // Getters & Setters Used for none simple binding
    public void setSchemeName(String name) {
        scheme.getSchemeName().setvalue(name);
    }

    public String getSchemeName() {
        if (scheme.getSchemeName() != null) {
            return scheme.getSchemeName().getvalue();
        } else {
            return null;
        }
    }

    public void setManagerType(int selected) {
    }

    public int getManagerType() {
        int i = 0;

        List<Object> storeMgrList = scheme.getAsyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager();
        for (Object o : storeMgrList) {
            if (o instanceof AsyncStoreManager) {
                i = 0;
            } else if (o instanceof CustomStoreManager) {
                i = 1;
            } else if (o instanceof LhFileManager) {
                i = 2;
            } else if (o instanceof BdbStoreManager) {
                i = 3;
            } else if (o instanceof NioFileManager) {
                i = 4;
            } else if (o instanceof NioMemoryManager) {
                i = 5;
            } else {
                i = 0;
            }
        }

        return i;
    }

    /*
     * Binding Overrides
     */

    @Override
    public void bindingBecameBound(Binding binding) {
    }

    @Override
    public void bindingBecameUnbound(Binding binding) {
    }

    @Override
    public void syncFailed(Binding binding, SyncFailure failure) {
    }

    @Override
    public void synced(Binding binding) {
    }

    @Override
    public void sourceChanged(Binding binding, PropertyStateEvent event) {
    }

    @Override
    public void targetChanged(Binding binding, PropertyStateEvent event) {
        fireSchemeModified(scheme);
    }

    /*
     * Overrides
     */
    @Override
    public void setScheme(Object o) {
        this.scheme = (ExternalScheme) o;
        setPanelData(this.scheme);
    }

    @Override
    public Object getScheme() {
        return scheme;
    }

    @Override
    public void setSchemeList(List<Scheme> schemeList) {
    }

    @Override
    public List<Scheme> getSchemeList() {
        return null;
    }

    @Override
    public String toString() {
        return BUTTON_TEXT_PREFIX;
    }

    @Override
    public void hideTitle() {
        jToggleButton1.setVisible(false);
        jToggleButton1.setSelected(true);
        jToggleButton1ActionPerformed(null);
        btnRemove.setVisible(false);
    }

    // Listener Code
    private List<SchemeRemoveListener> removeListeners = new ArrayList<SchemeRemoveListener>();

    @Override
    public void addSchemeRemoveListener(SchemeRemoveListener listener) {
        removeListeners.add(listener);
    }

    @Override
    public void removeSchemeRemoveListener(SchemeRemoveListener listener) {
        removeListeners.remove(listener);
    }

    @Override
    public void fireSchemeRemove(Object o) {
        for (SchemeRemoveListener listener : removeListeners) {
            listener.removeScheme(o);
        }
    }

    private List<SchemeModifiedListener> modifyListeners = new ArrayList<SchemeModifiedListener>();

    @Override
    public void addSchemeModifiedListener(SchemeModifiedListener listener) {
        modifyListeners.add(listener);
    }

    @Override
    public void removeSchemeModifiedListener(SchemeModifiedListener listener) {
        modifyListeners.remove(listener);
    }

    @Override
    public void fireSchemeModified(Object o) {
        for (SchemeModifiedListener listener : modifyListeners) {
            listener.modifyScheme(o);
        }
    }
    /*
     * =========================================================================
     * END: Custom Code
     * =========================================================================
     */
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToggleButton1 = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tfSchemeName = new javax.swing.JTextField();
        StoreMgrPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        cbSchemeTypes = new javax.swing.JComboBox();
        managerPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        tfHighUnits = new javax.swing.JTextField();
        btnRemove = new javax.swing.JButton();

        jToggleButton1.setText(org.openide.util.NbBundle.getMessage(ExternalSchemePanel.class, "ExternalSchemePanel.jToggleButton1.text")); // NOI18N
        jToggleButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getMessage(ExternalSchemePanel.class, "ExternalSchemePanel.jLabel1.text")); // NOI18N

        tfSchemeName.setText(org.openide.util.NbBundle.getMessage(ExternalSchemePanel.class, "ExternalSchemePanel.tfSchemeName.text")); // NOI18N

        StoreMgrPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ExternalSchemePanel.class, "ExternalSchemePanel.StoreMgrPanel.border.title"))); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(ExternalSchemePanel.class, "ExternalSchemePanel.jLabel3.text")); // NOI18N

        cbSchemeTypes.setModel(getTypeModel());
        cbSchemeTypes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSchemeTypesActionPerformed(evt);
            }
        });

        managerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ExternalSchemePanel.class, "ExternalSchemePanel.managerPanel.border.title"))); // NOI18N

        javax.swing.GroupLayout managerPanelLayout = new javax.swing.GroupLayout(managerPanel);
        managerPanel.setLayout(managerPanelLayout);
        managerPanelLayout.setHorizontalGroup(
            managerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 259, Short.MAX_VALUE)
        );
        managerPanelLayout.setVerticalGroup(
            managerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 36, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout StoreMgrPanelLayout = new javax.swing.GroupLayout(StoreMgrPanel);
        StoreMgrPanel.setLayout(StoreMgrPanelLayout);
        StoreMgrPanelLayout.setHorizontalGroup(
            StoreMgrPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(StoreMgrPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbSchemeTypes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(198, Short.MAX_VALUE))
            .addComponent(managerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        StoreMgrPanelLayout.setVerticalGroup(
            StoreMgrPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(StoreMgrPanelLayout.createSequentialGroup()
                .addGroup(StoreMgrPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cbSchemeTypes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(managerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setText(org.openide.util.NbBundle.getMessage(ExternalSchemePanel.class, "ExternalSchemePanel.jLabel2.text")); // NOI18N

        tfHighUnits.setText(org.openide.util.NbBundle.getMessage(ExternalSchemePanel.class, "ExternalSchemePanel.tfHighUnits.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(StoreMgrPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfHighUnits, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                            .addComponent(tfSchemeName, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfSchemeName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfHighUnits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(StoreMgrPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnRemove.setText(org.openide.util.NbBundle.getMessage(ExternalSchemePanel.class, "ExternalSchemePanel.btnRemove.text")); // NOI18N
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemove)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton1)
                    .addComponent(btnRemove))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        jPanel1.setVisible(jToggleButton1.isSelected());
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        fireSchemeRemove(scheme);
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void cbSchemeTypesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSchemeTypesActionPerformed
        int selected = cbSchemeTypes.getSelectedIndex();
        List<Object> managerList = scheme.getAsyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager();
        Object currentMgr = null;
        for (Object o : managerList) {
            currentMgr = o;
            break;
        }
        switch (selected) {
            case 0:
                if (currentMgr != null && currentMgr instanceof AsyncStoreManager) {
                    asyncStoreManager = (AsyncStoreManager) currentMgr;
                } else {
                    if (asyncStoreManager == null) asyncStoreManager = new AsyncStoreManager();
                    managerList.clear();
                    scheme.getAsyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager().add(asyncStoreManager);
                }
                if (asyncStoreManagerPanel == null) {
                    asyncStoreManagerPanel = new AsyncStoreManagerPanel(asyncStoreManager);
                } else {
                    asyncStoreManagerPanel.setManager(asyncStoreManager);
                }
                ((GroupLayout) StoreMgrPanel.getLayout()).replace(managerPanel, asyncStoreManagerPanel);
                managerPanel = asyncStoreManagerPanel;
                break;
            case 1:
                if (currentMgr != null && currentMgr instanceof CustomStoreManager) {
                    customStoreManager = (CustomStoreManager) currentMgr;
                } else {
                    if (customStoreManager == null) customStoreManager = new CustomStoreManager();
                    managerList.clear();
                    scheme.getAsyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager().add(customStoreManager);
                }
                if (customStoreManagerPanel == null) {
                    customStoreManagerPanel = new CustomStoreManagerPanel(customStoreManager);
                } else {
                    customStoreManagerPanel.setManager(customStoreManager);
                }
                ((GroupLayout) StoreMgrPanel.getLayout()).replace(managerPanel, customStoreManagerPanel);
                managerPanel = customStoreManagerPanel;
                break;
            case 2:
                if (currentMgr != null && currentMgr instanceof LhFileManager) {
                    lhFileManager = (LhFileManager) currentMgr;
                } else {
                    if (lhFileManager == null) lhFileManager = new LhFileManager();
                    managerList.clear();
                    scheme.getAsyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager().add(lhFileManager);
                }
                if (lhFileManagerPanel == null) {
                    lhFileManagerPanel = new LHFileManagerPanel(lhFileManager);
                } else {
                    lhFileManagerPanel.setManager(lhFileManager);
                }
                ((GroupLayout) StoreMgrPanel.getLayout()).replace(managerPanel, lhFileManagerPanel);
                managerPanel = lhFileManagerPanel;
                break;
            case 3:
                if (currentMgr != null && currentMgr instanceof BdbStoreManager) {
                    bdbStoreManager = (BdbStoreManager) currentMgr;
                } else {
                    if (bdbStoreManager == null) bdbStoreManager = new BdbStoreManager();
                    managerList.clear();
                    scheme.getAsyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager().add(bdbStoreManager);
                }
                if (bdbStoreManagerPanel == null) {
                    bdbStoreManagerPanel = new BDBStoreManagerPanel(bdbStoreManager);
                } else {
                    bdbStoreManagerPanel.setManager(bdbStoreManager);
                }
                ((GroupLayout) StoreMgrPanel.getLayout()).replace(managerPanel, bdbStoreManagerPanel);
                managerPanel = bdbStoreManagerPanel;
                break;
            case 4:
                if (currentMgr != null && currentMgr instanceof NioFileManager) {
                    nioFileManager = (NioFileManager) currentMgr;
                } else {
                    if (nioFileManager == null) nioFileManager = new NioFileManager();
                    managerList.clear();
                    scheme.getAsyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager().add(nioFileManager);
                }
                if (nioFileManagerPanel == null) {
                    nioFileManagerPanel = new NIOFileManagerPanel(nioFileManager);
                } else {
                    nioFileManagerPanel.setManager(nioFileManager);
                }
                ((GroupLayout) StoreMgrPanel.getLayout()).replace(managerPanel, nioFileManagerPanel);
                managerPanel = nioFileManagerPanel;
                break;
            case 5:
                if (currentMgr != null && currentMgr instanceof NioMemoryManager) {
                    nioMemoryManager = (NioMemoryManager) currentMgr;
                } else {
                    if (nioMemoryManager == null) nioMemoryManager = new NioMemoryManager();
                    managerList.clear();
                    scheme.getAsyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager().add(nioMemoryManager);
                }
                if (nioMemoryManagerPanel == null) {
                    nioMemoryManagerPanel = new NIOMemoryManagerPanel(nioMemoryManager);
                } else {
                    nioMemoryManagerPanel.setManager(nioMemoryManager);
                }
                ((GroupLayout) StoreMgrPanel.getLayout()).replace(managerPanel, nioMemoryManagerPanel);
                managerPanel = nioMemoryManagerPanel;
                break;
            case 6:
                break;
        }
    }//GEN-LAST:event_cbSchemeTypesActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel StoreMgrPanel;
    private javax.swing.JButton btnRemove;
    private javax.swing.JComboBox cbSchemeTypes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JPanel managerPanel;
    private javax.swing.JTextField tfHighUnits;
    private javax.swing.JTextField tfSchemeName;
    // End of variables declaration//GEN-END:variables
}
