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
import org.netbeans.modules.coherence.xml.cache.BackScheme;
import org.netbeans.modules.coherence.xml.cache.FrontScheme;
import org.netbeans.modules.coherence.xml.cache.LocalScheme;
import org.netbeans.modules.coherence.xml.cache.NearScheme;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
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
public class NearSchemePanel extends JPanel implements SchemePanelInterface, BindingListener {

    /** Creates new form DistributedSchemePanel */
    public NearSchemePanel() {
        initComponents();
        initialise();
    }
    /*
     * =========================================================================
     * START: Custom Code
     * =========================================================================
     */

    public NearSchemePanel(NearScheme scheme, List<Scheme> schemeNameList) {
        this();
        this.scheme = scheme;
        this.schemeList = schemeNameList;
        setPanelData(scheme);
    }
    /*
     * Properties
     */
    private static final String BUTTON_TEXT_PREFIX = "Near Scheme";
    private NearScheme scheme = new NearScheme();
    private List<Scheme> schemeList = null;
    private BindingGroup bindingGroup = new BindingGroup();
    private LocalScheme localScheme = new LocalScheme();
    /*
     * Methods
     */

    private void initialise() {
        jPanel1.setVisible(jToggleButton1.isSelected());
    }

    private void refreshBindings() {
        for (Binding b : bindingGroup.getBindings()) {
            bindingGroup.removeBinding(b);
        }
        setupBindings();
    }

    private void setPanelData(NearScheme scheme) {
        cbSchemeRef.setModel(getSchemaRefModel());
        jToggleButton1.setText(BUTTON_TEXT_PREFIX.concat(" : ").concat(CacheConfigSchemeView.getSchemeName(scheme)));
        if (scheme != null) {
            // Front Scheme
            FrontScheme frontScheme = scheme.getFrontScheme();
            if (frontScheme != null) {
                List frontSchemeList = frontScheme.getLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassScheme();
                for (Object o : frontSchemeList) {
                    if (o instanceof LocalScheme) {
                        localScheme = (LocalScheme)o;
                        break;
                    }
                }
            }
        }
        refreshBindings();
    }

    private ComboBoxModel getSchemaRefModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        if (getSchemeList() != null) {
            model = new DefaultComboBoxModel(getSchemeList().toArray());
        }
        return model;
    }

    private void setupBindings() {
        // Set Bindings
        Property propertyTextValue = BeanProperty.create("text");
        Property propertySelected = BeanProperty.create("selected");
        Property propertySelectedItem = BeanProperty.create("selectedItem");
        // SchemeName
        Property propertySchemeName = BeanProperty.create("schemeName");
        Binding bindingSchemeName = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertySchemeName, tfSchemeName, propertyTextValue);
        bindingGroup.addBinding(bindingSchemeName);
        // Service Name
        Property propertyInvalidationStrategy = BeanProperty.create("invalidationStrategy");
        Binding bindingServiceName = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, scheme, propertyInvalidationStrategy, tfInvalidationStrategy, propertyTextValue);
        bindingGroup.addBinding(bindingServiceName);
        // Autostart
        Property propertyAutostart = BeanProperty.create("autostart");
        Binding bindingAutostart = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyAutostart, cbAutostart, propertySelected);
        bindingGroup.addBinding(bindingAutostart);
        // Reference Scheme
        Property propertySchemeRef = BeanProperty.create("schemeRef");
        Binding bindingSchemeRef = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertySchemeRef, cbSchemeRef, propertySelectedItem);
        bindingGroup.addBinding(bindingSchemeRef);
        // EvictionPolicy
        Property propertyEvictionPolicy = BeanProperty.create("evictionPolicy");
        Binding bindingEvictionPolicy = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, localScheme, propertyEvictionPolicy, tfEvictionPolicy, propertyTextValue);
        bindingGroup.addBinding(bindingEvictionPolicy);
        // LowUnits
        Property propertyLowUnits = BeanProperty.create("lowUnits");
        Binding bindingLowUnits = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, localScheme, propertyLowUnits, tfLowUnits, propertyTextValue);
        bindingGroup.addBinding(bindingLowUnits);
        // HighUnits
        Property propertyHighUnits = BeanProperty.create("highUnits");
        Binding bindingHighUnits = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, localScheme, propertyHighUnits, tfHighUnits, propertyTextValue);
        bindingGroup.addBinding(bindingHighUnits);
        // ExpiryDelay
        Property propertyExpiryDelay = BeanProperty.create("expiryDelay");
        Binding bindingExpiryDelay = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, localScheme, propertyExpiryDelay, tfExpiryDelay, propertyTextValue);
        bindingGroup.addBinding(bindingExpiryDelay);
        // Bind All
        bindingGroup.addBindingListener(this);
        bindingGroup.bind();
    }

   /*
    * Binding Methods
    */
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

    public void setAutostart(boolean auto) {
        scheme.getAutostart().setvalue(Boolean.toString(auto));
    }

    public boolean getAutostart() {
        boolean auto = false;
        try {
            auto = Boolean.parseBoolean(scheme.getAutostart().getvalue());
        } catch (Exception e) {
        }
        return auto;
    }

    public void setSchemeRef(Scheme refScheme) {
        if (refScheme != null) {
            if (scheme.getBackScheme() == null)scheme.setBackScheme(new BackScheme());
            scheme.getBackScheme().getDistributedSchemeOrTransactionalSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrRemoteCacheScheme().clear();
            scheme.getBackScheme().getDistributedSchemeOrTransactionalSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrRemoteCacheScheme().add(refScheme);
        }
    }

    public Scheme getSchemeRef() {
        Scheme name = null;
        if (scheme != null && scheme.getBackScheme() != null) {
            List backingMapList = scheme.getBackScheme().getDistributedSchemeOrTransactionalSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrRemoteCacheScheme();
            for (Object o : backingMapList) {
                name = new Scheme(o);
                break;
            }
        }
        return name;
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
        this.scheme = (NearScheme) o;
        setPanelData(this.scheme);
    }

    @Override
    public Object getScheme() {
        return scheme;
    }

    @Override
    public void setSchemeList(List<Scheme> schemeList) {
        this.schemeList = schemeList;
        cbSchemeRef.setModel(getSchemaRefModel());
    }

    @Override
    public List<Scheme> getSchemeList() {
        return schemeList;
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
        backSchemePanel = new javax.swing.JPanel();
        cbSchemeRef = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        cbAutostart = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        tfEvictionPolicy = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        tfHighUnits = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        tfExpiryDelay = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        tfLowUnits = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        tfInvalidationStrategy = new javax.swing.JTextField();
        btnRemove = new javax.swing.JButton();

        jToggleButton1.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.jToggleButton1.text")); // NOI18N
        jToggleButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.jLabel1.text")); // NOI18N

        tfSchemeName.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.tfSchemeName.text")); // NOI18N

        backSchemePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.backSchemePanel.border.title"))); // NOI18N

        cbSchemeRef.setModel(getSchemaRefModel());

        jLabel3.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.jLabel3.text")); // NOI18N

        javax.swing.GroupLayout backSchemePanelLayout = new javax.swing.GroupLayout(backSchemePanel);
        backSchemePanel.setLayout(backSchemePanelLayout);
        backSchemePanelLayout.setHorizontalGroup(
            backSchemePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backSchemePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbSchemeRef, 0, 156, Short.MAX_VALUE)
                .addContainerGap())
        );
        backSchemePanelLayout.setVerticalGroup(
            backSchemePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backSchemePanelLayout.createSequentialGroup()
                .addGroup(backSchemePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cbSchemeRef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cbAutostart.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.cbAutostart.text")); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.jPanel3.border.title"))); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.jLabel4.text")); // NOI18N

        tfEvictionPolicy.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.tfEvictionPolicy.text")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.jLabel5.text")); // NOI18N

        tfHighUnits.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.tfHighUnits.text")); // NOI18N

        jLabel6.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.jLabel6.text")); // NOI18N

        tfExpiryDelay.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.tfExpiryDelay.text")); // NOI18N

        jLabel7.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.jLabel7.text")); // NOI18N

        tfLowUnits.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.tfLowUnits.text")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfEvictionPolicy, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                    .addComponent(tfExpiryDelay, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                    .addComponent(tfHighUnits, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                    .addComponent(tfLowUnits, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(tfEvictionPolicy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(tfLowUnits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(tfHighUnits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(tfExpiryDelay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.jLabel2.text")); // NOI18N

        tfInvalidationStrategy.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.tfInvalidationStrategy.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(tfSchemeName, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbAutostart))
                            .addComponent(tfInvalidationStrategy, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(backSchemePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfSchemeName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(cbAutostart))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfInvalidationStrategy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(backSchemePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btnRemove.setText(org.openide.util.NbBundle.getMessage(NearSchemePanel.class, "NearSchemePanel.btnRemove.text")); // NOI18N
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
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE))
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
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        jPanel1.setVisible(jToggleButton1.isSelected());
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        fireSchemeRemove(scheme);
    }//GEN-LAST:event_btnRemoveActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backSchemePanel;
    private javax.swing.JButton btnRemove;
    private javax.swing.JCheckBox cbAutostart;
    private javax.swing.JComboBox cbSchemeRef;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JTextField tfEvictionPolicy;
    private javax.swing.JTextField tfExpiryDelay;
    private javax.swing.JTextField tfHighUnits;
    private javax.swing.JTextField tfInvalidationStrategy;
    private javax.swing.JTextField tfLowUnits;
    private javax.swing.JTextField tfSchemeName;
    // End of variables declaration//GEN-END:variables
}
