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
import org.netbeans.modules.coherence.xml.cache.ReadWriteBackingMapScheme;
import org.netbeans.modules.coherence.xml.cache.WriteDelay;
import org.netbeans.modules.coherence.xml.cache.WriteDelaySeconds;
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
public class ReadWriteBackingMapSchemePanel extends JPanel implements SchemePanelInterface, BindingListener {

    /** Creates new form DistributedSchemePanel */
    public ReadWriteBackingMapSchemePanel() {
        initComponents();
        initialise();
    }
    /*
     * =========================================================================
     * START: Custom Code
     * =========================================================================
     */

    public ReadWriteBackingMapSchemePanel(ReadWriteBackingMapScheme scheme, List<Scheme> schemeNameList) {
        this();
        this.scheme = scheme;
        this.schemeList = schemeNameList;
        setPanelData(scheme);
    }
    /*
     * Properties
     */
    private static final String BUTTON_TEXT_PREFIX = "Read / Write Backing Map Scheme";
    private ReadWriteBackingMapScheme scheme = new ReadWriteBackingMapScheme();
    private List<Scheme> schemeList = null;
    private BindingGroup bindingGroup = new BindingGroup();
    /*
     * Methods
     */

    private void initialise() {
        jPanel1.setVisible(jToggleButton1.isSelected());
    }

    private void setPanelData(ReadWriteBackingMapScheme scheme) {
        cbSchemeRef.setModel(getSchemaRefModel());
        jToggleButton1.setText(BUTTON_TEXT_PREFIX.concat(" : ").concat(CacheConfigSchemeView.getSchemeName(scheme)));
        refreshBindings();

//        if (scheme != null) {
//            jToggleButton1.setText(BUTTON_TEXT_PREFIX.concat(scheme.getSchemeName().getvalue()));
//            tfSchemeName.setText(scheme.getSchemeName().getvalue());
//            List writeDelayList = scheme.getWriteDelayOrWriteDelaySeconds();
//            WriteDelay writeDelay = null;
//            WriteDelaySeconds writeDelaySeconds = null;
//            for (Object o : writeDelayList) {
//                if (o instanceof WriteDelay) {
//                    writeDelay = (WriteDelay) o;
//                    tfWriteDelay.setText(writeDelay.getvalue());
//                } else if (o instanceof WriteDelaySeconds) {
//                    writeDelaySeconds = (WriteDelaySeconds) o;
//                    tfWriteDelay.setText(writeDelaySeconds.getvalue());
//                }
//            }
//
//            try {
//                cbReadonly.setSelected(Boolean.parseBoolean(scheme.getReadOnly()));
//            } catch (Exception e) {
//                cbReadonly.setSelected(false);
//            }
//
//            // Set Selected Reference
//            List backingMapList = scheme.getInternalCacheScheme().getLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassScheme();
//            for (Object o : backingMapList) {
//                cbSchemeRef.setSelectedItem(CacheConfigSchemeView.getSchemeRefName(o));
//            }
//        }
    }

    private ComboBoxModel getSchemaRefModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        if (getSchemeList() != null) {
            model = new DefaultComboBoxModel(getSchemeList().toArray());
        }
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
        // SchemeName
        Property propertySchemeName = BeanProperty.create("schemeName");
        Binding bindingSchemeName = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertySchemeName, tfSchemeName, propertyTextValue);
        bindingGroup.addBinding(bindingSchemeName);
        // Write Delay
        Property propertyWriteDelay = BeanProperty.create("writeDelay");
        Binding bindingWriteDelay = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyWriteDelay, tfWriteDelay, propertyTextValue);
        bindingGroup.addBinding(bindingWriteDelay);
        // Autostart
        Property propertyAutostart = BeanProperty.create("readOnly");
        Binding bindingAutostart = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyAutostart, cbReadonly, propertySelected);
        bindingGroup.addBinding(bindingAutostart);
        // Reference Scheme
        Property propertySchemeRef = BeanProperty.create("schemeRef");
        Binding bindingSchemeRef = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertySchemeRef, cbSchemeRef, propertySelectedItem);
        bindingGroup.addBinding(bindingSchemeRef);
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

    public void setReadOnly(boolean auto) {
        scheme.setReadOnly(Boolean.toString(auto));
    }

    public boolean getReadOnly() {
        boolean auto = false;
        try {
            auto = Boolean.parseBoolean(scheme.getReadOnly());
        } catch (Exception e) {
        }
        return auto;
    }

    public void setSchemeRef(Scheme refScheme) {
        if (refScheme != null) {
            scheme.getInternalCacheScheme().getLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassScheme().clear();
            scheme.getInternalCacheScheme().getLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassScheme().add(refScheme);
        }
    }

    public Scheme getSchemeRef() {
        Scheme name = null;
        if (scheme != null && scheme.getInternalCacheScheme() != null) {
            List backingMapList = scheme.getInternalCacheScheme().getLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassScheme();
            for (Object o : backingMapList) {
                name = new Scheme(o);
                break;
            }
        }
        return name;
    }

    public void setWriteDelay(String s) {
        List writeDelayList = scheme.getWriteDelayOrWriteDelaySeconds();
        WriteDelay writeDelay = null;
        WriteDelaySeconds writeDelaySeconds = null;
        for (Object o : writeDelayList) {
            if (o instanceof WriteDelay) {
                writeDelay = (WriteDelay) o;
                writeDelay.setvalue(s);
            } else if (o instanceof WriteDelaySeconds) {
                writeDelaySeconds = (WriteDelaySeconds) o;
                writeDelaySeconds.setvalue(s);
            }
            break;
        }
    }

    public String getWriteDelay() {
        String s = null;

        List writeDelayList = scheme.getWriteDelayOrWriteDelaySeconds();
        WriteDelay writeDelay = null;
        WriteDelaySeconds writeDelaySeconds = null;
        for (Object o : writeDelayList) {
            if (o instanceof WriteDelay) {
                writeDelay = (WriteDelay) o;
                s = writeDelay.getvalue();
            } else if (o instanceof WriteDelaySeconds) {
                writeDelaySeconds = (WriteDelaySeconds) o;
                s = writeDelaySeconds.getvalue();
            }
            break;
        }

        return s;
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
        this.scheme = (ReadWriteBackingMapScheme) o;
        setPanelData(this.scheme);
    }

    @Override
    public Object getScheme() {
        return scheme;
    }

    @Override
    public void setSchemeList(List<Scheme> schemeList) {
        this.schemeList = schemeList;
        setPanelData(this.scheme);
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
        jLabel2 = new javax.swing.JLabel();
        tfSchemeName = new javax.swing.JTextField();
        tfWriteDelay = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        cbSchemeRef = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        cbReadonly = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        btnRemove = new javax.swing.JButton();

        jToggleButton1.setText(org.openide.util.NbBundle.getMessage(ReadWriteBackingMapSchemePanel.class, "ReadWriteBackingMapSchemePanel.jToggleButton1.text")); // NOI18N
        jToggleButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jLabel2.setText(org.openide.util.NbBundle.getMessage(ReadWriteBackingMapSchemePanel.class, "ReadWriteBackingMapSchemePanel.jLabel2.text")); // NOI18N

        tfSchemeName.setText(org.openide.util.NbBundle.getMessage(ReadWriteBackingMapSchemePanel.class, "ReadWriteBackingMapSchemePanel.tfSchemeName.text")); // NOI18N

        tfWriteDelay.setText(org.openide.util.NbBundle.getMessage(ReadWriteBackingMapSchemePanel.class, "ReadWriteBackingMapSchemePanel.tfWriteDelay.text")); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ReadWriteBackingMapSchemePanel.class, "ReadWriteBackingMapSchemePanel.jPanel2.border.title"))); // NOI18N

        cbSchemeRef.setModel(getSchemaRefModel());

        jLabel3.setText(org.openide.util.NbBundle.getMessage(ReadWriteBackingMapSchemePanel.class, "ReadWriteBackingMapSchemePanel.jLabel3.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbSchemeRef, 0, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cbSchemeRef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cbReadonly.setText(org.openide.util.NbBundle.getMessage(ReadWriteBackingMapSchemePanel.class, "ReadWriteBackingMapSchemePanel.cbReadonly.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(ReadWriteBackingMapSchemePanel.class, "ReadWriteBackingMapSchemePanel.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfWriteDelay, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(tfSchemeName, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbReadonly))))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfSchemeName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbReadonly))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfWriteDelay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnRemove.setText(org.openide.util.NbBundle.getMessage(ReadWriteBackingMapSchemePanel.class, "ReadWriteBackingMapSchemePanel.btnRemove.text")); // NOI18N
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
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE))
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
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        jPanel1.setVisible(jToggleButton1.isSelected());
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        fireSchemeRemove(scheme);
    }//GEN-LAST:event_btnRemoveActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRemove;
    private javax.swing.JCheckBox cbReadonly;
    private javax.swing.JComboBox cbSchemeRef;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JTextField tfSchemeName;
    private javax.swing.JTextField tfWriteDelay;
    // End of variables declaration//GEN-END:variables

}
